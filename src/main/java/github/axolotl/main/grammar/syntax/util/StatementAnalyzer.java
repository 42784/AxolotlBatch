package github.axolotl.main.grammar.syntax.util;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.syntax.Method;
import github.axolotl.main.grammar.syntax.Syntax;
import github.axolotl.main.grammar.syntax.Sentence;
import github.axolotl.main.grammar.util.InitParser;

import java.util.*;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 11:22
 */
//语句分析器
public class StatementAnalyzer {
    /**
     * 解析语句
     *
     * @param sentence 语句
     * @return 可执行的语法
     */
    private static final HashMap<String, List<Syntax>> codeblocksSyntax = InitParser.getCodeblocks_Syntax();

    public static List<Syntax> analyze(Sentence sentence) {
        ArrayList<Syntax> syntaxes = new ArrayList<>();

        String text = sentence.getSentence();
        text = text.trim();//去除前后空格

        if (text.startsWith(InitParser.CodeBlockSymbol)) {
            syntaxes.addAll(codeblocksSyntax.get(text.replace(";", "")));
        }
        if (text.contains("=")) {
            String[] split = text.replace(";", "").split("=");
            int len = split.length;
            for (int i = 0; i < len - 1; i++) {
                syntaxes.add(new Method(MethodService.SetVariable, new Object[]{split[i].trim(), split[len - 1]}));
            }
        }

        if (text.contains("(") && text.contains(")")) {//不支持方法嵌套
            //TODO [B] 类似于CodeBlock一样支持方法的嵌套
            int start = text.indexOf("(");
            int end = text.indexOf(")");
            String methodName = text.substring(0, start);
            String params = text.substring(start + 1, end);
            System.out.println("methodName = " + methodName);
            System.out.println("params = " + params);
            Object[] variables = Arrays.stream(params.split(","))
                    .toArray(Object[]::new);

            syntaxes.add(new Method(methodName, variables));
        }

        return syntaxes;
    }

    public static List<Syntax> analyze(List<Sentence> sentences) {
        ArrayList<Syntax> syntaxes = new ArrayList<>();
        sentences.stream().map(StatementAnalyzer::analyze).forEach(syntaxes::addAll);
        return syntaxes;
    }
}
