package com.ltqtest.springbootquickstart.buyrequest.repository;

import com.ltqtest.springbootquickstart.buyrequest.entity.BuyRequest;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class BuyRequestRepositoryTest {

    @Autowired
    private BuyRequestRepository buyRequestRepository;

    @Test
    void searchByKeyword_shouldMatchTitleOrContent() {
        buyRequestRepository.saveAndFlush(
                TestDataFactory.newBuyRequest("求购草莓", "需要新鲜草莓"));
        buyRequestRepository.saveAndFlush(
                TestDataFactory.newBuyRequest("玉米采购", "求购优质玉米"));

        List<BuyRequest> results = buyRequestRepository.searchByKeyword("草莓");

        assertEquals(1, results.size());
        assertTrue(results.get(0).getTitle().contains("草莓"));
    }
}
