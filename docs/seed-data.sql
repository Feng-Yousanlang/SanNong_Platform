-- 三农服务平台 · 演示测试数据
-- 用法：在已执行 init.sql 的数据库中运行本文件
-- mysql -u root -p test < docs/seed-data.sql

SET NAMES utf8mb4;

-- 修复求购表主键自增（init.sql 未设置 AUTO_INCREMENT）
ALTER TABLE buy_request MODIFY buy_request_id INT NOT NULL AUTO_INCREMENT;

-- ========== 替换 init.sql 中的旧演示专家/审批人，统一为平台测试数据 ==========
-- 说明：农户端「专家列表」读取 experts 表；expert01 是登录账号（tb_user），需与 experts 档案关联
DELETE FROM expert_appointment;
DELETE FROM expert_user_chat_record;
DELETE FROM experts;

INSERT INTO experts (expertName, field, expertDescription, expertImg, example, expertPhone, expertEmail) VALUES
('王专家', '种植技术,病虫害防治', '平台认证农业专家，登录账号 expert01', '', '指导多地番茄晚疫病防控', '13800000003', 'expert01@test.com');

DELETE FROM approver;
INSERT INTO approver (approverName, approverPhone, approverEmail) VALUES
('赵经理', '13800000004', 'bank01@test.com');

-- ========== 测试账号（密码均为 123456，明文存储） ==========
INSERT INTO tb_user (username, password, real_name, role_type, phone, email, status, login_status, create_time, expert_id, approver_id)
VALUES
  ('farmer01', '123456', '张农户', 1, '13800000001', 'farmer01@test.com', 1, 0, NOW(), 0, 0),
  ('buyer01',  '123456', '李买家', 2, '13800000002', 'buyer01@test.com',  1, 0, NOW(), 0, 0),
  ('expert01', '123456', '王专家', 3, '13800000003', 'expert01@test.com', 1, 0, NOW(), 0, 0),
  ('bank01',   '123456', '赵经理', 4, '13800000004', 'bank01@test.com',   1, 0, NOW(), 0, 0),
  ('admin01',  '123456', '系统管理员', 5, '13800000005', 'admin01@test.com', 1, 0, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  real_name = VALUES(real_name),
  role_type = VALUES(role_type),
  phone = VALUES(phone),
  email = VALUES(email);

UPDATE tb_user u
INNER JOIN experts e ON e.expertName = '王专家' AND e.expertPhone = '13800000003'
SET u.expert_id = e.expertId
WHERE u.username = 'expert01';

UPDATE tb_user u
INNER JOIN approver a ON a.approverPhone = '13800000004'
SET u.approver_id = a.approverId
WHERE u.username = 'bank01';

-- ========== 农产品（挂载到 farmer01） ==========
DELETE FROM product WHERE user_id IN (SELECT user_id FROM tb_user WHERE username = 'farmer01');

INSERT INTO product (product_name, price, producer, salesVolume, productImg, surplus, user_id, total_volumn, status)
SELECT '有机红富士苹果', 8.50, '山东烟台', 86, '/products/apple.svg', 500, user_id, 500, 1
FROM tb_user WHERE username = 'farmer01' LIMIT 1;

INSERT INTO product (product_name, price, producer, salesVolume, productImg, surplus, user_id, total_volumn, status)
SELECT '新鲜小白菜', 3.20, '河北廊坊', 210, '/products/cabbage.svg', 800, user_id, 800, 1
FROM tb_user WHERE username = 'farmer01' LIMIT 1;

INSERT INTO product (product_name, price, producer, salesVolume, productImg, surplus, user_id, total_volumn, status)
SELECT '五常大米 5kg', 45.00, '黑龙江五常', 56, '/products/rice.svg', 200, user_id, 200, 1
FROM tb_user WHERE username = 'farmer01' LIMIT 1;

INSERT INTO product (product_name, price, producer, salesVolume, productImg, surplus, user_id, total_volumn, status)
SELECT '散养土鸡蛋 30枚', 38.00, '四川眉山', 132, '/products/egg.svg', 150, user_id, 150, 1
FROM tb_user WHERE username = 'farmer01' LIMIT 1;

INSERT INTO product (product_name, price, producer, salesVolume, productImg, surplus, user_id, total_volumn, status)
SELECT '阳光玫瑰葡萄', 18.80, '云南建水', 45, '/products/grape.svg', 300, user_id, 300, 1
FROM tb_user WHERE username = 'farmer01' LIMIT 1;

INSERT INTO product (product_name, price, producer, salesVolume, productImg, surplus, user_id, total_volumn, status)
SELECT '黄心土豆', 2.50, '内蒙古乌兰察布', 320, '/products/potato.svg', 2000, user_id, 2000, 1
FROM tb_user WHERE username = 'farmer01' LIMIT 1;

-- ========== 买家收货地址 ==========
INSERT INTO user_address (user_id, address_name)
SELECT user_id, '北京市海淀区西直门外大街137号'
FROM tb_user WHERE username = 'buyer01' LIMIT 1;

-- ========== 求购信息 ==========
INSERT INTO buy_request (title, content, contact, create_time) VALUES
('求购新鲜草莓', '需要产地直供的新鲜草莓，每日约50kg，长期合作。', '13800000002', NOW()),
('求购有机蔬菜礼盒', '公司节日福利采购，需要有机认证蔬菜组合装，数量200份。', '13900001111', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('求购优质玉米', '饲料加工厂采购，要求水分≤14%，霉变率≤2%。', '13700002222', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('求购土蜂蜜', '寻找本地散养土蜂蜜，需提供质检报告。', '13600003333', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ========== 新闻轮播（补充） ==========
DELETE FROM tb_news WHERE title IN (
  '春季小麦田间管理要点',
  '智慧大棚助力蔬菜稳产增收',
  '农村电商带动农产品出村进城'
);

INSERT INTO tb_news (title, imgUrl, newsUrl) VALUES
('春季小麦田间管理要点', '/news/wheat.svg', 'https://www.moa.gov.cn/'),
('智慧大棚助力蔬菜稳产增收', '/news/greenhouse.svg', 'https://www.moa.gov.cn/'),
('农村电商带动农产品出村进城', '/news/farm.svg', 'https://www.moa.gov.cn/');

-- ========== 农业知识库（补充） ==========
INSERT INTO agriculture_knowledge (title, source, url, publish) VALUES
('小麦赤霉病防治技术指南', '农技推广', 'https://www.moa.gov.cn/', NOW()),
('设施蔬菜温度湿度管理', '专家讲堂', 'https://www.moa.gov.cn/', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('绿色防控减少农药使用', '科普宣传', 'https://www.moa.gov.cn/', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('畜禽养殖粪污资源化利用', '政策解读', 'https://www.moa.gov.cn/', DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ========== 专家预约示例 ==========
INSERT INTO expert_appointment (expert_id, user_id, date, startTime, endTime, topic, status, remark, create_time, update_time)
SELECT e.expertId, u.user_id, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', '10:00:00', '番茄晚疫病防治咨询', '待审核', '买家预约', NOW(), NOW()
FROM tb_user u, experts e
WHERE u.username = 'buyer01'
ORDER BY e.expertId LIMIT 1;

-- ========== 贷款申请示例（农户 → 首个贷款产品） ==========
INSERT INTO loan_application (userId, fpId, amount, term, documents, status, applyTime)
SELECT u.user_id, fp.fpId, 20000, 12, NULL, 1, NOW()
FROM tb_user u
CROSS JOIN (SELECT fpId FROM financial_product ORDER BY fpId LIMIT 1) fp
WHERE u.username = 'farmer01'
LIMIT 1;

SELECT 'seed data loaded' AS status;
SELECT expertId, expertName, expertPhone FROM experts;
SELECT username, real_name, role_type, expert_id, approver_id FROM tb_user WHERE username LIKE '%01';
SELECT product_id, product_name, price, surplus FROM product ORDER BY product_id DESC LIMIT 6;
