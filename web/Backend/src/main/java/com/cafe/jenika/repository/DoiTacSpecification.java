package com.cafe.jenika.repository;

import com.cafe.jenika.model.DoiTac;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class DoiTacSpecification {

    public static Specification<DoiTac> filterPartners(String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate tenLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("ten")), searchLower);
                Predicate sdtLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("sdt")), searchLower);
                Predicate emailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchLower);
                Predicate diaChiLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("diaChi")), searchLower);
                
                predicates.add(criteriaBuilder.or(tenLike, sdtLike, emailLike, diaChiLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
