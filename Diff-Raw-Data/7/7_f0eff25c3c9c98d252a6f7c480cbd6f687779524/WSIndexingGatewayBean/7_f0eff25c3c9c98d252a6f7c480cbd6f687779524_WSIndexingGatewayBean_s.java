 package org.nuxeo.ecm.platform.indexing.gateway.ws;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 import javax.jws.soap.SOAPBinding.Style;
 import javax.naming.NamingException;
 
 import org.jboss.annotation.ejb.SerializedConcurrentAccess;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
 import org.nuxeo.ecm.core.api.security.ACL;
 import org.nuxeo.ecm.core.api.security.ACP;
 import org.nuxeo.ecm.platform.api.ws.DocumentBlob;
 import org.nuxeo.ecm.platform.api.ws.DocumentDescriptor;
 import org.nuxeo.ecm.platform.api.ws.DocumentProperty;
 import org.nuxeo.ecm.platform.api.ws.DocumentSnapshot;
 import org.nuxeo.ecm.platform.api.ws.NuxeoRemoting;
 import org.nuxeo.ecm.platform.api.ws.WsACE;
 import org.nuxeo.ecm.platform.api.ws.session.WSRemotingSession;
 import org.nuxeo.ecm.platform.audit.api.AuditException;
 import org.nuxeo.ecm.platform.audit.ws.EventDescriptorPage;
 import org.nuxeo.ecm.platform.audit.ws.ModifiedDocumentDescriptor;
 import org.nuxeo.ecm.platform.audit.ws.ModifiedDocumentDescriptorPage;
 import org.nuxeo.ecm.platform.audit.ws.api.WSAudit;
 import org.nuxeo.ecm.platform.audit.ws.delegate.WSAuditBeanBusinessDelegate;
 import org.nuxeo.ecm.platform.indexing.gateway.adapter.IndexingAdapter;
 import org.nuxeo.ecm.platform.indexing.gateway.ws.api.WSIndexingGateway;
 import org.nuxeo.ecm.platform.ws.AbstractNuxeoWebService;
 import org.nuxeo.ecm.platform.ws.delegate.NuxeoRemotingBeanBusinessDelegate;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  *
  * Base class for WS beans used for external indexers. Implements most of
  * NuxeoRemotingBean trying as hard as possible no to throw ClientException when
  * a requested document is missing but returning empty descriptions instead so
  * as to make external indexers not view recently deleted documents as
  * applicative errors.
  *
  * @author tiry
  *
  */
 @Stateless
 @SerializedConcurrentAccess
 @WebService(name = "WSIndexingGatewayInterface", serviceName = "WSIndexingGatewayService")
 @SOAPBinding(style = Style.DOCUMENT)
 public class WSIndexingGatewayBean extends AbstractNuxeoWebService implements
         WSIndexingGateway {
 
     private static final long serialVersionUID = 4696352633818100451L;
 
     protected final WSAuditBeanBusinessDelegate auditBeanDelegate = new WSAuditBeanBusinessDelegate();
 
     protected transient WSAudit auditBean;
 
     protected final NuxeoRemotingBeanBusinessDelegate platformBeanDelegate = new NuxeoRemotingBeanBusinessDelegate();
 
     protected transient NuxeoRemoting platformRemoting;
 
     protected IndexingAdapter adapter;
 
     protected WSAudit getWSAudit() throws AuditException {
         if (auditBean == null) {
             try {
                 auditBean = auditBeanDelegate.getWSAuditRemote();
             } catch (NamingException ne) {
                 throw new AuditException("Cannot find audit bean...");
             }
         }
         if (auditBean == null) {
             throw new AuditException("Cannot find audit bean...");
         }
         return auditBean;
     }
 
     protected NuxeoRemoting getWSNuxeoRemoting() throws ClientException {
         if (platformRemoting == null) {
             try {
                 platformRemoting = platformBeanDelegate.getWSNuxeoRemotingRemote();
             } catch (NamingException ne) {
                 throw new ClientException("Cannot find nuxeo remoting bean...");
             }
         }
 
         if (platformRemoting == null) {
             throw new ClientException("Cannot find nuxeo remoting bean...");
         }
         return platformRemoting;
     }
 
     protected IndexingAdapter getAdapter() throws ClientException {
         if (adapter == null) {
             adapter = Framework.getLocalService(IndexingAdapter.class);
             if (adapter == null) {
                 throw new ClientException(
                         "could not find IntuitionAdapterService");
             }
         }
         return adapter;
     }
 
     @WebMethod
     public DocumentDescriptor[] getChildren(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "uuid")
     String uuid) throws ClientException {
         CoreSession session = initSession(sessionId).getDocumentManager();
         if (session.exists(new IdRef(uuid))) {
             return getWSNuxeoRemoting().getChildren(sessionId, uuid);
         } else {
             return new DocumentDescriptor[0];
         }
     }
 
     @WebMethod
     public DocumentDescriptor getCurrentVersion(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uid) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         if (session.exists(new IdRef(uid))) {
             return getWSNuxeoRemoting().getCurrentVersion(sid, uid);
         } else {
             return missingDocumentDescriptor(uid);
         }
     }
 
     @WebMethod
     public DocumentDescriptor getDocument(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "uuid")
     String uuid) throws ClientException {
         CoreSession session = initSession(sessionId).getDocumentManager();
         DocumentDescriptor dd;
         if (session.exists(new IdRef(uuid))) {
             dd = getWSNuxeoRemoting().getDocument(sessionId, uuid);
         } else {
             dd = missingDocumentDescriptor(uuid);
         }
         return getAdapter().adaptDocumentDescriptor(session, uuid, dd);
     }
 
     @WebMethod
     public  WsACE[] getDocumentACL(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uuid) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         WsACE[] aces;
         if (session.exists(new IdRef(uuid))) {
             aces = getWSNuxeoRemoting().getDocumentACL(sid, uuid);
         } else {
             aces = new  WsACE[0];
         }
         return getAdapter().adaptDocumentACL(session, uuid, aces);
     }
 
     @WebMethod
     public  WsACE[] getDocumentLocalACL(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uuid) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         WsACE[] aces;
         if (session.exists(new IdRef(uuid))) {
             aces = getWSNuxeoRemoting().getDocumentLocalACL(sid, uuid);
         } else {
             aces = new  WsACE[0];
         }
         return getAdapter().adaptDocumentLocalACL(session, uuid, aces);
     }
 
     public DocumentBlob[] getDocumentBlobsExt(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uuid, @WebParam(name = "useDownloadUrl")
     boolean useDownloadUrl) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         DocumentBlob[] blobs;
         if (session.exists(new IdRef(uuid))) {
             blobs = getWSNuxeoRemoting().getDocumentBlobsExt(sid, uuid,
                     useDownloadUrl);
         } else {
             blobs = new DocumentBlob[0];
         }
         return getAdapter().adaptDocumentBlobs(session, uuid, blobs);
     }
 
     @WebMethod
     public DocumentBlob[] getDocumentBlobs(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uuid) throws ClientException {
         return getDocumentBlobsExt(sid, uuid,
                 getAdapter().useDownloadUrlForBlob());
     }
 
     @WebMethod
     public DocumentProperty[] getDocumentNoBlobProperties(
             @WebParam(name = "sessionId")
             String sid, @WebParam(name = "uuid")
             String uuid) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         DocumentProperty[] properties;
         if (session.exists(new IdRef(uuid))) {
             properties = getWSNuxeoRemoting().getDocumentNoBlobProperties(sid,
                     uuid);
         } else {
             properties = new DocumentProperty[0];
         }
         return getAdapter().adaptDocumentNoBlobProperties(session, uuid,
                 properties);
     }
 
     @WebMethod
     public DocumentProperty[] getDocumentProperties(
             @WebParam(name = "sessionId")
             String sid, @WebParam(name = "uuid")
             String uuid) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         DocumentProperty[] properties;
         if (session.exists(new IdRef(uuid))) {
             properties = getWSNuxeoRemoting().getDocumentProperties(sid, uuid);
         } else {
             properties = new DocumentProperty[0];
         }
         return getAdapter().adaptDocumentProperties(session, uuid, properties);
     }
 
     @WebMethod
     public String[] getGroups(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "parentGroup")
     String parentGroup) throws ClientException {
         return getWSNuxeoRemoting().getGroups(sid, parentGroup);
     }
 
     @WebMethod
     public String getRepositoryName(@WebParam(name = "sessionId")
     String sid) throws ClientException {
         return getWSNuxeoRemoting().getRepositoryName(sid);
     }
 
     @WebMethod
     public DocumentDescriptor getRootDocument(@WebParam(name = "sessionId")
     String sessionId) throws ClientException {
         return getWSNuxeoRemoting().getRootDocument(sessionId);
     }
 
     @WebMethod
     public String resolvePathToUUID(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "path")
     String path) throws ClientException {
         CoreSession session = initSession(sessionId).getDocumentManager();
         if (session != null) {
             PathRef pathRef = new PathRef(path);
             if (session.exists(pathRef)) {
                 return session.getDocument(pathRef).getId();
             }
         }
         return null;
     }
 
     @WebMethod
     public DocumentDescriptor getDocumentFromPath(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "path")
     String path) throws ClientException {
         String uuid = resolvePathToUUID(sessionId, path);
         if (uuid != null) {
             return getWSNuxeoRemoting().getDocument(sessionId, uuid);
         } else {
             // should we return a missing document with an null uuid instead?
             return null;
         }
     }
 
     @WebMethod
     public DocumentDescriptor getSourceDocument(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uid) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         if (session.exists(new IdRef(uid))) {
             return getWSNuxeoRemoting().getSourceDocument(sid, uid);
         } else {
             return missingDocumentDescriptor(uid);
         }
     }
 
     @WebMethod
     public String[] getUsers(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "parentGroup")
     String parentGroup) throws ClientException {
         return getWSNuxeoRemoting().getUsers(sid, parentGroup);
     }
 
     @WebMethod
     public DocumentDescriptor[] getVersions(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uid) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         if (session.exists(new IdRef(uid))) {
             return getWSNuxeoRemoting().getVersions(sid, uid);
         } else {
             return new DocumentDescriptor[0];
         }
     }
 
     @WebMethod
     public String[] listGroups(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "from")
     int from, @WebParam(name = "to")
     int to) throws ClientException {
         return getWSNuxeoRemoting().listGroups(sid, from, to);
     }
 
     @WebMethod
     public String[] listUsers(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "from")
     int from, @WebParam(name = "to")
     int to) throws ClientException {
         return getWSNuxeoRemoting().listUsers(sid, from, to);
     }
 
     @WebMethod
     public ModifiedDocumentDescriptor[] listModifiedDocuments(
             @WebParam(name = "sessionId")
             String sessionId, @WebParam(name = "dateRangeQuery")
             String dateRangeQuery) throws AuditException {
         return getWSAudit().listModifiedDocuments(sessionId, dateRangeQuery);
     }
 
     @WebMethod
     public ModifiedDocumentDescriptorPage listModifiedDocumentsByPage(
             @WebParam(name = "sessionId")
             String sessionId, @WebParam(name = "dateRangeQuery")
             String dateRangeQuery, @WebParam(name = "path")
             String path, @WebParam(name = "page")
             int page, @WebParam(name = "pageSize")
             int pageSize) throws AuditException {
         return getWSAudit().listModifiedDocumentsByPage(sessionId,
                 dateRangeQuery, path, page, pageSize);
     }
 
     @WebMethod
     public EventDescriptorPage listEventsByPage(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "dateRangeQuery")
     String dateRangeQuery, @WebParam(name = "page")
     int page, @WebParam(name = "pageSize")
     int pageSize) throws AuditException {
         return getWSAudit().listEventsByPage(sessionId, dateRangeQuery, page,
                 pageSize);
     }
 
     @WebMethod
     public EventDescriptorPage listDocumentEventsByPage(
             @WebParam(name = "sessionId")
             String sessionId, @WebParam(name = "dateRangeQuery")
             String dateRangeQuery, @WebParam(name = "startDate")
             String startDate, @WebParam(name = "path")
             String path, @WebParam(name = "page")
             int page, @WebParam(name = "pageSize")
             int pageSize) throws AuditException {
         return getWSAudit().listDocumentEventsByPage(sessionId, dateRangeQuery,
                 startDate, path, page, pageSize);
     }
 
     @WebMethod
     public String getRelativePathAsString(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "uuid")
     String uuid) throws ClientException {
         CoreSession session = initSession(sessionId).getDocumentManager();
         if (session.exists(new IdRef(uuid))) {
             return getWSNuxeoRemoting().getRelativePathAsString(sessionId, uuid);
         } else {
             return null;
         }
     }
 
     @WebMethod
     public boolean hasPermission(@WebParam(name = "sessionId")
     String sid, @WebParam(name = "uuid")
     String uuid, @WebParam(name = "permission")
     String permission) throws ClientException {
         CoreSession session = initSession(sid).getDocumentManager();
         if (session.exists(new IdRef(uuid))) {
             return getWSNuxeoRemoting().hasPermission(sid, uuid, permission);
         } else {
             return false;
         }
     }
 
     @WebMethod
     public String uploadDocument(@WebParam(name = "sessionId")
     String sid, String path, String type, String[] properties)
             throws ClientException {
         return getWSNuxeoRemoting().uploadDocument(sid, path, type, properties);
     }
 
     @WebMethod
     public String connect(@WebParam(name = "userName")
     String username, @WebParam(name = "password")
     String password) throws ClientException {
         return getWSNuxeoRemoting().connect(username, password);
     }
 
     @WebMethod
     public void disconnect(@WebParam(name = "sessionId")
     String sid) throws ClientException {
         getWSNuxeoRemoting().disconnect(sid);
     }
 
     @WebMethod
     public EventDescriptorPage queryEventsByPage(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "whereClause")
     String whereClause, @WebParam(name = "pageIndex")
     int page, @WebParam(name = "pageSize")
     int pageSize) throws AuditException {
         return getWSAudit().queryEventsByPage(sessionId, whereClause, page,
                 pageSize);
     }
 
     @WebMethod
     public boolean validateUserPassword(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "username")
     String username, @WebParam(name = "password")
     String password) throws ClientException {
         WSRemotingSession rs = initSession(sessionId);
         return rs.getUserManager().checkUsernamePassword(username, password);
     }
 
     @WebMethod
     public String[] getUserGroups(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "username")
     String username) throws ClientException {
         WSRemotingSession rs = initSession(sessionId);
         List<String> groups = rs.getUserManager().getPrincipal(username).getAllGroups();
         String[] groupArray = new String[groups.size()];
         groups.toArray(groupArray);
         return groupArray;
     }
 
     public DocumentSnapshot getDocumentSnapshotExt(
             @WebParam(name = "sessionId")
             String sessionId, @WebParam(name = "uuid")
             String uuid, @WebParam(name = "useDownloadUrl")
             boolean useDownloadUrl) throws ClientException {
         WSRemotingSession rs = initSession(sessionId);
         DocumentModel doc = rs.getDocumentManager().getDocument(new IdRef(uuid));
 
         DocumentProperty[] props = getDocumentNoBlobProperties(sessionId, uuid);
         DocumentBlob[] blobs = getDocumentBlobs(sessionId, uuid);
 
         WsACE[] resACP = null;
 
         ACP acp = doc.getACP();
        if (acp != null) {
             ACL acl = acp.getMergedACLs("MergedACL");
            resACP = acl.toArray(new  WsACE[acl.size()]);
         }
         DocumentSnapshot ds = new DocumentSnapshot(props, blobs,
                 doc.getPathAsString(), resACP);
         return ds;
     }
 
     @WebMethod
     public DocumentSnapshot getDocumentSnapshot(@WebParam(name = "sessionId")
     String sessionId, @WebParam(name = "uuid")
     String uuid) throws ClientException {
         return getDocumentSnapshotExt(sessionId, uuid,
                 getAdapter().useDownloadUrlForBlob());
     }
 
     public ModifiedDocumentDescriptorPage listDeletedDocumentsByPage(
             @WebParam(name = "sessionId")
             String sessionId, @WebParam(name = "dataRangeQuery")
             String dateRangeQuery, @WebParam(name = "docPath")
             String path, @WebParam(name = "pageIndex")
             int page, @WebParam(name = "pageSize")
             int pageSize) throws AuditException {
 
         return getWSAudit().listDeletedDocumentsByPage(sessionId,
                 dateRangeQuery, path, page, pageSize);
     }
 
     /**
      * Utility method to build descriptor for a document that is non longer to
      * be found in the repository.
      *
      * @param uuid
      * @return
      */
     protected DocumentDescriptor missingDocumentDescriptor(String uuid) {
         // TODO: if we have to make the API / WSDL evolve it would be nice to
         // include an explicit attribute in DocumentDescriptor to mark missing
         // documents
         DocumentDescriptor dd = new DocumentDescriptor();
         dd.setUUID(uuid);
         return dd;
     }
 
 }
