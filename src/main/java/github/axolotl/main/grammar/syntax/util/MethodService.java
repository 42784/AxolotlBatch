package github.axolotl.main.grammar.syntax.util;

import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.syntax.Method;
import github.axolotl.main.grammar.syntax.Syntax;
import github.axolotl.main.grammar.util.InitParser;

import java.io.File;
import java.util.*;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 12:12
 */
public class MethodService {
    public static final String SetVariable = "$setVariable";
    public static final String Foreach = "$Foreach";
    private static final HashMap<String, MethodEntity> methods = new HashMap<>();

    public static MethodEntity getMethod(String methodName) {
        return methods.getOrDefault(methodName, emptyArgs -> {
            System.err.println("未被定义的方法: " + methodName);
            return null;
        });
    }

    public static void registerMethod(String methodName, MethodEntity method) {
        methods.put(methodName, method);
    }

    static {
        regDefaultMethod();//必备方法 优先注册
        regSystemOutputMethod();//基本输出类
        regFileMethod();//文件相关
    }

    private static void regSystemOutputMethod() {
        registerMethod("println", v -> {
            for (Object variable : v) {
                if (variable instanceof Object[]) {
                    System.out.print(Arrays.toString((Object[]) variable));
                } else {
                    System.out.print(variable.toString());
                }
            }
            System.out.println();
            return null;
        });
    }

    private static void regFileMethod() {
        registerMethod("getFile", v -> {
            return new File(String.valueOf(v[0]));
        });
        registerMethod("listFiles", v -> {
            return ((File) v[0]).listFiles();
        });
    }

    private static void regDefaultMethod() {
        registerMethod(SetVariable, v -> {
            Object var = v[1];
            try {//字面量和函数返回值的处理
                String name = ((String) var).trim();
                if (name.startsWith(InitParser.StringSymbol)) {
                    var = GlobalVariable.requestValue(name);
                }

                if (name.contains("(") && name.contains(")")) {//不支持方法嵌套
                    Method method = StatementAnalyzer.tryGetMethod(name.trim());
                    var = method.execute();
                }
            } catch (Exception ignored) {
            }

            GlobalVariable.addVariable((String) v[0], var);
            return null;
        });
        registerMethod(Foreach, inputVar -> {
            String codeBlockName = (String) inputVar[1];
            List<Syntax> syntaxes = InitParser.getCodeblocks_Syntax().get(codeBlockName);//获取Foreach体

            String varName = ((String) inputVar[2]).replace("#","");
            Object var = GlobalVariable.requestValue(varName);
            List<String> tempVarList = new ArrayList<>();//Foreach中的临时变量
            switch (inputVar[0]) {//已被转换为Var对象
                case Object[] objects -> {
                    for (Object object : objects) {
                        addTempSubField("#" + varName, object, tempVarList);
                        syntaxes.forEach(Syntax::execute);//运行一次循环体
                    }
                }
                case Map<?, ?> map -> {
                    map.forEach((k, v) -> {
                        addTempSubField("#k", k, tempVarList);
                        addTempSubField("#v", v, tempVarList);
                        syntaxes.forEach(Syntax::execute);//运行一次循环体
                    });
                }
                case Collection<?> collection -> {
                    collection.forEach(object -> {
                        addTempSubField("#" + varName, object, tempVarList);
                        syntaxes.forEach(Syntax::execute);//运行一次循环体
                    });
                }
                default -> throw new IllegalStateException("Unexpected value: " + var);
            }


            tempVarList.forEach(GlobalVariable::removeVariable);//清空临时变量
            return null;
        });
    }

    /**
     * 添加临时变量
     *
     * @param varName     变量名
     * @param var         变量
     * @param tempVarList 添加了的会加到这个list
     */
    private static void addTempVar(String varName, Object var, List<String> tempVarList) {
        tempVarList.add(varName);
        GlobalVariable.addVariable(varName, var);
    }

    /**
     * 添加一个变量包括其常用字段临时变量
     */
    private static void addTempSubField(String varName, Object var, List<String> tempVarList) {
        addTempVar(varName, var, tempVarList);
        switch (var) {
            case File file -> {
                addTempVar(varName + "#path", file.getPath(), tempVarList);
                addTempVar(varName + "#name", file.getName(), tempVarList);
            }

            default -> {
            }
        }
    }

}
