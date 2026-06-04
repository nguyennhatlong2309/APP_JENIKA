package com.cafe.jenika.service;

import com.cafe.jenika.model.LoaiThuChi;
import com.cafe.jenika.repository.LoaiThuChiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LoaiThuChiService {
    @Autowired
    private LoaiThuChiRepository loaiThuChiRepository;
    
    public List<LoaiThuChi> getAll() {
        return loaiThuChiRepository.findAll();
    }
}
