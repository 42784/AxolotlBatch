package github.axolotl.main.grammar.syntax.util;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.util.InitParser;

import java.util.*;

import static github.axolotl.main.GlobalVariable.requestValue;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 11:22
 */
//语句分析器
public class StatementAnalyzer {

    private static final HashMap<String, List<String>> codeblocksSentence = InitParser.getCodeblocks_Sentence();

    /**
     * 解析语句
     *
     * @param sentence 语句
     * @return 可执行的语法
     */
    public static Object analyzeAndRun(String sentence) {
        sentence = sentence.replace(";", "").trim();//先去除分号
        if (sentence.startsWith(InitParser.CodeBlockSymbol)) {
            codeblocksSentence.get(sentence).forEach(StatementAnalyzer::analyzeAndRun);//解析运行代码块
        }
        if (sentence.contains("=")) {
            String[] split = sentence.split("=");
            int len = split.length;

            String text = split[len - 1];
            Object result = requestValueForSyntax(text);

            for (int i = 0; i < len - 1; i++) {
                MethodService.call(MethodService.SetVariable, new String[]{split[i].trim()}, new Object[]{result});
            }
            return result;

        }

        if (sentence.contains("->")) {//Foreach
            String[] split = sentence.split("->");
            return MethodService.call(MethodService.Foreach,
                    split[0].trim(), split[1].trim());
        }


        if (sentence.contains("(") && sentence.contains(")")) {//不支持方法嵌套
            return requestValueForSyntax(sentence);
        }
        return null;

    }


    /**
     * 获取变量的值或
     * 获取一段语句的结果 可以是嵌套的方法调用
     */
    public static Object requestValueForSyntax(String text) {
        Object var = text.trim();
        try {//字面量和函数返回值的处理
            String name = var.toString();

            if (GlobalVariable.requestValue(name) != null) {
                var = requestValue(name);
            }

            if (name.contains("(") && name.contains(")")) {//不支持方法嵌套
                String methodName = name.substring(0, name.indexOf("("));
                String[] params = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")")).split(",");//此时还是有空格的
                var = MethodService.call(methodName, params);

            }
        } catch (Exception ignored) {
        }
        return var;
    }

    public static List<Object> analyzeAndRun(List<String> sentences) {
        ArrayList<Object> returns = new ArrayList<>();
        sentences.stream().map(StatementAnalyzer::analyzeAndRun).forEach(returns::add);
        return returns;
    }
}
