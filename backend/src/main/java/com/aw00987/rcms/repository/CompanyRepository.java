package com.aw00987.rcms.repository;

import com.aw00987.rcms.entity.Company;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyCode(String companyCode);

    List<Company> findByCompanyNameContaining(String companyName, Pageable pageable);
}