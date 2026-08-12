package com.cafe.jenika.repository;

import com.cafe.jenika.model.SanPham;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SanPhamSpecification {

    public static Specification<SanPham> filterProducts(
            Boolean biXoa,
            String search,
            List<Integer> categoryIds,
            List<Integer> groupIds,
            List<String> statuses) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (biXoa != null) {
                predicates.add(criteriaBuilder.equal(root.get("biXoa"), biXoa));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("tenSanPham")), searchLower);
                
                Predicate idLike = null;
                String cleanedSearch = search.trim().toLowerCase();
                if (cleanedSearch.startsWith("sp-")) {
                    cleanedSearch = cleanedSearch.substring(3);
                }
                try {
                    Integer id = Integer.parseInt(cleanedSearch);
                    idLike = criteriaBuilder.equal(root.get("id"), id);
                } catch (NumberFormatException e) {
                    // Not an integer id, skip id matching
                }
                
                if (idLike != null) {
                    predicates.add(criteriaBuilder.or(nameLike, idLike));
                } else {
                    predicates.add(nameLike);
                }
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("danhMuc").get("id").in(categoryIds));
            }

            if (groupIds != null && !groupIds.isEmpty()) {
                predicates.add(root.get("nhomSanPham").get("id").in(groupIds));
            }

            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("trangThai").in(statuses));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
