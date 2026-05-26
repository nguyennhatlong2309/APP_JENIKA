$path = "c:\Long\hk2_2025-2026\PersonalProject\APP_CAFE\swing-app\src\main\java\com\brewmaster\panels\PurchasesPanel.java"
$bytes = [System.IO.File]::ReadAllBytes($path)
if ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    $bytes = $bytes[3..($bytes.Length - 1)]
    [System.IO.File]::WriteAllBytes($path, $bytes)
    Write-Host "BOM removed successfully"
} else {
    Write-Host "No BOM found"
}
