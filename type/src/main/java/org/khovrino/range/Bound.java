package org.khovrino.range;

import java.util.Objects;

public final class Bound<T extends Comparable<? super T>> implements Comparable<Bound<T>> {

    public static <T extends Comparable<? super T>> Bound<T> before(T point) {
        return near(point, false);
    }

    public static <T extends Comparable<? super T>> Bound<T> after(T point) {
        return near(point, true);
    }

    public static <T extends Comparable<? super T>> Bound<T> near(T point, boolean after) {
        return Discrete.adapter(point)
                .map(adapter -> after
                        ? adapter.nextValue(point).map(next -> new Bound<>(next, false)).orElse(positiveInfinity())
                        : adapter.prevValue(point).map(prev -> new Bound<>(point, false)).orElse(negativeInfinity()))
                .orElseGet(() -> new Bound<>(point, after));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Bound<T> negativeInfinity() {
        return NEGATIVE_INFINITY;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Bound<T> positiveInfinity() {
        return POSITIVE_INFINITY;
    }

    @SuppressWarnings("rawtypes")
    private static final Bound NEGATIVE_INFINITY = new Bound<>(null, false);

    @SuppressWarnings("rawtypes")
    private static final Bound POSITIVE_INFINITY = new Bound<>(null, true);

    private final T point;
    private final boolean after;

    private Bound(T point, boolean after) {
        this.point = point;
        this.after = after;
    }

    public boolean isInfinity() {
        return point == null;
    }

    public boolean isNegativeInfinity() {
        return point == null && !after;
    }

    public boolean isPositiveInfinity() {
        return point == null && after;
    }

    public T point() {
        return requireNonInfinity().point;
    }

    public boolean isBeforePoint() {
        return !requireNonInfinity().after;
    }

    public boolean isAfterPoint() {
        return requireNonInfinity().after;
    }

    public boolean isBefore(T point) {
        if (this.point == null) return !after;
        int pointDiff = this.point.compareTo(point);
        return pointDiff < 0 || pointDiff == 0 && !after;
    }

    public boolean isAfter(T point) {
        if (this.point == null) return after;
        int pointDiff = this.point.compareTo(point);
        return pointDiff > 0 || pointDiff == 0 && after;
    }

    public Bound<T> requireNonInfinity() {
        if (point == null) throw new IllegalStateException("Infinity");
        return this;
    }

    @Override
    public int compareTo(Bound<T> that) {
        if (this.point == null) {
            if (that.point == null) return Boolean.compare(this.after, that.after);
            return this.after ? +1 : -1;
        }
        if (that.point == null) {
            return that.after ? -1 : +1;
        }
        int pointDiff = this.point.compareTo(that.point);
        if (pointDiff != 0) return pointDiff;
        return Boolean.compare(this.after, that.after);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Bound) {
            @SuppressWarnings("unchecked") Bound<T> that = (Bound<T>) obj;
            try {
                return this.compareTo(that) == 0;
            } catch (ClassCastException notComparable) {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, after);
    }

    @Override
    public String toString() {
        if (point == null) return after ? "+∞" : "-∞";
        return point + (after ? "+δ" : "-δ");
    }

    public String asLowerBound() {
        if (point == null) return after ? "(+∞" : "(-∞";
        return (after ? "(" : "[") + point;
    }

    public String asUpperBound() {
        if (point == null) return after ? "+∞)" : "-∞)";
        return Discrete.adapter(point)
                .map(adapter -> adapter.prevValue(point).map(prev -> prev + "]")
                        .orElseThrow(IllegalStateException::new))
                .orElseGet(() -> point + (after ? "]" : ")"));
    }
}