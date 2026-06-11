'use client';

import React, { useState, useEffect, useRef } from 'react';
import Tesseract from 'tesseract.js';
import { ProductItem } from '@/types';
import { formatVND } from '@/lib/utils';

interface OcrScannerProps {
  mode: 'purchases' | 'sales';
  availableProducts: ProductItem[];
  onApply: (data: {
    invoiceId: string;
    date: string;
    items: Array<{
      productId: number;
      qty: number;
      price: number;
      dvt?: string;
      isGift?: boolean;
    }>;
  }) => void;
  onClose: () => void;
}

interface ParsedItem {
  id: string; // Unique temporary ID for list keys
  rawName: string;
  dvt: string;
  mappedProductId: number; // 0 if not mapped
  qty: number;
  price: number;
  total: number;
  isGift?: boolean;
}

// Binarize/Thresholding image to remove watermark and table gridlines
const preprocessImage = (imageFile: File, threshold: number): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (event) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('Cannot get canvas context'));
          return;
        }
        canvas.width = img.width;
        canvas.height = img.height;
        ctx.drawImage(img, 0, 0);

        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const data = imageData.data;

        // Convert to grayscale and apply thresholding
        for (let i = 0; i < data.length; i += 4) {
          const r = data[i];
          const g = data[i + 1];
          const b = data[i + 2];
          // Standard grayscale formula
          const gray = 0.299 * r + 0.587 * g + 0.114 * b;
          
          // Thresholding
          const value = gray < threshold ? 0 : 255;
          data[i] = value;     // R
          data[i + 1] = value; // G
          data[i + 2] = value; // B
          data[i + 3] = 255;   // A
        }

        ctx.putImageData(imageData, 0, 0);
        resolve(canvas.toDataURL('image/png'));
      };
      img.onerror = () => reject(new Error('Failed to load image'));
      img.src = event.target?.result as string;
    };
    reader.onerror = () => reject(new Error('Failed to read file'));
    reader.readAsDataURL(imageFile);
  });
};

export default function OcrScanner({ mode, availableProducts, onApply, onClose }: OcrScannerProps) {
  const [image, setImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [language, setLanguage] = useState<'vie' | 'eng' | 'vie+eng'>('vie');
  const [isScanning, setIsScanning] = useState(false);
  const [scanProgress, setScanProgress] = useState(0);
  const [scanStatus, setScanStatus] = useState('');
  const [rawText, setRawText] = useState('');
  
  // Parsed states
  const [invoiceId, setInvoiceId] = useState('');
  const [invoiceDate, setInvoiceDate] = useState('');
  const [parsedItems, setParsedItems] = useState<ParsedItem[]>([]);
  const [isDragOver, setIsDragOver] = useState(false);
  
  // Preprocessing states
  const [threshold, setThreshold] = useState<number>(200);
  const [usePreprocessing, setUsePreprocessing] = useState<boolean>(true);
  const [processedPreview, setProcessedPreview] = useState<string | null>(null);
  const [showProcessed, setShowProcessed] = useState<boolean>(true);
  
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Clean preview URL on unmount
  useEffect(() => {
    return () => {
      if (imagePreview) {
        URL.revokeObjectURL(imagePreview);
      }
    };
  }, [imagePreview]);

  // Trigger binarization when image, threshold, or preprocessing option changes
  useEffect(() => {
    if (!image) {
      setProcessedPreview(null);
      return;
    }
    if (!usePreprocessing) {
      setProcessedPreview(null);
      return;
    }

    let active = true;
    preprocessImage(image, threshold)
      .then((url) => {
        if (active) {
          setProcessedPreview(url);
        }
      })
      .catch((err) => {
        console.error('Failed to preprocess image:', err);
      });

    return () => {
      active = false;
    };
  }, [image, threshold, usePreprocessing]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setImage(file);
      if (imagePreview) URL.revokeObjectURL(imagePreview);
      setImagePreview(URL.createObjectURL(file));
      setRawText('');
      resetParsedData();
      setThreshold(200);
      setShowProcessed(true);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(true);
  };

  const handleDragLeave = () => {
    setIsDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (file.type.startsWith('image/')) {
        setImage(file);
        if (imagePreview) URL.revokeObjectURL(imagePreview);
        setImagePreview(URL.createObjectURL(file));
        setRawText('');
        resetParsedData();
        setThreshold(200);
        setShowProcessed(true);
      } else {
        alert('Vui lòng chọn một file ảnh (PNG, JPG, JPEG).');
      }
    }
  };

  const resetParsedData = () => {
    setInvoiceId('');
    setInvoiceDate('');
    setParsedItems([]);
  };

  // Fuzzy word matching logic
  const cleanWord = (w: string) => 
    w.toLowerCase()
     .normalize('NFD')
     .replace(/[\u0300-\u036f]/g, '') // Strip Vietnamese accents for matching
     .replace(/[^\w\d]/g, '');

  const getWords = (str: string) => 
    str.split(/\s+/)
       .map(cleanWord)
       .filter(w => w.length > 1);

  const getWordOverlapSimilarity = (str1: string, str2: string) => {
    const w1 = getWords(str1);
    const w2 = getWords(str2);
    if (w1.length === 0 || w2.length === 0) return 0;
    const intersection = w1.filter(w => w2.includes(w));
    return intersection.length / Math.max(w1.length, w2.length);
  };

  const findBestProductMatch = (rawName: string): number => {
    let bestProductId = 0;
    let maxSim = 0;

    for (const prod of availableProducts) {
      const sim = getWordOverlapSimilarity(rawName, prod.tenSanPham);
      // If exact substring match, boost score
      const isSub = rawName.toLowerCase().includes(prod.tenSanPham.toLowerCase()) || 
                    prod.tenSanPham.toLowerCase().includes(rawName.toLowerCase());
      const score = isSub ? Math.max(sim, 0.6) : sim;

      if (score > maxSim && score > 0.25) {
        maxSim = score;
        bestProductId = prod.id;
      }
    }
    return bestProductId;
  };

  const handleStartScan = async () => {
    const scanSource = (usePreprocessing && processedPreview) ? processedPreview : image;
    if (!scanSource) return;

    setIsScanning(true);
    setScanProgress(0);
    setScanStatus('Đang khởi tạo bộ máy OCR...');

    try {
      const result = await Tesseract.recognize(
        scanSource,
        language,
        {
          langPath: window.location.origin + '/tessdata',
          logger: (m) => {
            if (m.status === 'recognizing text') {
              setScanStatus('Đang nhận diện văn bản...');
              setScanProgress(Math.round(m.progress * 100));
            } else {
              setScanStatus(translateStatus(m.status));
            }
          }
        }
      );

      const text = result.data.text;
      setRawText(text);
      parseOcrText(text);
      setScanStatus('Quét thành công!');
    } catch (err: any) {
      console.error(err);
      alert('Đã xảy ra lỗi trong quá trình quét: ' + (err.message || err));
      setScanStatus('Quét thất bại.');
    } finally {
      setIsScanning(false);
    }
  };

  const translateStatus = (status: string) => {
    switch (status) {
      case 'loading tesseract core': return 'Đang tải nhân Tesseract...';
      case 'initializing api': return 'Đang thiết lập API...';
      case 'recognizing text': return 'Đang nhận diện văn bản...';
      default: return status;
    }
  };

  const parseOcrText = (text: string) => {
    const lines = text.split('\n');
    
    // Parse Invoice ID
    const idRegex = /(?:số hóa đơn|số hđ|mã hđ|hđ|invoice\s*no|invoice|so\s*hd)\s*[:.-]?\s*([^\s,;]+)/i;
    let foundId = '';
    
    // Parse Date
    const dateRegex = /(?:ngày|date)\s*[:.-]?\s*(\d{1,2}[\/\.-]\d{1,2}[\/\.-]\d{2,4})/i;
    let foundDate = '';

    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed) continue;

      if (!foundId) {
        const idMatch = trimmed.match(idRegex);
        if (idMatch) foundId = idMatch[1];
      }

      if (!foundDate) {
        const dateMatch = trimmed.match(dateRegex);
        if (dateMatch) {
          const parts = dateMatch[1].split(/[\/\.-]/);
          if (parts.length === 3) {
            let day = parts[0];
            let month = parts[1];
            let year = parts[2];
            if (day.length === 1) day = '0' + day;
            if (month.length === 1) month = '0' + month;
            if (year.length === 2) year = '20' + year;
            foundDate = `${year}-${month}-${day}`;
          }
        }
      }
    }

    setInvoiceId(foundId);
    setInvoiceDate(foundDate || new Date().toISOString().split('T')[0]);

    // Parse Items
    const COMMON_DVTS = [
      'gam', 'g', 'cái', 'cai', 'ly', 'kg', 'kg.', 'chai', 'lon', 'bao', 'hộp', 'hop', 
      'bịch', 'bich', 'gói', 'goi', 'tấm', 'tam', 'cuộn', 'cuon', 'hủ', 'hu', 
      'thùng', 'thung', 'két', 'ket', 'mét', 'met', 'lọ', 'lo', 'vỉ', 'vi', 
      'miếng', 'mieng', 'cục', 'cuc', 'bó', 'bo', 'lít', 'lit', 'ml', 'đĩa', 
      'dia', 'phần', 'phan', 'suất', 'suat', 'túi', 'tui', 'khay', 'set'
    ];

    const itemsList: ParsedItem[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;

      const lowerLine = line.toLowerCase();
      // Skip metadata headers or footers
      if (
        lowerLine.includes('địa chỉ') ||
        lowerLine.includes('điện thoại') ||
        lowerLine.includes('sđt') ||
        lowerLine.includes('số tk') ||
        lowerLine.includes('tài khoản') ||
        lowerLine.includes('ngân hàng') ||
        lowerLine.includes('chủ tài khoản') ||
        lowerLine.includes('hóa đơn') ||
        lowerLine.includes('ngày') ||
        lowerLine.includes('khách hàng') ||
        lowerLine.includes('tổng cộng') ||
        lowerLine.includes('cộng') ||
        lowerLine.includes('thanh toán') ||
        lowerLine.includes('còn lại') ||
        lowerLine.includes('người bán') ||
        lowerLine.includes('coffee shop') ||
        lowerLine.includes('tên hàng') ||
        lowerLine.includes('đơn giá') ||
        lowerLine.includes('thành tiền')
      ) {
        continue;
      }

      // Clean up vertical bars and trim
      let cleaned = line.replace(/^[\s|]*\d+[\s|]+/, ''); // Remove leading STT index
      cleaned = cleaned.replace(/^[\s|]+|[\s|]+$/g, '').trim();
      if (!cleaned) continue;

      // Merge thousands separator: 260.000 -> 260000
      const normalized = cleaned.replace(/(\d+)[.,](\d{3})\b/g, '$1$2');

      // Split normalized line into words
      const words = normalized.split(/\s+/).filter(w => w !== '|' && w.length > 0);
      if (words.length < 2) continue;

      // Find DVT in words
      let dvtIndex = -1;
      let foundDvt = '';
      for (let idx = words.length - 1; idx >= 0; idx--) {
        const wNormalized = words[idx].toLowerCase()
                                      .normalize('NFD')
                                      .replace(/[\u0300-\u036f]/g, '')
                                      .replace(/[^\w\d]/g, '')
                                      .trim();
        if (COMMON_DVTS.includes(wNormalized)) {
          dvtIndex = idx;
          foundDvt = words[idx];
          break;
        }
      }

      let qty = 1;
      let priceVal = 0;
      let totalVal = 0;
      let productNameWords: string[] = [];
      let isGift = false;
      let hasDvt = dvtIndex !== -1;

      if (hasDvt) {
        const postWords = words.slice(dvtIndex + 1);
        const prevWord = dvtIndex > 0 ? words[dvtIndex - 1] : '';
        const prevWordIsQty = prevWord && /^\d+$/.test(prevWord);

        if (postWords.length >= 3) {
          // Case B: [Product Name] [DVT] [Quantity] [Price] [Total]
          // e.g. "Máy xay cà phê Lingdong 021 Cái 1 3,500,000 3,500,000"
          // Here postWords is ["1", "3500000", "3500000"]
          qty = parseInt(postWords[0]) || 1;
          priceVal = parseInt(postWords[1].replace(/[^\d]/g, '')) || 0;
          totalVal = parseInt(postWords[2].replace(/[^\d]/g, '')) || 0;
          productNameWords = words.slice(0, dvtIndex);
        } else if (postWords.length === 2) {
          // Could be Case A or Case B (if total or qty is missing)
          // E.g. Case A: [Product Name] [Quantity] [DVT] [Price] [Total]
          // E.g. "Trà đào 2 ly 30000 60000" -> postWords is ["30000", "60000"]
          if (prevWordIsQty) {
            qty = parseInt(prevWord) || 1;
            priceVal = parseInt(postWords[0].replace(/[^\d]/g, '')) || 0;
            totalVal = parseInt(postWords[1].replace(/[^\d]/g, '')) || 0;
            productNameWords = words.slice(0, dvtIndex - 1);
          } else {
            // Case B without quantity (default qty = 1)
            // E.g. "Hộp đập bã Cái - -" -> postWords is ["-", "-"]
            qty = 1;
            priceVal = parseInt(postWords[0].replace(/[^\d]/g, '')) || 0;
            totalVal = parseInt(postWords[1].replace(/[^\d]/g, '')) || 0;
            productNameWords = words.slice(0, dvtIndex);
          }
        } else if (postWords.length === 1) {
          if (prevWordIsQty) {
            qty = parseInt(prevWord) || 1;
            priceVal = parseInt(postWords[0].replace(/[^\d]/g, '')) || 0;
            totalVal = priceVal * qty;
            productNameWords = words.slice(0, dvtIndex - 1);
          } else {
            qty = 1;
            priceVal = parseInt(postWords[0].replace(/[^\d]/g, '')) || 0;
            totalVal = priceVal;
            productNameWords = words.slice(0, dvtIndex);
          }
        } else {
          // postWords.length === 0
          if (prevWordIsQty) {
            qty = parseInt(prevWord) || 1;
            productNameWords = words.slice(0, dvtIndex - 1);
          } else {
            qty = 1;
            productNameWords = words.slice(0, dvtIndex);
          }
          priceVal = 0;
          totalVal = 0;
        }

        // If price and total are missing/invalid (< 1000) or contain '-' (representing empty price in gift lines), treat as gift item
        const hasDashInPrice = postWords.some(w => w.includes('-'));
        if ((priceVal < 1000 && totalVal < 1000) || hasDashInPrice) {
          isGift = true;
          priceVal = 0;
          totalVal = 0;
        }
      } else {
        // No DVT in line. Check if there are price & total numbers at the end.
        const lastWord = words[words.length - 1];
        const secondLastWord = words.length >= 2 ? words[words.length - 2] : '';
        const thirdLastWord = words.length >= 3 ? words[words.length - 3] : '';

        const parsedTotal = parseInt(lastWord.replace(/[^\d]/g, '')) || 0;
        const parsedPrice = secondLastWord ? (parseInt(secondLastWord.replace(/[^\d]/g, '')) || 0) : 0;

        if (parsedTotal >= 1000 && parsedPrice >= 1000) {
          priceVal = parsedPrice;
          totalVal = parsedTotal;
          // Check if third last word is quantity
          if (thirdLastWord && /^\d+$/.test(thirdLastWord)) {
            qty = parseInt(thirdLastWord) || 1;
            productNameWords = words.slice(0, words.length - 3);
          } else {
            qty = 1;
            productNameWords = words.slice(0, words.length - 2);
          }
        } else {
          // No DVT and no prices. Might be a gift line without DVT.
          // Let's see if we can find any word that is a small integer (1 to 99) as a candidate for quantity.
          let qtyIndex = -1;
          for (let idx = 0; idx < words.length; idx++) {
            if (/^\d+$/.test(words[idx])) {
              const val = parseInt(words[idx]);
              if (val > 0 && val < 100) {
                qtyIndex = idx;
                qty = val;
                break;
              }
            }
          }

          if (qtyIndex !== -1) {
            productNameWords = words.filter((_, idx) => idx !== qtyIndex);
          } else {
            qty = 1;
            productNameWords = words;
          }

          // Clean productNameWords from symbols
          productNameWords = productNameWords.filter(w => !/^[\W_]+$/.test(w));
          priceVal = 0;
          totalVal = 0;
          isGift = true;
        }
      }

      const productName = productNameWords.join(' ').replace(/^[\s|.-]+|[\s|.-]+$/g, '').trim();
      if (productName.length < 2) continue;

      // Find best matching product in DB
      let mappedId = findBestProductMatch(productName);
      
      // If we didn't match and there was no DVT in the line, we skip to avoid parsing random sentences.
      if (mappedId === 0 && !hasDvt) {
        continue;
      }

      // Determine final DVT
      let finalDvt = hasDvt ? foundDvt : '';
      if (mappedId > 0 && !finalDvt) {
        const matchedProd = availableProducts.find(p => p.id === mappedId);
        finalDvt = matchedProd?.donViTinh?.tenDonVi || 'cái';
      }

      const normalizedDvt = finalDvt.toLowerCase()
                                    .normalize('NFD')
                                    .replace(/[\u0300-\u036f]/g, '')
                                    .replace(/[^\w\d]/g, '')
                                    .trim();
      const isDvtValid = COMMON_DVTS.includes(normalizedDvt);

      if (!isDvtValid && mappedId === 0) {
        continue;
      }

      itemsList.push({
        id: `${i}_${Math.random()}`,
        rawName: productName,
        dvt: finalDvt,
        mappedProductId: mappedId,
        qty,
        price: priceVal,
        total: totalVal,
        isGift
      });
    }

    setParsedItems(itemsList);
  };

  const handleRawTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setRawText(e.target.value);
  };

  const handleReParse = () => {
    parseOcrText(rawText);
  };

  const handleRowChange = (id: string, field: keyof ParsedItem, value: any) => {
    setParsedItems(prev => prev.map(item => {
      if (item.id === id) {
        const updated = { ...item, [field]: value } as ParsedItem;
        
        // If product mapped is changed, auto-update the unit price
        if (field === 'mappedProductId') {
          const prodId = parseInt(value);
          const matchedProd = availableProducts.find(p => p.id === prodId);
          if (matchedProd) {
            updated.price = mode === 'purchases' ? matchedProd.giaNhapHienTai : matchedProd.giaBanHienTai;
          }
        }

        if (field === 'isGift') {
          if (value) {
            updated.price = 0;
            updated.total = 0;
          } else {
            const matchedProd = availableProducts.find(p => p.id === updated.mappedProductId);
            if (matchedProd) {
              updated.price = mode === 'purchases' ? matchedProd.giaNhapHienTai : matchedProd.giaBanHienTai;
            } else {
              updated.price = 0;
            }
            updated.total = updated.qty * updated.price;
          }
        }

        // Re-calculate line total
        if (field === 'qty' || field === 'price' || field === 'mappedProductId') {
          updated.total = updated.qty * updated.price;
        }

        return updated;
      }
      return item;
    }));
  };

  const handleDeleteRow = (id: string) => {
    setParsedItems(prev => prev.filter(item => item.id !== id));
  };

  const handleAddManualRow = () => {
    setParsedItems(prev => [
      ...prev,
      {
        id: `manual_${Date.now()}`,
        rawName: 'Sản phẩm mới thêm',
        dvt: 'cái',
        mappedProductId: 0,
        qty: 1,
        price: 0,
        total: 0,
        isGift: false
      }
    ]);
  };

  const handleApply = () => {
    // Map items, skip rows that are not linked to a product
    const mapped = parsedItems
      .filter(item => item.mappedProductId > 0)
      .map(item => ({
        productId: item.mappedProductId,
        qty: item.qty,
        price: item.price,
        dvt: item.dvt,
        isGift: !!item.isGift
      }));

    if (mapped.length === 0) {
      alert('Vui lòng liên kết ít nhất một sản phẩm để áp dụng vào biểu mẫu!');
      return;
    }

    onApply({
      invoiceId,
      date: invoiceDate,
      items: mapped
    });
  };

  return (
    <div className="flex-1 flex flex-col min-h-0 bg-[#0c1322] border-r border-white/5 overflow-hidden">
      {/* OCR Sub-Header */}
      <div className="p-4 border-b border-white/5 bg-[#0f192b] flex justify-between items-center">
        <div>
          <h3 className="text-xs font-bold text-white flex items-center gap-1.5">
            <span className="material-symbols-outlined text-primary text-sm">document_scanner</span>
            OCR Trích xuất Hóa đơn
          </h3>
          <p className="text-[9px] text-on-surface-variant">Tải ảnh hóa đơn lên để trích xuất vật phẩm tự động.</p>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="text-xs font-bold text-on-surface-variant hover:text-white px-2 py-1 bg-white/5 hover:bg-white/10 rounded-md transition-all cursor-pointer"
        >
          Đóng Scan
        </button>
      </div>

      <div className="flex-1 flex min-h-0 divide-x divide-white/5 overflow-hidden">
        {/* Left Column: Image Loader & Raw text */}
        <div className="w-1/2 flex flex-col p-4 space-y-4 overflow-y-auto min-h-0">
          {/* File input */}
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="image/*"
            className="hidden"
          />

          {/* Drag & Drop Area */}
          {!imagePreview ? (
            <div
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`flex-1 min-h-[160px] border-2 border-dashed rounded-xl flex flex-col items-center justify-center p-6 text-center cursor-pointer transition-all ${
                isDragOver ? 'border-primary bg-primary/5' : 'border-white/10 hover:border-primary/40 hover:bg-white/[0.01]'
              }`}
            >
              <span className="material-symbols-outlined text-3xl text-on-surface-variant mb-2 group-hover:scale-115 transition-transform">cloud_upload</span>
              <p className="text-xs font-semibold text-white">Kéo thả ảnh hóa đơn vào đây</p>
              <p className="text-[9px] text-on-surface-variant mt-1">hoặc click để chọn tệp (PNG, JPG, JPEG)</p>
            </div>
          ) : (
            <div className="relative group rounded-xl overflow-hidden border border-white/10 bg-black/20 flex flex-col items-center justify-center p-4 min-h-[220px] max-h-[280px]">
              {/* Preview Toggle Tabs */}
              {processedPreview && (
                <div className="absolute top-2 left-2 z-10 flex gap-1 bg-black/60 p-0.5 rounded-lg border border-white/5">
                  <button
                    type="button"
                    onClick={() => setShowProcessed(false)}
                    className={`px-2 py-0.5 rounded text-[8px] font-bold uppercase transition-all cursor-pointer ${
                      !showProcessed ? 'bg-primary text-on-primary font-extrabold' : 'text-on-surface-variant hover:text-white'
                    }`}
                  >
                    Ảnh gốc
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowProcessed(true)}
                    className={`px-2 py-0.5 rounded text-[8px] font-bold uppercase transition-all cursor-pointer ${
                      showProcessed ? 'bg-primary text-on-primary font-extrabold' : 'text-on-surface-variant hover:text-white'
                    }`}
                  >
                    Ảnh đen trắng
                  </button>
                </div>
              )}

              <img
                src={showProcessed && processedPreview ? processedPreview : imagePreview}
                alt="Invoice preview"
                className="max-h-[160px] object-contain rounded mt-4"
              />
              <button
                type="button"
                onClick={() => {
                  setImage(null);
                  setImagePreview(null);
                  setProcessedPreview(null);
                  resetParsedData();
                }}
                className="absolute top-2 right-2 bg-black/60 hover:bg-error text-white p-1 rounded-full hover:scale-105 transition-all flex items-center justify-center cursor-pointer"
                title="Xóa ảnh"
              >
                <span className="material-symbols-outlined text-sm">delete</span>
              </button>
            </div>
          )}

          {/* Preprocessing Controls */}
          {image && (
            <div className="bg-white/[0.02] border border-white/5 rounded-xl p-3.5 space-y-3">
              <div className="flex items-center justify-between">
                <label className="flex items-center gap-1.5 text-[10px] font-bold text-white uppercase tracking-wider cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={usePreprocessing}
                    onChange={(e) => setUsePreprocessing(e.target.checked)}
                    className="w-3.5 h-3.5 rounded bg-[#080d1a] border border-white/10 text-primary focus:ring-0 cursor-pointer"
                  />
                  Tiền xử lý ảnh (Lọc nền mờ & lưới Excel)
                </label>
                <span className="text-[8px] text-primary bg-primary/10 px-1.5 py-0.5 rounded font-mono font-bold uppercase tracking-wider">
                  Khuyên dùng
                </span>
              </div>
              
              {usePreprocessing && (
                <div className="space-y-2">
                  <div className="flex justify-between items-center text-[9px]">
                    <span className="text-on-surface-variant">Ngưỡng lọc nét chữ (Threshold)</span>
                    <span className="font-bold text-primary font-mono">{threshold} / 255</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-[9px] text-on-surface-variant">Tối hơn</span>
                    <input
                      type="range"
                      min="100"
                      max="245"
                      value={threshold}
                      onChange={(e) => setThreshold(parseInt(e.target.value))}
                      className="flex-1 h-1 bg-white/10 rounded-lg appearance-none cursor-pointer accent-primary"
                    />
                    <span className="text-[9px] text-on-surface-variant">Sáng hơn</span>
                  </div>
                  <p className="text-[8px] text-on-surface-variant italic leading-normal">
                    * Kéo slider để lọc sạch chữ "Page 1" và các đường lưới màu xanh, chỉ để lại chữ in hoặc viết tay nét đen.
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Scan controls */}
          <div className="flex gap-2">
            <div className="flex-1">
              <select
                value={language}
                onChange={(e) => setLanguage(e.target.value as any)}
                disabled={isScanning || !image}
                className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2.5 py-1.5 text-[11px] text-white focus:outline-none focus:ring-1 focus:ring-primary/50"
              >
                <option value="vie">Ngôn ngữ: Tiếng Việt</option>
                <option value="eng">Ngôn ngữ: Tiếng Anh</option>
                <option value="vie+eng">Ngôn ngữ: Việt + Anh</option>
              </select>
            </div>
            <button
              type="button"
              onClick={handleStartScan}
              disabled={isScanning || !image}
              className={`px-4 py-1.5 rounded-lg font-bold text-xs flex items-center gap-1 transition-all active:scale-95 cursor-pointer ${
                isScanning || !image
                  ? 'bg-white/5 text-on-surface-variant/40 cursor-not-allowed'
                  : 'bg-primary text-on-primary hover:shadow-[0_0_10px_rgba(73,252,223,0.3)]'
              }`}
            >
              <span className="material-symbols-outlined text-xs">play_arrow</span>
              <span>{isScanning ? 'Đang quét...' : 'Bắt đầu quét'}</span>
            </button>
          </div>

          {/* Progress bar */}
          {isScanning && (
            <div className="space-y-1">
              <div className="flex justify-between text-[9px] text-on-surface-variant">
                <span>{scanStatus}</span>
                <span className="font-bold text-primary">{scanProgress}%</span>
              </div>
              <div className="h-1.5 w-full bg-black/20 rounded-full overflow-hidden">
                <div
                  className="h-full bg-primary transition-all duration-300"
                  style={{ width: `${scanProgress}%` }}
                ></div>
              </div>
            </div>
          )}

          {/* Raw extracted text box */}
          {rawText && (
            <div className="flex-1 flex flex-col min-h-[140px] space-y-1.5">
              <div className="flex justify-between items-center">
                <label className="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Văn bản thô đã trích xuất:</label>
                <button
                  type="button"
                  onClick={handleReParse}
                  className="text-[9px] text-primary hover:underline flex items-center gap-0.5 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[10px]">sync</span> Phân tích lại
                </button>
              </div>
              <textarea
                value={rawText}
                onChange={handleRawTextChange}
                className="flex-1 bg-[#080d1a] border border-white/5 rounded-lg p-2 text-[10px] font-mono text-white resize-none focus:outline-none focus:ring-1 focus:ring-primary/50 leading-relaxed scrollbar"
              />
            </div>
          )}
        </div>

        {/* Right Column: Structured parser review */}
        <div className="w-1/2 flex flex-col p-4 space-y-4 overflow-y-auto min-h-0">
          <label className="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Kết quả phân tích:</label>

          {parsedItems.length === 0 && !isScanning ? (
            <div className="flex-1 border border-white/5 rounded-xl flex flex-col items-center justify-center text-center p-6 text-on-surface-variant bg-black/5">
              <span className="material-symbols-outlined text-2xl mb-1.5 opacity-50">analytics</span>
              <p className="text-xs">Chưa có kết quả.</p>
              <p className="text-[9px] mt-1 max-w-[200px]">Sau khi quét hóa đơn thành công, các mục sản phẩm được phân tích sẽ hiển thị ở đây.</p>
            </div>
          ) : (
            <div className="flex-1 flex flex-col space-y-4 min-h-0">
              {/* Metadata row */}
              <div className="grid grid-cols-2 gap-3 bg-white/[0.02] border border-white/5 p-3 rounded-xl">
                <div>
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Mã HĐ trích xuất:</label>
                  <input
                    type="text"
                    value={invoiceId}
                    onChange={(e) => setInvoiceId(e.target.value)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none focus:ring-1 focus:ring-primary/50"
                    placeholder="Chưa nhận diện được"
                  />
                </div>
                <div>
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Ngày hóa đơn:</label>
                  <input
                    type="date"
                    value={invoiceDate}
                    onChange={(e) => setInvoiceDate(e.target.value)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none focus:ring-1 focus:ring-primary/50"
                  />
                </div>
              </div>

              {/* Items Table container */}
              <div className="flex-1 flex flex-col border border-white/5 rounded-xl overflow-hidden min-h-[180px]">
                <div className="bg-[#0f192b] px-3 py-2 flex justify-between items-center border-b border-white/5">
                  <span className="text-[9px] uppercase font-bold text-white">Sản phẩm trích xuất ({parsedItems.length})</span>
                  <button
                    type="button"
                    onClick={handleAddManualRow}
                    className="text-[9px] text-primary hover:underline flex items-center gap-0.5 cursor-pointer"
                  >
                    <span className="material-symbols-outlined text-[10px]">add</span> Thêm dòng
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto divide-y divide-white/[0.04]">
                  {parsedItems.map((item) => {
                    const isMatched = item.mappedProductId > 0;
                    return (
                      <div key={item.id} className="p-3 space-y-2 hover:bg-white/[0.01] transition-all">
                        {/* Header: Raw Name & Mapping Status */}
                        <div className="flex items-center justify-between">
                          <span
                            className="text-[10px] font-bold text-white truncate max-w-[50%]"
                            title={item.rawName}
                          >
                            {item.rawName}
                          </span>
                          <div className="flex items-center gap-2">
                            <label className="flex items-center gap-1 text-[9px] text-on-surface-variant cursor-pointer select-none">
                              <input
                                type="checkbox"
                                checked={!!item.isGift}
                                onChange={(e) => handleRowChange(item.id, 'isGift', e.target.checked)}
                                className="w-3 h-3 rounded bg-[#080d1a] border border-white/10 text-primary focus:ring-0 cursor-pointer"
                              />
                              <span>Tặng</span>
                            </label>
                            <span
                              className={`px-1.5 py-0.5 rounded text-[8px] font-bold uppercase tracking-wider ${
                                isMatched ? 'bg-success/10 text-success' : 'bg-warning/10 text-warning'
                              }`}
                            >
                              {isMatched ? 'Đã Khớp' : 'Chưa Khớp'}
                            </span>
                          </div>
                        </div>

                        {/* Match Dropdown */}
                        <div>
                          <select
                            value={item.mappedProductId}
                            onChange={(e) => handleRowChange(item.id, 'mappedProductId', e.target.value)}
                            className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-[10px] text-white focus:outline-none focus:ring-1 focus:ring-primary/50"
                          >
                            <option value={0}>-- Chọn sản phẩm liên kết --</option>
                            {availableProducts.map(p => (
                              <option key={p.id} value={p.id}>
                                {p.tenSanPham} ({mode === 'purchases' ? formatVND(p.giaNhapHienTai) : formatVND(p.giaBanHienTai)})
                              </option>
                            ))}
                          </select>
                        </div>

                        {/* Qty, DVT, Price Row */}
                        <div className="grid grid-cols-5 gap-2 items-center">
                          <div>
                            <label className="block text-[8px] text-on-surface-variant">S.Lượng</label>
                            <input
                              type="number"
                              min={1}
                              value={item.qty}
                              onChange={(e) => handleRowChange(item.id, 'qty', parseInt(e.target.value) || 1)}
                              className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-1.5 py-0.5 text-[10px] text-white text-center"
                            />
                          </div>
                          <div>
                            <label className="block text-[8px] text-on-surface-variant">ĐVT</label>
                            <input
                              type="text"
                              value={item.dvt || ''}
                              onChange={(e) => handleRowChange(item.id, 'dvt', e.target.value)}
                              className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-1.5 py-0.5 text-[10px] text-white text-center"
                            />
                          </div>
                          <div className="col-span-2">
                            <label className="block text-[8px] text-on-surface-variant">Đơn giá</label>
                            <input
                              type="number"
                              min={0}
                              value={item.price}
                              onChange={(e) => handleRowChange(item.id, 'price', parseInt(e.target.value) || 0)}
                              className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-1.5 py-0.5 text-[10px] text-white text-right"
                            />
                          </div>
                          <div className="flex flex-col justify-end items-end h-full">
                            <button
                              type="button"
                              onClick={() => handleDeleteRow(item.id)}
                              className="text-error hover:text-white p-1 hover:bg-error/10 rounded transition-colors cursor-pointer flex items-center justify-center self-end"
                            >
                              <span className="material-symbols-outlined text-[14px]">delete</span>
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Action Button */}
              <button
                type="button"
                onClick={handleApply}
                className="w-full py-2 bg-primary text-on-primary font-bold text-xs rounded-xl shadow-lg hover:shadow-[0_0_15px_rgba(73,252,223,0.4)] transition-all active:scale-98 cursor-pointer flex items-center justify-center gap-1.5"
              >
                <span className="material-symbols-outlined text-sm">done_all</span>
                <span>Áp dụng vào biểu mẫu</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
