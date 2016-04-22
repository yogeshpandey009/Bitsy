package edu.asu.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.asu.runtime.exceptions.ProgramExecutionException;

public class FuncMetaData {

	private final Map<String, String> variables = new HashMap<>();
	private final int returnAddress;

	public FuncMetaData(int returnAddress) {
		this.returnAddress = returnAddress;
	}

	public String getVariable(String var) {
		if (variables.containsKey(var)) {
			return variables.get(var);
		}
		throw new ProgramExecutionException("Undefined Variable: " + var);
	}

	public void setVariable(String var, String value) {
		variables.put(var, value);
	}

	public int getReturnAddress() {
		return returnAddress;
	}

	public Map<String, String> getVariables() {
		return Collections.unmodifiableMap(variables);
	}
}