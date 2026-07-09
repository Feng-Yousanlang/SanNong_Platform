package com.ltqtest.springbootquickstart.knowledge.repository;

import com.ltqtest.springbootquickstart.knowledge.entity.AgricultureKnowledge;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class KnowledgeRepositoryTest {

    @Autowired
    private AgricultureKnowledgeRepository knowledgeRepository;

    @Test
    void findByTitleContaining_shouldReturnMatchingArticles() {
        knowledgeRepository.save(TestDataFactory.newKnowledge("水稻病虫害防治"));
        knowledgeRepository.save(TestDataFactory.newKnowledge("玉米种植技术"));

        Page<AgricultureKnowledge> page = knowledgeRepository.findByTitleContaining(
                "水稻",
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().get(0).getTitle().contains("水稻"));
        assertEquals(1, knowledgeRepository.countByTitleContaining("水稻"));
    }
}
