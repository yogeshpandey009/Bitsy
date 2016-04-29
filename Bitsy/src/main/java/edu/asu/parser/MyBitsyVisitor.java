package edu.asu.parser;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.parser.BitsyBaseVisitor;
import compiler.parser.BitsyParser.AssignWithDeclContext;
import compiler.parser.BitsyParser.AssignmentContext;
import compiler.parser.BitsyParser.BooleanContext;
import compiler.parser.BitsyParser.ConditionBlockContext;
import compiler.parser.BitsyParser.DivContext;
import compiler.parser.BitsyParser.FunctionCallContext;
import compiler.parser.BitsyParser.FunctionDefinitionContext;
import compiler.parser.BitsyParser.GreaterContext;
import compiler.parser.BitsyParser.GreaterEqContext;
import compiler.parser.BitsyParser.IfStatContext;
import compiler.parser.BitsyParser.InputContext;
import compiler.parser.BitsyParser.IsEqContext;
import compiler.parser.BitsyParser.LessContext;
import compiler.parser.BitsyParser.LessEqContext;
import compiler.parser.BitsyParser.LogicalANDContext;
import compiler.parser.BitsyParser.LogicalORContext;
import compiler.parser.BitsyParser.MainStatementContext;
import compiler.parser.BitsyParser.MinusContext;
import compiler.parser.BitsyParser.ModContext;
import compiler.parser.BitsyParser.MultContext;
import compiler.parser.BitsyParser.NegativeContext;
import compiler.parser.BitsyParser.NotEqContext;
import compiler.parser.BitsyParser.NumberContext;
import compiler.parser.BitsyParser.PlusContext;
import compiler.parser.BitsyParser.PositiveContext;
import compiler.parser.BitsyParser.PostDecExprContext;
import compiler.parser.BitsyParser.PostDecVarContext;
import compiler.parser.BitsyParser.PostIncExprContext;
import compiler.parser.BitsyParser.PostIncVarContext;
import compiler.parser.BitsyParser.PowerContext;
import compiler.parser.BitsyParser.PreDecExprContext;
import compiler.parser.BitsyParser.PreDecVarContext;
import compiler.parser.BitsyParser.PreIncExprContext;
import compiler.parser.BitsyParser.PreIncVarContext;
import compiler.parser.BitsyParser.PrintExprContext;
import compiler.parser.BitsyParser.PrintTextContext;
import compiler.parser.BitsyParser.ProgramContext;
import compiler.parser.BitsyParser.ReturnStatContext;
import compiler.parser.BitsyParser.StackIsEmptyContext;
import compiler.parser.BitsyParser.StackPeekContext;
import compiler.parser.BitsyParser.StackPopContext;
import compiler.parser.BitsyParser.StackPushContext;
import compiler.parser.BitsyParser.StackVariableDeclarationContext;
import compiler.parser.BitsyParser.VarDeclarationContext;
import compiler.parser.BitsyParser.VariableContext;
import compiler.parser.BitsyParser.VariableDeclarationContext;
import compiler.parser.BitsyParser.WhileConditionBlockContext;
import compiler.parser.BitsyParser.WhileStatContext;

import edu.asu.compiler.exceptions.UndeclaredVariableException;
import edu.asu.compiler.exceptions.VariableAlreadyDefinedException;

public class MyBitsyVisitor extends BitsyBaseVisitor<String> {

	private Set<String> variables = new HashSet<>();
	private Set<String> stackVariables = new HashSet<>();
	private int labelCounter = 1;
	private Stack<String> scopeEndLabel = new Stack<String>();

	@Override
	public String visitPrintExpr(PrintExprContext ctx) {
		return visit(ctx.argument) + "PRINT" + "\n";
	}

	@Override
	public String visitInput(InputContext ctx) {
		return "INPUT" + "\n";
	}

	@Override
	public String visitPrintText(PrintTextContext ctx) {
		return "PUSH " + ctx.text.getText() + "\n" + "PRINT" + "\n";
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
	public String visitStackPush(StackPushContext ctx) {
		return visit(ctx.expr) + "STACK_PUSH "
				+ getStackVariableNameIfExist(ctx.varName) + "\n";
	}

	@Override
	public String visitStackPop(StackPopContext ctx) {
		return "STACK_POP " + getStackVariableNameIfExist(ctx.varName) + "\n";
	}

	@Override
	public String visitStackPeek(StackPeekContext ctx) {
		return "STACK_PEEK " + getStackVariableNameIfExist(ctx.varName) + "\n";
	}

	@Override
	public String visitStackIsEmpty(StackIsEmptyContext ctx) {
		return "STACK_ISEMPTY " + getStackVariableNameIfExist(ctx.varName)
				+ "\n";
	}

	@Override
	public String visitNumber(NumberContext ctx) {
		return "PUSH " + ctx.number.getText() + "\n";
	}

	@Override
	public String visitBoolean(BooleanContext ctx) {
		return "PUSH " + ctx.boolValue.getText() + "\n";
	}

	// TODO:
	@Override
	public String visitPostIncExpr(PostIncExprContext ctx) {
		return visitChildren(ctx) + "PUSH 1" + "\n" + "ADD" + "\n";
	}

	// TODO: Prefix and Postfix expr should be handled differently
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
	public String visitVariableDeclaration(VariableDeclarationContext ctx) {
		String name = ctx.varName.getText();
		if (variables.contains(name) || stackVariables.contains(name)) {
			throw new VariableAlreadyDefinedException(ctx.varName);
		}
		variables.add(ctx.varName.getText());
		return "";
	}

	@Override
	public String visitStackVariableDeclaration(
			StackVariableDeclarationContext ctx) {
		String name = ctx.varName.getText();
		if (variables.contains(name) || stackVariables.contains(name)) {
			throw new VariableAlreadyDefinedException(ctx.varName);
		}
		stackVariables.add(ctx.varName.getText());
		return "";
	}

	@Override
	public String visitAssignment(AssignmentContext ctx) {
		return visit(ctx.expr) + "STORE " + getVariableNameIfExist(ctx.varName)
				+ "\n";
	}

	@Override
	public String visitAssignWithDecl(
			AssignWithDeclContext ctx) {
		VarDeclarationContext varDecCtx = ctx.varDeclaration();
		return visit(varDecCtx) + visit(ctx.expr) + "STORE "
				+ getVariableNameToken(varDecCtx).getText() + "\n";
	}

	@Override
	public String visitVariable(VariableContext ctx) {
		return "LOAD " + getVariableNameIfExist(ctx.varName) + "\n";
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
		Set<String> oldVariables = variables;;
		variables = new HashSet<>();
		//add global variables at function level
		variables.addAll(oldVariables);
		visit(ctx.params);
		String statementInstructions = visit(ctx.statements);
		String result = "LABEL " + ctx.funcName.getText() + "\n";
		int numberOfParameters = ctx.params.declarations.size();
		for (int i = numberOfParameters - 1; i >= 0; i--) {
			result += "STORE "
					+ getVariableNameToken(ctx.params.declarations.get(i))
							.getText() + "\n";
		}
		result += (statementInstructions == null ? "" : statementInstructions);
		//revert to global variables
		variables = oldVariables;
		return result;
	}

	@Override
	public String visitReturnStat(ReturnStatContext ctx) {
		String result = "";
		if(ctx.returnValue != null) {
			result += visitChildren(ctx);
		}
		return result + "RET\n";
	}

	@Override
	public String visitPostIncVar(PostIncVarContext ctx) {
		return "LOAD " + getVariableNameIfExist(ctx.varName) + "\n" + "PUSH 1"
				+ "\n" + "ADD" + "\n" + "STORE "
				+ getVariableNameIfExist(ctx.varName) + "\n";
	}

	@Override
	public String visitPostDecVar(PostDecVarContext ctx) {
		return "LOAD " + getVariableNameIfExist(ctx.varName) + "\n" + "PUSH 1"
				+ "\n" + "SUB" + "\n" + "STORE "
				+ getVariableNameIfExist(ctx.varName) + "\n";
	}

	@Override
	public String visitPreIncVar(PreIncVarContext ctx) {
		return "LOAD " + getVariableNameIfExist(ctx.varName) + "\n" + "PUSH 1"
				+ "\n" + "ADD" + "\n" + "STORE "
				+ getVariableNameIfExist(ctx.varName) + "\n";
	}

	@Override
	public String visitPreDecVar(PreDecVarContext ctx) {
		return "LOAD " + getVariableNameIfExist(ctx.varName) + "\n" + "PUSH 1"
				+ "\n" + "SUB" + "\n" + "STORE "
				+ getVariableNameIfExist(ctx.varName) + "\n";
	}

	@Override
	public String visitPositive(PositiveContext ctx) {
		return visitChildren(ctx) + "\n";
	}

	@Override
	public String visitNegative(NegativeContext ctx) {
		return "PUSH 0 " + visitChildren(ctx) + "\n" + "SUB " + "\n";
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

	private Token getVariableNameToken(VarDeclarationContext varDecCtx) {
		Token varNameToken = null;
		if (varDecCtx instanceof VariableDeclarationContext) {
			varNameToken = ((VariableDeclarationContext) varDecCtx).varName;
		} else {
			varNameToken = ((StackVariableDeclarationContext) varDecCtx).varName;
		}
		return varNameToken;
	}

	private String getVariableNameIfExist(Token varNameToken) {
		String varName = varNameToken.getText();
		if (!variables.contains(varName)) {
			throw new UndeclaredVariableException(varNameToken);
		}
		return varName;
	}

	private String getStackVariableNameIfExist(Token stackVarNameToken) {
		String stackVarName = stackVarNameToken.getText();
		if (!stackVariables.contains(stackVarName)) {
			throw new UndeclaredVariableException(stackVarNameToken);
		}
		return stackVarName;
	}

	private String generateLabel() {
		String label = "label_" + labelCounter;
		labelCounter++;
		return label;
	}

	private String getScopeEndLabel() {
		return scopeEndLabel.peek();
	}

}
