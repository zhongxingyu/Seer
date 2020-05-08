 package net.davekieras.model.impl;
 
 import javax.enterprise.context.Dependent;
 
 import net.davekieras.model.TestBean;
 
//@Dependent  //Default value if not specified, not a Singleton like Spring
 public class TestBeanImpl implements TestBean {
 
 	private String name = "Test";
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	
 }
