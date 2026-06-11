const Tesseract = require('tesseract.js');
const path = require('path');
const fs = require('fs');

const imagePath = path.join(__dirname, '..', '..', 'zExamImportImage', 'z7871556744457_bc039a171023307de5c4fd88560518f0.jpg');
const langPath = path.join(__dirname, 'public', 'tessdata');

console.log("Image path exists:", fs.existsSync(imagePath));
console.log("Lang path exists:", fs.existsSync(langPath));
console.log("vie.traineddata exists:", fs.existsSync(path.join(langPath, 'vie.traineddata')));

const imageBuffer = fs.readFileSync(imagePath);

Tesseract.recognize(imageBuffer, 'vie', {
  langPath: langPath,
  logger: m => console.log(m)
}).then(({ data: { text } }) => {
  console.log("=== RESULT ===");
  console.log(text);
  process.exit(0);
}).catch(err => {
  console.error(err);
  process.exit(1);
});
