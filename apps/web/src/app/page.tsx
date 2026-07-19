"use client";

import React, { useEffect, useState } from "react";
import { Navbar } from "@/components/Navbar";
import { AuthModal } from "@/components/AuthModal";
import { Trophy, ShieldCheck, Newspaper, Award, FileText, CheckCircle2, ArrowRight, Server, Brain } from "lucide-react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";

interface Exam {
    id: number;
    title: string;
    description: string;
    durationMinutes: number;
    startTime: string;
    endTime: string;
    published: boolean;
}

interface Submission {
    id: number;
    questionId: number;
    status: string;
}

export default function LandingExplorePage() {
    const { user, token } = useAuth();
    const [authModalOpen, setAuthModalOpen] = useState(false);
    
    // API data states
    const [activeExams, setActiveExams] = useState<Exam[]>([]);
    const [loadingExams, setLoadingExams] = useState(false);
    const [solvedCount, setSolvedCount] = useState<number>(0);
    const [totalProblems, setTotalProblems] = useState<number>(0);
    const [loadingStats, setLoadingStats] = useState(false);

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
        if (user) {
            fetchActiveExams();
            fetchUserStats();
        }
    }, [user]);

    const fetchActiveExams = async () => {
        setLoadingExams(true);
        try {
            const res = await fetch(`${apiHost}/api/v1/exams/candidate/active`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                setActiveExams(data.data || []);
            }
        } catch (err) {
            console.error("Failed to load active exams", err);
        } finally {
            setLoadingExams(false);
        }
    };

    const fetchUserStats = async () => {
        setLoadingStats(true);
        try {
            // Fetch public questions count
            const questionsRes = await fetch(`${apiHost}/api/v1/questions?size=1`, {
                headers: getAuthHeaders(),
            });
            const questionsData = await questionsRes.json();
            if (questionsData.success && questionsData.data) {
                setTotalProblems(questionsData.data.totalElements || 0);
            }

            // Fetch my submissions to compute solved count
            const submissionsRes = await fetch(`${apiHost}/api/v1/submissions/my-submissions`, {
                headers: getAuthHeaders(),
            });
            const submissionsData = await submissionsRes.json();
            if (submissionsData.success && submissionsData.data) {
                const list: Submission[] = submissionsData.data;
                const uniqueSolvedIds = new Set(
                    list
                        .filter(s => s.status === "ACCEPTED")
                        .map(s => s.questionId)
                );
                setSolvedCount(uniqueSolvedIds.size);
            }
        } catch (err) {
            console.error("Failed to load stats", err);
        } finally {
            setLoadingStats(false);
        }
    };

    const handleProtectedClick = (e: React.MouseEvent) => {
        if (!user) {
            e.preventDefault();
            setAuthModalOpen(true);
        }
    };

    return (
        <div className="min-h-screen bg-[#1a1a1a] text-[#eff1f6] flex flex-col font-sans">
            <Navbar />

            <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Hero / Welcome Banner */}
                <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-8 mb-8 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-80 h-full bg-[#ffa116]/5 rounded-full blur-3xl pointer-events-none" />
                    <h1 className="text-2xl md:text-3xl font-black text-white tracking-wide">
                        Welcome to <span className="text-[#ffa116]">CodeJudge Pro</span>
                    </h1>
                    <p className="text-sm text-slate-400 mt-2 max-w-2xl leading-relaxed">
                        An enterprise online coding assessment and practice sandbox. Tackle custom compiler challenges, verify your logic inside isolated Docker runtime environments, and participate in scheduled assessments.
                    </p>
                    
                    <div className="mt-6 flex flex-wrap gap-3">
                        <Link
                            href="/practice"
                            className="px-5 py-2 bg-[#ffa116] hover:bg-[#ffb03a] text-[#1a1a1a] font-extrabold text-xs rounded transition-colors flex items-center gap-1.5"
                        >
                            Browse Problems <ArrowRight className="h-4 w-4" />
                        </Link>
                        {!user && (
                            <button
                                onClick={() => setAuthModalOpen(true)}
                                className="px-5 py-2 bg-[#3e3e3e] hover:bg-[#4e4e4e] text-slate-200 border border-[#3e3e3e] font-bold text-xs rounded transition-colors cursor-pointer"
                            >
                                Get Started
                            </button>
                        )}
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Left Columns: Assessments Lobbies & Notices */}
                    <div className="lg:col-span-2 space-y-6">
                        
                        {/* Exams Section */}
                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                            <h2 className="text-base font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-2">
                                <Trophy className="h-5 w-5 text-[#ffa116]" />
                                Timed Exam Assessments
                            </h2>

                            {!user ? (
                                <div className="p-6 bg-[#1e1e1e] border border-[#3e3e3e] rounded text-center">
                                    <p className="text-xs text-slate-400 mb-4">
                                        Sign in to check your active exam schedules and register for pending assessments.
                                    </p>
                                    <button
                                        onClick={() => setAuthModalOpen(true)}
                                        className="px-4 py-1.5 bg-[#ffa116] hover:bg-[#ffb03a] text-[#1a1a1a] font-bold text-xs rounded transition-colors cursor-pointer"
                                    >
                                        Log In to Dashboard
                                    </button>
                                </div>
                            ) : loadingExams ? (
                                <div className="py-6 flex justify-center">
                                    <div className="w-6 h-6 border-2 border-[#ffa116] border-t-transparent rounded-full animate-spin" />
                                </div>
                            ) : activeExams.length === 0 ? (
                                <div className="p-6 bg-[#1e1e1e] border border-[#3e3e3e] rounded text-center">
                                    <ShieldCheck className="h-8 w-8 text-emerald-400 mx-auto mb-2 opacity-80" />
                                    <h3 className="text-xs font-bold text-white uppercase tracking-wider">No Exams Scheduled</h3>
                                    <p className="text-[11px] text-slate-400 mt-1 max-w-sm mx-auto leading-relaxed">
                                        You don't have any active exam schedules at this moment. You can browse problems to keep practicing.
                                    </p>
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {activeExams.map((exam) => (
                                        <div key={exam.id} className="flex flex-col md:flex-row md:items-center justify-between p-4 bg-[#1e1e1e] border border-[#3e3e3e] rounded hover:border-[#ffa116]/50 transition-colors gap-4">
                                            <div>
                                                <div className="text-[10px] text-[#ffa116] font-bold uppercase tracking-widest">Active Assessment</div>
                                                <h3 className="text-sm font-bold text-white mt-1">{exam.title}</h3>
                                                <p className="text-xs text-slate-400 mt-0.5">{exam.durationMinutes} minutes duration</p>
                                            </div>
                                            <Link
                                                href="/exams"
                                                className="text-center text-xs font-bold bg-[#ffa116] text-[#1a1a1a] hover:bg-[#ffb03a] px-4 py-2 rounded transition-colors whitespace-nowrap"
                                            >
                                                Enter Exam Lobby
                                            </Link>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* CodeJudge Pro Announcements Feed */}
                        <div className="space-y-4">
                            <h2 className="text-xs font-bold text-slate-400 uppercase tracking-widest px-1 flex items-center gap-1.5">
                                <Newspaper className="h-4 w-4" /> Platform Publications
                            </h2>

                            <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                                <div className="flex justify-between text-[10px] text-slate-500 mb-1">
                                    <span>System Administrator</span>
                                    <span>Today</span>
                                </div>
                                <h3 className="text-sm font-bold text-white">Isolated Compiler Sandbox Runtime</h3>
                                <p className="text-xs text-slate-400 mt-2 leading-relaxed">
                                    CodeJudge Pro runs all submitted code fragments inside disposable Docker containers configured with exact CPU and Memory limit margins. Be mindful of loops and excessive allocation to prevent Memory Limit Exceeded (MLE) or Time Limit Exceeded (TLE) responses!
                                </p>
                            </div>

                            <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                                <div className="flex justify-between text-[10px] text-slate-500 mb-1">
                                    <span>Security Auditor</span>
                                    <span>2 days ago</span>
                                </div>
                                <h3 className="text-sm font-bold text-white">Exam Proctoring & Telemetry Integrity</h3>
                                <p className="text-xs text-slate-400 mt-2 leading-relaxed">
                                    Invited candidates entering active exams are subject to strict anti-cheat proctoring rules. Active window tab switches, navigation events, and cursor exit metrics are recorded and sent directly to the Spring Boot administration dashboard audit log.
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Right Column: User stats and compiler info */}
                    <div className="space-y-6">
                        {/* Solved Progress Overview */}
                        {user && (
                            <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">Your Progress Stats</h3>
                                
                                {loadingStats ? (
                                    <div className="py-4 flex justify-center">
                                        <div className="w-5 h-5 border-2 border-[#ffa116] border-t-transparent rounded-full animate-spin" />
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center gap-2">
                                                <CheckCircle2 className="h-5 w-5 text-emerald-400" />
                                                <span className="text-xs font-semibold text-slate-200">Unique Solved</span>
                                            </div>
                                            <span className="text-sm font-bold text-white">{solvedCount} / {totalProblems}</span>
                                        </div>
                                        <div className="w-full bg-[#1e1e1e] h-2 rounded overflow-hidden">
                                            <div 
                                                className="bg-emerald-400 h-full rounded transition-all duration-500" 
                                                style={{ width: `${totalProblems > 0 ? (solvedCount / totalProblems) * 100 : 0}%` }}
                                            />
                                        </div>
                                        <Link 
                                            href="/profile"
                                            className="block text-center text-xs font-semibold text-[#ffa116] hover:text-[#ffb03a] pt-2"
                                        >
                                            View Profile Heatmap & Stats
                                        </Link>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Compiler sandbox status */}
                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-1.5">
                                <Server className="h-4 w-4 text-emerald-400" /> Compiler Sandboxes
                            </h3>
                            
                            <div className="space-y-3 text-xs">
                                <div className="flex justify-between items-center p-2 bg-[#1e1e1e] rounded">
                                    <span className="font-medium text-slate-300">Java Runtime</span>
                                    <span className="px-1.5 py-0.5 bg-emerald-500/10 text-emerald-400 rounded text-[10px] font-bold">ONLINE (v21)</span>
                                </div>
                                <div className="flex justify-between items-center p-2 bg-[#1e1e1e] rounded">
                                    <span className="font-medium text-slate-300">Python Compiler</span>
                                    <span className="px-1.5 py-0.5 bg-emerald-500/10 text-emerald-400 rounded text-[10px] font-bold">ONLINE (v3.11)</span>
                                </div>
                                <div className="flex justify-between items-center p-2 bg-[#1e1e1e] rounded">
                                    <span className="font-medium text-slate-300">C++ Sandbox</span>
                                    <span className="px-1.5 py-0.5 bg-emerald-500/10 text-emerald-400 rounded text-[10px] font-bold">ONLINE (GCC 13)</span>
                                </div>
                            </div>
                        </div>

                        {/* Quick Tips */}
                        <div className="bg-[#282828] border border-[#3e3e3e] rounded-lg p-6">
                            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
                                <Brain className="h-4 w-4 text-[#ffa116]" /> Learning Tips
                            </h3>
                            <p className="text-xs text-slate-400 leading-relaxed">
                                General practice submissions are saved to your profile and build your contribution calendar history. Submit daily solutions to check code optimization speeds and improve execution performance!
                            </p>
                        </div>
                    </div>
                </div>
            </main>

            <AuthModal isOpen={authModalOpen} onClose={() => setAuthModalOpen(false)} />
        </div>
    );
}
