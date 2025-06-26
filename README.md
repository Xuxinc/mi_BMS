# mi_BMS
小米大作业 - BMS系统设计

系统设计方案在线文档：[飞书文档](https://wayawbott0.f.mioffice.cn/docx/doxk4V8ZvZZ8icovA5qBTQu3Fkh)

## 项目结构与各模块说明（mi）

### 1. 配置模块
- **RedisConfig.java**  
Redis相关的配置类，主要用于配置Redis连接、序列化等相关参数，便于项目中使用Redis缓存。
- **RedissonConfig.java**  
Redisson客户端的配置类，Redisson是Redis的一个Java客户端，支持分布式锁等高级功能。此类用于配置Redisson相关参数。

### 2. 控制器模块
- **SignalReportController.java**  
预警信号相关的REST接口控制器，负责接收前端请求并调用Service层处理业务逻辑。主要接口包括：
  - 提交预警报告
  - 根据车辆ID获取电池信号状态
  - 根据车辆ID获取预警报告
  - 修改预警报告
  - 删除预警报告
  - 获取所有预警报告

### 3. 数据访问层
- **RuleMapper.java**  
规则表的数据访问接口，负责与数据库中的规则表进行交互。
- **SignalReportMapper.java**  
预警信号报告的数据访问接口，负责与数据库中的信号报告表进行交互。
- **VehicleInfoMapper.java**  
车辆信息的数据访问接口，负责与数据库中的车辆信息表进行交互。

### 4. 实体类模块
- **Result.java**  
通用的API返回结果封装类，包含返回码、消息、数据等字段，便于前后端数据交互。
- **Rule.java**  
规则实体类，对应数据库中的规则表，描述规则的属性。
- **SignalReport.java**  
预警信号报告实体类，对应数据库中的信号报告表，描述报告的详细信息。
- **SignalRequest.java**  
预警信号请求参数类，通常用于接收前端提交的信号数据。
- **SignalResponse.java**  
预警信号响应参数类，通常用于向前端返回信号处理结果。
- **VehicleInfo.java**  
车辆信息实体类，对应数据库中的车辆信息表，描述车辆的相关属性。

### 5. 消息队列相关模块
- **SignalReportConsumer.java**  
预警信号的消息消费者类，负责从消息队列中消费信号相关消息，打印出来并写入日志。
- **SignalReportScheduler.java**  
预警信号的定时任务调度类，负责定时拉取、发送信号数据等任务。

### 6. 业务逻辑层
- **SignalReportService.java**  
预警信号相关的业务接口，定义了信号处理的主要业务方法。
- **impl/SignalReportServiceImpl.java**  
预警信号相关的业务实现类，实现了SignalReportService接口，具体处理信号的业务逻辑。

### 7. 工具类模块
- **RedisCacheUtil.java**  
Redis缓存工具类，封装了常用的Redis操作方法，便于在项目中复用。
- **WarningLevelUtil.java**  
预警等级工具类，封装了与预警等级相关的计算、转换等工具方法。

### 8. 入口类
- **MiApplication.java**  
Spring Boot应用的主启动类，包含`main`方法，用于启动整个项目。

### 9. 配置文件
- **application.properties / application.yml**  
Spring Boot项目的核心配置文件，包含数据库、Redis、端口等配置信息。
- **logback-spring.xml**  
日志配置文件，定义了日志的格式、级别、输出位置等。

### 10. 其他
- **static/mi.sql**  
数据库初始化脚本，包含建表、初始化数据等SQL语句。
- **test/java/com/example/mi/**  
单元测试类目录，包含对各模块的测试用例，如`MiApplicationTests.java`、`SignalReportControllerTest.java`等。
