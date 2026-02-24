package com.hms.backend.controller;

import com.hms.backend.dto.LoginLogDto;
import com.hms.backend.model.LoginLog;
import com.hms.backend.repository.LoginLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/logs")
public class LoginLogController {

    private final LoginLogRepository loginLogRepository;

    public LoginLogController(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<LoginLogDto>> getAllLogs() {
        List<LoginLog> logs = loginLogRepository.findAll(Sort.by(Sort.Direction.DESC, "loginTime"));
        return ResponseEntity.ok(logs.stream().map(LoginLogDto::new).collect(Collectors.toList()));
    }
}
