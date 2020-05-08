 package safran;
 
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.dom4j.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.global.Constants;
 import utils.ParamParser;
 
 import java.io.*;
 import java.util.*;
 
 public class Client {
 
     Integer reconnects = 0;
     HttpClient httpclient = new HttpClient();
     Properties props = new Properties();
     protected String sessionTicket;
     String username;
     String password;
     String machine = "localhost";
     String repository = "cmn_test";
     String url_server = "http://localhost:8080/cinnamon/cinnamon";
 
     private transient Logger log = LoggerFactory.getLogger(this.getClass());
 
     public Client() {
         initializeHttpClient(url_server);
     }
 
     /**
      * Create a new Client object which is identical to the old one - except
      * for it's session and HTTP-client. If the original client's session was not null,
      * forkSession is called to generate a unique session for this client object.
      *
      * @param oldClient the client object to copy.
      */
     public Client(Client oldClient) {
         username = oldClient.getUsername();
         password = oldClient.getPassword();
         machine = oldClient.getMachine();
         repository = oldClient.getRepository();
         url_server = oldClient.url_server;
         props = oldClient.getProps();
         initializeHttpClient(url_server);
         if (oldClient.getSessionTicket() != null) {
             sessionTicket = oldClient.getSessionTicket();
             forkSession();
         }
     }
 
     public Client(String url, String username, String password) {
         url_server = url;
         this.username = username;
         this.password = password;
         initializeHttpClient(url_server);
     }
 
     public Client(String url, String username, String password, String repository) {
         url_server = url;
         this.username = username;
         this.password = password;
         this.repository = repository;
         initializeHttpClient(url_server);
     }
 
     void initializeHttpClient(String url) {
         try {
             HostConfiguration hostConfiguration = new HostConfiguration();
             hostConfiguration.setHost(new URI(url, false));
             HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
             connectionParams.setMaxConnectionsPerHost(hostConfiguration, 10);
             MultiThreadedHttpConnectionManager connectionManager =
                     new MultiThreadedHttpConnectionManager();
             connectionManager.setParams(connectionParams);
             httpclient = new HttpClient(connectionManager);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Create a Client object with parameters found in a properties object.
      *
      * @param props properties:
      *              <ul>
      *              <li>server.url</li>
      *              <li>server.username</li>
      *              <li>server.password</li>
      *              <li>default_repository</li>
      *              </ul>
      */
     public Client(Properties props) {
         this(
                 props.getProperty("server.url"),
                 props.getProperty("server.username"),
                 props.getProperty("server.password"),
                 props.getProperty("default_repository")
         );
         this.props = props;
     }
 
     public Client(String username, String password) {
         this.username = username;
         this.password = password;
         initializeHttpClient(url_server);
     }
 
     public static boolean checkBooleanResponse(String result, String msg) {
         if (result.length() == 0) {
             LoggerFactory.getLogger(Client.class).debug("result of length 0");
             return false;
         }
         if (result.charAt(0) == 'F') {
             return false;
         }
         else if (result.charAt(0) == 'T') {
             return true;
         }
         else {
             throw new RuntimeException(msg);
         }
     }
 
     public static Long parseId(String id) {
         if (id.trim().length() == 0) {
             return 0L;
         }
 
         return Long.parseLong(id.trim());
     }
 
     public Boolean initializeDatabase(String repository) {
         Part[] parts = {
                 new StringPart("command", "initializedatabase"),
                 new StringPart("repository", repository),
         };
 
         String result = executeMethod(parts);
         return result.contains("Initialization completed.");
     }
 
     public Boolean initializeComponent(String apiMethod) {
         Part[] parts = {
                 new StringPart("command", apiMethod),
                 new StringPart("ticket", sessionTicket),
         };
 
         String result = executeMethod(parts);
         return result.contains("Initialization completed.");
     }
 
     public String listLanguages() {
         Part[] parts = {
                 new StringPart("command", "listlanguages"),
                 new StringPart("ticket", sessionTicket),
         };
 
         return executeMethod(parts);
     }
 
     public String listMessages() {
         Part[] parts = {
                 new StringPart("command", "listmessages"),
                 new StringPart("ticket", sessionTicket),
         };
 
         return executeMethod(parts);
     }
 
     public String listUiLanguages() {
         Part[] parts = {
                 new StringPart("command", "listuilanguages"),
                 new StringPart("ticket", sessionTicket),
         };
 
         return executeMethod(parts);
     }
 
     public String listLifeCycles() {
         Part[] parts = {
                 new StringPart("command", "listlifecycles"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getLifeCycleByName(String lifeCycleName) {
         Part[] parts = {
                 new StringPart("command", "getlifecycle"),
                 new StringPart("name", lifeCycleName),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getLifeCycleById(Long id) {
         Part[] parts = {
                 new StringPart("command", "getlifecycle"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getLifeCycleState(Long id) {
         Part[] parts = {
                 new StringPart("command", "getlifecyclestate"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String attachLifeCycle(Long osdId, Long lifeCycleId) {
         Part[] parts = {
                 new StringPart("command", "attachlifecycle"),
                 new StringPart("id", osdId.toString()),
                 new StringPart("lifecycle_id", lifeCycleId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String changeLifeCycleState(Long osdId, Long stateId) {
         Part[] parts = {
                 new StringPart("command", "changestate"),
                 new StringPart("id", osdId.toString()),
                 new StringPart("lifecycle_state_id", stateId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String changeLifeCycleState(Long osdId, String stateName) {
         Part[] parts = {
                 new StringPart("command", "changestate"),
                 new StringPart("id", osdId.toString()),
                 new StringPart("state_name", stateName),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getNextStates(Long osdId) {
         Part[] parts = {
                 new StringPart("command", "getnextstates"),
                 new StringPart("id", osdId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String attachLifeCycle(Long osdId, Long lifeCycleId, Long state) {
         Part[] parts = {
                 new StringPart("command", "attachlifecycle"),
                 new StringPart("id", osdId.toString()),
                 new StringPart("lifecycle_id", lifeCycleId.toString()),
                 new StringPart("lifecycle_state_id", state.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String detachLifeCycle(Long osdId) {
         Part[] parts = {
                 new StringPart("command", "detachlifecycle"),
                 new StringPart("id", osdId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String deleteLifeCycle(Long id) {
         Part[] parts = {
                 new StringPart("command", "deletelifecycle"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public Long createLifeCycle(String name) {
         Part[] parts = {
                 new StringPart("command", "createlifecycle"),
                 new StringPart("name", name),
                 new StringPart("ticket", sessionTicket),
         };
         return parseLongNode(executeMethod(parts), "lifeCycleId");
     }
 
     public Long addLifeCycleState(Long lifeCycleId, String name, Long lcsForCopyId, Boolean setDefault, String stateClass, String config) {
         ArrayList<Part> partList = new ArrayList<Part>();
         Part[] parts = {
                 new StringPart("command", "addlifecyclestate"),
                 new StringPart("lifecycle_id", lifeCycleId.toString()),
                 new StringPart("name", name),
                 new StringPart("state_class", stateClass),
                 new StringPart("ticket", sessionTicket),
         };
         partList.addAll(Arrays.asList(parts));
         if (lcsForCopyId != null) {
             partList.add(new StringPart("lcs_for_copy_id", lcsForCopyId.toString()));
         }
         if (setDefault != null) {
             partList.add(new StringPart("set_default", setDefault.toString()));
         }
         if (config != null) {
             partList.add(new StringPart("config", config));
         }
 
         return parseLongNode(executeMethod(partList.toArray(new Part[partList.size()])), "lifeCycleStateId");
     }
 
     public Boolean connect() {
         log.debug(String.format("starting connect() to %s with: user=%s rep=%s", url_server, username, repository));
         Part[] parts = {
                 new StringPart("command", "connect"),
                 new StringPart("user", username),
                 new StringPart("pwd", password),
                 new StringPart("machine", machine),
                 new StringPart("repository", repository),
         };
 
         String response = executeMethod(parts);
         log.debug("response: " + response);
         sessionTicket = getFieldValue(response, "/connection/ticket");
 //        log.debug("connect received session ticket: "+sessionTicket);
         return sessionTicket != null &&
                 sessionTicket.length() > 0 && sessionTicket.length() < 256;
     }
 
     /**
      * Fork a session and receive another session ticket for the current repository.
      * This method is intended for multi-threaded clients, which should not share
      * the same ticket over parallel requests.
      *
      * @return a new session ticket based on the client's current ticket.
      */
     public String forkSession() {
         Part[] parts = {
                 new StringPart("command", "forksession"),
                 new StringPart("ticket", sessionTicket),
         };
 
         String response = executeMethod(parts);
         sessionTicket = getFieldValue(response, "/connection/ticket");
         return sessionTicket;
     }
 
     public Long copy(Long sourceId, Long targetFolderId) {
         Part[] parts = {
                 new StringPart("command", "copy"),
                 new StringPart("sourceid", sourceId.toString()),
                 new StringPart("targetfolderid", targetFolderId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/objectId");
         return ParamParser.parseLong(result, "copy failed: \n" + result);
     }
 
     public String copyAllVersions(Long sourceId, Long targetFolderId) {
         Part[] parts = {
                 new StringPart("command", "copyallversions"),
                 new StringPart("source_id", sourceId.toString()),
                 new StringPart("target_folder_" +
                         "id", targetFolderId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String copyFolder(Long sourceFolderId, Long targetFolderId, String versions, Boolean croakOnError) {
         Part[] parts = {
                 new StringPart("command", "copyfolder"),
                 new StringPart("source_folder", sourceFolderId.toString()),
                 new StringPart("target_folder", targetFolderId.toString()),
                 new StringPart("versions", versions),
                 new StringPart("croak_on_error", croakOnError.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public Long createFolder(String name, Long parentId, Long aclId, Long ownerId) {
         Part[] parts = {
                 new StringPart("command", "createfolder"),
                 new StringPart("name", name),
                 new StringPart("parentid", parentId.toString()),
                 new StringPart("aclid", aclId.toString()),
                 new StringPart("ticket", sessionTicket),
                 new StringPart("ownerid", ownerId.toString())
         };
         String result = getFieldValue(executeMethod(parts), "/folders/folder/id");
         return ParamParser.parseLong(result, "failed to create folder:\n" + result);
     }
 
     public Long createFolder(String name, Long parentId, Long aclId) {
         Part[] parts = {
                 new StringPart("command", "createfolder"),
                 new StringPart("name", name),
                 new StringPart("parentid", parentId.toString()),
                 new StringPart("aclid", aclId.toString()),
                 new StringPart("ticket", sessionTicket),
                 new StringPart("ownerid", getCurrentUserId().toString())
         };
         String result = getFieldValue(executeMethod(parts), "/folders/folder/id");
         return ParamParser.parseLong(result, "failed to create folder:\n" + result);
     }
 
     // does not yet pass along metadata.
     public Long createFolder(String name, Long parentId) {
         Part[] parts = {
                 new StringPart("command", "createfolder"),
                 new StringPart("name", name),
                 new StringPart("parentid", parentId.toString()),
                 new StringPart("ownerid", getCurrentUserId().toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/folders/folder/id");
         return ParamParser.parseLong(result, "failed to create folder:\n" + result);
     }
 
     public boolean deleteFolder(Long folderId) {
         Part[] parts = {
                 new StringPart("command", "deletefolder"),
                 new StringPart("id", folderId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = executeMethod(parts);
         return result.contains("success.delete.folder");
     }
 
     public Long startRenderTask(Long folderId, String metadata) {
         Part[] parts = {
                 new StringPart("command", "startrendertask"),
                 new StringPart("parentid", folderId.toString()),
                 new StringPart("metadata", metadata),
                 new StringPart("ticket", sessionTicket),
         };
         String response = executeMethod(parts);
 //        log.debug(response);
         String result = getFieldValue(response, "//taskObjectId");
         return ParamParser.parseLong(result, "failed to create render task:\n" + result);
     }
 
     /**
      * @param name        name of the new group (message id)
      * @param description description of the new group (message id)
      * @param parentId    - parent group. 0 means no parent group relation.
      * @return String - xml response of server as a string.
      */
     public String createGroup(String name, String description, Long parentId) {
         Part[] parts = {
                 new StringPart("command", "creategroup"),
                 new StringPart("name", name),
                 new StringPart("parentid", parentId.toString()),
                 new StringPart("description", description),
                 new StringPart("ticket", sessionTicket),
         };
 
         return executeMethod(parts);
     }
 
     public String sendPasswordMail(String repository, String login) {
         Part[] parts = {
                 new StringPart("command", "sendpasswordmail"),
                 new StringPart("repository", repository),
                 new StringPart("login", login),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String listTransformers() {
         Part[] parts = {
                 new StringPart("command", "listTransformers".toLowerCase()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String transformObject(Long id, Long transformerId, String params) {
         Part[] parts = {
                 new StringPart("command", "transformObject".toLowerCase()),
                 new StringPart("id", id.toString()),
                 new StringPart("transformer_id", transformerId.toString()),
                 new StringPart("transformation_params", params),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public File transformObjectToFile(Long id, Long transformerId, String params) {
         Part[] parts = {
                 new StringPart("command", "transformObjectToFile".toLowerCase()),
                 new StringPart("id", id.toString()),
                 new StringPart("transformer_id", transformerId.toString()),
                 new StringPart("transformation_params", params),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethodReturnFile(parts);
     }
 
     public String listIndexItems() {
         Part[] parts = {
                 new StringPart("command", "listIndexItems".toLowerCase()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * Schedule all folders and objects in the database to be re-indexed.
      * This may take a long time.
      * Note: this command requires superuser privileges.
      * @return the success or error XML document, depending on whether the action was successful. 
      */
     public String reindex() {
         Part[] parts = {
                 new StringPart("command", "reindex"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String clearIndex(String classname) {
         Part[] parts = {
                 new StringPart("command", "clearindex"),
                 new StringPart("classname", classname),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String searchFolders(String query) {
         Part[] parts = {
                 new StringPart("command", "searchfolders"),
                 new StringPart("query", query),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String searchFoldersPaged(String query, Integer pageSize, Integer page) {
         Part[] parts = {
                 new StringPart("command", "searchfolders"),
                 new StringPart("query", query),
                 new StringPart("page_size", pageSize.toString()),
                 new StringPart("page", page.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String searchObjects(String query) {
         Part[] parts = {
                 new StringPart("command", "searchobjects"),
                 new StringPart("query", query),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String search(String query) {
         Part[] parts = {
                 new StringPart("command", "search"),
                 new StringPart("query", query),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String searchObjectsPaged(String query, Integer pageSize, Integer page) {
         Part[] parts = {
                 new StringPart("command", "searchobjects"),
                 new StringPart("query", query),
                 new StringPart("page_size", pageSize.toString()),
                 new StringPart("page", page.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String searchSimple(String query, Integer pageSize, Integer page) {
         Part[] parts = {
                 new StringPart("command", "searchsimple"),
                 new StringPart("query", query),
                 new StringPart("page_size", pageSize.toString()),
                 new StringPart("page", page.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String addUserToGroup(Long userId, Long groupId) {
         Part[] parts = {
                 new StringPart("command", "addusertogroup"),
                 new StringPart("userId", userId.toString()),
                 new StringPart("groupId", groupId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String removeUserFromGroup(Long userId, Long groupId) {
         Part[] parts = {
                 new StringPart("command", "removeuserfromgroup"),
                 new StringPart("userId", userId.toString()),
                 new StringPart("groupId", groupId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String createLink(Long target, Long ownerId, Long aclId, Long parentId,
                              String resolver, String type) {
         Part[] parts = {
                 new StringPart("command", "createlink"),
                 new StringPart("acl_id", aclId.toString()),
                 new StringPart("type", type),
                 new StringPart("owner_id", ownerId.toString()),
                 new StringPart("parent_id", parentId.toString()),
                 new StringPart("id", target.toString()),
                 new StringPart("resolver", resolver == null ? "FIXED" : resolver),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String updateLink(Long linkId, Long ownerId, Long aclId, Long parentId,
                              String resolver) {
         ArrayList<Part> partList = new ArrayList<Part>();
         Part[] parts = {
                 new StringPart("command", "updatelink"),
                 new StringPart("link_id", linkId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         if (ownerId != null) {
             partList.add(new StringPart("owner_id", ownerId.toString()));
         }
         if (aclId != null) {
             partList.add(new StringPart("acl_id", aclId.toString()));
         }
         if (parentId != null) {
             partList.add(new StringPart("parent_id", parentId.toString()));
         }
         if (resolver != null) {
             partList.add(new StringPart("resolver", resolver));
         }
         partList.addAll(Arrays.asList(parts));
         return executeMethod(partList.toArray(new Part[partList.size()]));
     }
 
     public Boolean deleteLink(Long linkId) {
         Part[] parts = {
                 new StringPart("command", "deletelink"),
                 new StringPart("link_id", linkId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "success");
         return result.contains("success.delete.link");
     }
 
     public String addGroupToAcl(Long groupId, Long aclId) {
         Part[] parts = {
                 new StringPart("command", "addgrouptoacl"),
                new StringPart("acl_id", aclId.toString()),
                new StringPart("group_id", groupId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String addPermissionToAclEntry(Long permissionId, Long aclEntryId) {
         Part[] parts = {
                 new StringPart("command", "addpermissiontoaclentry"),
                 new StringPart("entry_id", aclEntryId.toString()),
                 new StringPart("permission_id", permissionId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String removePermissionFromAclEntry(Long permissionId, Long aclEntryId) {
         Part[] parts = {
                 new StringPart("command", "removepermissionfromaclentry"),
                 new StringPart("entry_id", aclEntryId.toString()),
                 new StringPart("permission_id", permissionId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * Retrieve a list of all AclEntries for a group or acl.
      *
      * @param id     the id of the acl or group whose entries you want.
      * @param idType - either "groupid" or "aclid"
      * @return the list of AclEntries as an XML String - or an error message in XML format.
      */
     public String listAclEntries(Long id, String idType) {
         Part[] parts = {
                 new StringPart("command", "listaclentries"),
                 new StringPart(idType, id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
 
         return executeMethod(parts);
     }
 
     public boolean disconnect() {
         Part[] parts = {
                 new StringPart("command", "disconnect"),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/success");
         return "success.disconnect".equals(result);
     }
 
     public Long create(String metadata, String name, Long parentId) {
         return create(metadata, name, parentId, "_default_objtype");
     }
 
     public Long create(String metadata, String name, Long parentId, Long objTypeId) {
         Part[] parts = {
                 new StringPart("command", "create"),
                 new StringPart("parentid", parentId.toString()),
                 new StringPart("metadata", metadata),
                 new StringPart("name", name),
                 new StringPart("ticket", sessionTicket),
                 new StringPart("objtype_id", objTypeId.toString()),
         };
         String result = getFieldValue(executeMethod(parts), "/objectId");
         return ParamParser.parseLong(result, "Failed to parse server response:" + result);
     }
 
     public Long create(String metadata, String name, Long parentId, String objType) {
         Part[] parts = {
                 new StringPart("command", "create"),
                 new StringPart("parentid", parentId.toString()),
                 new StringPart("metadata", metadata),
                 new StringPart("name", name),
                 new StringPart("ticket", sessionTicket),
                 new StringPart("objtype", objType),
         };
         String result = getFieldValue(executeMethod(parts), "/objectId");
         return ParamParser.parseLong(result, "Failed to parse server response:" + result);
     }
 
     // TODO: test if create with file upload is compatible to executeMethod(parts).
     public Long create(String metadata, String name, String filename, String format, String contentType, Long parentId) {
 
         PostMethod query = new PostMethod(url_server);
         try {
             Part[] parts = {
                     new StringPart("command", "create"),
                     new StringPart("parentid", parentId.toString()),
                     new StringPart("metadata", metadata),
                     new StringPart("name", name),
                     new FilePart(filename, new File(filename), contentType, "utf-8"),
                     new StringPart("format", format),
                     new StringPart("ticket", sessionTicket),
             };
 
             String result = getFieldValue(executeMethod(parts), "/objectId");
             return Client.parseId(result);
         } catch (FileNotFoundException e) {
             //e.printStackTrace();
             throw new RuntimeException("File " + filename + " not found!");
         }
     }
 
     public boolean delete(Long id) {
         log.debug("delete object with id: " + id);
         Part[] parts = {
                 new StringPart("command", "delete"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "success");
         return result.contains("success.delete.object");
     }
 
     /**
      * Delete all versions of an object.<br/>
      * Note: you can supply any leaf of the object tree.<br/>
      * Requires superadmin powers.
      *
      * @param id the object's id
      * @return true on success.
      */
     public Boolean deleteAllVersions(Long id) {
         log.debug("delete object with id: " + id);
         Part[] parts = {
                 new StringPart("command", "deleteallversions"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = executeMethod(parts);
         return Client.checkSuccess(result);
     }
 
     public String getObjects(Long parentId) {
         return getObjects(parentId, "head");
     }
 
     public String getObjects(Long parentId, String versions) {
         Part[] parts = {
                 new StringPart("command", "getobjects"),
                 new StringPart("parentid", parentId.toString()),
                 new StringPart("versions", versions),
                 new StringPart("ticket", sessionTicket),
         };
 
         return executeMethod(parts);
     }
 
     public Long createTranslation(String attribute, String attributeValue,
                                   Long sourceId, Long objectRelationTypeId, Long rootRelationTypeId) {
         return createTranslation(attribute, attributeValue, sourceId,
                 objectRelationTypeId, rootRelationTypeId, null);
     }
 
     public Long createTranslation(String attribute, String attributeValue,
                                   Long sourceId, Long objectRelationTypeId, Long rootRelationTypeId,
                                   Long targetFolderId) {
         ArrayList<Part> partList = new ArrayList<Part>();
         Part[] parts = {
                 new StringPart("command", "createtranslation"),
                 new StringPart("attribute", attribute),
                 new StringPart("attribute_value", attributeValue),
                 new StringPart("source_id", sourceId.toString()),
                 new StringPart("object_relation_type_id", objectRelationTypeId.toString()),
                 new StringPart("root_relation_type_id", rootRelationTypeId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         partList.addAll(Arrays.asList(parts));
         if (targetFolderId != null) {
             partList.add(new StringPart("target_folder_id", targetFolderId.toString()));
         }
         String result = executeMethod(partList.toArray(new Part[partList.size()]));
         if (result.contains("<error>")) {
             throw new RuntimeException(result);
         }
 //        log.debug("result: "+result);
         return parseLongNode(result, "/createTranslation/translationId");
     }
 
     public String checkTranslation(String attribute, String attributeValue,
                                    Long sourceId, Long objectRelationTypeId, Long rootRelationTypeId) {
         Part[] parts = {
                 new StringPart("command", "checktranslation"),
                 new StringPart("attribute", attribute),
                 new StringPart("attribute_value", attributeValue),
                 new StringPart("source_id", sourceId.toString()),
                 new StringPart("object_relation_type_id", objectRelationTypeId.toString()),
                 new StringPart("root_relation_type_id", rootRelationTypeId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
 
     // TODO: test if setContent with file upload is compatible to executeMethod(parts).
     public boolean setContent(File file, String format, Long Id) {
         PostMethod query = new PostMethod(url_server);
         try {
             Part[] parts = {
                     new StringPart("command", "setcontent"),
                     new StringPart("id", Id.toString()),
                     new FilePart("file", file),
                     new StringPart("format", format),
                     new StringPart("ticket", sessionTicket),
             };
             query.setRequestEntity(
                     new MultipartRequestEntity(parts, query.getParams())
             );
         } catch (FileNotFoundException e) {
             log.debug("Cannot set content with non-existent file.", e);
             throw new RuntimeException(e);
         }
 
         int resp;
         String result;
         try {
             resp = httpclient.executeMethod(query);
             log.debug("ResponseValue from httpclient: " + resp);
             result = query.getResponseBodyAsString();
         } catch (IOException e) {
             log.debug("", e);
             throw new RuntimeException(e);
         } finally {
             query.releaseConnection();
         }
         result = getFieldValue(result, "/success");
         return "success.set.content".equals(result);
     }
 
     public boolean setSysMeta(Long id, String parameter, String value) {
         Part[] parts = {
                 new StringPart("command", "setsysmeta"),
                 new StringPart("parameter", parameter),
                 new StringPart("value", value),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
 
         String result = getFieldValue(executeMethod(parts), "/success");
         return result.equals("success.set.sys_meta");
     }
 
     public boolean lock(Long Id) {
         Part[] parts = {
                 new StringPart("command", "lock"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "success");
         return "success.object.lock".equals(result);
     }
 
     public boolean unlock(Long Id) {
         Part[] parts = {
                 new StringPart("command", "unlock"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "success");
         return "success.object.unlock".equals(result);
     }
 
     public String getContent(Long Id) {
         Part[] parts = {
                 new StringPart("command", "getcontent"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public File getContentAsFile(Long Id) {
         Part[] parts = {
                 new StringPart("command", "getcontent"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethodReturnFile(parts);
     }
 
     public File zipFolder(Long id) {
         Part[] parts = {
                 new StringPart("command", "zipfolder"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethodReturnFile(parts);
     }
 
     public File zipFolder(Long id, Boolean latestHead, Boolean latestBranch) {
         List<StringPart> parts = new ArrayList<StringPart>();
         parts.add(new StringPart("command", "zipfolder"));
         parts.add(new StringPart("ticket", sessionTicket));
         parts.add(new StringPart("id", id.toString()));
         if (latestHead != null) {
             parts.add(new StringPart("latest_head", latestHead.toString()));
         }
         if (latestBranch != null) {
             parts.add(new StringPart("latest_branch", latestBranch.toString()));
         }
         return executeMethodReturnFile(parts.toArray(new Part[parts.size()]));
     }
 
     public Long zipFolderToObject(Long id, Boolean latestHead, Boolean latestBranch, Long targetFolderId,
                                   String objectTypeName, String objectMeta) {
         List<StringPart> parts = new ArrayList<StringPart>();
         parts.add(new StringPart("command", "zipfolder"));
         parts.add(new StringPart("ticket", sessionTicket));
         parts.add(new StringPart("id", id.toString()));
         if (latestHead != null) {
             parts.add(new StringPart("latest_head", latestHead.toString()));
         }
         if (latestBranch != null) {
             parts.add(new StringPart("latest_branch", latestBranch.toString()));
         }
         if (targetFolderId != null) {
             parts.add(new StringPart("target_folder_id", targetFolderId.toString()));
         }
         if (objectTypeName != null) {
             parts.add(new StringPart("object_type_name", objectTypeName));
         }
         if (objectMeta != null) {
             parts.add(new StringPart("object_meta", objectMeta));
         }
         return parseLongNode(executeMethod(parts.toArray(new Part[parts.size()])), "//objectId");
     }
 
     public Long createUser(String description, String fullname, String name, String pwd, String email) {
         Part[] parts = {
                 new StringPart("command", "createuser"),
                 new StringPart("description", description),
                 new StringPart("fullname", fullname),
                 new StringPart("name", name),
                 new StringPart("pwd", pwd),
                 new StringPart("email", email),
                 new StringPart("ticket", sessionTicket),
         };
 
         String result = getFieldValue(executeMethod(parts), "/userId");
         // TODO: find a better way to communicate the error message in result
         // if it _is_ an error message.
         return ParamParser.parseLong(result.trim(), "createUser failed:\n " + result);
     }
 
     public Long createUser(Map<String, String> fields) {
         List<StringPart> parts = new ArrayList<StringPart>();
         parts.add(new StringPart("command", "createuser"));
         parts.add(new StringPart("ticket", sessionTicket));
         for (Map.Entry<String, String> e : fields.entrySet()) {
             parts.add(new StringPart(e.getKey(), e.getValue()));
         }
 
         String result = getFieldValue(executeMethod(parts.toArray(new Part[parts.size()])), "/userId");
         return ParamParser.parseLong(result.trim(), "createUser failed:\n " + result);
     }
 
     public boolean deleteUser(Long Id) {
         Part[] parts = {
                 new StringPart("command", "deleteuser"),
                 new StringPart("user_id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = executeMethod(parts);
         return result.contains("success.delete.user");
     }
 
     public String getFolder(Long Id) {
         Part[] parts = {
                 new StringPart("command", "getfolder"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getFolderByPath(String path) {
         return getFolderByPath(path, false);
     }
 
     public String getFolderByPath(String path, Boolean autocreate) {
         Part[] parts = {
                 new StringPart("command", "getfolderbypath"),
                 new StringPart("path", "/" + path),
                 new StringPart("autocreate", autocreate.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getSubfolders(Long parentID) {
         Part[] parts = {
                 new StringPart("command", "getsubfolders"),
                 new StringPart("parentid", parentID.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getUsersPermissions(Long userId, Long aclId) {
         Part[] parts = {
                 new StringPart("command", "getuserspermissions"),
                 new StringPart("userId", userId.toString()),
                 new StringPart("aclId", aclId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getUserByName(String name) {
         Part[] parts = {
                 new StringPart("command", "getuserbyname"),
                 new StringPart("name", name),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public Long createFormat(String name, String extension, String contentType, String description) {
         Part[] parts = {
                 new StringPart("command", "createformat"),
                 new StringPart("name", name),
                 new StringPart("extension", extension),
                 new StringPart("contenttype", contentType),
                 new StringPart("description", description),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/formatId");
         return ParamParser.parseLong(result, "Could not parse result of createFormat:\n" + result);
 
     }
 
     public boolean deleteFormat(Long Id) {
         Part[] parts = {
                 new StringPart("command", "deleteformat"),
                 new StringPart("format_id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/success");
         return result != null && result.contains("success.delete.format");
     }
 
     public String getFormats() {
         return getFormats(null, null);
     }
 
     public String getFormats(Long id, String name) {
         List<Part> partlist = new ArrayList<Part>();
         partlist.add(new StringPart("command", "getformats"));
         partlist.add(new StringPart("ticket", sessionTicket));
         if (id != null) {
             partlist.add(new StringPart("id", id.toString()));
         }
         if (name != null) {
             partlist.add(new StringPart("name", name));
         }
         Part[] parts = partlist.toArray(new Part[partlist.size()]);
         return executeMethod(parts);
     }
 
     public long createRelationType(String name, String description, Boolean leftobjectprotected,
                                    Boolean rightobjectprotected, String metadata) {
         return createRelationType(name, description, leftobjectprotected, rightobjectprotected, metadata, null, null, false, false);
     }
 
     public long createRelationType(String name, String description, Boolean leftobjectprotected,
                                    Boolean rightobjectprotected, String metadata, Boolean cloneOnLeftCopy, Boolean cloneOnRightCopy) {
         return createRelationType(name, description, leftobjectprotected, rightobjectprotected, metadata, null, null, cloneOnLeftCopy, cloneOnRightCopy);
     }
 
     public long createRelationType(String name, String description, Boolean leftobjectprotected,
                                    Boolean rightobjectprotected, String metadata, String leftResolver, String rightResolver,
                                    Boolean cloneOnLeftCopy, Boolean cloneOnRightCopy) {
         if (metadata == null) {
             metadata = "<meta/>";
         }
         List<Part> partList = new ArrayList<Part>();
         partList.add(new StringPart("command", "createrelationtype"));
         partList.add(new StringPart("name", name));
         partList.add(new StringPart("description", description));
         partList.add(new StringPart("leftobjectprotected", leftobjectprotected.toString()));
         partList.add(new StringPart("rightobjectprotected", rightobjectprotected.toString()));
         partList.add(new StringPart("cloneOnLeftCopy", cloneOnLeftCopy.toString()));
         partList.add(new StringPart("cloneOnRightCopy", cloneOnRightCopy.toString()));
         partList.add(new StringPart("metadata", metadata));
         partList.add(new StringPart("ticket", sessionTicket));
         if (leftResolver != null) {
             partList.add(new StringPart("left_resolver", leftResolver));
         }
         if (rightResolver != null) {
             partList.add(new StringPart("right_resolver", rightResolver));
         }
         String result = getFieldValue(executeMethod(partList.toArray(new Part[partList.size()])), "/relationTypeId");
         return ParamParser.parseLong(result, "CreateRelationType returned something strange:\n" + result);
     }
 
     public boolean deleteRelationType(Long Id) {
         Part[] parts = {
                 new StringPart("command", "deleterelationtype"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
 
         String result = getFieldValue(executeMethod(parts), "success");
         return result.equals("success.delete.relation_type");
     }
 
     public String createAcl(String name, String description) {
         log.debug("url_server: " + url_server);
         log.debug(String.format("create Parts with name=%s and description=%s and sessionTicket=%s",
                 name, description, sessionTicket));
         StringPart cmd = new StringPart("command", "createacl");
         StringPart aclName = new StringPart("name", name);
         StringPart desc = new StringPart("description", description);
         StringPart ticket = new StringPart("ticket", sessionTicket);
         Part[] parts = {
                 cmd, aclName, desc, ticket
         };
         return executeMethod(parts);
     }
 
     public Long versionAsLong(Long id) {
         return Long.parseLong(version(id));
     }
 
     public String version(Long id) {
         return version(id, null);
     }
 
     public Long versionAsLong(Long id, String format) {
         return Long.parseLong(version(id, format));
     }
 
     // simple version of version, skipping all the optional params.
     public String version(Long id, String format) {
         List<Part> parts = new ArrayList<Part>();
         parts.add(new StringPart("command", "version"));
         parts.add(new StringPart("preid", id.toString()));
         parts.add(new StringPart("ticket", sessionTicket));
 
         if (format != null) {
             parts.add(new StringPart("format", format));
         }
         return getFieldValue(executeMethod(parts.toArray(new Part[parts.size()])), "objectId");
     }
 
     public String createWorkflow(Long id) {
         Part[] parts = {
                 new StringPart("command", "createworkflow"),
                 new StringPart("template_id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String doTransition(Long taskId, String transitionName) {
         Part[] parts = {
                 new StringPart("command", "dotransition"),
                 new StringPart("id", taskId.toString()),
                 new StringPart("transition_name", transitionName),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * List groups in a repository. If id parameter is not null, only the
      * group specified by this id is returned.
      *
      * @param id id of a group. Set to null to list all groups. Set to a group's id
      *           to receive only this group's data.
      * @return XML-Response in the format
      *         <pre>
      *          {@code
      *            <groups>
      *              <group><id>123</id>...</group>
      *              ...
      *              </groups>
      *         }
      *         </pre>
      */
     public String listGroups(Long id) {
         if (id != null && id > 0L) {
             Part[] parts = {
                     new StringPart("command", "listgroups"),
                     new StringPart("id ", id.toString()),
                     new StringPart("ticket", sessionTicket),
             };
             return executeMethod(parts);
         }
         else {
             Part[] parts = {
                     new StringPart("command", "listgroups"),
                     new StringPart("ticket", sessionTicket),
             };
             return executeMethod(parts);
         }
     }
 
     /**
      * Given an XML list of ids, this method returns the objects that were found,
      * provided the user has a BROWSE permission for them.
      *
      * @param query a query string containing an xml list of ids.
      * @return the XML response as String
      */
     public String getObjectsById(String query) {
         Part[] parts = {
                 new StringPart("command", "getobjectsbyid"),
                 new StringPart("ids", query),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * Given an XML list of ids, this method returns the folders that were found,
      * provided the user has a BROWSE permission for them.
      *
      * @param query a query string containing an xml list of ids.
      * @return the XML response as String
      */
     public String getFoldersById(String query) {
         Part[] parts = {
                 new StringPart("command", "getfoldersbyid"),
                 new StringPart("ids", query),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String deleteGroup(Long id) {
         Part[] parts = {
                 new StringPart("command", "deletegroup"),
                 new StringPart("id ", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public boolean deleteAcl(Long Id) {
         Part[] parts = {
                 new StringPart("command", "deleteacl"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "success");
         return result.equals("success.delete.acl");
     }
 
     public String getObjectTypeIdByName(String name) {
         String xml = getObjTypes();
         Node node = ParamParser.parseXmlToDocument(xml, "Could not parse response to getObjTypes");
         Node workflowObjectType = node.selectSingleNode("/objectTypes/objectType[sysName='" + name + "']");
         log.debug("node: " + node);
         return workflowObjectType.selectSingleNode("id").getText();
     }
 
     public String getWorkflowTemplateList() {
         // determine WorkflowTemplate-ObjectType:
         String ids = getObjectTypeIdByName(Constants.OBJTYPE_WORKFLOW_TEMPLATE);
         log.debug("workflowTemplateObjectTypeId: " + ids);
         Long id = Long.parseLong(ids);
         String query = String.format("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='objecttype'>%020d</TermQuery></Clause>" +
                 "<Clause occurs='must'><TermQuery fieldName='active_workflow'>true</TermQuery></Clause></BooleanQuery>", id);
         Part[] parts = {
                 new StringPart("command", "searchobjects"),
                 new StringPart("query", query),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getWorkflowList(String status) {
         // determine WorkflowTemplate-ObjectType:
         String ids = getObjectTypeIdByName(Constants.OBJTYPE_WORKFLOW);
         Long id = Long.parseLong(ids);
         String query = String.format("<BooleanQuery><Clause occurs='must'><TermQuery fieldName='objecttype'>%020d</TermQuery></Clause>" +
                 "<Clause occurs='must'><TermQuery fieldName='procstate'>%s</TermQuery></Clause></BooleanQuery>",
                 id, status);
         Part[] parts = {
                 new StringPart("command", "searchobjects"),
                 new StringPart("query", query),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String findOpenTasks(Long workflowId, Long userId) {
         List<Part> partlist = new ArrayList<Part>();
         partlist.add(new StringPart("command", "findopentasks"));
         partlist.add(new StringPart("ticket", sessionTicket));
         if (workflowId != null) {
             partlist.add(new StringPart("workflow_id", workflowId.toString()));
         }
         if (userId != null) {
             partlist.add(new StringPart("user_id", userId.toString()));
         }
         return executeMethod(partlist.toArray(new Part[partlist.size()]));
     }
 
     public String getMeta(Long id) {
         Part[] parts = {
                 new StringPart("command", "getmeta"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * Fetch a metaset from the server and return it as a String.
      *
      * @param id        id of OSD or folder object
      * @param type      name of the metaset type
      * @param className one of OSD, Folder, Metaset [Metaset currently disabled]
      * @return the requested XML metaset as a string
      */
     public String getMetaset(Long id, String type, String className) {
         Part[] parts = {
                 new StringPart("command", "getmetaset"),
                 new StringPart("id", id.toString()),
                 new StringPart("type_name", type),
                 new StringPart("class_name", className),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * Set a metaset's content.
      *
      * @param id        id of OSD or folder object
      * @param type      name of the metaset type
      * @param className one of OSD, Folder, Metaset [Metaset currently disabled]
      * @return the updated metaset as a string
      */
     public String setMetaset(Long id, String type, String className, String content) {
         Part[] parts = {
                 new StringPart("command", "setmetaset"),
                 new StringPart("id", id.toString()),
                 new StringPart("type_name", type),
                 new StringPart("class_name", className),
                 new StringPart("content", content),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * Fetch an XML config entry from the server and return it as a String.
      * Note: Access to config entries is limited to superusers unless the XML config entry
      * contains the element {@code <isPublic>true</isPublic> }
      *
      * @param name the name of the config entry
      * @return the requested XML config as a string
      */
     public String getConfigEntry(String name) {
         Part[] parts = {
                 new StringPart("command", "getconfigentry"),
                 new StringPart("name", name),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * Set the value of a config entry.
      *
      * @param name   name of the config entry
      * @param config XML string for the config entry's value
      * @return XML-Response:
      *         <pre>{@code
      *         <configEntryId>$configEntryId</configEntryId>
      *         }</pre>
      */
     public String setConfigEntry(String name, String config) {
         Part[] parts = {
                 new StringPart("command", "setconfigentry"),
                 new StringPart("name", name),
                 new StringPart("config", config),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getFolderMeta(Long id) {
         Part[] parts = {
                 new StringPart("command", "getfoldermeta"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String setMetaGetRawResponse(Long id, String metadata) {
         Part[] parts = {
                 new StringPart("command", "setmeta"),
                 new StringPart("id", id.toString()),
                 new StringPart("metadata", metadata),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public Boolean setMeta(Long id, String metadata) {
         String result = setMetaGetRawResponse(id, metadata);
         result = getFieldValue(result, "/cinnamon/success");
         return result.equals("success.set.metadata");
     }
 
     public Boolean lockAndSetMeta(Long id, String metadata) {
         lock(id);
         Boolean result = setMeta(id, metadata);
         unlock(id);
         return result;
     }
 
     /**
      * Convenience method: return the id of the given object's root version.
      *
      * @param id the object of which the root id is needed
      * @return the id of the root object.
      */
     public Long getRootObjectId(Long id) {
         String source = getObject(id);
         return parseLongNode(source, "//object/rootId");
     }
 
     /**
      * Fetch the root version (= v1) of an object.
      *
      * @param id the object whose root you need.
      * @return the serialized version of the root object
      */
     public String getRootObject(Long id) {
         String source = getObject(id);
         Long rootId = parseLongNode(source, "//object/rootId");
         return getObject(rootId);
     }
 
     /**
      * Lock an object, set the content to the given file and unlock it.
      *
      * @param content the file containing the content
      * @param format  the sysName of the format you want to set
      * @param id      the object's id
      */
     public void lockAndSetContent(File content, String format, Long id) {
         lock(id);
         setContent(content, format, id);
         unlock(id);
     }
 
     public Boolean lockAndSetSysMeta(Long id, String paramName, String value) {
         lock(id);
         Boolean result = setSysMeta(id, paramName, value);
         unlock(id);
         return result;
     }
 
     public boolean dummy(Long Id) {
         Part[] parts = {
                 new StringPart("command", ""),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = executeMethod(parts);
         return Client.checkBooleanResponse(result, "Invalid Response to ");
 
     }
 
 
     public Boolean updateFolder(String id, Map<String, String> fields) {
         List<StringPart> parts = new ArrayList<StringPart>();
         parts.add(new StringPart("command", "updatefolder"));
         parts.add(new StringPart("id", id));
         parts.add(new StringPart("ticket", sessionTicket));
         for (Map.Entry<String, String> e : fields.entrySet()) {
             parts.add(new StringPart(e.getKey(), e.getValue()));
         }
 
         String result = getFieldValue(executeMethod(parts.toArray(new Part[parts.size()])), "/success");
         return "success.update.folder".equals(result);
     }
 
     public Boolean updateFolder(Long id, Map<String, String> fields) {
         return updateFolder(id.toString(), fields);
     }
 
     public String getSysMeta(Long id, Long folderID, String parameter) {
         if ((id != null && folderID != null) || (id == null && folderID == null)) {
             throw new IllegalArgumentException("Either id or folderid must be specified.");
         }
 
         Part[] parts = {
                 new StringPart("command", "getsysmeta"),
                 (id == null) ? new StringPart("folderid", folderID.toString()) : new StringPart("id", id.toString()),
                 new StringPart("parameter", parameter),
                 new StringPart("ticket", sessionTicket),
         };
 
         return getFieldValue(executeMethod(parts), "/sysMetaValue");
     }
 
     public Long createFolderType(String name, String description) {
         Part[] parts = {
                 new StringPart("command", "createfoldertype"),
                 new StringPart("name", name),
                 new StringPart("description", description),
                 new StringPart("ticket", sessionTicket),
         };
         String result = executeMethod(parts);
         return parseLongNode(result, "/folderTypes/folderType/id");
     }
 
     public String getFolderTypes() {
         Part[] parts = {
                 new StringPart("command", "getfoldertypes"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public Boolean deleteFolderType(Long Id) {
         Part[] parts = {
                 new StringPart("command", "deletefoldertype"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/success");
         return "success.delete.folder_type".equals(result);
     }
 
 
     public long createObjectType(String name, String description) {
         Part[] parts = {
                 new StringPart("command", "createobjecttype"),
                 new StringPart("name", name),
                 new StringPart("description", description),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/objectTypeId");
         return ParamParser.parseLong(result, "createObjectType returned something strange:\n" + result);
 
     }
 
     public boolean deleteObjectType(Long Id) {
         Part[] parts = {
                 new StringPart("command", "deleteobjecttype"),
                 new StringPart("id", Id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/success");
         return "success.delete.object_type".equals(result);
 
     }
 
     public String getAcls(Long id) {
         Part[] parts = {
                 new StringPart("command", "getacls"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getGroupsOfUser(Long id, Boolean recursive) {
         Part[] parts = {
                 new StringPart("command", "getgroupsofuser"),
                 new StringPart("id", id.toString()),
                 new StringPart("recursive", recursive.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getAcls() {
         Part[] parts = {
                 new StringPart("command", "getacls"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String listAclMembers(Long id) {
         Part[] parts = {
                 new StringPart("command", "listaclmembers"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
 
     public String getRelationTypes() {
         Part[] parts = {
                 new StringPart("command", "getrelationtypes"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getObjTypes() {
         Part[] parts = {
                 new StringPart("command", "getobjtypes"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getAclEntry(Long id) {
         Part[] parts = {
                 new StringPart("command", "getaclentry"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String executeMethod(Part[] parts) {
         for (Part p : parts) {
             if (p instanceof StringPart) {
                 ((StringPart) p).setCharSet("UTF-8");
             }
         }
         PostMethod query = new PostMethod(url_server);
         query.setRequestEntity(
                 new MultipartRequestEntity(parts, query.getParams())
         );
         String result;
         try {
             int resp = httpclient.executeMethod(query);
             log.debug("ResponseValue from httpclient: " + resp);
             result = query.getResponseBodyAsString().trim();
         } catch (IOException e) {
             log.debug("", e);
             throw new RuntimeException(e);
         } finally {
             query.releaseConnection();
         }
         if (checkForSessionFailure(result)) {
             log.debug("Session failure detected - will try reconnect");
             log.debug("old, invalid session ticket: " + sessionTicket);
             // if the session has timed out, try to reconnect.
             if (connect()) {
                 log.debug("reconnect succeeded.");
                 reconnects++;
                 setNewSessionTicketAfterReconnect(parts);
                 log.debug("new session ticket:" + sessionTicket);
                 // try again after successful reconnect:
                 result = executeMethod(parts);
             }
             else {
                 log.debug("reconnect failed.");
             }
 
         }
         checkForError(result);
         return result;
     }
 
     public void setNewSessionTicketAfterReconnect(Part[] parts) {
         int index = 0;
         for (Part p : parts) {
             if (p.getName().equals("ticket")) {
                 parts[index] = new StringPart("ticket", sessionTicket);
                 break;
             }
             index++;
         }
     }
 
     public File executeMethodReturnFile(Part[] parts) {
         for (Part p : parts) {
             if (p instanceof StringPart) {
                 ((StringPart) p).setCharSet("UTF-8");
             }
         }
         PostMethod query = new PostMethod(url_server);
         query.setRequestEntity(
                 new MultipartRequestEntity(parts, query.getParams())
         );
         File response;
         try {
             String filename = "cinnamonFileResponse";
             response = File.createTempFile(filename, null);
 
             @SuppressWarnings("unused")
             int resp = httpclient.executeMethod(query);
 //			log.debug("ResponseValue from httpclient: "+resp);
 
             FileOutputStream fos = new FileOutputStream(response);
             InputStream is = query.getResponseBodyAsStream();
             byte[] bytes = new byte[1024];
             byte[] first1024 = new byte[1024]; // needed for expiredSession-check.
             boolean isFirstPacket = true;
             int len = 0;
             int total = 0;
             while ((len = is.read(bytes)) > 0) {
                 if (isFirstPacket) {
                     first1024 = bytes.clone();
                     isFirstPacket = false;
                 }
                 fos.write(bytes, 0, len);
                 total += len;
             }
             fos.close();
             log.debug("read bytes: " + total);
             // try to reconnect once if session has expired:
             String firstPacketResponse = new String(first1024);
             if (checkForSessionFailure(firstPacketResponse)) {
                 if (connect()) {
                     setNewSessionTicketAfterReconnect(parts);
                     return executeMethodReturnFile(parts);
                 }
             }
 
         } catch (IOException e) {
             log.debug("", e);
             throw new RuntimeException(e);
         } finally {
             query.releaseConnection();
         }
         return response;
     }
 
     public String createRelation(String relationTypeName, Long leftID, Long rightID) {
         Part[] parts = {
                 new StringPart("command", "createrelation"),
                 new StringPart("name", relationTypeName),
                 new StringPart("leftid", leftID.toString()),
                 new StringPart("rightid", rightID.toString()),
                 new StringPart("ticket", sessionTicket),
         };
 
         return executeMethod(parts);
     }
 
     public boolean deleteRelation(Long id) {
         Part[] parts = {
                 new StringPart("command", "deleterelation"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         String result = getFieldValue(executeMethod(parts), "/success");
         return result != null && result.contains("success.delete.relation");
 
     }
 
     public String getRelations(String name, Long leftID, Long rightID) {
         List<Part> partlist = new ArrayList<Part>();
         partlist.add(new StringPart("command", "getrelations"));
         partlist.add(new StringPart("ticket", sessionTicket));
         if (name != null) {
             partlist.add(new StringPart("name", name));
         }
         if (leftID != null) {
             partlist.add(new StringPart("leftid", leftID.toString()));
         }
         if (rightID != null) {
             partlist.add(new StringPart("rightid", rightID.toString()));
         }
         Part[] parts = partlist.toArray(new Part[partlist.size()]);
         return executeMethod(parts);
     }
 
     public String findObjectByName(String name) {
         Part[] parts = {
                 new StringPart("command", "findobjectbyname"),
                 new StringPart("name", name),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String setPassword(String password) {
         Part[] parts = {
                 new StringPart("command", "setpassword"),
                 new StringPart("password", password),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String setEmail(String email) {
         Part[] parts = {
                 new StringPart("command", "setemail"),
                 new StringPart("email", email),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String removeGroupFromAcl(Long groupID, Long aclID) {
         Part[] parts = {
                 new StringPart("command", "removegroupfromacl"),
                 new StringPart("aclid", aclID.toString()),
                 new StringPart("groupid", groupID.toString()),
                 new StringPart("ticket", sessionTicket),
         };
 
         String result = executeMethod(parts);
         log.debug("removeGroupFromAcl:\n" + result);
         return result;
     }
 
     public String getUser(Long id) throws DocumentException {
         Part[] parts = {
                 new StringPart("command", "getuser"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getUsers() throws DocumentException {
         Part[] parts = {
                 new StringPart("command", "getusers"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getUsersAcls(Long id) {
         Part[] parts = {
                 new StringPart("command", "getusersacls"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getObject(Long id) {
         Part[] parts = {
                 new StringPart("command", "getobject"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String getPermission(Long id) {
         Part[] parts = {
                 new StringPart("command", "getpermission"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String createPermission(String name, String description) {
         Part[] parts = {
                 new StringPart("command", "createpermission"),
                 new StringPart("name", name),
                 new StringPart("description", description),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String deletePermission(Long id) {
         Part[] parts = {
                 new StringPart("command", "deletepermission"),
                 new StringPart("id", id.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String listPermissions() {
         Part[] parts = {
                 new StringPart("command", "listpermissions"),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String sudo(Long userId) {
         Part[] parts = {
                 new StringPart("command", "sudo"),
                 new StringPart("user_id", userId.toString()),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     public String echo(Long id, String xml) {
         Part[] parts = {
                 new StringPart("command", "echo"),
                 new StringPart("id", id.toString()),
                 new StringPart("xml", xml),
                 new StringPart("ticket", sessionTicket),
         };
         return executeMethod(parts);
     }
 
     /**
      * @return the sessionTicket
      */
     public String getSessionTicket() {
         return sessionTicket;
     }
 
     /**
      * @param sessionTicket the sessionTicket to set
      */
     public void setSessionTicket(String sessionTicket) {
         this.sessionTicket = sessionTicket;
     }
 
     Long getCurrentUserId() {
         String user = getUserByName(username);
         return parseLongNode(user, "/users/user/id");
     }
 
     /**
      * If the CinnamonServer returned an XML error message, throw it as
      * a RuntimeException. The message is assumed to contain an error if it
      * ends with &gt;/error&lt;.
      *
      * @param message the message to check.
      */
     public void checkForError(String message) {
         if (message.endsWith("</error>")) {
             throw new RuntimeException(message);
         }
     }
 
     public Boolean checkForSessionFailure(String message) {
         String[] errors = {"<code>error.session.not_found</code>",
                 "<code>error.session.invalid</code>",
                 "<code>error.session.expired</code>"};
         Boolean isFailure = false;
         for (String error : errors) {
             if (message.contains(error)) {
                 isFailure = true;
                 break;
             }
         }
         return isFailure;
     }
 
     public static Long parseLongNode(String xml, String xpath) {
 //		Logger log = LoggerFactory.getLogger(Client.class);
 //		log.debug("parseLongNode: "+xml);
         Document doc = ParamParser.parseXmlToDocument(xml, null);
         Node node = doc.selectSingleNode(xpath);
 //		Logger log = LoggerFactory.getLogger(Client.class);
 //		log.debug("node: "+node);
 //		log.debug("node-test:"+node.getText());
         return ParamParser.parseLong(node.getText(), null);
     }
 
     public static Boolean checkSuccess(String xml) {
         Document doc = ParamParser.parseXmlToDocument(xml, null);
         Node successNode = doc.selectSingleNode("/success");
         return successNode != null;
     }
 
     /**
      * @return the props
      */
     public Properties getConfigurationProperties() {
         if (props.size() == 0) {
             props.put("server.url", url_server);
             props.put("server.username", username);
             props.put("server.password", password);
             props.put("default_repository", repository);
         }
         return props;
     }
 
     /**
      * @return the username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * @param username the username to set
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * @return the password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * @param password the password to set
      */
     public void setClientPassword(String password) {
         this.password = password;
     }
 
     public String getFieldValue(String xml, String xpath) {
         String value;
         try {
             Document doc = DocumentHelper.parseText(xml);
             if (doc.selectSingleNode("/error") != null) {
                 // it's an error message!
                 throw new RuntimeException(xml);
             }
 
             Node n = doc.selectSingleNode(xpath);
             value = n.getText();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         return value;
     }
 
     public Boolean hasTicket() {
         return sessionTicket != null;
     }
 
     public String getRepository() {
         return repository;
     }
 
     public void setRepository(String repository) {
         this.repository = repository;
     }
 
     public String toString() {
         return username + "@" + url_server + "@" + repository;
     }
 
     /**
      * publishStackTrace allows you to upload a Java exception with the whole StackTrace to
      * the Cinnamon server where it will be stored in the specified object's metadata.
      * This method is used in the RenderServer to notify users of problems which have occurred during rendering..
      *
      * @param metaset The metaset which will receive the error node containing the stack trace. If
      *                it does not exist, it will be created.
      * @param id      id of the Cinnamon object that will be updated.
      * @param e       the Exception you want to publish.
      */
     public void publishStackTrace(String metaset, Long id, Exception e) {
         log.debug("Trying to publish StackTrace info.");
         try {
             StringBuilder trace = new StringBuilder();
             Throwable cause = e;
             while (cause != null) {
                 trace.append(cause.toString());
                 for (StackTraceElement ste : cause.getStackTrace()) {
                     trace.append("\n  ");
                     trace.append(ste);
                 }
                 trace.append('\n');
                 cause = cause.getCause();
             }
             try {
                 lock(id);
             } catch (Exception lockEx) {
                 log.debug("Exception while trying to lock object. Will ignore this for now.", lockEx);
             }
             Document doc = ParamParser.parseXmlToDocument("<error timestamp='" + new Date().toString() + "'/>");
             doc.getRootElement().addText(trace.toString());
             String meta = getMeta(id);
             Document metaDoc = ParamParser.parseXmlToDocument(meta);
             Node metasetNode = metaDoc.selectSingleNode("//metaset[@type='" + metaset + "']");
             Element metasetElement;
             if (metasetNode == null) {
                 metasetElement = metaDoc.getRootElement().addElement("metaset").addAttribute("type", metaset);
             }
             else {
                 metasetElement = (Element) metasetNode;
             }
             metasetElement.add(doc.getRootElement().detach());
             setMeta(id, metaDoc.asXML());
 
             changeLifeCycleState(id, Constants.RENDERSERVER_RENDER_TASK_FAILED);
             unlock(id);
         } catch (Exception ex) {
             log.error("Could not publish StackTrace.", ex);
             if (e != null) {
                 e.printStackTrace();
             }
         }
     }
 
     public String getHost() {
         return url_server;
     }
 
     public HttpClient getHttpclient() {
         return httpclient;
     }
 
     public void setHttpclient(HttpClient httpclient) {
         this.httpclient = httpclient;
     }
 
     public Properties getProps() {
         return props;
     }
 
     public void setProps(Properties props) {
         this.props = props;
     }
 
     public String getMachine() {
         return machine;
     }
 
     public void setMachine(String machine) {
         this.machine = machine;
     }
 
     public String getUrl_server() {
         return url_server;
     }
 
     public void setUrl_server(String url_server) {
         this.url_server = url_server;
     }
 
     /**
      * Keep track of the number of reconnects. This is used for debugging and testing.
      *
      * @return the current number of reconnects.
      */
     public Integer getReconnects() {
         return reconnects;
     }
 }
