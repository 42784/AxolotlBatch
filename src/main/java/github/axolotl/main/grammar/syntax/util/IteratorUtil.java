package github.axolotl.main.grammar.syntax.util;


import java.util.ArrayList;
import java.util.List;

public class IteratorUtil {
    public static List<Integer> intRange(int start, int end, int iterator) {
        // 计算数组大小
        int size = Math.abs((end - start) / iterator) + 1;
        ArrayList<Integer> result = new ArrayList<>(size);

        // 填充数组 可以正逆
        if (iterator > 0) {
            // 正序生成
            for (int i = 0, value = start; i < size; i++, value += iterator) {
                result.add(value);
            }
        } else {
            // 逆序生成
            for (int i = 0, value = start; i < size; i++, value += iterator) {
                result.add(value);
            }
        }
        return result;
    }

    public static List<Integer> intRange(int start, int end) {
        return intRange(start, end, 1);
    }
}
