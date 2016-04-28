package edu.asu.compiler;

import java.util.Arrays;

import edu.asu.parser.Translator;
import edu.asu.runtime.VM;

public class Main {

	public static void main(String[] args) {
		if (args.length > 1) {
			if (args[0].equals("-c") || args[0].equals("--compile")) {
				Translator.main(Arrays.copyOfRange(args, 1, args.length));
				return;
			} else if (args[0].equals("-e") || args[0].equals("--execute")) {
				VM.main(Arrays.copyOfRange(args, 1, args.length));
				return;
			}
		}
		System.out
				.println("Usage: -c | --compile along with args for sourcepath and destpath");
		System.out
				.println("    or -e | --execute along with arg for intermediate code path");
		return;
	}
}
