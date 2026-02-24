package com.aw00987.rcms.controller;

import com.aw00987.rcms.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reconciliations")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/auto")
    public ResponseEntity<Map<String, Object>> autoReconcile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(reconciliationService.autoReconcile(file));
    }
}
