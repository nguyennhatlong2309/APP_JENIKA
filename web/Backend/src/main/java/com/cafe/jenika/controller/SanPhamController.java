package com.cafe.jenika.controller;

import com.cafe.jenika.dto.SanPhamDTO;
import com.cafe.jenika.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<List<SanPhamDTO>> getAllProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) String search) {
        
        if (categoryId != null) {
            return ResponseEntity.ok(sanPhamService.getProductsByCategory(categoryId));
        }
        if (groupId != null) {
            return ResponseEntity.ok(sanPhamService.getProductsByGroup(groupId));
        }
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(sanPhamService.searchProducts(search));
        }
        return ResponseEntity.ok(sanPhamService.getAllActiveProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPhamDTO> getProductById(@PathVariable Integer id) {
        return sanPhamService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<SanPhamDTO>> getLowStockWarnings() {
        return ResponseEntity.ok(sanPhamService.getLowStockWarnings());
    }

    @PostMapping
    public ResponseEntity<SanPhamDTO> createProduct(@RequestBody SanPhamDTO sanPhamDTO) {
        return ResponseEntity.ok(sanPhamService.saveProduct(sanPhamDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPhamDTO> updateProduct(@PathVariable Integer id, @RequestBody SanPhamDTO sanPhamDTO) {
        return sanPhamService.getProductById(id)
                .map(existing -> {
                    sanPhamDTO.setId(id);
                    return ResponseEntity.ok(sanPhamService.saveProduct(sanPhamDTO));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        return sanPhamService.getProductById(id)
                .map(existing -> {
                    sanPhamService.softDeleteProduct(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<SanPhamDTO>> getAllProductsIncludingDeleted() {
        return ResponseEntity.ok(sanPhamService.getAllProducts());
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreProduct(@PathVariable Integer id) {
        sanPhamService.restoreProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/page")
    public ResponseEntity<Page<SanPhamDTO>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean biXoa,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> categoryIds,
            @RequestParam(required = false) List<Integer> groupIds,
            @RequestParam(required = false) List<String> statuses) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(sanPhamService.getProductsPaginated(biXoa, search, categoryIds, groupIds, statuses, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        return ResponseEntity.ok(sanPhamService.getProductStats());
    }
}
