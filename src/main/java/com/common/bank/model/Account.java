package com.common.bank.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account", 
       indexes = {
           @Index(name = "idx_account_ref_id", columnList = "ref_id")
       })
@Data
@NoArgsConstructor
public class Account extends BaseEntity {

    @Column(name = "ref_id", nullable = false, unique = true, length = 50)
    private String refId;

    @Column(nullable = false)
    private Double balance;
}
