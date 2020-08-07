package com.baijia.gaotu.channel.config.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 类的描述
 *
 * @author sunyue
 * @date 2020/7/1 上午11:12
 */
public class RpcTimeDeserializer {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final static DateTimeFormatter DEFAULT_FORMATTER_23 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final static DateTimeFormatter FORMATTER_DT19_TW = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final static DateTimeFormatter FORMATTER_D8 = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final static DateTimeFormatter FORMATTER_D10 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static DateTimeFormatter FORMATTER_D10_TW = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final static DateTimeFormatter ISO_FIXED_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static LocalDateTimeDeserializer localDateTime() {
        return new LocalDateTimeDeserializer();
    }

    public static LocalDateDeserializer localDate() {
        return new LocalDateDeserializer();
    }

    public static DateDeserializer date() {
        return new DateDeserializer();
    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonToken token = jsonParser.getCurrentToken();
            if (token.isNumeric()) {
                long millisecond = jsonParser.getLongValue();
                return toLocalDateTime(millisecond);
            }
            switch (token) {
                case VALUE_STRING:
                    String patternValue = jsonParser.getText();
                    if (patternValue.length() == 0) {
                        return null;
                    }
                    return Parser.parse(patternValue, LocalDateTime.class);
                case VALUE_NULL:
                default:
                    return null;
            }
        }

        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }

    }

    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {


        @Override
        public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonToken token = jsonParser.getCurrentToken();
            if (token.isNumeric()) {
                Long millisecond = jsonParser.getLongValue();
                return toLocalDateTime(millisecond).toLocalDate();
            }
            switch (token) {
                case VALUE_STRING:
                    String patternValue = jsonParser.getText();
                    if (patternValue.length() == 0) {
                        return null;
                    }
                    return Parser.parse(patternValue, LocalDate.class);
                case VALUE_NULL:
                default:
                    return null;
            }
        }

        @Override
        public Class<LocalDate> handledType() {
            return LocalDate.class;
        }

    }

    public static class DateDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonToken token = jsonParser.getCurrentToken();
            if (token.isNumeric()) {
                Long millisecond = jsonParser.getLongValue();
                return new Date(millisecond);
            }
            switch (token) {
                case VALUE_STRING:
                    String patternValue = jsonParser.getText();
                    if (patternValue.length() == 0) {
                        return null;
                    }
                    LocalDateTime localDateTime = Parser.parse(patternValue, LocalDateTime.class);
                    ZonedDateTime zdt = localDateTime.atZone(ZoneOffset.ofHours(8));
                    return Date.from(zdt.toInstant());
                case VALUE_NULL:
                default:
                    return null;
            }
        }

        @Override
        public Class<LocalDate> handledType() {
            return LocalDate.class;
        }

    }

    @SuppressWarnings("all")
    private static class Parser {

        static <T> T parse(String text, Class<T> type) {
            if (type == LocalDateTime.class) {
                LocalDateTime localDateTime;
                if (text.length() == 8 || text.length() == 10) {
                    LocalDate localDate = parseLocalDate(text);
                    localDateTime = LocalDateTime.of(localDate, LocalTime.MIN);
                } else {
                    localDateTime = parseLocalDateTime(text);
                }
                return (T) localDateTime;
            } else if (type == LocalDate.class) {
                LocalDate localDate;
                if (text.length() == 19 || text.length() == 23) {
                    LocalDateTime localDateTime = parseLocalDateTime(text);
                    localDate = localDateTime.toLocalDate();
                } else {
                    localDate = parseLocalDate(text);
                }

                return (T) localDate;
            }
            return null;
        }

        private final static LocalDateTime parseLocalDateTime(String text) {
            DateTimeFormatter formatter = null;
            // 19 - yyyy-MM-dd HH:mm:ss | yyyy/MM/dd HH:mm:ss | yyyy-MM-ddTHH:mm:ss | yyyy/MM/ddTHH:mm:ss
            if (text.length() == 19) {
                char c4 = text.charAt(4);
                char c7 = text.charAt(7);
                char c10 = text.charAt(10);
                char c13 = text.charAt(13);
                char c16 = text.charAt(16);
                if (c13 == ':' && c16 == ':') {
                    if (c4 == '-' && c7 == '-') {
                        if (c10 == 'T') {
                            formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                        } else if (c10 == ' ') {
                            formatter = ISO_FIXED_FORMAT;
                        }
                    } else if (c4 == '/' && c7 == '/') {
                        formatter = FORMATTER_DT19_TW;
                    }
                }
            }
            // 23 - yyyy-MM-dd HH:mm:ss.SSS
            else if (text.length() == 23) {
                char c4 = text.charAt(4);
                char c7 = text.charAt(7);
                char c10 = text.charAt(10);
                char c13 = text.charAt(13);
                char c16 = text.charAt(16);
                char c19 = text.charAt(19);

                if (c13 == ':' && c16 == ':'
                        && c4 == '-' && c7 == '-'
                        && c10 == ' '
                        && c19 == '.') {
                    formatter = DEFAULT_FORMATTER_23;
                }
            }

            return formatter == null ? LocalDateTime.parse(text) : LocalDateTime.parse(text, formatter);
        }

        private final static LocalDate parseLocalDate(String text) {
            DateTimeFormatter formatter = null;
            // 8 - yyyyMMdd
            if (text.length() == 8) {
                formatter = FORMATTER_D8;
            }

            // 10 - yyyy-MM-dd | yyyy/MM/dd
            if (text.length() == 10) {
                char c4 = text.charAt(4);
                char c7 = text.charAt(7);
                if (c4 == '-' && c7 == '-') {
                    formatter = FORMATTER_D10;
                } else if (c4 == '/' && c7 == '/') {
                    formatter = FORMATTER_D10_TW;
                }
            }

            return formatter == null ? LocalDate.parse(text) : LocalDate.parse(text, formatter);
        }
    }

    private static LocalDateTime toLocalDateTime(long millisecond) {
        return LocalDateTime.ofEpochSecond(millisecond / 1000, 0, ZoneOffset.ofHours(8));
    }

}
