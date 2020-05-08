 package com.liqing;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 /**
  * User: LiQing Date: 8/12/13 Time: 10:15 PM
  */
 @Component
 public class Person
 {
	@Value("${person.name}")
 	private String name;
	@Autowired
 	private Dog dog;
 
 	public Person()
 	{
 		this.name = "";
 		this.dog = null;
 	}
 
 	public Person(String name, Dog dog)
 	{
 		this.name = name;
 		this.dog = dog;
 	}
 
 	public void setDog(Dog dog)
 	{
 		this.dog = dog;
 	}
 
 	public void setName(final String name)
 	{
 		this.name = name;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public String playDog()
 	{
 		return dog.bark(name);
 	}
 }
