 package pl.psnc.dl.wf4ever.monitoring;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Properties;
 
import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.darceo.client.DArceoClient;
 import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
 import pl.psnc.dl.wf4ever.db.dao.ResearchObjectPreservationStatusDAO;
 import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.preservation.ResearchObjectPreservationStatus;
 import pl.psnc.dl.wf4ever.preservation.Status;
 import se.kb.oai.OAIException;
 
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.DatasetFactory;
 
 public class SynchronizationTest {
 
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SynchronizationTest.class);

     static final String dArceoTestInstance = "https://calatola.man.poznan.pl:8181/zmd/oai-pmh";
     private Dataset dataset;
     private UserMetadata userProfile;
     private Builder builder;
     private ResearchObjectPreservationStatusDAO preservationDAO;
     private URI createdROUri = URI.create("http://ww.example.com/SynchronizationTestRO/");
 
 
     @Before
     public void setUp()
             throws IOException {
         Properties properties = new Properties();
         dataset = DatasetFactory.createMem();
         userProfile = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED, URI.create("http://jank"));
         builder = new Builder(userProfile, dataset, false);
         preservationDAO = new ResearchObjectPreservationStatusDAO();
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
     }
 
 
     @After
     public void tearDown() {
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
     }
 
 
     @Test
     public void testDarceoSynchronization()
             throws DArceoException, IOException, OAIException {
         ResearchObject createdRO = ResearchObject.create(builder, createdROUri);
         ResearchObjectPreservationStatus status = preservationDAO.findById(createdRO.getUri().toString());
         status.setStatus(Status.UP_TO_DATE);
         preservationDAO.save(status);
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
         Assert.assertEquals(Status.UP_TO_DATE, preservationDAO.findById(createdRO.getUri().toString()).getStatus());
        try {
            Synchronization.dArceo(dArceoTestInstance, builder);
        } catch (OAIException e) {
            if (e.getMessage().contains("Connection timed out")) {
                LOGGER.warn("Can't connect to dArceo, maybe it is blocked from this network.", e);
                return;
            } else {
                throw e;
            }
        }
         Assert.assertEquals(Status.NEW, preservationDAO.findById(createdRO.getUri().toString()).getStatus());
         preservationDAO.delete(preservationDAO.findById(createdRO.getUri().toString()));
         createdRO.delete();
     }
 
 
     @Test
     public void testRodlRestoreInCaseOfMissingRecord()
             throws IOException, DArceoException, OAIException {
         Properties properties = new Properties();
         properties.load(SynchronizationTest.class.getClassLoader().getResourceAsStream("connection.properties"));
         if (properties.getProperty("repository_url") == null || properties.getProperty("repository_url").equals("")) {
             System.out.println("repository url not specified SynchronizationTest skipped");
             return;
         }
         ResearchObject createdOnlyInDArceo = ResearchObject.create(builder,
             URI.create("http://www.example.com/createdOnlyInDArceo/"));
         DArceoClient.getInstance().postORUpdateBlocking(DArceoClient.getInstance().post(createdOnlyInDArceo));
         ResearchObjectPreservationStatus status = preservationDAO.findById(createdOnlyInDArceo.getUri().toString());
         preservationDAO.delete(status);
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
         Assert.assertNull(preservationDAO.findById(createdOnlyInDArceo.getUri().toString()));
         Synchronization.dArceo();
         Assert.assertEquals(Status.LOST, preservationDAO.findById(createdOnlyInDArceo.getUri().toString()).getStatus());
         preservationDAO.delete(preservationDAO.findById(createdOnlyInDArceo.getUri().toString()));
         DArceoClient.getInstance().delete(createdOnlyInDArceo.getUri());
     }
 
 
     @Test
     public void testRodlRestoreInCaseOfWrongRecord()
             throws IOException, DArceoException, OAIException {
         Properties properties = new Properties();
         properties.load(SynchronizationTest.class.getClassLoader().getResourceAsStream("connection.properties"));
         if (properties.getProperty("repository_url") == null || properties.getProperty("repository_url").equals("")) {
             System.out.println("repository url not specified SynchronizationTest skipped");
             return;
         }
         ResearchObject createdOnlyInDArceo = ResearchObject.create(builder,
             URI.create("http://www.example.com/createdOnlyInDArceo/"));
         DArceoClient.getInstance().postORUpdateBlocking(DArceoClient.getInstance().post(createdOnlyInDArceo));
         ResearchObjectPreservationStatus status = preservationDAO.findById(createdOnlyInDArceo.getUri().toString());
         status.setStatus(Status.UP_TO_DATE);
         preservationDAO.save(status);
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
         Synchronization.dArceo();
         Assert.assertEquals(Status.LOST, preservationDAO.findById(createdOnlyInDArceo.getUri().toString()).getStatus());
         preservationDAO.delete(preservationDAO.findById(createdOnlyInDArceo.getUri().toString()));
         DArceoClient.getInstance().delete(createdOnlyInDArceo.getUri());
     }
 }
