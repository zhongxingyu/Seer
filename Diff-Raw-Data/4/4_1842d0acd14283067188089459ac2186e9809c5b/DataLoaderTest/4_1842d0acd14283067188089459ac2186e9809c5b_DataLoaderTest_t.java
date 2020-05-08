 package org.sankozi.rogueland.data;
 
 import java.util.Collection;
 import java.util.Map;
 import org.junit.Test;
 import org.sankozi.rogueland.model.ItemTemplate;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 /**
  *
  * @author sankozi
  */
 public class DataLoaderTest {
 	
 	public DataLoaderTest() {
 	}
 
 	@Test
 	public void testGetScriptNames() {
 		Collection<String> scriptNames = new DataLoader().getScriptNames();
        assertThat(scriptNames, contains("items.cl"));
 	}
 
 	@Test
 	public void testLoadResource(){
 		String resource = new DataLoader().loadResource("items.cl");
 		assert resource.length() > 0: "resource not empty";
 	}
 
 	@Test
 	public void testItemsResource(){
 		Object res = new DataLoader().evaluateClResource("items.cl");
         assertThat(res, instanceOf(Map.class));
 	}
 
     @Test
     public void loadItemTemplates(){
         Map<String, ItemTemplate> templates = new DataLoader().loadItemTemplates();
         assertThat(templates, hasKey("test-item"));
     }
 }
