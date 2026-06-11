import { NextRequest, NextResponse } from 'next/server';
import Tesseract from 'tesseract.js';
import path from 'path';

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

const findBestProductMatch = (rawName: string, products: ProductItem[]): ProductItem | null => {
  let bestProduct: ProductItem | null = null;
  let maxSim = 0;

  for (const prod of products) {
    const sim = getWordOverlapSimilarity(rawName, prod.tenSanPham);
    const isSub = rawName.toLowerCase().includes(prod.tenSanPham.toLowerCase()) || 
                  prod.tenSanPham.toLowerCase().includes(rawName.toLowerCase());
    const score = isSub ? Math.max(sim, 0.6) : sim;

    if (score > maxSim && score > 0.25) {
      maxSim = score;
      bestProduct = prod;
    }
  }
  return bestProduct;
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

    // Load tessdata locally from filesystem on Node.js side (prevents self-request deadlocks)
    const langPath = path.join(process.cwd(), 'public', 'tessdata');

    // Call Tesseract OCR on Server side (runs WASM natively in Node) using local files
    const ocrResult = await Tesseract.recognize(buffer, lang, {
      langPath
    });
    const text = ocrResult.data.text;

    // Fetch active products from the Spring Boot backend
    let products: ProductItem[] = [];
    try {
      const prodRes = await fetch('http://localhost:8080/api/v1/san-pham');
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
    const dateRegex = /(?:ngày|date)\s*[:.-]?\s*(\d{1,2}[\/\.-]\d{1,2}[\/\.-]\d{2,4})/i;
    let date = '';

    // Parse Total Amount
    const totalRegex = /(?:tổng cộng|thành tiền|tổng tiền|tổng\s*thanh\s*toán|total)\s*[:.-]?\s*([\d.,\s]+)(?:đ|vnd|vnd)?/i;
    let totalAmount = 0;

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
          const parts = dateMatch[1].split(/[\/\.-]/);
          if (parts.length === 3) {
            let day = parts[0];
            let month = parts[1];
            let year = parts[2];
            if (day.length === 1) day = '0' + day;
            if (month.length === 1) month = '0' + month;
            if (year.length === 2) year = '20' + year;
            date = `${year}-${month}-${day}`;
          }
        }
      }

      if (!totalAmount) {
        const totalMatch = trimmed.match(totalRegex);
        if (totalMatch) {
          const cleaned = totalMatch[1].replace(/[^\d]/g, '');
          totalAmount = parseInt(cleaned) || 0;
        }
      }
    }

    // Parse Items
    const COMMON_DVTS = [
      'gam', 'g', 'cái', 'cai', 'ly', 'kg', 'kg.', 'chai', 'lon', 'bao', 'hộp', 'hop', 
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
    }> = [];

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
      if (words.length < 3) continue; // Must have at least: Name, Price, Total

      const lastWord = words[words.length - 1];
      const secondLastWord = words[words.length - 2];
      const thirdLastWord = words[words.length - 3];

      const totalVal = parseInt(lastWord.replace(/[^\d]/g, '')) || 0;
      const priceVal = parseInt(secondLastWord.replace(/[^\d]/g, '')) || 0;

      // Basic check: Total and Price must be valid currency values (>= 1000)
      if (totalVal < 1000 || priceVal < 1000) continue;

      let qty = 1;
      let dvt = '';
      let productNameWords: string[] = [];

      // Check if third last word is a quantity number
      const qtyMatch = thirdLastWord.match(/^\d+$/);
      if (qtyMatch && words.length >= 4) {
        qty = parseInt(thirdLastWord) || 1;
        dvt = words[words.length - 4];
        productNameWords = words.slice(0, words.length - 4);
      } else {
        qty = 1;
        dvt = thirdLastWord;
        productNameWords = words.slice(0, words.length - 3);
      }

      const productName = productNameWords.join(' ').replace(/^[\s|.-]+|[\s|.-]+$/g, '').trim();
      if (productName.length < 2) continue;

      const normalizedDvt = dvt.toLowerCase()
                               .normalize('NFD')
                               .replace(/[\u0300-\u036f]/g, '')
                               .replace(/[^\w\d]/g, '')
                               .trim();

      // Find best matching product
      let matchedProduct = findBestProductMatch(productName, products);
      const isDvtValid = COMMON_DVTS.includes(normalizedDvt);
      let finalDvt = isDvtValid ? dvt : '';

      if (!matchedProduct && words.length >= 4) {
        // Try including the dvt word in the product name
        const fullName = [...productNameWords, dvt].join(' ');
        matchedProduct = findBestProductMatch(fullName, products);
      }

      // If it doesn't have a valid ĐVT and doesn't match any system product, skip
      if (!isDvtValid && !matchedProduct) {
        continue;
      }

      if (matchedProduct && !finalDvt) {
        finalDvt = matchedProduct.donViTinh ? (matchedProduct.donViTinh as any).tenDonVi : 'cái';
      }

      items.push({
        rawName: productName,
        productId: matchedProduct ? matchedProduct.id : 0,
        productName: matchedProduct ? matchedProduct.tenSanPham : 'Không khớp',
        qty,
        price: priceVal,
        total: qty * priceVal
      });
    }

    return NextResponse.json({
      success: true,
      rawText: text,
      invoiceId,
      date,
      totalAmount,
      items
    });
  } catch (err: any) {
    console.error('Lỗi khi thực hiện quét OCR server-side:', err);
    return NextResponse.json({ success: false, error: err.message || 'Lỗi server khi quét OCR' }, { status: 500 });
  }
}
