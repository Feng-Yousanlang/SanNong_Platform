package com.ltqtest.springbootquickstart.knowledge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "agriculture_knowledge")
public class AgricultureKnowledge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "publish")
    private Date publish;
}
