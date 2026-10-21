package com.meilong.messenger.repository;

import com.meilong.messenger.model.Target;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetRepository extends JpaRepository<Target, Long> {
    boolean existsByUrl(String url);
}