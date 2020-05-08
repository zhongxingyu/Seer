 package au.com.miskinhill.rdf;
 
 import static org.junit.Assert.*;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import au.com.miskinhill.TestUtil;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
 public class HTMLRepresentationTest {
     
     @Autowired private RepresentationFactory representationFactory;
     private Representation representation;
     private Model model;
     
     @Before
     public void setUp() throws Exception {
         model = ModelFactory.load(HTMLRepresentationTest.class, "/au/com/miskinhill/rdf/test.xml");
         representation = representationFactory.getRepresentationByFormat("html");
     }
     
     @Test
     public void testJournal() throws Exception {
         String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/"));
         String expected = TestUtil.exhaust(this.getClass().getResource("template/html/Journal.out.xml").toURI());
         assertEquals(expected.trim(), result.trim());
     }
     
     @Test
    public void testIssue() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/"));
        String expected = TestUtil.exhaust(this.getClass().getResource("template/html/Issue.out.xml").toURI());
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
     public void testAuthor() throws Exception {
         String result = representation.render(model.getResource("http://miskinhill.com.au/authors/test-author"));
         String expected = TestUtil.exhaust(this.getClass().getResource("template/html/Author.out.xml").toURI());
         assertEquals(expected.trim(), result.trim());
     }
     
     @Test
     public void testForum() throws Exception {
         String result = representation.render(model.getResource("http://miskinhill.com.au/"));
         String expected = TestUtil.exhaust(this.getClass().getResource("template/html/Forum.out.xml").toURI());
         assertEquals(expected.trim(), result.trim());
     }
     
     @Test
     public void testClass() throws Exception {
         String result = representation.render(model.getResource("http://miskinhill.com.au/rdfschema/1.0/Book"));
         String expected = TestUtil.exhaust(this.getClass().getResource("template/html/Class.out.xml").toURI());
         assertEquals(expected.trim(), result.trim());
     }
     
     @Test
     public void testProperty() throws Exception {
         String result = representation.render(model.getResource("http://miskinhill.com.au/rdfschema/1.0/startPage"));
         String expected = TestUtil.exhaust(this.getClass().getResource("template/html/Property.out.xml").toURI());
         assertEquals(expected.trim(), result.trim());
     }
 
 }
