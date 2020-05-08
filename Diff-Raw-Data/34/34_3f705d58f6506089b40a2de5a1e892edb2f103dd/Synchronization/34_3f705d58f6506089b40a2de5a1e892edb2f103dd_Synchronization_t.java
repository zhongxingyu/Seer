 package pl.psnc.dl.wf4ever.monitoring;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
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
 import se.kb.oai.pmh.Header;
 import se.kb.oai.pmh.IdentifiersList;
 import se.kb.oai.pmh.OaiPmhServer;
 
 /**
  * Util class for synchronization of rodl with external services.
  * 
  * @author pejot
  * 
  */
 public final class Synchronization {
 
     /** dArceo OAI service uri. */
     private static String darceoOai = null;
 
     /** ROs builder. */
     private static Builder builder = null;
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(Synchronization.class);
 
 
     /**
      * Hidden constructor.
      */
     private Synchronization() {
         //nope
     }
 
 
     /**
      * Synchronize status of object to store/update/delete them in dArceo.
      * 
      * @throws DArceoException .
      * @throws IOException .
      * @throws OAIException .
      */
     public static void dArceo()
             throws DArceoException, IOException, OAIException {
         //initiate truststore and keystore
         DArceoClient.getInstance();
         if (darceoOai == null) {
             if (DArceoClient.getInstance().getServiceUri() != null) {
                 darceoOai = DArceoClient.getInstance().getServiceUri().resolve("../oai-pmh").toString();
             } else {
                 LOGGER.warn("Synchronization with dArceo has been turned off in the connection.properties file.");
                 return;
             }
         }
         OaiPmhServer server = new OaiPmhServer(darceoOai);
         boolean more = true;
         ArrayList<URI> dArceoROsUri = new ArrayList<>();
        IdentifiersList list = server.listIdentifiers("METS");
         //generate the list of stored uris
         while (more) {
             for (Header header : list.asList()) {
                 try {
                     server.getRecord(header.getIdentifier(), "METS");
                     dArceoROsUri.add(URI.create(header.getIdentifier()));
                 } catch (OAIException e) {
                     //it means record was deleted, don't log it. it's fine.
                 }
             }
             if (list.getResumptionToken() != null) {
                 list = server.listIdentifiers(list.getResumptionToken());
             } else {
                 more = false;
             }
         }
         if (builder == null) {
             //FIXME RODL URI should be better
             UserMetadata userMetadata = new UserMetadata("rodl", "RODL decay monitor", Role.ADMIN, URI.create("rodl"));
             builder = new Builder(userMetadata);
         }
         Set<ResearchObject> tripleStoreROs = ResearchObject.getAll(builder, null);
 
         boolean started = !HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive();
         if (started) {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
         }
         try {
             ResearchObjectPreservationStatusDAO statusDao = new ResearchObjectPreservationStatusDAO();
             //if ro is in the triple store but it isn't not in the dArceo should have status NEW.
             //if ro is in the darceo but it isn't in the the rodl should have status DELETED.
             // check if all of them are in dArceo
             for (ResearchObject rodlRO : tripleStoreROs) {
                 if (!dArceoROsUri.contains(rodlRO.getUri())) {
                     ResearchObjectPreservationStatus status = statusDao.findById(rodlRO.getUri().toString());
                     if (status == null) {
                         LOGGER.warn("Status of " + rodlRO.getUri().toString()
                                 + "is missing. Created as NEW to synchronize with dArceo");
                         ResearchObjectPreservationStatus newStatus = new ResearchObjectPreservationStatus(
                                 rodlRO.getUri(), Status.NEW);
                         statusDao.save(newStatus);
                     } else if (status.getStatus() == null || status.getStatus() != Status.NEW) {
                         LOGGER.warn("Status of " + rodlRO.getUri().toString()
                                 + "isn'corrected. Created as NEW to synchronize with dArceo");
                         status.setStatus(Status.NEW);
                         statusDao.save(status);
                     }
                 }
             }
             for (URI darceoROUri : dArceoROsUri) {
                 if (ResearchObject.get(builder, darceoROUri) == null) {
                     ResearchObjectPreservationStatus status = statusDao.findById(darceoROUri.toString());
                     if (status == null) {
                         LOGGER.warn("Research Object " + darceoROUri.toString() + "needs to be restored");
                         ResearchObjectPreservationStatus newStatus = new ResearchObjectPreservationStatus(darceoROUri,
                                 Status.LOST);
                         statusDao.save(newStatus);
                     } else if (status.getStatus() == null || status.getStatus() != Status.DELETED) {
                         LOGGER.warn("Research Object " + darceoROUri.toString() + "needs to be restored");
                         status.setStatus(Status.LOST);
                         statusDao.save(status);
                     }
                 }
             }
         } finally {
             if (started) {
                 HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
             }
         }
     }
 
 
     /**
      * Synchronize status of object to store/update/delete them in dArceo and initiate service Uri.
      * 
      * @param serverUri
      *            dArceo intance Uri
      * @param builder
      *            given builder
      * @throws OAIException .
      * @throws DArceoException .
      * @throws IOException .
      */
     public static void dArceo(String serverUri, Builder builder)
             throws DArceoException, IOException, OAIException {
         if (serverUri == null && darceoOai == null) {
             darceoOai = DArceoClient.getInstance().getServiceUri().resolve("../oai-pmh").toString();
         } else {
             darceoOai = serverUri;
         }
         if (builder != null) {
             Synchronization.builder = builder;
         }
 
         dArceo();
     }
 }
