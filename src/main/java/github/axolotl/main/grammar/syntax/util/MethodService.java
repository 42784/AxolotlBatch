package github.axolotl.main.grammar.syntax.util;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;
import dczx.axolotl.command.CommandResult;
import dczx.axolotl.command.DefaultExecutor;
import dczx.axolotl.util.FileUtil;
import github.axolotl.main.GlobalVariable;
import github.axolotl.main.grammar.util.InitParser;
import lombok.Getter;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static github.axolotl.main.grammar.syntax.util.PolymorphismUtil.convertArg;
import static github.axolotl.main.grammar.syntax.util.PolymorphismUtil.getFile;
import static github.axolotl.main.grammar.util.LogUtil.log;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 12:12
 */
public class MethodService {
    //TODO [A] 通过反射 基于Variable的类型调用可用的成员方法（这需要大幅度修改代码结构）
    //TODO [A] 修改代码结构，使用其他方式维护方法，避免变量传递的臃肿
    public static final String SetVariable = "$setVariable";
    public static final String Foreach = "$Foreach";
    public static final String AddMethod = "$AddMethod";
    public static final String StringAppend = "append";
    //仅用于newThread 定长线程池
    private static final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            ValueUtil.getInt(
                    GlobalVariable.requestValue(GlobalVariable.MAX_Threads)
            ).orElse(1)
    );


    @Getter
    private static final HashMap<String, MethodCallable> methods = new HashMap<>();
    private static final MethodCallable defaultMethod = new MethodCallable((n, p, v) -> {
        System.err.println("未被定义的方法: " + n);
        return null;
    });

    public static Object call(String methodName, String... parameters) {
        return getMethod(methodName).call(methodName, parameters);
    }

    public static Object call(String methodName, String[] parameters, Object[] variables) {
        return getMethod(methodName).call(methodName, parameters, variables);
    }

    private static MethodCallable getMethod(String methodName) {
        return methods.getOrDefault(methodName, defaultMethod);
    }

    public static void registerMethod(String methodName, IMethod method) {
        methods.put(methodName, new MethodCallable(method));
    }

    static {
        regDefaultMethod();//必备方法 优先注册
        regSystemOutputMethod();//基本输出类
        regFileMethod();//文件相关
        regExecMethod();//命令操作相关
        regSimpleMethod();//常用
    }

    private static void regSimpleMethod() {
        registerMethod("getTimeMillis", (n, p, v) -> System.currentTimeMillis());
        registerMethod("getTime", (n, p, v) -> System.currentTimeMillis() / 1000);
        registerMethod("getDate", (n, p, v) -> new Date());
        registerMethod("getDateFormat", (n, p, v) -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return simpleDateFormat.format(new Date());
        });
    }

    static DefaultExecutor defaultExecutor = new DefaultExecutor(getExecHome());

    private static void regExecMethod() {
        registerMethod("exec", (n, p, v) -> {
//            System.out.println("------>" + Arrays.toString((Object[]) v));
            return defaultExecutor.runCommand(getExecPreFix() + v[0]);
        });
        registerMethod("execWithHome", (n, p, v) -> {
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
        registerMethod("println", (n, p, v) -> {
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
        registerMethod("print", (n, p, v) -> {
            for (Object variable : v) {
                if (variable instanceof Object[]) {
                    System.out.print(Arrays.toString((Object[]) variable));
                } else {
                    System.out.print(variable.toString());
                }
            }
            return null;
        });
        registerMethod("printf", (n, p, v) -> {
            Object[] objects = Arrays.copyOfRange(v, 1, v.length);
            String out = v[0].toString().formatted(objects);
            System.out.println(out);
            return null;
        });
        registerMethod("newThread", (n, p, v) -> {
            List<String> sentences = InitParser.getCodeblocks_Sentence().get(p[0]);
            executorService.submit(() -> StatementAnalyzer.analyzeAndRun(sentences));
            return null;
        });
        registerMethod("sleep", (n, p, v) -> {
            Thread.sleep(ValueUtil.getLong(v[0]).orElse(0));
            return null;
        });
        registerMethod("update", (n, p, v) -> {
            executorService.setCorePoolSize(
                    ValueUtil.getInt(
                            GlobalVariable.requestValue(GlobalVariable.MAX_Threads)
                    ).orElse(1)
            );
            return null;
        });
        registerMethod("expression", (n, p, v) -> expression(p, v).getStringValue());
        registerMethod("expressionInt", (n, p, v) -> expression(p, v).getNumberValue().intValue());
        registerMethod("expressionDouble", (n, p, v) -> expression(p, v).getValue());
        registerMethod("expressionBoolean", (n, p, v) -> expression(p, v).getBooleanValue());
        registerMethod("expressionString", (n, p, v) -> expression(p, v).getStringValue());
    }

    // 使用缓存 缓存已经创建好的公式
    private static final ConcurrentHashMap<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(100);
    private static final ExpressionConfiguration defaultConfig = ExpressionConfiguration.builder().build();

    //缓存这个算数以加快运行速度 还有配置文件不要多次构建
    private static EvaluationValue expression(String[] p, Object[] v) throws EvaluationException, ParseException {
        Expression expression;
        if (EXPRESSION_CACHE.get(p[0]) == null) {
            expression = new Expression(v[0].toString(), defaultConfig);
            EXPRESSION_CACHE.put(p[0], expression);
        } else {
            expression = EXPRESSION_CACHE.get(p[0]);
        }
        Map<String, Object> values = new HashMap<>();
        for (int i = 1; i < v.length; i++) {
            Object var = v[i];
            switch (var) {
                case Double value -> values.put("var" + i, value);
                case Integer value -> values.put("var" + i, value);
                default -> values.put("var" + i, Double.parseDouble(var.toString()));
            }
        }
        return expression
                .withValues(values)
                .evaluate();
    }

    private static void regFileMethod() {
        registerMethod("getFile", (n, p, v) -> new File(String.valueOf(v[0])));
        registerMethod("listFiles", (n, p, v) -> ((File) v[0]).listFiles());
        registerMethod("moveFile", (n, p, v) -> {
            File oldfile = getFile(v[0]);
            File newfile = getFile(v[1]);
            FileUtil.keepFileExists(newfile.getPath());
            if (oldfile.isDirectory()) return false;
            boolean newFileExists = newfile.exists();//新位置有没有文件
            oldfile.renameTo(newfile);// 覆盖
            return newFileExists;
        });
        registerMethod("copyFile", (n, p, v) -> {//自动创建文件
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
        registerMethod(SetVariable, (n, p, v) -> {
            GlobalVariable.addVariable(p[0], v[0]);
//            System.out.printf("SetVariable: %s -> %s\n", p[0],v[0]);
            return null;
        });
        registerMethod(StringAppend, (n, p, v) -> {
            StringBuilder builder = new StringBuilder();
            for (Object o : v) {
                builder.append(o);
            }
            return builder.toString();
        });
        registerMethod("iterator", (n, p, v) -> {
            int start = Integer.parseInt(convertArg(v[0], String.class).toString());
            int end = Integer.parseInt(convertArg(v[1], String.class).toString());
            int iterator = Integer.parseInt(convertArg(v[2], "1", String.class).toString());
            return IteratorUtil.intRange(start, end, iterator);
        });
        registerMethod(AddMethod, (n, p, v) -> {
            String[] parms = Arrays.copyOfRange(p, 2, p.length);
            log("[MethodService]注册方法： %s(%s){%s}\n", p[0], Arrays.toString(parms), p[1]);
            MethodService.registerMethod(p[0], (n2, p2, v2) -> {
                for (int i = 0; i < parms.length; i++) {
                    GlobalVariable.addVariable("#" + parms[i], v2[i] == null ? p[i] : v2[i]);//添加临时变量
                }
                List<String> sentence = InitParser.getCodeblocks_Sentence().get(p[1]);
                StatementAnalyzer.analyzeAndRun(sentence);
                return GlobalVariable.requestValue("#" + p[0]);//方法名称的修改即为修改返回值
            });
            return v[0];
        });
        registerMethod(Foreach, (n, p, v) -> {
            String varName = "#" + p[0];
            Object var = v[0];
            if (var == null) {
                return "null";
            }
            List<String> sentences = InitParser.getCodeblocks_Sentence().get(p[1]);
            //由于临时变量的处理运行慢 所以不清空临时变量
            switch (var) {//已被转换为Var对象
                case Object[] objects -> {
                    for (Object object : objects) {
                        addTempSubField(varName, object);
                        addTempSubField("#foreach", object);
                        analyseAndRun(sentences);//解析并运行一次循环体
                    }
                }
                case Map<?, ?> map -> {
                    map.forEach((k, value) -> {
                        addTempSubField("#key", k);
                        addTempSubField("#value", v);
                        analyseAndRun(sentences);//解析并运行一次循环体
                    });
                }
                case Collection<?> collection -> {
                    collection.forEach(object -> {
                        addTempSubField(varName, object);
                        addTempSubField("#foreach", object);
                        analyseAndRun(sentences);//解析并运行一次循环体
                    });
                }
                default -> {
                    addTempSubField(varName, var);
                    addTempSubField("#foreach", var);
                    analyseAndRun(sentences);//解析并运行一次循环体
                }
            }

            return null;
        });
    }

    private static void analyseAndRun(List<String> sentences) {
        sentences.forEach(StatementAnalyzer::analyzeAndRun);
    }

    /**
     * 添加临时变量
     *
     * @param varName 变量名
     * @param var     变量
     */
    private static void addTempVar(String varName, Object var) {
        GlobalVariable.addVariable(varName, var);
    }

    /**
     * 添加一个变量包括其常用字段临时变量
     */
    private static void addTempSubField(String varName, Object var) {
        addTempVar(varName, var);
        switch (var) {
            case File file -> {
                addTempVar(varName + "#path", file.getPath());
                addTempVar(varName + "#name", file.getName());
            }
            case CommandResult result -> {
                addTempVar(varName + "#out", result.getOut());
                addTempVar(varName + "#err", result.getErr());
                addTempVar(varName + "#exitcode", result.getExitCode());
            }

            default -> {
            }
        }
    }

}
