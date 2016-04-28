package edu.asu.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class CompileTimeException extends RuntimeException {
	protected int line;
	protected int column;
	
	public CompileTimeException(Token token) {
		line = token.getLine();
		column = token.getCharPositionInLine();
	}
}
