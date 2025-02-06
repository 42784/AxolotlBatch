package github.axolotl.main.grammar;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.variable.StringVariable;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/5 22:22
 */

public class InitParser {
    @Getter
    private static final HashMap<String, List<Sentence>> codeblocks = new HashMap<>();

    // 解析器主方法
    public static void parse(String code) {
        AtomicReference<String> reference = new AtomicReference<>(code);//为了引用传递 使得方法可以修改code
        parseString(reference);
        System.out.println("reference.get() = " + reference.get());
        System.out.println("==================================");
        List<Sentence> list = parseCodeBlock(reference.get(), 0, "$C0B$");
        System.out.println("list = " + list);
        System.out.println("==================================");
        codeblocks.forEach((k, v) -> {
            System.out.println(k + " = " + v.toString().replace("\n", " "));
        });
        System.out.println("==================================");


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
        int length = 0;//outputCode的长度
        int deepCnt = 0;//进入的层级
        for (int i = start; i < chars.length; i++) {
            char aChar = chars[i];

            if (aChar == '{') {
                if (deepCnt == 0) {
                    sentences.add(new Sentence(SentenceType.CodeBlock, "$C" + cnt + "B$"));
                    parseCodeBlock(code, i + 1, "$C" + cnt++ + "B$");
//                    cnt++;
                }

                deepCnt++;
                continue;
            }

            if (aChar == '}') {
                //找到闭合括号 处理前括号
                deepCnt--;
                if (deepCnt < 0) {
//                break;//结束本代码块分析
                    codeblocks.put(id,
                            sentences
                    );
                    break;
                }
                continue;
            }

            if (deepCnt == 0) {
                outputCode.append(aChar);
                length++;
            }
            if (length == 1 && aChar == ';') {//行首的分号不要
                outputCode = new StringBuilder();
                length = 0;
            }
            if (aChar == ';' && deepCnt == 0) {
                if (outputCode.toString().replace(" ", "").replace("\n", "").length() == 1)
                    continue;//只有一个分号
                sentences.add(new Sentence(SentenceType.Sentence, outputCode.toString()));
                outputCode = new StringBuilder();
                length = 0;
            }

        }
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
            String varName = "$Str" + i;
            code = code.replace("\"%s\"".formatted(stringVar), varName);
            //TODO [C] 添加转义字符的支持
            GlobalVariable.addVariable(varName, new StringVariable(stringVar));//添加全局变量
        }
        reference.set(code);//设置替换后的代码
    }



}