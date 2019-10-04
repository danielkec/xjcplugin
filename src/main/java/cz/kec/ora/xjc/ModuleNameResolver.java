package cz.kec.ora.xjc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Seems like resolving module name dynamically is kind of a bummer,
 * getModule method is part of a reflection api
 */
public class ModuleNameResolver {

    public static final String CLASS_NAME = "Module9Accessor";
    public static final String PACKAGE_NAME = "cz.kec.ora.xjc";
    public static final String FQDN = String.format("%s.%s", PACKAGE_NAME, CLASS_NAME);

    public static void main(String[] args) {
        ModuleAccessor moduleAccessor = generate();
        System.out.println("String.class module name: " + moduleAccessor.getModuleName(String.class));
        System.out.println("HelloWorldPlugin.class module name: " + moduleAccessor.isModuleNamed(HelloWorldPlugin.class));
    }

    private ModuleNameResolver() {
    }

    /**
     * Over-complicated approach, breaks on AntClassloader
     */
    @Deprecated
    public static ModuleAccessor generate() {
        final String source = "package cz.kec.ora.xjc;\n"
                + "public final class Module9Accessor extends ModuleAccessor {\n"
                + "    public String getModuleName(Class<?> clazz) {\n"
                + "        return clazz.getModule().getName();\n"
                + "    }\n"
                + "    public boolean isModuleNamed(Class<?> clazz) {\n"
                + "        return clazz.getModule().isNamed();\n"
                + "    }\n"
                + "}";
        ClassLoader parentCl = ModuleNameResolver.class.getClassLoader();
        JavaEvalClassLoader evalClassLoader = new JavaEvalClassLoader(parentCl);
        return evalClassLoader.eval(FQDN, source);
    }

    public static String resolve() {
            //TODO: find current class reference
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[1];
            Optional<Method> methodGetModuleName = Arrays.stream(traceElement.getClass().getMethods())
                    .filter(m -> m.getName().equals("getModuleName"))
                    .findFirst();
            if (methodGetModuleName.isPresent()) {
                try {
                    String moduleName = (String) methodGetModuleName.get().invoke(traceElement);
                    return String.format("%s@%s", traceElement.getClassName(), Optional.ofNullable(moduleName).orElse("unnamed"));
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        return "n/a";
    }


}
