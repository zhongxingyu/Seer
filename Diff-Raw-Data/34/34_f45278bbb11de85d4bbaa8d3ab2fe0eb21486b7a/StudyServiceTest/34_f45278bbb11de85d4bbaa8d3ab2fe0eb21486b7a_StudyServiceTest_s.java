 package jogoanatomia.services;
 
 import static org.junit.Assert.*;
 
 import jogoanatomia.entidades.Study;
 import org.junit.Test;
 
 import java.util.List;
 
 // TODO: This is only a dummy test.
 public class StudyServiceTest {
     private StudyService service = new StudyServiceImpl();
 
     @Test
     public void listByOrganId() {
        List<Study> studies = service.listByOrganId("52404778ec48b10e0e000002");
 
         assertNotNull(studies);
         assertFalse(studies.isEmpty());
 
         for (Study study: studies) {
             assertNotNull(study.getId());
             assertNotNull(study.getContent());
            assertEquals(study.getOrganId(), "52404778ec48b10e0e000002");
         }
     }
 
     @Test
     public void findTest() {
        Study study = service.find("52404778ec48b10e0e000001");
 
         assertNotNull(study);
        assertEquals("52404778ec48b10e0e000001", study.getId());
         assertNotNull(study.getContent());
         assertNotNull(study.getOrganId());
     }
 }
