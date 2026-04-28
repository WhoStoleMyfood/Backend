package com.example.whostolemyfood.user;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.example.whostolemyfood.user.infrastructure.repository.UserRepositoryImpl;

@DataJpaTest
@Import(UserRepositoryImpl.class) // QueryDSL이나 커스텀 레포 구현체를 쓰면 임포트가 필요합니다.
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("나를 제외한 전체 사용자 목록 조회 (소프트 딜리트 제외 확인)")
    void 전체_사용자_조회_성공() {
        // 1. 준비
        UserEntity 나 = UserEntity.builder()
                .email("me@test.com").name("소윤").role(UserRole.MASTER).build();
        UserEntity 유저1 = UserEntity.builder()
                .email("user1@test.com").name("유저1").role(UserRole.CUSTOMER).build();

        userRepository.save(나);
        userRepository.save(유저1);
        // save 후에는 영속성 컨텍스트를 비우거나 flush해서 ID를 확정짓는게 안전해요.
        userRepository.flush();

        // 2. 실행
        Page<UserEntity> 결과 = userRepository.findAllExceptMe(PageRequest.of(0, 10), 나.getId());

        // 3. 검증 (더 안전한 방식)
        assertThat(결과.getContent()).isNotEmpty(); // 우선 비어있지 않은지 확인
        assertThat(결과.getTotalElements()).isEqualTo(1); // 나를 제외한 1명만 있어야 함

        // 이메일 리스트를 추출해서 "user1@test.com"이 포함되어 있는지 확인
        assertThat(결과.getContent())
                .extracting(UserEntity::getUserEmail)
                .containsExactly("user1@test.com");
    }

    @Test
    @DisplayName("이메일로 유저 찾기 - 활성 유저만")
    void 이메일_조회_테스트() {
        // 1. 준비
        String targetEmail = "findme@test.com";
        UserEntity 유저 = UserEntity.builder()
                .email(targetEmail)
                .name("조회대상")
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.saveAndFlush(유저); // 즉시 반영

        // 2. 실행
        Optional<UserEntity> 결과 = userRepository.findByUserEmail(targetEmail);

        // 3. 검증
        assertThat(결과).isPresent(); // 비어있으면 여기서 실패 메시지가 명확히 나옴
        assertThat(결과.get().getUserName()).isEqualTo("조회대상");
    }

    @Test
    @DisplayName("findByIdAndIsDeletedFalse - 삭제된 유저는 조회되지 않아야 함")
    void 소프트딜리트_조회_제한_테스트() {
        // 1. 준비
        UserEntity 삭제된_유저 = UserEntity.builder()
                .email("hidden@test.com").name("비공개").role(UserRole.CUSTOMER).build();
        org.springframework.test.util.ReflectionTestUtils.setField(삭제된_유저, "isDeleted", true);
        userRepository.save(삭제된_유저);

        // 2. 실행
        java.util.Optional<UserEntity> 결과 = userRepository.findByIdAndIsDeletedFalse(삭제된_유저.getId());

        // 3. 검증
        assertThat(결과).isEmpty();
    }
}