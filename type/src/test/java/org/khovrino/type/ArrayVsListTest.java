package org.khovrino.type;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArrayVsListTest {

    @Test
    public void arrayTest() {
        Number[] numberArray = {BigInteger.ONE, BigDecimal.TEN, 2, Long.MAX_VALUE};
        Object[] objectArray;
        objectArray = numberArray;
        Consumer<Object[]> objectArrayWriter = a -> a[0] = "Hello!";
        assertEquals(Number.class, objectArray.getClass().componentType());
        assertEquals(BigInteger.class, objectArray[0].getClass());
        assertEquals(BigDecimal.class, objectArray[1].getClass());
        assertEquals(Integer.class, objectArray[2].getClass());
        assertEquals(Long.class, objectArray[3].getClass());
        assertThrows(ArrayStoreException.class, () -> objectArrayWriter.accept(objectArray));
    }

    @Test
    public void listTest() {
        List<?> list = new ArrayList<Number>(Arrays.asList(BigInteger.ONE, BigDecimal.TEN, 2, Long.MAX_VALUE));
        list.add(null);
        assertEquals(ArrayList.class, list.getClass());
        assertFalse(list.isEmpty());
        assertEquals(5, list.size());
        assertEquals(BigInteger.class, list.get(0).getClass());
        assertEquals(BigDecimal.class, list.get(1).getClass());
        assertEquals(Integer.class, list.get(2).getClass());
        assertEquals(Long.class, list.get(3).getClass());
        assertNull(list.get(4));
        assertThrows(ClassCastException.class, () -> {
            @SuppressWarnings("unchecked")
            List<String> stringList = (List<String>)list;
            stringList.add("Hello!");
            @SuppressWarnings("unchecked")
            List<Number> numberList = (List<Number>)list;
            Number n = numberList.get(5);
            System.out.println(n);
        });
    }
}
