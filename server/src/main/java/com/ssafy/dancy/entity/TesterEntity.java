package com.ssafy.dancy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TesterEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entityId;

    private String name;
    private String address;
}