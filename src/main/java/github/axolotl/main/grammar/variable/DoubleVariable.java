package github.axolotl.main.grammar.variable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 1:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DoubleVariable extends Variable<Double> {
    private Double value;
}
