package org.machiev.pojo.mapping;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Mapper {

    private boolean primitiveAcceptsNull;
    private boolean mapMustSupplyAllValues;

    private static Map<Class<?>, Object> typeToDefaultValue = new HashMap<>();
    static {
        typeToDefaultValue.put(boolean.class, false);
        typeToDefaultValue.put(byte.class, 0);
        typeToDefaultValue.put(short.class, 0);
        typeToDefaultValue.put(int.class, 0);
        typeToDefaultValue.put(long.class, 0L);
        typeToDefaultValue.put(float.class, 0.0f);
        typeToDefaultValue.put(double.class, 0.0);
    }

    public static class Builder {
        private boolean primitiveAcceptsNull = true;
        private boolean mapMustSupplyAllValues;

        public Builder withPrimitiveNotNull() {
            primitiveAcceptsNull = false;
            return this;
        }

        public Builder withMapMustSupplyAllValues() {
            mapMustSupplyAllValues = true;
            return this;
        }

        public Mapper build() {
            return new Mapper(primitiveAcceptsNull, mapMustSupplyAllValues);
        }
    }

    private Mapper(boolean primitiveAcceptsNull, boolean mapMustSupplyAllValues) {
        this.primitiveAcceptsNull = primitiveAcceptsNull;
        this.mapMustSupplyAllValues = mapMustSupplyAllValues;
    }

    public Map<String, Object> toMap(Object pojo) throws IllegalAccessException, IntrospectionException {
        return toMap(pojo, new HashMap<>());
    }

    public Map<String, Object> toMap(Object pojo, Map<String, Object> outputMap) throws IllegalAccessException, IntrospectionException {
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
    public <T> T toPojo(Map<String, Object> fieldsMap, Class<T> pojoClass) {
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
     * @throws IllegalArgumentException if map does not contain a value for a mapped field and mapMustSupplyAllValues is true.
     */
    public <T> T toPojo(Map<String, Object> fieldsMap, T outputPojo) {
        Field[] fields = outputPojo.getClass().getDeclaredFields();

        for (Field field : fields) {
            String mappingKey = getMappingKey(field);
            if (!mappingKey.isEmpty()) {
                Object value = fieldsMap.get(mappingKey);
                checkSuppliedValue(value, fieldsMap, mappingKey);
                setFieldValue(field, outputPojo, value);
            }
        }
        return outputPojo;
    }

    private void checkSuppliedValue(Object value, Map<String, Object> fieldsMap, String mappingKey) {
        if (value == null && mapMustSupplyAllValues && !fieldsMap.containsKey(mappingKey)) {
            throw new IllegalArgumentException("Map does not supply a value for field's key: " + mappingKey);
        }
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

    private void setFieldValue(Field field, Object outputPojo, Object value) {
        field.setAccessible(true);
        try {
            Object convertedValue = convertIfNeeded(field, value);
            field.set(outputPojo, convertedValue);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot set field's value", e);
        }
    }

    private Object convertIfNeeded(Field field, Object value) {
        if (value == null && primitiveAcceptsNull && field.getType().isPrimitive()) {
            return typeToDefaultValue.get(field.getType());
        }
        return value;
    }

}
