package com.cafe.jenika.service;

import com.cafe.jenika.model.DonViTinh;
import com.cafe.jenika.repository.DonViTinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DonViTinhService {
    @Autowired
    private DonViTinhRepository donViTinhRepository;
    
    public List<DonViTinh> getAll() {
        return donViTinhRepository.findAll();
    }
}
