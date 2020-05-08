 import java.io.IOException;
 
 import org.instructionexecutor.FunRbTreeTest;
 import org.instructionexecutor.LogicCompilerLevel2Test;
 import org.junit.Test;
 import org.suite.Suite;
 import org.suite.doer.ProverConfig;
 import org.suite.kb.RuleSet;
 import org.suite.node.Node;
 import org.suite.search.CompiledProverBuilder.CompiledProverBuilderLevel2;
 import org.suite.search.ProverBuilder.Builder;
 
 public class FailedTests {
 
 	// Type check take 11 seconds
 	@Test
 	public void test0() throws IOException {
 		new FunRbTreeTest().test();
 	}
 
 	// Strange error message "Unknown expression if b"
 	@Test
 	public void test1() throws IOException {
 		Suite.evaluateFun("if a then b", false);
 	}
 
	@Test
	public void test2() throws IOException {
		new LogicCompilerLevel2Test().test1();
	}

 }
