package org.machiev.pojo.mapping;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    /**
     * Creates object of specified class and sets fields to values contained in a map.
     * @param fieldsMap map containing field's mapped name and field's value.
     * @param pojoClass object's class.
     * @return created and filled instance of supplied class.
     */
    public static <T> T toPojo(Map<String, Object> fieldsMap, Class<T> pojoClass) {
        try {
            Constructor<T> constructor = pojoClass.getDeclaredConstructor();
            return toPojo(fieldsMap, constructor.newInstance());
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot create an object", e);
        }
    }

    /**
     * Sets fields of supplied object to values contained in a map.
     * @param fieldsMap map containing field's mapped name and field's value.
     * @param outputPojo object to be filled in.
     * @return filled supplied object.
     */
    public static <T> T toPojo(Map<String, Object> fieldsMap, T outputPojo) {
        Field[] fields = outputPojo.getClass().getDeclaredFields();

        for (Field field : fields) {
            String mappingKey = getMappingKey(field);
            if (!mappingKey.isEmpty()) {
                Object value = fieldsMap.get(mappingKey);
                //TODO: add check if map contains a value
                setFieldValue(field, outputPojo, value);
            }
        }
        return outputPojo;
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

    private static void setFieldValue(Field field, Object outputPojo, Object value) {
        field.setAccessible(true);
        try {
            //TODO: handle primitive types - null case
            field.set(outputPojo, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot set field's value", e);
        }
    }

}
