 package com.maxifier.guice.bootstrap.groovy;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.name.Names;
 import com.magenta.guice.bootstrap.xml.*;
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 
 @Test
 public class GroovyModuleTest {
 
     @DataProvider(name = "files")
     public Object[][] fileNames() {
         return new Object[][]{
                 {"groovy/test.groovy"}
         };
     }
 
     @Test(dataProvider = "files")
     public void testGroovyModule(String fileName) {
         InputStream resourceAsStream = GroovyModuleTest.class.getClassLoader().getResourceAsStream(fileName);
         GroovyShell shell = new GroovyShell();
         shell.setProperty("client", "forbes");
         GroovyModule gModule = new GroovyModule(resourceAsStream, shell);
         Injector inj = Guice.createInjector(gModule);
         //from FooModule
         inj.getInstance(Foo.class);
         //just component
         assertTrue(inj.getInstance(TestInterface.class) instanceof First);
         //just component with annotation
         assertTrue(inj.getInstance(Key.get(TestInterface.class, TestAnnotation.class)) instanceof Second);
         //test constant
         inj.getInstance(Key.get(String.class, Constant.class));
         //test alone
         inj.getInstance(Alone.class);
         //test in SINGLETON scope
         In in1 = inj.getInstance(In.class);
         In in2 = inj.getInstance(In.class);
         assertTrue(in1 instanceof InImpl);
         assertTrue(in2 instanceof InImpl);
         assertTrue(in1 == in2);
 
         assertEquals(((InImpl) in1).getProperty(), "testValue");
         assertEquals(((InImpl) in1).getWeight(), 523.23);
 
         //test asEager
         inj.getInstance(AsEager.class);
         //test constant
         inj.getInstance(Key.get(String.class, Names.named("test.name")));
     }
 
 }
