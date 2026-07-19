"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navbar } from "@/components/Navbar";
import { ShieldAlert, BookOpen, Layers, Users, Calendar, Plus, BarChart2 } from "lucide-react";

export default function AdminDashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState({
        totalExams: 3,
        totalQuestions: 24,
        activeCandidates: 18,
    });

    return (
        <div className="min-h-screen bg-[#090d16] flex flex-col">
            <Navbar />

            <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Hero */}
                <div className="bg-gradient-to-r from-purple-950/40 via-indigo-950/20 to-transparent border border-[#1e293b] rounded-xl p-8 mb-8 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-80 h-full bg-purple-500/10 rounded-full blur-[80px] pointer-events-none" />
                    <h1 className="text-3xl font-bold text-white tracking-tight">
                        Admin Workspace
                    </h1>
                    <p className="text-slate-400 mt-2 max-w-xl text-sm leading-relaxed">
                        Control center for managing CodeJudge Pro. Create and schedule assessments, configure coding questions and test cases, monitor anti-cheating logs, and view candidate grade reports.
                    </p>
                </div>

                {/* Stats Grid */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex items-center gap-4">
                        <div className="p-3 bg-purple-500/10 rounded-lg text-purple-400 border border-purple-500/20">
                            <Calendar className="h-6 w-6" />
                        </div>
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Total Exams</p>
                            <h3 className="text-2xl font-bold text-slate-200 mt-1">{stats.totalExams}</h3>
                        </div>
                    </div>

                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex items-center gap-4">
                        <div className="p-3 bg-indigo-500/10 rounded-lg text-indigo-400 border border-indigo-500/20">
                            <BookOpen className="h-6 w-6" />
                        </div>
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Total Questions</p>
                            <h3 className="text-2xl font-bold text-slate-200 mt-1">{stats.totalQuestions}</h3>
                        </div>
                    </div>

                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6 flex items-center gap-4">
                        <div className="p-3 bg-emerald-500/10 rounded-lg text-emerald-400 border border-emerald-500/20">
                            <Users className="h-6 w-6" />
                        </div>
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Active Candidates</p>
                            <h3 className="text-2xl font-bold text-slate-200 mt-1">{stats.activeCandidates}</h3>
                        </div>
                    </div>
                </div>

                {/* Split Action Panels */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    {/* Active Exam Sessions */}
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6">
                        <h2 className="text-lg font-bold text-slate-200 mb-6 flex items-center gap-2">
                            <BarChart2 className="h-5 w-5 text-indigo-400" />
                            Recent Exam Sessions
                        </h2>
                        <div className="space-y-4">
                            {[
                                { title: "Java Spring Certification Exam", candidates: 12, status: "IN_PROGRESS" },
                                { title: "Python Algorithm Assessment", candidates: 8, status: "COMPLETED" },
                                { title: "Frontend CSS/HTML Evaluation", candidates: 14, status: "COMPLETED" },
                            ].map((session, idx) => (
                                <div key={idx} className="flex items-center justify-between p-4 bg-[#090d16] border border-[#1e293b] rounded-lg">
                                    <div>
                                        <h4 className="font-semibold text-slate-200 text-sm">{session.title}</h4>
                                        <p className="text-xs text-slate-500 mt-1">{session.candidates} candidates registered</p>
                                    </div>
                                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                        session.status === "IN_PROGRESS"
                                            ? "bg-indigo-950 text-indigo-400 border border-indigo-900"
                                            : "bg-slate-900 text-slate-400 border border-slate-800"
                                    }`}>
                                        {session.status}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Quick Admin Actions */}
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-6">
                        <h2 className="text-lg font-bold text-slate-200 mb-6 flex items-center gap-2">
                            <Plus className="h-5 w-5 text-indigo-400" />
                            Quick Setup Commands
                        </h2>
                        
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <button className="p-4 bg-[#090d16] border border-[#1e293b] hover:border-indigo-500/50 rounded-lg text-left transition-all group cursor-pointer">
                                <h4 className="font-semibold text-slate-200 text-sm group-hover:text-indigo-400">Schedule Exam</h4>
                                <p className="text-xs text-slate-500 mt-1">Configure candidate email invitations and assessment timers.</p>
                            </button>

                            <button className="p-4 bg-[#090d16] border border-[#1e293b] hover:border-indigo-500/50 rounded-lg text-left transition-all group cursor-pointer">
                                <h4 className="font-semibold text-slate-200 text-sm group-hover:text-indigo-400">Create MCQ</h4>
                                <p className="text-xs text-slate-500 mt-1">Design single or multiple-choice questions with partial marking rules.</p>
                            </button>

                            <button className="p-4 bg-[#090d16] border border-[#1e293b] hover:border-indigo-500/50 rounded-lg text-left transition-all group cursor-pointer">
                                <h4 className="font-semibold text-slate-200 text-sm group-hover:text-indigo-400">Add Coding Problem</h4>
                                <p className="text-xs text-slate-500 mt-1">Configure compilation limits, template structures, and testing criteria.</p>
                            </button>

                            <button className="p-4 bg-[#090d16] border border-[#1e293b] hover:border-indigo-500/50 rounded-lg text-left transition-all group cursor-pointer">
                                <h4 className="font-semibold text-slate-200 text-sm group-hover:text-indigo-400">Proctoring Telemetry</h4>
                                <p className="text-xs text-slate-500 mt-1">Review student browser activity logs and tab switching warnings.</p>
                            </button>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
