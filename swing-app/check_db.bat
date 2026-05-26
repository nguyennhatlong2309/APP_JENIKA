@echo off
echo === Kiem tra id_doi_tac trong ban_hang va nhap_hang ===
mysql -u root -proot cfe_di_rom -e "SHOW COLUMNS FROM ban_hang LIKE 'id_doi_tac';"
mysql -u root -proot cfe_di_rom -e "SHOW COLUMNS FROM nhap_hang LIKE 'id_doi_tac';"
mysql -u root -proot cfe_di_rom -e "SELECT COUNT(*) as bh_mapped FROM ban_hang WHERE id_doi_tac IS NOT NULL;"
echo.
echo === Xoa cot loai khoi doi_tac ===
mysql -u root -proot cfe_di_rom --default-character-set=utf8mb4 -e "ALTER TABLE doi_tac DROP COLUMN loai;"
echo Done drop loai.
echo.
echo === Cau truc doi_tac sau khi xoa ===
mysql -u root -proot cfe_di_rom -e "SHOW COLUMNS FROM doi_tac;"
mysql -u root -proot cfe_di_rom -e "SELECT id, ten FROM doi_tac ORDER BY id;"
