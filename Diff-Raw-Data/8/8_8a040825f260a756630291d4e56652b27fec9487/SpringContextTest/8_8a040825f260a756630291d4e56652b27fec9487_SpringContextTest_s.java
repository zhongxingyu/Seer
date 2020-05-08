 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.spring;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import ru.histone.Histone;
 import ru.histone.HistoneBuilder;
 import ru.histone.HistoneException;
 
 import java.io.IOException;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertSame;
 
 /**
  * Test Histone initialization via spring context file
  */
 @Ignore
 public class SpringContextTest {
 
     private ObjectMapper jackson;
     private Histone histone;
     private ClassPathXmlApplicationContext context;
     private HistoneBuilder histoneBuilder;
 
     @Before
     public void before() throws HistoneException {
         String[] configLocations = {"classpath:/spring-context.xml"};
         context = new ClassPathXmlApplicationContext(configLocations);
         histoneBuilder = context.getBean("histoneBuilder", HistoneBuilder.class);
         jackson = context.getBean("jackson", ObjectMapper.class);
         histone = histoneBuilder.build();
 
         assertNotNull(context);
         assertNotNull(histone);
 

         histone = context.getBean("histone", Histone.class);
         assertNotNull(histone);
     }
 
     /**
      * Check that Histone bean won't be the same for subsequent calls
      * @throws ru.histone.HistoneException
      */
     @Test
     public void testContext() throws HistoneException {
         Histone histone1 = context.getBean(Histone.class);
         Histone histone2 = context.getBean(Histone.class);
 
         assertNotNull(histone1);
         assertNotNull(histone2);
         assertSame(histone1, histone2);
     }
 
     @Test
     public void general() throws HistoneException, IOException {
         String input = "a {{true}} b {{x}} c";
        JsonNode context = jackson.readTree("{'x':123}");
         String output = histone.evaluate(input, context);
         assertEquals("a true b 123 c", output);
     }
 
     @Test
     public void globalFunctions() throws HistoneException {
         String input = "a {{globalA()}} b {{globalB()}} c";
         String output = histone.evaluate(input);
         assertEquals("a global_a_result b global_b_result c", output);
     }
 
     @Test
     public void numberNodeFunctions() throws HistoneException {
         String input = "a {{123.numberFunctionA()}} b {{123.numberFunctionB()}} c";
         String output = histone.evaluate(input);
         assertEquals("a number_a_result b number_b_result c", output);
     }
 
     @Test
     public void stringNodeFunctions() throws HistoneException {
         String input = "a {{'abc'.stringFunctionA()}} b {{'abc'.stringFunctionB()}} c";
         String output = histone.evaluate(input);
         assertEquals("a string_a_result b string_b_result c", output);
     }
 
     @Test
     public void resolvers() throws HistoneException {
         String input = "a {{loadText('dummyA:/text.txt')}} b {{loadText('dummyB:/text.txt')}} c";
         String output = histone.evaluate(input);
         assertEquals("a resolver_a_result b resolver_b_result c", output);
     }

}
