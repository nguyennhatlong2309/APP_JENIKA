package com.cafe.jenika.repository;

import com.cafe.jenika.model.NhanVien;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class NhanVienSpecification {

    public static Specification<NhanVien> filterEmployees(String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate tenLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("tenNhanVien")), searchLower);
                Predicate sdtLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("sdt")), searchLower);
                Predicate emailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchLower);
                Predicate vaiTroLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("vaiTro")), searchLower);
                
                predicates.add(criteriaBuilder.or(tenLike, sdtLike, emailLike, vaiTroLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
