import { ProductItem } from '@/types';

// List of common unit of measures
export const COMMON_DVTS = [
  'cái', 'cai', 'ca', 'cá', 'ly', 'kg', 'kg.', 'chai', 'lon', 'bao', 'hộp', 'hop', 
  'bịch', 'bich', 'gói', 'goi', 'tấm', 'tam', 'cuộn', 'cuon', 'hủ', 'hu', 
  'thùng', 'thung', 'két', 'ket', 'mét', 'met', 'lọ', 'lo', 'vỉ', 'vi', 
  'miếng', 'mieng', 'cục', 'cuc', 'bó', 'bo', 'lít', 'lit', 'ml', 'đĩa', 
  'dia', 'phần', 'phan', 'suất', 'suat', 'túi', 'tui', 'khay', 'set'
];

// Clean words for similarity match
export const cleanWord = (w: string): string => 
  w.toLowerCase()
   .normalize('NFD')
   .replace(/[\u0300-\u036f]/g, '') // Strip Vietnamese accents
   .replace(/[đĐ]/g, 'd')
   .replace(/[^\w\d]/g, '');

export const getWords = (str: string): string[] => 
  str.split(/\s+/)
     .map(cleanWord)
     .filter(w => w.length > 1);

export const getWordOverlapSimilarity = (str1: string, str2: string): number => {
  const w1 = getWords(str1);
  const w2 = getWords(str2);
  if (w1.length === 0 || w2.length === 0) return 0;
  const intersection = w1.filter(w => w2.includes(w));
  return intersection.length / Math.max(w1.length, w2.length);
};

export const getCleanStringNoSpaces = (str: string): string => {
  return str.toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[đĐ]/g, 'd')
            .replace(/[^\w\d]/g, '')
            .trim();
};

export const getCharacterBigrams = (str: string): string[] => {
  const bigrams: string[] = [];
  for (let i = 0; i < str.length - 1; i++) {
    bigrams.push(str.substring(i, i + 2));
  }
  return bigrams;
};

export const getBigramSimilarity = (str1: string, str2: string): number => {
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

export const findBestProductMatch = (rawName: string, availableProducts: ProductItem[]): number => {
  let bestProductId = 0;
  let maxSim = 0;

  for (const prod of availableProducts) {
    const wordSim = getWordOverlapSimilarity(rawName, prod.tenSanPham);
    const bigramSim = getBigramSimilarity(rawName, prod.tenSanPham);
    const sim = Math.max(wordSim, bigramSim);
    
    // If exact substring match, boost score
    const cleanRaw = getCleanStringNoSpaces(rawName);
    const cleanProd = getCleanStringNoSpaces(prod.tenSanPham);
    const isSub = cleanRaw.length >= 4 && cleanProd.length >= 4 && (
      cleanRaw.includes(cleanProd) || cleanProd.includes(cleanRaw)
    );
    const score = isSub ? Math.max(sim, 0.6) : sim;

    if (score > maxSim && score > 0.25) {
      maxSim = score;
      bestProductId = prod.id;
    }
  }
  return bestProductId;
};

export const parseRobustDate = (matchStr: string): string => {
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

export const translateStatus = (status: string): string => {
  switch (status) {
    case 'loading tesseract core': return 'Đang tải nhân Tesseract...';
    case 'initializing api': return 'Đang thiết lập API...';
    case 'recognizing text': return 'Đang nhận diện văn bản...';
    default: return status;
  }
};

// Binarize/Thresholding image to remove watermark and table gridlines
export const preprocessImage = (imageFile: File, threshold: number): Promise<string> => {
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

        // Calculate dynamic scale factor to upscale small images (improving Tesseract OCR accuracy)
        let scale = 1.0;
        if (img.width < 1500) {
          scale = 2.0;
        }
        if (img.width < 800) {
          scale = 3.0;
        }

        canvas.width = img.width * scale;
        canvas.height = img.height * scale;

        ctx.imageSmoothingEnabled = true;
        ctx.imageSmoothingQuality = 'high';
        ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const data = imageData.data;
        const width = canvas.width;
        const height = canvas.height;

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

        // Detect and remove vertical and horizontal gridlines to help Tesseract layout analysis
        const binary = new Uint8Array(width * height);
        for (let idx = 0; idx < binary.length; idx++) {
          binary[idx] = data[idx * 4] === 0 ? 1 : 0;
        }

        const toRemove = new Uint8Array(width * height);

        // Scale thresholds according to upscaling factor
        const minVerticalRun = Math.round(15 * scale);
        const maxVerticalThickness = Math.round(4 * scale);
        const minHorizontalRun = Math.round(25 * scale);
        const maxHorizontalThickness = Math.round(4 * scale);

        // Remove vertical gridlines (length >= minVerticalRun, >= 80% thin pixels)
        for (let x = 0; x < width; x++) {
          let runStart = -1;
          for (let y = 0; y < height; y++) {
            const idx = y * width + x;
            if (binary[idx] === 1) {
              if (runStart === -1) runStart = y;
            } else {
              if (runStart !== -1) {
                const runLength = y - runStart;
                if (runLength >= minVerticalRun) {
                  let thinPixels = 0;
                  for (let ry = runStart; ry < y; ry++) {
                    let left = 0;
                    while (x - left >= 0 && binary[ry * width + (x - left)] === 1) left++;
                    let right = 0;
                    while (x + right < width && binary[ry * width + (x + right)] === 1) right++;
                    const thickness = left + right - 1;
                    if (thickness <= maxVerticalThickness) thinPixels++;
                  }
                  if (thinPixels / runLength >= 0.8) {
                    for (let ry = runStart; ry < y; ry++) {
                      toRemove[ry * width + x] = 1;
                    }
                  }
                }
                runStart = -1;
              }
            }
          }
          if (runStart !== -1) {
            const runLength = height - runStart;
            if (runLength >= minVerticalRun) {
              let thinPixels = 0;
              for (let ry = runStart; ry < height; ry++) {
                let left = 0;
                while (x - left >= 0 && binary[ry * width + (x - left)] === 1) left++;
                let right = 0;
                while (x + right < width && binary[ry * width + (x + right)] === 1) right++;
                const thickness = left + right - 1;
                if (thickness <= maxVerticalThickness) thinPixels++;
              }
              if (thinPixels / runLength >= 0.8) {
                for (let ry = runStart; ry < height; ry++) {
                  toRemove[ry * width + x] = 1;
                }
              }
            }
          }
        }

        // Remove horizontal gridlines (length >= minHorizontalRun, >= 80% thin pixels)
        for (let y = 0; y < height; y++) {
          let runStart = -1;
          for (let x = 0; x < width; x++) {
            const idx = y * width + x;
            if (binary[idx] === 1) {
              if (runStart === -1) runStart = x;
            } else {
              if (runStart !== -1) {
                const runLength = x - runStart;
                if (runLength >= minHorizontalRun) {
                  let thinPixels = 0;
                  for (let rx = runStart; rx < x; rx++) {
                    let up = 0;
                    while (y - up >= 0 && binary[(y - up) * width + rx] === 1) up++;
                    let down = 0;
                    while (y + down < height && binary[(y + down) * width + rx] === 1) down++;
                    const thickness = up + down - 1;
                    if (thickness <= maxHorizontalThickness) thinPixels++;
                  }
                  if (thinPixels / runLength >= 0.8) {
                    for (let rx = runStart; rx < x; rx++) {
                      toRemove[y * width + rx] = 1;
                    }
                  }
                }
                runStart = -1;
              }
            }
          }
          if (runStart !== -1) {
            const runLength = width - runStart;
            if (runLength >= minHorizontalRun) {
              let thinPixels = 0;
              for (let rx = runStart; rx < width; rx++) {
                let up = 0;
                while (y - up >= 0 && binary[(y - up) * width + rx] === 1) up++;
                let down = 0;
                while (y + down < height && binary[(y + down) * width + rx] === 1) down++;
                const thickness = up + down - 1;
                if (thickness <= maxHorizontalThickness) thinPixels++;
              }
              if (thinPixels / runLength >= 0.8) {
                for (let rx = runStart; rx < width; rx++) {
                  toRemove[y * width + rx] = 1;
                }
              }
            }
          }
        }

        // Apply removal mask to final image data (white)
        for (let idx = 0; idx < toRemove.length; idx++) {
          if (toRemove[idx] === 1) {
            const i = idx * 4;
            data[i] = 255;
            data[i + 1] = 255;
            data[i + 2] = 255;
          }
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
