package com.cafe.jenika.repository;

import com.cafe.jenika.model.NhatKy;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class NhatKySpecification {

    public static Specification<NhatKy> filterLogs(
            String search,
            String thaoTac,
            String tab) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate moTaLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("moTa")), searchLower);
                Predicate maBanGhiLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("maBanGhi")), searchLower);
                Predicate thaoTacLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("thaoTac")), searchLower);
                
                Predicate idLike = null;
                String cleanedSearch = search.trim().toLowerCase();
                if (cleanedSearch.startsWith("lg-")) {
                    cleanedSearch = cleanedSearch.substring(3);
                }
                try {
                    Integer id = Integer.parseInt(cleanedSearch);
                    idLike = criteriaBuilder.equal(root.get("id"), id);
                } catch (NumberFormatException e) {
                    // Skip id matching
                }

                if (idLike != null) {
                    predicates.add(criteriaBuilder.or(moTaLike, maBanGhiLike, thaoTacLike, idLike));
                } else {
                    predicates.add(criteriaBuilder.or(moTaLike, maBanGhiLike, thaoTacLike));
                }
            }

            if (thaoTac != null && !thaoTac.trim().isEmpty() && !thaoTac.equalsIgnoreCase("Tất cả")) {
                predicates.add(criteriaBuilder.equal(root.get("thaoTac"), thaoTac));
            }

            if (tab != null && !tab.trim().isEmpty() && !tab.equalsIgnoreCase("Tất cả phân hệ")) {
                predicates.add(criteriaBuilder.equal(root.get("tab"), tab));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
