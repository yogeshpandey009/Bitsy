package main.java.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class VM {

	public static void main(String[] args) {
		String filename = "";
		ArrayList<String> program = new ArrayList<>();
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
			while (input.hasNextLine()) {
				Scanner line = new Scanner(input.nextLine());
				while (line.hasNext()) {
					String s = line.next();
					program.add(s);
				}
				line.close();
			}
			input.close();
		}
		StackMachine sm = new StackMachine((String[]) program.toArray());
		sm.run();
	}
}
