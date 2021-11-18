package org.khovrino.type;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

final class VariableSolution {

    static VariableSolution empty() {
        return new VariableSolution();
    }

    static VariableSolution upperBound(Type upperBound) {
        return VariableSolution.empty().addUpperBound(upperBound);
    }

    static VariableSolution lowerBound(Type lowerBound) {
        return VariableSolution.empty().addLowerBound(lowerBound);
    }

    private final Set<Type> upperBounds = new LinkedHashSet<>();
    private final Set<Type> lowerBounds = new LinkedHashSet<>();

    private VariableSolution() {}

    private VariableSolution addUpperBound(Type newType) {
        Set<Type> useless = new HashSet<>();
        for (Type type : upperBounds) {
            if (JavaType.testExtends(type, newType)) {
                return this;
            } else if (JavaType.testExtends(newType, type)) {
                useless.add(type);
            }
        }
        upperBounds.removeAll(useless);
        upperBounds.add(newType);
        return this;
    }

    private VariableSolution addLowerBound(Type newType) {
        Set<Type> useless = new HashSet<>();
        for (Type type : lowerBounds) {
            if (JavaType.testExtends(newType, type)) {
                return this;
            } else if (JavaType.testExtends(type, newType)) {
                useless.add(type);
            }
        }
        lowerBounds.removeAll(useless);
        lowerBounds.add(newType);
        return this;
    }

    void add(VariableSolution that) {
        for (Type type : that.upperBounds) {
            addUpperBound(type);
        }
        for (Type type : that.lowerBounds) {
            addLowerBound(type);
        }
    }

    Type root() {
        // more about inference
        // https://docs.oracle.com/javase/specs/jls/se8/html/jls-18.html
        if (lowerBounds.size() == 1) {
            for (Type exact : lowerBounds) {
                return exact;
            }
        } else if (upperBounds.size() == 1) {
            for (Type exact : upperBounds) {
                return exact;
            }
        } else if (upperBounds.isEmpty()) {
            return Object.class;
        }
        throw new IllegalStateException(
                String.format("Unresolvable bounds: extends %s and super %s", upperBounds, lowerBounds));
    }

    @Override
    public int hashCode() {
        return upperBounds.hashCode() ^ lowerBounds.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof VariableSolution that) {
            return this.upperBounds.equals(that.upperBounds) && this.lowerBounds.equals(that.lowerBounds);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return root().getTypeName();
    }

}
