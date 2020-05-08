 package org.motechproject.ghana.national.repository;
 
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.motechproject.ghana.national.BaseIntegrationTest;
 import org.motechproject.ghana.national.domain.CWCEnrollment;
 import org.motechproject.ghana.national.domain.MotechProgram;
 import org.motechproject.ghana.national.vo.MotechProgramName;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 public class AllEnrollmentTestIT extends BaseIntegrationTest {
 
     @Autowired
     private AllEnrollment allEnrollments;
 
     @Test
     public void shouldFindEnrollmentByPatientId() {
         String patientId = "123456";
         MotechProgram motechProgram = new MotechProgram(MotechProgramName.CWC);
         CWCEnrollment CWCEnrollment = new CWCEnrollment(patientId, motechProgram);
         allEnrollments.add(CWCEnrollment);
 
         CWCEnrollment actual = allEnrollments.findBy(patientId);
         assertThat(actual.getPatientId(), is(equalTo(patientId)));
         assertThat(actual.getProgram().getName(), is(equalTo(motechProgram.getName())));
     }
 
     @Test
     public void shouldFindEnrollmentByPatientIdAndProgramName() {
         String patientId = "123453";
         MotechProgram motechProgram = new MotechProgram(MotechProgramName.CWC);
         CWCEnrollment CWCEnrollment = new CWCEnrollment(patientId, motechProgram);
         allEnrollments.add(CWCEnrollment);
 
         CWCEnrollment actual = allEnrollments.findBy(patientId, motechProgram.getName());
         assertThat(actual.getPatientId(), is(equalTo(patientId)));
         assertThat(actual.getProgram().getName(), is(equalTo(motechProgram.getName())));
     }
 
     @Test
     public void shouldReturnNullIfPatientIdIsNotFound() {
         assertNull(allEnrollments.findBy("1234567"));
     }
 
     @After
     public void tearDown() {
         List<CWCEnrollment> all = allEnrollments.getAll();
         for (CWCEnrollment CWCEnrollment : all)
             allEnrollments.remove(CWCEnrollment);
     }
 }
