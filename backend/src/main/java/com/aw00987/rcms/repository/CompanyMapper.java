package com.aw00987.rcms.repository;

import com.aw00987.rcms.dto.CompanyDictionaryQueryResponseDto;
import com.aw00987.rcms.dto.CompanyQueryResponseDto;
import com.aw00987.rcms.entity.Company;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CompanyMapper {

    void insert(Company company);

    void update(Company company);

    @Delete("delete from companies where id = #{id}")
    void deleteById(@Param("id") Long id);

    @Select("select * from companies where id = #{id}")
    Optional<Company> findById(@Param("id") Long id);

    @Select("select companies.*,users.real_name as pic_user_real_name " +
            "from companies left join users on companies.pic_user_id =  users.id " +
            "order by companies.id limit #{pageSize} offset #{offset}")
    List<CompanyQueryResponseDto> selectPage(@Param("offset") long offset, @Param("pageSize") int pageSize);

    @Select("select count(id) from companies")
    long selectCount();

    @Select("select id, company_code, company_name from companies " +
            "where company_name like concat('%', #{companyName}, '%') " +
            "order by company_name limit 10")
    List<CompanyDictionaryQueryResponseDto> selectCompanySimpleDictionary(@Param("companyName") String companyName);
}
