-- 平台分发账号（专家 / 银行 / 管理员）
-- 不可通过注册页自助注册，需由管理员导入或后台创建
-- 用法：mysql -u root -p test < docs/staff-accounts.sql
-- 密码均为 123456（明文，与演示环境一致）

SET NAMES utf8mb4;

-- ========== 专家 expert01 ==========
INSERT INTO experts (expertName, field, expertDescription, expertImg, example, expertPhone, expertEmail)
SELECT '王专家', '种植技术,病虫害防治', '平台认证农业专家，擅长果蔬栽培与病害诊断', '', '指导多地番茄晚疫病防控', '13800000003', 'expert01@test.com'
WHERE NOT EXISTS (
  SELECT 1 FROM experts e
  INNER JOIN tb_user u ON u.expert_id = e.expertId AND u.username = 'expert01'
);

INSERT INTO tb_user (username, password, real_name, role_type, phone, email, status, login_status, create_time, expert_id, approver_id)
VALUES ('expert01', '123456', '王专家', 3, '13800000003', 'expert01@test.com', 1, 0, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  real_name = VALUES(real_name),
  role_type = VALUES(role_type),
  phone = VALUES(phone),
  email = VALUES(email);

UPDATE tb_user u
INNER JOIN experts e ON e.expertName = '王专家' AND e.expertPhone = '13800000003'
SET u.expert_id = e.expertId, u.approver_id = 0
WHERE u.username = 'expert01' AND (u.expert_id IS NULL OR u.expert_id = 0);

-- ========== 银行 bank01 ==========
INSERT INTO approver (approverName, approverPhone, approverEmail)
SELECT '赵经理', '13800000004', 'bank01@test.com'
WHERE NOT EXISTS (
  SELECT 1 FROM approver a
  INNER JOIN tb_user u ON u.approver_id = a.approverId AND u.username = 'bank01'
);

INSERT INTO tb_user (username, password, real_name, role_type, phone, email, status, login_status, create_time, expert_id, approver_id)
VALUES ('bank01', '123456', '赵经理', 4, '13800000004', 'bank01@test.com', 1, 0, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  real_name = VALUES(real_name),
  role_type = VALUES(role_type),
  phone = VALUES(phone),
  email = VALUES(email);

UPDATE tb_user u
INNER JOIN approver a ON a.approverName = '赵经理' AND a.approverPhone = '13800000004'
SET u.approver_id = a.approverId, u.expert_id = 0
WHERE u.username = 'bank01' AND (u.approver_id IS NULL OR u.approver_id = 0);

-- ========== 管理员 admin01 ==========
INSERT INTO tb_user (username, password, real_name, role_type, phone, email, status, login_status, create_time, expert_id, approver_id)
VALUES ('admin01', '123456', '系统管理员', 5, '13800000005', 'admin01@test.com', 1, 0, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  real_name = VALUES(real_name),
  role_type = VALUES(role_type),
  phone = VALUES(phone),
  email = VALUES(email);

SELECT username, real_name, role_type, expert_id, approver_id
FROM tb_user
WHERE username IN ('expert01', 'bank01', 'admin01');
