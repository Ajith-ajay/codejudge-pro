"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navbar } from "@/components/Navbar";
import { AuthModal } from "@/components/AuthModal";
import { Award, Calendar, CheckCircle2, ChevronRight, Globe, MapPin, Eye, Trophy, HelpCircle, FileText, Code, Check } from "lucide-react";
import { useRouter } from "next/navigation";

interface Submission {
    id: number;
    questionId: number;
    questionTitle: string;
    languageCode: string;
    languageName: string;
    status: string;
    score: number;
    createdAt: string;
    executionTimeMs: number;
    executionMemoryMb: number;
}

interface Question {
    id: number;
    title: string;
    difficulty: "EASY" | "MEDIUM" | "HARD";
    type: "CODING" | "MCQ";
}

export default function ProfilePage() {
    const { user, token, loading: authLoading } = useAuth();
    const router = useRouter();
    const [authModalOpen, setAuthModalOpen] = useState(false);

    // API loaded state
    const [submissions, setSubmissions] = useState<Submission[]>([]);
    const [questionsMap, setQuestionsMap] = useState<{ [key: number]: Question }>({});
    const [loadingData, setLoadingData] = useState(true);

    const apiHost = process.env.NEXT_PUBLIC_API_HOST || "http://localhost:8080";

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

    useEffect(() => {
        if (!authLoading && !user) {
            setAuthModalOpen(true);
        } else if (user) {
            fetchProfileData();
        }
    }, [user, authLoading]);

    const fetchProfileData = async () => {
        setLoadingData(true);
        try {
            // 1. Fetch user submissions
            const subRes = await fetch(`${apiHost}/api/v1/submissions/my-submissions`, {
                headers: getAuthHeaders(),
            });
            const subData = await subRes.json();
            const subList: Submission[] = subData.success ? subData.data || [] : [];
            setSubmissions(subList);

            // 2. Fetch public questions to map difficulties
            const questionsRes = await fetch(`${apiHost}/api/v1/questions?size=1000`, {
                headers: getAuthHeaders(),
            });
            const questionsData = await questionsRes.json();
            const qList: Question[] = questionsData.success && questionsData.data.content ? questionsData.data.content : [];
            
            const qMap: { [key: number]: Question } = {};
            qList.forEach(q => {
                qMap[q.id] = q;
            });
            setQuestionsMap(qMap);
        } catch (err) {
            console.error("Failed to load profile details", err);
        } finally {
            setLoadingData(false);
        }
    };

    // Calculate unique solved questions metrics
    const solvedSubmissions = submissions.filter(s => s.status === "ACCEPTED");
    const uniqueSolvedIds = Array.from(new Set(solvedSubmissions.map(s => s.questionId)));
    
    let easySolved = 0;
    let mediumSolved = 0;
    let hardSolved = 0;

    uniqueSolvedIds.forEach(qId => {
        const q = questionsMap[qId];
        const diff = q ? q.difficulty : "MEDIUM"; // Fallback to medium
        if (diff === "EASY") easySolved++;
        else if (diff === "HARD") hardSolved++;
        else mediumSolved++;
    });

    const totalSolved = uniqueSolvedIds.length;

    // Get unique programming languages user has submitted in
    const languagesUsed = Array.from(new Set(submissions.map(s => s.languageName))).filter(Boolean);

    // Group submission counts by date for the contribution calendar grid
    const getSubmissionsPerDayMap = () => {
        const dayMap: { [dateStr: string]: number } = {};
        submissions.forEach(sub => {
            if (!sub.createdAt) return;
            // Parse date component
            const dateStr = sub.createdAt.split("T")[0]; // YYYY-MM-DD
            dayMap[dateStr] = (dayMap[dateStr] || 0) + 1;
        });
        return dayMap;
    };

    const submissionsPerDay = getSubmissionsPerDayMap();

    // Generate contribution calendar grid (53 weeks, ending today)
    const generateContributionGrid = () => {
        const today = new Date();
        const grid: Date[][] = [];
        const tempDate = new Date(today);
        // Offset start to Sunday 52 weeks ago
        tempDate.setDate(today.getDate() - 364 - today.getDay());

        for (let w = 0; w < 53; w++) {
            const week: Date[] = [];
            for (let d = 0; d < 7; d++) {
                week.push(new Date(tempDate));
                tempDate.setDate(tempDate.getDate() + 1);
            }
            grid.push(week);
        }
        return grid;
    };

    const contributionGrid = generateContributionGrid();
    const months = ["Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"];

    if (authLoading || (user && loadingData)) {
        return (
            <div className="min-h-screen bg-[#1a1a1a] flex items-center justify-center">
                <div className="w-8 h-8 border-3 border-[#ffa116] border-t-transparent rounded-full animate-spin" />
            </div>
        );
    }

    if (!user) {
        return (
            <div className="min-h-screen bg-[#1a1a1a] flex flex-col">
                <Navbar />
                <div className="flex-1 flex items-center justify-center p-6 text-center">
                    <div className="bg-[#282828] border border-[#3e3e3e] p-8 rounded-lg max-w-sm w-full">
                        <Trophy className="h-10 w-10 text-[#ffa116] mx-auto mb-4" />
                        <h3 className="text-lg font-bold text-white mb-2">Access Profile Details</h3>
                        <p className="text-xs text-slate-400 mb-6 leading-relaxed">
                            Sign in to review your solved problem statistics, certificates, and submission metrics history.
                        </p>
                        <button
                            onClick={() => setAuthModalOpen(true)}
                            className="w-full py-2 bg-[#ffa116] hover:bg-[#ffb03a] text-[#1a1a1a] font-bold text-xs rounded transition-colors cursor-pointer"
                        >
                            Sign In / Register
                        </button>
                    </div>
                </div>
                <AuthModal isOpen={authModalOpen} onClose={() => setAuthModalOpen(false)} onSuccess={() => router.push("/profile")} />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#1a1a1a] text-[#eff1f6] flex flex-col font-sans">
            <Navbar />

            <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Left Sidebar Profile Details card */}
                    <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6 flex flex-col gap-6 h-fit">
                        {/* Profile Info Header */}
                        <div className="flex items-center gap-4">
                            <div className="w-16 h-16 rounded-full bg-[#3e3e3e] flex items-center justify-center text-3xl font-bold text-[#ffa116] border border-[#3e3e3e]">
                                {user.username.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <h2 className="text-lg font-bold text-white">{user.username}</h2>
                                <p className="text-xs text-slate-400 mt-0.5">{user.email}</p>
                                <div className="flex flex-wrap gap-1 mt-2">
                                    {user.roles.map(role => (
                                        <span key={role} className="px-1.5 py-0.5 bg-[#ffa116]/10 text-[#ffa116] border border-[#ffa116]/20 rounded text-[9px] font-bold uppercase">
                                            {role.replace("ROLE_", "").replace("_", " ")}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Extra metadata details */}
                        <div className="space-y-3 pt-4 border-t border-[#3e3e3e] text-xs text-slate-400">
                            <div className="flex items-center justify-between text-xs">
                                <span className="text-slate-500 font-semibold uppercase text-[10px]">Total Submissions</span>
                                <span className="font-bold text-white">{submissions.length}</span>
                            </div>
                            <div className="flex items-center justify-between text-xs">
                                <span className="text-slate-500 font-semibold uppercase text-[10px]">Accepted Runs</span>
                                <span className="font-bold text-emerald-400">{solvedSubmissions.length}</span>
                            </div>
                            <div className="flex items-center justify-between text-xs">
                                <span className="text-slate-500 font-semibold uppercase text-[10px]">Acceptance Rate</span>
                                <span className="font-bold text-white">
                                    {submissions.length > 0 
                                        ? `${((solvedSubmissions.length / submissions.length) * 100).toFixed(1)}%` 
                                        : "0.0%"}
                                </span>
                            </div>
                        </div>

                        {/* Languages tags */}
                        <div className="pt-4 border-t border-[#3e3e3e]">
                            <h4 className="text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-2">Compilers Used</h4>
                            {languagesUsed.length === 0 ? (
                                <p className="text-[10px] text-slate-500">No compilers execution history yet.</p>
                            ) : (
                                <div className="flex flex-wrap gap-1.5">
                                    {languagesUsed.map((lang) => (
                                        <span key={lang} className="px-2 py-0.5 bg-[#1e1e1e] border border-[#3e3e3e] text-slate-300 rounded text-[10px] font-medium uppercase tracking-wider">
                                            {lang}
                                        </span>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Right Content Panels */}
                    <div className="lg:col-span-2 space-y-6">
                        {/* Problems Solved circular progress breakdown */}
                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-6">Solved Breakdown</h3>
                            
                            <div className="flex flex-col sm:flex-row items-center gap-8">
                                {/* Circular ring */}
                                <div className="w-28 h-28 rounded-full border-[10px] border-slate-700 border-t-[#ffa116] flex flex-col items-center justify-center flex-shrink-0">
                                    <span className="text-xl font-extrabold text-white">{totalSolved}</span>
                                    <span className="text-[9px] uppercase tracking-wider text-slate-400 font-bold">Solved</span>
                                </div>

                                {/* Difficulty counts list */}
                                <div className="flex-1 w-full space-y-3">
                                    <div>
                                        <div className="flex justify-between items-center text-xs mb-1">
                                            <span className="font-semibold text-emerald-400">Easy Problems</span>
                                            <span className="font-bold text-white">{easySolved} solved</span>
                                        </div>
                                        <div className="w-full bg-[#1e1e1e] h-1.5 rounded overflow-hidden">
                                            <div className="bg-emerald-400 h-full rounded" style={{ width: `${totalSolved > 0 ? (easySolved / totalSolved) * 100 : 0}%` }} />
                                        </div>
                                    </div>
                                    <div>
                                        <div className="flex justify-between items-center text-xs mb-1">
                                            <span className="font-semibold text-amber-500">Medium Problems</span>
                                            <span className="font-bold text-white">{mediumSolved} solved</span>
                                        </div>
                                        <div className="w-full bg-[#1e1e1e] h-1.5 rounded overflow-hidden">
                                            <div className="bg-amber-500 h-full rounded" style={{ width: `${totalSolved > 0 ? (mediumSolved / totalSolved) * 100 : 0}%` }} />
                                        </div>
                                    </div>
                                    <div>
                                        <div className="flex justify-between items-center text-xs mb-1">
                                            <span className="font-semibold text-red-500">Hard Problems</span>
                                            <span className="font-bold text-white">{hardSolved} solved</span>
                                        </div>
                                        <div className="w-full bg-[#1e1e1e] h-1.5 rounded overflow-hidden">
                                            <div className="bg-red-500 h-full rounded" style={{ width: `${totalSolved > 0 ? (hardSolved / totalSolved) * 100 : 0}%` }} />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Recent Submissions list */}
                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">Recent Submissions History</h3>
                            {submissions.length === 0 ? (
                                <p className="text-xs text-slate-500 text-center py-6">You have not submitted any coding solutions yet.</p>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full text-left text-xs border-collapse">
                                        <thead>
                                            <tr className="border-b border-[#3e3e3e] text-slate-500">
                                                <th className="py-2">Question Title</th>
                                                <th className="py-2">Language</th>
                                                <th className="py-2">Status</th>
                                                <th className="py-2">Execution Time</th>
                                                <th className="py-2">Date</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {submissions.slice(0, 6).map((sub) => (
                                                <tr key={sub.id} className="border-b border-[#3e3e3e]/40 hover:bg-[#333]/20">
                                                    <td className="py-3 font-bold text-slate-200">{sub.questionTitle}</td>
                                                    <td className="py-3 uppercase text-[10px] tracking-wider font-semibold text-slate-400">{sub.languageName}</td>
                                                    <td className="py-3">
                                                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                                                            sub.status === "ACCEPTED" ? "bg-emerald-500/10 text-emerald-400" :
                                                            sub.status === "WRONG_ANSWER" ? "bg-red-500/10 text-red-400" :
                                                            "bg-slate-500/10 text-slate-400"
                                                        }`}>
                                                            {sub.status.replace("_", " ")}
                                                        </span>
                                                    </td>
                                                    <td className="py-3 text-slate-400">{sub.executionTimeMs} ms</td>
                                                    <td className="py-3 text-slate-400">{new Date(sub.createdAt).toLocaleDateString()}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>

                        {/* GitHub-style Contribution Grid Heatmap */}
                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                            <div className="flex justify-between items-center mb-4">
                                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-300">
                                    {submissions.length} Submissions in the past one year
                                </h3>
                                <div className="flex gap-2 text-[10px] font-semibold text-slate-500 items-center">
                                    <span>Active days: {Object.keys(submissionsPerDay).length}</span>
                                </div>
                            </div>

                            {/* Months label */}
                            <div className="flex justify-between pl-6 text-[10px] font-semibold text-slate-500 mb-1">
                                {months.map((m, idx) => (
                                    <span key={idx}>{m}</span>
                                ))}
                            </div>

                            {/* Heatmap grid */}
                            <div className="flex gap-[3px] overflow-x-auto select-none pt-1">
                                {/* Row indicators */}
                                <div className="flex flex-col justify-between pr-2 text-[9px] font-bold text-slate-600 h-[81px] py-1">
                                    <span>Mon</span>
                                    <span>Wed</span>
                                    <span>Fri</span>
                                </div>

                                {/* Columns representing weeks */}
                                <div className="flex gap-[3px] flex-grow justify-between">
                                    {contributionGrid.map((week, wIdx) => (
                                        <div key={wIdx} className="flex flex-col gap-[3px]">
                                            {week.map((cellDate, dIdx) => {
                                                const cellDateStr = cellDate.toISOString().split("T")[0];
                                                const count = submissionsPerDay[cellDateStr] || 0;
                                                return (
                                                    <div
                                                        key={dIdx}
                                                        className={`w-[9px] h-[9px] rounded-sm transition-colors duration-200 ${
                                                            count === 0 ? "bg-[#1e1e1e]" :
                                                            count === 1 ? "bg-emerald-900" :
                                                            count === 2 ? "bg-emerald-700" : "bg-[#2cbb5d]"
                                                        }`}
                                                        title={`${count} submissions on ${cellDate.toLocaleDateString()}`}
                                                    />
                                                );
                                            })}
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Key legend indicator */}
                            <div className="flex justify-end gap-1.5 text-[9px] text-slate-500 font-semibold items-center mt-3">
                                <span>Less</span>
                                <div className="w-2.5 h-2.5 rounded-sm bg-[#1e1e1e]" />
                                <div className="w-2.5 h-2.5 rounded-sm bg-emerald-950" />
                                <div className="w-2.5 h-2.5 rounded-sm bg-emerald-800" />
                                <div className="w-2.5 h-2.5 rounded-sm bg-[#2cbb5d]" />
                                <span>More</span>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            <AuthModal isOpen={authModalOpen} onClose={() => setAuthModalOpen(false)} onSuccess={() => router.push("/profile")} />
        </div>
    );
}
