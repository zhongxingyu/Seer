 package au.net.netstorm.boost.util.reflect;
 
 import java.lang.reflect.Method;
 
 import junit.framework.Assert;
 
 public final class DefaultModifierTestChecker implements ModifierTestChecker {
     private static final String[] EXCLUSIONS = {"hashCode", "getClass", "equals", "toString", "wait", "notify", "notifyAll"};
    private final ClassMaster classMaster = new DefaultClassMaster();
     private final ModifierTestUtil modifier = new DefaultModifierTestUtil();
 
     public void checkSynchronized(Method method) {
         boolean isExclusion = isExclusion(method);
         if (isExclusion) return;
         boolean isSynchronized = modifier.isSynchronized(method);
         Assert.assertTrue("" + method, isSynchronized);
     }
 
     public void checkPublic(Class cls) {
         boolean isPublic = modifier.isPublic(cls);
         Assert.assertTrue(getName(cls), isPublic);
     }
 
     public void checkFinal(Class cls) {
         boolean isFinal = modifier.isFinal(cls);
         // FIXME: SC042 assertTrue into helper method.
         Assert.assertTrue(getName(cls), isFinal);
     }
 
     // FIXME: SC042 Tidy the section below up.
 
     // FIXME: SC042 Expose via interface.
     // FIXME: SC042 Merge with existing functionality.
     // FIXME: SC042 Given the current state of affairs, this looks like it belongs in ClassPropertiesTestUtil.
     public void checkSynchronized(Class cls) {
         Method[] methods = cls.getMethods();
         for (int i = 0; i < methods.length; i++) {
             checkSynchronized(methods[i]);
         }
     }
 
     private boolean isExclusion(String methodName) {
         for (int i = 0; i < EXCLUSIONS.length; i++) {
             if (methodName.equals(EXCLUSIONS[i])) return true;
         }
         return false;
     }
 
     private boolean isExclusion(Method method) {
         String name = method.getName();
         return isExclusion(name);
     }
 
     private String getName(Class cls) {
         // FIXME: SC042 Fieldize.
        return classMaster.getShortName(cls);
     }
 }
