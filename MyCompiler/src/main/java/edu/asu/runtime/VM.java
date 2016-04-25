package edu.asu.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VM {

	private static HashMap<String, Integer> labelMap = new HashMap<String, Integer>();

	public static void main(String[] args) {
		String filename = "intermediate/sample.int";
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
			String code = input.useDelimiter("\\Z").next();
			run(code);
			input.close();
		}
	}

	public static void run(String code) {
		List<String> instr = new ArrayList<>();
		Pattern p = Pattern.compile("(\"[^\"]+\")|\\S+");
		Matcher m = p.matcher(code);
		while (m.find()) {
			instr.add(m.group().replaceAll("\"", ""));
		}
		executeInstr(instr.toArray(new String[0]));
	}

	public static void executeInstr(String[] instr) {
		int line = 0;
		ArrayList<String> program = new ArrayList<String>();
		for (int i = 0; i < instr.length; i++) {
			String s = instr[i];
			if (!s.isEmpty()) {
				if (s.equals(Instruction.LABEL.toString())) {
					i++;
					labelMap.put(instr[i], line);
				} else {
					program.add(s);
					line++;
				}
			}
		}
		StackMachine sm = new StackMachine(program.toArray(new String[program
				.size()]), labelMap);
		sm.run();
	}
}
