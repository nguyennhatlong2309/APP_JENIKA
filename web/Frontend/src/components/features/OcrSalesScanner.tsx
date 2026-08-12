'use client';

import React, { useState } from 'react';
import { ProductItem } from '@/types';
import { formatVND } from '@/lib/utils';
import { useOcrEngine } from './hooks/useOcrEngine';
import { uploadService } from '@/services/uploadService';
import {
  findBestProductMatch,
  parseRobustDate,
  COMMON_DVTS
} from './utils/ocrUtils';

interface OcrSalesScannerProps {
  availableProducts: ProductItem[];
  onApply: (data: {
    invoiceId: string;
    date: string;
    customerName?: string;
    customerPhone?: string;
    customerAddress?: string;
    paidAmount?: number;
    anhHoaDonUrl?: string;
    items: Array<{
      productId: number;
      qty: number;
      price: number;
      dvt?: string;
      isGift?: boolean;
    }>;
  }) => void;
  onClose: () => void;
  initialData?: {
    invoiceId: string;
    date: string;
    customerName?: string;
    customerPhone?: string;
    customerAddress?: string;
    paidAmount?: number;
    anhHoaDonUrl?: string;
    items: Array<{
      id?: string;
      rawName: string;
      dvt?: string;
      productId?: number;
      mappedProductId?: number;
      qty: number;
      price: number;
      total: number;
      isGift?: boolean;
    }>;
  };
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

export default function OcrSalesScanner({ availableProducts, onApply, onClose, initialData }: OcrSalesScannerProps) {
  const ocr = useOcrEngine();

  // S3 upload states
  const [s3Url, setS3Url] = useState(initialData?.anhHoaDonUrl || '');
  const [isUploading, setIsUploading] = useState(false);

  React.useEffect(() => {
    if (!ocr.image) {
      setS3Url(initialData?.anhHoaDonUrl || '');
      return;
    }
    const uploadImage = async () => {
      setIsUploading(true);
      try {
        const res = await uploadService.uploadImage(ocr.image!);
        if (res.success && res.url) {
          setS3Url(res.url);
        }
      } catch (err) {
        console.error('Failed to upload image to S3:', err);
      } finally {
        setIsUploading(false);
      }
    };
    uploadImage();
  }, [ocr.image, initialData?.anhHoaDonUrl]);

  // Parsed states
  const [invoiceId, setInvoiceId] = useState(initialData?.invoiceId || '');
  const [invoiceDate, setInvoiceDate] = useState(initialData?.date || '');
  const [customerName, setCustomerName] = useState(initialData?.customerName || '');
  const [customerPhone, setCustomerPhone] = useState(initialData?.customerPhone || '');
  const [customerAddress, setCustomerAddress] = useState(initialData?.customerAddress || '');
  const [paidAmount, setPaidAmount] = useState<number>(initialData?.paidAmount || 0);
  const [parsedItems, setParsedItems] = useState<ParsedItem[]>(() => {
    if (initialData?.items) {
      return initialData.items.map((it, idx) => {
        const mappedId = it.mappedProductId !== undefined ? it.mappedProductId : (it.productId || 0);
        const prod = availableProducts.find(p => p.id === mappedId);
        return {
          id: it.id || `${idx}_${Math.random()}`,
          rawName: it.rawName,
          dvt: it.dvt || prod?.donViTinh?.tenDonVi || 'cái',
          mappedProductId: mappedId,
          qty: it.qty || 1,
          price: it.price || 0,
          total: it.total || 0,
          isGift: !!it.isGift
        };
      });
    }
    return [];
  });
  const [rowSearchTerms, setRowSearchTerms] = useState<Record<string, string>>({});
  const [activeDropdownId, setActiveDropdownId] = useState<string | null>(null);

  // Sync state if initialData changes
  React.useEffect(() => {
    if (initialData) {
      setInvoiceId(initialData.invoiceId || '');
      setInvoiceDate(initialData.date || '');
      setCustomerName(initialData.customerName || '');
      setCustomerPhone(initialData.customerPhone || '');
      setCustomerAddress(initialData.customerAddress || '');
      setPaidAmount(initialData.paidAmount || 0);
      setS3Url(initialData.anhHoaDonUrl || '');
      if (initialData.items) {
        setParsedItems(initialData.items.map((it, idx) => {
          const mappedId = it.mappedProductId !== undefined ? it.mappedProductId : (it.productId || 0);
          const prod = availableProducts.find(p => p.id === mappedId);
          return {
            id: it.id || `${idx}_${Math.random()}`,
            rawName: it.rawName,
            dvt: it.dvt || prod?.donViTinh?.tenDonVi || 'cái',
            mappedProductId: mappedId,
            qty: it.qty || 1,
            price: it.price || 0,
            total: it.total || 0,
            isGift: !!it.isGift
          };
        }));
      }
    }
  }, [initialData, availableProducts]);

  const resetParsedData = () => {
    setInvoiceId(initialData?.invoiceId || '');
    setInvoiceDate(initialData?.date || '');
    setCustomerName(initialData?.customerName || '');
    setCustomerPhone(initialData?.customerPhone || '');
    setCustomerAddress(initialData?.customerAddress || '');
    setPaidAmount(initialData?.paidAmount || 0);
    if (initialData?.items) {
      setParsedItems(initialData.items.map((it, idx) => {
        const mappedId = it.mappedProductId !== undefined ? it.mappedProductId : (it.productId || 0);
        const prod = availableProducts.find(p => p.id === mappedId);
        return {
          id: it.id || `${idx}_${Math.random()}`,
          rawName: it.rawName,
          dvt: it.dvt || prod?.donViTinh?.tenDonVi || 'cái',
          mappedProductId: mappedId,
          qty: it.qty || 1,
          price: it.price || 0,
          total: it.total || 0,
          isGift: !!it.isGift
        };
      }));
    } else {
      setParsedItems([]);
    }
    setRowSearchTerms({});
    setActiveDropdownId(null);
    setS3Url(initialData?.anhHoaDonUrl || '');
  };

  const parseOcrText = (text: string) => {
    const lines = text.split('\n');
    
    // Parse Invoice ID
    const idRegex = /(?:số hóa đơn|số hđ|mã hđ|hđ|invoice\s*no|invoice|so\s*hd)\s*[:.-]?\s*([^\s,;]+)/i;
    let foundId = '';
    
    // Parse Date
    const dateRegex = /(?:ngày|date)\s*[:.-]?\s*([\d\/\.-]+)/i;
    let foundDate = '';

    // Parse Customer details
    const customerNameRegex = /\b(?:khách\s+[hàb]àng|khach\s+[hab]ang|customer|tên\s+khách|ten\s+khach|người\s+mua|nguoi\s+mua|\bkh(?=\s*[:.-]|\s+))\s*[:.-]?\s*([^\n\r]+)/i;
    let extractedCustomerName = '';

    const phoneRegex = /(?:sđt|sdt|đt|dt|điện thoại|dien thoai|phone|tel)\s*[:.-]?\s*([\d\s.-]{7,15})/i;
    let extractedCustomerPhone = '';

    const addressRegex = /(?:địa chỉ|dia chi|đạa chỉ|daa chi|da chi|address)\s*[:.-]?\s*([^\n\r]+)/i;
    let extractedCustomerAddress = '';

    const paidRegex = /(?:khách hàng thanh toán|khách thanh toán|đã trả|thanh toán|khach hang thanh toan|khach thanh toan|da tra|paid)\s*[:.-]?\s*([\d.,\s]+)/i;
    let extractedPaidAmount = 0;

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
          foundDate = parseRobustDate(dateMatch[1]);
        }
      }

      if (!extractedCustomerName) {
        const nameMatch = trimmed.match(customerNameRegex);
        if (nameMatch) {
          const val = nameMatch[1].trim();
          const cleanVal = val.toLowerCase()
                              .normalize('NFD')
                              .replace(/[\u0300-\u036f]/g, '')
                              .replace(/[đĐ]/g, 'd');
          if (!cleanVal.includes('thanh toan') && !cleanVal.includes('tra') && !cleanVal.includes('chuyen khoan') && !cleanVal.includes('ck')) {
            extractedCustomerName = val;
          }
        }
      }

      const phoneMatch = trimmed.match(phoneRegex);
      if (phoneMatch) {
        extractedCustomerPhone = phoneMatch[1].replace(/[^\d]/g, '');
      }

      const addressMatch = trimmed.match(addressRegex);
      if (addressMatch) {
        extractedCustomerAddress = addressMatch[1].trim();
      }

      const paidMatch = trimmed.match(paidRegex);
      if (paidMatch) {
        const clean = paidMatch[1].replace(/[^\d]/g, '');
        extractedPaidAmount = parseInt(clean) || 0;
      }
    }

    setInvoiceId(foundId);
    setInvoiceDate(foundDate || new Date().toISOString().split('T')[0]);
    setCustomerName(extractedCustomerName);
    setCustomerPhone(extractedCustomerPhone);
    setCustomerAddress(extractedCustomerAddress);
    setPaidAmount(extractedPaidAmount);

    // Parse Items
    const itemsList: ParsedItem[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;

      const lowerLine = line.toLowerCase();

      // Stop parsing items if we hit the footer / policy section
      const cleanLine = lowerLine
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[đĐ]/g, 'd')
        .trim();

      if (
        cleanLine.includes('luu y') ||
        cleanLine.includes('huu y') ||
        cleanLine.includes('quy dinh') ||
        cleanLine.includes('bao hanh')
      ) {
        break;
      }

      // Skip metadata headers or footers
      if (
        cleanLine.includes('dia chi') ||
        cleanLine.includes('daa chi') ||
        cleanLine.includes('da chi') ||
        cleanLine.includes('dien thoai') ||
        cleanLine.includes('sdt') ||
        cleanLine.includes('so tk') ||
        cleanLine.includes('tai khoan') ||
        cleanLine.includes('ngan hang') ||
        cleanLine.includes('chu tai khoan') ||
        cleanLine.includes('hoa don') ||
        cleanLine.includes('ngay') ||
        cleanLine.includes('khach hang') ||
        cleanLine.includes('tong cong') ||
        cleanLine.includes('cong') ||
        cleanLine.includes('thanh toan') ||
        cleanLine.includes('con lai') ||
        cleanLine.includes('nguoi ban') ||
        cleanLine.includes('coffee shop') ||
        cleanLine.includes('ten hang') ||
        cleanLine.includes('don gia') ||
        cleanLine.includes('thanh tien')
      ) {
        continue;
      }

      // Clean up leading STT index and any trailing noise symbols
      let cleaned = line.replace(/^[\s|]*\d+[\s|=\-./*)]*/, '');
      cleaned = cleaned.replace(/^[\s|]+|[\s|]+$/g, '').trim();
      if (!cleaned) continue;

      // Merge thousands separator: 260.000 -> 260000
      const normalized = cleaned.replace(/(\d+)[.,](\d{3})\b/g, '$1$2');

      // Split normalized line into words
      const rawWords = normalized.split(/\s+/).filter(w => w !== '|' && w.length > 0);
      if (rawWords.length < 2) continue;

      const words: string[] = [];
      for (let idx = 0; idx < rawWords.length; idx++) {
        const w = rawWords[idx];
        if (/^[^\w\d]+$/.test(w)) {
          const hasAlphanumericAfter = rawWords.slice(idx + 1).some(nextW => /[\w\d]/.test(nextW));
          if (hasAlphanumericAfter) {
            continue;
          }
        }
        words.push(w);
      }
      if (words.length < 2) continue;

      // Find DVT in words
      let dvtIndex = -1;
      let foundDvt = '';
      for (let idx = words.length - 1; idx > 0; idx--) {
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
          qty = parseInt(postWords[0]) || 1;
          priceVal = parseInt(postWords[1].replace(/[^\d]/g, '')) || 0;
          totalVal = parseInt(postWords[2].replace(/[^\d]/g, '')) || 0;
          productNameWords = words.slice(0, dvtIndex);
        } else if (postWords.length === 2) {
          if (prevWordIsQty) {
            qty = parseInt(prevWord) || 1;
            priceVal = parseInt(postWords[0].replace(/[^\d]/g, '')) || 0;
            totalVal = parseInt(postWords[1].replace(/[^\d]/g, '')) || 0;
            productNameWords = words.slice(0, dvtIndex - 1);
          } else {
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

        const hasDashInPrice = postWords.some(w => w.includes('-'));
        if ((priceVal < 1000 && totalVal < 1000) || hasDashInPrice) {
          isGift = true;
          priceVal = 0;
          totalVal = 0;
        }
      } else {
        const lastWord = words[words.length - 1];
        const secondLastWord = words.length >= 2 ? words[words.length - 2] : '';
        const thirdLastWord = words.length >= 3 ? words[words.length - 3] : '';

        const parsedTotal = parseInt(lastWord.replace(/[^\d]/g, '')) || 0;
        const parsedPrice = secondLastWord ? (parseInt(secondLastWord.replace(/[^\d]/g, '')) || 0) : 0;

        if (parsedTotal >= 1000 && parsedPrice >= 1000) {
          priceVal = parsedPrice;
          totalVal = parsedTotal;
          if (thirdLastWord && /^\d+$/.test(thirdLastWord)) {
            qty = parseInt(thirdLastWord) || 1;
            productNameWords = words.slice(0, words.length - 3);
          } else {
            qty = 1;
            productNameWords = words.slice(0, words.length - 2);
          }
        } else {
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

          productNameWords = productNameWords.filter(w => !/^[\W_]+$/.test(w));
          priceVal = 0;
          totalVal = 0;
          isGift = true;
        }
      }

      const productName = productNameWords.join(' ').replace(/^[\s|.-]+|[\s|.-]+$/g, '').trim();
      if (productName.length < 2) continue;

      let mappedId = findBestProductMatch(productName, availableProducts);
      if (mappedId === 0 && !hasDvt) {
        continue;
      }

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
    ocr.setRawText(e.target.value);
  };

  const handleReParse = () => {
    parseOcrText(ocr.rawText);
  };

  const handleRowChange = (id: string, field: keyof ParsedItem, value: any) => {
    setParsedItems(prev => prev.map(item => {
      if (item.id === id) {
        const updated = { ...item, [field]: value } as ParsedItem;
        
        // If product mapped is changed, auto-update the unit price (Sales uses giaBanHienTai)
        if (field === 'mappedProductId') {
          const prodId = parseInt(value);
          const matchedProd = availableProducts.find(p => p.id === prodId);
          if (matchedProd) {
            updated.price = matchedProd.giaBanHienTai;
          }
        }

        if (field === 'isGift') {
          if (value) {
            updated.price = 0;
            updated.total = 0;
          } else {
            const matchedProd = availableProducts.find(p => p.id === updated.mappedProductId);
            if (matchedProd) {
              updated.price = matchedProd.giaBanHienTai;
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
      customerName,
      customerPhone,
      customerAddress,
      paidAmount,
      anhHoaDonUrl: s3Url,
      items: mapped
    });
  };

  const handleStartScan = () => {
    ocr.handleStartScan((extractedText) => {
      parseOcrText(extractedText);
    });
  };

  const handleClearAll = () => {
    ocr.resetEngine();
    resetParsedData();
  };

  return (
    <div className="flex-1 flex flex-col min-h-0 bg-[#0c1322] border-r border-white/5 overflow-hidden">
      {/* OCR Sub-Header */}
      <div className="p-4 border-b border-white/5 bg-[#0f192b] flex justify-between items-center">
        <div>
          <h3 className="text-xs font-bold text-white flex items-center gap-1.5">
            <span className="material-symbols-outlined text-primary text-sm">document_scanner</span>
            OCR Trích xuất Hóa đơn (Bán hàng)
          </h3>
          <p className="text-[9px] text-on-surface-variant">Tải ảnh hóa đơn lên để trích xuất vật phẩm bán lẻ tự động.</p>
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
          <input
            type="file"
            onChange={ocr.handleFileChange}
            accept="image/*"
            className="hidden"
            id="sales-file-input"
          />

          {/* Drag & Drop Area */}
          {!ocr.imagePreview ? (
            <div
              onDragOver={ocr.handleDragOver}
              onDragLeave={ocr.handleDragLeave}
              onDrop={ocr.handleDrop}
              onClick={() => document.getElementById('sales-file-input')?.click()}
              className={`flex-1 min-h-[160px] border-2 border-dashed rounded-xl flex flex-col items-center justify-center p-6 text-center cursor-pointer transition-all ${
                ocr.isDragOver ? 'border-primary bg-primary/5' : 'border-white/10 hover:border-primary/40 hover:bg-white/[0.01]'
              }`}
            >
              <span className="material-symbols-outlined text-3xl text-on-surface-variant mb-2">cloud_upload</span>
              <p className="text-xs font-semibold text-white">Kéo thả ảnh hóa đơn vào đây</p>
              <p className="text-[9px] text-on-surface-variant mt-1">hoặc click để chọn tệp (PNG, JPG, JPEG)</p>
            </div>
          ) : (
            <div className="relative group rounded-xl overflow-hidden border border-white/10 bg-black/20 flex flex-col items-center justify-center p-4 min-h-[220px] max-h-[280px]">
              {ocr.processedPreview && (
                <div className="absolute top-2 left-2 z-10 flex gap-1 bg-black/60 p-0.5 rounded-lg border border-white/5">
                  <button
                    type="button"
                    onClick={() => ocr.setShowProcessed(false)}
                    className={`px-2 py-0.5 rounded text-[8px] font-bold uppercase transition-all cursor-pointer ${
                      !ocr.showProcessed ? 'bg-primary text-on-primary font-extrabold' : 'text-on-surface-variant hover:text-white'
                    }`}
                  >
                    Ảnh gốc
                  </button>
                  <button
                    type="button"
                    onClick={() => ocr.setShowProcessed(true)}
                    className={`px-2 py-0.5 rounded text-[8px] font-bold uppercase transition-all cursor-pointer ${
                      ocr.showProcessed ? 'bg-primary text-on-primary font-extrabold' : 'text-on-surface-variant hover:text-white'
                    }`}
                  >
                    Ảnh đen trắng
                  </button>
                </div>
              )}

              <img
                src={ocr.showProcessed && ocr.processedPreview ? ocr.processedPreview : ocr.imagePreview}
                alt="Invoice preview"
                className="max-h-[160px] object-contain rounded mt-4"
              />
              {isUploading && (
                <div className="absolute bottom-2 left-2 right-2 bg-black/80 backdrop-blur-sm border border-primary/20 text-primary text-[9px] font-bold py-1 px-2 rounded-lg flex items-center justify-center gap-1.5 animate-pulse">
                  <span className="w-1.5 h-1.5 rounded-full bg-primary animate-ping"></span>
                  Đang tải ảnh lên S3...
                </div>
              )}
              <button
                type="button"
                onClick={handleClearAll}
                className="absolute top-2 right-2 bg-black/60 hover:bg-error text-white p-1 rounded-full hover:scale-105 transition-all flex items-center justify-center cursor-pointer"
                title="Xóa ảnh"
              >
                <span className="material-symbols-outlined text-sm">delete</span>
              </button>
            </div>
          )}

          {/* Preprocessing Controls */}
          {ocr.image && (
            <div className="bg-white/[0.02] border border-white/5 rounded-xl p-3.5 space-y-3">
              <div className="flex items-center justify-between">
                <label className="flex items-center gap-1.5 text-[10px] font-bold text-white uppercase tracking-wider cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={ocr.usePreprocessing}
                    onChange={(e) => ocr.setUsePreprocessing(e.target.checked)}
                    className="w-3.5 h-3.5 rounded bg-[#080d1a] border border-white/10 text-primary focus:ring-0 cursor-pointer"
                  />
                  Tiền xử lý ảnh (Lọc nền mờ & lưới Excel)
                </label>
                <span className="text-[8px] text-primary bg-primary/10 px-1.5 py-0.5 rounded font-mono font-bold uppercase tracking-wider">
                  Khuyên dùng
                </span>
              </div>
              
              {ocr.usePreprocessing && (
                <div className="space-y-2">
                  <div className="flex justify-between items-center text-[9px]">
                    <span className="text-on-surface-variant">Ngưỡng lọc nét chữ (Threshold)</span>
                    <span className="font-bold text-primary font-mono">{ocr.threshold} / 255</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-[9px] text-on-surface-variant">Tối hơn</span>
                    <input
                      type="range"
                      min="100"
                      max="245"
                      value={ocr.threshold}
                      onChange={(e) => ocr.setThreshold(parseInt(e.target.value))}
                      className="flex-1 h-1 bg-white/10 rounded-lg appearance-none cursor-pointer accent-primary"
                    />
                    <span className="text-[9px] text-on-surface-variant">Sáng hơn</span>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Scan controls */}
          <div className="flex gap-2">
            <div className="flex-1">
              <select
                value={ocr.language}
                onChange={(e) => ocr.setLanguage(e.target.value as any)}
                disabled={ocr.isScanning || !ocr.image}
                className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2.5 py-1.5 text-[11px] text-white focus:outline-none"
              >
                <option value="vie">Ngôn ngữ: Tiếng Việt</option>
                <option value="eng">Ngôn ngữ: Tiếng Anh</option>
                <option value="vie+eng">Ngôn ngữ: Việt + Anh</option>
              </select>
            </div>
            <button
              type="button"
              onClick={handleStartScan}
              disabled={ocr.isScanning || !ocr.image}
              className={`px-4 py-1.5 rounded-lg font-bold text-xs flex items-center gap-1 transition-all active:scale-95 cursor-pointer ${
                ocr.isScanning || !ocr.image
                  ? 'bg-white/5 text-on-surface-variant/40 cursor-not-allowed'
                  : 'bg-primary text-on-primary hover:shadow-[0_0_10px_rgba(73,252,223,0.3)]'
              }`}
            >
              <span className="material-symbols-outlined text-xs">play_arrow</span>
              <span>{ocr.isScanning ? 'Đang quét...' : 'Bắt đầu quét'}</span>
            </button>
          </div>

          {/* Progress bar */}
          {ocr.isScanning && (
            <div className="space-y-1">
              <div className="flex justify-between text-[9px] text-on-surface-variant">
                <span>{ocr.scanStatus}</span>
                <span className="font-bold text-primary">{ocr.scanProgress}%</span>
              </div>
              <div className="h-1.5 w-full bg-black/20 rounded-full overflow-hidden">
                <div
                  className="h-full bg-primary transition-all duration-300"
                  style={{ width: `${ocr.scanProgress}%` }}
                ></div>
              </div>
            </div>
          )}

          {/* Raw text */}
          {ocr.rawText && (
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
                value={ocr.rawText}
                onChange={handleRawTextChange}
                className="flex-1 bg-[#080d1a] border border-white/5 rounded-lg p-2 text-[10px] font-mono text-white resize-none focus:outline-none"
              />
            </div>
          )}
        </div>

        {/* Right Column: Structured review */}
        <div className="w-1/2 flex flex-col p-4 space-y-4 overflow-y-auto min-h-0">
          <label className="text-[10px] uppercase font-bold text-on-surface-variant tracking-wider">Kết quả phân tích (Bán hàng):</label>

          {parsedItems.length === 0 && !ocr.isScanning ? (
            <div className="flex-1 border border-white/5 rounded-xl flex flex-col items-center justify-center text-center p-6 text-on-surface-variant bg-black/5">
              <span className="material-symbols-outlined text-2xl mb-1.5 opacity-50">analytics</span>
              <p className="text-xs">Chưa có kết quả.</p>
              <p className="text-[9px] mt-1 max-w-[200px]">Sau khi quét hóa đơn thành công, các mục sản phẩm được phân tích sẽ hiển thị ở đây.</p>
            </div>
          ) : (
            <div className="flex-1 flex flex-col space-y-4 min-h-0">
              <div className="grid grid-cols-2 gap-3 bg-white/[0.02] border border-white/5 p-3 rounded-xl">
                <div>
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Mã HĐ trích xuất:</label>
                  <input
                    type="text"
                    value={invoiceId}
                    onChange={(e) => setInvoiceId(e.target.value)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                    placeholder="Chưa nhận diện được"
                  />
                </div>
                <div>
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Ngày hóa đơn:</label>
                  <input
                    type="date"
                    value={invoiceDate}
                    onChange={(e) => setInvoiceDate(e.target.value)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Khách hàng / Đối tác:</label>
                  <input
                    type="text"
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                    placeholder="Chưa nhận diện được"
                  />
                </div>
                <div>
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Số điện thoại:</label>
                  <input
                    type="text"
                    value={customerPhone}
                    onChange={(e) => setCustomerPhone(e.target.value)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                    placeholder="Chưa nhận diện được"
                  />
                </div>
                <div className="col-span-2">
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Địa chỉ giao hàng:</label>
                  <input
                    type="text"
                    value={customerAddress}
                    onChange={(e) => setCustomerAddress(e.target.value)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                    placeholder="Chưa nhận diện được"
                  />
                </div>
                <div className="col-span-2">
                  <label className="block text-[9px] text-on-surface-variant mb-1 font-semibold">Tiền khách trả / thanh toán:</label>
                  <input
                    type="number"
                    value={paidAmount || ''}
                    onChange={(e) => setPaidAmount(parseInt(e.target.value) || 0)}
                    className="w-full bg-[#080d1a] border border-white/10 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                    placeholder="Chưa nhận diện được"
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
                    const matchedProd = availableProducts.find(p => p.id === item.mappedProductId);
                    return (
                      <div key={item.id} className="p-3 space-y-2 hover:bg-white/[0.01] transition-all">
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

                        {/* Searchable Combobox */}
                        <div className="relative">
                          <div className="relative">
                            <input
                              type="text"
                              value={
                                activeDropdownId === item.id
                                  ? (rowSearchTerms[item.id] ?? '')
                                  : (matchedProd ? matchedProd.tenSanPham : '')
                              }
                              placeholder={
                                activeDropdownId === item.id
                                  ? (matchedProd ? matchedProd.tenSanPham : 'Gõ để tìm kiếm sản phẩm...')
                                  : '-- Chọn sản phẩm liên kết --'
                              }
                              onFocus={() => {
                                setActiveDropdownId(item.id);
                                setRowSearchTerms(prev => ({ ...prev, [item.id]: '' }));
                              }}
                              onBlur={() => {
                                setTimeout(() => {
                                  setActiveDropdownId(null);
                                  setRowSearchTerms(prev => ({ ...prev, [item.id]: '' }));
                                }, 250);
                              }}
                              onChange={(e) => {
                                setRowSearchTerms(prev => ({ ...prev, [item.id]: e.target.value }));
                              }}
                              className="w-full bg-[#080d1a] border border-white/10 rounded-lg pl-3 pr-8 py-1.5 text-[10px] text-white focus:outline-none"
                            />
                            <span className="material-symbols-outlined text-[14px] text-on-surface-variant absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none">
                              {activeDropdownId === item.id ? 'arrow_drop_up' : 'arrow_drop_down'}
                            </span>
                          </div>

                          {activeDropdownId === item.id && (
                            <div className="absolute left-0 right-0 mt-1 bg-[#0f192b] border border-white/10 rounded-lg shadow-xl max-h-40 overflow-y-auto z-50 divide-y divide-white/[0.02]">
                              <div
                                onClick={() => {
                                  handleRowChange(item.id, 'mappedProductId', 0);
                                  setActiveDropdownId(null);
                                  setRowSearchTerms(prev => ({ ...prev, [item.id]: '' }));
                                }}
                                className={`px-3 py-1.5 text-[9px] cursor-pointer text-warning hover:bg-white/5 ${
                                  item.mappedProductId === 0 ? 'bg-white/[0.02] font-semibold' : ''
                                }`}
                              >
                                -- Bỏ chọn sản phẩm --
                              </div>
                              {availableProducts
                                .filter(p => {
                                  if (p.id === item.mappedProductId) return true;
                                  const searchVal = (rowSearchTerms[item.id] || '').trim();
                                  if (!searchVal) return true;
                                  return p.tenSanPham.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').includes(
                                    searchVal.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')
                                  );
                                })
                                .map(p => (
                                  <div
                                    key={p.id}
                                    onClick={() => {
                                      handleRowChange(item.id, 'mappedProductId', p.id);
                                      setActiveDropdownId(null);
                                      setRowSearchTerms(prev => ({ ...prev, [item.id]: '' }));
                                    }}
                                    className={`px-3 py-1.5 text-[9px] cursor-pointer hover:bg-primary/10 hover:text-primary ${
                                      p.id === item.mappedProductId ? 'bg-primary/5 text-primary font-semibold' : 'text-white'
                                    }`}
                                  >
                                    {p.tenSanPham} ({formatVND(p.giaBanHienTai)})
                                  </div>
                                ))}
                            </div>
                          )}
                        </div>

                        {/* Quantity, DVT, Price */}
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
                              className="text-error hover:text-white p-1 hover:bg-error/10 rounded transition-colors cursor-pointer self-end"
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
