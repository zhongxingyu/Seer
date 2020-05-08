 package jogoanatomia.services;
 
 import static org.junit.Assert.*;
 
 import jogoanatomia.entidades.Study;
 import org.junit.Test;
 
 import java.util.List;
 
 // TODO: This is only a dummy test.
 public class StudyServiceTest {
     private StudyService service = new StudyServiceImpl();
 
    private final String ORRGAN_ID = "5241a3234afaca7a9a000002";
    private final String STUDY_ID  = "5241a3234afaca7a9a000001";

     @Test
     public void listByOrganId() {
        List<Study> studies = service.listByOrganId(ORRGAN_ID);
 
         assertNotNull(studies);
         assertFalse(studies.isEmpty());
 
         for (Study study: studies) {
             assertNotNull(study.getId());
             assertNotNull(study.getContent());
            assertEquals(study.getOrganId(), ORRGAN_ID);
         }
     }
 
     @Test
     public void findTest() {
        Study study = service.find(STUDY_ID);
 
         assertNotNull(study);
        assertEquals(STUDY_ID, study.getId());
         assertNotNull(study.getContent());
         assertNotNull(study.getOrganId());
     }
 }
