package com.cafe.jenika.controller;

import com.cafe.jenika.service.BaoCaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bao-cao")
public class BaoCaoController {

    @Autowired
    private BaoCaoService baoCaoService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(baoCaoService.getDashboardData());
    }

    @GetMapping("/san-pham-ban")
    public ResponseEntity<?> getProductSales(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isGift,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            return ResponseEntity.ok(baoCaoService.getProductSales(search, isGift, fromDate, toDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/san-pham-ban/stats")
    public ResponseEntity<?> getProductSalesStats(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            return ResponseEntity.ok(baoCaoService.getProductSalesStats(fromDate, toDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
