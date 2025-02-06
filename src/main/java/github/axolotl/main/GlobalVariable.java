package github.axolotl.main;


import java.util.HashMap;

/**
 * @author AxolotlXM
 * @version 1.0
 * @since 2025/2/6 1:42
 */
public class GlobalVariable {
    public static final HashMap<String, Object> variables = new HashMap<>();

    //CRUD Variable
    public static Object getVariable(String variableName) {
        return variables.get(variableName);
    }

    public static void setVariable(String variableName, Object variable) {
        variables.put(variableName, variable);
    }

    public static void removeVariable(String variableName) {
        variables.remove(variableName);
    }

    public static void addVariable(String variableName, Object variable) {
        variables.put(variableName, variable);
    }

    //getValue
    public static Object getValue(String variableName) {
        return variables.get(variableName);
    }

    public static <T> T getValue(String variableName, Class<T> type) {
        return type.cast(variables.get(variableName));

    }

    public static Object requestValue(Object variableName) {
        try {
            String name = ((String) variableName).trim();
            if (variables.containsKey(name))
                return variables.get(name);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static <T> T requestValue(Object variableName, Class<T> type) {
        return type.cast(requestValue(variableName));
    }


    public static final String EXEC_PREFIX = "EXEC_PREFIX";
    public static final String EXEC_HOME = "EXEC_HOME";
    public static void initDefaultVariable() {
//        addVariable(EXEC_PREFIX,"cmd /c ");
        addVariable(EXEC_PREFIX,"");
        addVariable(EXEC_HOME,"./");
    }
}
