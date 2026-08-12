package com.cafe.jenika.service;

import com.cafe.jenika.model.NhanVien;
import com.cafe.jenika.repository.NhanVienRepository;
import com.cafe.jenika.repository.NhanVienSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NhanVienService {
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    public List<NhanVien> getAll() {
        return nhanVienRepository.findAll();
    }
    
    public Page<NhanVien> getEmployeesPaginated(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "tenNhanVien"));
        Specification<NhanVien> spec = NhanVienSpecification.filterEmployees(search);
        return nhanVienRepository.findAll(spec, pageable);
    }

    public NhanVien save(NhanVien nv) {
        return nhanVienRepository.save(nv);
    }

    public void delete(Integer id) {
        nhanVienRepository.deleteById(id);
    }
}
