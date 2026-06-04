package com.cafe.jenika.service;

import com.cafe.jenika.model.NhatKy;
import com.cafe.jenika.repository.NhatKyRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
