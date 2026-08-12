package com.cafe.jenika.controller;

import com.cafe.jenika.dto.BanHangDTO;
import com.cafe.jenika.service.BanHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ban-hang")
public class BanHangController {

    @Autowired
    private BanHangService banHangService;

    @GetMapping
    public ResponseEntity<List<BanHangDTO>> getAllOrders() {
        return ResponseEntity.ok(banHangService.getAllSalesOrders());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<BanHangDTO>> getOrdersPage(
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
        return ResponseEntity.ok(banHangService.getSalesOrdersPaginated(page, size, search, status, from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BanHangDTO> getOrderById(@PathVariable Integer id) {
        return banHangService.getSalesOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody BanHangDTO order) {
        try {
            return ResponseEntity.ok(banHangService.createSalesOrder(order));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            return ResponseEntity.ok(banHangService.updateOrderStatus(id, status));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                from = java.time.LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                to = java.time.LocalDate.parse(toDate).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Định dạng ngày không hợp lệ. Sử dụng YYYY-MM-DD.");
        }
        
        return ResponseEntity.ok(banHangService.getSalesStats(from, to));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Integer id, @RequestBody BanHangDTO order) {
        try {
            return ResponseEntity.ok(banHangService.updateSalesOrder(id, order));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportOrderToExcel(@PathVariable Integer id) {
        try {
            byte[] excelBytes = banHangService.exportSalesOrderToExcel(id);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                    .filename("HD-" + id + ".xlsx")
                    .build());
            return new ResponseEntity<>(excelBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
