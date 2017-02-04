package org.machiev.pojo.mapping;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Mapper {

    public static Map<String, Object> toMap(Object pojo) throws IllegalAccessException, IntrospectionException {
        return toMap(pojo, new HashMap<>());
    }

    public static Map<String, Object> toMap(Object pojo, Map<String, Object> outputMap) throws IllegalAccessException, IntrospectionException {
        Field[] fields = pojo.getClass().getDeclaredFields();

        for (Field field : fields) {
            String mappingKey = getMappingKey(field);
            if (!mappingKey.isEmpty()) {
                Object fieldValue = getFieldValue(field, pojo);
                outputMap.put(mappingKey, fieldValue);
            }
        }

        return outputMap;
    }

    private static String getMappingKey(Field field) {
        Mapped[] mappedAnnotations = field.getAnnotationsByType(Mapped.class);
        if (mappedAnnotations.length == 0) {
            return "";
        }

        if (mappedAnnotations.length > 1) {
            throw new IllegalArgumentException("Incorrectly annotated field");
        }

        String mappingKey = mappedAnnotations[0].keyName();
        return (mappingKey.isEmpty()) ? field.getName() : mappingKey;
    }

    private static Object getFieldValue(Field field, Object pojo) {
        field.setAccessible(true);
        try {
            return field.get(pojo);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Inaccessible or non-instance field", e);
        }
    }

}
