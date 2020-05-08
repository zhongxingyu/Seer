 package eionet.meta.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.unitils.UnitilsJUnit4;
 import org.unitils.spring.annotation.SpringApplicationContext;
 import org.unitils.spring.annotation.SpringBeanByType;
 
 import eionet.meta.dao.domain.DataElement;
 import eionet.meta.dao.domain.VocabularyConcept;
 
 /**
  * DataService tests.
  *
  * @author Kaido Laine
  */
 @SpringApplicationContext("mock-spring-context.xml")
 public class DataServiceTest extends UnitilsJUnit4  {
 
     /**
      * Service instance.
      */
     @SpringBeanByType
     IDataService dataService;
 
     /**
      * Load seed data file.
      * @throws Exception if loading fails
      */
     @BeforeClass
     public static void loadData() throws Exception {
         DBUnitHelper.loadData("seed-dataelements.xml");
     }
 
     /**
      * Delete helper data.
      * @throws Exception if delete fails
      */
     @AfterClass
     public static void deleteData() throws Exception {
         DBUnitHelper.deleteData("seed-dataelements.xml");
     }
 
     /**
      * Test on getting common data elements.
      * @throws Exception if fail
      */
     @Test
     public void testGetCommonElements() throws Exception {
         List<DataElement> elements = dataService.getReleasedCommonDataElements();
 
         Assert.assertTrue(elements.size() == 7);
 
     }
 
     /**
      * test set element attribute values.
      * @throws Exception if fail
      */
     @Test
     public void testSetElementAttributes() throws Exception {
         DataElement elem1 = dataService.getDataElement(1);
         dataService.setDataElementAttributes(elem1);
 
         DataElement elem2 = dataService.getDataElement(2);
         dataService.setDataElementAttributes(elem2);
 
         DataElement elem3 = dataService.getDataElement(3);
         dataService.setDataElementAttributes(elem3);
 
         Assert.assertTrue(elem1.getElemAttributeValues().size() == 2);
         Assert.assertEquals(elem1.getName(), "Common element");
 
         Assert.assertTrue(elem2.getElemAttributeValues().size() == 0);
         Assert.assertTrue(elem3.getElemAttributeValues().size() == 1);
         Assert.assertEquals(elem3.getElemAttributeValues().get("Definition").get(0), "Third definition");
 
     }
 
     /**
      * tests if datasets contain only released elements.
      * @throws Exception if fail
      */
     @Test
     public void testHasElementsReleased() throws Exception {
         List<DataElement> elems1 = dataService.getUnreleasedCommonElements(1);
         List<DataElement> elems2 = dataService.getUnreleasedCommonElements(2);
 
         Assert.assertTrue("Dataset1 contains an unreleased element", elems1.size() == 1);
         Assert.assertTrue("Dataset2 contains released elements", elems2.size() == 0);
     }
 
     /**
      * test method receiving elements having a vocabulary as source.
      * @throws Exception if fail
      */
     @Test
     public void testVocabularyElems() throws Exception {
 
         List<Integer> p1 = new ArrayList<Integer>();
         p1.add(1);
 
         List<Integer> p2 = new ArrayList<Integer>();
         p2.add(1);
         p2.add(2);
 
         List <DataElement> elems1 = dataService.getVocabularySourceElements(p1);
         List <DataElement> elems2 = dataService.getVocabularySourceElements(p2);
 
         Assert.assertTrue("Vocabulary1 is source for 2 elements ", elems1.size() == 2);
         Assert.assertTrue("Vocabularies 1 and 2 are source for 3 elements ", elems2.size() == 3);
 
 
     }
 
 
     /**
      * test if correct count of concepts are bound to element.
      * Especially important is obsolete date check functionality if
      * element type is not all concepts valid
      * @throws Exception if error
      */
     @Test
     public void testElementConcepts() throws Exception {
 
         List<VocabularyConcept> concepts1 = dataService.getElementVocabularyConcepts(301);
 
         //this element does not have 2 concepts one marked obsolete before releasing and the other created after
         //releasing of the element:
         List<VocabularyConcept> concepts2 = dataService.getElementVocabularyConcepts(302);
 
        Assert.assertEquals("Element ID=301 has to have 5 concepts in fxvs ", 5, concepts1.size());
        Assert.assertEquals("Element ID=302 has to have 3 concepts in fxvs ", 3, concepts2.size());
 
     }
 
 
 }
