package github.axolotl.main.grammar.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AnalyzerSentenceCache {
    public static final Map<String, Function<String, Object>> cacheTable = new HashMap<>();

    public static boolean isCache(final String sentence) {//检查有没有被缓存
        return cacheTable.containsKey(sentence);

    }

    public static Function<String, Object> getCache(final String sentence) {
        return cacheTable.get(sentence);
    }

    //缓存并且运行
    public static Object cacheAndRun(final String sentence, final Function<String, Object> function) {
        cacheTable.put(sentence, function);
        return function.apply(sentence);
    }

    //运行缓存了的程序
    public static Object executeCache(final String sentence) {
        return getCache(sentence).apply(sentence);
    }
}
