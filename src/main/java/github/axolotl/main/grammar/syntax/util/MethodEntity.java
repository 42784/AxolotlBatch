package github.axolotl.main.grammar.syntax.util;


import github.axolotl.main.GlobalVariable;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;

import static github.axolotl.main.grammar.syntax.util.PolymorphismUtil.convertArg;

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


    @SneakyThrows
    Object execute(Object[] args) throws Exception;
}
