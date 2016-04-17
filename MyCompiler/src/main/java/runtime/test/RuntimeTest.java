package main.java.runtime.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
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
		VM.execute(new String[] { "PUSH", "hello world", "PRINT", "HALT" });
		assertEquals("hello world", outContent.toString());
	}

	@Test
	public void testEcho() {
		/*
		 * print(input());
		 */
		ByteArrayInputStream in = new ByteArrayInputStream("hello".getBytes());
		System.setIn(in);
		VM.execute(new String[] { "INPUT", "PRINT", "HALT" });
		assertEquals("hello", outContent.toString());
		System.setIn(System.in);
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
	public void testPow() {
		/*
		 * print(2^3);
		 */
		VM.execute(new String[] { "PUSH", "2", "PUSH", "3", "POW", "PRINT",
				"HALT" });
		assertEquals("8", outContent.toString());
	}


	@Test
	public void testMod() {
		/*
		 * print(7%4);
		 */
		VM.execute(new String[] { "PUSH", "7", "PUSH", "4", "MOD", "PRINT",
				"HALT" });
		assertEquals("3", outContent.toString());
	}

	@Test
	public void testVariable() {
		/*
		 * a = 1;
		 * b = 2;
		 * print(a-b);
		 */
		VM.execute(new String[] { "PUSH", "1", "STORE", "a", "PUSH", "2",
				"STORE", "b", "LOAD", "a", "LOAD", "b", "SUB", "PRINT", "HALT" });
		assertEquals("-1", outContent.toString());
	}

	@Test
	public void testComplexExpression() {
		/*
		 * x = (2 + 3 * 4)/7;
		 * print(x);
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
				"PUSH", "0", "JIF", "label1", "LABEL", "label2", "LOAD", "sum",
				"PRINT", "HALT" });
		assertEquals("55", outContent.toString());
	}

	@Test
	public void testFunctionCall() {
		/*
		 * x = 3;
		 * y = 5;
		 * print(avg(x, y));
		 * func avg(int a, int b) {
		 * 	return (a + b)/2;
		 * }
		 */
		VM.execute(new String[] { "PUSH", "3", "STORE", "x", "PUSH", "5",
				"STORE", "y", "LOAD", "x", "LOAD", "y", "CALL", "avg", "PRINT",
				"HALT", "LABEL", "avg", "STORE", "b", "STORE", "a", "LOAD",
				"a", "LOAD", "b", "ADD", "PUSH", "2", "DIV", "RET" });
		assertEquals("4", outContent.toString());
	}

	@Test
	public void testRecursion() {
		/*
		 * x = 5;
		 * print(fact(x));
		 * func fact(int n) {
		 * 	if(n==1) {
		 * 		return 1;
		 * 	}
		 * 	return n * fact(n-1);
		 * }
		 */
		VM.execute(new String[] { "PUSH", "5", "STORE", "x", "LOAD", "x",
				"CALL", "fact", "PRINT", "HALT", "LABEL", "fact", "STORE", "n",
				"LOAD", "n", "PUSH", "1", "ISEQ", "JIF", "label1", "PUSH", "1",
				"RET", "LABEL", "label1", "LOAD", "n", "LOAD", "n", "PUSH",
				"1", "SUB", "CALL", "fact", "MUL", "RET" });
		assertEquals("120", outContent.toString());
	}
}
