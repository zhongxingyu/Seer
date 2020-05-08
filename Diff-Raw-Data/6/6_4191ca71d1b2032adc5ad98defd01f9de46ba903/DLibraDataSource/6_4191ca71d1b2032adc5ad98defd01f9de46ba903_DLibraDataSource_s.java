 package pl.psnc.dl.wf4ever.dlibra.helpers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.List;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.ResourceInfo;
 import pl.psnc.dl.wf4ever.common.UserProfile;
 import pl.psnc.dl.wf4ever.common.UserProfile.Role;
 import pl.psnc.dl.wf4ever.dlibra.ConflictException;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dlibra.content.ContentServer;
 import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
 import pl.psnc.dlibra.metadata.DirectoryId;
 import pl.psnc.dlibra.metadata.EditionId;
 import pl.psnc.dlibra.metadata.LibCollectionId;
 import pl.psnc.dlibra.metadata.MetadataServer;
 import pl.psnc.dlibra.metadata.PublicationId;
 import pl.psnc.dlibra.metadata.PublicationInfo;
 import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
 import pl.psnc.dlibra.mgmt.UnavailableServiceException;
 import pl.psnc.dlibra.mgmt.UserServiceResolver;
 import pl.psnc.dlibra.service.AccessDeniedException;
 import pl.psnc.dlibra.service.AuthorizationToken;
 import pl.psnc.dlibra.service.DLibraException;
 import pl.psnc.dlibra.service.IdNotFoundException;
 import pl.psnc.dlibra.service.ServiceUrl;
 import pl.psnc.dlibra.system.UserInterface;
 import pl.psnc.dlibra.user.User;
 import pl.psnc.dlibra.user.UserManager;
 
 import com.google.common.collect.Multimap;
 
 /**
  * Implementation of the digital library interface based on dLibra.
  * 
  * @author piotrhol
  * 
  */
 public class DLibraDataSource implements DigitalLibrary {
 
     /** logger. */
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(DLibraDataSource.class);
 
     public static final DirectoryId ROOT_DIRECTORY_ID = new DirectoryId(1L);
 
     public final static int BUFFER_SIZE = 4096;
 
     private final UserServiceResolver serviceResolver;
 
     private final String userLogin;
 
     private final ContentServer contentServer;
 
     private final UserManager userManager;
 
     private final MetadataServer metadataServer;
 
     private final UsersHelper usersHelper;
 
     private final PublicationsHelper publicationsHelper;
 
     private final EditionHelper editionHelper;
 
     private final FilesHelper filesHelper;
 
     private final AttributesHelper attributesHelper;
 
     private final DirectoryId workspacesContainerDirectoryId;
 
     private final LibCollectionId collectionId;
 
 
     /**
      * Constructor.
      * 
      * @param host
      *            dLibra server host
      * @param port
      *            dLibra server RMI port
      * @param workspacesContainerDirectoryId
      *            dLibra directory id in which all content is stored
      * @param collectionId
      *            id of collection that will have all published ROs
      * @param userLogin
      *            user login
      * @param password
      *            user password
      * @throws DigitalLibraryException
      *             internal dLibra error
      * @throws IOException
      *             error connecting to dLibra
      */
     public DLibraDataSource(String host, int port, long workspacesContainerDirectoryId, long collectionId,
             String userLogin, String password)
             throws DigitalLibraryException, IOException {
         try {
             AuthorizationToken authorizationToken = new AuthorizationToken(userLogin, password);
             serviceResolver = new UserServiceResolver(new ServiceUrl(InetAddress.getByName(host),
                     UserInterface.SERVICE_TYPE, port), authorizationToken);
 
             this.userLogin = userLogin;
             this.workspacesContainerDirectoryId = new DirectoryId(workspacesContainerDirectoryId);
             this.collectionId = new LibCollectionId(collectionId);
 
             metadataServer = DLStaticServiceResolver.getMetadataServer(serviceResolver, null);
             contentServer = DLStaticServiceResolver.getContentServer(serviceResolver, null);
             userManager = DLStaticServiceResolver.getUserServer(serviceResolver, null).getUserManager();
         } catch (DLibraException e) {
             throw new DigitalLibraryException(e);
         } catch (MalformedURLException | UnknownHostException e) {
             throw new IOException(e);
         }
 
         usersHelper = new UsersHelper(this);
         publicationsHelper = new PublicationsHelper(this);
         filesHelper = new FilesHelper(this);
         editionHelper = new EditionHelper(metadataServer.getPublicationManager());
         attributesHelper = new AttributesHelper(this);
     }
 
 
     UserServiceResolver getServiceResolver() {
         return serviceResolver;
     }
 
 
     ContentServer getContentServer() {
         return contentServer;
     }
 
 
     UserManager getUserManager() {
         return userManager;
     }
 
 
     MetadataServer getMetadataServer() {
         return metadataServer;
     }
 
 
     String getUserLogin() {
         return userLogin;
     }
 
 
     DirectoryId getWorkspacesContainerDirectoryId() {
         return workspacesContainerDirectoryId;
     }
 
 
     LibCollectionId getCollectionId() {
         return collectionId;
     }
 
 
     EditionHelper getEditionHelper() {
         return editionHelper;
     }
 
 
     @Override
     public UserProfile getUserProfile()
             throws DigitalLibraryException, NotFoundException {
         return getUserProfile(userLogin);
     }
 
 
     @Override
     public UserProfile getUserProfile(String login)
             throws DigitalLibraryException, NotFoundException {
         User user;
         try {
             user = userManager.getUserData(login);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
         // FIXME should be based on sth else than login
         UserProfile.Role role;
         if (login.equals("wfadmin")) {
             role = Role.ADMIN;
         } else if (login.equals("wf4ever_reader")) {
             role = Role.PUBLIC;
         } else {
             role = Role.AUTHENTICATED;
         }
         return new UserProfile(user.getLogin(), user.getName(), role);
     }
 
 
     @Override
     public List<String> getResourcePaths(ResearchObject ro, String folder)
             throws DigitalLibraryException, NotFoundException {
         try {
             return filesHelper.getFilePathsInFolder(ro, folder);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public InputStream getZippedFolder(ResearchObject ro, String folder)
             throws DigitalLibraryException, NotFoundException {
         try {
             return filesHelper.getZippedFolder(ro, folder);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public InputStream getFileContents(ResearchObject ro, String filePath)
             throws DigitalLibraryException, NotFoundException {
         try {
             return filesHelper.getFileContents(ro, filePath);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public boolean fileExists(ResearchObject ro, String filePath)
             throws DigitalLibraryException {
         try {
             return filesHelper.fileExists(ro, filePath);
         } catch (IdNotFoundException e) {
             return false;
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public ResourceInfo createOrUpdateFile(ResearchObject ro, String filePath, InputStream inputStream, String type)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         try {
             return filesHelper.createOrUpdateFile(ro, filePath, inputStream, type);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (AccessDeniedException e) {
             throw e;
         } catch (IOException | DLibraException | TransformerException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public ResourceInfo getFileInfo(ResearchObject ro, String filePath)
             throws NotFoundException, DigitalLibraryException, AccessDeniedException {
         try {
             return filesHelper.getFileInfo(ro, filePath);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (AccessDeniedException e) {
             throw e;
         } catch (IOException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public void deleteFile(ResearchObject ro, String filePath)
             throws DigitalLibraryException, NotFoundException {
         try {
             filesHelper.deleteFile(ro, filePath);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (IOException | DLibraException | TransformerException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public void createResearchObject(ResearchObject ro, InputStream mainFileContent, String mainFilePath,
             String mainFileMimeType)
             throws DigitalLibraryException, ConflictException {
         try {
             createWorkspaceGroupPublication(ro);
             createRoGroupPublication(ro);
             createVersionPublication(ro);
             createEdition(ro, mainFileContent, mainFilePath, mainFileMimeType);
             publicationsHelper.publishPublication(ro);
         } catch (IOException | DLibraException | TransformerException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     private void createEdition(ResearchObject ro, InputStream mainFileContent, String mainFilePath,
             String mainFileMimeType)
             throws AccessDeniedException, IdNotFoundException, RemoteException, DLibraException, TransformerException,
             IOException {
         EditionId editionId = publicationsHelper.preparePublicationAsNew(ro.getId(), new PublicationId(
                 getDlROVersionId(ro)), mainFileContent, mainFilePath, mainFileMimeType);
         ro.setDlEditionId(editionId.getId());
         ro.save();
     }
 
 
     private void createVersionPublication(ResearchObject ro)
             throws RemoteException, DLibraException, IOException, TransformerException, ConflictException {
         if (getDlROVersionId(ro) == 0) {
             PublicationId verId = publicationsHelper.createVersionPublication(new PublicationId(getDlROId(ro)), "v1");
             ro.setDlROVersionId(verId.getId());
             ro.save();
         } else {
             throw new ConflictException(ro.getUri().toString());
         }
     }
 
 
     private void createRoGroupPublication(ResearchObject ro)
             throws RemoteException, DLibraException {
         if (getDlROId(ro) == 0) {
             PublicationId roId = publicationsHelper.createROGroupPublication(new PublicationId(ro.getDlWorkspaceId()),
                 ro.getId());
             ro.setDlROId(roId.getId());
             ro.save();
         }
     }
 
 
     private void createWorkspaceGroupPublication(ResearchObject ro)
             throws RemoteException, DLibraException, IdNotFoundException, AccessDeniedException,
             UnavailableServiceException {
         if (getDlWorkspaceId(ro) == 0) {
             PublicationId workspaceId = publicationsHelper.createWorkspaceGroupPublication("default");
             usersHelper.grantReadAccessToPublication(workspaceId);
             ro.setDlWorkspaceId(workspaceId.getId());
             ro.save();
         }
     }
 
 
     /**
      * Return workspace id loading it if necessary.
      * 
      * @param ro
      *            RO
      * @return dLibra workspace id
      * @throws DLibraException
      * @throws RemoteException
      */
     long getDlWorkspaceId(ResearchObject ro)
             throws RemoteException, DLibraException {
         if (ro.getDlWorkspaceId() == 0) {
             PublicationId publicationId = publicationsHelper.getGroupId("default");
             ro.setDlWorkspaceId(publicationId != null ? publicationId.getId() : 0);
             ro.save();
         }
         return ro.getDlWorkspaceId();
     }
 
 
     /**
      * Return RO id loading it if necessary.
      * 
      * @param ro
      *            RO
      * @return dLibra RO id
      * @throws DLibraException
      * @throws RemoteException
      */
     long getDlROId(ResearchObject ro)
             throws RemoteException, DLibraException {
         if (ro.getDlROId() == 0) {
             PublicationId publicationId = publicationsHelper.getPublicationId(new PublicationId(getDlWorkspaceId(ro)),
                 ro.getId());
             ro.setDlROId(publicationId != null ? publicationId.getId() : 0);
             ro.save();
         }
         return ro.getDlROId();
     }
 
 
     /**
      * Return RO version id loading it if necessary.
      * 
      * @param ro
      *            RO
      * @return dLibra RO version id or 0 if not found
      * @throws DLibraException
      * @throws RemoteException
      */
     long getDlROVersionId(ResearchObject ro)
             throws RemoteException, DLibraException {
         if (ro.getDlROVersionId() == 0) {
             PublicationId roId = new PublicationId(getDlROId(ro));
             PublicationId publicationId = publicationsHelper.getPublicationId(roId, "v1");
             ro.setDlROVersionId(publicationId != null ? publicationId.getId() : 0);
             ro.save();
         }
         return ro.getDlROVersionId();
     }
 
 
     /**
      * Return RO version last edition id loading it if necessary.
      * 
      * @param ro
      *            RO
      * @return dLibra RO version last edition id or 0 if not found
      * @throws DLibraException
      * @throws RemoteException
      */
     long getDlEditionId(ResearchObject ro)
             throws RemoteException, DLibraException {
         if (ro.getDlEditionId() == 0) {
             long versionIdLong = getDlROVersionId(ro);
            EditionId editionId = (EditionId) editionHelper.getLastEdition(new PublicationId(versionIdLong)).getId();
            ro.setDlEditionId(editionId != null ? editionId.getId() : 0);
             ro.save();
         }
         return ro.getDlEditionId();
     }
 
 
     @Override
     public void deleteResearchObject(ResearchObject ro)
             throws DigitalLibraryException, NotFoundException {
         try {
             publicationsHelper.deleteVersionPublication(ro);
             List<PublicationInfo> vers = publicationsHelper.listPublicationsInROGroupPublication(new PublicationId(ro
                     .getDlROVersionId()));
             if (vers.isEmpty()) {
                 publicationsHelper.deleteGroupPublication(new PublicationId(ro.getDlROId()));
                 List<AbstractPublicationInfo> ros = publicationsHelper.listROGroupPublications();
                 if (ros.isEmpty()) {
                     publicationsHelper.deleteGroupPublication(new PublicationId(ro.getDlWorkspaceId()));
                 }
             }
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (IOException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
 
     }
 
 
     @Override
     public boolean createUser(String userId, String password, String username)
             throws DigitalLibraryException {
         try {
             return usersHelper.createUser(userId, password, username);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public boolean userExists(String userId)
             throws DigitalLibraryException {
         try {
             return usersHelper.userExists(userId);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public void deleteUser(String userId)
             throws DigitalLibraryException, NotFoundException {
         try {
             usersHelper.deleteUser(userId);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public InputStream getZippedResearchObject(ResearchObject ro)
             throws DigitalLibraryException, NotFoundException {
         try {
             return filesHelper.getZippedFolder(ro, null);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
     }
 
 
     @Override
     public void storeAttributes(ResearchObject ro, Multimap<URI, Object> roAttributes)
             throws NotFoundException, DigitalLibraryException {
         try {
             attributesHelper.storeAttributes(ro, roAttributes);
         } catch (IdNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RemoteException | DLibraException e) {
             throw new DigitalLibraryException(e);
         }
 
     }
 
 }
