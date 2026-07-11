-- 更新商品与新闻图片
-- Get-Content docs/fix-product-images.sql -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4 test
SET NAMES utf8mb4;

UPDATE product SET productImg = '/products/apple.svg'   WHERE product_name LIKE '%苹果%';
UPDATE product SET productImg = '/products/cabbage.svg' WHERE product_name LIKE '%白菜%';
UPDATE product SET productImg = '/products/rice.svg'    WHERE product_name LIKE '%大米%';
UPDATE product SET productImg = '/products/egg.svg'     WHERE product_name LIKE '%鸡蛋%';
UPDATE product SET productImg = '/products/grape.svg'   WHERE product_name LIKE '%葡萄%';
UPDATE product SET productImg = '/products/potato.svg'  WHERE product_name LIKE '%土豆%';

UPDATE tb_news SET imgUrl = '/news/wheat.svg'      WHERE title LIKE '%小麦%';
UPDATE tb_news SET imgUrl = '/news/greenhouse.svg' WHERE title LIKE '%大棚%';
UPDATE tb_news SET imgUrl = '/news/farm.svg'       WHERE title LIKE '%电商%';

SELECT product_name, productImg FROM product ORDER BY product_id;
SELECT title, imgUrl FROM tb_news;
