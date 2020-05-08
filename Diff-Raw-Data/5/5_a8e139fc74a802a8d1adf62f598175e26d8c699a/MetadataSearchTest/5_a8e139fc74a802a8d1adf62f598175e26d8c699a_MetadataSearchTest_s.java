 package edu.wustl.cab2b.client.metadatasearch;
 
 import java.util.Set;
 
 import junit.framework.TestCase;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.cache.ClientSideCache;
 import edu.wustl.cab2b.common.beans.MatchedClass;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.util.Constants;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author Chandrakant Talele
  */
 public class MetadataSearchTest extends TestCase {
     static {
         Logger.configure();
     }
 
     static MatchedClass resultMatchedClass = new MatchedClass();
 
     static ClientSideCache entityCache = ClientSideCache.getInstance();
 
     static MetadataSearch metadataSearch = new MetadataSearch(entityCache);
     public void testSearchAttributeBasedOnConceptCode() {
         
         int[] searchTargetStatus = { Constants.ATTRIBUTE };
         String[] searchString = { "C45763" };
         int basedOn = Constants.BASED_ON_CONCEPT_CODE;
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, basedOn);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         for (EntityInterface eI : entities) {
             String result = eI.getName();
             b = b || result.contains("Protein");
         }
         assertTrue(b);
     }
   public void testSearchEntityBasedOnConceptCode() {
     
             int[] searchTargetStatus = { Constants.CLASS };
             String[] searchString = { "C17021" };
             int basedOn = Constants.BASED_ON_CONCEPT_CODE;
             try {
                 resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, basedOn);
             } catch (CheckedException e) {
                 e.printStackTrace();
                 fail();
             }
             Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
             boolean b = false;
             for (EntityInterface eI : entities) {
                 String result = eI.getName();
                 b = b || result.contains("Protein");
             }
             assertTrue(b);
         }
     
     public void testSearchInvalidTarget() {
 
         int[] searchTargetStatus = { 1234 };
         String[] searchString = { "Romania" };
         boolean gotException = false;
         int basedOn = Constants.BASED_ON_TEXT;
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, basedOn);
         } catch (CheckedException e) {
             gotException = true;
         }
         assertTrue(gotException);
     }
     public void testSearchPvBasedOnText() {
 
         int[] searchTargetStatus = { Constants.PV };
         String[] searchString = { "Romania" };
         int basedOn = Constants.BASED_ON_TEXT;
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, basedOn);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         for (EntityInterface eI : entities) {
             String result = eI.getName();
             b = b || result.contains("Address");
         }
         assertTrue(b);
     }
     
     public void testSearchPvOnConceptCode() {
 
         int[] searchTargetStatus = { Constants.PV };
        String[] searchString = { "C19157", "C25447" };
         int basedOn = Constants.BASED_ON_CONCEPT_CODE;
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, basedOn);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         for (EntityInterface eI : entities) {
             String result = eI.getName();
            b = b || result.contains("SpecimenCharacteristics");
         }
         assertTrue(b);
     }
 
     public void testsearchNullTargetString() {
 
         int[] searchTargetStatus = { Constants.PV };
         int basedOn = Constants.BASED_ON_CONCEPT_CODE;
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, null, basedOn);
             fail();
         } catch (CheckedException e) {
             assertTrue(true);
         }
     }
 
     public void testsearchNullSearchTargetStatus() {
 
         String[] searchString = { "C25228", "C62637" };
         int basedOn = Constants.BASED_ON_CONCEPT_CODE;
         try {
             resultMatchedClass = metadataSearch.search(null, searchString, basedOn);
             fail();
         } catch (CheckedException e) {
             assertTrue(true);
         }
     }
 
     public void testsearchBasedOnOutOfBounds() {
 
         int[] searchTargetStatus = { Constants.PV };
         String[] searchString = { "C25228", "C62637" };
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, 3);
             fail();
         } catch (CheckedException e) {
             assertTrue(true);
         }
     }
 
     public void testsearchCategory() {
 
         int[] searchTargetStatus = { Constants.CLASS };
         String[] searchString = { "chromosome" };
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, Constants.BASED_ON_TEXT);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         for (EntityInterface eI : entities) {
             String result = eI.getName();
             b = b || result.contains("Chromosome");
         }
         assertTrue(b);
     }
 
     public void testsearchCategoruWithDescription() {
 
         int[] searchTargetStatus = { Constants.CLASS_WITH_DESCRIPTION };
         String[] searchString = { "The combined anatomic state" };
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, Constants.BASED_ON_TEXT);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         for (EntityInterface eI : entities) {
             String result = eI.getName();
             b = b || result.contains("SpecimenCharacteristics");
         }
         assertTrue(b);
     }
 
     public void testsearchAttribute() {
         int[] searchTargetStatus = { Constants.ATTRIBUTE };
         String[] searchString = { "chromosome" };
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, Constants.BASED_ON_TEXT);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         for (EntityInterface eI : entities) {
             String result = eI.getName();
             b = b || result.contains("Gene Annotation");
         }
         assertTrue(b);
     }
 
     public void testsearchAttributeWithDescription() {
         int[] searchTargetStatus = { Constants.ATTRIBUTE_WITH_DESCRIPTION };
         String[] searchString = { "chromosome" };
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, Constants.BASED_ON_TEXT);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         for (EntityInterface eI : entities) {
             String result = eI.getName();
             b = b || result.contains("Literature-based Gene Association");
         }
         assertTrue(b);
     }
 
     public void testsearchCategoryPrecedance() {
 
         int[] searchTargetStatus = { Constants.CLASS };
         String[] searchString = { "specimen" };
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, Constants.BASED_ON_TEXT);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         int specimenIndex = 0;
         for (EntityInterface eI : entities) {
             if (eI.getName().equals("edu.wustl.catissuecore.domain.Specimen")) {
                 break;
             }
             specimenIndex++;
         }
 
         int tissueSpecimenIndex = 0;
         for (EntityInterface eI : entities) {
             if (eI.getName().equals("edu.wustl.catissuecore.domain.TissueSpecimen")) {
                 break;
             }
             tissueSpecimenIndex++;
         }
         assertTrue(specimenIndex < tissueSpecimenIndex);
     }
 
     public void testsearchCategoryPrecedance1() {
 
         int[] searchTargetStatus = { Constants.CLASS };
         String[] searchString = { "gene" };
         try {
             resultMatchedClass = metadataSearch.search(searchTargetStatus, searchString, Constants.BASED_ON_TEXT);
         } catch (CheckedException e) {
             e.printStackTrace();
             fail();
         }
         Set<EntityInterface> entities = resultMatchedClass.getEntityCollection();
         boolean b = false;
         int index1 = 0;
         for (EntityInterface eI : entities) {
             if (eI.getName().equals("Gene Annotation")) {
                 break;
             }
             index1++;
         }
 
         int index2 = 0;
         for (EntityInterface eI : entities) {
             if (eI.getName().equals("edu.wustl.fe.Gene")) {
                 break;
             }
             index2++;
         }
 
         int index3 = 0;
         for (EntityInterface eI : entities) {
             if (eI.getName().equals("edu.wustl.fe.Unigene")) {
                 break;
             }
             index3++;
         }
 
         assertTrue(index1 < index2);
         assertTrue(index2 < index3);
     }
 }
