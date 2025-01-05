package com.example.threadsafepractice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class OrderServiceWithReentrantLock {

    // 상품 DB (공유 자원)
    private final Map<String, Integer> productDatabase = new HashMap<>();
    private final Map<String, OrderInfo> latestOrderDatabase = new HashMap<>();

    // ReentrantLock 객체 추가
    private final ReentrantLock lock = new ReentrantLock();

    public OrderServiceWithReentrantLock() {
        // 초기 상품 데이터 설정
        productDatabase.put("apple", 100);
        productDatabase.put("banana", 50);
        productDatabase.put("orange", 75);
    }

    // ReentrantLock을 사용하여 동기화된 주문 처리 메서드
    public void order(String productName, int amount) {
        lock.lock(); // 락 획득 (스레드 간 경합 방지)
        try {
            // 재고 체크 및 감소 로직
            Integer currentStock = productDatabase.getOrDefault(productName, 0);

            try {
                Thread.sleep(1); // 동시성 이슈 유발을 위한 인위적 지연
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            if (currentStock >= amount) {
                System.out.printf("Thread %d 주문 정보:\n", Thread.currentThread().threadId());
                System.out.printf("%8s: %d 건 ([%d])\n", productName, 1, amount);

                // 재고 감소
                productDatabase.put(productName, currentStock - amount);
                latestOrderDatabase.put(productName, new OrderInfo(productName, amount, System.currentTimeMillis()));
            } else {
                System.out.println("[ERROR] 재고 부족: " + productName);
            }
        } finally {
            lock.unlock(); // 락 해제 (데드락 방지)
        }
    }

    public static class OrderInfo {
        public OrderInfo(String productName, int amount, long orderTime) {
        }
    }

    // 재고 조회 메서드
    public int getStock(String productName) {
        lock.lock(); // 락 획득
        try {
            return productDatabase.getOrDefault(productName, 0);
        } finally {
            lock.unlock(); // 락 해제
        }
    }
}
