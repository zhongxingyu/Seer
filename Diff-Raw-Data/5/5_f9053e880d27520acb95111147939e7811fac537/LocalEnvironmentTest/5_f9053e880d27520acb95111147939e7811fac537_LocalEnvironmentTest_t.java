 package edu.tum.lua.junit;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
import edu.tum.lua.GlobalEnvironment;
 import edu.tum.lua.LocalEnvironment;
 
 public class LocalEnvironmentTest {
 
 	@Test
 	public void test() {
 		LocalEnvironment l = new LocalEnvironment();
 		LocalEnvironment f = new LocalEnvironment(l);
		GlobalEnvironment.getGlobalEnvironment().set("a", "a");
 		l.setLocal("b", "b");
 		f.setLocal("a", "c");
 		f.set("b", "a");
 		f.set("c", "c");
 		assertEquals("c", f.get("c"));
 		assertEquals("c", f.get("a"));
 		assertEquals("a", f.get("b"));
 	}
 
 }
