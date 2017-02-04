package org.machiev.pojo.mapping;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MapperTest {

    @Test
    public void shouldMapCorrectPojo() throws Exception {
        //given
        CorrectTestPojo pojo = new CorrectTestPojo();
        //when
        Map<String, Object> objectMap = Mapper.toMap(pojo);
        //then
        assertThat(objectMap.size(), is(7));
        assertThat(objectMap.keySet(), hasItems("stringField", "integerField", "intField", "someLong", "o", "staticString", "stringList"));
        assertThat(objectMap.values(), hasItems("text", 42, 256, 789L, null, asList("elem1")));
    }

    @Test
    public void shouldReturnEmptyMapWhenNoFieldsInPojo() throws Exception {
        //given
        EmptyTestPojo pojo = new EmptyTestPojo();
        //when
        Map<String, Object> objectMap = Mapper.toMap(pojo);
        //then
        assertTrue(objectMap.isEmpty());
    }

    @Test
    public void shouldReturnMapWithDeclaredFieldsOnly() throws Exception {
        //given
        SubclassTestPojo pojo = new SubclassTestPojo();
        //when
        Map<String, Object> objectMap = Mapper.toMap(pojo);
        //then
        assertThat(objectMap.size(), is(1));
        assertThat(objectMap.get("subclassField"), is("subclass"));
    }
}

@SuppressWarnings("unused")
class CorrectTestPojo {

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
    private List<String> stringList = asList("elem1");

    private Object unmappedField;
}

class EmptyTestPojo {
}

@SuppressWarnings("unused")
class SubclassTestPojo extends CorrectTestPojo {
    @Mapped
    private String subclassField = "subclass";
}