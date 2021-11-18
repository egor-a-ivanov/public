package org.khovrino.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

final class ParameterizedTypeImpl implements ParameterizedType {

    private final Type ownerType;
    private final Class<?> rawType;
    private final Type[] actualTypeArguments;

    ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type... actualTypeArguments) {
        super();
        this.ownerType = ownerType;
        this.rawType = Objects.requireNonNull(rawType);
        this.actualTypeArguments = Arrays.copyOf(actualTypeArguments, actualTypeArguments.length);
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return Arrays.copyOf(actualTypeArguments, actualTypeArguments.length);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ownerType) ^ Objects.hashCode(rawType) ^ Arrays.hashCode(actualTypeArguments);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ParameterizedType that) {
            return Objects.equals(this.getOwnerType(), that.getOwnerType())
                    && Objects.equals(this.getRawType(), that.getRawType())
                    && Arrays.equals(this.getActualTypeArguments(), that.getActualTypeArguments());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ownerType != null) {
            sb.append(ownerType.getTypeName());
            sb.append("$");
            sb.append(rawType.getSimpleName());
        } else {
            sb.append(rawType.getName());
        }
        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
            sb.append("<");
            boolean first = true;
            for (Type t : actualTypeArguments) {
                if (!first)
                    sb.append(", ");
                sb.append(t.getTypeName());
                first = false;
            }
            sb.append(">");
        }
        return sb.toString();
    }

}
