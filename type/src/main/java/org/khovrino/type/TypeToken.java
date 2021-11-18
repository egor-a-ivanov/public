package org.khovrino.type;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class TypeToken<T> {

    public static <T> TypeToken<T> of(Class<T> c) {
        return fromJavaType(c);
    }

    private static <T> TypeToken<T> fromJavaType(Type javaType) {
        return new TypeToken<>(javaType) {};
    }

    private final Type javaType;

    private TypeToken(Type type) {
        this.javaType = JavaType.requireNonRawClass(Objects.requireNonNull(type));
    }

    protected TypeToken() {
        this.javaType = JavaType.requireNonRawClass(Objects.requireNonNull(
                JavaType.typeArguments(JavaType.downgrade(this.getClass(), TypeToken.class))
                        .get(TypeToken.class.getTypeParameters()[0])));
    }

    @SuppressWarnings("unchecked")
    public final T newInstance() {
        return (T) JavaType.newInstance(javaType);
    }

    public final String name() {
        return javaType.getTypeName();
    }

    public final String simpleName() {
        return JavaType.simpleName(javaType);
    }

    public final Type javaType() {
        return javaType;
    }

    @Override
    public final int hashCode() {
        return javaType.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TypeToken<?> that) {
            return this.javaType.equals(that.javaType);
        } else {
            return false;
        }
    }

    @Override
    public final String toString() {
        return simpleName();
    }

    public final <R> TypeToken<R> transform(TypeToken<? super T> fromMask, TypeToken<R> toMask) {
        return fromJavaType(JavaType.transform(this.javaType, fromMask.javaType, toMask.javaType));
    }

    public final <S extends T> TypeToken<S> upgrade(TypeToken<S> subtypeMask) {
        return fromJavaType(JavaType.upgrade(this.javaType, subtypeMask.javaType));
    }

    // helpers and samples

    public final <S extends T> TypeToken<T> infer(TypeToken<S> typeArgumentSource) {
        return typeArgumentSource.transform(this, this);
    }

    public final TypeToken<T[]> array() {
        return this.transform(new TypeToken<>() {}, new TypeToken<>() {
        });
    }

    public final TypeToken<Optional<T>> optional() {
        return this.transform(new TypeToken<>() {}, new TypeToken<>() {});
    }

    public final TypeToken<Collection<T>> collection() {
        return this.transform(new TypeToken<>() {}, new TypeToken<>() {});
    }

    public final TypeToken<List<T>> list() {
        return this.transform(new TypeToken<>() {}, new TypeToken<>() {});
    }

    public final TypeToken<Set<T>> set() {
        return this.transform(new TypeToken<>() {}, new TypeToken<>() {});
    }

    public final <K> TypeToken<Map<K, T>> mapFrom(TypeToken<K> key) {
        return key.transform(new TypeToken<>() {}, new TypeToken<>() {});
    }

    public final <V> TypeToken<Map<T, V>> mapTo(TypeToken<V> value) {
        return value.transform(new TypeToken<>() {}, new TypeToken<>() {});
    }

    public static final TypeToken<Object> OBJECT = new TypeToken<>() {};

    public static final TypeToken<Void> VOID = new TypeToken<>() {};

    public static <S, T extends S> TypeToken<S> downgrade(TypeToken<T> type, TypeToken<S> supertypeMask) {
        return supertypeMask.infer(type);
    }

    public static <E> TypeToken<E> arrayComponent(TypeToken<E[]> array) {
        return array.transform(new TypeToken<>() {}, new TypeToken<>() {
        });
    }

    public static <T> TypeToken<T> optionalContent(TypeToken<Optional<T>> optional) {
        return optional.transform(new TypeToken<>() {}, new TypeToken<>() {});
    }

    public static <E> TypeToken<E> collectionElement(TypeToken<? extends Iterable<E>> collection) {
        return collection.transform(new TypeToken<Iterable<E>>() {}, new TypeToken<>() {});
    }

    public static <K> TypeToken<K> mapKey(TypeToken<? extends Map<K, ?>> map) {
        return map.transform(new TypeToken<Map<K, ?>>() {}, new TypeToken<>() {});
    }

    public static <V> TypeToken<V> mapValue(TypeToken<? extends Map<?, V>> map) {
        return map.transform(new TypeToken<Map<?, V>>() {}, new TypeToken<>() {});
    }

}