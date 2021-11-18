package org.khovrino.type;

import java.lang.reflect.GenericArrayType;
import java.util.Objects;

final class GenericArrayTypeImpl implements GenericArrayType {

    private final java.lang.reflect.Type genericComponentType;

    public GenericArrayTypeImpl(java.lang.reflect.Type genericComponentType) {
        this.genericComponentType = Objects.requireNonNull(genericComponentType);
    }

    @Override
    public java.lang.reflect.Type getGenericComponentType() {
        return genericComponentType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(genericComponentType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof GenericArrayType that) {
            return this.genericComponentType.equals(that.getGenericComponentType());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return genericComponentType.getTypeName() + "[]";
    }

}