package com.musinsa.course.domain.department.entity;

import com.musinsa.course.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "department")
@Getter
@SQLDelete(sql = "UPDATE department SET deleted_at = NOW() where id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Builder
    public Department(String name) {
        this.name = name;
    }
}
