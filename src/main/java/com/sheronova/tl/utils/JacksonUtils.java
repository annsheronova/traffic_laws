package com.sheronova.tl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JacksonUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        OBJECT_MAPPER.registerModule(javaTimeModule);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    public static <T> Map<Integer, T> toMap(JsonNode json) {
        try {
            return OBJECT_MAPPER.convertValue(json, new TypeReference<Map<Integer, T>>() {
            });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Can't convert this json.", e);
        }
    }

    public static JsonNode toJsonNodeFromObject(Object value) {
        try {
            return OBJECT_MAPPER.valueToTree(value);
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JsonNode readTreeFromString(String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String toString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The given Json object value: "
                    + value + " cannot be transformed to a String");
        }
    }

    public static <T> List<T> toList(JsonNode jsonNode, TypeReference<List<T>> reference) {
        try {
            return OBJECT_MAPPER.convertValue(jsonNode, reference);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Can't convert this json.", e);
        }
    }

    public static <T> T readValue(JsonNode str, Class<T> c) {
        try {
            return OBJECT_MAPPER.convertValue(str, c);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Can't read this string.", e);
        }
    }
}
