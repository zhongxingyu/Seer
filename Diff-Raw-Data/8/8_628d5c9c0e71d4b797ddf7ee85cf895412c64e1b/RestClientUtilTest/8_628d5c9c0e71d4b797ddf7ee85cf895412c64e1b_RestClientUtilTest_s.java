 package org.bluemagic.config.service.utils;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.junit.Test;
 
 public class RestClientUtilTest {
 
 	@Test
 	public void testGet() {
 		URI uri = null;
 		
 		try {
			uri = new URI("http://localhost:8080/property/some/prop?tags=production");
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		String rval = RestClientUtil.get(uri);
 		System.out.println(rval);
 	}
 	
 	@Test
 	public void testPost() {
 		String json = "{\"base\":\"some/prop\",\"tags\":[{\"tags\":\"new\"}]}";
 		
         URI uri = null;
 		
 		try {
			uri = new URI("http://localhost:8080/create/some/prop?tags=new");
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		RestClientUtil.post(uri, json);
 	}
 	
 	@Test
 	public void testDelete() {
         URI uri = null;
 		
 		try {
			uri = new URI("http://localhost:8080/delete/some/prop?tags=production");
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		RestClientUtil.delete(uri);
 	}
 }
