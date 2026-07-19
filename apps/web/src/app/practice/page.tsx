"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navbar } from "@/components/Navbar";
import { AuthModal } from "@/components/AuthModal";
import { 
    Play, Send, ChevronLeft, Search, CheckCircle, HelpCircle, 
    Lock, Code, Database, Terminal, Settings, RefreshCw, AlertCircle 
} from "lucide-react";

interface TestCase {
    id: number;
    input: string;
    expectedOutput: string;
    hidden: boolean;
    marks: number;
}

interface Language {
    id: number;
    name: string;
    code: string;
    compilerVersion: string;
}

interface Question {
    id: number;
    title: string;
    description: string;
    difficulty: "EASY" | "MEDIUM" | "HARD";
    marks: number;
    type: "CODING" | "MCQ";
    options?: { id: string; text: string }[];
    isMultipleChoice?: boolean;
    timeLimitMs?: number;
    memoryLimitMb?: number;
    allowedLanguages?: Language[];
    testCases?: TestCase[];
}

interface SubmissionResult {
    status: string;
    score: number;
    executionTimeMs: number;
    executionMemoryMb: number;
    testCaseResults?: {
        testCaseId: number;
        status: string;
        executionTimeMs: number;
        output?: string;
        errorMessage?: string;
    }[];
}

export default function LeetCodePracticePage() {
    const { user, token } = useAuth();
    
    // UI state
    const [questions, setQuestions] = useState<Question[]>([]);
    const [selectedQuestion, setSelectedQuestion] = useState<Question | null>(null);
    const [loading, setLoading] = useState(true);
    const [languages, setLanguages] = useState<Language[]>([]);
    
    // Auth guard trigger
    const [authModalOpen, setAuthModalOpen] = useState(false);
    
    // Workspace state
    const [workspaceTab, setWorkspaceTab] = useState<"desc" | "sub">("desc");
    const [selectedLanguageId, setSelectedLanguageId] = useState<number>(1);
    const [sourceCode, setSourceCode] = useState<string>("");
    const [selectedMcqOptions, setSelectedMcqOptions] = useState<string[]>([]);
    
    // Code evaluation states
    const [submitting, setSubmitting] = useState(false);
    const [running, setRunning] = useState(false);
    const [runOutput, setRunOutput] = useState<{status: string, output?: string, error?: string} | null>(null);
    const [submissionResult, setSubmissionResult] = useState<SubmissionResult | null>(null);
    
    // Filter/Search state
    const [searchQuery, setSearchQuery] = useState("");
    const [difficultyFilter, setDifficultyFilter] = useState("ALL");
    const [selectedTopic, setSelectedTopic] = useState("ALL");

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
        fetchQuestions();
        fetchLanguages();
    }, []);

    const fetchQuestions = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${apiHost}/api/v1/questions?size=50`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success && data.data.content) {
                setQuestions(data.data.content);
            }
        } catch (err) {
            console.error("Failed to load questions", err);
        } finally {
            setLoading(false);
        }
    };

    const fetchLanguages = async () => {
        try {
            const res = await fetch(`${apiHost}/api/v1/questions/languages`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                setLanguages(data.data);
                if (data.data.length > 0) {
                    setSelectedLanguageId(data.data[0].id);
                }
            }
        } catch (err) {
            console.error("Failed to load languages", err);
        }
    };

    const handleSelectQuestion = async (q: Question) => {
        if (!user) {
            setAuthModalOpen(true);
            return;
        }

        setLoading(true);
        setSubmissionResult(null);
        setRunOutput(null);
        setSelectedMcqOptions([]);
        
        try {
            const res = await fetch(`${apiHost}/api/v1/questions/${q.id}`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                const fullQ = data.data;
                setSelectedQuestion(fullQ);
                setWorkspaceTab("desc");
                
                // Initialize workspace templates
                if (fullQ.type === "CODING") {
                    if (fullQ.allowedLanguages && fullQ.allowedLanguages.length > 0) {
                        setSelectedLanguageId(fullQ.allowedLanguages[0].id);
                        setSourceCode(getTemplateForLanguage(fullQ.allowedLanguages[0].code));
                    } else if (languages.length > 0) {
                        setSelectedLanguageId(languages[0].id);
                        setSourceCode(getTemplateForLanguage(languages[0].code));
                    }
                }
            }
        } catch (err) {
            console.error("Failed to fetch question details", err);
        } finally {
            setLoading(false);
        }
    };

    const handleLanguageChange = (langId: number) => {
        setSelectedLanguageId(langId);
        const lang = languages.find(l => l.id === langId);
        if (lang) {
            setSourceCode(getTemplateForLanguage(lang.code));
        }
    };

    const getTemplateForLanguage = (code: string) => {
        if (code === "java") {
            return `import java.util.*;\n\nclass Solution {\n    public static void main(String[] args) {\n        // Write your Java code here\n        System.out.println("Hello World");\n    }\n}`;
        }
        if (code === "cpp" || code === "c++") {
            return `#include <iostream>\nusing namespace std;\n\nint main() {\n    // Write your C++ code here\n    cout << "Hello World" << endl;\n    return 0;\n}`;
        }
        return `# Write your Python code here\nprint("Hello World")`;
    };

    // MCQ choices select
    const handleMcqSelect = (optionId: string) => {
        if (selectedQuestion?.isMultipleChoice) {
            if (selectedMcqOptions.includes(optionId)) {
                setSelectedMcqOptions(selectedMcqOptions.filter(id => id !== optionId));
            } else {
                setSelectedMcqOptions([...selectedMcqOptions, optionId]);
            }
        } else {
            setSelectedMcqOptions([optionId]);
        }
    };

    // Run custom code (before full submission)
    const handleRunCode = async () => {
        if (!selectedQuestion) return;
        setRunning(true);
        setRunOutput(null);

        try {
            // Call compile run sandbox endpoint directly
            const res = await fetch(`${apiHost}/api/v1/submissions`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({
                    questionId: selectedQuestion.id,
                    languageId: selectedLanguageId,
                    sourceCode: sourceCode,
                    // If no candidate enrollment is set, it compiles inside sandbox and runs against visible cases only!
                }),
            });
            const data = await res.json();
            if (data.success) {
                const subRes: SubmissionResult = data.data;
                // Treat this compile result as run output
                setRunOutput({
                    status: subRes.status,
                    output: subRes.testCaseResults?.[0]?.output || "Code executed successfully.",
                    error: subRes.testCaseResults?.[0]?.errorMessage
                });
            } else {
                setRunOutput({ status: "FAILED", error: data.message || "Execution error" });
            }
        } catch (err) {
            setRunOutput({ status: "ERROR", error: "Connection to compiler sandbox failed" });
        } finally {
            setRunning(false);
        }
    };

    // Submit solution for final grading
    const handleSubmit = async () => {
        if (!selectedQuestion) return;
        setSubmitting(true);
        setSubmissionResult(null);
        setWorkspaceTab("sub");

        let finalSource = sourceCode;
        if (selectedQuestion.type === "MCQ") {
            finalSource = JSON.stringify(selectedMcqOptions);
        }

        try {
            const res = await fetch(`${apiHost}/api/v1/submissions`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify({
                    questionId: selectedQuestion.id,
                    languageId: selectedQuestion.type === "CODING" ? selectedLanguageId : languages[0]?.id || 1,
                    sourceCode: finalSource,
                }),
            });
            const data = await res.json();
            if (data.success) {
                setSubmissionResult(data.data);
            }
        } catch (err) {
            console.error("Submission failed", err);
        } finally {
            setSubmitting(false);
        }
    };

    // Filter logic
    const filteredQuestions = questions.filter(q => {
        const matchesSearch = q.title.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesDifficulty = difficultyFilter === "ALL" || q.difficulty === difficultyFilter;
        const matchesTopic = selectedTopic === "ALL" || 
            (selectedTopic === "Algorithms" && q.type === "CODING") ||
            (selectedTopic === "Database" && q.title.toLowerCase().includes("sql")) ||
            (selectedTopic === "Concurrency" && q.type === "MCQ");
        return matchesSearch && matchesDifficulty && matchesTopic;
    });

    const topicPills = ["All Topics", "Algorithms", "Database", "Shell", "Concurrency", "JavaScript"];

    return (
        <div className="min-h-screen bg-[#1a1a1a] text-[#eff1f6] flex flex-col font-sans relative">
            <Navbar />

            {/* Submitting Loading Overlay Spinner */}
            {submitting && (
                <div className="absolute inset-0 bg-black/70 z-[200] flex flex-col items-center justify-center gap-4">
                    <div className="w-12 h-12 border-4 border-[#ffa116] border-t-transparent rounded-full animate-spin" />
                    <p className="text-sm font-bold tracking-widest text-[#ffa116] uppercase animate-pulse">
                        Evaluating submission code inside sandbox...
                    </p>
                </div>
            )}

            {/* List Mode View */}
            {!selectedQuestion && (
                <div className="flex-grow max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 flex gap-8">
                    {/* Left Sidebar Menu */}
                    <div className="hidden lg:block w-56 flex-shrink-0 space-y-6">
                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg overflow-hidden">
                            {["Library", "Quest", "Explore", "Study Plan", "Favorite"].map((item, idx) => (
                                <button
                                    key={idx}
                                    className={`w-full text-left px-4 py-3 text-xs font-bold uppercase tracking-wider border-b border-[#3e3e3e] hover:bg-[#333] transition-colors cursor-pointer ${
                                        idx === 0 ? "text-[#ffa116]" : "text-slate-400"
                                    }`}
                                >
                                    {item}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Main Table view */}
                    <div className="flex-1 space-y-6">
                        {/* Topic selector bar */}
                        <div className="flex flex-wrap gap-2">
                            {topicPills.map((topic) => (
                                <button
                                    key={topic}
                                    onClick={() => setSelectedTopic(topic === "All Topics" ? "ALL" : topic)}
                                    className={`px-3 py-1.5 rounded-full text-xs font-semibold tracking-wide border transition-all cursor-pointer ${
                                        (selectedTopic === "ALL" && topic === "All Topics") || selectedTopic === topic
                                            ? "bg-[#ffa116] text-[#1a1a1a] border-[#ffa116]"
                                            : "bg-[#282828] text-slate-300 border-[#3e3e3e] hover:bg-[#333]"
                                    }`}
                                >
                                    {topic}
                                </button>
                            ))}
                        </div>

                        {/* Search & filters */}
                        <div className="flex flex-col sm:flex-row gap-3">
                            <div className="relative flex-grow">
                                <Search className="absolute inset-y-0 left-0 pl-3 h-full w-5 text-slate-500 flex items-center pointer-events-none" />
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="Search questions..."
                                    className="w-full bg-[#282828] border border-[#3e3e3e] rounded-lg py-2 pl-10 pr-4 text-xs text-slate-100 placeholder-slate-500 focus:border-[#ffa116] focus:outline-none"
                                />
                            </div>

                            <select
                                value={difficultyFilter}
                                onChange={(e) => setDifficultyFilter(e.target.value)}
                                className="bg-[#282828] border border-[#3e3e3e] text-slate-300 text-xs font-semibold rounded-lg px-3 py-2 focus:border-[#ffa116] focus:outline-none"
                            >
                                <option value="ALL">All Difficulties</option>
                                <option value="EASY">Easy</option>
                                <option value="MEDIUM">Medium</option>
                                <option value="HARD">Hard</option>
                            </select>
                        </div>

                        {/* Table */}
                        {loading ? (
                            <div className="flex justify-center py-20">
                                <div className="w-8 h-8 border-3 border-[#ffa116] border-t-transparent rounded-full animate-spin" />
                            </div>
                        ) : filteredQuestions.length === 0 ? (
                            <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-12 text-center text-slate-500">
                                No problems found matching criteria.
                            </div>
                        ) : (
                            <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg overflow-hidden">
                                <table className="min-w-full divide-y divide-[#3e3e3e]">
                                    <thead className="bg-[#1e1e1e]">
                                        <tr>
                                            <th scope="col" className="w-10 px-4 py-3"></th>
                                            <th scope="col" className="px-6 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-400">Title</th>
                                            <th scope="col" className="px-6 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-400">Acceptance</th>
                                            <th scope="col" className="px-6 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-400">Difficulty</th>
                                            <th scope="col" className="px-6 py-3 text-right text-xs font-bold uppercase tracking-wider text-slate-400">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-[#3e3e3e] bg-[#282828]">
                                        {filteredQuestions.map((q, idx) => (
                                            <tr key={q.id} className="hover:bg-[#333] transition-colors">
                                                <td className="px-4 py-4 text-center">
                                                    <CheckCircle className="h-4 w-4 text-emerald-500 opacity-60" />
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <button
                                                        onClick={() => handleSelectQuestion(q)}
                                                        className="text-sm font-semibold text-slate-200 hover:text-[#ffa116] text-left cursor-pointer"
                                                    >
                                                        {idx + 1}. {q.title}
                                                    </button>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-xs text-slate-400 font-medium">
                                                    {q.difficulty === "EASY" ? "57.8%" : q.difficulty === "MEDIUM" ? "48.8%" : "38.2%"}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-xs font-semibold">
                                                    <span className={
                                                        q.difficulty === "EASY" ? "text-emerald-500" :
                                                        q.difficulty === "MEDIUM" ? "text-amber-500" : "text-red-500"
                                                    }>
                                                        {q.difficulty}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-right text-slate-400">
                                                    <Lock className="h-4 w-4 ml-auto text-slate-500 opacity-40" />
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* Split Workspace View */}
            {selectedQuestion && (
                <div className="flex-1 flex flex-col lg:flex-row overflow-hidden max-h-[calc(100vh-56px)]">
                    {/* Left Pane (Description & Solutions Tabs) */}
                    <div className="w-full lg:w-1/2 flex flex-col border-r border-[#3e3e3e] bg-[#1a1a1a] overflow-hidden">
                        <div className="bg-[#282828] border-b border-[#3e3e3e] px-4 py-2 flex items-center justify-between">
                            <div className="flex gap-2">
                                <button
                                    onClick={() => setWorkspaceTab("desc")}
                                    className={`px-3 py-1.5 rounded text-xs font-bold transition-colors ${
                                        workspaceTab === "desc" ? "bg-[#333] text-white" : "text-slate-400 hover:text-white"
                                    }`}
                                >
                                    Description
                                </button>
                                <button
                                    onClick={() => setWorkspaceTab("sub")}
                                    className={`px-3 py-1.5 rounded text-xs font-bold transition-colors ${
                                        workspaceTab === "sub" ? "bg-[#333] text-white" : "text-slate-400 hover:text-white"
                                    }`}
                                >
                                    Submissions
                                </button>
                            </div>
                            <button
                                onClick={() => setSelectedQuestion(null)}
                                className="flex items-center gap-1 text-slate-400 hover:text-white text-xs font-bold cursor-pointer"
                            >
                                <ChevronLeft className="h-4 w-4" />
                                Problem List
                            </button>
                        </div>

                        {/* Left Tab Panel content */}
                        <div className="flex-1 overflow-y-auto p-6 space-y-6">
                            {workspaceTab === "desc" ? (
                                <>
                                    <div>
                                        <h2 className="text-xl font-extrabold text-white tracking-tight">{selectedQuestion.title}</h2>
                                        <div className="flex gap-2 items-center mt-3">
                                            <span className={`text-xs font-bold px-2 py-0.5 rounded ${
                                                selectedQuestion.difficulty === "EASY" ? "bg-emerald-950/40 text-emerald-400" :
                                                selectedQuestion.difficulty === "MEDIUM" ? "bg-amber-950/40 text-amber-500" : "bg-red-950/40 text-red-500"
                                            }`}>
                                                {selectedQuestion.difficulty}
                                            </span>
                                            <span className="text-xs font-semibold text-slate-400 bg-[#282828] border border-[#3e3e3e] px-2 py-0.5 rounded">
                                                {selectedQuestion.type}
                                            </span>
                                        </div>
                                    </div>

                                    {/* Description content */}
                                    <div className="text-sm text-slate-300 whitespace-pre-wrap leading-relaxed">
                                        {selectedQuestion.description}
                                    </div>

                                    {/* Examples */}
                                    {selectedQuestion.type === "CODING" && selectedQuestion.testCases && (
                                        <div className="space-y-4 pt-4 border-t border-[#3e3e3e]">
                                            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">Examples</h4>
                                            {selectedQuestion.testCases.map((tc, idx) => (
                                                <div key={tc.id} className="bg-[#282828] border border-[#3e3e3e] rounded p-4">
                                                    <div className="text-xs font-bold text-[#ffa116] mb-2">Example {idx + 1}</div>
                                                    <div className="space-y-2 text-xs">
                                                        <div>
                                                            <span className="text-slate-500 font-bold">Input:</span>
                                                            <pre className="mt-1 bg-[#1e1e1e] p-2 border border-[#3e3e3e] rounded text-slate-300 font-mono overflow-x-auto">{tc.input}</pre>
                                                        </div>
                                                        <div>
                                                            <span className="text-slate-500 font-bold">Expected Output:</span>
                                                            <pre className="mt-1 bg-[#1e1e1e] p-2 border border-[#3e3e3e] rounded text-slate-300 font-mono overflow-x-auto">{tc.expectedOutput}</pre>
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </>
                            ) : (
                                /* Submissions lists */
                                <div className="space-y-4">
                                    <h3 className="text-sm font-bold text-white uppercase tracking-wider">Attempt Submissions</h3>
                                    {submissionResult ? (
                                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-5">
                                            <div className="flex items-center justify-between mb-4">
                                                <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${
                                                    submissionResult.status === "ACCEPTED" ? "bg-emerald-950 text-emerald-400" : "bg-red-950 text-red-400"
                                                }`}>
                                                    {submissionResult.status}
                                                </span>
                                                <span className="text-xs text-slate-500 font-medium">Score: {submissionResult.score} pts</span>
                                            </div>

                                            <div className="grid grid-cols-2 gap-4 text-xs bg-[#1e1e1e] p-3 rounded">
                                                <div>
                                                    <span className="text-slate-500 font-semibold block uppercase">Runtime Time</span>
                                                    <span className="text-sm font-bold text-slate-300 mt-1 block">{submissionResult.executionTimeMs} ms</span>
                                                </div>
                                                <div>
                                                    <span className="text-slate-500 font-semibold block uppercase">Memory Limit</span>
                                                    <span className="text-sm font-bold text-slate-300 mt-1 block">{submissionResult.executionMemoryMb} MB</span>
                                                </div>
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="text-xs text-slate-500 text-center py-10">
                                            Submit code in workspace to view grade submissions history.
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Right Pane (Code Editor & Test console) */}
                    <div className="w-full lg:w-1/2 flex flex-col bg-[#1e1e1e]">
                        {/* Editor Header controls */}
                        <div className="bg-[#282828] border-b border-[#3e3e3e] px-6 py-2.5 flex items-center justify-between">
                            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                                <Code className="h-4 w-4 text-[#ffa116]" />
                                Code Editor Workspace
                            </span>

                            {selectedQuestion.type === "CODING" && (
                                <div className="flex items-center gap-2">
                                    <span className="text-xs text-slate-500 font-semibold">Language:</span>
                                    <select
                                        value={selectedLanguageId}
                                        onChange={(e) => handleLanguageChange(Number(e.target.value))}
                                        className="bg-[#1a1a1a] border border-[#3e3e3e] text-slate-300 text-xs font-bold rounded px-2.5 py-1 focus:border-[#ffa116] focus:outline-none cursor-pointer"
                                    >
                                        {selectedQuestion.allowedLanguages?.map((lang) => (
                                            <option key={lang.id} value={lang.id}>
                                                {lang.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            )}
                        </div>

                        {/* Editor Body */}
                        <div className="flex-1 p-6 flex flex-col min-h-[350px]">
                            {selectedQuestion.type === "CODING" ? (
                                <textarea
                                    value={sourceCode}
                                    onChange={(e) => setSourceCode(e.target.value)}
                                    className="flex-1 w-full bg-[#1a1a1a] border border-[#3e3e3e] focus:border-[#ffa116] rounded-lg p-4 font-mono text-sm text-slate-100 placeholder-slate-600 focus:outline-none resize-none overflow-y-auto leading-relaxed"
                                    spellCheck={false}
                                />
                            ) : (
                                <div className="flex-grow bg-[#282828] border border-[#3e3e3e] rounded-lg p-6 space-y-4">
                                    <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Select the correct option(s):</p>
                                    <div className="space-y-3">
                                        {selectedQuestion.options?.map((opt) => {
                                            const isChecked = selectedMcqOptions.includes(opt.id);
                                            return (
                                                <div
                                                    key={opt.id}
                                                    onClick={() => handleMcqSelect(opt.id)}
                                                    className={`p-4 border rounded-lg flex items-center gap-3 cursor-pointer transition-all ${
                                                        isChecked
                                                            ? "bg-slate-800 border-[#ffa116] text-[#ffa116]"
                                                            : "bg-[#1a1a1a] border-[#3e3e3e] hover:border-slate-600 text-slate-300"
                                                    }`}
                                                >
                                                    <span className="font-semibold text-xs py-0.5 px-1.5 bg-[#282828] border border-[#3e3e3e] rounded text-slate-400">{opt.id}</span>
                                                    <span className="text-sm">{opt.text}</span>
                                                </div>
                                            );
                                        })}
                                    </div>
                                </div>
                            )}

                            {/* Run vs Submit action buttons */}
                            <div className="mt-4 flex justify-between items-center bg-[#282828] border border-[#3e3e3e] p-3 rounded-lg">
                                {/* Running status indicator */}
                                <div className="text-xs text-slate-400">
                                    {running ? (
                                        <span className="flex items-center gap-1 text-[#ffa116] font-semibold animate-pulse">
                                            <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                                            Running sandbox compiler...
                                        </span>
                                    ) : (
                                        <span>Click Run to execute solution against example testcases.</span>
                                    )}
                                </div>

                                <div className="flex gap-2">
                                    {selectedQuestion.type === "CODING" && (
                                        <button
                                            onClick={handleRunCode}
                                            disabled={running || submitting}
                                            className="flex items-center gap-1 px-4 py-2 border border-[#3e3e3e] hover:border-slate-500 rounded font-semibold text-xs text-slate-300 hover:text-white transition-colors cursor-pointer"
                                        >
                                            <Play className="h-3.5 w-3.5" />
                                            Run Code
                                        </button>
                                    )}
                                    <button
                                        onClick={handleSubmit}
                                        disabled={running || submitting || (selectedQuestion.type === "MCQ" && selectedMcqOptions.length === 0)}
                                        className="flex items-center gap-1 px-4 py-2 bg-[#ffa116] hover:bg-[#ffb03a] text-[#1a1a1a] font-extrabold rounded text-xs transition-colors cursor-pointer"
                                    >
                                        <Send className="h-3.5 w-3.5" />
                                        Submit Code
                                    </button>
                                </div>
                            </div>
                        </div>

                        {/* Inline Run console output results */}
                        {runOutput && (
                            <div className="border-t border-[#3e3e3e] bg-[#282828] p-5">
                                <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">Sandbox Run Output</h4>
                                <div className="p-3 bg-[#1e1e1e] border border-[#3e3e3e] rounded font-mono text-xs">
                                    {runOutput.error ? (
                                        <pre className="text-red-400 whitespace-pre-wrap">{runOutput.error}</pre>
                                    ) : (
                                        <pre className="text-emerald-400 whitespace-pre-wrap">{runOutput.output}</pre>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* Integration checkmodal gating */}
            <AuthModal isOpen={authModalOpen} onClose={() => setAuthModalOpen(false)} />
        </div>
    );
}
