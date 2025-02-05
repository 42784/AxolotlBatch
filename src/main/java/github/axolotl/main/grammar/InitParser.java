package github.axolotl.main.grammar;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.variable.StringVariable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/5 22:22
 */

public class InitParser {


    // 解析器主方法
    public static void parse(String code) {
        AtomicReference<String> reference = new AtomicReference<>(code);//为了引用传递 使得方法可以修改code
        parseString(reference);
        System.out.println("reference.get() = " + reference.get());
        System.out.println("==================================");
//        parseCodeBlock(optional);
        System.out.println("==================================");


    }

    /**
     * 处理字符串，将字符串字面量存储起来
     */
    private static void parseString(AtomicReference<String> reference) {
        String code = reference.get();
        char[] chars = code.toCharArray();
        List<String> strings = new ArrayList<>();
        boolean flag = false;
        int start = -1;//前引号
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

    /**
     * 解析代码块
     * 优先解析最最小字串的
     */
    //FIXME 希望返回最小代码块，然后替换处理的 但是失败了，稍后做
    private static void parseCodeBlock(String code) {
        int[][] pairs = findPairs(code, '{', '}');
        for (int i = 0; i < pairs.length; i++) {
            int[] pair = pairs[i];
            char[] chars = code.toCharArray();
//            System.out.printf("[%d,%d]:%c,%c\n", pair[0], pair[1], chars[pair[0]], chars[pair[1]]);
            System.out.printf("[%d][%d,%d,%d]: %s\n", i, pair[0], pair[1], pair[2],
                    new String(
                            Arrays.copyOfRange(chars, pair[0], pair[1] + 1)
                    ).replace("\n", " ").replace("  ", " "), ""
            );
            if (pair[2] > 0) {
                int cnt = pair[2];
                for (int j = 0; j < cnt; j++) {
                    System.out.printf("%d ", pair[3 + j]);
                }
                System.out.println();
            }

        }
    }

    public static int[][] findPairs(String input, char openChar, char closeChar) {
        List<int[]> pairs = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();

        // 第一步：找到所有括号对
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == openChar) {
                stack.push(i);
            } else if (currentChar == closeChar) {
                if (!stack.isEmpty()) {
                    int openIndex = stack.pop();

                    pairs.add(new int[]{openIndex, i, 0}); // 初始化第三个元素为 0
                }
            }
        }

        // 第二步：处理嵌套逻辑，统计子代码块数量并记录子代码块的索引
        for (int i = 0; i < pairs.size(); i++) {
            int[] currentPair = pairs.get(i);
            int start = currentPair[0];
            int end = currentPair[1];
            int childCount = 0;
            List<Integer> childIndices = new ArrayList<>();

            // 遍历所有括号对，寻找嵌套在当前括号对中的子代码块
            for (int j = 0; j < pairs.size(); j++) {
                if (i == j) continue; // 跳过自身

                int[] childPair = pairs.get(j);
                int childStart = childPair[0];
                int childEnd = childPair[1];

                // 检查当前子代码块是否被包含在当前括号对中
                if (start < childStart && childEnd < end) {
                    childCount++;
                    childIndices.add(j); // 记录子代码块的索引
                }
            }

            // 更新当前括号对的子代码块数量和子代码块索引
            currentPair[2] = childCount; // 第三个维度存储子代码块数量
            if (childCount > 0) {
                // 创建一个新的数组来存储完整的括号对信息
                int[] expandedPair = new int[3 + childCount];
                for (int k = 0; k < 3; k++) {
                    expandedPair[k] = currentPair[k];//复制currentPair前3项
                }

                // 将子代码块索引依次存入从 index[3] 开始的位置
                for (int k = 0; k < childIndices.size(); k++) {
                    expandedPair[3 + k] = childIndices.get(k);
                }

                pairs.set(i, expandedPair);
            }
        }

        // 将结果转换为二维数组
        int[][] result = new int[pairs.size()][];
        for (int i = 0; i < pairs.size(); i++) {
            result[i] = pairs.get(i);
        }

        for (int i = pairs.size(); i > 0; i--) {
            int cnt = result[i][3];
            for (int j = cnt; j > 0; j--) {

            }
        }

        return result;
    }


}