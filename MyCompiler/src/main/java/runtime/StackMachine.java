package main.java.runtime;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Stack;

public class StackMachine {
	private final String[] program;
	private int instructionAddress = 0;
	private final Deque<String> stack = new ArrayDeque<>();
	private boolean halted = false;
	private Stack<FuncMetaData> frames = new Stack<>();
	private HashMap<String, Integer> labelsMap = new HashMap<String, Integer>();

	public StackMachine(String[] instructions,
			HashMap<String, Integer> labelsMap) {
		if (instructions.length == 0) {
			throw new InValidProgramException(
					"A program should have at least an instruction");
		}
		this.program = instructions;
		this.labelsMap = labelsMap;
		this.frames.push(new FuncMetaData(0)); // Prepare the initial frame
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
			throw new InValidProgramException(
					"An halted CPU cannot execute the program");
		}
	}

	private void decodeInstruction(String instruction) {
		switch (Instruction.valueOf(instruction)) {
		default:
			throw new InValidProgramException("Unknown instruction: "
					+ instruction);

		case HALT:
			this.halted = true;
			break;

		case PUSH: {
			// The word after the instruction will contain the value to push
			String value = getNextInstruction("Should have the value after the PUSH instruction");
			stack.push(value);
			break;
		}

		case POP: {
			checkStackHasAtLeastOneItem("POP");
			stack.pop();
			break;
		}

		case DUP: {
			checkStackHasAtLeastOneItem("DUP");
			String n = stack.peek();
			stack.push(n);
			break;
		}

		case LOAD: {
			String var = getNextInstruction("Should have the variable number after the LOAD instruction");
			stack.push(getCurrentContext().getVariable(var));
			break;
		}

		case STORE: {
			String var = getNextInstruction("Should have the variable number after the STORE instruction");
			checkStackHasAtLeastOneItem("STORE");
			getCurrentContext().setVariable(var, stack.pop());
			break;
		}

		case NOT: {
			checkStackHasAtLeastOneItem("NOT");
			stack.push(toInt(!toBool(Integer.parseInt(stack.pop()))) + "");
			break;
		}

		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case AND:
		case OR:
		case ISEQ:
		case ISGE:
		case ISGT: {
			if (stack.size() < 2) {
				throw new InValidProgramException(
						"There should be at least two items on the stack to execute a binary instruction");
			}
			int n2 = Integer.parseInt(stack.pop());
			int n1 = Integer.parseInt(stack.pop());
			stack.push(doBinaryOp(instruction, n1, n2) + "");
			break;
		}

		case JMP: {
			// The word after the instruction will contain the address to jump
			// to
			String label = getNextInstruction("Should have the address after the JMP instruction");
			int address = getLabelAddress(label);
			this.instructionAddress = address;
			break;
		}

		case JIF: {
			// The word after the instruction will contain the address to jump
			// to
			String label = getNextInstruction("Should have the address after the JIF instruction");
			int address = getLabelAddress(label);
			checkStackHasAtLeastOneItem("JIF");
			if (toBool(Integer.parseInt(stack.pop()))) {
				this.instructionAddress = address;
			}
			break;
		}

		case CALL: {
			// The word after the instruction will contain the function address
			String label = getNextInstruction("Should have the address after the CALL instruction");
			int address = getLabelAddress(label);
			this.frames.push(new FuncMetaData(this.instructionAddress)); // Push
																			// a
																			// new
																			// stack
																			// frame
			this.instructionAddress = address; // and jump!
			break;
		}

		case RET: {
			// Pop the stack frame and return to the previous address
			checkThereIsAReturnAddress();
			int returnAddress = getCurrentContext().getReturnAddress();
			this.frames.pop();
			this.instructionAddress = returnAddress;
			break;
		}

		case PRINT: {
			checkStackHasAtLeastOneItem("PRINT");
			System.out.print(stack.peek());
		}
		}
	}

	private int getLabelAddress(String label) {
		if (!labelsMap.containsKey(label)) {
			throw new InValidProgramException(String.format("Invalid label %s",
					label));
		}
		int address = labelsMap.get(label);
		checkJumpAddress(address);
		return address;
	}

	private void checkJumpAddress(int address) {
		if (address < 0 || address >= program.length) {
			throw new InValidProgramException(String.format(
					"Invalid jump address %d at %d", address,
					instructionAddress));
		}
	}

	private void checkThereIsAReturnAddress() {
		if (this.frames.size() == 1) {
			throw new InValidProgramException(String.format(
					"Invalid RET instruction: no current function call %d",
					instructionAddress));
		}
	}

	private void checkStackHasAtLeastOneItem(String instruction) {
		if (stack.size() < 1) {
			throw new InValidProgramException(
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
			throw new InValidProgramException(errorMessage);
		}
		String nextWord = program[instructionAddress];
		++instructionAddress;
		return nextWord;
	}

	private FuncMetaData getCurrentContext() {
		return frames.peek();
	}
}