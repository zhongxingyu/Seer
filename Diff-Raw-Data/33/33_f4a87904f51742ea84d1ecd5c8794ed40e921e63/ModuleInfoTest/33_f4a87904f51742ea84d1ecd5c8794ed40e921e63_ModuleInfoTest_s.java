 package afc.ant.modular;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 
 import junit.framework.TestCase;
 
 public class ModuleInfoTest extends TestCase
 {
     public void testConstructWithNullPath()
     {
         try {
             new ModuleInfo(null);
             fail();
         }
         catch (NullPointerException ex) {
             assertEquals("path", ex.getMessage());
         }
     }
     
     public void testConstructWithEmptyPath()
     {
         final ModuleInfo m = new ModuleInfo("");
         
         assertSame("", m.getPath());
         assertEquals(Collections.emptySet(), m.getDependencies());
     }
     
     public void testConstructWithNonEmptyPath()
     {
         final ModuleInfo m = new ModuleInfo("a/b/c");
         
         assertSame("a/b/c", m.getPath());
         assertEquals(Collections.emptySet(), m.getDependencies());
     }
     
     public void testSetValidDependencies()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         final ArrayList<ModuleInfo> deps = new ArrayList<ModuleInfo>();
         deps.add(m2);
         deps.add(m3);
         m2.setDependencies(Collections.singleton(m3));
         
         final HashSet<ModuleInfo> expectedDeps = new HashSet<ModuleInfo>(Arrays.asList(m2, m3));
         m.setDependencies(deps);
         assertSame("foo", m.getPath());
         assertEquals(expectedDeps, m.getDependencies());
         assertEquals(Arrays.asList(m2, m3), deps);
         
         deps.clear();
         assertEquals(expectedDeps, m.getDependencies());
         
         // ensure deps of m2 and m3 are not modified
         assertEquals(Collections.singleton(m3), m2.getDependencies());
         assertEquals(Collections.emptySet(), m3.getDependencies());
     }
     
     public void testReSetValidDependencies()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         final ModuleInfo m4 = new ModuleInfo("quux");
 
         m.setDependencies(Arrays.asList(m2));
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2)), m.getDependencies());
         
         m.setDependencies(Arrays.asList(m3, m4));
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m3, m4)), m.getDependencies());
     }
     
     public void testTryAddDependencyViaGetter()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         
         m.setDependencies(Arrays.asList(m2, m3));
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
         
         try {
             m.getDependencies().add(new ModuleInfo("quux"));
             fail();
         }
         catch (RuntimeException ex) {
             // expected
         }
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
     }
     
     public void testTryAddNullDependencyViaGetter()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         
         m.setDependencies(Arrays.asList(m2, m3));
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
         
         try {
             m.getDependencies().add(null);
             fail();
         }
         catch (RuntimeException ex) {
             // expected
         }
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
     }
     
     public void testTryClearDependenciesViaGetter()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
 
         m.setDependencies(Arrays.asList(m2, m3));
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
         
         try {
             m.getDependencies().clear();
             fail();
         }
         catch (RuntimeException ex) {
             // expected
         }
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
     }
     
     public void testSetNullDependencies()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         
         m.setDependencies(Arrays.asList(m2, m3));
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
         
         try {
             m.setDependencies(null);
             fail();
         }
         catch (NullPointerException ex) {
             assertEquals("dependencies", ex.getMessage());
         }
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
     }
     
     public void testSetDependenciesWithNullElement()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         
         m.setDependencies(Arrays.asList(m2, m3));
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
         
         try {
             m.setDependencies(Arrays.asList(new ModuleInfo("quux"), null));
             fail();
         }
         catch (NullPointerException ex) {
             assertEquals("dependencies contains null dependency.", ex.getMessage());
         }
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
     }
     
     public void testAddDependency_NoOtherDependencies()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         
         m.addDependency(m2);
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2)), m.getDependencies());
     }
     
     public void testAddDependency_ToExistingDependencies()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         
         m.setDependencies(Arrays.asList(m2));
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2)), m.getDependencies());
         
         m.addDependency(m3);
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
     }
     
     public void testAddDependency_NullDependency()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         
         m.setDependencies(Collections.singletonList(m2));
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Collections.singletonList(m2)), m.getDependencies());
         
         try {
             m.addDependency(null);
             fail();
         }
         catch (NullPointerException ex) {
             assertEquals("dependency", ex.getMessage());
         }
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Collections.singletonList(m2)), m.getDependencies());
     }
     
     public void testAddDependency_AddItselfAsDependency()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("baz");
         
         m.setDependencies(Collections.singletonList(m2));
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Collections.singletonList(m2)), m.getDependencies());
         
         try {
             m.addDependency(m);
             fail();
         }
         catch (IllegalArgumentException ex) {
             assertEquals("Cannot add itself as a dependency.", ex.getMessage());
         }
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Collections.singletonList(m2)), m.getDependencies());
             
         m.addDependency(m3);
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
     }
     
     /**
      * <p>Test description: though discouraged using different instances of ModuleInfo with the same path is allowed.
      * Such instances must be treat as different modules.</p>
      */
     public void testAddDependency_AddModuleWithTheSamePathAsDependency()
     {
         final ModuleInfo m = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("foo");
         
         m.setDependencies(Collections.singletonList(m2));
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Collections.singletonList(m2)), m.getDependencies());
         
         m.addDependency(m3);
         assertSame("foo", m.getPath());
         assertEquals(new HashSet<ModuleInfo>(Arrays.asList(m2, m3)), m.getDependencies());
         assertEquals(Collections.emptySet(), m3.getDependencies());
     }
     
     /**
      * <p>Test description: though discouraged using different instances of ModuleInfo with the same path is allowed.
      * Such instances must be treat as different modules.</p>
      */
     public void testEquals()
     {
         final ModuleInfo m1 = new ModuleInfo("foo");
         final ModuleInfo m2 = new ModuleInfo("bar");
         final ModuleInfo m3 = new ModuleInfo("foo");
         
         assertFalse(m1.equals(null));
         assertFalse(m1.equals(new Object()));
         assertTrue(m1.equals(m1));
         assertFalse(m1.equals(m2));
         assertFalse(m1.equals(m3));
         
         m1.addDependency(m2);
         assertTrue(m1.equals(m1));
     }
 }
