"use client";

import React, { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { AuthModal } from "@/components/AuthModal";
import { Menu, X, ChevronDown, User, BookOpen, Calendar, Award, LogOut, Settings, List, Award as PointsIcon, Compass } from "lucide-react";

export const Navbar: React.FC = () => {
    const { user, logout } = useAuth();
    const pathname = usePathname();
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [profileMenuOpen, setProfileMenuOpen] = useState(false);
    const [authModalOpen, setAuthModalOpen] = useState(false);

    const isActive = (href: string) => {
        if (href === "/") return pathname === "/";
        return pathname.startsWith(href);
    };

    const navLinks = [
        { name: "Explore", href: "/", icon: Compass },
        { name: "Problems", href: "/practice", icon: BookOpen },
        { name: "Contest", href: "/contest", icon: Award },
        { name: "Exams Portal", href: "/exams", icon: Calendar },
        { name: "Leaderboard", href: "/leaderboard", icon: Award },
    ];

    return (
        <nav className="bg-[#282828] border-b border-[#3e3e3e] sticky top-0 z-50 shadow-md text-[#eff1f6]">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-14">
                    {/* Logo & Navigation */}
                    <div className="flex items-center gap-6">
                        <Link href="/" className="flex items-center gap-2 mr-2">
                            {/* LeetCode Mock yellow/black logo */}
                            <div className="w-6 h-6 rounded bg-[#ffa116] flex items-center justify-center text-[#1a1a1a] font-black text-sm">
                                L
                            </div>
                            <span className="text-base font-bold text-white tracking-wide uppercase">
                                CodeJudge Pro
                            </span>
                        </Link>

                        {/* Desktop Navigation */}
                        <div className="hidden md:flex items-center space-x-1">
                            {navLinks.map((link) => {
                                const active = isActive(link.href);
                                return (
                                    <Link
                                        key={link.name}
                                        href={link.href}
                                        className={`px-3 py-1.5 rounded text-xs font-semibold tracking-wider transition-colors ${
                                            active
                                                ? "text-white border-b-2 border-[#ffa116]"
                                                : "text-slate-400 hover:text-white"
                                        }`}
                                    >
                                        {link.name}
                                    </Link>
                                );
                            })}
                        </div>
                    </div>

                    {/* Right Controls */}
                    <div className="hidden md:flex items-center gap-4">
                        {user ? (
                            <div className="relative">
                                {/* Profile Dropdown Trigger */}
                                <button
                                    onClick={() => setProfileMenuOpen(!profileMenuOpen)}
                                    className="flex items-center gap-1.5 focus:outline-none hover:bg-[#333] px-2.5 py-1.5 rounded transition-colors cursor-pointer"
                                >
                                    <div className="w-6 h-6 rounded-full bg-[#3e3e3e] flex items-center justify-center text-xs font-bold text-[#ffa116]">
                                        {user.username.charAt(0).toUpperCase()}
                                    </div>
                                    <span className="text-xs font-bold text-slate-300">{user.username}</span>
                                    <ChevronDown className="h-3 w-3 text-slate-400" />
                                </button>

                                {/* Dropdown Menu (LeetCode screenshot-style) */}
                                {profileMenuOpen && (
                                    <div className="absolute right-0 mt-2 w-72 bg-[#282828] border border-[#3e3e3e] rounded-lg shadow-2xl z-50 text-[#eff1f6] overflow-hidden py-2 animate-fade-in">
                                        <div className="px-4 py-3 border-b border-[#3e3e3e] flex items-center gap-3">
                                            <div className="w-10 h-10 rounded-full bg-[#3e3e3e] flex items-center justify-center text-lg font-bold text-[#ffa116]">
                                                {user.username.charAt(0).toUpperCase()}
                                            </div>
                                            <div>
                                                <div className="font-bold text-sm text-white">{user.username}</div>
                                                <div className="text-xs text-slate-400 truncate max-w-[180px]">{user.email}</div>
                                            </div>
                                        </div>

                                        {/* Grid stats mock (Lists, Notebook, Progress, Points) */}
                                        <div className="grid grid-cols-3 gap-2 p-3 bg-[#1e1e1e] border-b border-[#3e3e3e] text-center">
                                            <Link href="/profile" onClick={() => setProfileMenuOpen(false)} className="p-2 hover:bg-[#282828] rounded transition-colors">
                                                <List className="h-4 w-4 mx-auto text-[#ffa116] mb-1" />
                                                <span className="text-[10px] text-slate-400 font-semibold uppercase block">Profile</span>
                                            </Link>
                                            <div className="p-2 hover:bg-[#282828] rounded transition-colors cursor-not-allowed">
                                                <Compass className="h-4 w-4 mx-auto text-emerald-400 mb-1" />
                                                <span className="text-[10px] text-slate-400 font-semibold uppercase block">Notebook</span>
                                            </div>
                                            <div className="p-2 hover:bg-[#282828] rounded transition-colors cursor-not-allowed">
                                                <PointsIcon className="h-4 w-4 mx-auto text-purple-400 mb-1" />
                                                <span className="text-[10px] text-slate-400 font-semibold uppercase block">120 pts</span>
                                            </div>
                                        </div>

                                        <div className="py-1">
                                            <Link
                                                href="/profile"
                                                onClick={() => setProfileMenuOpen(false)}
                                                className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white hover:bg-[#333] transition-colors"
                                            >
                                                <User className="h-4 w-4 text-slate-400" />
                                                View Profile
                                            </Link>
                                            <button
                                                className="w-full flex items-center gap-2 px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white hover:bg-[#333] transition-colors text-left cursor-not-allowed"
                                            >
                                                <Settings className="h-4 w-4 text-slate-400" />
                                                Account Settings
                                            </button>
                                        </div>

                                        <div className="border-t border-[#3e3e3e] mt-1 pt-1">
                                            <button
                                                onClick={() => {
                                                    setProfileMenuOpen(false);
                                                    logout();
                                                }}
                                                className="w-full flex items-center gap-2 px-4 py-2 text-xs font-bold text-red-400 hover:bg-[#333] transition-colors text-left cursor-pointer"
                                            >
                                                <LogOut className="h-4 w-4" />
                                                Sign Out
                                            </button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="flex items-center gap-2">
                                <button
                                    onClick={() => setAuthModalOpen(true)}
                                    className="px-3 py-1.5 border border-[#3e3e3e] hover:border-slate-400 rounded text-xs font-semibold text-slate-300 hover:text-white transition-colors cursor-pointer"
                                >
                                    Sign In
                                </button>
                                <button
                                    onClick={() => setAuthModalOpen(true)}
                                    className="px-3 py-1.5 bg-[#ffa116] hover:bg-[#ffb03a] text-[#1a1a1a] rounded text-xs font-extrabold transition-colors cursor-pointer"
                                >
                                    Register
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Mobile toggle */}
                    <div className="md:hidden flex items-center">
                        <button
                            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                            className="inline-flex items-center justify-center p-2 rounded text-slate-400 hover:text-white hover:bg-[#333]"
                        >
                            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
                        </button>
                    </div>
                </div>
            </div>

            {/* Mobile links */}
            {mobileMenuOpen && (
                <div className="md:hidden bg-[#282828] border-t border-[#3e3e3e] px-2 pt-2 pb-3 space-y-1">
                    {navLinks.map((link) => (
                        <Link
                            key={link.name}
                            href={link.href}
                            onClick={() => setMobileMenuOpen(false)}
                            className="block px-3 py-2 rounded text-sm font-semibold hover:bg-[#333] text-slate-300 hover:text-white"
                        >
                            {link.name}
                        </Link>
                    ))}
                    {user ? (
                        <button
                            onClick={() => {
                                setMobileMenuOpen(false);
                                logout();
                            }}
                            className="block w-full text-left px-3 py-2 rounded text-sm font-semibold hover:bg-[#333] text-red-400"
                        >
                            Sign Out
                        </button>
                    ) : (
                        <button
                            onClick={() => {
                                setMobileMenuOpen(false);
                                setAuthModalOpen(true);
                            }}
                            className="block w-full text-left px-3 py-2 rounded text-sm font-semibold hover:bg-[#333] text-indigo-400"
                        >
                            Sign In
                        </button>
                    )}
                </div>
            )}

            {/* Authentication modal integration */}
            <AuthModal isOpen={authModalOpen} onClose={() => setAuthModalOpen(false)} />
        </nav>
    );
};
