 package org.motechproject.whp.performancetests.patientreports;
 
 import org.databene.benerator.main.Benerator;
import org.junit.Ignore;
 import org.junit.Test;
 
 public class PatientSummaryPerformanceIT {
 
    @Ignore("will load data everytime mci is run")
     @Test
     public void shouldLoadPatients() throws Exception {
        System.setProperty("count", String.valueOf(70000));
         Benerator.main(new String[]{"patients.ben.xml"});
     }

 }
