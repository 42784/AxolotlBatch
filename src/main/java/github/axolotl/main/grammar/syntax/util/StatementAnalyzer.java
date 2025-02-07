package github.axolotl.main.grammar.syntax.util;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.util.InitParser;

import java.util.*;

import static github.axolotl.main.GlobalVariable.requestValue;
import static github.axolotl.main.grammar.util.ConfigUtil.cheekVar;

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

        if (sentence.startsWith("def ")) {//Foreach
            String methodName = sentence.split("def ")[1].split("\\(")[0].trim();
            String[] paramsName = sentence.substring(sentence.indexOf("(") + 1, sentence.lastIndexOf(")")).split(",");//此时还是有空格的
            String codeBlock = sentence.split("\\)")[1].trim();
            String[] params = new String[paramsName.length + 2];//[方法名称,CodeBlock,方法参数]
            for (int i = 0; i < paramsName.length; i++) {
                params[i + 2] = paramsName[i].trim();
            }
            params[0] = methodName;
            params[1] = codeBlock;
//            System.out.println("params = " + Arrays.toString(params));
            return MethodService.call(MethodService.AddMethod,
                    params);
        }


        if (sentence.contains("(") && sentence.contains(")")) {
            return requestValueForSyntax(sentence, false);//直接调用的方法不检查返回值
        }
        return null;

    }


    /**
     * 获取变量的值或
     * 获取一段语句的结果 可以是嵌套的方法调用
     *
     * @param cheek 是否检查返回值
     */
    public static Object requestValueForSyntax(String text, boolean cheek) {
        Object var = null;
        try {//字面量和函数返回值的处理
            String name = text.trim();
            if (!cheekVar || !cheek) var = name;
            if (GlobalVariable.requestValue(name) != null) {
                var = requestValue(name);
            }

            if (name.contains("(") && name.contains(")")) {
                String methodName = name.substring(0, name.indexOf("("));
                List<String> paramsList = getParamsList(name);

                // 转换为数组
                String[] params = paramsList.toArray(new String[0]);
//                System.out.println("params = " + Arrays.toString(params));

                var = MethodService.call(methodName, params);
                if (cheekVar && cheek && var == null) {
                    System.err.printf("解析运算[%s]的时候出错，请检查函数[%s]是否可有返回值\n", methodName, methodName);
                }
                return var;
            }

        } catch (Exception ignored) {
        }
        if (cheekVar && cheek && var == null)
            System.err.printf("解析变量[%s]的时候出错，请检查[%s]是否可运算\n", text, text);
        return var;
    }

    /**
     * 获取参数列表
     * 该方法由AI修复
     */
    private static List<String> getParamsList(String name) {
        String paramsString = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));

        // 分割参数，支持嵌套方法
        List<String> paramsList = new ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        int balance = 0; // 用于跟踪括号的嵌套层级

        for (char ch : paramsString.toCharArray()) {
            if (ch == '(') {
                balance++;
            } else if (ch == ')') {
                balance--;
            }

            // 遇到逗号且不在嵌套括号内，分割参数
            if (ch == ',' && balance == 0) {
                paramsList.add(currentParam.toString().trim());
                currentParam.setLength(0); // 清空当前参数
            } else {
                currentParam.append(ch);
            }
        }

        // 添加最后一个参数
        if (!currentParam.isEmpty()) {
            paramsList.add(currentParam.toString().trim());
        }
        return paramsList;
    }

    public static Object requestValueForSyntax(String text) {
        return requestValueForSyntax(text, true);
    }

    public static List<Object> analyzeAndRun(List<String> sentences) {
        ArrayList<Object> returns = new ArrayList<>();
        sentences.stream().map(StatementAnalyzer::analyzeAndRun).forEach(returns::add);
        return returns;
    }
}
