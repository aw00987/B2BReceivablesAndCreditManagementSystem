package com.aw00987.rcms.repository;

import com.aw00987.rcms.entity.Reconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationRepository extends JpaRepository<Reconciliation, Long> {

}
