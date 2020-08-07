package com.baijia.gaotu.channel.config.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 类的描述
 *confi
 * @author sunyue
 * @date 2020/7/1 上午10:19
 */
@Configuration
public class LocalFeignConfig {

    @Resource
    @ConditionalOnBean(ObjectMapper.class)
    public ObjectMapper objectMapper(final ObjectMapper objectMapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, RpcTimeSerializer.localDateTime());
        module.addSerializer(LocalDate.class, RpcTimeSerializer.localDate());
        module.addSerializer(Date.class, RpcTimeSerializer.date());
        module.addDeserializer(LocalDateTime.class, RpcTimeDeserializer.localDateTime());
        module.addDeserializer(LocalDate.class, RpcTimeDeserializer.localDate());
        module.addDeserializer(Date.class, RpcTimeDeserializer.date());
        objectMapper.registerModule(module);
        return objectMapper;
    }

}
