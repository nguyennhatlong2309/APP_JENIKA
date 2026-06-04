package com.cafe.jenika.repository;

import com.cafe.jenika.model.DoiTac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoiTacRepository extends JpaRepository<DoiTac, Integer> {
}
