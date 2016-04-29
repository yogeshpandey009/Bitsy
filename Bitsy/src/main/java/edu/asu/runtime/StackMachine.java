package edu.asu.runtime;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;

import edu.asu.runtime.exceptions.ProgramExecutionException;

public class StackMachine {
	private final String[] program;
	private int instructionAddress = 0;
	private final Stack<String> executionStack = new Stack<>();
	private boolean halted = false;
	private Stack<FuncMetaData> callStack = new Stack<>();
	private HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
	private FuncMetaData mainMethod = new FuncMetaData(0, "main",
			new HashMap<>(), new HashMap<>());

	public StackMachine(String[] instructions,
			HashMap<String, Integer> labelsMap) {
		if (instructions.length == 0) {
			throw new ProgramExecutionException(
					"A program should have at least an instruction");
		}
		this.program = instructions;
		this.labelMap = labelsMap;
		this.callStack.push(mainMethod); // Prepare the main method with 0 as
											// retAddr
	}

	public void run() {
		while (!halted) {
			step();
		}
	}

	private void step() {
		checkState();
		String nextInstruction = getNextInstruction("Should have a next instruction");
		decodeInstruction(nextInstruction);
	}

	private void checkState() {
		if (halted) {
			throw new ProgramExecutionException(
					"An halted CPU cannot execute the program");
		}
	}

	private void decodeInstruction(String instruction) {

		switch (Instruction.valueOf(instruction)) {

		case HALT:
			this.halted = true;
			break;

		case PUSH: {
			// The word after the instruction will contain the value to push
			String value = getNextInstruction("Should have the value after the PUSH instruction");
			executionStack.push(value);
			break;
		}

		case POP: {
			checkStackHasAtLeastOneItem("POP");
			executionStack.pop();
			break;
		}

		case LOAD: {
			String var = getNextInstruction("Should have the variable name after the LOAD instruction");
			executionStack.push(getCurrFuncContext().getVariable(var));
			break;
		}

		case STORE: {
			String var = getNextInstruction("Should have the variable name after the STORE instruction");
			checkStackHasAtLeastOneItem("STORE");
			getCurrFuncContext().setVariable(var, executionStack.pop());
			break;
		}

		case STACK_PUSH: {
			String var = getNextInstruction("Should have the variable name after the STACK_PUSH instruction");
			checkStackHasAtLeastOneItem("STACK_PUSH");
			getCurrFuncContext().pushOnStackVariable(var, executionStack.pop());
			break;
		}
		case STACK_POP: {
			String var = getNextInstruction("Should have the variable name after the STACK_POP instruction");
			executionStack.push(getCurrFuncContext().popOnStackVariable(var));
			break;
		}
		case STACK_PEEK: {
			String var = getNextInstruction("Should have the variable name after the STACK_PEEK instruction");
			executionStack.push(getCurrFuncContext().peekOnStackVariable(var));
			break;
		}
		case STACK_ISEMPTY: {
			String var = getNextInstruction("Should have the variable name after the STACK_ISEMPTY instruction");
			executionStack.push(Boolean.toString(getCurrFuncContext()
					.isEmptyStackVariable(var)));
			break;
		}
		case NOT: {
			checkStackHasAtLeastOneItem("NOT");
			String v = executionStack.pop();
			executionStack.push(Boolean.toString(!Boolean.parseBoolean(v)));
			break;
		}

		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case MOD:
		case POW:
		case ISEQ:
		case ISGE:
		case ISGT:
		case AND:
		case OR: {
			if (executionStack.size() < 2) {
				throw new ProgramExecutionException(
						"There should be at least two items on the stack to execute a binary instruction");
			}
			String v1 = executionStack.pop();
			String v2 = executionStack.pop();
			executionStack.push(doBinaryOp(instruction, v2, v1).toString());
			break;
		}
		case JIF: {
			// JMP if stack top value is false
			// The word after the instruction will contain the address to jump
			// to
			String label = getNextInstruction("Should have the address after the JIF instruction");
			int address = getLabelAddress(label);
			checkStackHasAtLeastOneItem("JIF");
			String stackValue = executionStack.pop();
			if (!toBool(stackValue)) {
				this.instructionAddress = address;
			}
			break;
		}

		case JMP: {
			// The word after the instruction will contain the address to jump
			// to
			String label = getNextInstruction("Should have the address after the JIF instruction");
			int address = getLabelAddress(label);
			this.instructionAddress = address;
			break;
		}

		case CALL: {
			// The word after the instruction will contain the function address
			String label = getNextInstruction("Should have the address after the CALL instruction");
			int address = getLabelAddress(label);
			// Push a new stack frame
			this.callStack.push(new FuncMetaData(this.instructionAddress,
					label, mainMethod.getVariables(), mainMethod
							.getStackVariables()));
			this.instructionAddress = address; // and jump!
			break;
		}

		case RET: {
			// Pop the stack frame and return to the previous address
			checkThereIsAReturnAddress();
			int returnAddress = getCurrFuncContext().getReturnAddress();
			this.callStack.pop();
			this.instructionAddress = returnAddress;
			break;
		}

		case PRINT: {
			checkStackHasAtLeastOneItem("PRINT");
			String escapedStr = executionStack.peek();
			System.out.print(StringEscapeUtils.unescapeJava(escapedStr));
			break;
		}

		case INPUT: {
			Scanner sc = new Scanner(System.in);
			String input = sc.nextLine();
			sc.close();
			executionStack.push(input);
			break;
		}

		default:
			throw new ProgramExecutionException("Unknown instruction: "
					+ instruction);

		}
	}

	private int getLabelAddress(String label) {
		if (!labelMap.containsKey(label)) {
			throw new ProgramExecutionException(String.format(
					"Invalid label %s", label));
		}
		int address = labelMap.get(label);
		checkJumpAddress(address);
		return address;
	}

	private void checkJumpAddress(int address) {
		if (address < 0 || address >= program.length) {
			throw new ProgramExecutionException(String.format(
					"Invalid jump address %d at %d", address,
					instructionAddress));
		}
	}

	private void checkThereIsAReturnAddress() {
		if (this.callStack.size() == 1) {
			throw new ProgramExecutionException(String.format(
					"Invalid RET instruction: no current function call %d",
					instructionAddress));
		}
	}

	private void checkStackHasAtLeastOneItem(String instruction) {
		if (executionStack.size() < 1) {
			throw new ProgramExecutionException(
					"There should be at least one item on the stack to execute an "
							+ instruction + " instruction");
		}
	}

	private Object doBinaryOp(String instruction, String op1, String op2) {
		switch (Instruction.valueOf(instruction)) {
		case ADD:
			return Integer.parseInt(op1) + Integer.parseInt(op2);
		case SUB:
			return Integer.parseInt(op1) - Integer.parseInt(op2);
		case MUL:
			return Integer.parseInt(op1) * Integer.parseInt(op2);
		case DIV:
			return Integer.parseInt(op1) / Integer.parseInt(op2);
		case MOD:
			return Integer.parseInt(op1) % Integer.parseInt(op2);
		case POW:
			return (int) Math.pow(Integer.parseInt(op1), Integer.parseInt(op2));
		case ISEQ:
			return op1.equals(op2);
		case ISGE:
			return Integer.parseInt(op1) >= Integer.parseInt(op2);
		case ISGT:
			return Integer.parseInt(op1) > Integer.parseInt(op2);
		case AND:
			return Boolean.parseBoolean(op1) && Boolean.parseBoolean(op2);
		case OR:
			return Boolean.parseBoolean(op1) || Boolean.parseBoolean(op2);
		default:
			throw new AssertionError();
		}
	}

	private boolean toBool(String val) {
		try {
			int n = Integer.parseInt(val);
			return n != 0;
		} catch (NumberFormatException e) {
			return Boolean.parseBoolean(val);
		}
	}

	private String getNextInstruction(String errorMessage) {
		if (instructionAddress >= program.length) {
			throw new ProgramExecutionException(errorMessage);
		}
		String nextWord = program[instructionAddress];
		++instructionAddress;
		return nextWord;
	}

	private FuncMetaData getCurrFuncContext() {
		return callStack.peek();
	}
}