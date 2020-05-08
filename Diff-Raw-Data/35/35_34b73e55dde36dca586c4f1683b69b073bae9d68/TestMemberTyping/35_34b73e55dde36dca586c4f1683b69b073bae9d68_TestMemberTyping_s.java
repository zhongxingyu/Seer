 package ch.codedump.ooplss.test;
 
 import org.junit.Test;
 
 import ch.codedump.ooplss.utils.ErrorHandler;
 
 public class TestMemberTyping extends OoplssTest {
 	
 	@Test
 	public void testMemberType() throws Exception {
 		String str = 	"class foo {" +
 					 	"	var x:foo;" +
 						"	var y:Int;" +
 						"	def __construct() {" +
 						"		x.y;" +
 						"	}" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testMemberCallType() throws Exception {
 		String str = 	"class foo {" +
 					 	"	var x:foo;" +
						"	def blah():Int;" +
						"	def blubb():foo;" +	
 						"	def __construct() {" +
 						"		x.blah();" +
 						"		/*self.blubb();*/" +
 						"	}" +
 						"}";
 		this.createTyper(str);
 		ErrorHandler.getInstance().throwException();
 	}
 }
