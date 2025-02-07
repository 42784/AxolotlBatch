package github.axolotl.main.grammar.util;

import static github.axolotl.main.grammar.util.ConfigUtil.isLog;

public class LogUtil {

    public static void log(String msg) {
        if (isLog)
            System.out.printf("\u001B[32m%s\u001B[0m\n", msg);
    }

    public static void log(String format, Object... args) {
        if (isLog)
            System.out.printf("\u001B[32m%s\u001B[0m\n", format.formatted(args));
    }
}
