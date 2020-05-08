 package org.patientview.test.quartz;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.patientview.model.Centre;
 import org.patientview.quartz.XmlImportJobQuartzScheduler;
 import org.patientview.service.CentreManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
 
 import javax.inject.Inject;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:spring-context.xml", "classpath:test-context.xml"})
@Transactional
 public class XmlImportJobQuartzSchedulerTest {
 
     @Autowired
     private XmlImportJobQuartzScheduler xmlImportJobQuartzScheduler;
 
     @Inject
     private CentreManager centreManager;
 
     @Test
     public void testExecute() throws Exception {
 
         xmlImportJobQuartzScheduler.execute();
         List<Centre> centres = centreManager.getAll();
 
         assertEquals("Wrong number of centre list size", 0, centres.size());
 
     }
 
 }
