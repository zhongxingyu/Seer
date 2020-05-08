 package com.mpower.test.dao.ibatis;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.mpower.dao.interfaces.ConstituentDao;
 import com.mpower.domain.model.Person;
 import com.mpower.domain.model.Site;
 import com.mpower.util.StringConstants;
 
 public class IBatisConstituentDaoTest extends AbstractIBatisTest {
     
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
     
     private ConstituentDao constituentDao;
 
     @BeforeMethod
     public void setup() {
         constituentDao = (ConstituentDao)super.applicationContext.getBean("constituentDAO");
     }
     
     public static void testConstituentId100(Person constituent) {
         assert constituent.getId() == 100L;
         assert "Billy Graham Ministries".equals(constituent.getOrganizationName());
         assert "Graham".equals(constituent.getLastName());
         assert "Billy".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert constituent.getSuffix() == null;
         assert "company1".equals(constituent.getSite().getName());        
     }
     
     public static void testConstituentId200(Person constituent) {
         assert constituent.getId() == 200L;
         assert "Painters, Inc.".equals(constituent.getOrganizationName());
         assert "Picasso".equals(constituent.getLastName());
         assert "Pablo".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert "Sr".equals(constituent.getSuffix());
         assert "company1".equals(constituent.getSite().getName());
     }
 
     public static void testConstituentId300(Person constituent) {
         assert constituent.getId() == 300L;
         assert "Howdy Doody Inc".equals(constituent.getOrganizationName());
         assert "Doody".equals(constituent.getLastName());
         assert "Howdy".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert constituent.getSuffix() == null;
         assert "company1".equals(constituent.getSite().getName());
     }
 
     @Test(groups = { "testMaintainConstituent" }, dependsOnGroups = { "testReadConstituent" })
     public void testMaintainConstituent() throws Exception {
         // Insert
         Person constituent = new Person();
         constituent.setFirstName("Joe");
         constituent.setLastName("Bob");
         constituent.setSite(new Site("company1"));
         constituent.setConstituentType(Person.INDIVIDUAL);
         constituent.setTitle("Sir");
         
         constituent = constituentDao.maintainConstituent(constituent);
         assert constituent.getId() > 0;
         
         Person readConstituent = constituentDao.readConstituentById(constituent.getId());
         assert readConstituent != null;
         assert constituent.getFirstName().equals(readConstituent.getFirstName());
         assert constituent.getLastName().equals(readConstituent.getLastName());
         assert constituent.getSite().getName().equals(readConstituent.getSite().getName());
         assert constituent.getConstituentType().equals(readConstituent.getConstituentType());
         assert constituent.getTitle().equals(readConstituent.getTitle());
         
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentIndividualRoles());
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentOrganizationRoles());
         assert readConstituent.getLegalName() == null;
         assert readConstituent.getLoginId() == null;
         assert readConstituent.isMajorDonor() == false;
        assert "Unknown".equals(readConstituent.getMaritalStatus());
         assert readConstituent.getMiddleName() == null;
         assert readConstituent.getNcaisCode() == null;
         assert readConstituent.getOrganizationName() == null;
         assert readConstituent.getPreferredPhoneType() == null;
         assert readConstituent.getRecognitionName() == null;
         assert readConstituent.getSuffix() == null;
         assert readConstituent.getCreateDate() != null;
         assert readConstituent.getUpdateDate() != null;
         
         // Update
         constituent.setTitle("Lady");
         constituent.setLoginId("joe@bob.com");
         constituent.setNcaisCode("huh");        
         constituent = constituentDao.maintainConstituent(constituent);
         readConstituent = constituentDao.readConstituentById(constituent.getId());
         assert readConstituent != null;
         assert "Lady".equals(readConstituent.getTitle());
         assert "joe@bob.com".equals(readConstituent.getLoginId());
         assert "huh".equals(readConstituent.getNcaisCode());
         
         assert constituent.getFirstName().equals(readConstituent.getFirstName());
         assert constituent.getLastName().equals(readConstituent.getLastName());
         assert constituent.getSite().getName().equals(readConstituent.getSite().getName());
         assert constituent.getConstituentType().equals(readConstituent.getConstituentType());
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentIndividualRoles());
         assert StringConstants.EMPTY.equals(readConstituent.getConstituentOrganizationRoles());
         assert readConstituent.getLegalName() == null;
         assert readConstituent.isMajorDonor() == false;
        assert "Unknown".equals(readConstituent.getMaritalStatus());
         assert readConstituent.getMiddleName() == null;
         assert readConstituent.getOrganizationName() == null;
         assert readConstituent.getPreferredPhoneType() == null;
         assert readConstituent.getRecognitionName() == null;
         assert readConstituent.getSuffix() == null;
         assert readConstituent.getCreateDate() != null;
         assert readConstituent.getUpdateDate() != null;      
     }
     
     @Test(groups = { "testSetLapsedDonor" }, dependsOnGroups = { "testReadConstituent", "testMaintainConstituent" })
     public void testSetLapsedDonor() throws Exception {
         Person constituent = constituentDao.readConstituentById(200L);
         assert constituent.isLapsedDonor() == false;
         testConstituentId200(constituent);
         
         constituentDao.setLapsedDonor(200L);
         constituent = constituentDao.readConstituentById(200L);
         assert constituent.isLapsedDonor();
         testConstituentId200(constituent);
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentByIdInvalid() throws Exception {
         Person constituent = constituentDao.readConstituentById(0L);
         assert constituent == null;
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentById() throws Exception {
         Person constituent = constituentDao.readConstituentById(100L);
         assert constituent != null;
         assert constituent.getId() == 100;
         assert "Billy Graham Ministries".equals(constituent.getOrganizationName());
         assert "Graham".equals(constituent.getLastName());
         assert "Billy".equals(constituent.getFirstName());
         assert constituent.getMiddleName() == null;
         assert constituent.getSuffix() == null;
         assert "company1".equals(constituent.getSite().getName());        
     } 
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentByLoginIdInvalid() throws Exception {
         Person constituent = constituentDao.readConstituentByLoginId("pablo@companyDoesNotExist.com");
         assert constituent == null;
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadConstituentByLoginId() throws Exception {
         Person constituent = constituentDao.readConstituentByLoginId("pablo@company1.com");
         assert constituent != null;
         assert constituent.getId() == 200;
         testConstituentId200(constituent);
     }
     
     @Test(groups = { "testReadConstituent" })
     public void testReadAllConstituentsBySiteName() throws Exception {
         List<Person> constituents = constituentDao.readAllConstituentsBySite();
         assert constituents != null && constituents.size() == 3;
         for (Person constituent : constituents) {
             assert "company1".equals(constituent.getSite().getName());
             assert constituent.getId() == 200 || constituent.getId() == 300 || constituent.getId() == 100;
             assert "Painters, Inc.".equals(constituent.getOrganizationName()) || "Howdy Doody Inc".equals(constituent.getOrganizationName()) || "Billy Graham Ministries".equals(constituent.getOrganizationName());
             assert "Picasso".equals(constituent.getLastName()) || "Doody".equals(constituent.getLastName()) || "Graham".equals(constituent.getLastName());
             assert "Pablo".equals(constituent.getFirstName()) || "Howdy".equals(constituent.getFirstName()) || "Billy".equals(constituent.getFirstName());
             assert constituent.getMiddleName() == null;
             assert "Sr".equals(constituent.getSuffix()) || constituent.getSuffix() == null;
         }
     }
     
     
     @Test(groups = { "testSearchPersons" })
     public void testSearchPersons() throws Exception {
     	
     	Map<String, Object> params = new HashMap<String, Object>();
         List<Long> ignoreIds = new ArrayList<Long>();
         long ignoreId = 100l;
         ignoreIds.add(ignoreId);
         
         params.put("firstName", "Pablo");
         params.put("accountNumber", new Long(200));
         params.put("phoneMap[home].number", "214-113-2542");
         params.put("addressMap[home].addressLine1", "ACORN");
         params.put("emailMap[home].email", "");
         //params.put("customFieldMap[emailFormat].value", "HTML");
     	
         List<Person> constituents = constituentDao.searchConstituents(params, ignoreIds);
         assert constituents != null && constituents.size() > 0;
         for (Person constituent : constituents) {
         	System.out.println(constituent);
             assert constituent.getFirstName().equals("Pablo");
             assert constituent.getId().longValue() != ignoreId;
         }
         
     }    
     
 }
