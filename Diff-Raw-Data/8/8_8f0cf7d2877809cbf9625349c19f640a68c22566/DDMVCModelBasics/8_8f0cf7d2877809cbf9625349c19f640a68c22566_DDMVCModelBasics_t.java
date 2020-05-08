 package com.google.gwt.ddmvc.test;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import com.google.gwt.ddmvc.DDMVC;
 import com.google.gwt.ddmvc.model.Model;
 import com.google.gwt.ddmvc.model.ModelDoesNotExistException;
 
 /**
  * Unit tests for the basic model methods of DDMVC 
  * @author Kevin Dolan
  */
 public class DDMVCModelBasics {
 	
 	@Before
 	public void setUp() {
 		DDMVC.reset();
 		DDMVC.setValue("frillo", "Hodgepodge");
 		DDMVC.setValue("something", "text");
 	}
 	
 	@Test
 	public void modelExistence() {
 		assertFalse(DDMVC.hasModel("anything"));
 		assertTrue(DDMVC.hasModel("frillo"));
 		assertTrue(DDMVC.hasModel("something"));
 		assertFalse(DDMVC.hasModel("anything"));
 	}
 	
 	@Test
 	public void modelValue() {
 		Model model = DDMVC.getModel("something");
 		assertTrue(DDMVC.getValue("something").equals("text"));
 		assertTrue(DDMVC.getModel("something").get().equals("text"));
 		assertTrue(model.get().equals("text"));
 	}
 	
 	@Test
 	public void modelSetMethod() {
 		Model model = DDMVC.getModel("something");
 		model.set("forty-five");
 		assertTrue(DDMVC.getValue("something").equals("forty-five"));
 		assertTrue(DDMVC.getModel("something").get().equals("forty-five"));
 		assertTrue(model.get().equals("forty-five"));
 	}
 	
 	@Test
 	public void setValueMethod() {
 		DDMVC.setValue("something", "fifty-four");
 		assertTrue(DDMVC.getValue("something").equals("fifty-four"));
 		assertTrue(DDMVC.getModel("something").get().equals("fifty-four"));
 	}
 	
 	@Test
 	public void modelDeleting() {
 		DDMVC.deleteModel("something");
 		assertTrue(DDMVC.hasModel("frillo"));
 		assertFalse(DDMVC.hasModel("something"));
 		assertFalse(DDMVC.hasModel("anything"));
 	}
 	
 	@Test
 	public void explicitSetModel() {
 		DDMVC.setModel("model", new Model("somethingElse", "fresh"));
 		assertTrue(DDMVC.getValue("model").equals("fresh"));
 		assertTrue(DDMVC.getModel("model").get().equals("fresh"));
 	}
 	
 	@Test
 	public void nonExistent() {
 		try {
 			DDMVC.getValue("hahaNotHere");
 			fail();
 		}
 		catch(ModelDoesNotExistException e) {}
 	}
 	
 }
