 package org.motechproject.ghana.national.service;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
 public class IdentifierGenerationServiceIT {
 
     @Autowired
     private IdentifierGenerationService identifierService;
 
     @Test
//    @Ignore("just to test and verify with omod up and running")
     public void shouldGenerateIDForAnIDTypeAndGenerator() throws IOException {
 
         String facilityId = identifierService.newFacilityId();
         String patientId = identifierService.newPatientId();
         String staffId = identifierService.newStaffId();
 
         System.out.println("IDs: Facility=" + facilityId + "|Staff=" + staffId + "|Patient=" + patientId);
 
         assertTrue(StringUtils.isNotBlank(facilityId));
         assertTrue(StringUtils.isNotBlank(staffId));
         assertTrue(StringUtils.isNotBlank(patientId));
     }
 }
