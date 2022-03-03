package com.alok.spring.batch.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class GetExpensesResponse {

    private List<Expense> expenses;
    private Integer count;

    @Data
    @Builder
    public static class Expense {
        private Long id;
        private Date date;
        private String head;
        private Double amount;
        private String category;
        private String comment;
        private Date lastTransactionDate;
    }
}
