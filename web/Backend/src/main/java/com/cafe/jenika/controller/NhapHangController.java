package com.cafe.jenika.controller;

import com.cafe.jenika.dto.NhapHangDTO;
import com.cafe.jenika.service.NhapHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/nhap-hang")
public class NhapHangController {

    @Autowired
    private NhapHangService nhapHangService;

    @GetMapping
    public ResponseEntity<List<NhapHangDTO>> getAllImports() {
        return ResponseEntity.ok(nhapHangService.getAllImportOrders());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<NhapHangDTO>> getImportsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            from = java.time.LocalDate.parse(fromDate.trim()).atStartOfDay();
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            to = java.time.LocalDate.parse(toDate.trim()).atTime(23, 59, 59);
        }
        return ResponseEntity.ok(nhapHangService.getImportOrdersPaginated(page, size, search, status, from, to));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Integer id) {
        try {
            nhapHangService.deletePurchaseOrder(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                from = java.time.LocalDate.parse(fromDate.trim()).atStartOfDay();
            }
            if (toDate != null && !toDate.trim().isEmpty()) {
                to = java.time.LocalDate.parse(toDate.trim()).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Định dạng ngày không hợp lệ. Sử dụng YYYY-MM-DD.");
        }
        return ResponseEntity.ok(nhapHangService.getImportOrderStats(from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhapHangDTO> getImportById(@PathVariable Integer id) {
        return nhapHangService.getImportOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createImport(@RequestBody NhapHangDTO order) {
        try {
            return ResponseEntity.ok(nhapHangService.createImportOrder(order));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            return ResponseEntity.ok(nhapHangService.updateImportOrderStatus(id, status));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateImport(@PathVariable Integer id, @RequestBody NhapHangDTO order) {
        try {
            return ResponseEntity.ok(nhapHangService.updateImportOrder(id, order));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportOrderToExcel(@PathVariable Integer id) {
        try {
            byte[] excelBytes = nhapHangService.exportPurchaseOrderToExcel(id);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                    .filename("PN-" + id + ".xlsx")
                    .build());
            return new ResponseEntity<>(excelBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
