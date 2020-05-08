 package org.webmacro.template;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.StringTemplate;
 import org.webmacro.engine.DefaultEvaluationExceptionHandler;
 
 import junit.framework.*;
 
 import org.apache.regexp.RE;
 
 public class TestDefaultEEH extends AbstractVariableTestCase {
 
    public TestDefaultEEH (String name) {
       super (name);
    }
 
    public void stuffContext (Context context) throws Exception {
       context.setEvaluationExceptionHandler (
                 new DefaultEvaluationExceptionHandler ());
 
       context.put ("TestObject", new TestObject());
       context.put ("NullTestObject", new NullTestObject());
       context.put ("NullObject", null);
    }
 
    public void testGoodVariable () throws Exception {
       assertStringTemplateEquals ("$TestObject", "TestObject");
    }
 
    public void testEvalGoodVariable () throws Exception {
       assertStringTemplateEquals("#set $foo=$TestObject", "");
       assertBooleanExpr("$foo == $TestObject", true);
    }
 
    public void testGoodMethod () throws Exception {
       assertStringTemplateEquals ("$TestObject.getString()", "String");
    }
 
    public void testEvalGoodMethod () throws Exception {
       assertStringTemplateEquals("#set $foo=$TestObject.getString()", "");
       assertStringTemplateEquals("$foo", "String");
    }
 
    public void testGoodProperty () throws Exception {
       assertStringTemplateEquals ("$TestObject.property", "Property");
    }
 
    public void testEvalGoodProperty () throws Exception {
       assertStringTemplateEquals("#set $foo = $TestObject.property", "");
       assertStringTemplateEquals("$foo", "Property");
    }
 
 
    public void testNoSuchVariable () throws Exception {
       assertStringTemplateMatches ("$NotInContext", "Attempted to write an undefined variable");
    }
 
    public void testEvalNoSuchVariable () throws Exception {
       assertStringTemplateEquals ("#set $foo=$NotInContext", "");
       assertBooleanExpr("$foo == undefined", true);
    }
 
    public void testNoSuchMethod () throws Exception {
       assertStringTemplateThrows ("$TestObject.noSuchMethod()",
         PropertyException.NoSuchMethodException.class);
 
    }
 
    public void testNoSuchMethodWithArguments () throws Exception {
       assertStringTemplateThrows ("$TestObject.toString('foo', false, 1)",
         PropertyException.NoSuchMethodWithArgumentsException.class);
    }
 
    public void testEvalNoSuchMethod () throws Exception {
       assertStringTemplateThrows("#set $foo=$TestObject.noSuchMethod()",
         PropertyException.NoSuchMethodException.class);
    }
 
    public void testEvalNoSuchMethodWithArguments () throws Exception {
       assertStringTemplateThrows ("#set $foo=$TestObject.toString('foo', false, 1)",
         PropertyException.NoSuchMethodWithArgumentsException.class);
    }
 
    public void testNoSuchProperty () throws Exception {
       assertStringTemplateThrows ("$TestObject.noSuchProperty",
                                    PropertyException.NoSuchPropertyException.class);
    }
 
    public void testEvalNoSuchProperty () throws Exception {
       assertStringTemplateThrows ("#set $foo=$TestObject.noSuchProperty",
         PropertyException.NoSuchPropertyException.class);
    }
 
 
    public void testVoidMethod () throws Exception {
       assertStringTemplateEquals ("$TestObject.voidMethod()", "");
    }
 
    public void testEvalVoidMethod () throws Exception {
       assertStringTemplateThrows ("#set $foo=$TestObject.voidMethod()",
                                   PropertyException.VoidValueException.class);
    }
 
    public void testNullMethod () throws Exception {
       assertStringTemplateMatches ("$TestObject.nullMethod()",
         "^<!--.*Value is null.*-->$");
    }
 
    public void testEvalNullMethod () throws Exception {
       assertStringTemplateMatches ("#set $foo=$TestObject.nullMethod()", "");
       assertBooleanExpr("$foo == null", true);
    }
 
    public void testThrowsMethod() throws Exception {
       assertStringTemplateThrows ("$TestObject.throwException()",
                                    org.webmacro.PropertyException.class);
    }
 
    public void testEvalThrowsMethod() throws Exception {
      assertStringTemplateThrows ("$set $foo=$TestObject.throwException()",
                                    org.webmacro.PropertyException.class);
    }
 
   // @@@ The behavior should probably be changed for this
    public void testNullVariable () throws Exception {
      assertStringTemplateMatches ("$NullObject",  "");
    }
 
    public void testEvalNullVariable () throws Exception {
       assertStringTemplateMatches ("#set $foo=$NullObject", "");
       assertBooleanExpr("$foo == null", true);
    }
 }
