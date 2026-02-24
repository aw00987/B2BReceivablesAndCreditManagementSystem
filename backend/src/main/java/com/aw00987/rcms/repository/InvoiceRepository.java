package com.aw00987.rcms.repository;

import com.aw00987.rcms.entity.Invoice;
import com.aw00987.rcms.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    @Query("SELECT i.invoiceNo FROM Invoice i WHERE i.status = :status AND i.interestStartDate IS NOT NULL AND i.interestStartDate <= :date")
    List<String> findInvoiceNosByStatusAndInterestStartDateBefore(@Param("status") InvoiceStatus status, @Param("date") LocalDate date);

    @Query("SELECT i.invoiceNo FROM Invoice i WHERE i.status = :status AND i.dueDate < :date")
    List<String> findInvoiceNosByStatusAndDueDateBefore(@Param("status") InvoiceStatus status, @Param("date") LocalDate date);
}
