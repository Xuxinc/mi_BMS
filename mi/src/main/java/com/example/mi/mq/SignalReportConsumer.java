package com.example.mi.mq;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "signal-consumer-group",
        topic = "signal-warning-topic",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.BROADCASTING
)
public class SignalReportConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        // 使用log.info打印日志
        log.info("Received warning: {}", message);
    }
}