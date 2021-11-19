package org.khovrino.type;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TypeVariableTest {

    static class A<P> {
        class B<Q> {
            class C<R> {
                class D<S> {
                    public <T> D() {}
                    public <T> void dummy() {}
                    public static <T> void staticDummy() {}
                 }
            }
        }
    }

    @Test
    public void genericDeclarationTest() throws NoSuchMethodException {

        Constructor<?> constructor = JavaType.getDeclaredConstructor(A.B.C.D.class);
        Method nonStaticMethod = A.B.C.D.class.getDeclaredMethod("dummy");
        Method staticMethod = A.B.C.D.class.getDeclaredMethod("staticDummy");

        TypeVariable<?> p = A.class.getTypeParameters()[0];
        TypeVariable<?> q = A.B.class.getTypeParameters()[0];
        TypeVariable<?> r = A.B.C.class.getTypeParameters()[0];
        TypeVariable<?> s = A.B.C.D.class.getTypeParameters()[0];
        TypeVariable<?> t1 = constructor.getTypeParameters()[0];
        TypeVariable<?> t2 = nonStaticMethod.getTypeParameters()[0];
        TypeVariable<?> t3 = staticMethod.getTypeParameters()[0];

        assertNotEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertNotEquals(t2, t3);
        assertEquals(List.of(p), getEffectiveTypeParameters(A.class));
        assertEquals(List.of(p, q), getEffectiveTypeParameters(A.B.class));
        assertEquals(List.of(p, q, r), getEffectiveTypeParameters(A.B.C.class));
        assertEquals(List.of(p, q, r, s), getEffectiveTypeParameters(A.B.C.D.class));
        assertEquals(List.of(p, q, r, s, t1), getEffectiveTypeParameters(constructor));
        assertEquals(List.of(p, q, r, s, t2), getEffectiveTypeParameters(nonStaticMethod));
        assertEquals(List.of(t3), getEffectiveTypeParameters(staticMethod));
    }

    private List<TypeVariable<?>> getEffectiveTypeParameters(GenericDeclaration d) {
        List<TypeVariable<?>> result = new LinkedList<>();
        collectEffectiveTypeParameters(d, result);
        return Collections.unmodifiableList(result);
    }

    private void collectEffectiveTypeParameters(GenericDeclaration d, List<TypeVariable<?>> result) {
        GenericDeclaration parent;
        if (d instanceof Class<?> clazz) {
            parent = Modifier.isStatic(clazz.getModifiers()) ? null : clazz.getDeclaringClass();
        } else if (d instanceof Method method) {
            parent = Modifier.isStatic(method.getModifiers()) ? null : method.getDeclaringClass();
        } else if (d instanceof Constructor<?> constructor) {
            parent = constructor.getDeclaringClass();
        } else {
            throw new IllegalStateException(String.format("Unknown GenericDeclaration subclass: %s", d.getClass().getTypeName()));
        }
        if (parent != null) collectEffectiveTypeParameters(parent, result);
        Collections.addAll(result, d.getTypeParameters());
    }

}
