 package au.net.netstorm.boost.test.cases;
 
 import au.net.netstorm.boost.retire.reflect.AssertTestChecker;
 import au.net.netstorm.boost.retire.reflect.DefaultAssertTestChecker;
 import junit.framework.TestCase;
 
 // SUGGEST Remove the need for this altogether.
 // SUGGEST Check bottom level classes are final.
 // SUGGEST Check no-arg (single) constructor.
 
 // OK GenericIllegalRegexp {
 public abstract class BoooostCase extends TestCase {
     // } OK GenericIllegalRegexp
     private final AssertTestChecker assertTestChecker = new DefaultAssertTestChecker();
 
     protected final void setUp() throws Exception {
         super.setUp();
         gearup();
     }
 
     protected final void tearDown() throws Exception {
         geardown();
         super.tearDown();
     }
 
     protected void gearup() {
     }
 
     protected void geardown() {
     }
 
     public final void assertEquals(Object[] expected, Object[] actual) {
         assertTestChecker.checkEquals(expected, actual);
     }
 
     public final void assertBagEquals(Object[] expected, Object[] actual) {
         assertTestChecker.checkBagEquals(expected, actual);
     }
 
     public final void assertEquals(byte[] expected, byte[] actual) {
         assertTestChecker.checkEquals(expected, actual);
     }
 
     public final void assertEquals(int[] expected, int[] actual) {
         assertTestChecker.checkEquals(expected, actual);
     }
 
     public final void assertNotEquals(byte[] v1, byte[] v2) {
         assertTestChecker.checkNotEquals(v1, v2);
     }
 
     public final void assertNotEquals(Object v1, Object v2) {
         assertEquals(false, v1.equals(v2));
     }
 
     public final void assertNotEquals(int v1, int v2) {
         assertEquals(false, v1 == v2);
     }
 
     public static final void assertTrue(boolean expected) {
         suffer();
     }
 
     public static final void assertTrue(String msg, boolean expected) {
         suffer();
     }
 
     public static final void assertFalse(boolean expected) {
         suffer();
     }
 
     public static final void assertFalse(String msg, boolean expected) {
         suffer();
     }
 
     // OK LineLength {
     private static void suffer() {
         throw new UnsupportedOperationException("Use assertEquals(true|false, expected) ... assertTrue/assertFalse precludes refactoring opportunities (_x_)");
     }
     // } OK LineLength - Abusing others is fine if they are doing the wrong thing ;-)
 }
