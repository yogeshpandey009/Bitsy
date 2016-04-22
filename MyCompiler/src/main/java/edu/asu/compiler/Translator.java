package edu.asu.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import mycompiler.parser.MyLangLexer;
import mycompiler.parser.MyLangParser;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Translator {

	public static void main(String[] args) throws Exception {
		String filename = "input/simple.my";
		if (args.length > 0) {
			filename = args[0];
		}
		ANTLRInputStream input = new ANTLRFileStream(filename);

		System.out.println(compile(input));
	}

	public static String compile(ANTLRInputStream input) {
		MyLangLexer lexer = new MyLangLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MyLangParser parser = new MyLangParser(tokens);

		ParseTree tree = parser.program();
		return createIntermediateCode(new MyVisitor().visit(tree));
	}

	private static String createIntermediateCode(String instructions) {
		File intrFile = new File("intermediate/simple.int");
		try {
			if (!intrFile.exists()) {
				intrFile.createNewFile();
			}
			PrintWriter writer = new PrintWriter(intrFile);
			writer.print(instructions);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instructions;
	}

	private static String createJasminFile(String instructions) {
		return ".class public HelloWorld\n" + ".super java/lang/Object\n"
				+ "\n" + instructions;
	}
}
