package github.axolotl.main.grammar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 1:02
 */
//代码块也是一个语句
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CodeBlock extends Syntax {
    private List<Syntax> syntaxes;//代码块内的语句

    @Override
    public void execute() {
        syntaxes.forEach(Syntax::execute);
    }
}
