package t.simplespring.beans.factory.config;

import t.simplespring.beans.factory.annotation.Qualifier;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public record DependencyDescriptor(Class<?> dependencyType, String qualifier, String description) {

    public static DependencyDescriptor forField(Field field) {
        return new DependencyDescriptor(field.getType(), qualifier(field), "field '" + field.getName() + "'");
    }

    public static DependencyDescriptor forParameter(Parameter parameter, int index) {
        String description = parameter.getDeclaringExecutable() instanceof Method method
                ? "parameter %d of method '%s'".formatted(index, method.getName())
                : "constructor parameter " + index;
        return new DependencyDescriptor(parameter.getType(), qualifier(parameter), description);
    }

    private static String qualifier(AnnotatedElement injectionPoint) {
        Qualifier qualifier = injectionPoint.getAnnotation(Qualifier.class);
        return qualifier == null ? null : qualifier.value();
    }
}
