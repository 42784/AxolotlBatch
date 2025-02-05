package github.axolotl.main.grammar.variable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 1:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class IntegerVariable extends Variable<Integer> {
    private Integer value;
}
