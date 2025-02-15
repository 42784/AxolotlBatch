package github.axolotl.main.grammar.syntax.util;

import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author AxolotlXM
 * @Created by Axolotl
 * @Date 2025/2/15 23:34
 */
//处理对象到值的关系
public class ValueUtil {
    public static OptionalInt getInt(Object o) {
        switch (o) {
            case Number num -> {
                return OptionalInt.of(num.intValue());
            }
            case String str -> {
                return OptionalInt.of(Integer.parseInt(str));
            }
            default -> {
                return OptionalInt.empty();
            }
        }
    }
    public static OptionalLong getLong(Object o) {
        switch (o) {
            case Number num -> {
                return OptionalLong.of(num.longValue());
            }
            case String str -> {
                return OptionalLong.of(Long.parseLong(str));
            }
            default -> {
                return OptionalLong.empty();
            }
        }
    }
}
