"use client";

import React from "react";
import { Navbar } from "@/components/Navbar";
import { Trophy, ShieldCheck, Flame, Compass } from "lucide-react";

export default function ContestsPage() {
    return (
        <div className="min-h-screen bg-[#1a1a1a] text-[#eff1f6] flex flex-col font-sans">
            <Navbar />
            
            <main className="flex-1 max-w-4xl w-full mx-auto px-4 py-12 text-center flex flex-col justify-center">
                <div className="bg-[#282828] border border-[#3e3e3e] p-8 rounded-lg">
                    <Trophy className="h-12 w-12 text-[#ffa116] mx-auto mb-4" />
                    <h2 className="text-xl font-bold text-white mb-2">Contest Platform</h2>
                    <p className="text-xs text-slate-400 max-w-md mx-auto leading-relaxed mb-6">
                        Compete against other developers in real-time. Standings are ranked by solve speed and compiler execution performance. Contests are currently in private preview.
                    </p>
                    
                    <div className="space-y-3 max-w-sm mx-auto text-left text-xs bg-[#1e1e1e] p-4 rounded border border-[#3e3e3e] text-slate-300">
                        <div className="flex items-center gap-2">
                            <ShieldCheck className="h-4 w-4 text-emerald-400" />
                            <span>Contest Rating Updates (Post-evaluation)</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <Flame className="h-4 w-4 text-orange-400" />
                            <span>Weekly & Biweekly sessions</span>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
