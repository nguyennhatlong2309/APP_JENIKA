import { NextRequest, NextResponse } from 'next/server';
import path from 'path';
import fs from 'fs/promises';
import fsSync from 'fs';

export async function GET(req: NextRequest) {
  try {
    const id = req.nextUrl.searchParams.get('id');
    if (!id) {
      return NextResponse.json({ success: false, error: 'Thiếu id' }, { status: 400 });
    }

    // scratch is located at the workspace root
    const scratchDir = path.join(process.cwd(), '..', '..', 'scratch');
    const filePath = path.join(scratchDir, `pending_ocr_${id}.json`);

    if (!fsSync.existsSync(filePath)) {
      return NextResponse.json(
        { success: false, error: 'Không tìm thấy hóa đơn chờ xử lý hoặc đã hết hạn' },
        { status: 404 }
      );
    }

    const dataStr = await fs.readFile(filePath, 'utf8');
    const ocrData = JSON.parse(dataStr);

    return NextResponse.json({
      success: true,
      ocrData
    });
  } catch (err: any) {
    console.error('Lỗi khi lấy thông tin ocr chờ xử lý:', err);
    return NextResponse.json({ success: false, error: err.message || 'Lỗi server' }, { status: 500 });
  }
}
