 import org.junit.Test;
 import junit.framework.Assert;
 
 public class TestTokenStream {
 
 	@Test
 	public void testIdentifier() {
 		TokenStream ts = new TokenStream("Identifier.jay");
 		while (!ts.isEoF()) {
 			Assert.assertEquals("Identifier", ts.nextToken().getType());
 		}
 	}
 
 	@Test
 	public void testKeyword() {
 		TokenStream ts = new TokenStream("Keyword.jay");
 		while (!ts.isEoF()) {
 			Assert.assertEquals("Keyword", ts.nextToken().getType());
 		}
 	}
 
 	@Test
 	public void testBooleanLiteral() {
		TokenStream ts = new TokenStream("BooleanLiteral.jay");
 		while (!ts.isEoF()) {
			Assert.assertEquals("Boolean-Literal", ts.nextToken().getType());
		}
	}

	@Test
	public void testIntegerLiteral() {
		TokenStream ts = new TokenStream("IntegerLiteral.jay");
		while (!ts.isEoF()) {
			Assert.assertEquals("Integer-Literal", ts.nextToken().getType());
 		}
 	}
 
 	@Test
 	public void testOperator() {
 		TokenStream ts = new TokenStream("Operator.jay");
 		while (!ts.isEoF()) {
 			Assert.assertEquals("Operator", ts.nextToken().getType());
 		}
 	}
 
 	@Test
 	public void testSeparator() {
 		TokenStream ts = new TokenStream("Separator.jay");
 		while (!ts.isEoF()) {
 			Assert.assertEquals("Separator", ts.nextToken().getType());
 		}
 	}
 
 	@Test
 	public void testOther() {
 		TokenStream ts = new TokenStream("Other.jay");
 		while (!ts.isEoF()) {
 			Assert.assertEquals("Other", ts.nextToken().getType());
 		}
 	}
 }
