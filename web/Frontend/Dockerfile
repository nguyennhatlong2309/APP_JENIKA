# Bước 1: Cài đặt dependencies và build Next.js (Chuyển sang Node 20 và dùng npm install)
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
# Dùng npm install để tự sửa lỗi lệch file lock
RUN npm install
COPY . .

# Truyền link API để Next.js build tĩnh (Build-time ENV)
ARG NEXT_PUBLIC_API_URL=https://jenkam.site/api
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

RUN npm run build

# Bước 2: Khởi chạy ứng dụng Next.js
FROM node:20-alpine AS runner
WORKDIR /app
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
# Copy các file OCR tiếng Việt và tiếng Anh cần thiết
COPY --from=builder /app/vie.traineddata ./
COPY --from=builder /app/eng.traineddata ./

EXPOSE 3000
CMD ["npm", "run", "start"]
