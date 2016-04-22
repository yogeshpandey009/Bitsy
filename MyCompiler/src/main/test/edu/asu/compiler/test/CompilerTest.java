package edu.asu.compiler.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.asu.compiler.Translator;
import edu.asu.compiler.exceptions.UndeclaredVariableException;
import edu.asu.compiler.exceptions.VariableAlreadyDefinedException;
import edu.asu.runtime.VM;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CompilerTest {
	private Path tempDir;
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	@BeforeTest
	public void setUpStreams() {

		System.setOut(new PrintStream(outContent));
	}

	@AfterTest
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@BeforeMethod
	public void createTempDir() throws IOException {
		tempDir = Files.createTempDirectory("compilerTest");
	}

	@AfterMethod
	public void deleteTempDir() {
		deleteRecursive(tempDir.toFile());
	}

	private void deleteRecursive(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				deleteRecursive(child);
			}
		}
		if (!file.delete()) {
			throw new Error("Could not delete file <" + file + ">");
		}
	}

	@Test(dataProvider = "provide_code_expectedText")
	public void runningCode_outputsExpectedText(String code, String expectedText)
			throws Exception {
		// execution
		compileAndRun(code);

		// evaluation
		Assert.assertEquals(outContent.toString(), expectedText);
		outContent.reset();
	}

	@Test(expectedExceptions = UndeclaredVariableException.class, expectedExceptionsMessageRegExp = "1:6 undeclared variable <x>")
	public void compilingCode_throwsUndeclaredVariableException_ifReadingUndefinedVariable()
			throws Exception {
		// execution
		compileAndRun("print(x);");

		// evaluation performed by expected exception
	}

	@Test(expectedExceptions = UndeclaredVariableException.class, expectedExceptionsMessageRegExp = "1:0 undeclared variable <x>")
	public void compilingCode_throwsUndeclaredVariableException_ifWritingUndefinedVariable()
			throws Exception {
		// execution
		compileAndRun("x = 5;");

		// evaluation performed by expected exception
	}

	@Test(expectedExceptions = VariableAlreadyDefinedException.class, expectedExceptionsMessageRegExp = "2:4 variable already defined: <x>")
	public void compilingCode_throwsVariableAlreadyDefinedException_whenDefiningAlreadyDefinedVariable()
			throws Exception {
		// execution
		compileAndRun("int x;" + System.lineSeparator() + "int x;");

		// evaluation performed by expected exception
	}

	@DataProvider
	public Object[][] provide_code_expectedText() {
		return new Object[][] {
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
								+ "	return n * fact(n-1);\n" + "}", "120" } };
	}

	private void compileAndRun(String code) throws Exception {
		code = Translator.compile(new ANTLRInputStream(code));
		VM.run(code);
	}

}