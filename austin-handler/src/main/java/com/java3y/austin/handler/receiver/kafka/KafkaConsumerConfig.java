package com.java3y.austin.handler.receiver.kafka;

import cn.hutool.core.text.StrPool;
import com.java3y.austin.handler.utils.GroupIdMappingUtils;
import com.java3y.austin.support.constans.MessageQueuePipeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kafka 消费者配置类
 * 负责配置 Kafka 监听器相关的 Bean 和初始化 Receiver 实例
 *
 * @author 3y
 */
@Configuration
@ConditionalOnProperty(name = "austin.mq.pipeline", havingValue = MessageQueuePipeline.KAFKA)
@Slf4j
public class KafkaConsumerConfig implements SmartInitializingSingleton {

    /**
     * receiver 的消费方法常量
     */
    private static final String RECEIVER_METHOD_NAME = "Receiver.consumer";
    
    /**
     * 获取得到所有的 groupId
     */
    private static final List<String> GROUP_IDS = GroupIdMappingUtils.getAllGroupIds();
    
    /**
     * 下标(用于迭代 groupIds 位置)，使用 AtomicInteger 解决并发问题
     */
    private static final AtomicInteger index = new AtomicInteger(0);
    
    private final ApplicationContext applicationContext;
    public KafkaConsumerConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 给每个 Receiver 对象的 consumer 方法 @KafkaListener 赋值相应的 groupId
     * 
     * @return AnnotationEnhancer
     */
    @Bean
    public static KafkaListenerAnnotationBeanPostProcessor.AnnotationEnhancer groupIdEnhancer() {
        return (attrs, element) -> {
            if (element instanceof Method) {
                String name = ((Method) element).getDeclaringClass().getSimpleName() 
                    + StrPool.DOT + ((Method) element).getName();
                if (RECEIVER_METHOD_NAME.equals(name)) {
                    int currentIndex = index.getAndIncrement();
                    if (currentIndex < GROUP_IDS.size()) {
                        attrs.put("groupId", GROUP_IDS.get(currentIndex));
                    }
                }
            }
            return attrs;
        };
    }

    /**
     * 针对 tag 消息过滤的容器工厂
     * producer 将 tag 写进 header 里
     * 
     * @param consumerFactory Kafka 消费者工厂
     * @param tagIdKey tagId 的 key
     * @param tagIdValue tagId 的 value
     * @return 配置好的容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> filterContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            @Value("${austin.business.tagId.key}") String tagIdKey,
            @Value("${austin.business.tagId.value}") String tagIdValue) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setAckDiscarded(true);

        factory.setRecordFilterStrategy(consumerRecord -> {
            if (Optional.ofNullable(consumerRecord.value()).isPresent()) {
                for (Header header : consumerRecord.headers()) {
                    if (header.key().equals(tagIdKey) &&
                            new String(header.value(), StandardCharsets.UTF_8).equals(tagIdValue)) {
                        return false;
                    }
                }
            }
            return true;
        });
        return factory;
    }

    /**
     * 在所有单例 Bean 初始化完成后，为每个渠道不同的消息类型创建一个 Receiver 对象
     */
    @Override
    public void afterSingletonsInstantiated() {
        log.info("开始初始化 Receiver 实例，需要创建 {} 个实例", GROUP_IDS.size());
        for (int i = 0; i < GROUP_IDS.size(); i++) {
            applicationContext.getBean(Receiver.class);
        }
        log.info("Receiver 实例初始化完成");
    }
}
