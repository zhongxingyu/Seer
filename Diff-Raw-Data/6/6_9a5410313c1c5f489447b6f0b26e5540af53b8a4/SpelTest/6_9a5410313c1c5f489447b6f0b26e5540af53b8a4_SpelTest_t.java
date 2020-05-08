 package com.twoqubed.sjug.spel;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.expression.Expression;
 import org.springframework.expression.ExpressionParser;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.expression.spel.support.StandardEvaluationContext;
 
import static org.junit.Assert.*;
 
 public class SpelTest {
 
     private ExpressionParser parser;
     private StandardEvaluationContext context;
 
     @Before
     public void setUp() {
         parser = new SpelExpressionParser();
         context = new StandardEvaluationContext();
     }
 
     @Test
     public void testPublicMethodAccess() {
         Expression exp = parser.parseExpression("'Hello World'.bytes.length");
         int length = (Integer) exp.getValue();
 
         assertEquals(11, length);
     }
 
     @Test
     public void testPropertyAccessOfRootObject() {
         SpelBean bean = new SpelBean();
         bean.setName("foo");
         context.setRootObject(bean);
         
 
         Expression exp = parser.parseExpression("name");
         String name= (String) exp.getValue(bean);
 
         assertEquals("foo", name);
     }
 }
