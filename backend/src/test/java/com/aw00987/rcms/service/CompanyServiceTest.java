package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.CompanyDictionaryQueryResponseDto;
import com.aw00987.rcms.dto.CompanyQueryResponseDto;
import com.aw00987.rcms.dto.CompanySaveRequestDto;
import com.aw00987.rcms.entity.Company;
import com.aw00987.rcms.repository.CompanyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void addNewCompanyMapsRequestToCompanyAndInsertsWithoutId() {
        CompanySaveRequestDto request = companyRequest(9L);

        companyService.addNewCompany(request);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyMapper).insert(companyCaptor.capture());
        Company inserted = companyCaptor.getValue();
        assertThat(inserted.getId()).isNull();
        assertCompanyFields(inserted);
    }

    @Test
    void updateCompanyMapsRequestToCompanyAndUpdatesWithId() {
        CompanySaveRequestDto request = companyRequest(9L);

        companyService.updateCompany(request);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyMapper).update(companyCaptor.capture());
        Company updated = companyCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(9L);
        assertCompanyFields(updated);
    }

    @Test
    void deleteByIdDelegatesToMapper() {
        companyService.deleteById(15L);

        verify(companyMapper).deleteById(15L);
    }

    @Test
    void pageCompaniesReturnsPageFromMapperContentAndCount() {
        Pageable pageable = PageRequest.of(1, 4);
        CompanyQueryResponseDto first = companyResponse(1L, "C001");
        CompanyQueryResponseDto second = companyResponse(2L, "C002");
        when(companyMapper.selectPage(4L, 4)).thenReturn(List.of(first, second));
        when(companyMapper.selectCount()).thenReturn(6L);

        Page<CompanyQueryResponseDto> result = companyService.pageCompanies(pageable);

        assertThat(result.getContent()).containsExactly(first, second);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(4);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
        verify(companyMapper).selectPage(4L, 4);
        verify(companyMapper).selectCount();
    }

    @Test
    void pageCompaniesSupportsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(companyMapper.selectPage(0L, 20)).thenReturn(List.of());
        when(companyMapper.selectCount()).thenReturn(0L);

        Page<CompanyQueryResponseDto> result = companyService.pageCompanies(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        verify(companyMapper).selectPage(0L, 20);
        verify(companyMapper).selectCount();
    }

    @Test
    void getCompaniesDictionaryDelegatesFilterAndReturnsMapperResult() {
        CompanyDictionaryQueryResponseDto first = dictionaryResponse(1L, "C001", "Alpha Trading");
        CompanyDictionaryQueryResponseDto second = dictionaryResponse(2L, "C002", "Alpha Foods");
        when(companyMapper.selectCompanySimpleDictionary("Alpha")).thenReturn(List.of(first, second));

        List<CompanyDictionaryQueryResponseDto> result = companyService.getCompaniesDictionary("Alpha");

        assertThat(result).containsExactly(first, second);
        verify(companyMapper).selectCompanySimpleDictionary("Alpha");
    }

    @Test
    void getCompaniesDictionaryPassesNullFilterThroughToMapper() {
        when(companyMapper.selectCompanySimpleDictionary(null)).thenReturn(List.of());

        List<CompanyDictionaryQueryResponseDto> result = companyService.getCompaniesDictionary(null);

        assertThat(result).isEmpty();
        verify(companyMapper).selectCompanySimpleDictionary(null);
    }

    private static CompanySaveRequestDto companyRequest(Long id) {
        CompanySaveRequestDto dto = new CompanySaveRequestDto();
        dto.setId(id);
        dto.setCompanyCode("C001");
        dto.setCompanyName("Alpha Trading");
        dto.setEmail("billing@example.com");
        dto.setFaxNum("03-1111-2222");
        dto.setPhoneNum("03-3333-4444");
        dto.setAddress("Tokyo");
        dto.setCreditRating("A");
        dto.setCreditLimit(new BigDecimal("1000000"));
        dto.setPicUserId("user-7");
        return dto;
    }

    private static void assertCompanyFields(Company company) {
        assertThat(company.getCompanyCode()).isEqualTo("C001");
        assertThat(company.getCompanyName()).isEqualTo("Alpha Trading");
        assertThat(company.getEmail()).isEqualTo("billing@example.com");
        assertThat(company.getFaxNum()).isEqualTo("03-1111-2222");
        assertThat(company.getPhoneNum()).isEqualTo("03-3333-4444");
        assertThat(company.getAddress()).isEqualTo("Tokyo");
        assertThat(company.getCreditRating()).isEqualTo("A");
        assertThat(company.getCreditLimit()).isEqualByComparingTo("1000000");
        assertThat(company.getPicUserId()).isEqualTo("user-7");
    }

    private static CompanyQueryResponseDto companyResponse(Long id, String companyCode) {
        CompanyQueryResponseDto dto = new CompanyQueryResponseDto();
        dto.setId(id);
        dto.setCompanyCode(companyCode);
        dto.setCompanyName("Company " + id);
        return dto;
    }

    private static CompanyDictionaryQueryResponseDto dictionaryResponse(Long id, String code, String name) {
        CompanyDictionaryQueryResponseDto dto = new CompanyDictionaryQueryResponseDto();
        dto.setId(id);
        dto.setCompanyCode(code);
        dto.setCompanyName(name);
        return dto;
    }
}
