package com.aw00987.rcms.repository;

import com.aw00987.rcms.entity.Reconciliation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ReconciliationMapper {

    void save(Reconciliation reconciliation);

    @Select("SELECT * FROM reconciliations WHERE id = #{id}")
    Optional<Reconciliation> findById(Long id);

    @Select("SELECT * FROM reconciliations")
    List<Reconciliation> findAll();
}
