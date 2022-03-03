package com.alok.spring.batch.response;

import lombok.Builder;
import lombok.Data;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class GetTransactionsResponse {

    private List<Transaction> transactions;
    private Integer count;

    @Data
    @Builder
    public static class Transaction {
        private Integer id;
        private Date date;
        private Integer debit;
        private Integer credit;
        private String head;
        private Date lastTransactionDate;
    }
}
