 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.Arrays;
 
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
 
 	@Test
 	public void test0() throws IOException { // not balanced
 		RuleSet rs = Suite.createRuleSet(Arrays.asList("auto.sl", "23t.sl"));
 
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < 32; i++)
 			sb.append(i + ", ");
 
 		assertTrue(Suite.proveThis(rs, "23t-add-list (" + sb + ") T/.t, pretty.print .t, nl, dump .d, nl"));
 	}
 
 	@Test
 	public void test1() throws IOException { // not balanced
 		RuleSet rs = Suite.createRuleSet(Arrays.asList("auto.sl", "23t.sl"));
 		String list = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,";
 		assertTrue(Suite.proveThis(rs, "23t-add-list (" + list + ") T/.t,  pretty.print .t, nl, dump .d, nl"));
 	}
 
 	@Test
 	public void test2() { // takes very long
 		eval("" //
 				+ "define type (A %) of (t,) >> \n" //
 				+ "define type (B %) of (t,) >> \n" //
 				+ "define type (C %) of (t,) >> ( \n" //
 				+ "    ((A %):1:, (A %):2:,), \n" //
 				+ "    ((B %):1:, (B %):2:,), \n" //
 				+ "    ((C %):1:, (C %):2:,), \n" //
 				+ ")");
 	}
 
 	// need to increase InstructionExecutor.stackSize, or implement tail
 	// recursion
 	@Test
 	public void test3() {
 		RuleSet rs = Suite.nodeToRuleSet(Suite.parse("" //
 				+ "member (.e, _) .e #" //
 				+ "member (_, .tail) .e :- member .tail .e #" //
 				+ "sum .a .b .c :- bound .a, bound .b, let .c (.a - .b) #" //
 				+ "sum .a .b .c :- bound .a, bound .c, let .b (.a - .c) #" //
 				+ "sum .a .b .c :- bound .b, bound .c, let .a (.b + .c) #" //
 		));
 
 		Node goal = Suite.parse("(), sink ()");
 		Builder builder = new CompiledProverBuilderLevel2(new ProverConfig(), false);
 		Suite.evaluateLogical(builder, rs, goal);
 	}
 
 	// Unknown expression otherwise error
 	@Test
 	public void test4() throws IOException {
 		byte bytes[] = new byte[65536];
 		int nBytes = getClass().getResourceAsStream("/RB-TREE.slf").read(bytes);
 		String s = new String(bytes, 0, nBytes, IoUtil.charset);
 		String fp = s + "0 until 10 | map {add} | apply | {EMPTY %}\n";
 		System.out.println("IN:\n" + fp);
 		Node result = Suite.evaluateEagerFun(fp);
 		System.out.println("OUT:\n" + Formatter.dump(result));
 	}
 
	// Strange error message "Unknown expression if b"
	@Test
	public void test5() throws IOException {
		Suite.evaluateEagerFun("if a then b");
	}

 	private static Node eval(String f) {
 		return Suite.evaluateEagerFun(f);
 	}
 
 }
