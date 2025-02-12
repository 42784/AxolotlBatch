package github.axolotl.main;


import github.axolotl.main.grammar.util.AnalyzerMethodCache;
import github.axolotl.main.grammar.util.AnalyzerSentenceCache;
import github.axolotl.main.grammar.util.ConfigUtil;
import github.axolotl.main.grammar.util.InitParser;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;

import static github.axolotl.main.grammar.util.LogUtil.log;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/5 20:19
 */
public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        {//初始化配置文件
            if (System.getProperty("configPath") != null)
                ConfigUtil.initConfig(System.getProperty("configPath"));
            else
                ConfigUtil.initConfig("./config.txt");
            GlobalVariable.initDefaultVariable();//初始化基本变量
        }
        String mode = System.getProperty("mode", "run");
        switch (mode) {
            case "test" -> test(); //测试使用 平时不用
            case "run" -> {
                if (args.length > 0) {

                    File file = new File(args[0]);
                    if (file.exists()) {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            InitParser.parse(IOUtils.toString(fis));
                        }
                    }
                } else {
                    System.err.println("请传入待运行的文件");
                }
            }
        }


    }

    @SneakyThrows
    private static void test() {
        String code = IOUtils.toString(new FileInputStream("G:\\CreativeJava\\2025_02\\AxolotlBatch\\test\\batch\\迭代器求和.ab"));
        GlobalVariable.initDefaultVariable();
        ConfigUtil.initConfig("./config.txt");
        InitParser.parse(code);
        log("[Cache]AnalyzerSentenceCache.cacheTable = " + AnalyzerSentenceCache.cacheTable);
        log("[Cache]AnalyzerMethodCache.cacheTable = " + AnalyzerMethodCache.cacheTable);
    }
}