package com.cafe.jenika.service;

import com.cafe.jenika.model.DoiTac;
import com.cafe.jenika.repository.DoiTacRepository;
import com.cafe.jenika.repository.DoiTacSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoiTacService {
    @Autowired
    private DoiTacRepository doiTacRepository;
    
    public List<DoiTac> getAll() {
        return doiTacRepository.findAll();
    }
    
    public Page<DoiTac> getPartnersPaginated(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "ten"));
        Specification<DoiTac> spec = DoiTacSpecification.filterPartners(search);
        return doiTacRepository.findAll(spec, pageable);
    }
    
    public DoiTac save(DoiTac doiTac) {
        return doiTacRepository.save(doiTac);
    }

    public void delete(Integer id) {
        doiTacRepository.deleteById(id);
    }
}
