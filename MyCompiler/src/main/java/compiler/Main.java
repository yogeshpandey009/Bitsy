package main.java.compiler;

import mycompiler.parser.MyLangLexer;
import mycompiler.parser.MyLangParser;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;



public class Main {

	public static void main(String[] args) throws Exception {
		ANTLRInputStream input = new ANTLRFileStream("./src/main/resources/simple.my");
		
		System.out.println(compile(input));
	}
	
	public static String compile(ANTLRInputStream input) {
		MyLangLexer lexer = new MyLangLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MyLangParser parser = new MyLangParser(tokens);
		
		ParseTree tree = parser.program();
		return createJasminFile(new MyVisitor().visit(tree));
	}
	
	private static String createJasminFile(String instructions) {
		return ".class public HelloWorld\n" + 
				".super java/lang/Object\n" + 
				"\n" + 
				instructions;
	}
}
