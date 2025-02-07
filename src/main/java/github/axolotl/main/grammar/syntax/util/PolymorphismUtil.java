package github.axolotl.main.grammar.syntax.util;

import java.io.File;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 16:03
 */
public class PolymorphismUtil {
    /**
     * 尝试强转为数组里面的类型
     */
    static Object convertArg(Object arg, Class<?>... targetClasses) {
        for (Class<?> targetClass : targetClasses) {
            if (targetClass.isInstance(arg)) {
                return targetClass.cast(arg);
            }
        }
        return null; // 如果没有任何兼容的类型，返回 null
    }

    static Object convertArg(Object arg, Object defaultObj,Class<?>... targetClasses) {
        for (Class<?> targetClass : targetClasses) {
            if (targetClass.isInstance(arg)) {
                return targetClass.cast(arg);
            }
        }
        return defaultObj; // 如果没有任何兼容的类型，返回 null
    }

    /**
     * 获取File (多态String File)
     */
    static File getFile(Object var) {
        Object obj = convertArg(var, String.class, File.class);

        if (obj instanceof String)
            return new File((String) obj);
        else if (obj instanceof File)
            return (File) obj;
        return null;
    }
}
