package com.cafe.jenika.controller;

import com.cafe.jenika.model.StoreConfig;
import com.cafe.jenika.service.StoreConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/store-config")
public class StoreConfigController {

    @Autowired
    private StoreConfigService storeConfigService;

    @GetMapping
    public ResponseEntity<StoreConfig> getConfig() {
        return ResponseEntity.ok(storeConfigService.getConfig());
    }

    @PutMapping
    public ResponseEntity<StoreConfig> updateConfig(@RequestBody StoreConfig config) {
        return ResponseEntity.ok(storeConfigService.saveConfig(config));
    }
}
