-- 先清空数据（按顺序避免外键冲突）
DELETE FROM question;
DELETE FROM subject;

-- 插入示例科目
INSERT INTO subject (id, name) VALUES (1, '数据库'), (2, '习近平新时代中国特色社会主义思想概论'), (3, '毛泽东思想和中国特色社会主义理论体系概论');

-- 重置自增 ID
ALTER TABLE subject AUTO_INCREMENT = 4;
