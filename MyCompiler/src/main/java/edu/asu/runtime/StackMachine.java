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

	public StackMachine(String[] instructions,
			HashMap<String, Integer> labelsMap) {
		if (instructions.length == 0) {
			throw new ProgramExecutionException(
					"A program should have at least an instruction");
		}
		this.program = instructions;
		this.labelMap = labelsMap;
		this.callStack.push(new FuncMetaData(0)); // Prepare the initial frame
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
			executionStack.push(getCurrentContext().getVariable(var));
			break;
		}

		case STORE: {
			String var = getNextInstruction("Should have the variable name after the STORE instruction");
			checkStackHasAtLeastOneItem("STORE");
			getCurrentContext().setVariable(var, executionStack.pop());
			break;
		}

		case NOT: {
			checkStackHasAtLeastOneItem("NOT");
			executionStack.push(toInt(!toBool(Integer.parseInt(executionStack
					.pop()))) + "");
			break;
		}

		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case MOD:
		case POW:
		case AND:
		case OR:
		case ISEQ:
		case ISGE:
		case ISGT: {
			if (executionStack.size() < 2) {
				throw new ProgramExecutionException(
						"There should be at least two items on the stack to execute a binary instruction");
			}
			int n2 = Integer.parseInt(executionStack.pop());
			int n1 = Integer.parseInt(executionStack.pop());
			executionStack.push(doBinaryOp(instruction, n1, n2) + "");
			break;
		}

		case JIF: {
			// JMP if stack top value is false
			// The word after the instruction will contain the address to jump
			// to 
			String label = getNextInstruction("Should have the address after the JIF instruction");
			int address = getLabelAddress(label);
			checkStackHasAtLeastOneItem("JIF");
			if (!toBool(Integer.parseInt(executionStack.pop()))) {
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
			this.callStack.push(new FuncMetaData(this.instructionAddress));
			this.instructionAddress = address; // and jump!
			break;
		}

		case RET: {
			// Pop the stack frame and return to the previous address
			checkThereIsAReturnAddress();
			int returnAddress = getCurrentContext().getReturnAddress();
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

	private Integer doBinaryOp(String instruction, int n1, int n2) {
		switch (Instruction.valueOf(instruction)) {
		case ADD:
			return n1 + n2;
		case SUB:
			return n1 - n2;
		case MUL:
			return n1 * n2;
		case DIV:
			return n1 / n2;
		case MOD:
			return n1 % n2;
		case POW:
			return (int) Math.pow(n1, n2);
		case AND:
			return toInt(toBool(n1) && toBool(n2));
		case OR:
			return toInt(toBool(n1) || toBool(n2));
		case ISEQ:
			return toInt(n1 == n2);
		case ISGE:
			return toInt(n1 >= n2);
		case ISGT:
			return toInt(n1 > n2);
		default:
			throw new AssertionError();
		}
	}

	private boolean toBool(int n) {
		return n != 0;
	}

	private int toInt(boolean b) {
		return b ? 1 : 0;
	}

	private String getNextInstruction(String errorMessage) {
		if (instructionAddress >= program.length) {
			throw new ProgramExecutionException(errorMessage);
		}
		String nextWord = program[instructionAddress];
		++instructionAddress;
		return nextWord;
	}

	private FuncMetaData getCurrentContext() {
		return callStack.peek();
	}
}