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
 
 package org.patientview.test.quartz;
 
 import com.Ostermiller.util.CSVParser;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Patient;
 import org.patientview.model.Specialty;
 import org.patientview.patientview.model.*;
 import org.patientview.quartz.UktImportExportScheduler;
 import org.patientview.repository.PatientDao;
 import org.patientview.service.*;
 import org.patientview.test.helpers.RepositoryHelpers;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.ResourceUtils;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
import java.util.Date;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:spring-context.xml", "classpath:test-context.xml"})
 @Transactional
 public class UktImportExportSchedulerTest {
 
     @Autowired
     private UktImportExportScheduler uktImportExportScheduler;
 
     private String uktDirectory;
 
     private Specialty specialty;
 
     private User user;
 
     private Patient patient;
 
     @Inject
     private RepositoryHelpers repositoryHelpers;
 
     @Inject
     private UKTransplantManager ukTransplantManager;
 
     @Inject
     private PatientDao patientDao;
 
     @Before
     public void setupSystem() throws Exception {
 
         specialty = repositoryHelpers.createSpecialty("Specialty1", "ten1", "A test specialty");
 
         user = repositoryHelpers.createUserWithMapping("username", "paul@test.com", "p", "username", "UNITCODEA",
                 "9876543211", specialty);
 
         patient = new Patient();
         patient.setNhsno("9876543211");
         patient.setUnitcode("UNITCODEA");
         patient.setSurname("surname");
         patient.setForename("forname");
         patient.setPostcode("postcode");
        patient.setDateofbirth(new Date());
 
         patientDao.save(patient);
 
     }
 
     @Test
     public void testExecute() throws Exception {
 
         int uktFilesSize = 0;
 
         String parentDir = ResourceUtils.getFile("classpath:schedule/test-uktstatus.gpg.txt").getParent();
 
         setUktDirectory(parentDir);
 
         File uktDir = new File(uktDirectory);
         File[] uktFiles = uktDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith("uktstatus.gpg.txt");
             }
         });
 
         if (uktFiles != null) {
             uktFilesSize = uktFiles.length;
         }
 
         assertTrue("Can not read UKT files", uktFilesSize != 0);
 
         uktImportExportScheduler.setUktDirectory(parentDir);
         uktImportExportScheduler.setUktExportDirectory(parentDir);
         uktImportExportScheduler.execute();
 
         UktStatus uktStatus = ukTransplantManager.getUktStatus("9876543210");
 
         if (uktFilesSize > 0) {
             assertNotNull("UktStatus not be saved", uktStatus);
             File file = ResourceUtils.getFile("classpath:schedule/ukt_rpv_export.txt");
             CSVParser uktParser = new CSVParser(new FileReader(file));
             uktParser.changeDelimiter(',');
             String[][] uktValues = uktParser.getAllValues();
 
             assertEquals("nhsno not same", patient.getNhsno(), uktValues[0][0]);
             assertEquals("surname not same", patient.getSurname(), uktValues[0][1]);
             assertEquals("forname not same", patient.getForename(), uktValues[0][2]);
             assertEquals("postcode not same", patient.getPostcode(), uktValues[0][4]);
 
             uktParser.close();
         } else {
             assertNull("Wrong entity exists.", uktStatus);
         }
     }
 
     public void setUktDirectory(String uktDirectory) {
         this.uktDirectory = uktDirectory;
     }
 
 }
