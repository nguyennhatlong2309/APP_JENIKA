package com.cafe.jenika.service;

import com.cafe.jenika.model.DanhMuc;
import com.cafe.jenika.repository.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DanhMucService {
    @Autowired
    private DanhMucRepository danhMucRepository;
    
    public List<DanhMuc> getAll() {
        return danhMucRepository.findAll();
    }

    @Transactional
    public DanhMuc update(Integer id, DanhMuc updated) {
        return danhMucRepository.findById(id)
                .map(existing -> {
                    existing.setTenDanhMuc(updated.getTenDanhMuc());
                    if (updated.getMoTa() != null) {
                        existing.setMoTa(updated.getMoTa());
                    }
                    return danhMucRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với id " + id));
    }
}

