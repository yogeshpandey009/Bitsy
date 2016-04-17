package main.java.compiler;

import java.util.HashMap;
import java.util.Map;

import main.java.compiler.exceptions.UndeclaredVariableException;
import main.java.compiler.exceptions.VariableAlreadyDefinedException;
import mycompiler.parser.MyLangBaseVisitor;
import mycompiler.parser.MyLangParser.AssignmentContext;
import mycompiler.parser.MyLangParser.DivContext;
import mycompiler.parser.MyLangParser.FunctionCallContext;
import mycompiler.parser.MyLangParser.FunctionDefinitionContext;
import mycompiler.parser.MyLangParser.MainStatementContext;
import mycompiler.parser.MyLangParser.MinusContext;
import mycompiler.parser.MyLangParser.MultContext;
import mycompiler.parser.MyLangParser.NumberContext;
import mycompiler.parser.MyLangParser.PlusContext;
import mycompiler.parser.MyLangParser.PrintlnContext;
import mycompiler.parser.MyLangParser.ProgramContext;
import mycompiler.parser.MyLangParser.VarDeclarationContext;
import mycompiler.parser.MyLangParser.VariableContext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;


public class MyVisitor extends MyLangBaseVisitor<String> {
	
	private Map<String, Integer> variables = new HashMap<>();
	
	@Override
	public String visitPrintln(PrintlnContext ctx) {
//		return "  getstatic java/lang/System/out Ljava/io/PrintStream;\n" + 
//				 visit(ctx.argument) + "\n" + 
//				"  invokevirtual java/io/PrintStream/println(I)V\n";
		return visit(ctx.argument) +  "\n" + "PRINT";
	}
	
	@Override
	public String visitPlus(PlusContext ctx) {
		return visitChildren(ctx) + "\n" +
			"ADD";
	}
	
	@Override
	public String visitMinus(MinusContext ctx) {
		return visitChildren(ctx) + "\n" +
				"SUB";
	}
	
	@Override
	public String visitDiv(DivContext ctx) {
		return visitChildren(ctx) + "\n" +
				"DIV";
	}
	
	@Override
	public String visitMult(MultContext ctx) {
		return visitChildren(ctx) + "\n" +
				"MUL";
	}
	
	@Override
	public String visitNumber(NumberContext ctx) {
		return "PUSH " + ctx.number.getText();
	}
	
	@Override
	public String visitVarDeclaration(VarDeclarationContext ctx) {
		if (variables.containsKey(ctx.varName.getText())) {
			throw new VariableAlreadyDefinedException(ctx.varName);
		}
		variables.put(ctx.varName.getText(), variables.size());
		return "";
	}
	
	@Override
	public String visitAssignment(AssignmentContext ctx) {
		return visit(ctx.expr) + "\n" +
				"STORE " + ctx.varName.getText();
	}
	
	@Override
	public String visitVariable(VariableContext ctx) {
		return "LOAD " + ctx.varName.getText();
	}
	
	@Override
	public String visitFunctionCall(FunctionCallContext ctx) {
		String instructions = "";
		String argumentsInstructions = visit(ctx.arguments);
		if (argumentsInstructions != null) {
			instructions += argumentsInstructions + '\n';
		}
		instructions += "invokestatic HelloWorld/" + ctx.funcName.getText() + "(";
		int numberOfParameters = ctx.arguments.expressions.size();
		instructions += stringRepeat("I", numberOfParameters);
		instructions += ")I";
		return instructions;
	}
	
	@Override
	public String visitFunctionDefinition(FunctionDefinitionContext ctx) {
		Map<String, Integer> oldVariables = variables;
		variables = new HashMap<>();
		visit(ctx.params);
		String statementInstructions = visit(ctx.statements);
		String result = ".method public static " + ctx.funcName.getText() + "(";
		int numberOfParameters = ctx.params.declarations.size();
		result += stringRepeat("I", numberOfParameters);
		result += ")I\n" +
				".limit locals 100\n" +
				".limit stack 100\n" +
				(statementInstructions == null ? "" : statementInstructions + "\n") +
				visit(ctx.returnValue) + "\n" +
				"ireturn\n" +
				".end method";
		variables = oldVariables;
		return result;
	}
	
	private String stringRepeat(String string, int count) {
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < count; ++i) {
			result.append(string);
		}
		return result.toString();
	}

	@Override
	public String visitProgram(ProgramContext ctx) {
		String mainCode = "";
		String functions = "";
		for(int i = 0; i < ctx.getChildCount(); ++i) {
			ParseTree child = ctx.getChild(i);
			String instructions = visit(child);
			if (child instanceof MainStatementContext) {
				mainCode += instructions + "\n";
			} else {
				functions += instructions + "\n";
			}
		}
		/*
		return functions + "\n" +
		".method public static main([Ljava/lang/String;)V\n" + 
		"  .limit stack 100\n" + 
		"  .limit locals 100\n" + 
		"  \n" + 
		 mainCode + "\n" + 
		"  return\n" + 
		"  \n" + 
		".end method";
		*/
		return functions +
		 mainCode +
		"HALT";
	}
	
	private int requireVariableIndex(Token varNameToken) {
		Integer varIndex = variables.get(varNameToken.getText());
		if (varIndex == null) {
			throw new UndeclaredVariableException(varNameToken);
		}
		return varIndex;
	}
	
	@Override
	protected String aggregateResult(String aggregate, String nextResult) {
		if (aggregate == null) {
			return nextResult;
		}
		if (nextResult == null) {
			return aggregate;
		}
		return aggregate + "\n" + nextResult;
	}
}
