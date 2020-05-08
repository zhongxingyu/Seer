 package au.net.netstorm.boost.nursery.autoedge;
 
 import java.io.InputStream;
 import java.lang.reflect.Method;
 
 import au.net.netstorm.boost.edge.java.lang.EdgeClass;
 import au.net.netstorm.boost.edge.java.lang.reflect.EdgeMethod;
 import au.net.netstorm.boost.nursery.autoedge.testfixtures.EdgeStreamFixture;
 import au.net.netstorm.boost.sniper.core.LifecycleTestCase;
 import au.net.netstorm.boost.sniper.marker.HasFixtures;
 import au.net.netstorm.boost.sniper.marker.InjectableSubject;
 import au.net.netstorm.boost.sniper.marker.InjectableTest;
 import au.net.netstorm.boost.sniper.marker.LazyFields;
 
// FIX 2328 need to test case where real object is null (static method case)

// FIX 2328 need to test what happens on failures - this is not the test for it, but need somewhere to leave a trail.
 public final class DefaultAutoEdgeAtomicTest extends LifecycleTestCase implements HasFixtures, InjectableTest, InjectableSubject, LazyFields {
     private AutoEdge subject;
     private Method unedge;
     private Method toString;
     EdgeStreamFixture fixture;
 
     MethodWarp warperMock;
     Unedger unedgerMock;
     EdgeMethod invokerMock;
     ReturnEdger returnEdgerMock;
 
     EdgeClass classer;
 
     public void setUpFixtures() {
         subject = new DefaultAutoEdge(InputStream.class, fixture.stream());
         unedge = classer.getDeclaredMethod(Edge.class, "unedge");
         toString = classer.getDeclaredMethod(Object.class, "toString");
     }
 
     public void testInvoke() {
         byte[] result = new byte[fixture.length()];
         Object[] args = {result};
         expectations(fixture.edge(), fixture.real(), fixture.length(), args);
         Object length = subject.invoke(fixture.stream(), fixture.edge(), args);
         assertEquals(fixture.length(), length);
     }
 
     public void testInvokeNoArgs() {
         String expected = "result";
         expectations(toString, toString, expected, null);
         Object result = subject.invoke(fixture.stream(), toString, null);
         assertEquals(expected, result);
     }
 
     public void testInvokeUnedge() {
         Object[] args = {};
         Object result = subject.invoke(null, unedge, args);
         assertEquals(true, result instanceof InputStream);
         assertSame(fixture.stream(), result);
     }
 
     private void expectations(Method src, Method trg, Object expected, Object args) {
         expect.oneCall(warperMock, trg, "warp", InputStream.class, src);
         expect.oneCall(unedgerMock, args, "unedge", args);
         expect.oneCall(invokerMock, expected, "invoke", trg, fixture.stream(), args);
         expect.oneCall(returnEdgerMock, expected, "edge", src, expected);
     }
 }
