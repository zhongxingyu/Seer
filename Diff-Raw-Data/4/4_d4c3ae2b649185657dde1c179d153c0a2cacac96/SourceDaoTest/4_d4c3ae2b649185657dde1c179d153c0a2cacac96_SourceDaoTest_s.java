 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
 
 import java.util.List;
 
 public class SourceDaoTest extends DaoTestCase {
     private SourceDao dao = (SourceDao) getApplicationContext().getBean("sourceDao");
 
     public void testGetById() throws Exception {
         Source source = dao.getById(-1);
         assertEquals("Wrong source name", "ICD-9", source.getName());
         assertEquals("Wrong number of activities", 2, source.getActivities().size());
         assertEquals("Wrong activity name", "Screening Activity", source.getActivities().get(0).getName());
         assertEquals("Wrong activity name", "Administer Drug Z" , source.getActivities().get(1).getName());
     }
 
     public void testGetAll() throws Exception {
         List<Source> sources = dao.getAll();
         assertEquals("Wrong size", 2, sources.size());
         assertEquals("Wrong name", "Empty", sources.get(0).getName());
         assertEquals("Wrong name", "ICD-9", sources.get(1).getName());
     }
 
     public void testGetManualTargetSource() throws Exception {
         Source source = dao.getManualTargetSource();
         assertTrue("Wrong Manual Target Flag", source.getManualFlag());
         assertEquals("Wrong source name", "ICD-9", source.getName());
     }
 
     public void testCount() throws Exception {
         assertEquals("Should be two sources to start", 2, dao.getCount());
 
         Source newSource = new Source();
         newSource.setName("newSource");
         dao.save(newSource);
         interruptSession();
         assertEquals("Should be three sources after saving", 3 ,dao.getCount());
 
        getJdbcTemplate().update("DELETE FROM sources");
        assertEquals("And now there should be none", 0, dao.getCount());
     }
 }
