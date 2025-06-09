package com.openpayd.currency_management.repository;


import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyManagementRepository extends JpaRepository<CurrencyConverterEntity, Long> {

}
