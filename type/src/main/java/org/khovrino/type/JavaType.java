package org.khovrino.type;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

public final class JavaType {

    public static Object newInstance(Type type) {
        Class<?> c = rawType(type);
        int rank = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            rank++;
        }
        if (rank == 0) {
            try {
                return c.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            return Array.newInstance(c, new int[rank]);
        }
    }

    public static String simpleName(Type type) {
        Objects.requireNonNull(type);
        StringBuilder sb = new StringBuilder();
        writeSimpleName(type, sb);
        return sb.toString();
    }

    public static String typeSignature(Class<?> clazz) {
        StringBuilder signature = new StringBuilder();
        while (clazz.isArray()) {
            signature.append('[');
            clazz = clazz.getComponentType();
        }
        if (clazz.equals(void.class)) {
            signature.append('V');
        } else if (clazz.equals(boolean.class)) {
            signature.append('Z');
        } else if (clazz.equals(char.class)) {
            signature.append('C');
        } else if (clazz.equals(byte.class)) {
            signature.append('B');
        } else if (clazz.equals(short.class)) {
            signature.append('S');
        } else if (clazz.equals(int.class)) {
            signature.append('I');
        } else if (clazz.equals(long.class)) {
            signature.append('J');
        } else if (clazz.equals(float.class)) {
            signature.append('F');
        } else if (clazz.equals(double.class)) {
            signature.append('D');
        } else {
            signature.append('L');
            signature.append(clazz.getName().replace('.', '/'));
            signature.append(';');
        }
        return signature.toString();
    }

    public static boolean isPrimitive(Type type) {
        Objects.requireNonNull(type);
        return type instanceof Class && ((Class<?>) type).isPrimitive();
    }

    public static boolean isReference(Type type) {
        return !isPrimitive(type);
    }

    public static boolean isArray(Type type) {
        Objects.requireNonNull(type);
        if (type instanceof TypeVariable) {
            throw new IllegalArgumentException();
        } else if (type instanceof GenericArrayType) {
            return true;
        } else if (type instanceof Class) {
            return ((Class<?>) type).isArray();
        } else {
            return false;
        }
    }

    public static Type componentType(Type type) {
        Objects.requireNonNull(type);
        if (type instanceof Class && ((Class<?>) type).isArray()) {
            return ((Class<?>) type).getComponentType();
        } else if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        } else {
            throw new IllegalArgumentException(String.format("Not an array: %s", type.getTypeName()));
        }
    }

    public static Type arrayType(Type type) {
        Objects.requireNonNull(type);
        if (type instanceof Class) {
            return ((Class<?>) type).arrayType();
        } else {
            return new GenericArrayTypeImpl(type);
        }
    }

    public static List<Type> supertypes(Type type) {
        Objects.requireNonNull(type);
        if (isPrimitive(type)) {
            return Collections.emptyList();
        } else if (isArray(type)) {
            List<Type> componentSupertypes = supertypes(componentType(type));
            if (componentSupertypes.isEmpty()) {
                return Collections.singletonList(Object.class);
            } else {
                List<Type> supertypes;
                supertypes = new ArrayList<>(componentSupertypes.size());
                for (Type componentSupertype : componentSupertypes) {
                    supertypes.add(arrayType(componentSupertype));
                }
                return Collections.unmodifiableList(supertypes);
            }
        } else {
            List<Type> supertypes = new ArrayList<>();
            Map<TypeVariable<?>, Type> args = typeArguments(type);
            Class<?> raw = rawType(type);
            Type superClass = raw.getGenericSuperclass();
            Type[] superInterfaces = raw.getGenericInterfaces();
            if (superClass != null) {
                supertypes.add(replace(superClass, args));
            }
            for (Type superInterface : superInterfaces) {
                supertypes.add(replace(superInterface, args));
            }
            return Collections.unmodifiableList(supertypes);
        }
    }

    public static Class<?> rawType(Type type) {
        Objects.requireNonNull(type);
        if (type instanceof Class) {
            return (Class<?>) requireNonRawClass(type);
        } else if (type instanceof ParameterizedType paramType) {
            return (Class<?>) paramType.getRawType();
        } else if (type instanceof GenericArrayType arrayType) {
            return rawType(arrayType.getGenericComponentType()).arrayType();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Map<TypeVariable<?>, Type> typeArguments(Type type) {
        Objects.requireNonNull(type);
        if (isPrimitive(type)) {
            return Collections.emptyMap();
        }
        if (isArray(type)) {
            return typeArguments(componentType(type));
        }
        if (type instanceof Class) {
            requireNonRawClass(type);
            return Collections.emptyMap();
        } else if (type instanceof ParameterizedType) {
            Type t = type;
            ParameterizedType p;
            Stack<ParameterizedType> scope = new Stack<>();
            Map<TypeVariable<?>, Type> map = new LinkedHashMap<>();
            do {
                p = (ParameterizedType) t;
                scope.push(p);
            } while ((t = p.getOwnerType()) instanceof ParameterizedType);
            while (!scope.empty()) {
                p = scope.pop();
                Class<?> c = (Class<?>) p.getRawType();
                TypeVariable<?>[] vars = c.getTypeParameters();
                Type[] args = p.getActualTypeArguments();
                for (int i = 0; i < vars.length; i++) {
                    map.put(vars[i], args[i]);
                }
            }
            return Collections.unmodifiableMap(map);
        } else {
            throw new IllegalArgumentException(type.getClass().getName());
        }
    }

    public static boolean testExtends(Type subType, Type superType) {
        return solveExtends(subType, superType, SolutionMode.IDENTITY).isIdentity();
    }

    public static boolean testSuper(Type superType, Type subType) {
        return solveExtends(subType, superType, SolutionMode.IDENTITY).isIdentity();
    }

    public static Type transform(Type src, Type fromMask, Type toMask) {
        return JavaType.replace(toMask, JavaType.solveSuper(fromMask, src));
    }

    public static Type shift(Type src, Class<?> dstClass) {
        Class<?> srcClass = rawType(src);
        if (dstClass.isAssignableFrom(srcClass)) {
            return downgrade(src, dstClass);
        } else if (srcClass.isAssignableFrom(dstClass)) {
            return upgrade(src, dstClass);
        } else {
            Set<Class<?>> commonSupers = commonSuperclasses(srcClass, dstClass);
            Type dstMask = unresolved(dstClass);
            Map<TypeVariable<?>, Type> args = typeArguments(dstMask);
            for (Class<?> commonSuperclass : commonSupers) {
                Type commonSupertype = downgrade(src, commonSuperclass);
                Map<TypeVariable<?>, Type> root = solveExtends(dstMask, commonSupertype);
                if (root.keySet().containsAll(args.keySet())) {
                    return replace(dstMask, root);
                }
            }
            throw new IllegalArgumentException(
                    String.format("Can't infer type parameters of %s from %s", dstClass.getName(), src.getTypeName()));
        }
    }

    public static Type upgrade(Type superType, Type subTypeMask) {
        return replace(subTypeMask, solveExtends(subTypeMask, superType));
    }

    public static Type upgrade(Type superType, Class<?> subClass) {
        Type subTypeMask = unresolved(subClass);
        Map<TypeVariable<?>, Type> args = typeArguments(subTypeMask);
        Map<TypeVariable<?>, Type> root = solveExtends(subTypeMask, superType);
        if (root.keySet().containsAll(args.keySet())) {
            return replace(subTypeMask, root);
        } else {
            Set<TypeVariable<?>> unknowns = new LinkedHashSet<>(args.keySet());
            unknowns.removeAll(root.keySet());
            throw new IllegalArgumentException(String.format("Can't infer type parameters %s of %s from %s", unknowns,
                    subTypeMask.getTypeName(), superType.getTypeName()));
        }
    }

    public static Type downgrade(Type subType, Type superTypeMask) {
        return JavaType.replace(superTypeMask, JavaType.solveSuper(superTypeMask, subType));
    }

    public static Type downgrade(Type subType, Class<?> superClass) {
        Objects.requireNonNull(subType);
        Objects.requireNonNull(superClass);
        if (isArray(subType)) {
            return arrayType(downgrade(componentType(subType), superClass.componentType()));
        }
        Type currType = subType;
        Class<?> currClass = rawType(currType);
        if (!superClass.isAssignableFrom(currClass)) {
            throw new IllegalArgumentException(
                    String.format("%s is not assignable from %s", superClass.getName(), currClass.getName()));
        }
        while (!currClass.equals(superClass)) {
            Map<TypeVariable<?>, Type> args = typeArguments(currType);
            Class<?> nextClass = currClass.getSuperclass();
            if (nextClass != null && superClass.isAssignableFrom(nextClass)) {
                currType = replace(currClass.getGenericSuperclass(), args);
                currClass = nextClass;
            } else {
                Class<?>[] superInterfaces = currClass.getInterfaces();
                for (int i = 0; i < superInterfaces.length; i++) {
                    if (superClass.isAssignableFrom(superInterfaces[i])) {
                        currType = replace(currClass.getGenericInterfaces()[i], args);
                        currClass = superInterfaces[i];
                        break;
                    }
                }
            }
        }
        return currType;
    }

    static Map<TypeVariable<?>, Type> solveExtends(Type subTypeMask, Type superType) {
        try {
            return solveExtends(subTypeMask, superType, SolutionMode.FOR_LEFT_SIDE).root();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(String.format("%s don't extends %s", subTypeMask, superType));
        }
    }

    static Map<TypeVariable<?>, Type> solveSuper(Type superTypeMask, Type subType) {
        try {
            return solveExtends(subType, superTypeMask, SolutionMode.FOR_RIGHT_SIDE).root();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(String.format("%s don't super %s", superTypeMask, subType));
        }
    }

    static SolutionSet solveExtends(Type subType, Type superType, SolutionMode mode) {
        Objects.requireNonNull(subType, "subType");
        Objects.requireNonNull(superType, "superType");
        if (subType instanceof WildcardType) {
            throw new IllegalArgumentException();
        }
        if (superType instanceof WildcardType) {
            throw new IllegalArgumentException();
        }
        if (isPrimitive(subType) || isPrimitive(superType)) {
            return SolutionSet.createConst(subType.equals(superType));
        }
        if (superType.equals(Object.class)) {
            return SolutionSet.createTrue();
        }
        if (subType instanceof TypeVariable) {
            switch (mode) {
                case FOR_LEFT_SIDE:
                    return SolutionSet.createUpperBound((TypeVariable<?>) subType, superType);
                case IDENTITY:
                    if (subType.equals(superType)) {
                        return SolutionSet.createTrue();
                    }
                case FOR_RIGHT_SIDE:
                    Type[] subTypeBounds = ((TypeVariable<?>) subType).getBounds();
                    SolutionSet disjunction = SolutionSet.createFalse();
                    for (Type subTypeBound : subTypeBounds) {
                        disjunction.or(solveExtends(subTypeBound, superType, mode));
                        if (disjunction.isIdentity()) {
                            break;
                        }
                    }
                    return disjunction;
                default:
                    throw new IllegalStateException();
            }
        }
        if (superType instanceof TypeVariable) {
            return switch (mode) {
                case FOR_RIGHT_SIDE -> SolutionSet.createLowerBound((TypeVariable<?>) superType, subType);
                case IDENTITY -> SolutionSet.createConst(subType.equals(superType));
                case FOR_LEFT_SIDE -> SolutionSet.createFalse();
            };
        }
        if (isArray(subType)) {
            if (isArray(superType)) {
                Type subTypeElement = componentType(subType);
                Type superTypeElement = componentType(superType);
                return solveExtends(subTypeElement, superTypeElement, mode);
            } else {
                return SolutionSet.createFalse();
            }
        } else if (isArray(superType)) {
            return SolutionSet.createFalse();
        }
        Class<?> subClass = rawType(subType);
        Class<?> superClass = rawType(superType);
        if (!superClass.isAssignableFrom(subClass)) {
            return SolutionSet.createFalse();
        }
        Map<TypeVariable<?>, Type> subArgs = typeArguments(downgrade(subType, superClass));
        Map<TypeVariable<?>, Type> superArgs = typeArguments(superType);
        SolutionSet conjunction = SolutionSet.createTrue();
        for (TypeVariable<?> var : subArgs.keySet()) {
            conjunction.and(solveContainedBy(subArgs.get(var), superArgs.get(var), mode));
            if (conjunction.isEmpty()) {
                break;
            }
        }
        return conjunction;
    }

    static SolutionSet solveContainedBy(Type innerRange, Type outerRange, SolutionMode mode) {
        Objects.requireNonNull(innerRange);
        Objects.requireNonNull(outerRange);
        Type[] innerSuperBounds;
        Type[] innerExtendsBounds;
        Type[] outerSuperBounds;
        Type[] outerExtendsBounds;
        if (innerRange instanceof WildcardType) {
            innerSuperBounds = ((WildcardType) innerRange).getLowerBounds();
            innerExtendsBounds = ((WildcardType) innerRange).getUpperBounds();
        } else {
            innerSuperBounds = new Type[] { innerRange };
            innerExtendsBounds = new Type[] { innerRange };
        }
        if (outerRange instanceof WildcardType) {
            outerSuperBounds = ((WildcardType) outerRange).getLowerBounds();
            outerExtendsBounds = ((WildcardType) outerRange).getUpperBounds();
        } else {
            outerSuperBounds = new Type[] { outerRange };
            outerExtendsBounds = new Type[] { outerRange };
        }
        SolutionSet conjunction = SolutionSet.createTrue();
        for (Type outerExtendsBound : outerExtendsBounds) {
            SolutionSet disjunction = SolutionSet.createFalse();
            for (Type innerExtendsBound : innerExtendsBounds) {
                disjunction.or(solveExtends(innerExtendsBound, outerExtendsBound, mode));
                if (disjunction.isIdentity()) {
                    break;
                }
            }
            conjunction.and(disjunction);
            if (conjunction.isEmpty()) {
                break;
            }
        }
        for (Type outerSuperBound : outerSuperBounds) {
            SolutionSet disjunction = SolutionSet.createFalse();
            for (Type innerSuperBound : innerSuperBounds) {
                disjunction.or(solveExtends(outerSuperBound, innerSuperBound, mode.invert()));
                if (disjunction.isIdentity()) {
                    break;
                }
            }
            conjunction.and(disjunction);
            if (conjunction.isEmpty()) {
                break;
            }
        }
        return conjunction;
    }

    public static Type unresolved(Class<?> clazz) {
        Class<?> parentClass = clazz.getDeclaringClass();
        Type parentType;
        if (parentClass == null) {
            parentType = null;
        } else if (Modifier.isStatic(clazz.getModifiers())) {
            parentType = parentClass;
        } else {
            parentType = unresolved(parentClass);
        }
        TypeVariable<?>[] params = clazz.getTypeParameters();
        if (parentType == null && params.length == 0) {
            return clazz;
        } else {
            return new ParameterizedTypeImpl(parentType, clazz, params);
        }
    }

    static Type[] replace(Type[] src, Map<?, ? extends Type> substitution) {
        Type[] dst = new Type[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = replace(src[i], substitution);
        }
        return dst;
    }

    static Type replace(Type src, Map<?, ? extends Type> substitution) {
        if (src instanceof TypeVariable) {
            Type dst = substitution.get(src);
            return Objects.requireNonNullElse(dst, src);
        } else if (src instanceof WildcardType) {
            Type[] srcLowerBounds = ((WildcardType) src).getLowerBounds();
            Type[] srcUpperBounds = ((WildcardType) src).getUpperBounds();
            Type[] dstLowerBounds = replace(srcLowerBounds, substitution);
            Type[] dstUpperBounds = replace(srcUpperBounds, substitution);
            if (arrayEqualRefs(srcLowerBounds, dstLowerBounds) && arrayEqualRefs(srcUpperBounds, dstUpperBounds)) {
                return src;
            } else {
                return new WildcardTypeImpl(dstLowerBounds, dstUpperBounds);
            }
        } else if (src instanceof GenericArrayType) {
            Type srcComponent = ((GenericArrayType) src).getGenericComponentType();
            Type dstComponent = replace(srcComponent, substitution);
            if (srcComponent == dstComponent) {
                return src;
            } else {
                return new GenericArrayTypeImpl(dstComponent);
            }
        } else if (src instanceof ParameterizedType) {
            Type srcOwner = ((ParameterizedType) src).getOwnerType();
            Class<?> srcRaw = (Class<?>) ((ParameterizedType) src).getRawType();
            Type[] srcArgs = ((ParameterizedType) src).getActualTypeArguments();
            Type dstOwner = replace(srcOwner, substitution);
            Type[] dstArgs = replace(srcArgs, substitution);
            if (srcOwner == dstOwner && arrayEqualRefs(srcArgs, dstArgs)) {
                return src;
            } else {
                return new ParameterizedTypeImpl(dstOwner, srcRaw, dstArgs);
            }
        } else {
            return src;
        }
    }

    static Type requireNonRawClass(Type type) {
        if (type instanceof Class<?> clazz) {
            do {
                if (clazz.getTypeParameters().length > 0) {
                    throw new IllegalArgumentException(String.format("Raw class %s", clazz.getName()));
                }
            } while (!Modifier.isStatic(clazz.getModifiers()) && (clazz = clazz.getDeclaringClass()) != null);
        }
        return type;
    }

    private static Set<Class<?>> commonSuperclasses(Class<?> class1, Class<?> class2) {
        if (class1.isAssignableFrom(class2)) {
            return Collections.singleton(class1);
        } else {
            Set<Class<?>> result = new LinkedHashSet<>();
            Set<Class<?>> superclasses1 = superclasses(class1);
            for (Class<?> superclass1 : superclasses1) {
                result.addAll(commonSuperclasses(superclass1, class2));
            }
            return Collections.unmodifiableSet(result);
        }
    }

    private static Set<Class<?>> superclasses(Class<?> clazz) {
        if (clazz.isPrimitive() || clazz.equals(Object.class)) {
            return Collections.emptySet();
        } else {
            Set<Class<?>> superclasses = new LinkedHashSet<>();
            Class<?> superclass = clazz.getSuperclass();
            Class<?>[] superinterfaces = clazz.getInterfaces();
            if (superclass != null) {
                superclasses.add(superclass);
            }
            Collections.addAll(superclasses, superinterfaces);
            return Collections.unmodifiableSet(superclasses);
        }
    }

    private static void writeSimpleName(Type type, StringBuilder sb) {
        if (type instanceof Class) {
            sb.append(((Class<?>) type).getSimpleName());
        } else if (type instanceof GenericArrayType) {
            sb.append(simpleName(((GenericArrayType) type).getGenericComponentType()));
            sb.append("[]");
        } else if (type instanceof ParameterizedType) {
            Type t = type;
            ParameterizedType p;
            Stack<ParameterizedType> scope = new Stack<>();
            boolean dot = false;
            do {
                p = (ParameterizedType) t;
                scope.push(p);
            } while ((t = p.getOwnerType()) instanceof ParameterizedType);
            while (!scope.empty()) {
                p = scope.pop();
                if (dot) {
                    sb.append('.');
                } else {
                    dot = true;
                }
                sb.append(((Class<?>) p.getRawType()).getSimpleName());
                Type[] args = p.getActualTypeArguments();
                if (args.length > 0) {
                    sb.append('<');
                    for (int i = 0; i < args.length; i++) {
                        if (i > 0) {
                            sb.append(',');
                        }
                        sb.append(simpleName(args[i]));
                    }
                    sb.append('>');
                }
            }
        } else if (type instanceof WildcardType) {
            sb.append('?');
            Type[] superBounds = ((WildcardType) type).getLowerBounds();
            Type[] extendBounds = ((WildcardType) type).getUpperBounds();
            if (superBounds.length > 0) {
                sb.append(" super ");
                for (int i = 0; i < superBounds.length; i++) {
                    if (i > 0) {
                        sb.append('&');
                    }
                    sb.append(simpleName(superBounds[i]));
                }
            } else if ((extendBounds.length == 1 && !extendBounds[0].equals(Object.class)) || extendBounds.length > 1) {
                sb.append(" extends ");
                for (int i = 0; i < extendBounds.length; i++) {
                    if (i > 0) {
                        sb.append('&');
                    }
                    sb.append(simpleName(extendBounds[i]));
                }
            }
        } else if (type instanceof TypeVariable) {
            sb.append(((TypeVariable<?>) type).getName());
        } else {
            throw new IllegalArgumentException(type.getClass().getName());
        }
    }

    private static boolean arrayEqualRefs(Object[] src, Object[] dst) {
        if (src.length != dst.length) {
            return false;
        }
        for (int i = 0; i < src.length; i++) {
            if (src[i] != dst[i]) {
                return false;
            }
        }
        return true;
    }

    private JavaType() {
        throw new UnsupportedOperationException();
    }

}
