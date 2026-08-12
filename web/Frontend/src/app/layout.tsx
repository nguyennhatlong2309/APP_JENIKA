import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Agentation } from "agentation";
import SmoothScrollProvider from "@/components/providers/SmoothScrollProvider";
import AppLayoutWrapper from "@/components/providers/AppLayoutWrapper";

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
      suppressHydrationWarning
    >
      <head>
        <link
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
          rel="stylesheet"
        />
        <script
          dangerouslySetInnerHTML={{
            __html: `
              (function() {
                var theme = localStorage.getItem('theme') || 'Giao diện sáng';
                if (theme === 'Giao diện sáng') {
                  document.documentElement.classList.add('light');
                } else {
                  document.documentElement.classList.remove('light');
                }
              })()
            `
          }}
        />
      </head>
      <body className="min-h-full flex text-on-surface bg-[#0A0E17] font-sans selection:bg-primary/30 antialiased overflow-x-hidden relative">
        <SmoothScrollProvider>
          {/* Dynamic Glow Background Accents */}
          <div className="fixed inset-0 overflow-hidden pointer-events-none z-0">
            <div className="absolute -top-[10%] -left-[10%] w-[45%] h-[45%] bg-primary/5 rounded-full blur-[130px] animate-pulse" />
            <div className="absolute top-[40%] -right-[5%] w-[40%] h-[55%] bg-secondary/5 rounded-full blur-[110px]" />
            <div className="absolute bottom-[-10%] left-[20%] w-[35%] h-[45%] bg-tertiary/5 rounded-full blur-[120px]" />
          </div>

          <AppLayoutWrapper>{children}</AppLayoutWrapper>
        </SmoothScrollProvider>

        {process.env.NODE_ENV === "development" && <Agentation />}
      </body>
    </html>
  );
}

