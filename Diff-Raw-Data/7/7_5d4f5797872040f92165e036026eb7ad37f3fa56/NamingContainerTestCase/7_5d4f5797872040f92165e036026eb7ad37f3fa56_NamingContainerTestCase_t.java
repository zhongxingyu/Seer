 /*
 * $Id: NamingContainerTestCase.java,v 1.3 2003/11/04 01:22:38 craigmcc Exp $
  */
 
 /*
  * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
  * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package javax.faces.component;
 
 
 import junit.framework.TestCase;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 
 /**
  * <p>Unit tests for the {@link NamingContainer} functionality of all the
  * standard component classes.</p>
  */
 
 public class NamingContainerTestCase extends TestCase {
 
 
     // ------------------------------------------------------ Instance Variables
 
 
     // The root of the component tree to be tested
     private UIViewRoot root = null;
 
 
     // ------------------------------------------------------------ Constructors
 
 
     // Construct a new instance of this test case.
     public NamingContainerTestCase(String name) {
         super(name);
     }
 
 
     // ---------------------------------------------------- Overall Test Methods
 
 
     // Set up instance variables required by this test case.
     public void setUp() {
 
         root = new UIViewRoot();
 
     }
 
 
     // Return the tests included in this test case.
     public static Test suite() {
 
         return (new TestSuite(NamingContainerTestCase.class));
 
     }
 
 
     // Tear down instance variables required by this test case.
     public void tearDown() {
 
         root = null;
 
     }
 
 
     // ------------------------------------------------- Individual Test Methods
 
 
     // Test nested NamingContainer callbacks
     public void testNested() {
 
         TestNamingContainer a = new TestNamingContainer(); a.setId("a");
         TestNamingContainer b = new TestNamingContainer(); b.setId("b");
         TestNamingContainer d = new TestNamingContainer(); d.setId("d");
         UIPanel e = new UIPanel(); e.setId("e");
         UIPanel g = new UIPanel(); g.setId("g");
         a.getChildren().add(b);
         b.getChildren().add(d);
         b.getChildren().add(g);
         d.getChildren().add(e);
 
         TestNamingContainer.trace(null);
         assertTrue(a == a.findComponent("a"));
         assertEquals("/a/a", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(a == a.findComponent(":a"));
         assertEquals("/a/:a", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(b == a.findComponent("b"));
         assertEquals("/a/b", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(b == a.findComponent(":b"));
         assertEquals("/a/:b", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(d == a.findComponent("b:d"));
         assertEquals("/a/b:d/b/d", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(d == a.findComponent(":b:d"));
         assertEquals("/a/:b:d/b/d", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(e == a.findComponent("b:d:e"));
         assertEquals("/a/b:d:e/b/d:e/d/e", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(e == a.findComponent(":b:d:e"));
         assertEquals("/a/:b:d:e/b/d:e/d/e", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(g == a.findComponent("b:g"));
         assertEquals("/a/b:g/b/g", TestNamingContainer.trace());
 
         TestNamingContainer.trace(null);
         assertTrue(g == a.findComponent(":b:g"));
         assertEquals("/a/:b:g/b/g", TestNamingContainer.trace());
 
     }
 
 
     // Test standard NamingContainer functionality
     public void testStandard() {
 
         // Set up a component hierarchy as follows (component ids in quotes):
         // "a" - UIViewRoot at head of hierarchy
         // "a" has children "b" and "c"
         // "b" has children "d" and "g"
         // "d" has children "e" and "f"
         // "c" has children "h" and "i"
         // Components "b" and "d" implement NamingContainer
         UIViewRoot a = (UIViewRoot) root; a.setId("a");
         UIForm b = new UIForm(); b.setId("b");
         UIPanel c = new UIPanel(); c.setId("c");
         UINamingContainer d = new UINamingContainer(); d.setId("d");
         UIPanel e = new UIPanel(); e.setId("e");
         UIPanel f = new UIPanel(); f.setId("f");
         UIPanel g = new UIPanel(); g.setId("g");
         UIPanel h = new UIPanel(); h.setId("h");
         UIPanel i = new UIPanel(); i.setId("i");
         a.getChildren().add(b);
         a.getChildren().add(c);
         b.getChildren().add(d);
         b.getChildren().add(g);
         c.getChildren().add(h);
         c.getChildren().add(i);
         d.getChildren().add(e);
         d.getChildren().add(f);
 
         // Positive relative searches from "a"
         assertTrue(a == a.findComponent("a"));
         assertTrue(b == a.findComponent("b"));
         assertTrue(c == a.findComponent("c"));
         assertTrue(d == a.findComponent("b:d"));
         assertTrue(e == a.findComponent("b:d:e"));
         assertTrue(f == a.findComponent("b:d:f"));
         assertTrue(g == a.findComponent("b:g"));
         assertTrue(h == a.findComponent("h"));
         assertTrue(i == a.findComponent("i"));
 
         // Negative relative searches from "a"
         assertNull(a.findComponent("d"));
         assertNull(a.findComponent("e"));
         assertNull(a.findComponent("f"));
         assertNull(a.findComponent("g"));
 
         // Positive relative searches from "b"
         assertTrue(b == b.findComponent("b"));
         assertTrue(d == b.findComponent("d"));
         assertTrue(e == b.findComponent("d:e"));
         assertTrue(f == b.findComponent("d:f"));
         assertTrue(g == b.findComponent("g"));
 
         // Negative relative searches from "b"
         assertNull(b.findComponent("a"));
         assertNull(b.findComponent("c"));
         assertNull(b.findComponent("e"));
         assertNull(b.findComponent("f"));
         assertNull(b.findComponent("h"));
         assertNull(b.findComponent("i"));
 
         // Positive relative searches from "c"
         assertTrue(a == c.findComponent("a"));
         assertTrue(b == c.findComponent("b"));
         assertTrue(c == c.findComponent("c"));
         assertTrue(d == c.findComponent("b:d"));
         assertTrue(e == c.findComponent("b:d:e"));
         assertTrue(f == c.findComponent("b:d:f"));
         assertTrue(g == c.findComponent("b:g"));
         assertTrue(h == c.findComponent("h"));
         assertTrue(i == c.findComponent("i"));
 
         // Negative relative searches from "c"
         assertNull(c.findComponent("d"));
         assertNull(c.findComponent("e"));
         assertNull(c.findComponent("f"));
         assertNull(c.findComponent("g"));
 
         // Positive relative searches from "d"
         assertTrue(d == d.findComponent("d"));
        assertTrue(e == d.findComponent("e"));
        assertTrue(f == d.findComponent("f"));
 
         // Negative relative searches from "d"
         assertNull(d.findComponent("a"));
         assertNull(d.findComponent("b"));
         assertNull(d.findComponent("c"));
         assertNull(d.findComponent("g"));
         assertNull(d.findComponent("h"));
         assertNull(d.findComponent("i"));
 
         // Positive relative searches from "e"
         assertTrue(d == e.findComponent("d"));
         assertTrue(e == e.findComponent("e"));
         assertTrue(f == e.findComponent("f"));
 
         // Negative relative searches from "e"
         assertNull(e.findComponent("a"));
         assertNull(e.findComponent("b"));
         assertNull(e.findComponent("c"));
         assertNull(e.findComponent("g"));
         assertNull(e.findComponent("h"));
         assertNull(e.findComponent("i"));
 
         // Positive relative searches from "f"
         assertTrue(d == f.findComponent("d"));
         assertTrue(e == f.findComponent("e"));
         assertTrue(f == f.findComponent("f"));
 
         // Negative relative searches from "f"
         assertNull(f.findComponent("a"));
         assertNull(f.findComponent("b"));
         assertNull(f.findComponent("c"));
         assertNull(f.findComponent("g"));
         assertNull(f.findComponent("h"));
         assertNull(f.findComponent("i"));
 
         // Positive relative searches from "g"
         assertTrue(b == g.findComponent("b"));
         assertTrue(d == g.findComponent("d"));
         assertTrue(e == g.findComponent("d:e"));
         assertTrue(f == g.findComponent("d:f"));
         assertTrue(g == g.findComponent("g"));
 
         // Negative relative searches from "g"
         assertNull(g.findComponent("a"));
         assertNull(g.findComponent("c"));
         assertNull(g.findComponent("e"));
         assertNull(g.findComponent("f"));
         assertNull(g.findComponent("h"));
         assertNull(g.findComponent("i"));
 
         // Positive relative searches from "h"
         assertTrue(a == h.findComponent("a"));
         assertTrue(b == h.findComponent("b"));
         assertTrue(c == h.findComponent("c"));
         assertTrue(d == h.findComponent("b:d"));
         assertTrue(e == h.findComponent("b:d:e"));
         assertTrue(f == h.findComponent("b:d:f"));
         assertTrue(g == h.findComponent("b:g"));
         assertTrue(h == h.findComponent("h"));
         assertTrue(i == h.findComponent("i"));
 
         // Negative relative searches from "h"
         assertNull(h.findComponent("d"));
         assertNull(h.findComponent("e"));
         assertNull(h.findComponent("f"));
         assertNull(h.findComponent("g"));
 
         // Positive relative searches from "i"
         assertTrue(a == i.findComponent("a"));
         assertTrue(b == i.findComponent("b"));
         assertTrue(c == i.findComponent("c"));
         assertTrue(d == i.findComponent("b:d"));
         assertTrue(e == i.findComponent("b:d:e"));
         assertTrue(f == i.findComponent("b:d:f"));
         assertTrue(g == i.findComponent("b:g"));
         assertTrue(h == i.findComponent("h"));
         assertTrue(i == i.findComponent("i"));
 
         // Negative relative searches from "i"
         assertNull(i.findComponent("d"));
         assertNull(i.findComponent("e"));
         assertNull(i.findComponent("f"));
         assertNull(i.findComponent("g"));
 
         // Absolute searches from "a"
         assertTrue(a == a.findComponent(":a"));
         assertTrue(b == a.findComponent(":b"));
         assertTrue(c == a.findComponent(":c"));
         assertTrue(d == a.findComponent(":b:d"));
         assertTrue(e == a.findComponent(":b:d:e"));
         assertTrue(f == a.findComponent(":b:d:f"));
         assertTrue(g == a.findComponent(":b:g"));
         assertTrue(h == a.findComponent(":h"));
         assertTrue(i == a.findComponent(":i"));
 
         // Absolute searches from "b"
         assertTrue(a == b.findComponent(":a"));
         assertTrue(b == b.findComponent(":b"));
         assertTrue(c == b.findComponent(":c"));
         assertTrue(d == b.findComponent(":b:d"));
         assertTrue(e == b.findComponent(":b:d:e"));
         assertTrue(f == b.findComponent(":b:d:f"));
         assertTrue(g == b.findComponent(":b:g"));
         assertTrue(h == b.findComponent(":h"));
         assertTrue(i == b.findComponent(":i"));
 
         // Absolute searches from "c"
         assertTrue(a == c.findComponent(":a"));
         assertTrue(b == c.findComponent(":b"));
         assertTrue(c == c.findComponent(":c"));
         assertTrue(d == c.findComponent(":b:d"));
         assertTrue(e == c.findComponent(":b:d:e"));
         assertTrue(f == c.findComponent(":b:d:f"));
         assertTrue(g == c.findComponent(":b:g"));
         assertTrue(h == c.findComponent(":h"));
         assertTrue(i == c.findComponent(":i"));
 
         // Absolute searches from "d"
         assertTrue(a == d.findComponent(":a"));
         assertTrue(b == d.findComponent(":b"));
         assertTrue(c == d.findComponent(":c"));
         assertTrue(d == d.findComponent(":b:d"));
         assertTrue(e == d.findComponent(":b:d:e"));
         assertTrue(f == d.findComponent(":b:d:f"));
         assertTrue(g == d.findComponent(":b:g"));
         assertTrue(h == d.findComponent(":h"));
         assertTrue(i == d.findComponent(":i"));
 
         // Absolute searches from "e"
         assertTrue(a == e.findComponent(":a"));
         assertTrue(b == e.findComponent(":b"));
         assertTrue(c == e.findComponent(":c"));
         assertTrue(d == e.findComponent(":b:d"));
         assertTrue(e == e.findComponent(":b:d:e"));
         assertTrue(f == e.findComponent(":b:d:f"));
         assertTrue(g == e.findComponent(":b:g"));
         assertTrue(h == e.findComponent(":h"));
         assertTrue(i == e.findComponent(":i"));
 
         // Absolute searches from "f"
         assertTrue(a == f.findComponent(":a"));
         assertTrue(b == f.findComponent(":b"));
         assertTrue(c == f.findComponent(":c"));
         assertTrue(d == f.findComponent(":b:d"));
         assertTrue(e == f.findComponent(":b:d:e"));
         assertTrue(f == f.findComponent(":b:d:f"));
         assertTrue(g == f.findComponent(":b:g"));
         assertTrue(h == f.findComponent(":h"));
         assertTrue(i == f.findComponent(":i"));
 
         // Absolute searches from "g"
         assertTrue(a == g.findComponent(":a"));
         assertTrue(b == g.findComponent(":b"));
         assertTrue(c == g.findComponent(":c"));
         assertTrue(d == g.findComponent(":b:d"));
         assertTrue(e == g.findComponent(":b:d:e"));
         assertTrue(f == g.findComponent(":b:d:f"));
         assertTrue(g == g.findComponent(":b:g"));
         assertTrue(h == g.findComponent(":h"));
         assertTrue(i == g.findComponent(":i"));
 
         // Absolute searches from "h"
         assertTrue(a == h.findComponent(":a"));
         assertTrue(b == h.findComponent(":b"));
         assertTrue(c == h.findComponent(":c"));
         assertTrue(d == h.findComponent(":b:d"));
         assertTrue(e == h.findComponent(":b:d:e"));
         assertTrue(f == h.findComponent(":b:d:f"));
         assertTrue(g == h.findComponent(":b:g"));
         assertTrue(h == h.findComponent(":h"));
         assertTrue(i == h.findComponent(":i"));
 
         // Absolute searches from "i"
         assertTrue(a == i.findComponent(":a"));
         assertTrue(b == i.findComponent(":b"));
         assertTrue(c == i.findComponent(":c"));
         assertTrue(d == i.findComponent(":b:d"));
         assertTrue(e == i.findComponent(":b:d:e"));
         assertTrue(f == i.findComponent(":b:d:f"));
         assertTrue(g == i.findComponent(":b:g"));
         assertTrue(h == i.findComponent(":h"));
         assertTrue(i == i.findComponent(":i"));
 
         // Cases that should throw exceptions
         try {
             a.findComponent(null);
             fail("Should have thrown NullPointerException");
         } catch (NullPointerException ex) {
             ; // Expected result
         }
         try {
             a.findComponent("a:c:h");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
         try {
             a.findComponent("a:c:i");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
         try {
             a.findComponent(":a:c:h");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
         try {
             a.findComponent(":a:c:i");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
         try {
             a.findComponent("c:h");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
         try {
             a.findComponent("c:i");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
         try {
             a.findComponent(":c:h");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
         try {
             a.findComponent(":c:i");
             fail("Should have thrown IllegalArgumentException");
         } catch (IllegalArgumentException ex) {
             ; // Expected result
         }
 
     }
 
 
     // --------------------------------------------------------- Support Methods
 
 
 }
