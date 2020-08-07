package com.gaotu.commons.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 类的描述
 *
 * @author sunyue
 * @date 2020/7/1 下午12:27
 */
public class LocalDateDeserializer implements ObjectDeserializer {

    private static final String LINE = "-";
    private static final String COLON = ":";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {

        String value = parser.getLexer().stringVal();
        parser.getLexer().nextToken();
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (value.contains(LINE) || value.contains(COLON)) {
            if (type.equals(LocalDateTime.class)) {
                return (T) LocalDateTime.parse(value, dateTimeFormatter);
            } else if (type.equals(LocalDate.class)) {
                return (T) LocalDate.parse(value, dateTimeFormatter);
            }
        }
        long millisecond = Long.parseLong(value);
        if (type.equals(LocalDateTime.class)) {
            return (T) toLocalDateTime(millisecond);
        } else if (type.equals(LocalDate.class)) {
            return (T) toLocalDateTime(millisecond).toLocalDate();
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    private LocalDateTime toLocalDateTime(long millisecond) {
        return LocalDateTime.ofEpochSecond(millisecond / 1000, 0, ZoneOffset.ofHours(8));
    }

}