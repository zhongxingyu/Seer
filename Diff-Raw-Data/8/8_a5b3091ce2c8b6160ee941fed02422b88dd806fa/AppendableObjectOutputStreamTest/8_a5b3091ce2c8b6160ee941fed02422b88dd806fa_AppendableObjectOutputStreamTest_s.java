 package com.github.schali.util.io;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import junit.framework.Assert;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class AppendableObjectOutputStreamTest {
 
 	public static class Person implements Serializable{
 		private static final long serialVersionUID = 1L;
 		String name;
 		Integer age;
 		public Person(String name, Integer age) {
 			super();
 			this.name = name;
 			this.age = age;
 		}
 	}
 	
 	private static File outputfile;
 
 	private static int contentsCount = 10;
 	private static Person[] contents = new Person[contentsCount];
 	
 	@BeforeClass
 	public static void beforeClass() throws Throwable {
 		outputfile = File.createTempFile("test", "objects");
 		
 		for(int i=0; i<contentsCount; i++) {
 			Person p = new Person("test name " + i, 20+i);
 			contents[i] = p;
 		}
 	}
 	
 	@Test
 	public void testWriteObjects() throws Throwable {
 		ObjectOutputStream out = AppendableObjectOutputStream.createObjectOutputStreamForFile(outputfile);
 		for(int i=0; i<contentsCount; i++) {
 			out.writeObject(contents[i]);
 		}
 		out.flush();
 		out.close();
 		
 		out = AppendableObjectOutputStream.createObjectOutputStreamForFile(outputfile);
 		for(int i=0; i<contentsCount; i++) {
 			out.writeObject(contents[i]);
 		}
 		out.flush();
 		out.close();
 	}
 	
 	@Test 
 	public void testReadObjects() throws Throwable {
 		ObjectInputStream in = new ObjectInputStream(new FileInputStream(outputfile));
		
 		int i = 0;
 		
 		Object o = in.readObject();
 		while(o != null) {
 			Person p = (Person) o;
 			Person pReference = contents[i++ % contentsCount];
 			
 			Assert.assertEquals(pReference.name, p.name);
 			Assert.assertEquals(pReference.age, p.age);
 
 			try {
 				o = in.readObject();
 			}catch (EOFException ex) {
 				in.close();
 				break;
 			}
 		}
 		
 		Assert.assertEquals(contentsCount*2, i);
 	}
 	
 	@AfterClass
 	public static void afterClass() {
 		outputfile.delete();
 	}
 	
 }
