package github.axolotl.main.grammar.syntax.util;

import lombok.SneakyThrows;


public interface IMethod {

    /**
     * 方法的执行操作
     * @param parametersName 保持变量名称传入
     * @param variables 自动替换变量
     */
    @SneakyThrows
    Object execute(String methodName,String[] parametersName,Object[] variables) throws Exception;
}
