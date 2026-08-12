import { useState, useEffect } from 'react';
import Tesseract from 'tesseract.js';
import { preprocessImage, translateStatus } from '../utils/ocrUtils';

export function useOcrEngine() {
  const [image, setImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [language, setLanguage] = useState<'vie' | 'eng' | 'vie+eng'>('vie');
  const [isScanning, setIsScanning] = useState(false);
  const [scanProgress, setScanProgress] = useState(0);
  const [scanStatus, setScanStatus] = useState('');
  const [rawText, setRawText] = useState('');

  // Preprocessing states
  const [threshold, setThreshold] = useState<number>(200);
  const [usePreprocessing, setUsePreprocessing] = useState<boolean>(true);
  const [processedPreview, setProcessedPreview] = useState<string | null>(null);
  const [showProcessed, setShowProcessed] = useState<boolean>(true);
  const [isDragOver, setIsDragOver] = useState(false);

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
        setThreshold(200);
        setShowProcessed(true);
      } else {
        alert('Vui lòng chọn một file ảnh (PNG, JPG, JPEG).');
      }
    }
  };

  const handleStartScan = async (onScanSuccess: (text: string) => void) => {
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
          langPath: window.location.origin + '/tessdata/',
          gzip: false,
          cacheMethod: 'none',
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
      setScanStatus('Quét thành công!');
      onScanSuccess(text);
    } catch (err: any) {
      console.error(err);
      alert('Đã xảy ra lỗi trong quá trình quét: ' + (err.message || err));
      setScanStatus('Quét thất bại.');
    } finally {
      setIsScanning(false);
    }
  };

  const resetEngine = () => {
    setImage(null);
    setImagePreview(null);
    setProcessedPreview(null);
    setRawText('');
    setScanProgress(0);
    setScanStatus('');
    setThreshold(200);
    setShowProcessed(true);
  };

  return {
    image,
    imagePreview,
    processedPreview,
    language,
    isScanning,
    scanProgress,
    scanStatus,
    rawText,
    threshold,
    usePreprocessing,
    showProcessed,
    isDragOver,
    setImage,
    setImagePreview,
    setLanguage,
    setRawText,
    setThreshold,
    setUsePreprocessing,
    setShowProcessed,
    handleFileChange,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    handleStartScan,
    resetEngine
  };
}
