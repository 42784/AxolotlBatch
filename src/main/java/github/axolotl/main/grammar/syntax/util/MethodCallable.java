package github.axolotl.main.grammar.syntax.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Arrays;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 12:19
 */
@Data
@AllArgsConstructor
public class MethodCallable {
    private IMethod method;

    public Object[] requestValue(String... parametersName) {
        Object[] variables = new Object[parametersName.length];
        for (int i = 0; i < parametersName.length; i++) {
            parametersName[i] = parametersName[i].trim();
            Object requestValue = StatementAnalyzer.requestValueForSyntax(parametersName[i]);
            variables[i] = requestValue;
//            System.out.printf("Request: %s -> %s\n", parametersName[i], requestValue);
        }
        return variables;
    }


    @SneakyThrows
    public Object call(String methodName, String... parametersName) {
        Object[] variables = requestValue(parametersName);//刷新参数
//        System.out.printf("[%s]parametersName = %s\n", methodName, Arrays.toString(parametersName));
//        System.out.printf("[%s]variables = %s\n", methodName, Arrays.toString(variables));
        return method.execute(methodName, parametersName, variables);
    }

    @SneakyThrows
    public Object call(String methodName, String[] parametersName, Object[] variables) {
//        System.out.printf("[%s]parametersName = %s\n", methodName, Arrays.toString(parametersName));
//        System.out.printf("[%s]variables = %s\n", methodName, Arrays.toString(variables));
        return method.execute(methodName, parametersName, variables);
    }

}

