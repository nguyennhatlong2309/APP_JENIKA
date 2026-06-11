import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Agentation } from "agentation";
import Sidebar from "@/components/features/Sidebar";

const inter = Inter({
  variable: "--font-sans",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "JENIKA - BrewMaster Dashboard",
  description: "Ultra-premium Cafe dashboard.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${inter.variable} h-full antialiased`}
    >
      <head>
        <link
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
          rel="stylesheet"
        />
      </head>
      <body className="min-h-full flex text-on-surface bg-[#0A0E17] font-sans selection:bg-primary/30 antialiased overflow-x-hidden relative">
        {/* Dynamic Glow Background Accents */}
        <div className="fixed inset-0 overflow-hidden pointer-events-none z-0">
          <div className="absolute -top-[10%] -left-[10%] w-[45%] h-[45%] bg-primary/5 rounded-full blur-[130px] animate-pulse" />
          <div className="absolute top-[40%] -right-[5%] w-[40%] h-[55%] bg-secondary/5 rounded-full blur-[110px]" />
          <div className="absolute bottom-[-10%] left-[20%] w-[35%] h-[45%] bg-tertiary/5 rounded-full blur-[120px]" />
        </div>

        {/* Sidebar fixed left */}
        <Sidebar />

        {/* Main section wrapper */}
        <div className="ml-[210px] flex-1 flex flex-col min-h-screen relative z-10">
          <main className="flex-1">
            {children}
          </main>
        </div>

        {process.env.NODE_ENV === "development" && <Agentation />}
      </body>
    </html>
  );
}

