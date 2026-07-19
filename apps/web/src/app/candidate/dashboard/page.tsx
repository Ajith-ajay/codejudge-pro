"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navbar } from "@/components/Navbar";
import Link from "next/link";
import { BookOpen, Calendar, Award, CheckCircle2, ChevronRight, Play } from "lucide-react";

export default function CandidateDashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState({
        solvedCount: 12,
        unsolvedCount: 4,
        rank: 18,
        activeExams: 1,
    });

    return (
        <div className="min-h-screen bg-[#090d16] flex flex-col">
            <Navbar />
            
            <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Welcome Hero */}
                <div className="bg-gradient-to-r from-indigo-900/40 via-purple-900/20 to-transparent border border-[#1e293b] rounded-xl p-8 mb-8 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-80 h-full bg-indigo-500/10 rounded-full blur-[80px] pointer-events-none" />
                    <div className="relative z-10">
                        <h1 className="text-3xl font-bold text-white tracking-tight">
                            Welcome back, <span className="text-indigo-400 font-extrabold">{user?.username}</span>!
                        </h1>
                        <p className="text-slate-400 mt-2 max-w-xl text-sm md:text-base leading-relaxed">
                            Continuous practice leads to mastery. Solve coding challenges, attempt topic-wise MCQs, or join scheduled assessments to level up your ranking.
                        </p>
                        <div className="mt-6 flex flex-wrap gap-4">
                            <Link
                                href="/practice"
                                className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold px-5 py-2.5 rounded-lg text-sm transition-all shadow-lg shadow-indigo-600/20 active:scale-[0.98] cursor-pointer"
                            >
                                <Play className="h-4 w-4" />
                                Start Practicing
                            </Link>
                            <Link
                                href="/exams"
                                className="flex items-center gap-2 bg-[#131b2e] hover:bg-[#1e294b] border border-[#1e293b] text-slate-300 hover:text-white font-semibold px-5 py-2.5 rounded-lg text-sm transition-all cursor-pointer"
                            >
                                View Scheduled Exams
                            </Link>
                        </div>
                    </div>
                </div>

                {/* Dashboard Stats */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                    {/* Solved Problems */}
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex items-center gap-4">
                        <div className="p-3 bg-emerald-500/10 rounded-lg text-emerald-400 border border-emerald-500/20">
                            <CheckCircle2 className="h-6 w-6" />
                        </div>
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Solved Problems</p>
                            <h3 className="text-2xl font-bold text-slate-200 mt-1">{stats.solvedCount}</h3>
                        </div>
                    </div>

                    {/* Rank */}
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex items-center gap-4">
                        <div className="p-3 bg-amber-500/10 rounded-lg text-amber-400 border border-amber-500/20">
                            <Award className="h-6 w-6" />
                        </div>
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Global Ranking</p>
                            <h3 className="text-2xl font-bold text-slate-200 mt-1">#{stats.rank}</h3>
                        </div>
                    </div>

                    {/* Active Exams */}
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex items-center gap-4">
                        <div className="p-3 bg-indigo-500/10 rounded-lg text-indigo-400 border border-indigo-500/20">
                            <Calendar className="h-6 w-6" />
                        </div>
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Pending Exams</p>
                            <h3 className="text-2xl font-bold text-slate-200 mt-1">{stats.activeExams}</h3>
                        </div>
                    </div>

                    {/* Practice Questions */}
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex items-center gap-4">
                        <div className="p-3 bg-purple-500/10 rounded-lg text-purple-400 border border-purple-500/20">
                            <BookOpen className="h-6 w-6" />
                        </div>
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Incomplete Tasks</p>
                            <h3 className="text-2xl font-bold text-slate-200 mt-1">{stats.unsolvedCount}</h3>
                        </div>
                    </div>
                </div>

                {/* Dashboard Panels */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Practice Highlight */}
                    <div className="lg:col-span-2 bg-[#131b2e] border border-[#1e293b] rounded-xl p-6">
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-lg font-bold text-slate-200">Recommended for You</h2>
                            <Link href="/practice" className="text-xs text-indigo-400 hover:text-indigo-300 font-semibold flex items-center gap-1">
                                View all <ChevronRight className="h-3 w-3" />
                            </Link>
                        </div>
                        
                        <div className="space-y-4">
                            {[
                                { title: "Two Sum", difficulty: "EASY", type: "CODING", score: 10 },
                                { title: "SQL Window Functions Challenge", difficulty: "MEDIUM", type: "CODING", score: 20 },
                                { title: "Java Collections MCQ Quiz", difficulty: "EASY", type: "MCQ", score: 10 },
                            ].map((prob, idx) => (
                                <div key={idx} className="flex items-center justify-between p-4 bg-[#090d16] border border-[#1e293b] hover:border-indigo-500/50 rounded-lg transition-all">
                                    <div>
                                        <h4 className="font-semibold text-slate-200 text-sm">{prob.title}</h4>
                                        <div className="flex gap-3 items-center mt-1.5">
                                            <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                                                prob.difficulty === "EASY" ? "bg-emerald-950 text-emerald-300" : "bg-amber-950 text-amber-300"
                                            }`}>
                                                {prob.difficulty}
                                            </span>
                                            <span className="text-[10px] font-medium text-slate-400 bg-slate-900 px-2 py-0.5 rounded border border-[#1e293b]">
                                                {prob.type}
                                            </span>
                                            <span className="text-[10px] text-slate-500">{prob.score} Marks</span>
                                        </div>
                                    </div>
                                    <Link
                                        href={`/practice?id=${idx + 1}`}
                                        className="text-xs font-semibold bg-indigo-950 text-indigo-400 border border-indigo-800 hover:bg-indigo-900 rounded-lg px-3 py-1.5 transition-colors cursor-pointer"
                                    >
                                        Solve
                                    </Link>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Upcoming exam notices */}
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex flex-col">
                        <h2 className="text-lg font-bold text-slate-200 mb-6">Upcoming Exam Notifications</h2>
                        
                        <div className="flex-1 space-y-4">
                            <div className="p-4 bg-indigo-950/20 border border-indigo-900/50 rounded-lg relative overflow-hidden">
                                <div className="absolute top-0 right-0 w-2 h-full bg-indigo-500" />
                                <h4 className="font-bold text-indigo-300 text-sm">Java Spring Certification Exam</h4>
                                <p className="text-xs text-slate-400 mt-1">Scheduled for: July 12, 10:00 AM</p>
                                <p className="text-[10px] text-slate-500 mt-2">Duration: 120 minutes</p>
                                <Link
                                    href="/exams"
                                    className="mt-3 block text-center text-xs font-semibold bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg py-2 transition-colors cursor-pointer"
                                >
                                    Go to Exam Lobby
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
