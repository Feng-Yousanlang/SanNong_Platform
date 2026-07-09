package com.ltqtest.springbootquickstart.loan.entity;

import com.ltqtest.springbootquickstart.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "loan_application")
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicationId")
    private Integer applicationId;
    
    @Column(name = "userId", nullable = false)
    private Integer userId;
    
    @Column(name = "fpId", nullable = false)
    private Integer fpId;
    
    @Column(name = "amount", nullable = false)
    private Integer amount;
    
    @Column(name = "term", nullable = false)
    private Integer term;
    
    @Column(name = "documents")
    private String documents;
    
    @Column(name = "status", nullable = false)
    private Integer status; // 申请状态，对应loan_status表的status_code
    
    @Column(name = "applyTime", nullable = false)
    private Date applyTime;
    
    @ManyToOne
    @JoinColumn(name = "fpId", referencedColumnName = "fpId", insertable = false, updatable = false)
    private FinancialProduct financialProduct;
    
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
    
    // 关联到贷款状态表，通过status字段（对应status_code）
    @ManyToOne
    @JoinColumn(name = "status", referencedColumnName = "status_code", insertable = false, updatable = false)
    private LoanStatus loanStatus;
}
