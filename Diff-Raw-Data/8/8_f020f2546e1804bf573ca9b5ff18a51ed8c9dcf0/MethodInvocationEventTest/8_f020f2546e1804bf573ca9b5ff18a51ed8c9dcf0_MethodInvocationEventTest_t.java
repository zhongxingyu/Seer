 package org.polyforms.event.bus.support;
 
 import java.lang.reflect.Method;
import java.util.Arrays;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class MethodInvocationEventTest {
     @Test
     public void newInstance() throws NoSuchMethodException {
         final Class<? extends MethodInvocationEventTest> targetClass = this.getClass();
         final Method method = targetClass.getMethod("mockMethod", new Class<?>[0]);
         final Object[] arguments = new Object[0];
         final MethodInvocationEvent event = new MethodInvocationEvent("name", targetClass, method, arguments);
 
        Assert.assertEquals(targetClass, event.getTargetClass());
        Assert.assertEquals(method, event.getMethod());
        Assert.assertTrue(Arrays.deepEquals(arguments, event.getArguments()));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void newInstanceWithNullTargetClass() throws NoSuchMethodException {
         new MethodInvocationEvent("name", null, this.getClass().getMethod("mockMethod", new Class<?>[0]), new Object[0]);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void newInstanceWithNullMethod() throws NoSuchMethodException {
         new MethodInvocationEvent("name", this.getClass(), null, new Object[0]);
     }
 
     public void mockMethod() {
     }
 }
