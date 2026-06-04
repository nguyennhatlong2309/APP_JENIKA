package com.cafe.jenika.controller;

import com.cafe.jenika.dto.ThuChiDTO;
import com.cafe.jenika.service.ThuChiService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping
    public ResponseEntity<ThuChiDTO> createTransaction(@RequestBody ThuChiDTO dto) {
        try {
            return ResponseEntity.ok(thuChiService.saveTransaction(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
