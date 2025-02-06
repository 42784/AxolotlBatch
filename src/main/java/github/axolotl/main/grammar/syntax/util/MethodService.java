package github.axolotl.main.grammar.syntax.util;

import dczx.axolotl.command.CommandResult;
import dczx.axolotl.command.DefaultExecutor;
import dczx.axolotl.util.FileUtil;
import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.syntax.Method;
import github.axolotl.main.grammar.syntax.Sentence;
import github.axolotl.main.grammar.syntax.Syntax;
import github.axolotl.main.grammar.util.InitParser;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static github.axolotl.main.GlobalVariable.requestValue;
import static github.axolotl.main.grammar.syntax.util.PolymorphismUtil.getFile;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 12:12
 */
public class MethodService {
    //TODO [A] 通过反射 基于Variable的类型调用可用的成员方法（这需要大幅度修改代码结构）
    //TODO [A] 修改代码结构，使用其他方式维护方法，避免变量传递的臃肿
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
        regSimpleMethod();//常用
    }

    private static void regSimpleMethod() {
        registerMethod("GetTimeMillis", v -> System.currentTimeMillis());
        registerMethod("GetTime", v -> System.currentTimeMillis() / 1000);
        registerMethod("Date", v -> new Date());
        registerMethod("DateFormat", v -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return simpleDateFormat.format(new Date());
        });
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
                String name = var.toString().trim();
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
            for (Object o : v) {
                builder.append(o);
            }
            return builder.toString();
        });
        registerMethod(UpdateVariable, v -> {
            requestValue(v[0]);
            return v[0];
        });
        registerMethod(Foreach, inputVar -> {
            String codeBlockName = (String) inputVar[1];

            String varName = ((String) inputVar[2]).replace("^", "");
            Object var = requestValue(varName);
            List<String> tempVarList = new ArrayList<>();//Foreach中的临时变量

            List<Sentence> sentences = InitParser.getCodeblocks_Sentence().get(codeBlockName);
            //由于未知原因 直接运行代码块的内容有问题 所以只好重新解析然后运行
            switch (var) {//已被转换为Var对象
                case Object[] objects -> {
                    for (Object object : objects) {
                        addTempSubField("#" + varName, object, tempVarList);
                        analyseAndRun(sentences);//解析并运行一次循环体
                    }
                }
                case Map<?, ?> map -> {
                    map.forEach((k, v) -> {
                        addTempSubField("#k", k, tempVarList);
                        addTempSubField("#v", v, tempVarList);
                        analyseAndRun(sentences);//解析并运行一次循环体
                    });
                }
                case Collection<?> collection -> {
                    collection.forEach(object -> {
                        addTempSubField("#" + varName, object, tempVarList);
                        analyseAndRun(sentences);//解析并运行一次循环体
                    });
                }
                default -> {
                    addTempSubField("#" + varName, var, tempVarList);
                    analyseAndRun(sentences);//解析并运行一次循环体
                }
            }


            tempVarList.forEach(GlobalVariable::removeVariable);//清空临时变量
            return null;
        });
    }

    private static void analyseAndRun(List<Sentence> sentences) {
        sentences.forEach(v -> {
            List<Syntax> analyze = StatementAnalyzer.analyze(v);
            analyze.forEach(Syntax::execute);
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
