package t.simplespring.context.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import t.simplespring.beans.factory.BeanDefinitionStoreException;
import t.simplespring.beans.factory.config.BeanDefinition;
import t.simplespring.beans.factory.support.DefaultListableBeanFactory;
import t.simplespring.sample.JdbcOrderRepository;
import t.simplespring.sample.SmtpNotifier;

import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassPathBeanDefinitionScannerTest {

    private static final String SAMPLE_PACKAGE = "t.simplespring.sample";

    private final DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

    @Test
    void registersComponentsFromPackageAndSubpackages() {
        int registered = new ClassPathBeanDefinitionScanner(registry).scan(SAMPLE_PACKAGE);

        assertEquals(5, registered);
        assertEquals(List.of("jdbcOrderRepository", "orderService", "shoppingCart", "mailer", "paymentService"),
                registry.getBeanDefinitionNames());
    }

    @Test
    void derivesBeanNameFromClassNameOrAnnotationValue() {
        new ClassPathBeanDefinitionScanner(registry).scan(SAMPLE_PACKAGE);

        assertEquals(JdbcOrderRepository.class, registry.getBeanDefinition("jdbcOrderRepository").beanClass());
        assertEquals(SmtpNotifier.class, registry.getBeanDefinition("mailer").beanClass());
        assertFalse(registry.containsBeanDefinition("smtpNotifier"));
    }

    @Test
    void readsScopeAnnotation() {
        new ClassPathBeanDefinitionScanner(registry).scan(SAMPLE_PACKAGE);

        assertTrue(registry.getBeanDefinition("shoppingCart").isPrototype());
        assertTrue(registry.getBeanDefinition("orderService").isSingleton());
    }

    @Test
    void skipsUnannotatedAndAbstractClasses() {
        new ClassPathBeanDefinitionScanner(registry).scan(SAMPLE_PACKAGE);

        assertFalse(registry.containsBeanDefinition("priceFormatter"));
        assertFalse(registry.containsBeanDefinition("abstractHandler"));
    }

    @Test
    void scansOnlyRequestedPackage() {
        int registered = new ClassPathBeanDefinitionScanner(registry).scan(SAMPLE_PACKAGE + ".payment");

        assertEquals(List.of("paymentService"), registry.getBeanDefinitionNames());
        assertEquals(1, registered);
    }

    @Test
    void findsNothingInUnknownPackage() {
        assertEquals(0, new ClassPathBeanDefinitionScanner(registry).scan("t.simplespring.nowhere"));
    }

    @Test
    void failsWhenSamePackageIsScannedTwice() {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.scan(SAMPLE_PACKAGE);

        assertThrows(BeanDefinitionStoreException.class, () -> scanner.scan(SAMPLE_PACKAGE));
    }

    @Test
    void scansComponentsPackedIntoJar(@TempDir Path workDirectory) throws IOException {
        Path jar = buildJarWithComponent(workDirectory);
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader jarClassLoader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, original)) {
            Thread.currentThread().setContextClassLoader(jarClassLoader);

            int registered = new ClassPathBeanDefinitionScanner(registry).scan("jarred");

            assertEquals(1, registered);
            BeanDefinition definition = registry.getBeanDefinition("jarredComponent");
            assertEquals("jarred.inner.JarredComponent", definition.beanClass().getName());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private static Path buildJarWithComponent(Path workDirectory) throws IOException {
        String classFile = "jarred/inner/JarredComponent.class";
        Path source = Files.createDirectories(workDirectory.resolve("src/jarred/inner")).resolve("JarredComponent.java");
        Files.writeString(source, """
                package jarred.inner;

                @t.simplespring.stereotype.Component
                public class JarredComponent {
                }
                """);
        Path classes = Files.createDirectories(workDirectory.resolve("classes"));
        int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-classpath", System.getProperty("java.class.path"), "-d", classes.toString(), source.toString());
        assertEquals(0, exitCode);

        Path jar = workDirectory.resolve("components.jar");
        try (OutputStream file = Files.newOutputStream(jar); JarOutputStream out = new JarOutputStream(file)) {
            out.putNextEntry(new JarEntry("jarred/"));
            out.putNextEntry(new JarEntry("jarred/inner/"));
            out.putNextEntry(new JarEntry(classFile));
            Files.copy(classes.resolve(classFile), out);
            out.closeEntry();
        }
        return jar;
    }
}
