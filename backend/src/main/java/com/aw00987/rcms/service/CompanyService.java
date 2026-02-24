package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.CompanyRequestDto;
import com.aw00987.rcms.dto.CompanyResponseDto;
import com.aw00987.rcms.entity.Company;
import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.repository.CompanyRepository;
import com.aw00987.rcms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 取引先企業管理に関連するビジネスロジックを提供するサービス。
 * 企業の登録、一覧取得、検索などの操作を担当します。
 */
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    /**
     * 新しい企業を登録します。
     * @param companyRequestDto 企業登録リクエスト情報
     * @return 登録された企業のレスポンスDTO
     * @throws RuntimeException 担当者ユーザーが見つからない場合
     */
    @Transactional
    public CompanyResponseDto createCompany(CompanyRequestDto companyRequestDto) {
        if (userRepository.findByUsername(companyRequestDto.getPicUsername()).isEmpty()) {
            throw new RuntimeException("担当者ユーザーが見つかりません");
        }

        Company company = new Company();
        company.setCompanyCode(companyRequestDto.getCompanyCode());
        company.setCompanyName(companyRequestDto.getCompanyName());
        company.setCreditRating(companyRequestDto.getCreditRating());
        company.setCreditLimit(companyRequestDto.getCreditLimit());
        company.setPicUsername(companyRequestDto.getPicUsername());
        company.setEmail(companyRequestDto.getEmail());
        company.setFaxNum(companyRequestDto.getFaxNum());
        company.setPhoneNum(companyRequestDto.getPhoneNum());
        company.setAddress(companyRequestDto.getAddress());

        Company savedCompany = companyRepository.save(company);
        return convertToResponseDto(savedCompany);
    }

    /**
     * 企業の一覧をページングして取得します。
     * @param pageable ページング情報
     * @return 企業レスポンスDTOのページ
     */
    public Page<CompanyResponseDto> getCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(this::convertToResponseDto);
    }

    /**
     * 企業エンティティをレスポンスDTOに変換します。
     * @param company 企業エンティティ
     * @return 企業レスポンスDTO
     */
    private CompanyResponseDto convertToResponseDto(Company company) {
        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setCompanyCode(company.getCompanyCode());
        dto.setCompanyName(company.getCompanyName());
        dto.setCreditRating(company.getCreditRating());
        dto.setCreditLimit(company.getCreditLimit());
        dto.setPicUsername(company.getPicUsername());
        dto.setEmail(company.getEmail());
        dto.setPhoneNum(company.getPhoneNum());
        dto.setFaxNum(company.getFaxNum());
        dto.setAddress(company.getAddress());
        // 担当者の実名を設定
        userRepository.findByUsername(company.getPicUsername())
                .map(User::getRealName)
                .ifPresent(dto::setPicRealName);
        return dto;
    }

    /**
     * 企業名で企業を検索します（部分一致）。
     * @param userInput 検索キーワード
     * @return 検索結果の企業レスポンスDTOリスト（最大10件）
     */
    public List<CompanyResponseDto> searchCompanies(String userInput) {
        return companyRepository.findByCompanyNameContaining(
                userInput, PageRequest.of(0, 10)
        ).stream().map(this::convertToResponseDto).collect(Collectors.toList());
    }
}
