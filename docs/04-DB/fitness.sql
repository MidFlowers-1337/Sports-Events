-- 在现有 sports_events 数据库执行；不删除现有表或数据。
CREATE TABLE IF NOT EXISTS fitness_record (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  test_date DATE NOT NULL,
  height_cm DOUBLE NOT NULL COMMENT '身高cm',
  weight_kg DOUBLE NOT NULL COMMENT '体重kg',
  vital_capacity DOUBLE NOT NULL COMMENT '肺活量ml',
  run50_seconds DOUBLE NOT NULL COMMENT '50米秒数',
  jump_cm DOUBLE NOT NULL COMMENT '立定跳远cm',
  UNIQUE KEY uk_user_date (user_id, test_date),
  KEY idx_test_date (test_date),
  CONSTRAINT fk_fitness_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生体测原始数据';
