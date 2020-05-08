 import java.io.IOException;
 
 import org.junit.Test;
 
 import suite.Suite;
 import suite.fp.eval.FunRbTreeTest;
 import suite.instructionexecutor.InstructionTranslatorTest;
 
 public class FailedTests {
 
 	// Type check take 11 seconds
 	@Test
 	public void test0() throws IOException {
 		new FunRbTreeTest().test();
 	}
 
 	// /tmp/suite.instructionexecutor.InstructionTranslator/suite/instructionexecutor/TranslatedRun88.java:1034:
 	// error: code too large
 	// public Node exec(TranslatedRunConfig config, Closure closure) {
 	@Test
 	public void test1() throws IOException {
 		new InstructionTranslatorTest().testStandardLibrary();
 	}
 
 	// UnsupportedOperationException
 	@Test
 	public void test2() throws IOException {
 		new InstructionTranslatorTest().testAtomString();
 	}
 
 	// Cyclic types
 	@Test
 	public void test3() {
 		Suite.evaluateFunType("" //
				+ "using STANDARD >> \n" //
				+ "define grp = (list0 => \n" //
				+ "  if-bind (list0 = ($v0;)) then (v0;); else list0 \n" //
				+ ") >> \n" //
				+ "grp {} \n" //
 		);
 	}
 
 }
