package com.cafe.jenika.controller;

import com.cafe.jenika.dto.ThuChiDTO;
import com.cafe.jenika.service.ThuChiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/thu-chi")
public class ThuChiController {

    @Autowired
    private ThuChiService thuChiService;

    @GetMapping
    public ResponseEntity<List<ThuChiDTO>> getAllTransactions() {
        return ResponseEntity.ok(thuChiService.getAllTransactions());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ThuChiDTO>> getTransactionsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String transactionType) {
        java.time.LocalDateTime from = null;
        java.time.LocalDateTime to = null;
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            from = java.time.LocalDate.parse(fromDate.trim()).atStartOfDay();
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            to = java.time.LocalDate.parse(toDate.trim()).atTime(23, 59, 59);
        }
        return ResponseEntity.ok(thuChiService.getTransactionsPaginated(page, size, search, categoryId, status, from, to, transactionType));
    }

    @PostMapping
    public ResponseEntity<ThuChiDTO> createTransaction(@RequestBody ThuChiDTO dto) {
        try {
            return ResponseEntity.ok(thuChiService.saveTransaction(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThuChiDTO> updateTransaction(@PathVariable Integer id, @RequestBody ThuChiDTO dto) {
        try {
            return ResponseEntity.ok(thuChiService.updateTransaction(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Integer id) {
        try {
            thuChiService.deleteTransaction(id);
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
        return ResponseEntity.ok(thuChiService.getTransactionStats(from, to));
    }
}
