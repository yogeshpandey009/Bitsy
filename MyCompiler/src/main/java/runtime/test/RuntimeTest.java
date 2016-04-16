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
		 * print("hello")
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
		 * a = 1 b = 2 print(a-b)
		 */
		VM.execute(new String[] { "PUSH", "1", "STORE", "a", "PUSH", "2",
				"STORE", "b", "LOAD", "a", "LOAD", "b", "SUB", "PRINT", "HALT" });
		assertEquals("-1", outContent.toString());
	}

	@Test
	public void testExpression() {
		/*
		 * x = (2 + 3 * 4)/7 print(x)
		 */
		VM.execute(new String[] { "PUSH", "2", "PUSH", "3", "PUSH", "4", "MUL",
				"ADD", "PUSH", "7", "DIV", "STORE", "x", "LOAD", "x", "PRINT",
				"HALT" });
		assertEquals("2", outContent.toString());
	}
}
