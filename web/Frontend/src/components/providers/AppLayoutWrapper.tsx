'use client';

import { usePathname } from 'next/navigation';
import Sidebar from '@/components/features/Sidebar';
import { AuthProvider } from './AuthContext';

export default function AppLayoutWrapper({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isLoginPage = pathname === '/login';

  return (
    <AuthProvider>
      {isLoginPage ? (
        <main className="flex-1 min-h-screen w-full relative z-10">
          {children}
        </main>
      ) : (
        <>
          <Sidebar />
          <div className="ml-[210px] flex-1 flex flex-col min-h-screen relative">
            <main className="flex-1">
              {children}
            </main>
          </div>
        </>
      )}
    </AuthProvider>
  );
}
