package com.example.threadsafepractice;

import java.util.concurrent.ConcurrentHashMap;

public class OrderServiceWithConcurrentHashMap {
    // 상품 DB
    private final ConcurrentHashMap<String, Integer> productDatabase = new ConcurrentHashMap<>();
    // 가장 최근 주문 정보를 저장하는 DB
    private final ConcurrentHashMap<String, OrderInfo> latestOrderDatabase = new ConcurrentHashMap<>();

    public OrderServiceWithConcurrentHashMap() {
        // 초기 상품 데이터
        productDatabase.put("apple", 100);
        productDatabase.put("banana", 50);
        productDatabase.put("orange", 75);
    }

    // 주문 처리 메서드
    public void order(String productName, int amount) {
        productDatabase.compute(productName, (key, currentStock) -> {
            if (currentStock == null) {
                throw new IllegalArgumentException("상품이 존재하지 않습니다.");
            }

            try {
                Thread.sleep(1); // 동시성 이슈 유발을 위한 인위적 지연
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            if (currentStock >= amount) {
                System.out.printf("Thread %d 주문 정보:\n", Thread.currentThread().threadId());
                System.out.printf("%8s: %d 건 ([%d])\n", productName, 1, amount);

                productDatabase.put(productName, currentStock - amount);
                latestOrderDatabase.put(productName, new OrderInfo(productName, amount, System.currentTimeMillis()));
                return currentStock - amount; // 기존 재고에서 주문 수량 차감
            }
            System.out.println("[ERROR] 재고 부족: " + productName);
            return currentStock; // 차감할 수 없으면 기존 재고 유지
        });
    }

    public static class OrderInfo {
        public OrderInfo(String productName, int amount, long orderTime) {
        }
    }

    // 재고 조회
    public int getStock(String productName) {
        return productDatabase.getOrDefault(productName, 0);
    }
}
