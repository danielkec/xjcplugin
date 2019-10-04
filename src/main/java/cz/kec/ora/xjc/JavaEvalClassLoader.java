package cz.kec.ora.xjc;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

public class JavaEvalClassLoader extends ClassLoader {

    private HashMap<String, Class<?>> classCache = new HashMap<>();
    private HashMap<String, InMemoryFileObject> fileObjectMap = new HashMap<>();

    public JavaEvalClassLoader(ClassLoader parent) {
        super(parent);
        System.out.println("Parent classloader is: " + parent);
    }

    public <T> T eval(String classBinaryName, String classSource) {
        Class<?> clazz = classCache.get(classBinaryName);
        if (clazz != null) {
            try {
                return (T) clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(String.format("Evaluated class %s is missing required public noarg constructor.", classBinaryName), e);
            }
        }

        String[] tokenizedBinaryName = classBinaryName.split("\\.");
        String className = tokenizedBinaryName[tokenizedBinaryName.length - 1];
        String pkgName = classBinaryName.replaceAll("\\.[^.]+$", "");


        JavaFileObject fileObject = new InMemoryFileObject(className, classSource);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager defFileMgr = compiler.getStandardFileManager(null, null, null);
        InMemoryFileManager inMemoryFileManager = new InMemoryFileManager(defFileMgr, this);

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);

        compiler.getTask(
                outputStreamWriter,
                inMemoryFileManager,
                null,
                null,//Arrays.asList("-classpath", getParent().),
                null,
                Collections.singleton(fileObject)).call();
        try {
            outputStreamWriter.flush();
            clazz = this.findClass(classBinaryName);

            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException e) {
            throw new RuntimeException(String.format("Evaluated class %s is missing required public noarg constructor.", classBinaryName), e);
        }
    }

    @Override
    public Class<?> findClass(String name) {
        InMemoryFileObject inMemoryFileObject = fileObjectMap.get(name);
        if (inMemoryFileObject == null) {
            return this.findLoadedClass(name);
        }
        byte[] classAsByteArray = inMemoryFileObject.getClassAsByteArray();
        return defineClass(name, classAsByteArray, 0, classAsByteArray.length);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    public static class InMemoryFileObject extends SimpleJavaFileObject {

        private String source;
        private ByteArrayOutputStream out;

        protected InMemoryFileObject(String className, String source) {
            super(URI.create(String.format("file:///%s.java", className)), Kind.SOURCE);
            this.source = source;
        }

        protected InMemoryFileObject(String className) {
            super(URI.create(String.format("file:///%s.class", className)), Kind.CLASS);
            this.out = new ByteArrayOutputStream();
        }

        @Override
        public OutputStream openOutputStream() {
            return out;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return this.source;
        }

        public byte[] getClassAsByteArray() {
            return out.toByteArray();
        }


    }

    public static class InMemoryFileManager extends ForwardingJavaFileManager {

        private JavaEvalClassLoader classLoader;

        public InMemoryFileManager(JavaFileManager fileManager, JavaEvalClassLoader classLoader) {
            super(fileManager);
            this.classLoader = classLoader;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            InMemoryFileObject fileObject = new InMemoryFileObject(String.format("file:///%s.class", className));
            classLoader.addClassFileObject(className, fileObject);
            return fileObject;
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            if(location == StandardLocation.CLASS_PATH){
                return classLoader;
            }
            return super.getClassLoader(location);
        }
    }

    private void addClassFileObject(String binaryName, InMemoryFileObject fileObject) {
        this.fileObjectMap.put(binaryName, fileObject);
    }
}
