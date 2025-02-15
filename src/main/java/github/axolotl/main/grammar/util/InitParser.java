package github.axolotl.main.grammar.util;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.syntax.util.StatementAnalyzer;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static github.axolotl.main.grammar.util.LogUtil.log;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/5 22:22
 */

public class InitParser {
    public static final String CodeBlockSymbol = "$CODE_BLOCK";
    public static final String MainBlock = "$MainBlock";
    public static final String StringSymbol = "$Str";
    @Getter
    private static final HashMap<String, List<String>> codeblocks_Sentence = new HashMap<>();

    // 解析器主方法
    public static void parse(String code) {
        //Windows下的换行符处理
        AtomicReference<String> reference = new AtomicReference<>(code.replace("\r\n", "\n"));//为了引用传递 使得方法可以修改code
        parseNote(reference);//注释处理
        parseString(reference);//字符串处理
        parseStringAppend(reference);//语法糖 + 连接字符串

        List<String> mainCodes = parseCodeBlock(reference.get(), 0, MainBlock);//代码块解析
        System.out.println("==================Output==================");
        codeblocks_Sentence.forEach( (key, block) -> {
            block.replaceAll(s -> s.replace(";", ""));//处理分号提早 防止重复处理
        });

        //TODO [A] 分离执行和解析(载入)
        //TODO [A] 允许载入外部方法和执行外部方法
        mainCodes.forEach(sentence -> {
            log("[InitParser]执行语句: %s", sentence);
            //由全部解析，修改为动态的解析执行
            StatementAnalyzer.analyzeAndRun(sentence);//执行主方法每一句
        });


    }

    /**
     * 语法糖 字符串拼接
     * 此部分由DeepseekR1生成
     */
    public static void parseStringAppend(AtomicReference<String> reference) {
        String code = reference.get();
        while (true) {
            int plusIndex = code.indexOf('+');
            if (plusIndex == -1) break;

            // 找到+左边的操作数
            String leftOperand = extractOperand(code, plusIndex - 1, -1);

            // 找到+右边的操作数
            String rightOperand = extractOperand(code, plusIndex + 1, 1);

            // 构建 append 函数调用
            String appendCall = "append(" + leftOperand + ", " + rightOperand + ")";

            // 替换原始的 + 表达式
            String leftPart = code.substring(0, code.indexOf(leftOperand, plusIndex - leftOperand.length()));
            String rightPart = code.substring(code.indexOf(rightOperand, plusIndex + 1) + rightOperand.length());
            code = leftPart + appendCall + rightPart;
        }
        reference.set(code); // 更新代码
    }

    /**
     * 提取操作数
     */
    private static String extractOperand(String code, int start, int direction) {
        int index = start;
        StringBuilder operand = new StringBuilder();
        int balance = 0; // 用于匹配括号

        while (index >= 0 && index < code.length()) {
            char ch = code.charAt(index);

            // 处理括号
            if (ch == '(') {
                if (direction == -1) break; // 左操作数遇到左括号，结束
                balance++;
            } else if (ch == ')') {
                if (direction == 1 && balance == 0) break; // 右操作数遇到右括号，结束
                balance--;
            }

            // 如果不是操作数的有效字符，结束
            if (balance < 0 || (ch != ' ' && ch != '+' && !isValidOperandChar(ch))) {
                break;
            }

            // 添加字符到操作数
            if (direction == -1) {
                operand.insert(0, ch); // 左操作数从后往前添加
            } else {
                operand.append(ch); // 右操作数从前往后添加
            }

            index += direction;
        }

        return operand.toString().trim();
    }

    /**
     * 检查字符是否是操作数的有效字符
     */
    private static boolean isValidOperandChar(char ch) {
        // 允许字母、数字、下划线和括号
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '(' || ch == ')' || ch == '$' || ch == '#';
    }


    /**
     * 剔除//注释后的内容
     */
    private static void parseNote(AtomicReference<String> reference) {
        String code = reference.get();
        char[] chars = code.toCharArray();
        StringBuilder sb = new StringBuilder();

        boolean inComment = false;
        for (int i = 0; i < chars.length; i++) {
            if (i < chars.length - 1 && chars[i] == '/' && chars[i + 1] == '/') {
                inComment = true;
                i++;//下一个是/ 不用管
            } else if (chars[i] == '\n' && inComment) {
                inComment = false;
                sb.append(chars[i]); //下一行了，允许添加
            } else if (!inComment) {
                sb.append(chars[i]);
            }
        }

        reference.set(sb.toString());


    }

    static int cnt = 1;

    /**
     * 解析代码块
     *
     * @param code  代码
     * @param start 起始的位置
     * @param id    这一段解析代码的id
     * @return 一句一句代码
     */
    private static List<String> parseCodeBlock(String code, int start, String id) {
        ArrayList<String> sentences = new ArrayList<>();//本代码块的语句
        StringBuilder outputCode = new StringBuilder();//处理代码块逻辑后的代码
        char[] chars = code.toCharArray();
        int deepCnt = 0;//进入的层级
        for (int i = start; i < chars.length; i++) {
            char aChar = chars[i];

            if (aChar == '{') {
                if (deepCnt == 0) {
//                    sentences.add(new Sentence( "$C" + cnt + "B$"));
                    outputCode.append(CodeBlockSymbol + cnt);
                    parseCodeBlock(code, i + 1, CodeBlockSymbol + cnt++);
//                    cnt++;
                }

                deepCnt++;
                continue;
            }

            if (aChar == '}') {
                //找到闭合括号 处理前括号
                deepCnt--;
                if (deepCnt < 0) {

                    break;
                }
                continue;
            }

            if (deepCnt == 0) {
                outputCode.append(aChar);
            }

            if (aChar == ';' && deepCnt == 0) {
                if (outputCode.toString().replace(" ", "").replace("\n", "").length() <= 1)//无效的行
                    continue;//只有一个分号
                sentences.add(outputCode.toString().replace("\n", ""));
                outputCode = new StringBuilder();
            }

        }
        codeblocks_Sentence.put(id,
                sentences
        );
        return sentences;


    }

    /**
     * 处理字符串，将字符串字面量存储起来
     */
    private static void parseString(AtomicReference<String> reference) {
        String code = reference.get();
        char[] chars = code.toCharArray();
        List<String> strings = new ArrayList<>();
        boolean flag = false;
        int start = -1;//前引号+1(字符串开端)
        int length = chars.length;//数组长
        for (int i = 0; i < length; i++) {
            char aChar = chars[i];
            if (aChar == '\"') {
                if (!flag) {//找到前引号
                    flag = true;
                    start = i + 1;//引号后一字符
                } else {
                    flag = false;
                    String s = new String(chars, start, i - start);
                    strings.add(s);
                }
            }
        }

        for (int i = 0; i < strings.size(); i++) {
            String varName = StringSymbol + i;
            String stringVar = strings.get(i);
            code = code.replace("\"%s\"".formatted(stringVar), varName);//先替换源码

            //随后执行转义
            stringVar = stringVar
                    .replace("#t", "\t")
                    .replace("#n", "\n")
                    .replace("#r", "\r")
                    .replace("#b", "\b")
                    .replace("#f", "\f")
                    .replace("#1", "\"")
            ;
            GlobalVariable.addVariable(varName, stringVar);//添加全局变量
        }
        reference.set(code);//设置替换后的代码
    }


}