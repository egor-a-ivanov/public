package org.khovrino.type;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

final class SolutionSet {

    static SolutionSet createFalse() {
        return new SolutionSet(false);
    }

    static SolutionSet createTrue() {
        return new SolutionSet(true);
    }

    static SolutionSet createConst(boolean condition) {
        return new SolutionSet(condition);
    }

    static SolutionSet createUpperBound(TypeVariable<?> variable, Type upperBound) {
        return new SolutionSet(Solution.upperBound(variable, upperBound));
    }

    static SolutionSet createLowerBound(TypeVariable<?> variable, Type lowerBound) {
        return new SolutionSet(Solution.lowerBound(variable, lowerBound));
    }

    private final Set<Solution> cases = new LinkedHashSet<>();

    private SolutionSet(boolean initialValue) {
        if (initialValue) {
            cases.add(Solution.empty());
        }
    }

    private SolutionSet(Solution initialCase) {
        cases.add(initialCase);
    }

    void and(SolutionSet that) {
        List<Solution> ptoduct = new LinkedList<>();
        for (Solution thisCase : this.cases) {
            for (Solution thatCase : that.cases) {
                Solution p = Solution.empty().add(thisCase).add(thatCase);
                ptoduct.add(p);
            }
        }
        this.cases.clear();
        this.cases.addAll(ptoduct);
    }

    void or(SolutionSet that) {
        cases.addAll(that.cases);
    }

    public boolean isEmpty() {
        return cases.isEmpty();
    }

    public boolean isIdentity() {
        for (Solution c : cases) {
            if (c.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public Map<TypeVariable<?>, Type> root() throws NoSuchElementException {
        return cases.stream().findAny().map(Solution::root).orElseThrow();
    }

    @Override
    public int hashCode() {
        return cases.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof SolutionSet that) {
            return this.cases.equals(that.cases);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return root().toString();
    }

}

