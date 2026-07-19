"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navbar } from "@/components/Navbar";
import { Award, ShieldAlert, Clock, User, ChevronRight } from "lucide-react";

interface LeaderboardEntry {
    rank: number;
    userId: number;
    username: string;
    email: string;
    score: number;
    lastSubmittedAt: string | null;
}

interface Exam {
    id: number;
    title: string;
    startTime: string;
}

export default function LeaderboardPage() {
    const { token } = useAuth();
    const [exams, setExams] = useState<Exam[]>([]);
    const [selectedExamId, setSelectedExamId] = useState<number | null>(null);
    const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
    const [loading, setLoading] = useState(true);

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

    // 1. Fetch available exams list
    useEffect(() => {
        fetchExams();
    }, []);

    const fetchExams = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${apiHost}/api/v1/exams?size=50`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success && data.data.content) {
                setExams(data.data.content);
                if (data.data.content.length > 0) {
                    setSelectedExamId(data.data.content[0].id);
                    fetchLeaderboard(data.data.content[0].id);
                } else {
                    setLoading(false);
                }
            } else {
                setLoading(false);
            }
        } catch (err) {
            console.error("Failed to load exams", err);
            setLoading(false);
        }
    };

    // 2. Fetch leaderboard entries for the selected exam
    const fetchLeaderboard = async (examId: number) => {
        setLoading(true);
        try {
            const res = await fetch(`${apiHost}/api/v1/exams/${examId}/leaderboard`, {
                headers: getAuthHeaders(),
            });
            const data = await res.json();
            if (data.success) {
                setLeaderboard(data.data);
            }
        } catch (err) {
            console.error("Failed to fetch leaderboard", err);
        } finally {
            setLoading(false);
        }
    };

    const handleExamChange = (examId: number) => {
        setSelectedExamId(examId);
        fetchLeaderboard(examId);
    };

    return (
        <div className="min-h-screen bg-[#090d16] flex flex-col">
            <Navbar />

            <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
                    <div>
                        <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
                            <Award className="h-6 w-6 text-indigo-400" />
                            Live Contest Standings
                        </h1>
                        <p className="text-slate-400 text-sm mt-1">Real-time scoreboard for active coding assessments and exams.</p>
                    </div>

                    {/* Exam Dropdown Selector */}
                    {exams.length > 0 && (
                        <div className="flex items-center gap-2">
                            <span className="text-sm text-slate-400 font-medium">Select Exam:</span>
                            <select
                                value={selectedExamId || ""}
                                onChange={(e) => handleExamChange(Number(e.target.value))}
                                className="bg-[#131b2e] border border-[#1e293b] text-slate-200 text-sm font-semibold rounded-lg px-3 py-2 focus:border-indigo-500 focus:outline-none w-64"
                            >
                                {exams.map((ex) => (
                                    <option key={ex.id} value={ex.id}>
                                        {ex.title}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}
                </div>

                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
                    </div>
                ) : exams.length === 0 ? (
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-12 text-center">
                        <ShieldAlert className="h-10 w-10 text-slate-600 mx-auto mb-4" />
                        <h3 className="text-slate-300 font-semibold text-lg">No exam sessions found</h3>
                        <p className="text-slate-500 text-sm mt-1">Standings will become available once an exam has been scheduled.</p>
                    </div>
                ) : leaderboard.length === 0 ? (
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl p-12 text-center">
                        <Award className="h-10 w-10 text-slate-600 mx-auto mb-4" />
                        <h3 className="text-slate-300 font-semibold text-lg">No entries on the leaderboard</h3>
                        <p className="text-slate-500 text-sm mt-1">Standings will update as soon as candidates submit their solutions.</p>
                    </div>
                ) : (
                    <div className="bg-[#131b2e] border border-[#1e293b] rounded-xl overflow-hidden shadow-xl">
                        <table className="min-w-full divide-y divide-[#1e293b]">
                            <thead className="bg-[#0f1524]">
                                <tr>
                                    <th scope="col" className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-400 w-16">Rank</th>
                                    <th scope="col" className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">Username</th>
                                    <th scope="col" className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">Email</th>
                                    <th scope="col" className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">Score</th>
                                    <th scope="col" className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-400">Last Submission</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-[#1e293b] bg-[#131b2e]">
                                {leaderboard.map((entry) => (
                                    <tr key={entry.userId} className="hover:bg-[#1a233b]/40 transition-colors">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-extrabold">
                                            <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs ${
                                                entry.rank === 1 ? "bg-amber-500/20 text-amber-400 border border-amber-500/30" :
                                                entry.rank === 2 ? "bg-slate-400/20 text-slate-300 border border-slate-400/30" :
                                                entry.rank === 3 ? "bg-orange-500/20 text-orange-400 border border-orange-500/30" :
                                                "text-slate-400"
                                            }`}>
                                                {entry.rank}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-slate-200">
                                            <div className="flex items-center gap-2">
                                                <User className="h-4 w-4 text-indigo-400" />
                                                {entry.username}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-400">{entry.email}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-indigo-400">{entry.score} pts</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-xs font-mono text-slate-400">
                                            {entry.lastSubmittedAt ? (
                                                <span className="flex items-center justify-end gap-1.5">
                                                    <Clock className="h-3.5 w-3.5 text-slate-500" />
                                                    {new Date(entry.lastSubmittedAt).toLocaleTimeString()}
                                                </span>
                                            ) : (
                                                "-"
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>
        </div>
    );
}
