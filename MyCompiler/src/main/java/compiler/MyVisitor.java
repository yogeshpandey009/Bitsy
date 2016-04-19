package main.java.compiler;

import java.util.HashSet;
import java.util.Set;

import main.java.compiler.exceptions.UndeclaredVariableException;
import main.java.compiler.exceptions.VariableAlreadyDefinedException;
import mycompiler.parser.MyLangBaseVisitor;
import mycompiler.parser.MyLangParser.AssignmentContext;
import mycompiler.parser.MyLangParser.DivContext;
import mycompiler.parser.MyLangParser.FunctionCallContext;
import mycompiler.parser.MyLangParser.FunctionDefinitionContext;
import mycompiler.parser.MyLangParser.GreaterContext;
import mycompiler.parser.MyLangParser.GreaterEqContext;
import mycompiler.parser.MyLangParser.IsEqContext;
import mycompiler.parser.MyLangParser.LessContext;
import mycompiler.parser.MyLangParser.LessEqContext;
import mycompiler.parser.MyLangParser.LogicalANDContext;
import mycompiler.parser.MyLangParser.LogicalORContext;
import mycompiler.parser.MyLangParser.MainStatementContext;
import mycompiler.parser.MyLangParser.MinusContext;
import mycompiler.parser.MyLangParser.MultContext;
import mycompiler.parser.MyLangParser.NotEqContext;
import mycompiler.parser.MyLangParser.NumberContext;
import mycompiler.parser.MyLangParser.PlusContext;
import mycompiler.parser.MyLangParser.PrintlnContext;
import mycompiler.parser.MyLangParser.ProgramContext;
import mycompiler.parser.MyLangParser.VarDeclarationContext;
import mycompiler.parser.MyLangParser.VariableContext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;


public class MyVisitor extends MyLangBaseVisitor<String> {
	
	private Set<String> variables = new HashSet<>();
	
	@Override
	public String visitPrintln(PrintlnContext ctx) {
//		return "  getstatic java/lang/System/out Ljava/io/PrintStream;\n" + 
//				 visit(ctx.argument) + "\n" + 
//				"  invokevirtual java/io/PrintStream/println(I)V\n";
		return visit(ctx.argument) + "PRINT" +  "\n";
	}
	
	@Override
	public String visitPlus(PlusContext ctx) {
		return visitChildren(ctx) +
			"ADD" + "\n";
	}
	
	
	@Override
	public String visitMinus(MinusContext ctx) {
		return visitChildren(ctx) +
				"SUB" + "\n";
	}
	
	@Override
	public String visitDiv(DivContext ctx) {
		return visitChildren(ctx) +
				"DIV" + "\n";
	}
	
	@Override
	public String visitMult(MultContext ctx) {
		return visitChildren(ctx) +
				"MUL" + "\n";
	}
	
	@Override
	public String visitNumber(NumberContext ctx) {
		return "PUSH " + ctx.number.getText() + "\n";
	}
	
	@Override
	public String visitVarDeclaration(VarDeclarationContext ctx) {
		if (variables.contains(ctx.varName.getText())) {
			throw new VariableAlreadyDefinedException(ctx.varName);
		}
		variables.add(ctx.varName.getText());
		return "";
	}
	
	@Override
	public String visitAssignment(AssignmentContext ctx) {
		return visit(ctx.expr) +
				"STORE " + getVariableName(ctx.varName) + "\n";
	}
	
	@Override
	public String visitVariable(VariableContext ctx) {
		return "LOAD " + getVariableName(ctx.varName) + "\n";
	}
	
	@Override
	public String visitLogicalAND(LogicalANDContext ctx) {
		return visitChildren(ctx) +
				"AND" + "\n";
	}
	
	@Override
	public String visitLogicalOR(LogicalORContext ctx) {
		return visitChildren(ctx) +
				"OR" + "\n";
	}
	
	@Override
	public String visitLess(LessContext ctx) {
		return visitChildren(ctx) +
				"ISGE" + "\n" + "NOT" + "\n";
	}
	
	@Override
	public String visitLessEq(LessEqContext ctx) {
		return visitChildren(ctx) +
				"ISGT" + "\n" + "NOT" + "\n";
	}
	
	@Override
	public String visitGreater(GreaterContext ctx) {
		return visitChildren(ctx) +
				"ISGT" + "\n";
	}
	
	@Override
	public String visitGreaterEq(GreaterEqContext ctx) {
		return visitChildren(ctx) +
				"ISGE" + "\n";
	}
	
	@Override
	public String visitIsEq(IsEqContext ctx) {
		return visitChildren(ctx) +
				"ISEQ" + "\n";
	}
	
	@Override
	public String visitNotEq(NotEqContext ctx) {
		return visitChildren(ctx) +
				"ISEQ" + "\n" + "NOT" + "\n";
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
		Set<String> oldVariables = variables;
		variables = new HashSet<>();
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
				mainCode += instructions;
			} else {
				functions += instructions;
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
	
	private String getVariableName(Token varNameToken) {
		String varName = varNameToken.getText();
		if (!variables.contains(varName)) {
			throw new UndeclaredVariableException(varNameToken);
		}
		return varName;
	}
	
	@Override
	protected String aggregateResult(String aggregate, String nextResult) {
		if (aggregate == null) {
			return nextResult;
		}
		if (nextResult == null) {
			return aggregate;
		}
		return aggregate + nextResult;
	}
}
