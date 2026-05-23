package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.CompanyDictionaryQueryResponseDto;
import com.aw00987.rcms.dto.CompanySaveRequestDto;
import com.aw00987.rcms.dto.CompanyQueryResponseDto;
import com.aw00987.rcms.entity.Company;
import com.aw00987.rcms.repository.CompanyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * todo: 假删除？
 * todo：登陆用的验证用户？
 * todo：修改密码？
 */
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyMapper companyMapper;

    @Transactional
    public void addNewCompany(CompanySaveRequestDto companySaveRequestDto) {
        Company company = new Company();
        company.setCompanyCode(companySaveRequestDto.getCompanyCode());
        company.setCompanyName(companySaveRequestDto.getCompanyName());
        company.setEmail(companySaveRequestDto.getEmail());
        company.setFaxNum(companySaveRequestDto.getFaxNum());
        company.setPhoneNum(companySaveRequestDto.getPhoneNum());
        company.setAddress(companySaveRequestDto.getAddress());
        company.setCreditRating(companySaveRequestDto.getCreditRating());
        company.setCreditLimit(companySaveRequestDto.getCreditLimit());
        company.setPicUserId(companySaveRequestDto.getPicUserId());
        companyMapper.insert(company);
    }

    @Transactional
    public void updateCompany(CompanySaveRequestDto companySaveRequestDto) {
        Company company = new Company();
        company.setId(companySaveRequestDto.getId());
        company.setCompanyCode(companySaveRequestDto.getCompanyCode());
        company.setCompanyName(companySaveRequestDto.getCompanyName());
        company.setEmail(companySaveRequestDto.getEmail());
        company.setFaxNum(companySaveRequestDto.getFaxNum());
        company.setPhoneNum(companySaveRequestDto.getPhoneNum());
        company.setAddress(companySaveRequestDto.getAddress());
        company.setCreditRating(companySaveRequestDto.getCreditRating());
        company.setCreditLimit(companySaveRequestDto.getCreditLimit());
        company.setPicUserId(companySaveRequestDto.getPicUserId());
        companyMapper.update(company);
    }

    @Transactional
    public void deleteById(Long id) {
        companyMapper.deleteById(id);
    }

    public Page<CompanyQueryResponseDto> pageCompanies(Pageable pageable) {
        List<CompanyQueryResponseDto> list = companyMapper.selectPage(
                pageable.getOffset(), pageable.getPageSize()
        );
        long count = companyMapper.selectCount();
        return new PageImpl<>(list, pageable, count);
    }


    public List<CompanyDictionaryQueryResponseDto> getCompaniesDictionary(String companyName) {
        return companyMapper.selectCompanySimpleDictionary(companyName);
    }
}
