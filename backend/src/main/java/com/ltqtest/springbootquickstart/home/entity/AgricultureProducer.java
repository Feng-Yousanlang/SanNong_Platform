package com.ltqtest.springbootquickstart.home.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "agriculture_producer")
public class AgricultureProducer {
    @Id
    @Column(name = "producerId")
    private Integer producerId;
    
    @Column(name = "producerName")
    private String producerName;
}
