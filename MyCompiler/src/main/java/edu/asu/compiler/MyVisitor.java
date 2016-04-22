package edu.asu.compiler;

import java.util.HashSet;
import java.util.Set;

import edu.asu.compiler.exceptions.UndeclaredVariableException;
import edu.asu.compiler.exceptions.VariableAlreadyDefinedException;
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
import mycompiler.parser.MyLangParser.ParanContext;
import mycompiler.parser.MyLangParser.PlusContext;
import mycompiler.parser.MyLangParser.PrintContext;
import mycompiler.parser.MyLangParser.ProgramContext;
import mycompiler.parser.MyLangParser.VarDeclarationContext;
import mycompiler.parser.MyLangParser.VariableContext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

public class MyVisitor extends MyLangBaseVisitor<String> {

	private Set<String> variables = new HashSet<>();

	@Override
	public String visitPrint(PrintContext ctx) {
		return visit(ctx.argument) + "PRINT" + "\n";
	}

	@Override
	public String visitParan(ParanContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitPlus(PlusContext ctx) {
		return visitChildren(ctx) + "ADD" + "\n";
	}

	@Override
	public String visitMinus(MinusContext ctx) {
		return visitChildren(ctx) + "SUB" + "\n";
	}

	@Override
	public String visitDiv(DivContext ctx) {
		return visitChildren(ctx) + "DIV" + "\n";
	}

	@Override
	public String visitMult(MultContext ctx) {
		return visitChildren(ctx) + "MUL" + "\n";
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
		return visit(ctx.expr) + "STORE " + getVariableName(ctx.varName) + "\n";
	}

	@Override
	public String visitVariable(VariableContext ctx) {
		return "LOAD " + getVariableName(ctx.varName) + "\n";
	}

	@Override
	public String visitLogicalAND(LogicalANDContext ctx) {
		return visitChildren(ctx) + "AND" + "\n";
	}

	@Override
	public String visitLogicalOR(LogicalORContext ctx) {
		return visitChildren(ctx) + "OR" + "\n";
	}

	@Override
	public String visitLess(LessContext ctx) {
		return visitChildren(ctx) + "ISGE" + "\n" + "NOT" + "\n";
	}

	@Override
	public String visitLessEq(LessEqContext ctx) {
		return visitChildren(ctx) + "ISGT" + "\n" + "NOT" + "\n";
	}

	@Override
	public String visitGreater(GreaterContext ctx) {
		return visitChildren(ctx) + "ISGT" + "\n";
	}

	@Override
	public String visitGreaterEq(GreaterEqContext ctx) {
		return visitChildren(ctx) + "ISGE" + "\n";
	}

	@Override
	public String visitIsEq(IsEqContext ctx) {
		return visitChildren(ctx) + "ISEQ" + "\n";
	}

	@Override
	public String visitNotEq(NotEqContext ctx) {
		return visitChildren(ctx) + "ISEQ" + "\n" + "NOT" + "\n";
	}

	@Override
	public String visitFunctionCall(FunctionCallContext ctx) {
		String instructions = "";
		String argumentsInstructions = visit(ctx.arguments);
		if (argumentsInstructions != null) {
			instructions += argumentsInstructions;
		}
		instructions += "CALL " + ctx.funcName.getText() + "\n";
		return instructions;
	}

	@Override
	public String visitFunctionDefinition(FunctionDefinitionContext ctx) {
		Set<String> oldVariables = variables;
		variables = new HashSet<>();
		visit(ctx.params);
		String statementInstructions = visit(ctx.statements);
		String result = "LABEL " + ctx.funcName.getText() + "\n";
		int numberOfParameters = ctx.params.declarations.size();
		for (int i = numberOfParameters - 1; i >= 0; i--) {
			result += "STORE "
					+ ctx.params.declarations.get(i).varName.getText() + "\n";
		}
		result += (statementInstructions == null ? "" : statementInstructions)
				+ visit(ctx.returnValue) + "RET\n";
		variables = oldVariables;
		return result;
	}

	@Override
	public String visitProgram(ProgramContext ctx) {
		String mainCode = "";
		String functions = "";
		for (int i = 0; i < ctx.getChildCount(); ++i) {
			ParseTree child = ctx.getChild(i);
			String instructions = visit(child);
			if (child instanceof MainStatementContext) {
				mainCode += instructions;
			} else {
				functions += instructions;
			}
		}
		return mainCode + "HALT\n" + functions;
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