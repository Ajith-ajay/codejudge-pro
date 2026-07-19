"use client";

import React, { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { X, AlertCircle } from "lucide-react";

interface AuthModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess?: () => void;
}

export const AuthModal: React.FC<AuthModalProps> = ({ isOpen, onClose, onSuccess }) => {
    const { login } = useAuth();
    const [isRegister, setIsRegister] = useState(false);
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    
    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    const apiHost = process.env.NEXT_PUBLIC_API_HOST || "http://localhost:8080";

    if (!isOpen) return null;

    const handleLoginSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setErrorMsg(null);

        try {
            const success = await login(username, password);
            if (success) {
                if (onSuccess) onSuccess();
                onClose();
            } else {
                setErrorMsg("Invalid credentials or server error");
            }
        } catch (err) {
            setErrorMsg("Authentication failed");
        } finally {
            setLoading(false);
        }
    };

    const handleRegisterSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setErrorMsg("Passwords do not match");
            return;
        }
        setLoading(true);
        setErrorMsg(null);

        try {
            const res = await fetch(`${apiHost}/api/v1/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, email, password }),
            });
            const data = await res.json();
            if (data.success) {
                // Automatically log them in after registration!
                const success = await login(username, password);
                if (success) {
                    if (onSuccess) onSuccess();
                    onClose();
                }
            } else {
                setErrorMsg(data.message || "Registration failed");
            }
        } catch (err) {
            setErrorMsg("Connection to server failed");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
            <div className="bg-[#282828] border border-[#3e3e3e] w-full max-w-md rounded-lg shadow-2xl relative overflow-hidden text-[#eff1f6]">
                {/* Header */}
                <div className="flex justify-between items-center px-6 py-4 border-b border-[#3e3e3e]">
                    <span className="text-lg font-bold tracking-tight text-white flex items-center gap-2">
                        <span className="text-[#ffa116] font-extrabold text-xl">LC</span>
                        CodeJudge Pro Session
                    </span>
                    <button
                        onClick={onClose}
                        className="text-slate-400 hover:text-white transition-colors cursor-pointer"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                {/* Form Tabs Selector */}
                <div className="flex border-b border-[#3e3e3e] bg-[#1e1e1e]">
                    <button
                        onClick={() => { setIsRegister(false); setErrorMsg(null); }}
                        className={`flex-1 py-3 text-sm font-semibold tracking-wider transition-colors ${
                            !isRegister ? "text-white border-b-2 border-[#ffa116] bg-[#282828]" : "text-slate-400 hover:text-slate-200"
                        }`}
                    >
                        Sign In
                    </button>
                    <button
                        onClick={() => { setIsRegister(true); setErrorMsg(null); }}
                        className={`flex-1 py-3 text-sm font-semibold tracking-wider transition-colors ${
                            isRegister ? "text-white border-b-2 border-[#ffa116] bg-[#282828]" : "text-slate-400 hover:text-slate-200"
                        }`}
                    >
                        Create Account
                    </button>
                </div>

                {/* Body Content */}
                <div className="p-6">
                    {errorMsg && (
                        <div className="mb-4 p-3 bg-red-950/30 border border-red-800 rounded flex gap-2 items-start text-xs text-red-300">
                            <AlertCircle className="h-4 w-4 text-red-400 flex-shrink-0" />
                            <span>{errorMsg}</span>
                        </div>
                    )}

                    {!isRegister ? (
                        <form onSubmit={handleLoginSubmit} className="space-y-4">
                            <div>
                                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Username</label>
                                <input
                                    type="text"
                                    required
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    className="w-full bg-[#1a1a1a] border border-[#3e3e3e] focus:border-[#ffa116] rounded px-3 py-2 text-sm text-slate-100 placeholder-slate-600 focus:outline-none"
                                    placeholder="Enter username"
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Password</label>
                                <input
                                    type="password"
                                    required
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="w-full bg-[#1a1a1a] border border-[#3e3e3e] focus:border-[#ffa116] rounded px-3 py-2 text-sm text-slate-100 placeholder-slate-600 focus:outline-none"
                                    placeholder="Enter password"
                                />
                            </div>
                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full py-2 bg-[#ffa116] hover:bg-[#ffb03a] text-[#1a1a1a] font-bold rounded text-sm transition-all disabled:opacity-50 cursor-pointer"
                            >
                                {loading ? "Signing In..." : "Sign In"}
                            </button>
                        </form>
                    ) : (
                        <form onSubmit={handleRegisterSubmit} className="space-y-4">
                            <div>
                                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Username</label>
                                <input
                                    type="text"
                                    required
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    className="w-full bg-[#1a1a1a] border border-[#3e3e3e] focus:border-[#ffa116] rounded px-3 py-2 text-sm text-slate-100 placeholder-slate-600 focus:outline-none"
                                    placeholder="Username"
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Email Address</label>
                                <input
                                    type="email"
                                    required
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="w-full bg-[#1a1a1a] border border-[#3e3e3e] focus:border-[#ffa116] rounded px-3 py-2 text-sm text-slate-100 placeholder-slate-600 focus:outline-none"
                                    placeholder="Email"
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Password</label>
                                <input
                                    type="password"
                                    required
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="w-full bg-[#1a1a1a] border border-[#3e3e3e] focus:border-[#ffa116] rounded px-3 py-2 text-sm text-slate-100 placeholder-slate-600 focus:outline-none"
                                    placeholder="Password"
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Confirm Password</label>
                                <input
                                    type="password"
                                    required
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    className="w-full bg-[#1a1a1a] border border-[#3e3e3e] focus:border-[#ffa116] rounded px-3 py-2 text-sm text-slate-100 placeholder-slate-600 focus:outline-none"
                                    placeholder="Confirm password"
                                />
                            </div>
                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full py-2 bg-[#ffa116] hover:bg-[#ffb03a] text-[#1a1a1a] font-bold rounded text-sm transition-all disabled:opacity-50 cursor-pointer"
                            >
                                {loading ? "Creating Account..." : "Create Account"}
                            </button>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
};
