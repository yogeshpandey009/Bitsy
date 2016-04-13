package main.java.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FuncMetaData {

	private final Map<String, Integer> variables = new HashMap<>();
    private final int returnAddress;

    public FuncMetaData(int returnAddress) {
        this.returnAddress = returnAddress;
    }

    public int getVariable(String var) {
        return variables.getOrDefault(var, 0);
    }

    public void setVariable(String var, int value) {
        variables.put(var, value);
    }

    public int getReturnAddress() {
        return returnAddress;
    }

    public Map<String, Integer> getVariables() {
        return Collections.unmodifiableMap(variables);
    }
}