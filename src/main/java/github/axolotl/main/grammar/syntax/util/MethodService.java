package github.axolotl.main.grammar.syntax.util;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.util.InitParser;

import java.util.HashMap;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 12:12
 */
public class MethodService {
    public static final String SetVariable = "$setVariable";
    private static final HashMap<String, MethodEntity> methods = new HashMap<>();

    public static MethodEntity getMethod(String methodName) {
        return methods.getOrDefault(methodName, emptyArgs -> {
            System.err.println("未被定义的方法: " + methodName);
            return null;
        });
    }

    public static void registerMethod(String methodName, MethodEntity method) {
        methods.put(methodName, method);
    }

    static {
        registerMethod(SetVariable, v -> {
            Object var = v[1];
            try {//字面量的处理
                String name = ((String) var).trim();
                if (name.startsWith(InitParser.StringSymbol)) {
                    var = GlobalVariable.requestValue(name);
                }
            } catch (Exception ignored) {
            }

            GlobalVariable.addVariable((String) v[0], var);
            return null;
        });
        registerMethod("println", v -> {
            for (Object variable : v) {
                System.out.print(variable);
            }
            System.out.println();
            return null;
        });
    }

}
