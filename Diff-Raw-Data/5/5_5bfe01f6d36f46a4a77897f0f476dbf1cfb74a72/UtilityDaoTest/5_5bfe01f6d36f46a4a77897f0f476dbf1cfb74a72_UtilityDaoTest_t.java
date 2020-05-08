 package com.solidstategroup.radar.test.dao;
 
 import com.solidstategroup.radar.dao.DiagnosisDao;
 import com.solidstategroup.radar.dao.UtilityDao;
 import com.solidstategroup.radar.model.Centre;
 import com.solidstategroup.radar.model.Consultant;
 import com.solidstategroup.radar.model.Country;
 import com.solidstategroup.radar.model.DiagnosisCode;
 import com.solidstategroup.radar.model.Ethnicity;
 import com.solidstategroup.radar.model.Relative;
 import com.solidstategroup.radar.model.filter.ConsultantFilter;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 public class UtilityDaoTest extends BaseDaoTest {
 
     @Autowired
     private UtilityDao utilityDao;
     @Autowired
     private DiagnosisDao diagnosisDao;
 
     @Test
     public void testGetCentre() {
         Centre centre = utilityDao.getCentre(4L);
         assertNotNull("Centre is null", centre);
         assertEquals("Centre ID is wrong", new Long(4), centre.getId());
        assertEquals("Name is wrong", "Cardiff,  Children's Hospital for Wales ", centre.getName());
         assertEquals("Abbreviation is wrong", "Cardiff", centre.getAbbreviation());
 
         // Check country
         assertNotNull("Country is null", centre.getCountry());
         assertEquals("Country name is wrong", "GB and Ireland", centre.getCountry().getName());
     }
 
     @Test
     public void testGetCentres() {
         List<Centre> centres = utilityDao.getCentres();
         assertNotNull("Centres list is null", centres);
     }
 
     @Test
     public void testGetConsultant() {
         Consultant consultant = utilityDao.getConsultant(4L);
         assertNotNull("Consultant is null");
         assertEquals("Surname is wrong", "ANBU", consultant.getSurname());
         assertEquals("Forename is wrong", "Dr A Theodore", consultant.getForename());
         assertNull("Centre is not null", consultant.getCentre());
     }
 
     @Test
     public void testGetConsultants() {
         List<Consultant> consultants = utilityDao.getConsultants(new ConsultantFilter(), -1, -1);
         assertNotNull("Consultants list is null", consultants);
     }
 
     @Test
     public void testGetConsultantsByCentre() throws Exception {
         Centre centre = new Centre();
         centre.setId(4L);
         List<Consultant> consultants = utilityDao.getConsultantsByCentre(centre);
         assertEquals(consultants.size(), 3);
     }
 
     @Test
     public void testGetConsultantsPage1() {
         List<Consultant> consultants = utilityDao.getConsultants(new ConsultantFilter(), 1, 1);
         assertNotNull(consultants);
         assertTrue(consultants.size() == 1);
     }
 
     @Test
     public void testSearchConsultants() {
         ConsultantFilter consultantFlter = new ConsultantFilter();
         consultantFlter.addSearchCriteria(ConsultantFilter.UserField.FORENAME.getDatabaseFieldName(), "Sonbol");
         List<Consultant> consultants = utilityDao.getConsultants(consultantFlter, -1, -1);
         assertNotNull(consultants);
         assertTrue(consultants.size() > 0);
     }
 
     @Test
     public void testSaveNewConsultant() throws Exception {
         Consultant consultant = new Consultant();
         consultant.setSurname("test_surname");
         consultant.setForename("test_forename");
 
         Centre centre = new Centre();
         centre.setId((long) 2);
         consultant.setCentre(centre);
 
         utilityDao.saveConsultant(consultant);
 
         assertTrue("Saved consultant doesn't have an ID", consultant.getId() > 0);
 
         consultant = utilityDao.getConsultant(consultant.getId());
         assertNotNull("Saved consultant was null on getting from DAO", consultant);
     }
 
     @Test
     public void testSaveExistingConsultant() throws Exception {
         // have to make a user first
         Consultant consultant = utilityDao.getConsultant(1);
         consultant.setSurname("test_surname");
 
         utilityDao.saveConsultant(consultant);
 
         consultant = utilityDao.getConsultant(consultant.getId());
         assertTrue("Consultant surname has not been updated", consultant.getSurname().equals("test_surname"));
     }
 
     @Test
     public void deleteConsultant() throws Exception {
         utilityDao.deleteConsultant(utilityDao.getConsultant(1));
 
         Consultant consultant;
         try {
             consultant = utilityDao.getConsultant(1);
         } catch (Exception e) {
             consultant = null;
         }
 
         assertNull("Consultant was not deleted", consultant);
     }
 
     @Test
     public void testGetConsultantWithCentre() {
         Consultant consultant = utilityDao.getConsultant(5L);
         assertNotNull("Consultant is null");
         assertEquals("Surname is wrong", "ARNEIL", consultant.getSurname());
         assertEquals("Forename is wrong", "Professor Gavin", consultant.getForename());
         assertNotNull("Centre is null", consultant.getCentre());
        assertEquals("Centre is wrong", "Belfast", consultant.getCentre().getAbbreviation());
     }
 
     @Test
     public void testGetCountries() {
         List<Country> countries = utilityDao.getCountries();
         assertNotNull("Countries list is null", countries);
     }
 
     @Test
     public void testGetEthnicities() {
         List<Ethnicity> ethnicities = utilityDao.getEthnicities();
         assertNotNull("Ethnicities list is null", ethnicities);
     }
 
     @Test
     public void testGetEthnicityUnknown() throws Exception {
         Ethnicity ethnicity = utilityDao.getEthnicityByCode("asasda");
         assertNull("Ethnicity not null", ethnicity);
     }
 
     @Test
     public void testGetRelatives() {
         List<Relative> relatives = utilityDao.getRelatives();
         assertNotNull("Relatives list is null", relatives);
     }
 
     @Test
     public void testGetPatientCountPerUnitByDiagnosisCode() throws Exception {
         DiagnosisCode diagnosisCode = diagnosisDao.getDiagnosisCode(1L);
         Map<Long, Integer> patientCountMap = utilityDao.getPatientCountPerUnitByDiagnosisCode(diagnosisCode);
         assertTrue(patientCountMap.get(3L).equals(8));
     }
 
     @Test
     public void testGetPatientCountByUnit() throws Exception {
         Centre centre = new Centre();
         centre.setId(3L);
         int count = utilityDao.getPatientCountByUnit(centre);
         assertTrue(count == 10);
     }
 }
