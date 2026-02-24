package com.hms.backend.repository;

import com.hms.backend.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    List<LoginLog> findByUserIdOrderByLoginTimeDesc(Long userId);

    List<LoginLog> findTop1ByUserIdOrderByLoginTimeDesc(Long userId);
}
