import { NextRequest, NextResponse } from 'next/server';
import Tesseract from 'tesseract.js';
import path from 'path';
import fs from 'fs/promises';
import fsSync from 'fs';
import crypto from 'crypto';
import { exec } from 'child_process';
import util from 'util';

const execPromise = util.promisify(exec);

interface ProductItem {
  id: number;
  tenSanPham: string;
  giaNhapHienTai: number;
  giaBanHienTai: number;
  donViTinh?: {
    id: number;
    tenDonVi: string;
  };
}

// Helper clean words for similarity match
const cleanWord = (w: string) => 
  w.toLowerCase()
   .normalize('NFD')
   .replace(/[\u0300-\u036f]/g, '') // Strip Vietnamese accents
   .replace(/[đĐ]/g, 'd')
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

const getCleanStringNoSpaces = (str: string) => {
  return str.toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[đĐ]/g, 'd')
            .replace(/[^\w\d]/g, '')
            .trim();
};

const getCharacterBigrams = (str: string): string[] => {
  const bigrams: string[] = [];
  for (let i = 0; i < str.length - 1; i++) {
    bigrams.push(str.substring(i, i + 2));
  }
  return bigrams;
};

const getBigramSimilarity = (str1: string, str2: string): number => {
  const clean1 = getCleanStringNoSpaces(str1);
  const clean2 = getCleanStringNoSpaces(str2);
  if (!clean1 || !clean2) return 0;
  if (clean1 === clean2) return 1.0;

  const b1 = getCharacterBigrams(clean1);
  const b2 = getCharacterBigrams(clean2);
  if (b1.length === 0 || b2.length === 0) return 0;

  const map = new Map<string, number>();
  for (const bigram of b1) {
    map.set(bigram, (map.get(bigram) || 0) + 1);
  }

  let intersection = 0;
  for (const bigram of b2) {
    const count = map.get(bigram) || 0;
    if (count > 0) {
      intersection++;
      map.set(bigram, count - 1);
    }
  }

  return (2.0 * intersection) / (b1.length + b2.length);
};

const findBestProductMatch = (rawName: string, products: ProductItem[]): ProductItem | null => {
  let bestProduct: ProductItem | null = null;
  let maxSim = 0;

  for (const prod of products) {
    const wordSim = getWordOverlapSimilarity(rawName, prod.tenSanPham);
    const bigramSim = getBigramSimilarity(rawName, prod.tenSanPham);
    const sim = Math.max(wordSim, bigramSim);
    
    const cleanRaw = getCleanStringNoSpaces(rawName);
    const cleanProd = getCleanStringNoSpaces(prod.tenSanPham);
    const isSub = cleanRaw.length >= 4 && cleanProd.length >= 4 && (
      cleanRaw.includes(cleanProd) || cleanProd.includes(cleanRaw)
    );
    const score = isSub ? Math.max(sim, 0.6) : sim;

    if (score > maxSim && score > 0.25) {
      maxSim = score;
      bestProduct = prod;
    }
  }
  return bestProduct;
};

const findProductCandidates = (rawName: string, products: ProductItem[]): ProductItem[] => {
  const matches: Array<{ prod: ProductItem; score: number }> = [];

  for (const prod of products) {
    const wordSim = getWordOverlapSimilarity(rawName, prod.tenSanPham);
    const bigramSim = getBigramSimilarity(rawName, prod.tenSanPham);
    const sim = Math.max(wordSim, bigramSim);
    
    const cleanRaw = getCleanStringNoSpaces(rawName);
    const cleanProd = getCleanStringNoSpaces(prod.tenSanPham);
    const isExactName = cleanRaw === cleanProd;
    
    const isSub = cleanRaw.length >= 4 && cleanProd.length >= 4 && (
      cleanRaw.includes(cleanProd) || cleanProd.includes(cleanRaw)
    );
    let score = isSub ? Math.max(sim, 0.6) : sim;
    if (isExactName) {
      score = 1.0;
    }

    if (score >= 0.35 || isExactName) {
      matches.push({ prod, score });
    }
  }

  matches.sort((a, b) => b.score - a.score);

  if (matches.length === 0) return [];

  const topMatch = matches[0];
  
  // Find all active products in the DB with the exact same name as the top match
  const cleanTopName = getCleanStringNoSpaces(topMatch.prod.tenSanPham);
  const duplicates = products.filter(p => getCleanStringNoSpaces(p.tenSanPham) === cleanTopName);
  
  if (duplicates.length > 1) {
    // If there are duplicate names, return all of them
    return duplicates;
  }

  // If the top match is very strong, and there are no duplicate names, just return the top match
  if (topMatch.score >= 0.75) {
    return [topMatch.prod];
  }

  // Otherwise, return all matches with score >= 0.4 for user selection
  return matches.filter(m => m.score >= 0.4).map(m => m.prod);
};

export async function POST(req: NextRequest) {
  try {
    const formData = await req.formData();
    const file = formData.get('file') as File;
    const lang = (formData.get('lang') as string) || 'vie';

    if (!file) {
      return NextResponse.json({ success: false, error: 'Yêu cầu tệp ảnh trong trường "file"' }, { status: 400 });
    }

    const arrayBuffer = await file.arrayBuffer();
    const buffer = Buffer.from(arrayBuffer);

    // Image Preprocessing on Backend Server using Python (Binarization & Gridlines removal)
    const tempId = crypto.randomUUID();
    const scratchDir = path.join(process.cwd(), '..', '..', 'scratch');
    const inputTempPath = path.join(scratchDir, `temp_ocr_in_${tempId}.png`);
    const outputTempPath = path.join(scratchDir, `temp_ocr_out_${tempId}.png`);

    let processedBuffer = buffer;
    try {
      if (!fsSync.existsSync(scratchDir)) {
        await fs.mkdir(scratchDir, { recursive: true });
      }

      await fs.writeFile(inputTempPath, buffer);

      const scriptPath = path.join(scratchDir, 'preprocess.py');
      const pythonCmd = `python "${scriptPath}" "${inputTempPath}" "${outputTempPath}" 200`;
      
      await execPromise(pythonCmd);

      if (fsSync.existsSync(outputTempPath)) {
        processedBuffer = await fs.readFile(outputTempPath);
      }
    } catch (pyErr) {
      console.warn('Backend image preprocessing failed, falling back to raw image:', pyErr);
    } finally {
      try {
        if (fsSync.existsSync(inputTempPath)) await fs.unlink(inputTempPath);
        if (fsSync.existsSync(outputTempPath)) await fs.unlink(outputTempPath);
      } catch (cleanErr) {
        console.warn('Failed to clean up temporary OCR files:', cleanErr);
      }
    }

    // Load tessdata locally from filesystem on Node.js side (prevents self-request deadlocks)
    const langPath = path.join(process.cwd(), 'public', 'tessdata');

    // Call Tesseract OCR on Server side (runs WASM natively in Node) using local files
    const ocrResult = await Tesseract.recognize(processedBuffer, lang, {
      langPath,
      gzip: false,
      cacheMethod: 'none'
    });
    const text = ocrResult.data.text;

    // Get Authorization header from incoming NextRequest to forward to Spring Boot backend
    const authHeader = req.headers.get('authorization');
    const fetchHeaders: HeadersInit = {};
    if (authHeader) {
      fetchHeaders['Authorization'] = authHeader;
    }

    // Fetch active products from the Spring Boot backend
    let products: ProductItem[] = [];
    try {
      const prodRes = await fetch('http://localhost:8080/api/v1/san-pham', {
        headers: fetchHeaders
      });
      if (prodRes.ok) {
        products = await prodRes.json();
      }
    } catch (err) {
      console.warn('Không thể tải danh sách sản phẩm từ backend Spring Boot:', err);
    }

    const lines = text.split('\n');

    // Parse Invoice ID
    const idRegex = /(?:số hóa đơn|số hđ|mã hđ|hđ|invoice\s*no|invoice|so\s*hd)\s*[:.-]?\s*([^\s,;]+)/i;
    let invoiceId = '';

    // Parse Date
    const dateRegex = /(?:ngày|date)\s*[:.-]?\s*([\d\/\.-]+)/i;
    let date = '';

    // Parse Total Amount
    const totalRegex = /(?:tổng cộng|thành tiền|tổng tiền|tổng\s*thanh\s*toán|total)\s*[:.-]?\s*([\d.,\s]+)(?:đ|vnd|vnd)?/i;
    let totalAmount = 0;

    // Parse Customer details
    const customerNameRegex = /\b(?:khách\s+[hàb]àng|khach\s+[hab]ang|customer|tên\s+khách|ten\s+khach|người\s+mua|nguoi\s+mua|\bkh(?=\s*[:.-]|\s+))\s*[:.-]?\s*([^\n\r]+)/i;
    let customerName = '';

    const phoneRegex = /(?:sđt|sdt|đt|dt|điện thoại|dien thoai|phone|tel)\s*[:.-]?\s*([\d\s.-]{7,15})/i;
    let customerPhone = '';

    const addressRegex = /(?:địa chỉ|dia chi|đạa chỉ|daa chi|da chi|address)\s*[:.-]?\s*([^\n\r]+)/i;
    let customerAddress = '';

    const paidRegex = /(?:khách hàng thanh toán|khách thanh toán|đã trả|thanh toán|khach hang thanh toan|khach thanh toan|da tra|paid)\s*[:.-]?\s*([\d.,\s]+)/i;
    let paidAmount = 0;

    const parseRobustDate = (matchStr: string): string => {
      if (matchStr.includes('/') || matchStr.includes('.') || matchStr.includes('-')) {
        const parts = matchStr.split(/[\/\.-]/);
        if (parts.length === 3) {
          let day = parts[0];
          let month = parts[1];
          let year = parts[2];
          if (day.length === 1) day = '0' + day;
          if (month.length === 1) month = '0' + month;
          if (year.length === 2) year = '20' + year;
          return `${year}-${month}-${day}`;
        }
      }

      const clean = matchStr.replace(/[^\d]/g, '');
      if (clean.length === 8) {
        if (clean.startsWith('20') || clean.startsWith('19')) {
          return `${clean.substring(0, 4)}-${clean.substring(4, 6)}-${clean.substring(6, 8)}`;
        }
        return `${clean.substring(4, 8)}-${clean.substring(2, 4)}-${clean.substring(0, 2)}`;
      }
      if (clean.length === 9) {
        return `${clean.substring(5, 9)}-${clean.substring(3, 5)}-${clean.substring(0, 2)}`;
      }
      if (clean.length === 10) {
        if (clean.startsWith('20') || clean.startsWith('19')) {
          return `${clean.substring(0, 4)}-${clean.substring(5, 7)}-${clean.substring(8, 10)}`;
        }
        return `${clean.substring(6, 10)}-${clean.substring(3, 5)}-${clean.substring(0, 2)}`;
      }
      return '';
    };

    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed) continue;

      if (!invoiceId) {
        const idMatch = trimmed.match(idRegex);
        if (idMatch) invoiceId = idMatch[1];
      }

      if (!date) {
        const dateMatch = trimmed.match(dateRegex);
        if (dateMatch) {
          date = parseRobustDate(dateMatch[1]);
        }
      }

      if (!totalAmount) {
        const totalMatch = trimmed.match(totalRegex);
        if (totalMatch) {
          const cleaned = totalMatch[1].replace(/[^\d]/g, '');
          totalAmount = parseInt(cleaned) || 0;
        }
      }

      if (!customerName) {
        const nameMatch = trimmed.match(customerNameRegex);
        if (nameMatch) {
          const val = nameMatch[1].trim();
          const cleanVal = val.toLowerCase()
                              .normalize('NFD')
                              .replace(/[\u0300-\u036f]/g, '')
                              .replace(/[đĐ]/g, 'd');
          if (!cleanVal.includes('thanh toan') && !cleanVal.includes('tra') && !cleanVal.includes('chuyen khoan') && !cleanVal.includes('ck')) {
            customerName = val;
          }
        }
      }

      const phoneMatch = trimmed.match(phoneRegex);
      if (phoneMatch) {
        customerPhone = phoneMatch[1].replace(/[^\d]/g, '');
      }

      const addressMatch = trimmed.match(addressRegex);
      if (addressMatch) {
        customerAddress = addressMatch[1].trim();
      }

      const paidMatch = trimmed.match(paidRegex);
      if (paidMatch) {
        const clean = paidMatch[1].replace(/[^\d]/g, '');
        paidAmount = parseInt(clean) || 0;
      }
    }

    // Parse Items
    const COMMON_DVTS = [
      'cái', 'cai', 'ca', 'cá', 'ly', 'kg', 'kg.', 'chai', 'lon', 'bao', 'hộp', 'hop', 
      'bịch', 'bich', 'gói', 'goi', 'tấm', 'tam', 'cuộn', 'cuon', 'hủ', 'hu', 
      'thùng', 'thung', 'két', 'ket', 'mét', 'met', 'lọ', 'lo', 'vỉ', 'vi', 
      'miếng', 'mieng', 'cục', 'cuc', 'bó', 'bo', 'lít', 'lit', 'ml', 'đĩa', 
      'dia', 'phần', 'phan', 'suất', 'suat', 'túi', 'tui', 'khay', 'set'
    ];

    const items: Array<{
      rawName: string;
      productId: number;
      productName: string;
      qty: number;
      price: number;
      total: number;
      candidates: Array<{
        id: number;
        tenSanPham: string;
        giaBanHienTai: number;
        giaNhapHienTai: number;
        donVi: string;
      }>;
    }> = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;

      const lowerLine = line.toLowerCase();

      // Stop parsing items if we hit the footer / policy section (e.g. "Lưu ý", "Hưu ý", "Hưu ÿ", "Quy định", "Thời gian bảo hành")
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
        cleanLine.includes('daa chi') || // typo
        cleanLine.includes('da chi') ||  // typo
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

      // Filter words: keep only alphanumeric words, DVTs, or placeholder hyphens/symbols at the end of the line
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

      let candidates = findProductCandidates(productName, products);
      let finalDvt = hasDvt ? foundDvt : '';

      if (candidates.length === 0 && words.length >= 4) {
        const fullName = [...productNameWords, finalDvt].join(' ');
        candidates = findProductCandidates(fullName, products);
      }

      let matchedProduct = candidates.length > 0 ? candidates[0] : null;

      if (matchedProduct && !finalDvt) {
        finalDvt = matchedProduct.donViTinh ? (matchedProduct.donViTinh as any).tenDonVi : 'cái';
      }

      const normalizedDvt = finalDvt.toLowerCase()
                                    .normalize('NFD')
                                    .replace(/[\u0300-\u036f]/g, '')
                                    .replace(/[^\w\d]/g, '')
                                    .trim();
      const isDvtValid = COMMON_DVTS.includes(normalizedDvt);

      if (!isDvtValid && !matchedProduct) {
        continue;
      }

      items.push({
        rawName: productName,
        productId: matchedProduct ? matchedProduct.id : 0,
        productName: matchedProduct ? matchedProduct.tenSanPham : 'Không khớp',
        qty,
        price: isGift ? 0 : priceVal,
        total: isGift ? 0 : (qty * priceVal),
        candidates: candidates.map(c => ({
          id: c.id,
          tenSanPham: c.tenSanPham,
          giaBanHienTai: c.giaBanHienTai,
          giaNhapHienTai: c.giaNhapHienTai,
          donVi: c.donViTinh ? c.donViTinh.tenDonVi : 'cái'
        }))
      });
    }

    // Heuristics for Invoice suggestedType (NHAP_HANG or BAN_HANG)
    const textLower = text.toLowerCase();
    const purchaseKeywords = [
      'nhap kho', 'nhap hang', 'nha cung cap', 'ncc', 'don vi ban', 'nguoi ban', 'hoa don mua', 'phieu nhap'
    ];
    const salesKeywords = [
      'ban hang', 'ban le', 'khach hang', 'nguoi mua', 'ten khach', 'phieu xuat', 'xuat kho', 'hoa don ban hang', 'hoa don ban le'
    ];
    let purchaseScore = 0;
    let salesScore = 0;
    
    const cleanTextLower = getCleanStringNoSpaces(textLower);
    purchaseKeywords.forEach(k => {
      const cleanK = getCleanStringNoSpaces(k);
      if (cleanTextLower.includes(cleanK)) {
        purchaseScore++;
      }
    });
    salesKeywords.forEach(k => {
      const cleanK = getCleanStringNoSpaces(k);
      if (cleanTextLower.includes(cleanK)) {
        salesScore++;
      }
    });
    let suggestedType = 'CHUA_XAC_DINH';
    if (purchaseScore > salesScore) {
      suggestedType = 'NHAP_HANG';
    } else if (salesScore > purchaseScore) {
      suggestedType = 'BAN_HANG';
    }

    let isDuplicate = false;
    if (invoiceId) {
      const invoiceNumber = parseInt(invoiceId.replace(/[^\d]/g, ''));
      if (!isNaN(invoiceNumber)) {
        try {
          const checkSale = await fetch(`http://localhost:8080/api/v1/ban-hang/${invoiceNumber}`, { headers: fetchHeaders });
          if (checkSale.ok) {
            isDuplicate = true;
          } else {
            const checkPurchase = await fetch(`http://localhost:8080/api/v1/nhap-hang/${invoiceNumber}`, { headers: fetchHeaders });
            if (checkPurchase.ok) {
              isDuplicate = true;
            }
          }
        } catch (err) {
          console.warn('Không thể kiểm tra trùng hóa đơn:', err);
        }
      }
    }

    const stage = req.nextUrl.searchParams.get('stage') === 'true';
    let pendingOcrId = '';
    let confirmUrl = '';

    if (stage) {
      pendingOcrId = crypto.randomUUID();
      const scratchDir = path.join(process.cwd(), '..', '..', 'scratch');
      if (!fsSync.existsSync(scratchDir)) {
        await fs.mkdir(scratchDir, { recursive: true });
      }

      const ocrDataToSave = {
        invoiceId,
        date,
        customerName,
        customerPhone,
        customerAddress,
        paidAmount,
        totalAmount,
        suggestedType,
        items
      };

      await fs.writeFile(
        path.join(scratchDir, `pending_ocr_${pendingOcrId}.json`),
        JSON.stringify(ocrDataToSave, null, 2),
        'utf8'
      );

      const baseUrl = process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000';
      const pagePath = suggestedType === 'NHAP_HANG' ? 'purchases' : 'sales';
      confirmUrl = `${baseUrl}/${pagePath}?pendingOcrId=${pendingOcrId}`;
    }

    return NextResponse.json({
      success: true,
      rawText: text,
      suggestedType,
      invoiceId,
      isDuplicate,
      date,
      totalAmount,
      customerName,
      customerPhone,
      customerAddress,
      paidAmount,
      items,
      ...(stage ? { pendingOcrId, confirmUrl } : {})
    });
  } catch (err: any) {
    console.error('Lỗi khi thực hiện quét OCR server-side:', err);
    return NextResponse.json({ success: false, error: err.message || 'Lỗi server khi quét OCR' }, { status: 500 });
  }
}
