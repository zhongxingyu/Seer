 package au.net.netstorm.boost.test.fixture;
 
 import java.lang.reflect.Constructor;
 
 import au.net.netstorm.boost.primordial.Primordial;
 import au.net.netstorm.boost.test.checker.ClassTestChecker;
 import au.net.netstorm.boost.test.checker.DefaultClassTestChecker;
 import au.net.netstorm.boost.test.checker.NullParameterTestChecker;
 import au.net.netstorm.boost.util.introspect.FieldSpec;
 import junit.framework.Assert;
 
 // FIXME: SC502 Does it make sense to have this fixtures area?
 
 final class ClassTestFixture {
     private final InstanceTestUtil instancer = new DefaultInstanceTestUtil();
     private final ClassTestChecker clsChecker = new DefaultClassTestChecker();
     private final NullParameterTestChecker nuller = new NullParameterTestChecker();
     private final Class cls;
     private final FieldSpec[] parameters;
 
     public ClassTestFixture(Class cls, FieldSpec[] parameters) {
         this.cls = cls;
         this.parameters = parameters;
     }
 
     // FIXME: S502 ? Allow the interfaces to check to be changes Data vs Immutable.
     public void checkClass(Class targetInterface, InstanceProviderForTest additional) {
         clsChecker.checkSubclassOf(Primordial.class, cls);
         clsChecker.checkImplementsAndFinal(targetInterface, cls);
         checkConstructor(additional);
     }
 
     private void checkConstructor(InstanceProviderForTest additional) {
         // FIXME: SC050 ... So the BUG smell here is that we do not need to pass the constructor all the way through to IPTU via NTU.
         // FIXME: SC050 ... Tidying this up will remove a large amount of code.
         Constructor constructor = instancer.getConstructor(cls);
        Class[] expected = constructor.getParameterTypes();
        Class[] params = instancer.getClasses(parameters);
        nuller.checkNullConstructorParameters(constructor, params, additional);
        Assert.assertEquals("Class constructor does not have expected number arguments", expected.length, params.length); // FIXME: SC050 This seems to be back to front
     }
 }
