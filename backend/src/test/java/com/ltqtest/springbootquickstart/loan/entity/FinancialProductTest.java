package com.ltqtest.springbootquickstart.loan.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialProductTest {

    @Test
    void getTagsArray_shouldSplitCommaSeparatedTags() {
        FinancialProduct product = new FinancialProduct();
        product.setTags("低息,农户专享,快速放款");

        assertArrayEquals(
                new String[]{"低息", "农户专享", "快速放款"},
                product.getTagsArray()
        );
    }

    @Test
    void setTagsArray_shouldJoinTagsBackToString() {
        FinancialProduct product = new FinancialProduct();
        product.setTagsArray(new String[]{"A", "B"});

        assertEquals("A,B", product.getTags());
    }

    @Test
    void getTagsArray_shouldReturnEmptyArrayWhenTagsMissing() {
        FinancialProduct product = new FinancialProduct();
        assertArrayEquals(new String[0], product.getTagsArray());
    }
}
