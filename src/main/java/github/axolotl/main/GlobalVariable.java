package github.axolotl.main;

import github.axolotl.main.grammar.variable.Variable;

import java.util.HashMap;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 1:42
 */
public class GlobalVariable {
    public static final HashMap<String, Variable<?>> variables = new HashMap<>();

    //CRUD Variable
    public static Variable<?> getVariable(String variableName) {
        return variables.get(variableName);
    }

    public static void setVariable(String variableName, Variable<?> variable) {
        variables.put(variableName, variable);
    }

    public static void removeVariable(String variableName) {
        variables.remove(variableName);
    }

    public static void addVariable(String variableName, Variable<?> variable) {
        variables.put(variableName, variable);
    }

    //getValue
    public static Object getValue(String variableName) {
        return variables.get(variableName).getValue();
    }
    public static <T> T getValue(String variableName,Class<T> type) {
        return type.cast(variables.get(variableName).getValue()) ;
    }


}
