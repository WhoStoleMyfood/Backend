package com.example.whostolemyfood.user.infrastructure.repository;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.repository.UserRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    //final 지울게요 - 테스트돌리는데 jpa가 이 파일 인식을 못해요(final이라 초기화를 못해서 jpa가 이 필드에 값 주입을 못해서 터짐)
    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<UserEntity> findAllExceptMe(Pageable pageable, UUID myId) {
        // 1. 목록 조회 쿼리 (나 제외 + 삭제 안된 사람)
        String jpql = "select u from UserEntity u " +
                "where u.id != :myId " +
                "and u.isDeleted = false " +
                "order by u.createdAt desc";

        TypedQuery<UserEntity> query = em.createQuery(jpql, UserEntity.class)
                .setParameter("myId", myId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<UserEntity> content = query.getResultList();

        // 2. 카운트 쿼리 (페이징용)
        String countJpql = "select count(u) from UserEntity u " +
                "where u.id != :myId " +
                "and u.isDeleted = false";

        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("myId", myId)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}