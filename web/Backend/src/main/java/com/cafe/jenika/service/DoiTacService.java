package com.cafe.jenika.service;

import com.cafe.jenika.model.DoiTac;
import com.cafe.jenika.repository.DoiTacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoiTacService {
    @Autowired
    private DoiTacRepository doiTacRepository;
    
    public List<DoiTac> getAll() {
        return doiTacRepository.findAll();
    }
    
    public DoiTac save(DoiTac doiTac) {
        return doiTacRepository.save(doiTac);
    }
}
