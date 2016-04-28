package edu.asu.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import edu.asu.runtime.exceptions.ProgramExecutionException;

public class FuncMetaData {

	private final Map<String, String> variables = new HashMap<>();
	private final Map<String, Stack<String>> stackVariables = new HashMap<>();
	private int returnAddress;
	private String funcName = "";

	public FuncMetaData(int returnAddress, String funcName) {
		this.returnAddress = returnAddress;
		this.funcName = funcName;
	}

	public String getVariable(String var) {
		if (variables.containsKey(var)) {
			return variables.get(var);
		}
		throw new ProgramExecutionException("Undefined Variable: " + var
				+ " inside function: " + funcName);
	}

	public Stack<String> getStackVariable(String var) {
		if (stackVariables.containsKey(var)) {
			return stackVariables.get(var);
		}
		throw new ProgramExecutionException("Undefined Stack Variable: " + var
				+ " inside function: " + funcName);
	}

	public void setVariable(String var, String value) {
		variables.put(var, value);
	}

	public void pushOnStackVariable(String var, String value) {
		if (stackVariables.containsKey(var)) {
			stackVariables.get(var).push(value);
		} else {
			stackVariables.put(var, new Stack<String>() {
				{
					push(value);
				}
			});
		}
	}

	public String popOnStackVariable(String var) {
		Stack<String> s = getStackVariable(var);
		return s.pop();
	}

	public String peekOnStackVariable(String var) {
		Stack<String> s = getStackVariable(var);
		return s.peek();
	}

	public boolean isEmptyStackVariable(String var) {
		Stack<String> s = getStackVariable(var);
		return s.isEmpty();
	}

	public int getReturnAddress() {
		return returnAddress;
	}

	public Map<String, String> getVariables() {
		return Collections.unmodifiableMap(variables);
	}
}