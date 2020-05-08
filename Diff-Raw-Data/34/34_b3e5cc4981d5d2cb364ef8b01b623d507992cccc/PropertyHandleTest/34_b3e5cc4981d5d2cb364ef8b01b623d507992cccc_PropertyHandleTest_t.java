 package me.bukp.uioc.test;
 
 import static org.junit.Assert.*;
 
import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.bukp.uioc.context.BeanCreator;
 import me.bukp.uioc.context.PropertyHandler;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PropertyHandleTest {
 
 	private final String classname = "me.bukp.uioc.test.User";
 	
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void setterTest() {
 		User user = (User)BeanCreator.createBean(classname);
 		Map<String, Object> properties = new HashMap<>();
 		properties.put("id", 1);
 		properties.put("name", "lorin");	
 		PropertyHandler.setProperties(user, properties);
 		assertTrue(user.getId().equals(1));
 		assertEquals("lorin", user.getName());
 		System.out.println(user);
 	}
	
	@Test
	public void getSetterTest() {
		User user = (User)BeanCreator.createBean(classname);
		Map<String, Method> setterMap = PropertyHandler.getSetterMethods(user);
		for (String methodName : setterMap.keySet()) {
			System.out.println(methodName);
			System.out.println(setterMap.get(methodName));
		}
	}
	
	@Test
	public void executeSetterTest() {
		Object obj = BeanCreator.createBean(classname);
		Object bean = "lorin";
		Map<String, Method> setterMap = PropertyHandler.getSetterMethods(obj);
		Method method = setterMap.get("name");
		PropertyHandler.executeSetterMethod(obj, bean, method);
		
		assertEquals("lorin", ((User)obj).getName());
	}
 }
