 package org.motechproject.whp.performancetests.patientreports;
 
 import org.databene.benerator.main.Benerator;
 import org.junit.Test;
 
 public class PatientSummaryPerformanceIT {
 
     @Test
     public void shouldLoadPatients() throws Exception {
        System.setProperty("count", String.valueOf(65000));
         Benerator.main(new String[]{"patients.ben.xml"});
     }
 }
