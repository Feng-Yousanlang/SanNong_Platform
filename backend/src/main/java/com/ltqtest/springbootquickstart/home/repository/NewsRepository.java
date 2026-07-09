package com.ltqtest.springbootquickstart.home.repository;

import com.ltqtest.springbootquickstart.home.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
}
