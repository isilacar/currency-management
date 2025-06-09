package com.openpayd.currency_management.repository;


import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface CurrencyManagementRepository extends JpaRepository<CurrencyConverterEntity, Long> {

    Page<CurrencyConverterEntity> findByTransactionId(String transactionId, Pageable pageable);

    Page<CurrencyConverterEntity> findByTransactionDate(LocalDate date, Pageable pageable);

    Page<CurrencyConverterEntity> findByTransactionIdAndTransactionDate(
            String transactionId, LocalDate date, Pageable pageable
    );

}
