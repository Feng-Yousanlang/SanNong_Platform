package com.ltqtest.springbootquickstart.integration;

import com.ltqtest.springbootquickstart.home.entity.News;
import com.ltqtest.springbootquickstart.home.repository.NewsRepository;
import com.ltqtest.springbootquickstart.knowledge.entity.AgricultureKnowledge;
import com.ltqtest.springbootquickstart.knowledge.repository.AgricultureKnowledgeRepository;
import com.ltqtest.springbootquickstart.support.IntegrationTestBase;
import com.ltqtest.springbootquickstart.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContentApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private AgricultureKnowledgeRepository knowledgeRepository;

    @BeforeEach
    void seedContent() {
        newsRepository.save(TestDataFactory.newNews("春耕备耕要点"));
        AgricultureKnowledge knowledge = TestDataFactory.newKnowledge("水稻病虫害防治");
        knowledgeRepository.save(knowledge);
    }

    @Test
    void newsEndpoint_shouldReturnSeededNews() throws Exception {
        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.newsList[0].title").value("春耕备耕要点"));
    }

    @Test
    void knowledgeList_shouldReturnPagedData() throws Exception {
        mockMvc.perform(get("/api/knowledge/list").param("page", "1").param("page_size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("水稻病虫害防治"));
    }

    @Test
    void knowledgeSearch_shouldRequireKeyword() throws Exception {
        mockMvc.perform(get("/api/knowledge/search").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void knowledgeSearch_shouldFindByTitle() throws Exception {
        mockMvc.perform(get("/api/knowledge/search").param("q", "水稻"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("水稻病虫害防治"))
                .andExpect(jsonPath("$.total").value(1));
    }
}
