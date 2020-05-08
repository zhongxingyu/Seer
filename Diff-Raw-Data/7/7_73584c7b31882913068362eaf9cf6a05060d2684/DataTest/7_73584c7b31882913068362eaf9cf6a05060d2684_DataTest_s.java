 package org.weymouth.demo.model;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class DataTest {
 
 	@Test
 	public void test(){
 		String probe = "probe";
 		Data d = new Data(probe);
 		String data = d.getData();
		data = null;
 		Assert.assertNotNull(data);
 		Assert.assertEquals(probe, data);
 	}
 }
