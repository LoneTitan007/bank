package com.common.bank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction", 
       indexes = {
           @Index(name = "idx_transaction_ref_id", columnList = "ref_id"),
           @Index(name = "idx_source_account_ref_id", columnList = "source_account_ref_id"),
           @Index(name = "idx_destination_account_ref_id", columnList = "destination_account_ref_id")
       })
@Data
@NoArgsConstructor
public class Transaction extends BaseEntity {

    @Column(name = "ref_id", nullable = false, unique = true, length = 50)
    private String refId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @Column(name = "source_account_ref_id", nullable = false)
    private String sourceAccountRefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @Column(name = "destination_account_ref_id", nullable = false)
    private String destinationAccountRefId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status;
}
