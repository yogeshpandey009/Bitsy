package main.java.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class VM {

	private static HashMap<String, Integer> labelsMap = new HashMap<String, Integer>();

	public static void main(String[] args) {
		String filename = "intermediate/simple.int";
		if (args.length > 0) {
			filename = args[0];
		}
		File file = new File(filename);
		Scanner input = null;
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (input != null) {
			String content = input.useDelimiter("\\Z").next();
			String[] instr = content.split("\\s+");
			execute(instr);
			input.close();
		}
	}

	public static void execute(String[] instr) {
		int line = 0;
		ArrayList<String> program = new ArrayList<String>();
		for (int i = 0; i < instr.length; i++) {
			String s = instr[i];
			if (!s.isEmpty()) {
				if (s.equals(Instruction.LABEL.toString())) {
					i++;
					labelsMap.put(instr[i], line);
				} else {
					program.add(s);
					line++;
				}
			}
		}
		StackMachine sm = new StackMachine(program.toArray(new String[program
				.size()]), labelsMap);
		sm.run();
	}
}
