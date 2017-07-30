CREATE TABLE `t_transaction_message`(
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  uniqueCode VARCHAR(60) COMMENT '消息唯一标识',
  queue VARCHAR(100) COMMENT '目标队列',
  exchange VARCHAR(100) COMMENT '目标exchange',
  exchangeType VARCHAR(10) COMMENT '目标exchange类型',
  headers VARCHAR(200) COMMENT 'headers',
  routingKey VARCHAR(100) COMMENT '目标routingKey',
  content VARCHAR(2000) COMMENT '消息体字符串',
  UNIQUE unique_uniqueCode(`uniqueCode`)
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
  transactionEndTime DATETIME COMMENT '事务结束时间',
  pushTime DATETIME COMMENT '推送时间',
  fireTransactionTime DATETIME COMMENT '事务触发时间',
  checkerClassName VARCHAR(60) COMMENT 'checker全类名',
  UNIQUE unique_uniqueCode(`uniqueCode`),
  KEY idx_messageId(`messageId`),
  KEY idx_creditTime(`createTime`)
)COMMENT '事务日志表';

