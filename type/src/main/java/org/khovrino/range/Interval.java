package org.khovrino.range;

import java.util.Objects;

public final class Interval<T extends Comparable<? super T>> {

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Interval<T> empty() {
        return EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Interval<T> universe() {
        return UNIVERSE;
    }

    public static <T extends Comparable<? super T>> Interval<T> from(T point, boolean inclusive) {
        return new Interval<>(Bound.near(point, !inclusive), Bound.positiveInfinity());
    }

    public static <T extends Comparable<? super T>> Interval<T> to(T point, boolean inclusive) {
        return new Interval<>(Bound.negativeInfinity(), Bound.near(point, inclusive));
    }

    public static <T extends Comparable<? super T>> Interval<T> of(T fromPoint, boolean fromInclusive, T toPoint, boolean toInclusive) {
        return new Interval<>(Bound.near(fromPoint, !fromInclusive), Bound.near(toPoint, toInclusive));
    }

    public static <T extends Comparable<? super T>> Interval<T> of(Bound<T> from, Bound<T> to) {
        return new Interval<>(from, to);
    }

    @SuppressWarnings("rawtypes")
    private static final Interval EMPTY = new Interval();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Interval UNIVERSE = new Interval(Bound.negativeInfinity(), Bound.positiveInfinity());

    private final Bound<T> lowerBound;
    private final Bound<T> upperBound;

    private Interval() {
        this.lowerBound = Bound.negativeInfinity();
        this.upperBound = Bound.negativeInfinity();
    }

    private Interval(Bound<T> lowerBound, Bound<T> upperBound) {
        this.lowerBound = Objects.requireNonNull(lowerBound, "lowerBound");
        this.upperBound = Objects.requireNonNull(upperBound, "upperBound");
        if (lowerBound.compareTo(upperBound) >= 0) throw new IllegalArgumentException("lower bound must be less then upper bound");
    }

    public boolean isEmpty() {
        return upperBound.isNegativeInfinity();
    }

    private boolean isUniverse() {
        return lowerBound.isNegativeInfinity() && upperBound.isPositiveInfinity();
    }

    private boolean isSingleton() {
        return !lowerBound.isInfinity() && lowerBound.point().equals(upperBound.point());
    }

    public Bound<T> from() {
        if (isEmpty()) throw new IllegalStateException("Empty interval");
        return lowerBound;
    }

    public Bound<T> to() {
        if (isEmpty()) throw new IllegalStateException("Empty interval");
        return upperBound;
    }

    public T fromPoint() {
        return from().point();
    }

    public boolean fromInclusive() {
        return !from().isAfterPoint();
    }

    public T toPoint() {
        return to().point();
    }

    public boolean toInclusive() {
        return to().isAfterPoint();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Interval<?> that = (Interval<?>) obj;
        return lowerBound.equals(that.lowerBound) && upperBound.equals(that.upperBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    @Override
    public String toString() {
        if (isEmpty()) return "∅";
        return "Interval{" +
                "lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                '}';
    }
}
