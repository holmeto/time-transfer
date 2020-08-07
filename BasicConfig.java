package com.gaotu.commons.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.baijia.commons.lang.utils.cache.redis.Heartbeat;
import com.baijia.commons.lang.utils.cache.redis.RedisUtil;
import com.baijia.gaotu.mojito.inf.api.WeiXinService;
import com.baijia.gaotu.mojito.inf.config.LoggerIdInterceptor;
import com.baijia.gaotu.mojito.inf.service.WeiXinServiceImpl;
import com.baijia.gaotu.mojito.inf.util.InternalService;
import com.baijia.gaotu.mojito.inf.util.InternalServiceValidation;
import com.baijia.gaotu.mojito.inf.util.InternalServiceValidation.InternalServiceSecret;
import com.baijia.jedis.JedisPoolWrapper;
import com.baijia.jedis.JedisTemplate;
import com.baijia.timer.TimerSpringConfig;

import com.gaotu.feignconfig.FeignConfig;
import com.gaotu.utils.SimpleMailSender;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nepxion.discovery.plugin.strategy.adapter.DiscoveryEnabledAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;


/**
 * @author chenjiajun
 * Date: 16/7/12.
 */
@Configuration
@ComponentScan(basePackages = {"com.gaotu", "com.alibaba.cola"},
               excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = FeignConfig.class) })
@PropertySource(ignoreResourceNotFound = true, value = { "file:/apps/conf/api/redis.properties","classpath:redis.properties" })
@Import( { TimerSpringConfig.class })
@ComponentScan(basePackages = {"com.gaotu"})
@Slf4j
public class BasicConfig {

    @Bean
    @Lazy
    public JavaMailSender mailSender(@Value("${mail.host:smtpproxy.baijiahulian.com}") final String host,
        @Value("${mail.port:25}") final int port, @Value("${mail.encoding:utf-8}") final String encoding) {
        log.info("Java mail sender init.");
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setDefaultEncoding(encoding);
        return mailSender;
    }

    @Bean
    @Lazy
    public SimpleMailSender defaultMailSender(JavaMailSender mailSender,
        @Value("${mail.sender.default.from:gaotu-tech@baijiahulian.com}") String from,
        @Value("${mail.sender.default.to}") String to,
        @Value("${spring.profiles.active}") String envStr) {
        SimpleMailSender simpleMailSender = new SimpleMailSender(mailSender, from, to);
        simpleMailSender.setSubjectPrefix("[" + envStr + "]");
        return simpleMailSender;
    }

    @Bean
    public HttpMessageConverters httpMessageConvertersWithFastJson() {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        com.alibaba.fastjson.support.config.FastJsonConfig fastJsonConfig = new com.alibaba.fastjson.support.config.FastJsonConfig();

        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        ParserConfig.getGlobalInstance().putDeserializer(LocalDateTime.class, new LocalDateDeserializer());
        ParserConfig.getGlobalInstance().putDeserializer(LocalDate.class, new LocalDateDeserializer());
        fastJsonConfig.setParserConfig(parserConfig);

        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat, SerializerFeature.WriteDateUseDateFormat);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        ArrayList<MediaType> fastMediaTypeList = new ArrayList<>();
        fastMediaTypeList.add(MediaType.APPLICATION_JSON_UTF8);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(fastMediaTypeList);
        return new HttpMessageConverters((HttpMessageConverter<?>) fastJsonHttpMessageConverter);
    }

    @Bean
    public JedisPoolWrapper jedisPool(@Value("${redis.maxTotal:60}") int maxTotal,
        @Value("${redis.maxIdle:20}") int maxIdle, @Value("${redis.testOnBorrow:true}") boolean testOnBorrow,
        @Value("${redis.host}") String redisHost, @Value("${redis.port}") int port, @Value("${redis.timeout}") int timeout,
        @Value("${redis.pass}") String password, @Value("${redis.visit.db}") int database) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setTestOnBorrow(testOnBorrow);
        return new JedisPoolWrapper(config, redisHost, port, timeout, password, database);
    }

    @Bean
    public JedisTemplate jedisTemplate() {
        return new JedisTemplate();
    }

    @Bean
    public RedisUtil redisUtil(JedisPool jedisPool) {
        return new RedisUtil(jedisPool);
    }


    @Bean
    public Heartbeat heartbeat(RedisUtil redisUtil) {
        return Heartbeat.getDefHeartbeat(redisUtil);
    }

    @Bean
    @Lazy
    public RestTemplate restTemplate(@Autowired(required = false) RestTemplateBuilder builder) {
        if (builder == null) {
            builder = new RestTemplateBuilder();
        }
        return builder.additionalMessageConverters(new GsonHttpMessageConverter())
            .interceptors(new LoggerIdInterceptor()).build();
    }

    @Bean
    public CompletionService<Void> voidCompletionService() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("void-completion-pool-%d").build();
        ExecutorService executorService =
            new ThreadPoolExecutor(40, 40, 60L, TimeUnit.SECONDS
                    , new LinkedBlockingQueue<>(500), namedThreadFactory);
        return new ExecutorCompletionService<>(executorService);
    }

    @Bean
    public DiscoveryEnabledAdapter discoveryEnabledAdapter() {
        return new NoVersionDiscoveryAdapter();
    }
}
