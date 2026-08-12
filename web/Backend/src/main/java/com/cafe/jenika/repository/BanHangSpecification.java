package com.cafe.jenika.repository;

import com.cafe.jenika.model.BanHang;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BanHangSpecification {

    public static Specification<BanHang> filterSales(
            String search,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate partnerNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("doiTac").get("ten")), searchLower);
                
                Predicate idLike = null;
                String cleanedSearch = search.trim().toLowerCase();
                if (cleanedSearch.startsWith("bh-")) {
                    cleanedSearch = cleanedSearch.substring(3);
                }
                try {
                    Integer id = Integer.parseInt(cleanedSearch);
                    idLike = criteriaBuilder.equal(root.get("id"), id);
                } catch (NumberFormatException e) {
                    // Skip id matching
                }

                if (idLike != null) {
                    predicates.add(criteriaBuilder.or(partnerNameLike, idLike));
                } else {
                    predicates.add(partnerNameLike);
                }
            }

            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All")) {
                predicates.add(criteriaBuilder.equal(root.get("trangThai"), status));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("thoiGian"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("thoiGian"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
