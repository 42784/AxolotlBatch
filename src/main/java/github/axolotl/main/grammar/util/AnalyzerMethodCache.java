package github.axolotl.main.grammar.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AnalyzerMethodCache {
    public static final Map<String, Function<String, Object>> cacheTable = new HashMap<>();

    public static boolean isCache(final String method) {//检查有没有被缓存
        return cacheTable.containsKey(method);

    }

    public static Function<String, Object> getCache(final String method) {
        return cacheTable.get(method);
    }

    //缓存并且运行
    public static Object cacheAndRun(final String method, final Function<String, Object> function) {
        cacheTable.put(method, function);
        return function.apply(method);
    }

    //运行缓存了的程序
    public static Object executeCache(final String method) {
        return getCache(method).apply(method);
    }
}
