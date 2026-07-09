package com.ltqtest.springbootquickstart.expert.repository;

import com.ltqtest.springbootquickstart.expert.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Integer> {
    List<Expert> findByExpertNameContainingOrFieldContainingOrExpertDescriptionContaining(String nameKeyword, String fieldKeyword, String descriptionKeyword);
    
    Optional<Expert> findByExpertName(String expertName);
    
    Optional<Expert> findByExpertId(Integer expertId);
}
