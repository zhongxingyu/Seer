 package transformers.transformer;
 
 import java.math.BigDecimal;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import transformers.Transformer;
 
 
 public class TransformTest {
 	@Test public void returnsFromObjectWhenTransformationDoesNotExist() {
		Object actualObject = Transformer.newInstance().transform("Hello World", String.class);
 		Assert.assertEquals("Hello World", actualObject);
 	}
 	
 	@Test public void fromNullToNullReturnsNull() {
		Object actualObject = Transformer.newInstance().transform(null, null);
 		Assert.assertEquals(null, actualObject);
 	}
 	
 	@Test public void fromBigDecimalToDouble() {
		Object actualObject = Transformer.newInstance().transform(new BigDecimal(3.14), Double.class);
 		Double expectedObject = new Double(3.14);
 		Assert.assertEquals(expectedObject, actualObject);
 	}
 }
