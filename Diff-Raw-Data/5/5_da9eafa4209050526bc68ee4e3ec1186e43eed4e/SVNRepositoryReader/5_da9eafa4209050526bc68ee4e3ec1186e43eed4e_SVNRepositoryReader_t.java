 /**
  * Copyright (C) 2012 Tim Besard <tim.besard@gmail.com>
  *
  * All rights reserved.
  */
 
 package be.mira.codri.server.bo.repository.readers;
 
 import be.mira.codri.server.bo.Repository;
 import be.mira.codri.server.bo.repository.processors.ConfigurationProcessor;
 import be.mira.codri.server.exceptions.RepositoryException;
 import be.mira.codri.server.bo.repository.RepositoryEntity;
 import be.mira.codri.server.bo.repository.RepositoryReader;
 import be.mira.codri.server.bo.repository.entities.Configuration;
 import be.mira.codri.server.bo.repository.entities.Connection;
 import be.mira.codri.server.bo.repository.entities.Presentation;
 import be.mira.codri.server.bo.repository.processors.ConnectionProcessor;
 import be.mira.codri.server.spring.Slf4jLogger;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.annotation.PostConstruct;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.tigris.subversion.javahl.ClientException;
 import org.tigris.subversion.javahl.Depth;
 import org.tigris.subversion.javahl.Info2;
 import org.tigris.subversion.javahl.InfoCallback;
 import org.tigris.subversion.javahl.Revision;
 import org.tigris.subversion.javahl.SVNClient;
 
 /**
  *
  * @author tim
  */
 public abstract class SVNRepositoryReader extends RepositoryReader implements ApplicationContextAware {
     //
     // Data members
     //
     
     @Slf4jLogger
     private Logger mLogger;
     
     private File mCheckout;
     
     private SVNClient mClient;
     
     private long mConnectionsRevision;
     private long mConfigurationsRevision;
     private long mPresentationsRevision;
     
     private ApplicationContext mApplicationContext;
 
 
     //
     // Construction and destruction
     //
     
     @Autowired
     public SVNRepositoryReader(final Repository iRepository) {
         super(iRepository);    
     }
     
     @Required
     public final void setCheckout(final File iCheckout) {
         mCheckout = iCheckout;
     }
 
     @PostConstruct
     public final void init() throws Exception {
         // Subversion checkout root
         if (!mCheckout.exists()) {
             mCheckout.mkdirs();
         }
         if (!mCheckout.exists() || !mCheckout.canWrite()) {
             throw new Exception("checkout path does not exist or is not writable");
         }
         
         // Subversion location
         Pattern tLocationPattern = Pattern.compile("^(https?|file|svn)://");
         Matcher tLocationMatcher = tLocationPattern.matcher(getRepository().getRoot());
         if (!tLocationMatcher.find()) {
             throw new Exception("repository location '" + getRepository().getRoot() + "' is not a valid URL");
         }
         mClient = new SVNClient();
         
         // TODO: remove
         checkout();
     }
 
     // FIXME: can't we instantiate prototype beans without being application context aware?
     @Override
     public final void setApplicationContext(final ApplicationContext iApplicationContext) {
         mApplicationContext = iApplicationContext;
     }
 
 
     //
     // Auxiliary classes
     //
 
     
     private class XMLFilter implements FilenameFilter {
       private Pattern mPattern = Pattern.compile("\\.xml$", Pattern.CASE_INSENSITIVE);
 
       @Override
       public final boolean accept(final File iDirectory, final String iFilename) {
           return mPattern.matcher(iFilename).find();
       }
     }
     
     
     //
     // Presentation helpers
     //
     
     @Override
     public final void checkout() throws RepositoryException {        
         // TODO: reduce to get() and mRevision = -1, no processing
         
         // Get the connections
         mLogger.debug("Checking out and processing the connections");
         mConnectionsRevision = getConnections();
         processConnections();
         
         // Get the configurations
         mLogger.debug("Checking out and processing the configurations");
         mConfigurationsRevision = getConfigurations();
         processConfigurations();
         
         // Get the media
         mLogger.debug("Processing the presentations");
         mPresentationsRevision = checkPresentations();
         processPresentations();  
     }
     
     @Override
     public final void update() throws RepositoryException {
         // TODO: squash these three cases in something using the 
         //       RepositoryEntity interface
         // Check the connections
         mLogger.debug("Checking the connections");
         long tConnectionsRevision = checkConnections();
         if (mConnectionsRevision != tConnectionsRevision) {
             mLogger.info("Connections changed to revision {}", tConnectionsRevision);
             mConnectionsRevision = tConnectionsRevision;
             getConnections();
             processConnections();
         }
 
         // Check the configurations
         mLogger.debug("Checking the configurations");
         long tConfigurationsRevision = checkConfigurations();
         if (mConfigurationsRevision != tConfigurationsRevision) {
             mLogger.info("Configurations changed to revision {}", tConfigurationsRevision);
             mConfigurationsRevision = tConfigurationsRevision;
             getConfigurations();
             processConfigurations();
         }
 
         // Check the presentations
         mLogger.debug("Checking the presentations");
         long tPresentationRevision = checkPresentations();
         if (mPresentationsRevision != tPresentationRevision) {
             mLogger.info("Presentations changed to revision {}", tPresentationRevision);
             mPresentationsRevision = tPresentationRevision;
             processPresentations();
         }
     }
     
     private long checkPresentations() throws RepositoryException {
         return getRevision(getRepository().getRoot() + "/presentations");
     }
     
     private void processPresentations() throws RepositoryException {        
         // List
         mLogger.debug("Listing presentations");
         Map<String, Presentation> tNewPresentations = new HashMap<String, Presentation>();
         Map<String, Long> tPathEntries = getChildrenRevisions(getRepository().getRoot() + "/presentations");
         for (Map.Entry<String, Long> tEntry : tPathEntries.entrySet()) {
             String tLocation = getRepository().getRoot() + "/presentations/" + tEntry.getKey();
             Presentation tPresentation = (Presentation) mApplicationContext.getBean("presentation", new Object[]{
                 tEntry.getValue(),
                 tLocation});
             tNewPresentations.put(tEntry.getKey(), tPresentation);
         }
         
         // Update
         mLogger.debug("Updating presentations");
         Repository tRepository = getRepository();
         RepositoryChangeset<Presentation> tChangeset = new RepositoryChangeset<Presentation>(tRepository.getPresentations(), tNewPresentations);
         for (Map.Entry<String, Presentation> tEntry : tChangeset.getRemovals().entrySet()) {
             tRepository.removePresentation(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Presentation> tEntry : tChangeset.getAdditions().entrySet()) {
             tRepository.addPresentation(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Presentation> tEntry : tChangeset.getUpdates().entrySet()) {
             tRepository.updatePresentation(tEntry.getKey(), tEntry.getValue());
         }
     }
     
     
     //
     // Configuration helpers
     //
     
     private long checkConfigurations() throws RepositoryException {
         return getRevision(getRepository().getRoot() + "/configurations");
     }
     
     private long getConfigurations() throws RepositoryException {
         // Get a local checkout and location
         final File tCheckout =  new File(mCheckout, "configurations");
         final String tLocation = getRepository().getRoot() + "/configurations";
         
         // Check if the repository exists and is valid
         Long tConfigurationRevision = null;
         try {
             tConfigurationRevision = getRevision(tCheckout);
         } catch (RepositoryException tException) {
             // Do nothing
         }
         
         // Checkout or update
         try {
             if (tConfigurationRevision == null) {
                 mLogger.trace("Fetching configurations");
                 if (tCheckout.exists()) {
                     FileUtils.cleanDirectory(tCheckout);
                 }
                 tConfigurationRevision = checkoutRepository(tCheckout, tLocation);            
             } else {
                 mLogger.trace("Updating configurations");
                 tConfigurationRevision = updateRepository(tCheckout);              
             }
         } catch (RepositoryException tException) {
             throw new RepositoryException("could not download the repository", tException);
         } catch (IOException tException) {
             throw new RepositoryException("could not clean the existing (and seemingly invalid) copy of the repository", tException);
         }
         
         return tConfigurationRevision;
     }
     
     private void processConfigurations() throws RepositoryException {        
         // Read
         mLogger.debug("Reading configurations");
         Map<String, Configuration> tNewConfigurations = new HashMap<String, Configuration>();
         File tDirectory = new File(mCheckout, "configurations");
         for (File tFile : tDirectory.listFiles(new XMLFilter())) {
             // Generate an identifier
             String tFilename = tFile.getName();
             mLogger.trace("Processing '{}'", tFile.getAbsoluteFile());
             int tDotPosition = tFilename.lastIndexOf('.');
             String tId = tFilename.substring(0, tDotPosition);
             final long tRevision = getRevision(tFile);
             
             // Process the contents
             String tLocation = getRepository().getRoot() + "/configurations/" + tFilename;
             ConfigurationProcessor tReader = createConfigurationProcessor();
             Configuration tConfiguration = tReader.process(tFile, tRevision, tLocation);
             if (tConfiguration == null) {
                 throw new RepositoryException("found empty configuration file");
             }
             tNewConfigurations.put(tId, tConfiguration);
         }
         
         // Save
         mLogger.debug("Saving configurations");
         Repository tRepository = getRepository();
         RepositoryChangeset<Configuration> tChangeset = new RepositoryChangeset<Configuration>(tRepository.getConfigurations(), tNewConfigurations);
         for (Map.Entry<String, Configuration> tEntry : tChangeset.getRemovals().entrySet()) {
             tRepository.removeConfiguration(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Configuration> tEntry : tChangeset.getAdditions().entrySet()) {
             tRepository.addConfiguration(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Configuration> tEntry : tChangeset.getUpdates().entrySet()) {
            tRepository.updateConfiguration(tEntry.getKey(), tEntry.getValue());
         }
     }
     
     
     //
     // Connection helpers
     //
     
     // TODO: Remove the quite identical Connection/Configuration/Presentation setters
     //       somehow make it using the RepositoryEntity interface
     
     private long checkConnections() throws RepositoryException {
         return getRevision(getRepository().getRoot() + "/connections");
     }
     
     private long getConnections() throws RepositoryException {
         // Get a local checkout and location
         final File tCheckout =  new File(mCheckout, "connections");
         final String tLocation = getRepository().getRoot() + "/connections";
         
         // Check if the repository exists and is valid
         Long tConnectionRevision = null;
         try {
             tConnectionRevision = getRevision(tCheckout);
         } catch (RepositoryException tException) {
             // Do nothing
         }
         
         // Checkout or update
         try {
             if (tConnectionRevision == null) {
                 mLogger.trace("Fetching connections");
                 if (tCheckout.exists()) {
                     FileUtils.cleanDirectory(tCheckout);
                 }
                 tConnectionRevision = checkoutRepository(tCheckout, tLocation);            
             } else {
                 mLogger.trace("Updating connections");
                 tConnectionRevision = updateRepository(tCheckout);              
             }
         } catch (RepositoryException tException) {
             throw new RepositoryException("could not download the repository", tException);
         } catch (IOException tException) {
             throw new RepositoryException("could not clean the existing (and seemingly invalid) copy of the repository", tException);
         }
         
         return tConnectionRevision;
     }
     
     private void processConnections() throws RepositoryException {        
         // Read
         mLogger.debug("Reading connections");
         Map<String, Connection> tNewConnections = new HashMap<String, Connection>();
         File tDirectory = new File(mCheckout, "connections");
         for (File tFile : tDirectory.listFiles(new XMLFilter())) {
             // Generate an identifier
             String tFilename = tFile.getName();
             mLogger.trace("Processing '{}'", tFile.getAbsoluteFile());
             int tDotPosition = tFilename.lastIndexOf('.');
             String tId = tFilename.substring(0, tDotPosition);
             final long tRevision = getRevision(tFile);
             
             // Process the contents
             String tLocation = getRepository().getRoot() + "/connections/" + tFilename;
             ConnectionProcessor tReader = createConnectionProcessor();
             Connection tConnection = tReader.process(tFile, tRevision, tLocation);
             if (tConnection == null) {
                 throw new RepositoryException("found empty connection file");
             }
             tNewConnections.put(tId, tConnection);
         }
         
         // Save
         mLogger.debug("Saving connections");
         Repository tRepository = getRepository();
         RepositoryChangeset<Connection> tChangeset = new RepositoryChangeset<Connection>(tRepository.getConnections(), tNewConnections);
         for (Map.Entry<String, Connection> tEntry : tChangeset.getRemovals().entrySet()) {
             tRepository.removeConnection(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Connection> tEntry : tChangeset.getAdditions().entrySet()) {
             tRepository.addConnection(tEntry.getKey(), tEntry.getValue());
         }
         for (Map.Entry<String, Connection> tEntry : tChangeset.getUpdates().entrySet()) {
            tRepository.updateConnection(tEntry.getKey(), tEntry.getValue());
         }
     }
 
 
     //
     // Auxiliary
     //
     
     private Long getRevision(final File iFile) throws RepositoryException {
         if (!iFile.exists()) {
             return null;
         }
         return getRevision(iFile.getAbsolutePath());
     }
     
     // TODO: case for an URL
     
     private Long getRevision(final String iPath) throws RepositoryException {
         try {
             final List<Long> tRevisions = new ArrayList<Long>();
             mClient.info2(
                     iPath,
                     Revision.HEAD,
                     Revision.HEAD,
                     Depth.empty,
                     null,
                     new InfoCallback() {
                         @Override
                         public void singleInfo(final Info2 iInfo) {
                             tRevisions.add(iInfo.getLastChangedRev());
                         }
                     });
             if (tRevisions.size() != 1) {
                 throw new RepositoryException("unexpected amount of info entries");
             }
             return tRevisions.get(0);
         } catch (ClientException tException) {
             throw new RepositoryException("could not check the repository", tException);
         }
     }
     
     private Map<String, Long> getChildrenRevisions(final File iFile) throws RepositoryException {
         if (!iFile.exists()) {
             return null;
         }
         return getChildrenRevisions(iFile.getAbsoluteFile());
     }
     
     // TODO: case for an URL
     
     private Map<String, Long> getChildrenRevisions(final String iPath) throws RepositoryException {
         try {
             final Map<String, Long> tChildren = new HashMap<String, Long>();
             mClient.info2(
                     iPath,
                     Revision.HEAD,
                     Revision.HEAD,
                     Depth.immediates,
                     null,
                     new InfoCallback() {
                         @Override
                         public void singleInfo(final Info2 iInfo) {
                             if (iInfo.getPath().equals(iPath)) {
                                 return;
                             }
                             tChildren.put(iInfo.getPath(), iInfo.getLastChangedRev());
                         }
                     });
             return tChildren;
         } catch (ClientException tException) {
             throw new RepositoryException("could not check the repository", tException);
         }
     }
 
     private long checkoutRepository(final File iCheckout, final String iLocation) throws RepositoryException {
         try {
             long tRevision = mClient.checkout(
                     iLocation,
                     iCheckout.getAbsolutePath(),
                     Revision.HEAD,
                     Revision.HEAD,
                     Depth.infinity,
                     false,
                     false);            
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not checkout the repository", tException);
         }
     }
     
     private long updateRepository(final File iCheckout) throws RepositoryException {
         try {
             long tRevision = mClient.update(
                     iCheckout.getAbsolutePath(),
                     Revision.HEAD,
                     Depth.infinity,
                     true,
                     false,
                     false);
             return tRevision;
         } catch (ClientException tException) {
             throw new RepositoryException("could not update the repository", tException);
         }
     }
     
     private class RepositoryChangeset<T extends RepositoryEntity> {
         //
         // Member data
         //
         
         private final Map<String, T> mAdditions;
         private final Map<String, T> mRemovals;
         private final Map<String, T> mUpdates;
         
         
         //
         // Construction and destruction
         //
         
         public RepositoryChangeset(final Map<String, T> iOldEntities, final Map<String, T> iCurrentEntities) {
             // Check for removed entities
             mRemovals = new HashMap<String, T>();
             for (Map.Entry<String, T> tOldEntry : iOldEntities.entrySet()) {
                 if (!iCurrentEntities.containsKey(tOldEntry.getKey())) {
                     mLogger.debug("Entity "
                             + tOldEntry.getKey()
                             + " seems to have been deleted (last known rev "
                             + tOldEntry.getValue().getRevision()
                             + "), removing from repository");
                     mRemovals.put(tOldEntry.getKey(), tOldEntry.getValue());
                 }
             }
 
             // Check for new and updated entities      
             mAdditions = new HashMap<String, T>();
             mUpdates = new HashMap<String, T>();
             for (Map.Entry<String, T> tCurrentEntry : iCurrentEntities.entrySet()) {
                 T tOldEntity = iOldEntities.get(tCurrentEntry.getKey());
                 if (tOldEntity == null) {
                     mLogger.debug("Entity "
                             + tCurrentEntry.getKey()
                             + " seems new (rev "
                             + tCurrentEntry.getValue().getRevision()
                             + "), adding to repository");
                     mAdditions.put(tCurrentEntry.getKey(),  tCurrentEntry.getValue());
                 } else if (tCurrentEntry.getValue().getRevision() > tOldEntity.getRevision()) {
                     mLogger.debug("Entity "
                             + tCurrentEntry.getKey()
                             + " is a more recent version (rev "
                             +  tCurrentEntry.getValue().getRevision()
                             + ") of an existing media (rev "
                             + tOldEntity.getRevision()
                             + "), updating the repository");
                     mUpdates.put(tCurrentEntry.getKey(),  tCurrentEntry.getValue());
                 }
             }
         }
         
         
         //
         // Getters and setters
         //
         
         public final Map<String, T> getAdditions() {
             return mAdditions;
         }
         
         public final Map<String, T> getRemovals() {
             return mRemovals;
         }
         
         public final Map<String, T> getUpdates() {
             return mUpdates;
         }
     }
     
     
     //
     // Prototype bean injectors
     //
     
     protected abstract ConnectionProcessor createConnectionProcessor();
     protected abstract ConfigurationProcessor createConfigurationProcessor();
 }
