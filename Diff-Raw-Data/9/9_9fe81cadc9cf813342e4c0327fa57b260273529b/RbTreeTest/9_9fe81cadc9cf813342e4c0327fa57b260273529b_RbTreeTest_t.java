 package org.suite;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.suite.kb.RuleSet;
 import org.suite.kb.RuleSet.RuleSetUtil;
 
 public class RbTreeTest {
 
 	@Test
 	public void test() throws IOException {
 		RuleSet rs = RuleSetUtil.create();
 		SuiteUtil.importResource(rs, "auto.sl");
		SuiteUtil.importResource(rs, "rbt.sl");
 
 		assertTrue(SuiteUtil.proveThis(rs, "" //
				+ "rbt-add-list (6, 7, 8, 9, 10, 1, 2, 3, 4, 5,) ()/.t \n" //
				+ ", rbt-get .t 8" //
				+ ", rbt-member .t 4"));
 	}
 
 }
