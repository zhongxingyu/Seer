 package pl.psnc.dl.wf4ever.evo;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.URI;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.MediaType;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.common.EvoType;
 import pl.psnc.dl.wf4ever.evo.Job.State;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.sun.jersey.api.client.ClientResponse;
 
 public class JobTest extends EvoTest {
 
     @Override
     public void setUp()
             throws Exception {
         super.setUp();
     }
 
 
     @Override
     public void tearDown()
             throws Exception {
         super.tearDown();
     }
 
 
     @Test
     public final void testCopyJobCreation()
             throws InterruptedException {
         ClientResponse response = createCopyJob(new JobStatus(ro, EvoType.SNAPSHOT, true));
         URI copyJob = response.getLocation();
         assertEquals(response.getEntity(String.class), HttpServletResponse.SC_CREATED, response.getStatus());
         response.close();
         getRemoteStatus(copyJob, WAIT_FOR_COPY);
     }
 
 
     @Test
     public final void testCopyJobStatusDataIntegrity()
             throws InterruptedException {
         JobStatus status = new JobStatus(ro, EvoType.SNAPSHOT, true);
         status.setTarget("testTarget");
         URI copyJob = createCopyJob(status).getLocation();
         JobStatus remoteStatus = getRemoteStatus(copyJob, WAIT_FOR_COPY);
         String s = webResource.path(copyJob.getPath()).header("Authorization", "Bearer " + accessToken)
                 .accept(MediaType.APPLICATION_JSON).get(String.class);
         Assert.assertTrue(s.contains("testTarget"));
         Assert.assertEquals(status.getCopyfrom(), remoteStatus.getCopyfrom());
         Assert.assertEquals(status.getType(), remoteStatus.getType());
         Assert.assertEquals(status.isFinalize(), remoteStatus.isFinalize());
     }
 
 
     @Test
     public final void testJobFinalization()
             throws InterruptedException {
         URI copyJob = createCopyJob(new JobStatus(ro, EvoType.SNAPSHOT, false)).getLocation();
         JobStatus status = getRemoteStatus(copyJob, WAIT_FOR_COPY);
         JobStatus status2 = new JobStatus();
         status2.setTarget(status.getTarget());
        status2.setTarget(status.getCopyfrom().resolve("../" + status.getTarget() + "/").toString());
        System.out.println(status2.getTarget());
         URI finalizeJob = createFinalizeJob(status2).getLocation();
         JobStatus remoteStatus = getRemoteStatus(finalizeJob, WAIT_FOR_FINALIZE);
         Assert.assertEquals(State.DONE, remoteStatus.getState());
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         URI snapshotURI = status.getCopyfrom().resolve("../" + status.getTarget() + "/");
         model.read(snapshotURI.toString());
         Individual snapshot = model.getIndividual(snapshotURI.toString());
         Assert.assertNotNull(snapshot);
         Resource creator = snapshot.getPropertyResourceValue(DCTerms.creator);
         Assert.assertNotNull(creator);
         Assert.assertEquals(userId, creator.getURI());
         Set<RDFNode> aggregated = model.listObjectsOfProperty(snapshot, ORE.aggregates).toSet();
         for (RDFNode resource : aggregated) {
             Assert.assertTrue(resource.isURIResource());
             creator = resource.as(Individual.class).getPropertyResourceValue(DCTerms.creator);
             if (creator != null) {
                 Assert.assertEquals(userId, creator.getURI());
             }
         }
     }
 
 
     @Test
     public final void testCopyAndFinalizationJob()
             throws InterruptedException {
         URI copyAndFinalizeJob = createCopyJob(new JobStatus(ro, EvoType.SNAPSHOT, true)).getLocation();
         JobStatus remoteStatus = getRemoteStatus(copyAndFinalizeJob, WAIT_FOR_COPY + WAIT_FOR_FINALIZE);
         Assert.assertEquals(remoteStatus.toString(), State.DONE, remoteStatus.getState());
         //OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         //model.read(status.getTarget().toString());
         //TODO verify correct finalized RO
     }
 
 }
