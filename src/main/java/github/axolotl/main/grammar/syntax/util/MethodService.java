package github.axolotl.main.grammar.syntax.util;

import dczx.axolotl.command.CommandResult;
import dczx.axolotl.command.DefaultExecutor;
import dczx.axolotl.util.FileUtil;
import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.syntax.Method;
import github.axolotl.main.grammar.syntax.Syntax;
import github.axolotl.main.grammar.util.InitParser;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;

import static github.axolotl.main.GlobalVariable.requestValue;
import static github.axolotl.main.grammar.syntax.util.PolymorphismUtil.getFile;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 12:12
 */
public class MethodService {
    public static final String SetVariable = "$setVariable";
    public static final String UpdateVariable = "#";
    public static final String Foreach = "$Foreach";
    public static final String StringAppend = "+";
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
        regExecMethod();//命令操作相关
    }

    static DefaultExecutor defaultExecutor = new DefaultExecutor(getExecHome());

    private static void regExecMethod() {
        registerMethod("exec", v -> {
            System.out.println("------>" + Arrays.toString((Object[]) v));
            return defaultExecutor.runCommand(getExecPreFix() + v[0]);
        });
        registerMethod("execWithHome", v -> {
            if (getExecHome().equals(v[1]))
                return defaultExecutor.runCommand(getExecPreFix() + v[0]);
            else {
                DefaultExecutor defaultExecutor = new DefaultExecutor((String) v[1]);
                return defaultExecutor.runCommand(getExecPreFix() + v[0]);
            }
        });
    }

    private static String getExecPreFix() {
        return (String) GlobalVariable.getVariable(GlobalVariable.EXEC_PREFIX);
    }

    private static String getExecHome() {
        return (String) GlobalVariable.getVariable(GlobalVariable.EXEC_HOME);
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
        registerMethod("print", v -> {
            for (Object variable : v) {
                if (variable instanceof Object[]) {
                    System.out.print(Arrays.toString((Object[]) variable));
                } else {
                    System.out.print(variable.toString());
                }
            }
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
        registerMethod("moveFile", v -> {
            File oldfile = getFile(v[0]);
            File newfile = getFile(v[1]);
            FileUtil.keepFileExists(newfile.getPath());
            if (oldfile.isDirectory()) return false;
            boolean newFileExists = newfile.exists();//新位置有没有文件
            oldfile.renameTo(newfile);// 覆盖
            return newFileExists;
        });
        registerMethod("copyFile", v -> {//自动创建文件
            File oldfile = getFile(v[0]);
            File newfile = getFile(v[1]);
            FileUtil.keepFileExists(newfile.getPath());
            if (oldfile.isDirectory()) return false;
            FileOutputStream out = new FileOutputStream(newfile);
            long copy = Files.copy(oldfile.toPath(), out);
            out.close();
            return copy;
        });
    }

    private static void regDefaultMethod() {
        registerMethod(SetVariable, v -> {
            Object var = v[1];
            try {//字面量和函数返回值的处理
                String name = ((String) var).trim();
                if (name.startsWith(InitParser.StringSymbol)) {
                    var = requestValue(name);
                }

                if (name.contains("(") && name.contains(")")) {//不支持方法嵌套
                    Method method = StatementAnalyzer.tryGetMethod(name.trim());
                    var = method.execute();
                }
            } catch (Exception ignored) {
            }
            GlobalVariable.addVariable(((String) v[2]).replace("^", ""), var);
            return null;
        });
        registerMethod(StringAppend, v -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < v.length; i++) {
                builder.append(v[i]);
            }
            return builder.toString();
        });
        registerMethod(UpdateVariable, v -> {
            requestValue(v[0]);
            return v[0];
        });
        registerMethod(Foreach, inputVar -> {
            String codeBlockName = (String) inputVar[1];
            List<Syntax> syntaxes = InitParser.getCodeblocks_Syntax().get(codeBlockName);//获取Foreach体

            String varName = ((String) inputVar[2]).replace("#", "");
            Object var = requestValue(varName);
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
                        requestValue(inputVar);
                        addTempSubField("#" + varName, object, tempVarList);
                        syntaxes.forEach(Syntax::execute);//运行一次循环体
                    });
                }
                default -> {
                    addTempSubField("#" + varName, var, tempVarList);
                    syntaxes.forEach(Syntax::execute);//运行一次循环体
                }
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
            case CommandResult result -> {
                addTempVar(varName + "#out", result.getOut(), tempVarList);
                addTempVar(varName + "#err", result.getErr(), tempVarList);
                addTempVar(varName + "#exitcode", result.getExitCode(), tempVarList);
            }

            default -> {
            }
        }
    }

}
