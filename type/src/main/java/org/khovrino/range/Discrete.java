package org.khovrino.range;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Discrete<T extends Discrete<? super T> & Comparable<? super T>> extends Comparable<T> {

    boolean isMinimumDiscreteValue();

    boolean isMaximumDiscreteValue();

    T getPrevDiscreteValue();

    T getNextDiscreteValue();

    @SuppressWarnings("unchecked")
    static <T extends Comparable<? super T>> Optional<Adapter<T>> adapter(T object) {
        return adapter((Class<T>) object.getClass());
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<? super T>> Optional<Adapter<T>> adapter(Class<T> type) {
        Adapter<T> adapter = null;
        for (Adapter<?> a : Adapter.MAP.values()) {
            if (a.type.isAssignableFrom(type)) {
                if (adapter == null || adapter.type.isAssignableFrom(a.type)) adapter = (Adapter<T>) a;
            }
        }
        return Optional.ofNullable(adapter);
    }

    static <T extends Comparable<? super T>> void addAdapter(Class<T> type, T minValue, T maxValue, UnaryOperator<T> prevValueOperator, UnaryOperator<T> nextValueOperator) {
        Predicate<T> minValuePredicate = minValue == null ? t -> false : minValue::equals;
        Predicate<T> maxValuePredicate = maxValue == null ? t -> false : maxValue::equals;
        Adapter<T> adapter = new Adapter<>(type, minValuePredicate, maxValuePredicate, prevValueOperator, nextValueOperator);
        Adapter.MAP.putIfAbsent(type, adapter);
    }

    final class Adapter<T extends Comparable<? super T>> {

        private static final Year YEAR_MIN_VALUE = Year.of(Year.MIN_VALUE);
        private static final Year YEAR_MAX_VALUE = Year.of(Year.MAX_VALUE);
        private static final YearMonth YEAR_MONTH_MIN_VALUE = YearMonth.of(Year.MIN_VALUE, Month.JANUARY);
        private static final YearMonth YEAR_MONTH_MAX_VALUE = YearMonth.of(Year.MAX_VALUE, Month.DECEMBER);
        private static final LocalDate LOCAL_DATE_MIN_VALUE = LocalDate.of(Year.MIN_VALUE, Month.JANUARY, 1);
        private static final LocalDate LOCAL_DATE_MAX_VALUE = LocalDate.of(Year.MAX_VALUE, Month.DECEMBER, 31);

        private static final Map<Class<?>, Adapter<?>> MAP = new ConcurrentHashMap<>();

        static {
            MAP.put(Discrete.class, new Adapter<>(
                    Discrete.class,
                    Discrete::isMinimumDiscreteValue,
                    Discrete::isMaximumDiscreteValue,
                    Discrete::getPrevDiscreteValue,
                    Discrete::getNextDiscreteValue));

            MAP.put(Enum.class, new Adapter<>(
                    Enum.class,
                    t -> t.ordinal() == 0,
                    t -> t.ordinal() == t.getClass().getEnumConstants().length - 1,
                    t -> t.getClass().getEnumConstants()[t.ordinal() - 1],
                    t -> t.getClass().getEnumConstants()[t.ordinal() + 1]));

            Discrete.addAdapter(Boolean.class, Boolean.FALSE, Boolean.TRUE, t -> !t, t -> !t);
            Discrete.addAdapter(Character.class, Character.MIN_VALUE, Character.MAX_VALUE, t -> (char) (t - 1), t -> (char) (t + 1));
            Discrete.addAdapter(Byte.class, Byte.MIN_VALUE, Byte.MAX_VALUE, t -> (byte) (t - 1), t -> (byte) (t + 1));
            Discrete.addAdapter(Short.class, Short.MIN_VALUE, Short.MAX_VALUE, t -> (short) (t - 1), t -> (short) (t + 1));
            Discrete.addAdapter(Integer.class, Integer.MIN_VALUE, Integer.MAX_VALUE, t -> t - 1, t -> t + 1);
            Discrete.addAdapter(Long.class, Long.MIN_VALUE, Long.MAX_VALUE, t -> t - 1, t -> t + 1);
            Discrete.addAdapter(BigInteger.class, null, null, t -> t.subtract(BigInteger.ONE), t -> t.add(BigInteger.ONE));
            Discrete.addAdapter(Year.class, YEAR_MIN_VALUE, YEAR_MAX_VALUE, t -> t.minusYears(1), t -> t.plusYears(1));
            Discrete.addAdapter(YearMonth.class, YEAR_MONTH_MIN_VALUE, YEAR_MONTH_MAX_VALUE, t -> t.minusMonths(1), t -> t.plusMonths(1));
            Discrete.addAdapter(LocalDate.class, LOCAL_DATE_MIN_VALUE, LOCAL_DATE_MAX_VALUE, t -> t.minusDays(1), t -> t.plusDays(1));
        }

        private final Class<T> type;
        private final Predicate<T> minValuePredicate;
        private final Predicate<T> maxValuePredicate;
        private final UnaryOperator<T> prevValueOperator;
        private final UnaryOperator<T> nextValueOperator;

        private Adapter(Class<T> type, Predicate<T> minValuePredicate, Predicate<T> maxValuePredicate, UnaryOperator<T> prevValueOperator, UnaryOperator<T> nextValueOperator) {
            this.type = type;
            this.minValuePredicate = minValuePredicate;
            this.maxValuePredicate = maxValuePredicate;
            this.prevValueOperator = prevValueOperator;
            this.nextValueOperator = nextValueOperator;
        }

        public Class<T> type() {
            return type;
        }

        public boolean isMinValue(T value) {
            return minValuePredicate.test(value);
        }

        public boolean isMaxValue(T value) {
            return maxValuePredicate.test(value);
        }

        public Optional<T> prevValue(T value) {
            return isMinValue(value) ? Optional.empty() : Optional.of(prevValueOperator.apply(value));
        }

        public Optional<T> nextValue(T value) {
            return isMaxValue(value) ? Optional.empty() : Optional.of(nextValueOperator.apply(value));
        }
    }
}
