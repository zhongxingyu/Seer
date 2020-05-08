 package net.sourceforge.jfacets.annotations;
 
 import net.sourceforge.jfacets.JFacetsSpringTestBase;
 import net.sourceforge.jfacets.annotations.examples.TestFacet1;
 import net.sourceforge.jfacets.annotations.examples.TestFacet2;
 import net.sourceforge.jfacets.annotations.examples.TestFacet3;
 import net.sourceforge.jfacets.annotations.pkg2.MyFacet2;
 
 import java.util.Date;
 
 public class AnnotatedFacetsTest extends JFacetsSpringTestBase {
 
     public AnnotatedFacetsTest() {
         super("annotatedFacetsAppCtx.xml");
     }
 
     public void testDescriptorsCount() {
         int nbDescriptors = jFacets.getFacetRepository().getFacetDescriptorManager().getDescriptors().length;
         assertEquals("invalid number of descriptors found", 3, nbDescriptors);
     }
 
     public void testStringIvar() {
         Object facet = jFacets.getFacet("test", "ivar", "yikes");
         assertNotNull("facet is null", facet);
         assertEquals("invalid facet class found", TestFacet1.class, facet.getClass());
     }
 
 
     public void testIntegerIvar() {
         Object facet = jFacets.getFacet("test", "ivar", 123);
         assertNotNull("facet is null", facet);
         assertEquals("invalid facet class found", TestFacet2.class, facet.getClass());
     }
 
 
     public void testDateIvar() {
         Object facet = jFacets.getFacet("test", "ivar", new Date());
         assertNotNull("facet is null", facet);
         assertEquals("invalid facet class found", TestFacet3.class, facet.getClass());
     }
 
     public void testDateJohn() {
         Object facet = jFacets.getFacet("test", "john", new Date());
         assertNotNull("facet is null", facet);
         assertEquals("invalid facet class found", TestFacet1.class, facet.getClass());
     }
 
    public void testPackagePrecedencyInCaseOfDuplicates() {
        Object facet = jFacets.getFacet("my", "john", "foobarbaz");
        assertNotNull("facet is null", facet);
        assertEquals("invalid facet class found", MyFacet2.class, facet.getClass());

    }

 }
