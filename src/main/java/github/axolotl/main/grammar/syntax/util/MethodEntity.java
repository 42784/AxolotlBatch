package github.axolotl.main.grammar.syntax.util;


import github.axolotl.main.GlobalVariable;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 12:19
 */
public interface MethodEntity {
    default Object[] requestValue(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object requestValue = GlobalVariable.requestValue(args[i]);
            if (requestValue != null)
                args[i] = requestValue;
        }
        return args;
    }

    Object execute(Object[] args);
}
