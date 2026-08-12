package com.cafe.jenika.service;

import com.cafe.jenika.model.NhatKy;
import com.cafe.jenika.repository.NhatKyRepository;
import com.cafe.jenika.repository.NhatKySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NhatKyService {

    @Autowired
    private NhatKyRepository nhatKyRepository;

    public List<NhatKy> getAllLogs() {
        return nhatKyRepository.findAllByOrderByThoiGianDesc();
    }

    public Page<NhatKy> getLogsPaginated(
            int page,
            int size,
            String search,
            String thaoTac,
            String tab) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "thoiGian"));
        Specification<NhatKy> spec = NhatKySpecification.filterLogs(search, thaoTac, tab);
        return nhatKyRepository.findAll(spec, pageable);
    }

    @Transactional
    public void log(String thaoTac, String tab, String maBanGhi, String moTa) {
        NhatKy log = NhatKy.builder()
                .thoiGian(LocalDateTime.now())
                .thaoTac(thaoTac)
                .tab(tab)
                .maBanGhi(maBanGhi)
                .moTa(moTa)
                .build();
        nhatKyRepository.save(log);
    }
}
