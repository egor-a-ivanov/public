package org.khovrino.range;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Month;

import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static java.time.Month.SEPTEMBER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.khovrino.range.Bound.after;
import static org.khovrino.range.Bound.before;
import static org.khovrino.range.Bound.negativeInfinity;
import static org.khovrino.range.Bound.positiveInfinity;

public class IntervalTest {

    @Test
    public void intervalTest() {

        assertEquals(Enum.class, Discrete.adapter(Month.class).map(Discrete.Adapter::type).orElseThrow(IllegalStateException::new));
        assertEquals(Interval.unbounded(), Interval.of(before(JANUARY), after(DECEMBER)));
        assertEquals(Interval.of(before(JUNE), after(AUGUST)), Interval.of(after(MAY), before(SEPTEMBER)));

        assertTrue(before(JANUARY).isInfinity());
        assertTrue(after(DECEMBER).isInfinity());

        assertEquals(after(4), before(5));

        assertTrue(Interval.unbounded().isUnbounded());
        assertTrue(Interval.<Integer>unbounded().contains(5));

        assertFalse(Interval.of(before(3), positiveInfinity()).contains(2));
        assertTrue(Interval.of(before(3), positiveInfinity()).contains(3));
        assertTrue(Interval.of(negativeInfinity(), after(5)).contains(5));
        assertFalse(Interval.of(negativeInfinity(), after(5)).contains(6));

        assertFalse(Interval.of(before(3), after(5)).contains(2));
        assertTrue(Interval.of(before(3), after(5)).contains(3));
        assertTrue(Interval.of(before(3), after(5)).contains(4));
        assertTrue(Interval.of(before(3), after(5)).contains(5));
        assertFalse(Interval.of(before(3), after(5)).contains(6));

        assertFalse(Interval.of(after(3), before(5)).contains(3));
        assertTrue(Interval.of(after(3), before(5)).contains(4));
        assertFalse(Interval.of(after(3), before(5)).contains(5));

        assertFalse(Interval.of(after(3.0), before(5.0)).contains(3.0));
        assertTrue(Interval.of(after(3.0), before(5.0)).contains(3.1));
        assertTrue(Interval.of(after(3.0), before(5.0)).contains(4.0));
        assertTrue(Interval.of(after(3.0), before(5.0)).contains(4.9));
        assertFalse(Interval.of(after(3.0), before(5.0)).contains(5.0));

        assertNotEquals(Interval.unbounded(), new Object());
        assertNotEquals(Interval.of(before(3), before(5)).lowerBound(), new Object());
        assertNotEquals(Interval.of(before(3), before(5)), Interval.of(before(3.0), before(5.0)));
        assertEquals(6, Interval.of(before(3), after(5)).upperBound().point());
        assertTrue(Interval.of(before(3), after(5)).upperBound().isBeforePoint());
        assertEquals(3, Interval.of(after(2), before(6)).lowerBound().point());
        assertFalse(Interval.of(after(2), before(6)).lowerBound().isAfterPoint());
        assertEquals(Interval.of(before(3), after(5)), Interval.of(after(2), before(6)));
        assertEquals(Interval.of(before(3), after(5)), Interval.of(after(2), before(6)));

    }

    @SuppressWarnings("unchecked")
    private static void assertEquals(Object expected, Object actual) {
        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expected.hashCode(), actual.hashCode());
        Assertions.assertEquals(expected.toString(), actual.toString());
        if (expected instanceof Comparable && actual instanceof Comparable) {
            if (expected.getClass().isAssignableFrom(actual.getClass())) {
                Assertions.assertEquals(0, ((Comparable<Object>) expected).compareTo(actual));
            }
            if (actual.getClass().isAssignableFrom(expected.getClass())) {
                Assertions.assertEquals(0, ((Comparable<Object>) actual).compareTo(expected));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertNotEquals(Object expected, Object actual) {
        Assertions.assertNotEquals(expected, actual);
        Assertions.assertNotEquals(expected.toString(), actual.toString());
        if (expected instanceof Comparable && actual instanceof Comparable) {
            int diff1 = 0;
            int diff2 = 0;
            if (expected.getClass().isAssignableFrom(actual.getClass())) {
                diff1 = ((Comparable<Object>) expected).compareTo(actual);
                Assertions.assertNotEquals(0, diff1);
            }
            if (actual.getClass().isAssignableFrom(expected.getClass())) {
                diff2 = ((Comparable<Object>) actual).compareTo(expected);
                Assertions.assertNotEquals(0, ((Comparable<Object>) actual).compareTo(expected));
            }
            if (actual.getClass().equals(expected.getClass())) {
                Assertions.assertTrue((diff1 < 0) ^ (diff2 < 0));
            }
        }
    }

}
