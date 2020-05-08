 package de.escidoc.core.purge.internal;
 
 import de.escidoc.core.adm.business.admin.PurgeStatus;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.purge.PurgeRequest;
 import de.escidoc.core.purge.PurgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.escidoc.core.services.fedora.FedoraServiceClient;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 /**
  * Default implementation of {@link PurgeService}.
  */
 @Service("de.escidoc.core.purge.internal.PurgeServiceImpl")
 public class PurgeServiceImpl implements PurgeService {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeServiceImpl.class);
 
     @Autowired
     private FedoraServiceClient fedoraServiceClient;
 
     @Autowired
     private TripleStoreUtility tripleStoreUtility;
 
     @Autowired
     @Qualifier("admin.PurgeStatus")
     private PurgeStatus purgeStatus;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected PurgeServiceImpl() {
     }
 
     @Override
     public void purge(final PurgeRequest purgeRequest) {
         // TODO: Refector this old code.
         try {
             try {
                 final boolean isInternalUser = UserContext.isInternalUser();
 
                 if (!isInternalUser) {
                     UserContext.setUserContext("");
                     UserContext.runAsInternalUser();
                 }
             }
             catch (final Exception e) {
                 if (LOGGER.isWarnEnabled()) {
                     LOGGER.warn("Error on setting user context.");
                 }
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Error on setting user context.", e);
                 }
                 UserContext.setUserContext("");
                 UserContext.runAsInternalUser();
             }
             for (final String componentId : this.tripleStoreUtility.getComponents(purgeRequest.getResourceId())) {
                 this.fedoraServiceClient.deleteObject(componentId);
             }
             this.fedoraServiceClient.deleteObject(purgeRequest.getResourceId());
             // synchronize triple store
             this.fedoraServiceClient.sync();
             try {
                 this.tripleStoreUtility.reinitialize();
             }
             catch (TripleStoreSystemException e) {
                 throw new FedoraSystemException("Error on reinitializing triple store.", e);
             }
         }
         catch (final Exception e) {
             LOGGER.error("could not dequeue message", e);
         }
         finally {
             purgeStatus.dec();
         }
     }
 }
