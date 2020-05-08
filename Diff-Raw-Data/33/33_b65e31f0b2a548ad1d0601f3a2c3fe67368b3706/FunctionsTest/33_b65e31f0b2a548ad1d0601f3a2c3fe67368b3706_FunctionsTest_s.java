 package org.funcish.core;
 
 import org.funcish.core.ann.MethodFunction;
 import org.funcish.core.fn.Function;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class FunctionsTest {
 	@Test
 	public void testFn_Object() throws Exception {
 		Object plus = new Object() {
 			@MethodFunction
 			public int add(int lhs, int rhs) {
 				return lhs + rhs;
 			}
 		};
 		Function<?> f = Functions.fn(plus);
 		Assert.assertNotNull(f);
 	}
 }
