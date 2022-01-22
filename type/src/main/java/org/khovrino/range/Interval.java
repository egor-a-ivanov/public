package org.khovrino.range;

import java.util.Objects;

public final class Interval<T extends Comparable<? super T>> {

    public static <T extends Comparable<? super T>> Interval<T> of(Bound<T> lowerBound, Bound<T> upperBound) {
        Objects.requireNonNull(lowerBound, "lowerBound");
        Objects.requireNonNull(upperBound, "upperBound");
        if (lowerBound.compareTo(upperBound) >= 0) throw new IllegalArgumentException("lower bound must be less then upper bound");
        if (lowerBound.isNegativeInfinity() && upperBound.isPositiveInfinity()) return unbounded();
        return new Interval<>(lowerBound, upperBound);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Interval<T> unbounded() {
        return UNBOUNDED;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Interval UNBOUNDED = new Interval(Bound.negativeInfinity(), Bound.positiveInfinity());

    private final Bound<T> lowerBound;
    private final Bound<T> upperBound;

    private Interval(Bound<T> lowerBound, Bound<T> upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Bound<T> lowerBound() {
        return lowerBound;
    }

    public Bound<T> upperBound() {
        return upperBound;
    }

    public boolean isUnbounded() {
        return lowerBound.isNegativeInfinity() && upperBound.isPositiveInfinity();
    }

    public boolean contains(T point) {
        return lowerBound.isBefore(point) && upperBound.isAfter(point);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Interval<?> that) return lowerBound.equals(that.lowerBound) && upperBound.equals(that.upperBound);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    @Override
    public String toString() {
        //if (isEmpty()) return "∅";
        return lowerBound.asLowerBound() + ", " + upperBound.asUpperBound();
    }
}
