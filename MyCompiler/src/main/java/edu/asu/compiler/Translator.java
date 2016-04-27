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

	public static void main(String[] args) {
		String srcPath = "";
		if (args.length > 0) {
			srcPath = args[0];
		} else {
			System.out
					.println("Usage: -sourcepath <path> Specify where to find input source file");
			return;
		}
		ANTLRInputStream input = null;
		try {
			input = new ANTLRFileStream(srcPath);
		} catch (IOException e) {
			System.out.println("Error: Could not find or load souce file "
					+ srcPath);
			return;
		}
		if (input != null) {
			ParseTree tree = parse(input);
			String intermediateCode = generateIntermediateCode(tree);
			System.out.println(intermediateCode);
			String desPath = srcPath.substring(0, srcPath.lastIndexOf('.'))
					+ ".int";
			createFile(intermediateCode, desPath);
		}
	}

	public static ParseTree parse(ANTLRInputStream input) {
		MyLangLexer lexer = new MyLangLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MyLangParser parser = new MyLangParser(tokens);

		ParseTree tree = parser.program();
		return tree;
	}

	public static String generateIntermediateCode(ParseTree tree) {
		return new MyVisitor().visit(tree);
	}

	private static void createFile(String instructions, String desPath) {
		File intrFile = new File(desPath);
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
	}
}
