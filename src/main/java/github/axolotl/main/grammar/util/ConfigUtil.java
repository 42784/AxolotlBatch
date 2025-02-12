package github.axolotl.main.grammar.util;

import dczx.axolotl.util.FileUtil;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigUtil {
    public static boolean isLog = true;
    public static boolean cheekVar = true;//是否检查变量

    @SneakyThrows
    public static void initConfig(String configFile) {
        Properties properties = new Properties();
        File file = FileUtil.keepFileExists(configFile);
        FileInputStream inStream = new FileInputStream(file);
        properties.load(inStream);

        isLog = Boolean.parseBoolean(properties.getProperty("isLog", "true"));
        cheekVar = Boolean.parseBoolean(properties.getProperty("cheekVar", "true"));
        inStream.close();


        FileOutputStream fileOutputStream = new FileOutputStream(file);
        properties.setProperty("isLog", String.valueOf(isLog));
        properties.setProperty("cheekVar", String.valueOf(cheekVar));
        properties.store(fileOutputStream, null);
        fileOutputStream.close();
    }
}
