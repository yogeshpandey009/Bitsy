package edu.asu.compiler;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import mycompiler.parser.MyLangBaseVisitor;
import mycompiler.parser.MyLangParser.AssignmentContext;
import mycompiler.parser.MyLangParser.AssignmentWithDeclarationContext;
import mycompiler.parser.MyLangParser.BooleanContext;
import mycompiler.parser.MyLangParser.ConditionBlockContext;
import mycompiler.parser.MyLangParser.DivContext;
import mycompiler.parser.MyLangParser.FunctionCallContext;
import mycompiler.parser.MyLangParser.FunctionDefinitionContext;
import mycompiler.parser.MyLangParser.GreaterContext;
import mycompiler.parser.MyLangParser.GreaterEqContext;
import mycompiler.parser.MyLangParser.IfStatContext;
import mycompiler.parser.MyLangParser.IsEqContext;
import mycompiler.parser.MyLangParser.LessContext;
import mycompiler.parser.MyLangParser.LessEqContext;
import mycompiler.parser.MyLangParser.LogicalANDContext;
import mycompiler.parser.MyLangParser.LogicalORContext;
import mycompiler.parser.MyLangParser.MainStatementContext;
import mycompiler.parser.MyLangParser.MinusContext;
import mycompiler.parser.MyLangParser.ModContext;
import mycompiler.parser.MyLangParser.MultContext;
import mycompiler.parser.MyLangParser.NegativeContext;
import mycompiler.parser.MyLangParser.NotEqContext;
import mycompiler.parser.MyLangParser.NumberContext;
import mycompiler.parser.MyLangParser.PlusContext;
import mycompiler.parser.MyLangParser.PositiveContext;
import mycompiler.parser.MyLangParser.PostDecExprContext;
import mycompiler.parser.MyLangParser.PostDecVarContext;
import mycompiler.parser.MyLangParser.PostIncExprContext;
import mycompiler.parser.MyLangParser.PostIncVarContext;
import mycompiler.parser.MyLangParser.PowerContext;
import mycompiler.parser.MyLangParser.PreDecExprContext;
import mycompiler.parser.MyLangParser.PreDecVarContext;
import mycompiler.parser.MyLangParser.PreIncExprContext;
import mycompiler.parser.MyLangParser.PreIncVarContext;
import mycompiler.parser.MyLangParser.PrintContext;
import mycompiler.parser.MyLangParser.ProgramContext;
import mycompiler.parser.MyLangParser.ReturnStatContext;
import mycompiler.parser.MyLangParser.VarDeclarationContext;
import mycompiler.parser.MyLangParser.VariableContext;
import mycompiler.parser.MyLangParser.WhileConditionBlockContext;
import mycompiler.parser.MyLangParser.WhileStatContext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.asu.compiler.exceptions.UndeclaredVariableException;
import edu.asu.compiler.exceptions.VariableAlreadyDefinedException;

public class MyVisitor extends MyLangBaseVisitor<String> {

	private Set<String> variables = new HashSet<>();
	private int labelCounter = 1;
	private Stack<String> scopeEndLabel = new Stack<String>();

	@Override
	public String visitPrint(PrintContext ctx) {
		return visit(ctx.argument) + "PRINT" + "\n";
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
	public String visitPower(PowerContext ctx) {
		return visitChildren(ctx) + "POW" + "\n";
	}

	@Override
	public String visitMod(ModContext ctx) {
		return visitChildren(ctx) + "MOD" + "\n";
	}

	@Override
	public String visitNumber(NumberContext ctx) {
		return "PUSH " + ctx.number.getText() + "\n";
	}

	@Override
	public String visitBoolean(BooleanContext ctx) {
		return "PUSH " + getBooleanValue(ctx.boolValue.getText()) + "\n";
	}

	// TODO:
	@Override
	public String visitPostIncExpr(PostIncExprContext ctx) {
		return visitChildren(ctx) + "PUSH 1" + "\n" + "ADD" + "\n";
	}

	// TODO:
	@Override
	public String visitPostDecExpr(PostDecExprContext ctx) {
		return visitChildren(ctx) + "PUSH 1" + "\n" + "SUB" + "\n";
	}

	@Override
	public String visitPreIncExpr(PreIncExprContext ctx) {
		return visitChildren(ctx) + "PUSH 1" + "\n" + "ADD" + "\n";
	}

	@Override
	public String visitPreDecExpr(PreDecExprContext ctx) {
		return visitChildren(ctx) + "PUSH 1" + "\n" + "SUB" + "\n";
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
	public String visitAssignmentWithDeclaration(AssignmentWithDeclarationContext ctx){
		if (variables.contains(ctx.varName.getText())) {
			throw new VariableAlreadyDefinedException(ctx.varName);
		}
		variables.add(ctx.varName.getText());
		return visit(ctx.expr)+"STORE "+ getVariableName(ctx.varName)+"\n";
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
	public String visitIfStat(IfStatContext ctx) {
		String label = generateLabel();
		scopeEndLabel.push(label);
		String result = visitChildren(ctx) + "LABEL " + label + "\n";
		scopeEndLabel.pop();
		return result;
	}

	@Override
	public String visitConditionBlock(ConditionBlockContext ctx) {
		String label = generateLabel();
		String result = visit(ctx.expr);
		result += "JIF " + label + "\n";
		result += visit(ctx.statements);
		result += "JMP " + getScopeEndLabel() + "\n";
		result += "LABEL " + label + "\n";
		return result;
	}

	@Override
	public String visitWhileStat(WhileStatContext ctx) {
		String label = generateLabel();
		scopeEndLabel.push(label);
		String result = visitChildren(ctx) + "LABEL " + label + "\n";
		scopeEndLabel.pop();
		return result;
	}

	@Override
	public String visitWhileConditionBlock(WhileConditionBlockContext ctx) {
		String label = generateLabel();
		String result = "LABEL " + label + "\n";
		result += visit(ctx.expr);
		result += "JIF " + getScopeEndLabel() + "\n";
		result += visit(ctx.statements);
		result += "JMP " + label + "\n";
		return result;
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
		result += (statementInstructions == null ? "" : statementInstructions);
		variables = oldVariables;
		return result;
	}

	@Override
	public String visitReturnStat(ReturnStatContext ctx) {
		return visitChildren(ctx) + "RET\n";
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

	@Override
	public String visitPostIncVar(PostIncVarContext ctx) {
		return "LOAD " + getVariableName(ctx.varName) + "\n" + "PUSH 1" + "\n"
				+ "ADD" + "\n" + "STORE " + getVariableName(ctx.varName) + "\n";
	}

	@Override
	public String visitPostDecVar(PostDecVarContext ctx) {
		return "LOAD " + getVariableName(ctx.varName) + "\n" + "PUSH 1" + "\n"
				+ "SUB" + "\n" + "STORE " + getVariableName(ctx.varName) + "\n";
	}

	@Override
	public String visitPreIncVar(PreIncVarContext ctx) {
		return "LOAD " + getVariableName(ctx.varName) + "\n" + "PUSH 1" + "\n"
				+ "ADD" + "\n" + "STORE " + getVariableName(ctx.varName) + "\n";
	}

	@Override
	public String visitPreDecVar(PreDecVarContext ctx) {
		return "LOAD " + getVariableName(ctx.varName) + "\n" + "PUSH 1" + "\n"
				+ "SUB" + "\n" + "STORE " + getVariableName(ctx.varName) + "\n";
	}
	
	@Override
	public String visitPositive(PositiveContext ctx) {
		 return visitChildren(ctx)+"\n";
	}
	
	@Override
	public String visitNegative(NegativeContext ctx) {
		 return "PUSH 0 " + visitChildren(ctx) + "\n"+"SUB " + "\n";
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

	private String generateLabel() {
		String label = "label_" + labelCounter;
		labelCounter++;
		return label;
	}

	private String getScopeEndLabel() {
		return scopeEndLabel.peek();
	}

	private int getBooleanValue(String text) {
		if ("true".equals(text)) {
			return 1;
		}
		return 0;
	}
}
