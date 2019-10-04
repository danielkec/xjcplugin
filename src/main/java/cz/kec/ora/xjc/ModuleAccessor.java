package cz.kec.ora.xjc;

public abstract class ModuleAccessor {
    public abstract String getModuleName(Class<?> clazz);

    public abstract boolean isModuleNamed(Class<?> clazz);
}