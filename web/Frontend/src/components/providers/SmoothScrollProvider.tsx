"use client";

import React, { useEffect } from "react";
import { ReactLenis } from "lenis/react";
import "lenis/dist/lenis.css";

export default function SmoothScrollProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  useEffect(() => {
    const addLenisPrevent = (el: HTMLElement) => {
      if (
        el.classList &&
        (el.classList.contains("overflow-y-auto") ||
          el.classList.contains("overflow-auto") ||
          el.tagName === "TEXTAREA")
      ) {
        el.setAttribute("data-lenis-prevent", "");
      }
      
      // Also query descendants
      el.querySelectorAll?.(".overflow-y-auto, .overflow-auto, textarea").forEach((child) => {
        child.setAttribute("data-lenis-prevent", "");
      });
    };

    // Initialize on existing elements
    addLenisPrevent(document.body);

    // Observe future elements (e.g. page changes, modals opening)
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node instanceof HTMLElement) {
            addLenisPrevent(node);
          }
        });
      });
    });

    observer.observe(document.body, { childList: true, subtree: true });

    return () => observer.disconnect();
  }, []);

  return (
    <ReactLenis root options={{ lerp: 0.1, duration: 1.2, syncTouch: true }}>
      {children}
    </ReactLenis>
  );
}
