package com.example.whostolemyfood.review.infrastructure.schedule;

import com.example.whostolemyfood.review.application.service.ReviewRatingBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRatingBatchScheduler {

	private final ReviewRatingBatchService reviewRatingBatchService;

//	/**
//	 * 매일 한국시간 기준 자정 실행
//	 */
//	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
//	public void refreshStoreRatingSummary() {
//		log.info("리뷰 평점 집계 배치 시작");
//		reviewRatingBatchService.refreshAllStoreRatings();
//		log.info("리뷰 평점 집계 배치 종료");
//	}
}