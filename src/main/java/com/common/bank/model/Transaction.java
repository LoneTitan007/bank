package com.common.bank.model;

import com.common.bank.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseEntity {

    @Column(name = "ref_id", nullable = false, unique = true, length = 50)
    private String refId;

    @Column(name = "source_account_ref_id", nullable = false)
    private String sourceAccountRefId;

    @Column(name = "destination_account_ref_id", nullable = false)
    private String destinationAccountRefId;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
