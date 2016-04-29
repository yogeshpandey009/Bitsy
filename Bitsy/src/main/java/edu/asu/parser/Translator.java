package edu.asu.parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.parser.BitsyLexer;
import compiler.parser.BitsyParser;

public class Translator {

	public static void main(String[] args) {
		String srcPath = null;
		String destPath = null;
		if (args.length > 0) {
			srcPath = args[0];
			if(args.length == 2) {
				destPath = args[1];
			}
		} else {
			System.out
					.println(" Usage: <sourcepath> to specify where to find input source file");
			System.out
			.println("\t <destinationpath> (Optional) to specify where to save compiled file");
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
			if(destPath == null)
				destPath = srcPath.substring(0, srcPath.lastIndexOf('.'))
						+ ".int";
			createFile(intermediateCode, destPath);
		}
	}

	public static ParseTree parse(ANTLRInputStream input) {
		BitsyLexer lexer = new BitsyLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		BitsyParser parser = new BitsyParser(tokens);
		ParseTree tree = parser.program();
		//final TreeViewer view = new TreeViewer(null, tree);
	    //view.open();
		return tree;
	}

	public static String generateIntermediateCode(ParseTree tree) {
		return new MyBitsyVisitor().visit(tree);
	}

	private static void createFile(String instructions, String destPath) {
		File intrFile = new File(destPath);
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
