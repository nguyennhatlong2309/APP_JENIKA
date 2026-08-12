'use client';

import React, { useState } from 'react';
import { useAuth } from '@/components/providers/AuthContext';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!username.trim() || !password.trim()) {
      setError('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.');
      return;
    }

    try {
      setSubmitting(true);
      await login(username.trim(), password.trim());
    } catch (err) {
      console.error(err);
      setError((err as Error).message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      className="min-h-screen w-full flex items-center justify-center bg-cover bg-center px-4 relative overflow-hidden"
      style={{ backgroundImage: "url('/jenka.jpg')" }}
    >
      {/* Background Overlay */}
      <div className="absolute inset-0 bg-white/10 z-0" />

      {/* Decorative Blur Orbs */}
      <div className="absolute -top-[10%] -left-[10%] w-[50%] h-[50%] bg-primary/5 rounded-full blur-[120px] pointer-events-none z-0" />
      <div className="absolute -bottom-[10%] -right-[10%] w-[50%] h-[50%] bg-secondary/5 rounded-full blur-[120px] pointer-events-none z-0" />

      <div className="w-full max-w-md p-8 rounded-2xl border border-slate-200/10 bg-white/0 backdrop-blur-lg shadow-2xl relative z-10 space-y-8 animate-in fade-in zoom-in-95 duration-300">

        {/* Title / Logo */}
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            JenkaM
          </h1>
          <p className="text-[10px] uppercase tracking-widest text-slate-400 font-bold">
            Management System
          </p>
        </div>

        {/* Error Toast */}
        {error && (
          <div className="p-4 text-xs text-error bg-error/10 border border-error/20 rounded-xl flex items-center gap-3 animate-in slide-in-from-top-3 duration-250 overflow-hidden">
            <span className="material-symbols-outlined text-base flex-shrink-0">warning</span>
            <span className="font-semibold truncate">{error}</span>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <label className="text-[10px] font-bold uppercase tracking-widest text-slate-500">
              Tên đăng nhập
            </label>
            <div className="relative">
              <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-lg">
                person
              </span>
              <input
                required
                disabled={submitting}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                name="username"
                autoComplete="username"
                className="w-full bg-white border border-slate-300 rounded-xl py-3.5 pl-12 pr-4 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                placeholder="Nhập tên tài khoản..."
                type="text"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-[10px] font-bold uppercase tracking-widest text-slate-500">
              Mật khẩu
            </label>
            <div className="relative">
              <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-lg">
                lock
              </span>
              <input
                required
                disabled={submitting}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                name="password"
                autoComplete="current-password"
                className="w-full bg-white border border-slate-300 rounded-xl py-3.5 pl-12 pr-12 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all outline-none"
                placeholder="Nhập mật khẩu..."
                type={showPassword ? 'text' : 'password'}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors flex items-center justify-center"
              >
                <span className="material-symbols-outlined text-lg select-none">
                  {showPassword ? 'visibility_off' : 'visibility'}
                </span>
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-4 bg-primary hover:bg-primary/95 text-white font-bold rounded-xl shadow-lg shadow-primary/10 hover:scale-[1.01] active:scale-95 transition-all cursor-pointer text-xs uppercase tracking-wider disabled:opacity-50 disabled:pointer-events-none"
          >
            {submitting ? 'Đang xác thực...' : 'Đăng nhập hệ thống'}
          </button>
        </form>

        {/* Demo Info Bar */}
        <div className="p-4 bg-slate-50 border border-slate-100 rounded-xl text-center space-y-1">
          <p className="text-[10px] font-bold text-primary uppercase tracking-widest">Tài khoản trải nghiệm</p>
          <div className="text-xs text-slate-500 space-y-0.5">
            <p>Username: <code className="text-slate-700 font-mono bg-slate-200/60 px-1.5 py-0.5 rounded">admin</code></p>
            <p>Password: <code className="text-slate-700 font-mono bg-slate-200/60 px-1.5 py-0.5 rounded">JenkaM@2026</code></p>
          </div>
        </div>
      </div>
    </div>
  );
}
