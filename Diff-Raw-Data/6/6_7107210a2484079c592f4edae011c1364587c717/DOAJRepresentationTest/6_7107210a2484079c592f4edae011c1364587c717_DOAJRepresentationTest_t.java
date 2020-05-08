 package au.com.miskinhill.rdf;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.Collections;
import java.util.List;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.XPath;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:/au/com/miskinhill/web/test-spring-context-with-fake-model.xml")
 public class DOAJRepresentationTest {
     
     @Autowired private RepresentationFactory representationFactory;
     private Representation representation;
     @Autowired private Model model;
     
     @Before
     public void setUp() throws Exception {
         representation = representationFactory.getRepresentationByFormat("doaj");
     }
     
     @Test
     public void should_have_no_namespaces() throws Exception {
         Document doc = renderJournal();
         assertThat(doc.getRootElement().getNamespace(), equalTo(Namespace.NO_NAMESPACE));
         assertThat(doc.getRootElement().getNamespaceForURI("http://code.miskinhill.com.au/rdftemplate/"),
                 nullValue());
         assertThat(doc.getRootElement().element("record").getNamespace(),
                 equalTo(Namespace.NO_NAMESPACE));
         assertThat(doc.getRootElement().element("record")
                 .getNamespaceForURI("http://code.miskinhill.com.au/rdftemplate/"),
                 nullValue());
     }
     
     @Test
     public void should_include_language() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/language").selectSingleNode(doc).getText(),
                 equalTo("eng"));
     }
     
     @Test
     public void should_include_publisher() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/publisher").selectSingleNode(doc).getText(),
                 equalTo("Awesome Publishing House"));
     }
     
     @Test
     public void should_include_journalTitle() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/journalTitle").selectSingleNode(doc).getText(),
                 equalTo("Test Journal of Good Stuff"));
     }
     
     @Test
     public void should_include_issn() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/issn").selectSingleNode(doc).getText(),
                 equalTo("12345678"));
     }
     
     @Test
     public void should_include_publicationDate() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/publicationDate").selectSingleNode(doc).getText(),
                 equalTo("2008-02-01"));
     }
     
     @Test
     public void should_include_volume() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/volume").selectSingleNode(doc).getText(),
                 equalTo("1"));
     }
     
     @Test
     public void should_include_issue() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/issue").selectSingleNode(doc).getText(),
                 equalTo("1"));
     }
     
     @Test
     public void should_include_startPage() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/startPage").selectSingleNode(doc).getText(),
                 equalTo("5"));
     }
     
     @Test
     public void should_include_endPage() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/endPage").selectSingleNode(doc).getText(),
                 equalTo("35"));
     }
     
     @Test
     public void should_include_documentType() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/documentType").selectSingleNode(doc).getText(),
                 equalTo("article"));
     }
     
     @Test
     public void should_include_title_without_markup() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/title").selectSingleNode(doc).getText(),
                 equalTo("Moscow 1937: the interpreter’s story"));
     }
     
     @Test
     public void should_include_author() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/authors/author/name").selectSingleNode(doc).getText(),
                 equalTo("Test Author"));
     }
     
     @Ignore("DOAJ schema only permits one fullTextUrl per record :-(")
     @Test
     public void should_include_pdf_fullTextUrl() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/fullTextUrl[@format='pdf']").selectSingleNode(doc).getText(),
                 equalTo("http://miskinhill.com.au/journals/test/1:1/article.pdf"));
     }
     
     @Test
     public void should_include_html_fullTextUrl() throws Exception {
         Document doc = renderJournal();
         assertThat(xpath("/records/record/fullTextUrl[@format='html']").selectSingleNode(doc).getText(),
                 equalTo("http://miskinhill.com.au/journals/test/1:1/article"));
     }
     
     @Test
     public void should_strip_out_xmllang_attributes() throws Exception {
         Document doc = renderJournal();
        assertThat((List<Element>) xpath("//*[@xml:lang]").selectNodes(doc),
                 equalTo(Collections.<Element>emptyList()));
     }
     
     @Test
     public void should_set_language_on_title() throws Exception {
         // XXX implementation is dodgy, should use title language instead of article language
         Document doc = renderJournal();
         assertThat(xpath("/records/record/title/@language").selectSingleNode(doc).getText(),
                 equalTo("eng"));
     }
     
     private Document renderJournal() throws DocumentException {
         String result = representation.render(model.getResource(
                 "http://miskinhill.com.au/journals/test/"));
         return DocumentHelper.parseText(result);
     }
     
     private XPath xpath(String expression) {
         return DocumentHelper.createXPath(expression);
     }
 
 }
