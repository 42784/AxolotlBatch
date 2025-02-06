package github.axolotl.main;


import github.axolotl.main.grammar.util.InitParser;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/5 20:19
 */
public class Main {
    @SneakyThrows
    public static void main(String[] args) {

//        String code = IOUtils.toString(new FileInputStream("G:\\CreativeJava\\2025_02\\AxolotlBatch\\test\\batch\\ffmpeg.ab"));
        String code = IOUtils.toString(new FileInputStream("G:\\CreativeJava\\2025_02\\AxolotlBatch\\test\\batch\\fileMove.ab"));
        code = code.replace("\r\n", "\n");//Windows下的换行符处理
        GlobalVariable.initDefaultVariable();
        InitParser.parse(code);
        Thread.sleep(100);

    }
}