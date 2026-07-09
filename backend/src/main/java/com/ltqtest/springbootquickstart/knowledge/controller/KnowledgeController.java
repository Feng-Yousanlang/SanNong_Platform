package com.ltqtest.springbootquickstart.knowledge.controller;

import com.ltqtest.springbootquickstart.knowledge.entity.AgricultureKnowledge;
import com.ltqtest.springbootquickstart.knowledge.repository.AgricultureKnowledgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private AgricultureKnowledgeRepository knowledgeRepository;

    @GetMapping("/list")
    public Map<String, Object> getKnowledgeList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            Pageable pageable = PageRequest.of(page - 1, pageSize, org.springframework.data.domain.Sort.by("publish").descending());
            
            Page<AgricultureKnowledge> resultPage = knowledgeRepository.findAll(pageable);
            List<AgricultureKnowledge> knowledgeList = resultPage.getContent();
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", knowledgeList);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误：" + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/search")
    public Map<String, Object> searchKnowledge(
            @RequestParam(value = "q") String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "检索关键词不能为空");
                return response;
            }
            
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            Pageable pageable = PageRequest.of(page - 1, pageSize, org.springframework.data.domain.Sort.by("publish").descending());
            
            Page<AgricultureKnowledge> resultPage = knowledgeRepository.findByTitleContaining(keyword, pageable);
            List<AgricultureKnowledge> knowledgeList = resultPage.getContent();
            
            long total = knowledgeRepository.countByTitleContaining(keyword);
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", knowledgeList);
            response.put("page", page);
            response.put("page_size", pageSize);
            response.put("total", total);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "搜索失败：" + e.getMessage());
        }
        
        return response;
    }
}
