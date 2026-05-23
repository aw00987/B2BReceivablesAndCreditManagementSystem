package com.aw00987.rcms.controller;

import com.aw00987.rcms.dto.CompanyDictionaryQueryResponseDto;
import com.aw00987.rcms.dto.CompanySaveRequestDto;
import com.aw00987.rcms.dto.CompanyQueryResponseDto;
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
    public ResponseEntity<String> createCompany(@RequestBody CompanySaveRequestDto companySaveRequestDto) {
        companyService.addNewCompany(companySaveRequestDto);
        return ResponseEntity.ok("");
    }

    @GetMapping
    public ResponseEntity<Page<CompanyQueryResponseDto>> getCompanies(Pageable pageable) {
        return ResponseEntity.ok(companyService.pageCompanies(pageable));
    }

    @GetMapping("/dic")
    public ResponseEntity<List<CompanyDictionaryQueryResponseDto>> getCompanies(
            @RequestParam String companyName
    ) {
        return ResponseEntity.ok(companyService.getCompaniesDictionary(companyName));
    }
}