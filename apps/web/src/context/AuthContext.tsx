"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";

export interface User {
    id: number;
    username: string;
    email: string;
    roles: string[];
}

interface AuthContextType {
    user: User | null;
    token: string | null;
    loading: boolean;
    error: string | null;
    login: (username: string, password: string) => Promise<boolean>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    useEffect(() => {
        // Load session on startup
        const storedUser = localStorage.getItem("codejudge_user");
        const storedToken = getCookie("codejudge_token");

        if (storedUser && storedToken) {
            setUser(JSON.parse(storedUser));
            setToken(storedToken);
        }
        setLoading(false);
    }, []);

    const login = async (username: string, password: string): Promise<boolean> => {
        setLoading(true);
        setError(null);
        try {
            const apiHost = process.env.NEXT_PUBLIC_API_HOST || "http://localhost:8080";
            const response = await fetch(`${apiHost}/api/v1/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ usernameOrEmail: username, password }),
            });

            const data = await response.json();

            if (!response.ok || !data.success) {
                setError(data.message || "Invalid credentials");
                setLoading(false);
                return false;
            }

            const tokenVal = data.data.accessToken;
            const userData: User = {
                id: data.data.id,
                username: data.data.username,
                email: data.data.email,
                roles: data.data.roles || [],
            };

            setToken(tokenVal);
            setUser(userData);

            // Save session
            localStorage.setItem("codejudge_user", JSON.stringify(userData));
            setCookie("codejudge_token", tokenVal, 1); // 1 day

            setLoading(false);

            // Redirect based on role
            const isAdmin = userData.roles.some(role => 
                role === "ROLE_ADMIN" || role === "ROLE_SUPER_ADMIN" || role === "ROLE_EXAM_SETTER"
            );

            if (isAdmin) {
                router.push("/admin/dashboard");
            } else {
                router.push("/candidate/dashboard");
            }

            return true;
        } catch (err: any) {
            logError(err);
            setError("Server connection failed");
            setLoading(false);
            return false;
        }
    };

    const logout = () => {
        setToken(null);
        setUser(null);
        localStorage.removeItem("codejudge_user");
        deleteCookie("codejudge_token");
        router.push("/auth/login");
    };

    // Helper functions for cookie management
    const getCookie = (name: string): string | null => {
        if (typeof document === "undefined") return null;
        const nameEQ = name + "=";
        const ca = document.cookie.split(";");
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === " ") c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    };

    const setCookie = (name: string, value: string, days: number) => {
        if (typeof document === "undefined") return;
        let expires = "";
        if (days) {
            const date = new Date();
            date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + (value || "") + expires + "; path=/; SameSite=Strict";
    };

    const deleteCookie = (name: string) => {
        if (typeof document === "undefined") return;
        document.cookie = name + "=; Max-Age=-99999999; path=/;";
    };

    const logError = (err: any) => {
        console.error("Auth error:", err);
    };

    return (
        <AuthContext.Provider value={{ user, token, loading, error, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
};
