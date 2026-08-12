import React, { useRef, useEffect, useState } from 'react';

interface NumericInputProps {
  value: number;
  onChange: (val: number) => void;
  className?: string;
  placeholder?: string;
  required?: boolean;
}

export default function NumericInput({
  value,
  onChange,
  className,
  placeholder,
  required
}: NumericInputProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [localValue, setLocalValue] = useState('');

  const formatNumberWithDots = (num: number | string) => {
    if (num === undefined || num === null) return '';
    const clean = num.toString().replace(/\D/g, '');
    if (!clean) return '0';
    return clean.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  };

  useEffect(() => {
    setLocalValue(formatNumberWithDots(value));
  }, [value]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const originalValue = e.target.value;
    const clean = originalValue.replace(/\D/g, '');
    const num = parseInt(clean) || 0;

    const oldCaret = e.target.selectionStart || 0;
    const digitsBeforeCaret = originalValue.slice(0, oldCaret).replace(/\D/g, '').length;

    onChange(num);

    const formatted = formatNumberWithDots(num);
    let newCaret = 0;
    let digitCount = 0;

    for (let i = 0; i < formatted.length; i++) {
      if (digitCount === digitsBeforeCaret) {
        break;
      }
      if (/\d/.test(formatted[i])) {
        digitCount++;
      }
      newCaret = i + 1;
    }

    requestAnimationFrame(() => {
      if (inputRef.current) {
        inputRef.current.setSelectionRange(newCaret, newCaret);
      }
    });
  };

  return (
    <input
      ref={inputRef}
      type="text"
      required={required}
      value={localValue}
      onChange={handleChange}
      className={className}
      placeholder={placeholder}
    />
  );
}
