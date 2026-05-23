package com.aw00987.rcms.repository;

import com.aw00987.rcms.dto.InvoiceQueryResponseDto;
import com.aw00987.rcms.entity.Invoice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

//todo: 执行成功与否的检查如何做？
@Mapper
public interface InvoiceMapper {

    void insert(Invoice invoice);

    //todo: 询问ai,有啥更新啥是否符合最佳实践？
    void update(Invoice invoice);

    @Delete("delete from invoices where id = #{id}")
    void deleteById(@Param("id") Long id);

    //todo: 分页优化
    List<InvoiceQueryResponseDto> selectPage(
            @Param("invoiceNo") String invoiceNo,
            @Param("companyName") String companyName,
            @Param("invoiceStatus") String invoiceStatus,
            @Param("invoiceAmount") BigDecimal invoiceAmount,//todo: 兼容性
            @Param("invoiceDateFrom") LocalDate invoiceDateFrom,
            @Param("invoiceDateTo") LocalDate invoiceDateTo,
            @Param("createByUserRealName") String createByUserRealName,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    long selectCount(
            @Param("invoiceNo") String invoiceNo,
            @Param("companyName") String companyName,
            @Param("invoiceStatus") String invoiceStatus,
            @Param("invoiceAmount") BigDecimal invoiceAmount,//todo: 兼容性
            @Param("invoiceDateFrom") LocalDate invoiceDateFrom,
            @Param("invoiceDateTo") LocalDate invoiceDateTo,
            @Param("createByUserRealName") String createByUserRealName
    );

    Optional<InvoiceQueryResponseDto> selectById(@Param("id") long id);

    Optional<InvoiceQueryResponseDto> selectByInvoiceNo(@Param("invoiceNo") String invoiceNo);

    @Select("select id from invoices where status = 'NORMAL' and due_date < CURDATE()")
    List<Long> selectInvoiceIdsNeedsToOverdue();

    @Select("select id from invoices where status = 'DUNNING' and interest_start_date < CURDATE()")
    List<Long> selectInvoiceIdsNeedsToIncreaseInterest();

    //todo：学习这个sql的写法
    BigDecimal sumInvoiceAmountByStatuses(@Param("statuses") List<String> statuses);
}
