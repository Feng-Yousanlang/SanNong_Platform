package com.ltqtest.springbootquickstart.loan.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "financial_product")
public class FinancialProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fpId")
    private Integer fpId;
    
    @Column(name = "fpName", nullable = false)
    private String fpName;
    
    @Column(name = "fpDescription", nullable = false)
    private String fpDescription;
    
    @Column(name = "annualRate")
    private Float annualRate;
    
    @Column(name = "tags", nullable = false)
    private String tags;
    
    @Column(name = "fpManagerName", nullable = false)
    private String fpManagerName;
    
    @Column(name = "fpManagerPhone", nullable = false)
    private String fpManagerPhone;
    
    @Column(name = "fpManagerEmail")
    private String fpManagerEmail;
    
    @Column(name = "maxAmount", nullable = false)
    private Integer maxAmount;
    
    @Column(name = "minAmount", nullable = false)
    private Integer minAmount;
    
    @Column(name = "term")
    private Integer term;

    
    
    @Transient
    public String[] getTagsArray() {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }
    
    @Transient
    public void setTagsArray(String[] tagsArray) {
        if (tagsArray == null || tagsArray.length == 0) {
            this.tags = "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tagsArray.length; i++) {
                sb.append(tagsArray[i]);
                if (i < tagsArray.length - 1) {
                    sb.append(",");
                }
            }
            this.tags = sb.toString();
        }
    }
}
