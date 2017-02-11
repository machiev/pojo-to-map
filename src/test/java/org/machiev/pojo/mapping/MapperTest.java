package org.machiev.pojo.mapping;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MapperTest {

    @Test
    public void shouldMapCorrectPojo() throws Exception {
        //given
        TestPojo pojo = new TestPojo();
        Mapper mapper = new Mapper.Builder().build();
        //when
        Map<String, Object> objectMap = mapper.toMap(pojo);
        //then
        assertThat(objectMap.size(), is(7));
        assertThat(objectMap.keySet(), hasItems("stringField", "integerField", "intField", "someLong", "o", "staticString", "stringList"));
        assertThat(objectMap.values(), hasItems("text", 42, 256, 789L, null, singletonList("elem1")));
    }

    @Test
    public void shouldReturnEmptyMapWhenNoFieldsInPojo() throws Exception {
        //given
        TestPojo_NoFields pojo = new TestPojo_NoFields();
        Mapper mapper = new Mapper.Builder().build();
        //when
        Map<String, Object> objectMap = mapper.toMap(pojo);
        //then
        assertTrue(objectMap.isEmpty());
    }

    @Test
    public void shouldReturnMapWithDeclaredFieldsOnly() throws Exception {
        //given
        TestPojo_Subclass pojo = new TestPojo_Subclass();
        Mapper mapper = new Mapper.Builder().build();
        //when
        Map<String, Object> objectMap = mapper.toMap(pojo);
        //then
        assertThat(objectMap.size(), is(1));
        assertThat(objectMap.get("subclassField"), is("subclass"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenMapMustSupplyAllValues() throws Exception {
        //given
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("stringField", "some text");
        Mapper mapper = new Mapper.Builder().withMapMustSupplyAllValues().build();
        //when
        mapper.toPojo(fieldsMap, TestPojo.class);
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionWhenPrimitiveIsNull() throws Exception {
        //given
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("intField", null);
        Mapper mapper = new Mapper.Builder().withPrimitiveNotNull().build();
        //when
        mapper.toPojo(fieldsMap, TestPojo.class);
    }

    @Test
    public void shouldConvertNullToPrimitiveDefaultValue() throws Exception {
        //given
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("intField", null);
        Mapper mapper = new Mapper.Builder().build();
        //when
        TestPojo testPojo = mapper.toPojo(fieldsMap, TestPojo.class);
        //then
        assertThat(testPojo.getIntField(), is(0));
    }

    @Test
    public void shouldReturnCorrectlyFilledInPojo() throws Exception {
        //given
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("stringField", "some text");
        fieldsMap.put("integerField", 789987);
        fieldsMap.put("intField", 123321);
        fieldsMap.put("someLong", 111L);
        Mapper mapper = new Mapper.Builder().build();
        //when
        TestPojo testPojo = mapper.toPojo(fieldsMap, TestPojo.class);
        //then
        assertThat(testPojo.getStringField(), is("some text"));
        assertThat(testPojo.getIntegerField(), is(789987));
        assertThat(testPojo.getIntField(), is(123321));
        assertThat(testPojo.getLongField(), is(111L));
        assertNull(testPojo.getO());
    }
}

@SuppressWarnings("unused")
class TestPojo {

    @Mapped
    private String stringField = "text";

    @Mapped
    private Integer integerField = 42;

    @Mapped
    private int intField = 256;

    @Mapped(keyName = "someLong")
    private Long longField = 789L;

    @Mapped
    private Object o;

    @Mapped
    private static String staticString;

    @Mapped
    private List<String> stringList = singletonList("elem1");

    private Object unmappedField;

    public String getStringField() {
        return stringField;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public int getIntField() {
        return intField;
    }

    public Long getLongField() {
        return longField;
    }

    public Object getO() {
        return o;
    }
}

@SuppressWarnings("unused")
class TestPojo_Subclass extends TestPojo {

    @Mapped
    private String subclassField = "subclass";
}

class TestPojo_NoFields {
}
