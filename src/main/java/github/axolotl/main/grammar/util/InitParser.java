package github.axolotl.main.grammar.util;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.syntax.Syntax;
import github.axolotl.main.grammar.syntax.Sentence;
import github.axolotl.main.grammar.syntax.util.StatementAnalyzer;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/5 22:22
 */

public class InitParser {
    public static final String CodeBlockSymbol = "$CODE_BLOCK";
    public static final String StringSymbol = "$Str";
    @Getter
    private static final HashMap<String, List<Sentence>> codeblocks_Sentence = new HashMap<>();//第一步 分句 句子
    @Getter
    private static final HashMap<String, List<Syntax>> codeblocks_Syntax = new HashMap<>();//第二步 转换 语句

    // 解析器主方法
    public static void parse(String code) {
        AtomicReference<String> reference = new AtomicReference<>(code);//为了引用传递 使得方法可以修改code
        parseString(reference);//字符串处理
        System.out.println("==================================");

        List<Sentence> list = parseCodeBlock(reference.get(), 0, CodeBlockSymbol + 0);//代码块解析

        list.stream().map(Sentence::getSentence).forEach(s -> System.out.printf("Run: %s\n", s));
        System.out.println("==================================");

        /*codeblocks_Sentence.forEach((k, v) -> {
            System.out.println(k + " = " + v.toString().replace("\n", " "));
        });*/


        System.out.println("==================================");
        codeblocks_Sentence.forEach((key, value) -> {
            codeblocks_Syntax.put(key, StatementAnalyzer.analyze(value));
        });//句法转换
        System.out.println("================Output================");
//        System.out.println("codeblocks_Sentence = " + codeblocks_Sentence);
//        System.out.println("codeblocks_Syntax = " + codeblocks_Syntax);
        codeblocks_Syntax.get(CodeBlockSymbol + 0).forEach(Syntax::execute);//执行主方法每一句


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
    private static List<Sentence> parseCodeBlock(String code, int start, String id) {
        ArrayList<Sentence> sentences = new ArrayList<>();//本代码块的语句
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
                sentences.add(new Sentence(outputCode.toString().replace("\n", "")));
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
            String stringVar = strings.get(i);
            String varName = StringSymbol + i;
            code = code.replace("\"%s\"".formatted(stringVar), varName);
            //TODO [C] 添加转义字符的支持
            GlobalVariable.addVariable(varName,stringVar);//添加全局变量
        }
        reference.set(code);//设置替换后的代码
    }


}