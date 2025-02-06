package github.axolotl.main.grammar.syntax;

import github.axolotl.main.grammar.syntax.util.MethodEntity;
import github.axolotl.main.grammar.syntax.util.MethodService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Arrays;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 11:56
 */
@Data
@AllArgsConstructor
public class Method implements Syntax {
    private String methodName;
    private MethodEntity method;
    private Object[] parameters;

    public Method(String methodName, Object[] parameters) {
        this.methodName = methodName;
        method = MethodService.getMethod(methodName);
        this.parameters = parameters;
    }

    @Override
    @SneakyThrows
    public Object execute() {
        Object[] value = method.requestValue(parameters);//注意要请求一下参数
//        System.out.println(methodName + ": parameters = " + Arrays.toString(parameters));
//        System.out.println(methodName + ": value = " + Arrays.toString(value));
        return method.execute(value);
    }
}
