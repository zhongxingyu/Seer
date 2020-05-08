 package org.webmacro.template;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.StringTemplate;
 import org.webmacro.engine.CrankyEvaluationExceptionHandler;
 import java.util.Enumeration;
 import java.util.NoSuchElementException;
 
 import junit.framework.*;
 
 import org.apache.regexp.RE;
 
 /**
  * test case for the CrankyEEH.  Each ROGUE test should throw
  */
 public class TestCrankyEEH extends AbstractVariableTestCase {
 
    public TestCrankyEEH (String name) {
       super (name);
    }
 
    public void stuffContext (Context context) throws Exception {
       context.setEvaluationExceptionHandler (
                 new CrankyEvaluationExceptionHandler(_wm.getBroker()));
 
       context.put ("TestObject", new TestObject());
       context.put ("NullTestObject", new NullTestObject());
       context.put("enum",new ThrowingEnumeration());
 
    }
 
    public void testGoodVariable () throws Exception {
       assertStringTemplateEquals ("$TestObject", "TestObject");
    }
 
    public void testEvalGoodVariable () throws Exception {
       assertStringTemplateEquals("#set $foo=$TestObject", "");
       assertBooleanExpr("$foo == $TestObject", true);
       assertStringTemplateEquals("$foo", "TestObject");
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
       assertStringTemplateThrows ("$NotInContext",
         PropertyException.UndefinedVariableException.class);
    }
 
    public void testEvalNoSuchVariable () throws Exception {
       assertStringTemplateEquals ("#set $foo=$NotInContext", "");
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
       assertStringTemplateThrows ("$TestObject.nullMethod()",
                                    PropertyException.NullValueException.class);
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
 
    /** A variable in the context who's .toString() method returns null
     */
    public void testNullVariable () throws Exception {
       assertStringTemplateThrows ("$NullTestObject",
          PropertyException.NullToStringException.class);
    }
 
    public void testEvalNullVariable () throws Exception {
       assertStringTemplateEquals ("#set $foo=$NullTestObject", "");
       assertBooleanExpr("$foo == $NullTestObject", true);
    }
 
    /*
     * test cases designed to check that the caught exception is recorded
     */
    public void testThrowsWithCaught () throws Exception {
       assertStringTemplateThrowsWithCaught ("$enum.nextElement()",
                                    java.util.NoSuchElementException.class);
    }
 
    public void testThrowsWithCaughtInForeach () throws Exception {
       assertStringTemplateThrowsWithCaught ("#foreach $a in $enum #begin #end",
                                    java.util.NoSuchElementException.class);
    }
 
   /*
    * this is clearly a silly class, bit it is designed to prove that WebMacro
    * properly fills in the caught exception at all times
    *
    * in WM 0.97, an exception that occured when the nextElement() in a #foreach
    * loop was called.  ie when the object was constructed instead of
    * accessed, causes a PropertyException to be Thrown without filling in the
    * caught exception.
    */
 
   public class ThrowingEnumeration implements Enumeration {
       public boolean hasMoreElements() {
           return true;
       }
       public Object nextElement() throws NoSuchElementException {
           throw new NoSuchElementException();
       }
   }
 
  }
