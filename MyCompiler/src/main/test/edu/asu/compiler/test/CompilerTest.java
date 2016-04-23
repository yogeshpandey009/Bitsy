package edu.asu.compiler.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import edu.asu.compiler.Translator;
import edu.asu.compiler.exceptions.UndeclaredVariableException;
import edu.asu.compiler.exceptions.VariableAlreadyDefinedException;
import edu.asu.runtime.VM;
import edu.asu.runtime.exceptions.ProgramExecutionException;

public class CompilerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void perfromAllValidTest()
			throws Exception {
		String[][] data = dataProvider();
		for(String[] tc: data) {
			try {
				// execution
				ByteArrayOutputStream outSpy = new ByteArrayOutputStream();
				System.setOut(new PrintStream(outSpy));
				compileAndRun(tc[0]);
				// evaluation performed by expected exception
				Assert.assertEquals(tc[1], outSpy.toString());
				System.setOut(null);
			} catch(ProgramExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void throwsUndeclaredVariableException_ifReadingUndefinedVariableTest()
			throws Exception {
		thrown.expect(UndeclaredVariableException.class);
		thrown.expectMessage("1:6 undeclared variable <x>");
		// execution
		compileAndRun("print(x);");

		// evaluation performed by expected exception
	}

	@Test
	public void throwsUndeclaredVariableException_ifWritingUndefinedVariableTest()
			throws Exception {
		thrown.expect(UndeclaredVariableException.class);
		thrown.expectMessage("1:0 undeclared variable <x>");
		// execution
		compileAndRun("x = 5;");

		// evaluation performed by expected exception
	}

	@Test
	public void throwsVariableAlreadyDefinedException_whenDefiningAlreadyDefinedVariableTest()
			throws Exception {
		thrown.expect(VariableAlreadyDefinedException.class);
		thrown.expectMessage("2:4 variable already defined: <x>");
		// execution
		compileAndRun("int x;" + System.lineSeparator() + "int x;");

		// evaluation performed by expected exception
	}

	public String[][] dataProvider() {
		return new String[][] {
				{ "print(1+2);", "3" },
				{ "print(1+2+42);", "45" },
				{ "print(1); print(2);", "1" + "2" },
				{ "print(3-2);", "1" },
				{ "print(2*3);", "6" },
				{ "print(6/2);", "3" },
				{ "print(7/2);", "3" },
				{ "print(8/2*4);", "16" },
				{ "print(2+3*3);", "11" },
				{ "print(9-2*3);", "3" },
				{ "print(8-2+5);", "11" },
				{ "print(-2);", "-2" },
				{ "print(-2 - 3);", "-5" },
				{ "print(-2 - 3 + 4);", "-1" },
				{ "int foo; foo = 42; print(foo);", "42" },
				{ "int foo; foo = 42; print(foo+2);", "44" },
				{ "int a; int b; a = 2; b = 5; print(a+b);", "7" },
				{ "int randomNumber() { return 4; } print(randomNumber());",
						"4" },
				{
						"int randomNumber() {\n" + "  int i;\n" + "  i = 4;\n"
								+ "  return i;\n" + "}\n"
								+ "print(randomNumber());", "4" },

				{
						"int randomNumber() {\n" + "  int i;\n" + "  i = 4;\n"
								+ "  return i;\n" + "}\n" + "int i;\n"
								+ "i = 42;\n" + "print(randomNumber());\n"
								+ "print(i);", "4" + "42" },
				{
						"int add(int a, int b) {\n" + "  return a+b;\n" + "}\n"
								+ "print(add(5,8));", "13" },
				{
						"int a;\n" + "a = 6;\n" + "if(a < 5) {\n"
								+ "	print(1);\n" + "} elif(a < 10) {\n"
								+ "	print(2);\n" + "} else {\n"
								+ "	print(3);\n" + "}", "2" },
				{
						"int x;\n" + "x = 5;\n" + "print(fact(x));\n"
								+ "int fact(int n) {\n" + "	if(n==1) {\n"
								+ "		return 1;\n" + "	}\n"
								+ "	return n * fact(n-1);\n" + "}", "120" },
				{
						"if(1<2) {\n" + "	if(2>3) {\n" + "		print(-1);\n"
								+ "	} else {\n" + "		print(1);\n" + "	}\n"
								+ "} else {\n" + "	print(0);\n" + "}\n"
								+ "if(3<4) {\n" + "	print(2);\n" + "} else {\n"
								+ "	print(-2);\n" + "}\n" + "print(3);", "123" } };
	}

	private void compileAndRun(String code) throws Exception {
		code = Translator.compile(new ANTLRInputStream(code));
		VM.run(code);
	}

}