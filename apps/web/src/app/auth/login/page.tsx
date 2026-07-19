"use client";

import React, { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Lock, User as UserIcon, AlertCircle } from "lucide-react";

export default function LoginPage() {
    const { login, error, loading } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (username.trim() && password.trim()) {
            await login(username, password);
        }
    };

    return (
        <div className="flex-1 flex items-center justify-center min-h-screen bg-[#090d16] px-4 relative overflow-hidden">
            {/* Background glowing decorations */}
            <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-900/20 rounded-full blur-[100px] pointer-events-none" />
            <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-900/20 rounded-full blur-[100px] pointer-events-none" />

            <div className="w-full max-w-md bg-[#131b2e] border border-[#1e293b] rounded-xl shadow-2xl p-8 backdrop-blur-md relative z-10">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-extrabold bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 bg-clip-text text-transparent tracking-tight">
                        CodeJudge Pro
                    </h1>
                    <p className="text-slate-400 text-sm mt-2">
                        Online assessment and competitive learning platform
                    </p>
                </div>

                {error && (
                    <div className="mb-6 p-4 bg-red-950/40 border border-red-800 rounded-lg flex items-start gap-3">
                        <AlertCircle className="h-5 w-5 text-red-400 mt-0.5 flex-shrink-0" />
                        <span className="text-sm text-red-300">{error}</span>
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Username */}
                    <div>
                        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                            Username / Email
                        </label>
                        <div className="relative">
                            <span className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                                <UserIcon className="h-5 w-5" />
                            </span>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                className="w-full bg-[#090d16] border border-[#1e293b] focus:border-indigo-500 rounded-lg py-2.5 pl-10 pr-4 text-slate-100 placeholder-slate-500 text-sm focus:outline-none transition-colors"
                                placeholder="Enter your username"
                            />
                        </div>
                    </div>

                    {/* Password */}
                    <div>
                        <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                            Password
                        </label>
                        <div className="relative">
                            <span className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                                <Lock className="h-5 w-5" />
                            </span>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                className="w-full bg-[#090d16] border border-[#1e293b] focus:border-indigo-500 rounded-lg py-2.5 pl-10 pr-4 text-slate-100 placeholder-slate-500 text-sm focus:outline-none transition-colors"
                                placeholder="Enter your password"
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-semibold py-2.5 rounded-lg text-sm shadow-lg hover:shadow-indigo-500/20 active:scale-[0.98] transition-all duration-200 disabled:opacity-50 cursor-pointer"
                    >
                        {loading ? "Authenticating..." : "Sign In"}
                    </button>
                </form>

                <div className="mt-8 pt-6 border-t border-[#1e293b] text-center">
                    <p className="text-slate-500 text-xs">
                        Default admin: <code className="text-indigo-400 bg-slate-950 px-1 py-0.5 rounded">admin / admin123</code> <br />
                        Default candidate: <code className="text-indigo-400 bg-slate-950 px-1 py-0.5 rounded">candidate / candidate123</code>
                    </p>
                </div>
            </div>
        </div>
    );
}
