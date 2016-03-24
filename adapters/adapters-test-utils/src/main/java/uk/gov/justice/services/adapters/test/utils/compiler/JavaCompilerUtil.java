package uk.gov.justice.services.adapters.test.utils.compiler;

import com.google.common.collect.Sets;

import org.apache.commons.io.FileUtils;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import static java.lang.String.join;
import static java.text.MessageFormat.format;

/**
 * Compiles and loads classes and interfaces from the specified folders
 */
public class JavaCompilerUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCompilerUtil.class);
    private final File codegenOutputDir, compilationOutputDir;

    public JavaCompilerUtil(File codegenOutputDir, File compilationOutputDir) {
        this.codegenOutputDir = codegenOutputDir;
        this.compilationOutputDir = compilationOutputDir;
    }

    /**
     * Compiles and loads a single class
     *
     * @throws IllegalStateException - if more or less than one classes found
     */
    public Class<?> compiledClassOf(String basePackage)
            throws MalformedURLException {
        Set<Class<?>> resourceClasses = compiledClassesOf(basePackage);
        if (resourceClasses.size() != 1) {
            throw new IllegalStateException(format("Expected to find single class but found {0}", resourceClasses));
        }
        return resourceClasses.iterator().next();
    }

    /**
     * Compiles then finds a single class.
     *
     * @param basePackage the base package
     * @return the class
     * @throws IllegalStateException - if more or less than one classes found
     */
    public Class<?> compiledClassOf(String basePackage, String... additionalFilterElements) throws MalformedURLException {
        Set<Class<?>> resourceClasses = compiledClassesAndInterfaces(
                c -> !c.isInterface() && c.getName().equals(join(".", basePackage, join(".", additionalFilterElements))), basePackage);
        if (resourceClasses.size() != 1) {
            throw new IllegalStateException(format("Expected to find single class but found {0}", resourceClasses));
        }
        return resourceClasses.iterator().next();
    }

    public Class<?> classOf(Set<Class<?>> classes, String basePackage, String... additionalFilterElements) throws MalformedURLException {
        Set<Class<?>> resourceClasses = classesMatching(classes,
                c -> !c.isInterface() && c.getName().equals(join(".", basePackage, join(".", additionalFilterElements))));
        if (resourceClasses.size() != 1) {
            throw new IllegalStateException(format("Expected to find single class but found {0}", resourceClasses));
        }
        return resourceClasses.iterator().next();
    }

    /**
     * Compiles and loads a single interface
     *
     * @throws IllegalStateException - if more or less than one interfaces found
     */
    public Class<?> compiledInterfaceOf(String basePackageName)
            throws MalformedURLException {
        Set<Class<?>> resourceInterfaces = compiledInterfacesOf(basePackageName);
        if (resourceInterfaces.size() != 1) {
            throw new IllegalStateException(
                    format("Expected to find single interface but found {0}", resourceInterfaces));

        }
        return resourceInterfaces.iterator().next();
    }

    /**
     * compiles and loads specified classes
     */
    public Set<Class<?>> compiledClassesOf(String basePackage)
            throws MalformedURLException {
        return compiledClassesAndInterfaces(c -> !c.isInterface(), basePackage);
    }

    /**
     * compiles and loads specified interfaces
     */
    public Set<Class<?>> compiledInterfacesOf(String basePackage)
            throws MalformedURLException {
        return compiledClassesAndInterfaces(Class::isInterface, basePackage);
    }

    private Set<Class<?>> compiledClassesAndInterfaces(Predicate<? super Class<?>> predicate,
                                                       String basePackage)
            throws MalformedURLException {
        return classesMatching(compile(basePackage), predicate);
    }

    private Set<Class<?>> classesMatching(Set<Class<?>> classes, Predicate<? super Class<?>> predicate)
            throws MalformedURLException {
        return classes.stream().filter(predicate).collect(Collectors.toSet());
    }

    private Set<Class<?>> compile(String basePackage) throws MalformedURLException {
        compile();
        return loadClasses(basePackage);
    }

    private Set<Class<?>> loadClasses(String basePackage) throws MalformedURLException {

        ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
        Set<Class<?>> rootResourceClasses = new HashSet<>();
        try (URLClassLoader resourceClassLoader = new URLClassLoader(new URL[]{compilationOutputDir.toURI().toURL()})) {
            Thread.currentThread().setContextClassLoader(resourceClassLoader);
            Reflections reflections = new Reflections(basePackage, ClasspathHelper.forClass(Object.class),
                    new AllObjectsScanner());
            Set<String> classNames = reflections.getStore().get(AllObjectsScanner.class, Object.class.getName());
            rootResourceClasses.addAll(Sets.newHashSet(ReflectionUtils.forNames(classNames, reflections.getConfiguration().getClassLoaders())));
        } catch (IOException ex) {
            throw new RuntimeException("Error creating class loader", ex);
        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
        }
        return rootResourceClasses;
    }

    private void compile() {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);
        List<JavaFileObject> javaObjects = scanRecursivelyForJavaObjects(codegenOutputDir, fileManager);

        if (javaObjects.size() == 0) {
            throw new CompilationException(
                    format("There are no source files to compile in {0}", codegenOutputDir.getAbsolutePath()));
        }
        String[] compileOptions = new String[]{"-d", compilationOutputDir.getAbsolutePath()};
        Iterable<String> compilationOptions = Arrays.asList(compileOptions);

        CompilationTask compilerTask = compiler.getTask(null, fileManager, diagnostics, compilationOptions, null,
                javaObjects);

        if (!compilerTask.call()) {
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                LOG.error("Error on line {} in {}", diagnostic.getLineNumber(), diagnostic);
            }
            throw new CompilationException("Could not compile project");
        }
    }

    private List<JavaFileObject> scanRecursivelyForJavaObjects(File dir, StandardJavaFileManager fileManager) {
        List<JavaFileObject> javaObjects = new LinkedList<JavaFileObject>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                javaObjects.addAll(scanRecursivelyForJavaObjects(file, fileManager));
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
                try {
                    LOG.debug(FileUtils.readFileToString(file));
                } catch (IOException e) {
                    LOG.warn("Could not read file", e);
                }
                javaObjects.add(readJavaObject(file, fileManager));
            }
        }
        return javaObjects;
    }

    private JavaFileObject readJavaObject(File file, StandardJavaFileManager fileManager) {
        Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(file);
        Iterator<? extends JavaFileObject> it = javaFileObjects.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new CompilationException(format("Could not load {0} java file object", file.getAbsolutePath()));
    }

}
