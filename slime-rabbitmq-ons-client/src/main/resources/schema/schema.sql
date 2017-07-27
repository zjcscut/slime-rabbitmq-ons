CREATE TABLE `t_transaction_message`(
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  uniqueCode VARCHAR(60) COMMENT '消息唯一标识',
  queue VARCHAR(100) COMMENT '目标队列',
  exchange VARCHAR(100) COMMENT '目标exchange',
  routingKey VARCHAR(100) COMMENT '目标routingKey',
  content VARCHAR(2000) COMMENT '消息体字符串',
  UNIQUE unique_uniquecode(`uniqueCode`)
)COMMENT '事务消息表';

CREATE TABLE `t_transaction_log`(
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  transactionId BIGINT COMMENT '事务id,全局唯一',
  transactionMessageId BIGINT COMMENT '事务消息表主键',
  messageId VARCHAR(60) COMMENT '消息id',
  uniqueCode VARCHAR(60) COMMENT '消息唯一标识',
  transactionStats VARCHAR(20) COMMENT '事务状态',
  pushStats VARCHAR(20) COMMENT '推送状态',
  fireTransactionStats VARCHAR(20) COMMENT '激活事务状态',
  checkAttemptTime TINYINT COMMENT 'check重试次数' DEFAULT 0,
  pushAttemptTime TINYINT COMMENT 'push重试次数' DEFAULT 0,
  createTime DATETIME COMMENT '创建时间' DEFAULT CURRENT_TIMESTAMP(),
  updateTime DATETIME COMMENT '更新时间',
  UNIQUE unique_uniquecode(`uniqueCode`),
  KEY key_messageId(`messageId`)
)COMMENT '事务日志表';

