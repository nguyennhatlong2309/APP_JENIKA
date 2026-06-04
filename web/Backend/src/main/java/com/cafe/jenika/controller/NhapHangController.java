package com.cafe.jenika.controller;

import com.cafe.jenika.dto.NhapHangDTO;
import com.cafe.jenika.service.NhapHangService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
