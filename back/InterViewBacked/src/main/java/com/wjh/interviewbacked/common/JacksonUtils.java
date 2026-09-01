package com.wjh.interviewbacked.common;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Jackson 工具类：用于实体与 JSON 字符串互转（笔记 tags、简历 content 整存 JSON）
 */
public class JacksonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("对象序列化为 JSON 失败", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }
}
