package org.khovrino.type;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

final class WildcardTypeImpl implements WildcardType {

    static final WildcardTypeImpl UNBOUNDED = new WildcardTypeImpl(new Type[0], new Type[0]);

    static WildcardTypeImpl sup(Type... lowerBounds) {
        return new WildcardTypeImpl(lowerBounds, new Type[0]);
    }

    static WildcardTypeImpl ext(Type... upperBounds) {
        return new WildcardTypeImpl(new Type[0], upperBounds);
    }

    private final Type[] lowerBounds;
    private final Type[] upperBounds;

    WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
        super();
        this.lowerBounds = Arrays.copyOf(lowerBounds, lowerBounds.length);
        this.upperBounds = Arrays.copyOf(upperBounds, upperBounds.length);
    }

    @Override
    public Type[] getUpperBounds() {
        return Arrays.copyOf(upperBounds, upperBounds.length);
    }

    @Override
    public Type[] getLowerBounds() {
        return Arrays.copyOf(lowerBounds, lowerBounds.length);
    }

    @Override
    public String toString() {
        Type[] bounds;
        StringBuilder sb = new StringBuilder();
        if (lowerBounds.length > 0) {
            bounds = lowerBounds;
            sb.append("? super ");
        } else {
            if (upperBounds.length > 0 && !(upperBounds.length == 1 && upperBounds[0].equals(Object.class))) {
                bounds = upperBounds;
                sb.append("? extends ");
            } else {
                sb.append("?");
                bounds = null;
            }
        }
        if (bounds != null) {
            boolean first = true;
            for (Type bound : bounds) {
                if (!first) {
                    sb.append(" & ");
                } else {
                    first = false;
                }
                sb.append(bound.getTypeName());
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unused")
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof WildcardType that) {
            return Arrays.equals(lowerBounds, that.getLowerBounds())
                    && Arrays.equals(upperBounds, that.getUpperBounds());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(lowerBounds) ^ Arrays.hashCode(upperBounds);
    }

}
