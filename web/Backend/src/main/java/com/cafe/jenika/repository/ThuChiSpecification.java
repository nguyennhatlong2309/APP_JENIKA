package com.cafe.jenika.repository;

import com.cafe.jenika.model.ThuChi;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThuChiSpecification {

    public static Specification<ThuChi> filterTransactions(
            String search,
            Integer categoryId,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        return filterTransactions(search, categoryId, status, fromDate, toDate, null);
    }

    public static Specification<ThuChi> filterTransactions(
            String search,
            Integer categoryId,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String transactionType) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate moTaLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("moTa")), searchLower);
                Predicate nvLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("nhanVien").get("tenNhanVien")), searchLower);
                
                Predicate idLike = null;
                String cleanedSearch = search.trim().toLowerCase();
                if (cleanedSearch.startsWith("tc-")) {
                    cleanedSearch = cleanedSearch.substring(3);
                }
                try {
                    Integer id = Integer.parseInt(cleanedSearch);
                    idLike = criteriaBuilder.equal(root.get("id"), id);
                } catch (NumberFormatException e) {
                    // Skip id matching
                }

                if (idLike != null) {
                    predicates.add(criteriaBuilder.or(moTaLike, nvLike, idLike));
                } else {
                    predicates.add(criteriaBuilder.or(moTaLike, nvLike));
                }
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("loaiThuChi").get("id"), categoryId));
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

            // Filter by transaction type: THU (income) or CHI (expense)
            if (transactionType != null && !transactionType.trim().isEmpty()) {
                if ("THU".equalsIgnoreCase(transactionType.trim())) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("tienThu"), java.math.BigDecimal.ZERO));
                } else if ("CHI".equalsIgnoreCase(transactionType.trim())) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("tienChi"), java.math.BigDecimal.ZERO));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
