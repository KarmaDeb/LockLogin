package ml.karmaconfigs.LockLogin.BungeeCord.Utils.PluginManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflections {
    public static final <T extends java.lang.reflect.AccessibleObject> T setAccessible(T t) {
        t.setAccessible(true);
        return t;
    }

    public static <T> T getFieldValue(Object obj, String fieldname) throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = obj.getClass();
        while (true) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldname))
                    return (T)((Field)setAccessible(clazz.getDeclaredField(fieldname))).get(obj);
            }
            if ((clazz = clazz.getSuperclass()) == null)
                throw new NoSuchFieldException("Can't find field " + fieldname);
        }
    }

    public static <T> T getStaticFieldValue(Class<?> clazz, String fieldname) throws IllegalAccessException, NoSuchFieldException {
        while (true) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldname))
                    return (T)((Field)setAccessible(clazz.getDeclaredField(fieldname))).get(null);
            }
            if ((clazz = clazz.getSuperclass()) == null)
                throw new NoSuchFieldException("Can't find field " + fieldname);
        }
    }

    public static void setFieldValue(Object obj, String fieldname, Object value) throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = obj.getClass();
        while (true) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldname)) {
                    ((Field)setAccessible(clazz.getDeclaredField(fieldname))).set(obj, value);
                    return;
                }
            }
            if ((clazz = clazz.getSuperclass()) == null)
                throw new NoSuchFieldException("Can't find field " + fieldname);
        }
    }

    public static void invokeMethod(Object obj, String methodname, Object... args) throws IllegalAccessException, InvocationTargetException {
        Class<?> clazz = obj.getClass();
        do {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodname) && (method.getParameterTypes()).length == args.length)
                    ((Method)setAccessible(method)).invoke(obj, args);
            }
        } while ((clazz = clazz.getSuperclass()) != null);
    }
}
