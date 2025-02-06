package github.axolotl.main.grammar.syntax;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 11:10
 */
@Data
@AllArgsConstructor
//一条待被识别执行的语句
public class Sentence {
    private String sentence;
}
