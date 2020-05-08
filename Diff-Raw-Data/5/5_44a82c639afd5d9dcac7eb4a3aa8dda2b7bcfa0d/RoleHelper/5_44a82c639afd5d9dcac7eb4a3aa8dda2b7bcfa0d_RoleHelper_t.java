 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.radar.test.roles.unitadmin;
 
 import org.patientview.model.Centre;
 import org.patientview.model.Patient;
 import org.patientview.model.enums.NhsNumberType;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.service.UserManager;
 import org.springframework.stereotype.Component;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: james@solidstategroup.com
  * Date: 04/11/13
  * Time: 10:16
  */
 @Component
 public class RoleHelper {
 
     @Inject
     private UserDao userDao;
 
     @Inject
     private UtilityDao utilityDao;
 
     @Inject
     private UserManager userManager;
 
     public PatientUser createUnitAdmin(String nhsNo, String... unitCode) throws Exception {
         // Creates a user in patient with a mapping!!
         PatientUser patientUser = createUserInPatientView(nhsNo);
         userDao.createRoleInPatientView(patientUser.getUserId(), "unitadmin");
         // Create a mapping for the user
         for (String s : unitCode) {
             userDao.createUserMappingInPatientView(patientUser.getUsername(), nhsNo, s);
         }
         return patientUser;
     }
 
 
     public List<PatientUser> createPatientsInUnit(String unitName, String diseaseName, int count) throws Exception {
         List<PatientUser> patientUsers = new ArrayList<PatientUser>(count);
 
         // Add 10 patients to the RENAL Unit A
         for (int i = 1; i <= count; i++) {
 
             Patient patient = new Patient();
             patient.setUnitcode(unitName);
             patient.setForename("Unit");
             patient.setSurname("Tester");
            patient.setDateofbirth(new Date());
             patient.setDob(new Date());
             patient.setNhsno("1000000" + i);
             patient.setNhsNumberType(NhsNumberType.NHS_NUMBER);
             patient.setUnitcode(unitName);
             patient.setEmailAddress("test@test.com");
 
             DiseaseGroup diseaseGroup = new DiseaseGroup();
             diseaseGroup.setId(diseaseName);
             patient.setDiseaseGroup(diseaseGroup);
 
             Centre centre = new Centre();
             centre.setUnitCode(unitName);
             patient.setRenalUnit(centre);
 
             patientUsers.add(userManager.addPatientUserOrUpdatePatient(patient));
         }
         return patientUsers;
     }
 
 
     public void deleteUser(PatientUser patientUser) throws Exception {
         userDao.deletePatientUser(patientUser);
     }
 
 
     public PatientUser createUserInPatientView(String nhsNo) throws Exception {
         // Creates a user in patient with a mapping!!
         PatientUser patientUser = new PatientUser();
         patientUser.setEmail(nhsNo + "@patientview.com");
         patientUser.setUsername("TestUser");
         patientUser.setName("Test User");
         patientUser.setPassword("HasPassword");
 
         return (PatientUser) userDao.createUser(patientUser);
 
     }
 
     public void cleanTestData(List<Patient> patients, String unitCode) {
         // -- Clean up test (No Transaction Manager)
         for (Patient patient : patients) {
             utilityDao.deletePatientViewMapping(patient.getNhsno());
            // utilityDao.deletePatientViewUser(patientUser.getUsername());
         }
 
         utilityDao.deleteUnit(unitCode);
     }
 
 
     public Patient createPatient(String nhsNo, String unitName, String diseaseName) throws Exception {
         Patient patient = new Patient();
         patient.setUnitcode(unitName);
         patient.setForename("Unit");
         patient.setSurname("Tester");
        patient.setDateofbirth(new Date());
         patient.setDob(new Date());
         patient.setHospitalnumber("90789");
         patient.setAddress1("87 hgyt roda");
         patient.setPostcode("hg656hg");
         patient.setNhsno(nhsNo);
         patient.setNhsNumberType(NhsNumberType.NHS_NUMBER);
         patient.setUnitcode(unitName);
         patient.setEmailAddress("test@test.com");
         patient.setSex("Male");
 
         DiseaseGroup diseaseGroup = new DiseaseGroup();
         diseaseGroup.setId(diseaseName);
         patient.setDiseaseGroup(diseaseGroup);
 
         Centre centre = new Centre();
         centre.setUnitCode(unitName);
         patient.setRenalUnit(centre);
 
         //userManager.addPatientUserOrUpdatePatient(patient);
 
         return patient;
 
     }
 
 
 }
