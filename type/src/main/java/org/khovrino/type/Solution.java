package org.khovrino.type;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class Solution {

    static Solution empty() {
        return new Solution();
    }

    static Solution upperBound(TypeVariable<?> variable, Type upperBound) {
        return Solution.empty().add(variable, VariableSolution.upperBound(upperBound));
    }

    static Solution lowerBound(TypeVariable<?> variable, Type lowerBound) {
        return Solution.empty().add(variable, VariableSolution.lowerBound(lowerBound));
    }

    private final Map<TypeVariable<?>, VariableSolution> boundsMap = new LinkedHashMap<>();

    private Solution() {}

    private Solution add(TypeVariable<?> variable, VariableSolution otherBounds) {
        VariableSolution bounds = boundsMap.get(variable);
        if (bounds == null) {
            bounds = VariableSolution.empty();
            boundsMap.put(variable, bounds);
        }
        bounds.add(otherBounds);
        return this;
    }

    Solution add(Solution that) {
        for (Map.Entry<TypeVariable<?>, VariableSolution> entry : that.boundsMap.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    boolean isEmpty() {
        return boundsMap.isEmpty();
    }

    Map<TypeVariable<?>, Type> root() {
        Map<TypeVariable<?>, Type> result = new LinkedHashMap<>();
        for (Map.Entry<TypeVariable<?>, VariableSolution> entry : boundsMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().root());
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public int hashCode() {
        return boundsMap.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Solution that) {
            return this.boundsMap.equals(that.boundsMap);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return boundsMap.toString();
    }

}
