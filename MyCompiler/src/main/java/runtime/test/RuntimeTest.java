package main.java.runtime.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import main.java.runtime.VM;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RuntimeTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void testPrint() {
		/*
		 * print("hello");
		 */
		VM.execute(new String[] { "PUSH", "hello", "PRINT", "HALT" });
		assertEquals("hello", outContent.toString());
	}

	@Test
	public void testDivision() {
		/*
		 * print(9/3);
		 */
		VM.execute(new String[] { "PUSH", "9", "PUSH", "3", "DIV", "PRINT",
				"HALT" });
		assertEquals("3", outContent.toString());
	}

	@Test
	public void testVariable() {
		/*
		 * a = 1; b = 2; print(a-b);
		 */
		VM.execute(new String[] { "PUSH", "1", "STORE", "a", "PUSH", "2",
				"STORE", "b", "LOAD", "a", "LOAD", "b", "SUB", "PRINT", "HALT" });
		assertEquals("-1", outContent.toString());
	}

	@Test
	public void testComplexExpression() {
		/*
		 * x = (2 + 3 * 4)/7; print(x);
		 */
		VM.execute(new String[] { "PUSH", "2", "PUSH", "3", "PUSH", "4", "MUL",
				"ADD", "PUSH", "7", "DIV", "STORE", "x", "LOAD", "x", "PRINT",
				"HALT" });
		assertEquals("2", outContent.toString());
	}

	@Test
	public void testIsEqualOperator() {
		/*
		 * print(3 == 3);
		 */
		VM.execute(new String[] { "PUSH", "3", "PUSH", "3", "ISEQ", "PRINT",
				"HALT" });
		assertEquals("1", outContent.toString());
	}

	@Test
	public void testNotOperator() {
		/*
		 * print(3 != 3);
		 */
		VM.execute(new String[] { "PUSH", "3", "PUSH", "3", "ISEQ", "NOT",
				"PRINT", "HALT" });
		assertEquals("0", outContent.toString());
	}

	@Test
	public void testLogicalOperators() {
		/*
		 * print(5 > 3 && 4 <= 7)
		 */
		VM.execute(new String[] { "PUSH", "5", "PUSH", "3", "ISGT", "PUSH",
				"4", "PUSH", "7", "ISGT", "NOT", "AND", "PRINT", "HALT" });
		assertEquals("1", outContent.toString());
	}

	@Test
	public void testJumpIfFalse() {
		/*
		 * x = 1; if(0) { x = 2 } print(x);
		 */
		VM.execute(new String[] { "PUSH", "1", "STORE", "x", "PUSH", "0",
				"JIF", "Label1", "PUSH", "2", "STORE", "x", "LABEL", "Label1",
				"LOAD", "x", "PRINT", "HALT" });
		assertEquals("1", outContent.toString());
	}

	@Test
	public void testNoJumpIfTrue() {
		/*
		 * x = 1; 
		 * if(x == 1) { 
		 * 	x = 2 
		 * } 
		 * print(x);
		 */
		VM.execute(new String[] { "PUSH", "1", "STORE", "x", "LOAD", "x",
				"PUSH", "1", "ISEQ", "JIF", "Label1", "PUSH", "2", "STORE",
				"x", "LABEL", "Label1", "LOAD", "x", "PRINT", "HALT" });
		assertEquals("2", outContent.toString());
	}
	
	@Test
	public void testWhileLoop() {
		/*
		 * sum = 0; i = 10; 
		 * while(i > 0) { 
		 * 	sum = sum + i; 
		 * 	i = i - 1; 
		 * } 
		 * print(sum);
		 */
		VM.execute(new String[] { "PUSH", "0", "STORE", "sum", "PUSH", "10",
				"STORE", "i", "LABEL", "label1", "LOAD", "i", "PUSH", "0",
				"ISGT", "JIF", "label2", "LOAD", "sum", "LOAD", "i", "ADD",
				"STORE", "sum", "LOAD", "i", "PUSH", "1", "SUB", "STORE", "i",
				"JMP", "label1", "LABEL", "label2", "LOAD", "sum", "PRINT",
				"HALT" });
		assertEquals("55", outContent.toString());
	}
}
