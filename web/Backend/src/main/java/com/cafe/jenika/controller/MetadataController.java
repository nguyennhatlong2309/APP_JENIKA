package com.cafe.jenika.controller;

import com.cafe.jenika.model.*;
import com.cafe.jenika.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metadata")
public class MetadataController {

    @Autowired
    private DanhMucService danhMucService;

    @Autowired
    private NhomSanPhamService nhomSanPhamService;

    @Autowired
    private DonViTinhService donViTinhService;

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private DoiTacService doiTacService;

    @Autowired
    private LoaiThuChiService loaiThuChiService;

    @Autowired
    private NhatKyService nhatKyService;

    @GetMapping("/danh-muc")
    public ResponseEntity<List<DanhMuc>> getCategories() {
        return ResponseEntity.ok(danhMucService.getAll());
    }

    @PutMapping("/danh-muc/{id}")
    public ResponseEntity<DanhMuc> updateCategory(@PathVariable Integer id, @RequestBody DanhMuc danhMuc) {
        return ResponseEntity.ok(danhMucService.update(id, danhMuc));
    }

    @GetMapping("/nhom-san-pham")
    public ResponseEntity<List<NhomSanPham>> getGroups() {
        return ResponseEntity.ok(nhomSanPhamService.getAll());
    }

    @PutMapping("/nhom-san-pham/{id}")
    public ResponseEntity<NhomSanPham> updateGroup(@PathVariable Integer id, @RequestBody NhomSanPham nhomSanPham) {
        return ResponseEntity.ok(nhomSanPhamService.update(id, nhomSanPham));
    }

    @PostMapping("/nhom-san-pham")
    public ResponseEntity<NhomSanPham> createGroup(@RequestBody NhomSanPham nhomSanPham) {
        return ResponseEntity.ok(nhomSanPhamService.save(nhomSanPham));
    }

    @GetMapping("/don-vi")
    public ResponseEntity<List<DonViTinh>> getUnits() {
        return ResponseEntity.ok(donViTinhService.getAll());
    }

    @GetMapping("/nhan-vien")
    public ResponseEntity<List<NhanVien>> getEmployees() {
        return ResponseEntity.ok(nhanVienService.getAll());
    }

    @PostMapping("/nhan-vien")
    public ResponseEntity<NhanVien> createEmployee(@RequestBody NhanVien nhanVien) {
        return ResponseEntity.ok(nhanVienService.save(nhanVien));
    }

    @PutMapping("/nhan-vien/{id}")
    public ResponseEntity<NhanVien> updateEmployee(@PathVariable Integer id, @RequestBody NhanVien nhanVien) {
        nhanVien.setId(id);
        return ResponseEntity.ok(nhanVienService.save(nhanVien));
    }

    @DeleteMapping("/nhan-vien/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        nhanVienService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nhan-vien/page")
    public ResponseEntity<org.springframework.data.domain.Page<NhanVien>> getEmployeesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(nhanVienService.getEmployeesPaginated(page, size, search));
    }

    @GetMapping("/doi-tac")
    public ResponseEntity<List<DoiTac>> getPartners() {
        return ResponseEntity.ok(doiTacService.getAll());
    }

    @GetMapping("/doi-tac/page")
    public ResponseEntity<org.springframework.data.domain.Page<DoiTac>> getPartnersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(doiTacService.getPartnersPaginated(page, size, search));
    }

    @PostMapping("/doi-tac")
    public ResponseEntity<DoiTac> createPartner(@RequestBody DoiTac doiTac) {
        return ResponseEntity.ok(doiTacService.save(doiTac));
    }

    @PutMapping("/doi-tac/{id}")
    public ResponseEntity<DoiTac> updatePartner(@PathVariable Integer id, @RequestBody DoiTac doiTac) {
        doiTac.setId(id);
        return ResponseEntity.ok(doiTacService.save(doiTac));
    }

    @DeleteMapping("/doi-tac/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable Integer id) {
        doiTacService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/loai-thu-chi")
    public ResponseEntity<List<LoaiThuChi>> getExpenseTypes() {
        return ResponseEntity.ok(loaiThuChiService.getAll());
    }

    @GetMapping("/nhat-ky")
    public ResponseEntity<List<NhatKy>> getLogs() {
        return ResponseEntity.ok(nhatKyService.getAllLogs());
    }

    @GetMapping("/nhat-ky/page")
    public ResponseEntity<org.springframework.data.domain.Page<NhatKy>> getLogsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String thaoTac,
            @RequestParam(required = false) String tab) {
        return ResponseEntity.ok(nhatKyService.getLogsPaginated(page, size, search, thaoTac, tab));
    }
}
