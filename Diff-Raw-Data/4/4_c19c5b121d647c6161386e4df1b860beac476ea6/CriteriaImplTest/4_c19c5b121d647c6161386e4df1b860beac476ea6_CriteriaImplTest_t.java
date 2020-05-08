 package org.lazydog.repository.jpa.internal;
 
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 import org.lazydog.addressbook.model.Company;
 import org.lazydog.repository.criterion.Comparison;
 
 
 /**
  * Criteria implemented using the Java Persistence API test.
  * 
  * @author  Ron Rickard
  */
 public class CriteriaImplTest {
 
     @Test
     public void testAutoJoin() {
         String expectedQueryLanguageString = "SELECT DISTINCT company FROM Company company JOIN company.departments departments WHERE departments.name LIKE :param1";
         CriteriaImpl<Company> criteriaImpl = new CriteriaImpl<Company>(Company.class);
         criteriaImpl.add(Comparison.like("departments.name", "%Name 10%"));
         
         assertEquals(expectedQueryLanguageString, criteriaImpl.getQueryLanguageString());
     }
    
     @Test
     public void testNoAutoJoin() {
         String expectedQueryLanguageString = "SELECT company FROM Company company WHERE company.name LIKE :param1";
         CriteriaImpl<Company> criteriaImpl = new CriteriaImpl<Company>(Company.class);
         criteriaImpl.add(Comparison.like("name", "%Name 10%"));
         assertEquals(expectedQueryLanguageString, criteriaImpl.getQueryLanguageString());
     }
 }
