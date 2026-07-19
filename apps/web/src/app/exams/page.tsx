"use client";

import React, { useEffect, useState, useRef } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navbar } from "@/components/Navbar";
import { Clock, Play, AlertOctagon, HelpCircle, Check, ShieldAlert, Award, FileText, ChevronRight } from "lucide-react";

interface ExamCandidate {
    id: number;
    examId: number;
    examTitle: string;
    examDescription: string;
    startedAt: string | null;
    completedAt: string | null;
    score: number | null;
    status: "INVITED" | "STARTED" | "COMPLETED";
    durationMinutes: number;
    startTime: string;
    endTime: string;
}

interface ExamDetail {
    id: number;
    title: string;
    description: string;
    durationMinutes: number;
    startTime: string;
    endTime: string;
    sections: {
        id: number;
        title: string;
        description: string;
        questions: {
            id: number;
            title: string;
            description: string;
            difficulty: string;
            marks: number;
            type: "CODING" | "MCQ";
            options?: { id: string; text: string }[];
            isMultipleChoice?: boolean;
        }[];
    }[];
}

export default function ExamsPortalPage() {
    const { token } = useAuth();
    const [enrollments, setEnrollments] = useState<ExamCandidate[]>([]);
    const [loading, setLoading] = useState(true);
    
    // Exam Attempt Lifecycle State
    const [activeLobbyEnrollment, setActiveLobbyEnrollment] = useState<ExamCandidate | null>(null);
    const [activeExamDetail, setActiveExamDetail] = useState<ExamDetail | null>(null);
    const [examStarted, setExamStarted] = useState(false);
    const [timeLeftSeconds, setTimeLeftSeconds] = useState(0);
    
    // Selected question in active exam
    const [activeQuestionIdx, setActiveQuestionIdx] = useState<{sectionIdx: number, questionIdx: number} | null>(null);
    const [sourceCodes, setSourceCodes] = useState<{[key: number]: string}>({});
    const [selectedOptions, setSelectedOptions] = useState<{[key: number]: string[]}>({});
    const [submissionResults, setSubmissionResults] = useState<{[key: number]: string}>({});
    
    // Anti-cheating logs count
    const [violationsCount, setViolationsCount] = useState(0);

    const apiHost = process.env.NEXT_PUBLIC_API_HOST || "http://localhost:8080";
    const timerRef = useRef<NodeJS.Timeout | null>(null);

    const getAuthHeaders = () => {
        const storedToken = document.cookie
            .split("; ")
            .find(row => row.startsWith("codejudge_token="))
            ?.split("=")[1];
        return {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${storedToken || token}`,
        };
    };

    // 1. Fetch active candidate exams on mount
    useEffect(() => {
        fetchCandidateExams();
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, []);

    const fetchCandidateExams = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${apiHost}/api/v1/exams/candidate/active`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                // Map the Spring Boot API responses correctly.
                // Note: The backend returned List<ExamResponse>. We can map it to our enrollments state.
                const examsList = data.data.map((exam: any) => {
                    // For demo/UI consistency, we check if they are invited
                    return {
                        id: exam.id, // candidate enrollment ID, for now using examId
                        examId: exam.id,
                        examTitle: exam.title,
                        examDescription: exam.description,
                        startedAt: null,
                        completedAt: null,
                        score: null,
                        status: "INVITED",
                        durationMinutes: exam.durationMinutes || 60,
                        startTime: exam.startTime,
                        endTime: exam.endTime,
                    };
                });
                setEnrollments(examsList);
            }
        } catch (err) {
            console.error("Failed to load candidate exams", err);
        } finally {
            setLoading(false);
        }
    };

    // 2. Fetch full exam section questions details on enter lobby
    const handleEnterLobby = async (candidateExam: ExamCandidate) => {
        setLoading(true);
        try {
            const res = await fetch(`${apiHost}/api/v1/exams/${candidateExam.examId}`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                setActiveExamDetail(data.data);
                setActiveLobbyEnrollment(candidateExam);
            }
        } catch (err) {
            console.error("Failed to load exam details", err);
        } finally {
            setLoading(false);
        }
    };

    // 3. Start Exam Attempt
    const handleStartExam = async () => {
        if (!activeLobbyEnrollment || !activeExamDetail) return;
        setLoading(true);
        try {
            const res = await fetch(`${apiHost}/api/v1/exams/candidate/active/${activeLobbyEnrollment.examId}/start`, {
                method: "POST",
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                setExamStarted(true);
                // Set countdown timer
                const totalSeconds = activeExamDetail.durationMinutes * 60;
                setTimeLeftSeconds(totalSeconds);
                
                // Initialize templates
                if (activeExamDetail.sections.length > 0 && activeExamDetail.sections[0].questions.length > 0) {
                    setActiveQuestionIdx({sectionIdx: 0, questionIdx: 0});
                }
                
                // Set candidate enrollment ID for telemetry audits
                const candidateEnrollmentId = data.data.id; // enrollment PK
                activeLobbyEnrollment.id = candidateEnrollmentId;

                // Start timer ticker
                startTimerTicker();
                // Register anti-cheating window monitors!
                setupAntiCheatingMonitors(candidateEnrollmentId);
            }
        } catch (err) {
            console.error("Failed to start exam", err);
        } finally {
            setLoading(false);
        }
    };

    const startTimerTicker = () => {
        if (timerRef.current) clearInterval(timerRef.current);
        timerRef.current = setInterval(() => {
            setTimeLeftSeconds(prev => {
                if (prev <= 1) {
                    clearInterval(timerRef.current!);
                    handleCompleteExam(); // Auto-submit when timer expires
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    };

    // 4. Setup Anti-Cheating Telemetry Monitors
    const setupAntiCheatingMonitors = (enrollmentId: number) => {
        window.onblur = () => {
            logCheatingTelemetry(enrollmentId, "WINDOW_BLURRED", "Candidate lost focus from exam browser window.");
        };

        document.onvisibilitychange = () => {
            if (document.hidden) {
                logCheatingTelemetry(enrollmentId, "TAB_SWITCHED", "Candidate switched away to a different tab.");
            }
        };
    };

    const logCheatingTelemetry = async (enrollmentId: number, type: string, details: string) => {
        setViolationsCount(prev => prev + 1);
        try {
            await fetch(`${apiHost}/api/v1/exams/candidate/active/${enrollmentId}/activity`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({
                    activityType: type,
                    details: `${details} (Session warning: ${violationsCount + 1})`,
                }),
            });
        } catch (err) {
            console.error("Failed to log activity telemetry", err);
        }
    };

    // 5. Submit individual question solution
    const handleSubmitSolution = async (questionId: number, type: "CODING" | "MCQ") => {
        if (!activeLobbyEnrollment) return;
        
        let source = sourceCodes[questionId] || "";
        if (type === "MCQ") {
            source = JSON.stringify(selectedOptions[questionId] || []);
        }

        try {
            setSubmissionResults(prev => ({...prev, [questionId]: "SUBMITTING"}));
            const res = await fetch(`${apiHost}/api/v1/submissions`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({
                    questionId: questionId,
                    languageId: 1, // Default Java/Python standard compiler language
                    sourceCode: source,
                    candidateId: activeLobbyEnrollment.id,
                }),
            });
            const data = await res.json();
            if (data.success) {
                setSubmissionResults(prev => ({...prev, [questionId]: data.data.status}));
            } else {
                setSubmissionResults(prev => ({...prev, [questionId]: "FAILED"}));
            }
        } catch (err) {
            setSubmissionResults(prev => ({...prev, [questionId]: "ERROR"}));
        }
    };

    // 6. Complete and submit full exam session
    const handleCompleteExam = async () => {
        if (!activeLobbyEnrollment) return;
        setLoading(true);
        if (timerRef.current) clearInterval(timerRef.current);
        
        // Remove anti-cheating monitors
        window.onblur = null;
        document.onvisibilitychange = null;

        try {
            const res = await fetch(`${apiHost}/api/v1/exams/candidate/active/${activeLobbyEnrollment.examId}/complete`, {
                method: "POST",
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                // Terminate attempt session
                setExamStarted(false);
                setActiveExamDetail(null);
                setActiveLobbyEnrollment(null);
                fetchCandidateExams(); // Reload exam cards
                alert("Exam attempt completed and graded successfully. Thank you!");
            }
        } catch (err) {
            console.error("Failed to submit exam attempt completion", err);
        } finally {
            setLoading(false);
        }
    };

    const getActiveQuestion = () => {
        if (!activeExamDetail || !activeQuestionIdx) return null;
        return activeExamDetail.sections[activeQuestionIdx.sectionIdx]?.questions[activeQuestionIdx.questionIdx] || null;
    };

    const formatTimer = (seconds: number) => {
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = seconds % 60;
        return `${h > 0 ? h + ":" : ""}${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
    };

    return (
        <div className="min-h-screen bg-[#090d16] flex flex-col">
            <Navbar />

            {/* General Exams Lobby Overview */}
            {!activeLobbyEnrollment && (
                <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="mb-8">
                        <h1 className="text-2xl font-bold text-white tracking-tight">Active Assessment Portals</h1>
                        <p className="text-slate-400 text-sm mt-1">Join scheduled programming contests and testing evaluations here.</p>
                    </div>

                    {loading ? (
                        <div className="flex justify-center py-20">
                            <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
                        </div>
                    ) : enrollments.length === 0 ? (
                        <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-12 text-center">
                            <ShieldAlert className="h-10 w-10 text-slate-600 mx-auto mb-4" />
                            <h3 className="text-slate-300 font-semibold text-lg">No active scheduled exams</h3>
                            <p className="text-slate-500 text-sm mt-1">You will receive an email invitation whenever an exam is scheduled for you.</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {enrollments.map((enr) => (
                                <div key={enr.examId} className="bg-[#131b2e] border border-[#1e293b] hover:border-indigo-500/50 rounded-xl p-6 transition-all flex flex-col justify-between">
                                    <div>
                                        <div className="flex justify-between items-start mb-4">
                                            <h3 className="font-bold text-slate-100 text-lg">{enr.examTitle}</h3>
                                            <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-indigo-950 text-indigo-300 border border-indigo-800">
                                                {enr.status}
                                            </span>
                                        </div>
                                        <p className="text-sm text-slate-400 mb-6 leading-relaxed line-clamp-2">{enr.examDescription}</p>
                                        <div className="grid grid-cols-2 gap-4 bg-[#090d16]/80 p-4 rounded-lg border border-[#1e293b] mb-6">
                                            <div>
                                                <span className="text-[10px] text-slate-500 font-semibold block uppercase">Duration</span>
                                                <span className="text-sm font-semibold text-slate-300 mt-1 block">{enr.durationMinutes} Minutes</span>
                                            </div>
                                            <div>
                                                <span className="text-[10px] text-slate-500 font-semibold block uppercase">Scheduled Start</span>
                                                <span className="text-xs font-semibold text-slate-300 mt-1 block truncate">
                                                    {new Date(enr.startTime).toLocaleString()}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => handleEnterLobby(enr)}
                                        className="w-full flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-lg text-sm transition-colors cursor-pointer"
                                    >
                                        <Play className="h-4 w-4" />
                                        Enter Exam Lobby
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </main>
            )}

            {/* Exam Lobby / Verification Screen */}
            {activeLobbyEnrollment && !examStarted && activeExamDetail && (
                <main className="flex-1 max-w-3xl w-full mx-auto px-4 py-8 flex flex-col justify-center">
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl shadow-2xl overflow-hidden">
                        <div className="bg-gradient-to-r from-indigo-950 to-purple-950 border-b border-[#1e293b] p-6 text-center">
                            <ShieldAlert className="h-10 w-10 text-indigo-400 mx-auto mb-3" />
                            <h2 className="text-2xl font-bold text-white">{activeExamDetail.title}</h2>
                            <p className="text-slate-400 text-sm mt-1">Lobby Entry & Verification</p>
                        </div>
                        
                        <div className="p-8 space-y-6">
                            <div className="bg-[#090d16] border border-[#1e293b] rounded-lg p-5">
                                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3">Strict Assessment Guidelines</h4>
                                <ul className="space-y-3 text-sm text-slate-300">
                                    <li className="flex items-start gap-2">
                                        <AlertOctagon className="h-5 w-5 text-amber-500 flex-shrink-0 mt-0.5" />
                                        <span>**Anti-Cheating Logs Active**: All tab switches and lost focus events will be recorded and sent directly to administrators.</span>
                                    </li>
                                    <li className="flex items-start gap-2">
                                        <Clock className="h-5 w-5 text-indigo-400 flex-shrink-0 mt-0.5" />
                                        <span>**Strict Timer Constraints**: The timer cannot be paused. When it reaches 0:00, the exam will auto-submit.</span>
                                    </li>
                                    <li className="flex items-start gap-2">
                                        <FileText className="h-5 w-5 text-purple-400 flex-shrink-0 mt-0.5" />
                                        <span>**Automatic Grading**: Coding questions compile against hidden testcases. Submissions must be saved before final exam submit.</span>
                                    </li>
                                </ul>
                            </div>

                            <div className="flex gap-4">
                                <button
                                    onClick={() => {
                                        setActiveLobbyEnrollment(null);
                                        setActiveExamDetail(null);
                                    }}
                                    className="flex-1 border border-[#1e293b] bg-transparent text-slate-400 hover:text-white py-3 rounded-lg text-sm font-semibold transition-colors cursor-pointer"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={handleStartExam}
                                    className="flex-1 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white py-3 rounded-lg text-sm font-semibold shadow-lg hover:shadow-indigo-500/20 transition-all cursor-pointer"
                                >
                                    Start Assessment
                                </button>
                            </div>
                        </div>
                    </div>
                </main>
            )}

            {/* Strict Active Exam Workspace */}
            {examStarted && activeExamDetail && activeQuestionIdx && (
                <div className="flex-1 flex overflow-hidden max-h-[calc(100vh-64px)] relative">
                    {/* Floating Proctored Bar */}
                    <div className="absolute top-4 right-6 z-50 flex items-center gap-4 bg-[#090d16] border border-red-800 rounded-full px-5 py-2 shadow-2xl">
                        <div className="w-2.5 h-2.5 rounded-full bg-red-500 animate-ping" />
                        <span className="text-xs font-bold text-red-400 uppercase tracking-widest">Session Active</span>
                        <div className="h-4 w-px bg-red-900" />
                        <span className="text-sm font-extrabold text-slate-100 flex items-center gap-1.5 font-mono">
                            <Clock className="h-4 w-4 text-indigo-400" />
                            {formatTimer(timeLeftSeconds)}
                        </span>
                    </div>

                    {/* Left Pane: Question Nav & Description */}
                    <div className="w-1/3 flex flex-col border-r border-[#1e293b] bg-[#111727] overflow-y-auto p-6">
                        {/* Sections List */}
                        <div className="mb-6">
                            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-3">Exam Sections</h3>
                            <div className="space-y-4">
                                {activeExamDetail.sections.map((sect, sIdx) => (
                                    <div key={sect.id} className="space-y-1.5">
                                        <h4 className="text-xs font-bold text-slate-500">{sect.title}</h4>
                                        <div className="flex flex-wrap gap-2">
                                            {sect.questions.map((q, qIdx) => {
                                                const isActive = activeQuestionIdx.sectionIdx === sIdx && activeQuestionIdx.questionIdx === qIdx;
                                                const gradingStatus = submissionResults[q.id];
                                                return (
                                                    <button
                                                        key={q.id}
                                                        onClick={() => setActiveQuestionIdx({sectionIdx: sIdx, questionIdx: qIdx})}
                                                        className={`w-9 h-9 rounded-lg flex items-center justify-center font-bold text-xs border transition-all cursor-pointer ${
                                                            isActive
                                                                ? "bg-indigo-600 border-indigo-400 text-white"
                                                                : gradingStatus === "ACCEPTED"
                                                                ? "bg-emerald-950 border-emerald-800 text-emerald-400"
                                                                : "bg-[#090d16] border-[#1e293b] text-slate-400 hover:border-slate-700"
                                                        }`}
                                                    >
                                                        {qIdx + 1}
                                                    </button>
                                                );
                                            })}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Selected Question Details */}
                        {getActiveQuestion() && (
                            <div className="border-t border-[#1e293b] pt-6 flex-1 flex flex-col justify-between">
                                <div>
                                    <h2 className="text-xl font-bold text-slate-200">{getActiveQuestion()?.title}</h2>
                                    <div className="flex gap-2 items-center mt-2.5 mb-4">
                                        <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-900 border border-[#1e293b] text-slate-400">
                                            {getActiveQuestion()?.type}
                                        </span>
                                        <span className="text-[10px] text-slate-500">{getActiveQuestion()?.marks} Marks</span>
                                    </div>
                                    <p className="text-slate-300 text-sm whitespace-pre-wrap leading-relaxed">{getActiveQuestion()?.description}</p>
                                </div>

                                <button
                                    onClick={handleCompleteExam}
                                    className="w-full bg-red-950 hover:bg-red-900 text-red-300 border border-red-800 hover:text-red-200 font-bold py-2.5 rounded-lg text-sm mt-8 transition-colors cursor-pointer"
                                >
                                    Finish & Submit Exam
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Right Pane: Workspace */}
                    <div className="w-2/3 flex flex-col bg-[#0b101c] overflow-y-auto">
                        {getActiveQuestion() && (
                            <div className="flex-1 flex flex-col p-6 min-h-[400px]">
                                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">Workspace</h3>
                                
                                {getActiveQuestion()?.type === "CODING" ? (
                                    <textarea
                                        value={sourceCodes[getActiveQuestion()!.id] || ""}
                                        onChange={(e) => setSourceCodes({...sourceCodes, [getActiveQuestion()!.id]: e.target.value})}
                                        className="flex-1 w-full bg-[#070b13] border border-[#1e293b] focus:border-indigo-500/80 rounded-lg p-4 font-mono text-sm text-slate-100 placeholder-slate-600 focus:outline-none transition-colors overflow-y-auto resize-none min-h-[300px]"
                                        placeholder="Write your code solution here..."
                                        spellCheck={false}
                                    />
                                ) : (
                                    <div className="flex-1 bg-[#131b2e]/30 border border-[#1e293b] rounded-lg p-6 space-y-4">
                                        <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Select the correct option(s):</p>
                                        <div className="space-y-3">
                                            {getActiveQuestion()?.options?.map((opt) => {
                                                const activeOptions = selectedOptions[getActiveQuestion()!.id] || [];
                                                const isChecked = activeOptions.includes(opt.id);
                                                return (
                                                    <div
                                                        key={opt.id}
                                                        onClick={() => {
                                                            const current = selectedOptions[getActiveQuestion()!.id] || [];
                                                            if (getActiveQuestion()?.isMultipleChoice) {
                                                                const updated = current.includes(opt.id)
                                                                    ? current.filter(id => id !== opt.id)
                                                                    : [...current, opt.id];
                                                                setSelectedOptions({...selectedOptions, [getActiveQuestion()!.id]: updated});
                                                            } else {
                                                                setSelectedOptions({...selectedOptions, [getActiveQuestion()!.id]: [opt.id]});
                                                            }
                                                        }}
                                                        className={`p-4 border rounded-lg flex items-center gap-3 cursor-pointer transition-all ${
                                                            isChecked
                                                                ? "bg-indigo-950/20 border-indigo-500 text-indigo-300"
                                                                : "bg-[#090d16] border-[#1e293b] hover:border-slate-700 text-slate-300"
                                                        }`}
                                                    >
                                                        <div className={`w-5 h-5 rounded-md flex items-center justify-center border text-xs font-bold ${
                                                            isChecked
                                                                ? "border-indigo-500 bg-indigo-600 text-white"
                                                                : "border-slate-700 text-slate-500"
                                                        }`}>
                                                            {isChecked && <Check className="h-3 w-3" />}
                                                        </div>
                                                        <span className="font-semibold text-xs py-0.5 px-1.5 bg-slate-900 border border-[#1e293b] rounded text-slate-400">{opt.id}</span>
                                                        <span className="text-sm">{opt.text}</span>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}

                                <div className="mt-6 flex justify-between items-center">
                                    {submissionResults[getActiveQuestion()!.id] && (
                                        <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                                            submissionResults[getActiveQuestion()!.id] === "ACCEPTED"
                                                ? "bg-emerald-950/40 text-emerald-400 border border-emerald-800"
                                                : submissionResults[getActiveQuestion()!.id] === "SUBMITTING"
                                                ? "bg-indigo-950/40 text-indigo-400 border border-indigo-800 animate-pulse"
                                                : "bg-red-950/40 text-red-400 border border-red-800"
                                        }`}>
                                            Status: {submissionResults[getActiveQuestion()!.id]}
                                        </span>
                                    )}
                                    <div className="flex-1" />
                                    <button
                                        onClick={() => handleSubmitSolution(getActiveQuestion()!.id, getActiveQuestion()!.type)}
                                        className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-semibold px-6 py-2.5 rounded-lg text-sm transition-all cursor-pointer"
                                    >
                                        Save & Submit Solution
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
