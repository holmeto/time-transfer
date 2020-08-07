package com.gaotu.user.feignclient.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * 类的描述
 *
 * @author sunyue
 * @date 2020/7/1 上午11:12
 */
public class TimeSerializer {

    public static LocalDateTimeSerializer localDateTime() {
        return new LocalDateTimeSerializer();
    }

    public static LocalDateSerializer localDate() {
        return new LocalDateSerializer();
    }

    public static DateSerializer date() {
        return new DateSerializer();
    }

    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            long millisecond = localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            jsonGenerator.writeString(String.valueOf(millisecond));
        }

        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }

    }

    public static class LocalDateSerializer extends JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {
            long millisecond = localDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
            jsonGenerator.writeString(String.valueOf(millisecond));
        }

        @Override
        public Class<LocalDate> handledType() {
            return LocalDate.class;
        }

    }

    public static class DateSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {
            long millisecond = date.getTime();
            jsonGenerator.writeString(String.valueOf(millisecond));
        }

        @Override
        public Class<Date> handledType() {
            return Date.class;
        }

    }

}
