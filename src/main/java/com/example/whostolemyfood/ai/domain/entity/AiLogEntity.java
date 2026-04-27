package com.example.whostolemyfood.ai.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_ai_logs")
public class AiLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_log_id")
    private UUID aiLogId;

    // 요청자
    @Column(name = "user_id")
    private UUID userId;

    // 요청 텍스트
    @Column(name = "request_text", nullable = false, length = 100)
    private String requestText ;

    // ai 응답 텍스트
    @Column(name = "response_text", nullable = false, length = 100)
    private String responseText ;

    // 요청 시간
    @CreatedDate
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

//    // 요청자 == 생성자
//    @Column(name = "created_by")
//    private UUID createdBy;
}
