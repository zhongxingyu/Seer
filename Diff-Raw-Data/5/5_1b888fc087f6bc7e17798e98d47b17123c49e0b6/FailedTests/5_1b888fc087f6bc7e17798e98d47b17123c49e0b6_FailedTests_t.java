 import java.io.IOException;
 
 import org.instructionexecutor.InstructionCompilerTest;
 import org.junit.Test;
 import org.suite.Suite;
 import org.suite.doer.Formatter;
 import org.suite.doer.ProverConfig;
 import org.suite.kb.RuleSet;
 import org.suite.node.Node;
 import org.suite.search.CompiledProverBuilder.CompiledProverBuilderLevel2;
 import org.suite.search.ProverBuilder.Builder;
 import org.util.IoUtil;
 
 public class FailedTests {
 
	// Need to increase InstructionExecutor.stackSize, or implement tail
 	// recursion
 	@Test
 	public void test1() {
 		RuleSet rs = Suite.nodeToRuleSet(Suite.parse("" //
 				+ "member (.e, _) .e #" //
 				+ "member (_, .tail) .e :- member .tail .e #" //
 				+ "sum .a .b .c :- bound .a, bound .b, let .c (.a - .b) #" //
 				+ "sum .a .b .c :- bound .a, bound .c, let .b (.a - .c) #" //
 				+ "sum .a .b .c :- bound .b, bound .c, let .a (.b + .c) #" //
 		));
 
 		Node goal = Suite.parse("(), sink ()");
 		Builder builder = new CompiledProverBuilderLevel2(new ProverConfig(), false);
 		Suite.evaluateLogic(builder, rs, goal);
 	}
 
	// Type check take 16 seconds
 	@Test
 	public void test2() throws IOException {
 		String s = IoUtil.readStream(getClass().getResourceAsStream("/RB-TREE.slf"));
 		String fp = s + "0 until 10 | map {add} | apply | {EMPTY %}\n";
 		Node result = Suite.evaluateFun(fp, false);
 		System.out.println("OUT:\n" + Formatter.dump(result));
 	}
 
 	// Strange error message "Unknown expression if b"
 	@Test
 	public void test3() throws IOException {
 		Suite.evaluateFun("if a then b", false);
 	}
 
 	// Code too large
 	@Test
 	public void test4() throws IOException {
 		new InstructionCompilerTest().testEagerFunctional();
 	}
 
 }
