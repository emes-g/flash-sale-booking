-- 1. 사용자 더미 데이터
INSERT INTO users (name, point_balance, created_at)
VALUES ('홍길동', 100000, NOW());

-- 2. 숙소 더미 데이터
-- 상품 1: 이미 오픈된 초특가 상품 (어제 오픈)
INSERT INTO accommodations (name, price, open_at, check_in_time, check_out_time, created_at)
VALUES ('초특가 제주 호캉스 패키지', 50000, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 9 DAY), NOW());

-- 상품 2: 아직 오픈되지 않은 상품 (내일 오픈)
INSERT INTO accommodations (name, price, open_at, check_in_time, check_out_time, created_at)
VALUES ('부산 해운대 오션뷰 스위트', 80000, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 16 DAY), NOW());

-- 3. 숙소 재고 더미 데이터 (위에서 생성된 숙소 id와 1:1 매핑)
INSERT INTO accommodation_stocks (accommodation_id, stock, updated_at)
VALUES (1, 10, NOW());

INSERT INTO accommodation_stocks (accommodation_id, stock, updated_at)
VALUES (2, 5, NOW());