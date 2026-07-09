package com.ltqtest.springbootquickstart.knowledge.repository;

import com.ltqtest.springbootquickstart.knowledge.entity.AgricultureKnowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgricultureKnowledgeRepository extends JpaRepository<AgricultureKnowledge, Integer> {
    Page<AgricultureKnowledge> findByTitleContaining(String keyword, Pageable pageable);
    
    long countByTitleContaining(String keyword);
}
