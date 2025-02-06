package github.axolotl.main.grammar.syntax;

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
@Data
@AllArgsConstructor
public class CodeBlock implements Syntax {
    private List<Syntax> syntaxes;//代码块内的语句

    @Override
    public Object execute() {
        if (syntaxes == null) return null;
        syntaxes.forEach(Syntax::execute);
        return null;
    }
}
