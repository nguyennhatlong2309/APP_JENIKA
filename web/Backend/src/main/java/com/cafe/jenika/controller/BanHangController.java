package com.cafe.jenika.controller;

import com.cafe.jenika.dto.BanHangDTO;
import com.cafe.jenika.service.BanHangService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
