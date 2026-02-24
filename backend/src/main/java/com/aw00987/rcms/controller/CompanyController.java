package com.aw00987.rcms.controller;

import com.aw00987.rcms.dto.CompanyRequestDto;
import com.aw00987.rcms.dto.CompanyResponseDto;
import com.aw00987.rcms.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponseDto> createCompany(@RequestBody CompanyRequestDto companyRequestDto) {
        return ResponseEntity.ok(companyService.createCompany(companyRequestDto));
    }

    @GetMapping
    public ResponseEntity<Page<CompanyResponseDto>> getCompanies(Pageable pageable) {
        return ResponseEntity.ok(companyService.getCompanies(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CompanyResponseDto>> searchCompanies(@RequestParam String userInput) {
        return ResponseEntity.ok(companyService.searchCompanies(userInput));
    }
}