'use client';

import { useEffect } from 'react';

interface ConfirmModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'success' | 'warning' | 'error' | 'info';
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmModal({
  isOpen,
  title,
  message,
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  type = 'info',
  onConfirm,
  onCancel,
}: ConfirmModalProps) {
  
  // Close on Escape key press
  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onCancel();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onCancel]);

  if (!isOpen) return null;

  const getTypeStyles = () => {
    switch (type) {
      case 'success':
        return {
          icon: 'check_circle',
          iconColor: 'text-success bg-success/10',
          btnBg: 'bg-success text-on-success glow-success',
        };
      case 'error':
        return {
          icon: 'error',
          iconColor: 'text-error bg-error/10',
          btnBg: 'bg-error text-on-error glow-error',
        };
      case 'warning':
        return {
          icon: 'warning',
          iconColor: 'text-warning bg-warning/10',
          btnBg: 'bg-warning text-on-warning glow-warning',
        };
      default:
        return {
          icon: 'info',
          iconColor: 'text-primary bg-primary/10',
          btnBg: 'bg-primary text-on-primary glow-button',
        };
    }
  };

  const styles = getTypeStyles();

  return (
    <div className="fixed inset-0 z-[150] flex items-center justify-center p-4 bg-background/80 backdrop-blur-md animate-in fade-in duration-200">
      {/* Backdrop click handler */}
      <div className="absolute inset-0" onClick={onCancel}></div>

      {/* Modal Container */}
      <div className="relative glass-card w-full max-w-md rounded-2xl shadow-2xl overflow-hidden z-50 animate-in fade-in zoom-in-95 duration-200 border border-border-glass">
        <div className="p-6 flex flex-col items-center text-center space-y-4">
          {/* Icon Badge */}
          <div className={`w-12 h-12 rounded-full flex items-center justify-center ${styles.iconColor}`}>
            <span className="material-symbols-outlined text-2xl font-bold">{styles.icon}</span>
          </div>

          {/* Heading */}
          <div className="space-y-1.5">
            <h3 className="text-base font-bold text-white tracking-wide">{title}</h3>
            <p className="text-xs text-on-surface-variant leading-relaxed max-w-sm">{message}</p>
          </div>

          {/* Actions */}
          <div className="flex w-full gap-3 pt-2">
            <button
              onClick={onCancel}
              className="flex-1 px-4 py-2.5 rounded-xl border border-border-glass text-xs font-semibold text-on-surface hover:bg-white/5 transition-all active:scale-95 cursor-pointer outline-none"
            >
              {cancelText}
            </button>
            <button
              onClick={onConfirm}
              className={`flex-1 px-4 py-2.5 rounded-xl text-xs font-bold transition-all active:scale-95 cursor-pointer outline-none ${styles.btnBg}`}
            >
              {confirmText}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
