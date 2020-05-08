 package html2windows.dom;
 
 import html2windows.dom.*;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.mockito.Mockito.*;
 
 import java.util.*;
 
 public class ElementTest{
 	Document document;
 	Element element;
 
 	@Before
 	public void before(){
 		document = mock(Document.class);
 		element = new ElementInter("div");
 	}
 	
 	@Test
 	public void appendChildNodes(){
 		ArrayList<Node> children = new ArrayList<Node>();
 		
 		NodeInter childNodeElement = mock(NodeInter.class);
 		children.add(childNodeElement);
 		children.add(document.createTextNode("text"));
 		
 		for(Node child : children){
 			element.appendChild(child);
 		}
 		
 		NodeList childNodes = element.childNodes();
 		for(int i = 0; i < children.size(); i++){
			assertEquals(childNodes.get(i), children.get(i));
 		}
 	}
 
 	@Test
 	public void attribute(){
 		HashMap<String, String> attr = new HashMap<String, String>();
 		attr.put("id", "container");
 		attr.put("class", "red-box");
 
 		assertNull("Not existing name should return null", element.getAttribute("id"));
 
 		assertNull("Pass null should return null", element.getAttribute(null));
 
 		for(String name : attr.keySet()){
 			String value = attr.get(name);
 			element.setAttribute(name, value);
			assertEquals("'" + name + "' should get value '" + value + "'", element.getAttribute(name), value);
 		}
 
 		for(String name : attr.keySet()){
 			String value = attr.get(name);
 			element.removeAttribute(name);
 			assertNull("'" + name + "' should get null", element.getAttribute(name));
 		}
 	}
 }
