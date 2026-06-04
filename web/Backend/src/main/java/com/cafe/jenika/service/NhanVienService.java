package com.cafe.jenika.service;

import com.cafe.jenika.model.NhanVien;
import com.cafe.jenika.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NhanVienService {
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    public List<NhanVien> getAll() {
        return nhanVienRepository.findAll();
    }

    public NhanVien save(NhanVien nv) {
        return nhanVienRepository.save(nv);
    }
}
