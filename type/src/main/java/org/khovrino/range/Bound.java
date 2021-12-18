package org.khovrino.range;

import java.util.Objects;

public final class Bound<T extends Comparable<? super T>> implements Comparable<Bound<T>> {

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Bound<T> negativeInfinity() {
        return NEGATIVE_INFINITY;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Bound<T> positiveInfinity() {
        return POSITIVE_INFINITY;
    }

    public static <T extends Comparable<? super T>> Bound<T> before(T point) {
        return new Bound<>(point, false);
    }

    public static <T extends Comparable<? super T>> Bound<T> after(T point) {
        return new Bound<>(point, true);
    }

    public static <T extends Comparable<? super T>> Bound<T> near(T point, boolean after) {
        return new Bound<>(point, after);
    }

    @SuppressWarnings("rawtypes")
    private static final Bound NEGATIVE_INFINITY = new Bound<>(false);

    @SuppressWarnings("rawtypes")
    private static final Bound POSITIVE_INFINITY = new Bound<>(true);

    private final T point;
    private final boolean after;

    private Bound(boolean positiveInfinity) {
        this.point = null;
        this.after = positiveInfinity;
    }

    private Bound(T point, boolean after) {
        this.point = Objects.requireNonNull(point, "point");
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
        return ifNotInfinity(point);
    }

    public boolean isBeforePoint() {
        return ifNotInfinity(!after);
    }

    public boolean isAfterPoint() {
        return ifNotInfinity(after);
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

    private <X> X ifNotInfinity(X arg) {
        if (point == null) throw new IllegalStateException("Infinity");
        return arg;
    }

    @Override
    public int compareTo(Bound<T> that) {
        if (this.point == null && that.point == null) return Boolean.compare(this.after, that.after);
        if (this.point == null) return this.after ? +1 : -1;
        if (that.point == null) return that.after ? -1 : +1;
        int pointDiff = this.point.compareTo(that.point);
        return pointDiff != 0 ? pointDiff : Boolean.compare(this.after, that.after);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        @SuppressWarnings("unchecked") Bound<T> that = (Bound<T>) obj;
        try {
            return this.compareTo(that) == 0;
        } catch (ClassCastException notComparable) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, after);
    }

    @Override
    public String toString() {
        if (point == null) return after ? "+∞" : "-∞";
        return point + (after ? "+0" : "-0");
    }

    public String asLowerBound() {
        if (point == null) return after ? "(+∞" : "(-∞";
        return (after ? "(" : "[") + point;
    }

    public String asUpperBound() {
        if (point == null) return after ? "+∞)" : "-∞)";
        return point + (after ? "]" : ")");
    }
}