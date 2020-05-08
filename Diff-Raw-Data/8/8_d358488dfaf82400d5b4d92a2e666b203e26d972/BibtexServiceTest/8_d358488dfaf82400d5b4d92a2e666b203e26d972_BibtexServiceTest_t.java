 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import ohtu.domain.Book;
 import ohtu.domain.Reference;
import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)                             //PAKOLLISIA
 @ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/front-controller-servlet.xml"}) //PAKOLLISIA
 public class BibtexServiceTest {
 
     @Autowired
     ApplicationContext context;
     BibtexService service;
     ArrayList<Reference> list;
 
     @Before
     public void setUp() {
         service = context.getBean(BibtexService.class);
         list = new ArrayList<Reference>();
     }
 
     @Test
     public void beanFound() {
     }
 
     @Test
     public void generateListReturnsEmptyList() {
         List blank = service.generate(list);
         assertEquals(0, blank.size());
     }
 
     @Test
     public void generatingAndAddingIncreasesSize() {
         populateList(2);
         List<String> bibtexs = service.generate(list);
         assertEquals(2, bibtexs.size());
         for (int i = 0; i < bibtexs.size(); i++) {
             String ref = bibtexs.get(i);
             assertTrue(ref.contains("testAuthor"));
             assertTrue(ref.contains("testTitle"));
         }
 
     }
     
     @Test
     public void bookBibtexSyntaxContainsTagAndBrackets() {
         Reference ref1 = createBook();
         String bibtex = service.generate(ref1);
         checkTagAndBrackets(bibtex, "book");
         
     }
       
     
     @Test
     public void bookBibtexSyntaxContainsAuthorAndTitle() {
         Reference ref1 = createBook();
         String bibtex = service.generate(ref1);
         checkAuthorAndTitle(bibtex,"author","testAuthor");
         checkAuthorAndTitle(bibtex, "title", "testTitle");
     }
     
     private void checkAuthorAndTitle(String bibtex, String name, String value){
         String regex = "[\\s\\S]*"+name+"\\s+=\\s+\""+value+"\\s[0-9a-z\\-]*\"[\\s\\S]*";
         assertTrue(bibtex.matches(regex));
         // [\s\S]*author\s+=\s+"testAuthor\s[0-9a-z\-]*"[\s\S]*
     }
     
     private void checkTagAndBrackets(String bibtex, String tag){
         String regex = "@"+tag+"\\{(\\s|\\S)*\\}";
         bibtex=bibtex.replaceAll("\n", "");
         assertTrue(bibtex.toLowerCase().matches(regex));
         // [\s\S]*author\s+=\s+"testAuthor\s[0-9a-z\-]*"[\s\S]*
     }
 
     private Reference createBook() {
         Reference ref = new Book();
         ref.setAuthor("testAuthor " + UUID.randomUUID());
         ref.setTitle("testTitle " + UUID.randomUUID());
         return ref;
     }
     private void populateList(int amount){
         for (int i = 0; i < amount; i++) {
             list.add(createBook());
         }
     }
 
 
     @Test
     public void generateStringUsingOneReferenceWorks(){
         populateList(1);
         assertEquals(service.generate(list.get(0)),service.generateString(list));
     }
     @Test
     public void generateStringUsingTwoReferenceWorks(){
         populateList(2);
         String combined = service.generateString(list);
         assertTrue(combined.contains(service.generate(list.get(0))));
         assertTrue(combined.contains(service.generate(list.get(1))));
     }
     
     
 }
