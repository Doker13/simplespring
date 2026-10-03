package t.simplespring.context.annotation;

import t.simplespring.beans.BeansException;
import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.support.BeanDefinitionRegistry;
import t.simplespring.stereotype.Component;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ClassPathBeanDefinitionScanner {

    private static final String CLASS_SUFFIX = ".class";

    private final BeanDefinitionRegistry registry;
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public int scan(String... basePackages) {
        int registered = 0;
        for (String basePackage : basePackages) {
            for (String className : findClassNames(basePackage)) {
                Class<?> candidate = loadClass(className);
                Component component = findComponent(candidate);
                if (component != null) {
                    registry.registerBeanDefinition(beanName(candidate, component), new BeanDefinition(
                            candidate, scope(candidate), candidate.isAnnotationPresent(Primary.class)));
                    registered++;
                }
            }
        }
        return registered;
    }

    private Set<String> findClassNames(String basePackage) {
        String packagePath = basePackage.replace('.', '/');
        Set<String> classNames = new TreeSet<>();
        try {
            for (URL resource : Collections.list(classLoader.getResources(packagePath))) {
                switch (resource.getProtocol()) {
                    case "file" -> collectFromDirectory(Path.of(resource.toURI()), basePackage, classNames);
                    case "jar" -> collectFromJar(resource, packagePath, classNames);
                    default -> throw new BeansException("Unsupported classpath location '%s' for package '%s'"
                            .formatted(resource, basePackage));
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new BeansException("Failed to scan package '%s'".formatted(basePackage), e);
        }
        return classNames;
    }

    private void collectFromDirectory(Path packageDirectory, String basePackage, Collection<String> classNames)
            throws IOException {
        try (Stream<Path> files = Files.walk(packageDirectory)) {
            files.map(file -> packageDirectory.relativize(file).toString())
                    .filter(relativePath -> relativePath.endsWith(CLASS_SUFFIX))
                    .map(relativePath -> basePackage + '.' + toClassName(relativePath.replace('\\', '/')))
                    .forEach(classNames::add);
        }
    }

    private void collectFromJar(URL resource, String packagePath, Collection<String> classNames) throws IOException {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        connection.setUseCaches(false);
        try (JarFile jar = connection.getJarFile()) {
            jar.stream()
                    .map(entry -> entry.getName())
                    .filter(entryName -> entryName.startsWith(packagePath + '/') && entryName.endsWith(CLASS_SUFFIX))
                    .map(ClassPathBeanDefinitionScanner::toClassName)
                    .forEach(classNames::add);
        }
    }

    private static String toClassName(String classFilePath) {
        return classFilePath.substring(0, classFilePath.length() - CLASS_SUFFIX.length()).replace('/', '.');
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new BeansException("Failed to load class '%s' found during component scan".formatted(className), e);
        }
    }

    private static Component findComponent(Class<?> candidate) {
        if (candidate.isInterface() || Modifier.isAbstract(candidate.getModifiers())) {
            return null;
        }
        Component component = candidate.getAnnotation(Component.class);
        if (component != null) {
            return component;
        }
        for (Annotation annotation : candidate.getAnnotations()) {
            Component metaComponent = annotation.annotationType().getAnnotation(Component.class);
            if (metaComponent != null) {
                return metaComponent;
            }
        }
        return null;
    }

    private static String beanName(Class<?> componentClass, Component component) {
        String explicitName = component.value();
        if (!explicitName.isBlank()) {
            return explicitName;
        }
        String simpleName = componentClass.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private static String scope(Class<?> componentClass) {
        Scope scope = componentClass.getAnnotation(Scope.class);
        return scope == null ? BeanDefinition.SCOPE_SINGLETON : scope.value();
    }
}
