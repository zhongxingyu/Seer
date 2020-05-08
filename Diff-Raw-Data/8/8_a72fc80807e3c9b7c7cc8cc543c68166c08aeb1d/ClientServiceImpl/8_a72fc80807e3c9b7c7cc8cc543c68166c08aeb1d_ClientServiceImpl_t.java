 package org.docear.syncdaemon.client;
 
 import akka.actor.ActorRef;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.client.apache.ApacheHttpClient;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import com.typesafe.config.Config;
 import org.apache.commons.io.IOUtils;
 import org.docear.syncdaemon.NeedsConfig;
 import org.docear.syncdaemon.client.exceptions.NoFolderException;
 import org.docear.syncdaemon.fileindex.FileMetaData;
 import org.docear.syncdaemon.projects.Project;
 import org.docear.syncdaemon.users.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import java.io.*;
 import java.net.URLEncoder;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 public class ClientServiceImpl implements ClientService, NeedsConfig {
     private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);
     private Config config;
     private String serviceUrl;
     private Client restClient;
 
     private void intialize() {
 
         //trust certificates
         try {
             SSLContext ctx = SSLContext.getInstance("TLS");
             ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
             SSLContext.setDefault(ctx);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         restClient = ApacheHttpClient.create();
 
//        PrintStream stream = new PrintStream(new NullOutputStream());
//        restClient.addFilter(new LoggingFilter(stream));
         //restClient.addFilter(new LoggingFilter(System.out));
         //check for basic auth user
         try {
             final String basicAuthUser = config.getString("daemon.client.basicauth.username");
             final String basicAuthPw = config.getString("daemon.client.basicauth.password");
 
             restClient.addFilter(new HTTPBasicAuthFilter(basicAuthUser, basicAuthPw));
         } catch (Exception e) {
 
             System.out.println(e.getMessage());
         }
 
         // generate service url
         serviceUrl = config.getString("daemon.client.baseurl") + "/" + config.getString("daemon.client.api.version");
     }
 
     // ClientService implementation
     @Override
     public UploadResponse upload(User user, Project project, FileMetaData fileMetaData) throws FileNotFoundException {
         final String projectId = fileMetaData.getProjectId();
         // validation
         if (!project.getId().equals(projectId)) {
             throw new RuntimeException("file does not belong to project");
         }
 
         InputStream fileInStream = null;
         InputStream zipInStream = null;
         ZipOutputStream outStream = null;
         PipedOutputStream pipeOut = null;
 
         ClientResponse response = null;
         try {
             // get file stream
             final String absoluteFilePath = project.getRootPath() + fileMetaData.getPath();
             final File file = new File(absoluteFilePath);
             final String urlEncodedFilePath = normalizePath(fileMetaData.getPath());
 
             // byte[] bytes = IOUtils.toByteArray(file.toURI());
             fileInStream = new FileInputStream(absoluteFilePath);
 
 
 //            pipeOut = new PipedOutputStream();
 //            zipInStream = new PipedInputStream(pipeOut);
 //            outStream = new ZipOutputStream(pipeOut);
 //            outStream.putNextEntry(new ZipEntry("file"));
 //
 //            IOUtils.copyLarge(fileInStream, outStream);
 //            outStream.closeEntry();
 //            IOUtils.closeQuietly(pipeOut);
             // create request
             final WebResource request =
                     preparedResource(fileMetaData.getProjectId(), user).path("file").path(urlEncodedFilePath)
                             .queryParam("parentRev", "" + fileMetaData.getRevision())
                             .queryParam("zip", "false").queryParam("contentLength", file.length() + "");
 
             response = request.type(MediaType.APPLICATION_OCTET_STREAM).put(ClientResponse.class, fileInStream);
 
 
         } catch (IOException e) {
             logger.error("error sending file to server", e);
         } finally {
             IOUtils.closeQuietly(fileInStream);
             IOUtils.closeQuietly(outStream);
             IOUtils.closeQuietly(zipInStream);
             IOUtils.closeQuietly(pipeOut);
 
         }
 
         if (response != null && response.getStatus() == 200) {
             final FileMetaData newFileMeta = serverMetadataToLocalFileMetaData(projectId, response.getEntity(String.class));
             // check that file is no conflicted copy
             if (newFileMeta.getPath().equals(fileMetaData.getPath())) {
                 // equal path => no conflict
                 return new UploadResponse(newFileMeta);
             } else {
                 // conflict!
                 // get additional fileMetaData for original file
                 final FileMetaData currentOriMetaData = getCurrentFileMetaData(user, fileMetaData);
                 return new UploadResponse(currentOriMetaData, newFileMeta);
             }
         } else {
             throw new RuntimeException("Problem uploading file! HTTP Code: " + response.getStatus());
         }
     }
 
     @Override
     public FileMetaData createFolder(User user, Project project, FileMetaData fileMetaData) throws FileNotFoundException {
         final String projectId = fileMetaData.getProjectId();
         // validation
         if (!project.getId().equals(projectId)) {
             throw new IllegalStateException("file does not belong to project");
         }
 
         final String absoluteFilePath = project.getRootPath() + fileMetaData.getPath();
         final String urlEncodedFilePath = normalizePath(fileMetaData.getPath());
         final File file = new File(absoluteFilePath);
 
         if (file.isDirectory()) {
             final WebResource request = preparedResource(fileMetaData.getProjectId(), user).path("create_folder");
             final MultivaluedMapImpl map = new MultivaluedMapImpl();
             map.add("path", urlEncodedFilePath);
 
             final ClientResponse response = request.post(ClientResponse.class, map);
             if (response.getStatus() == 200) {
                 return serverMetadataToLocalFileMetaData(projectId, response.getEntity(String.class));
             } else {
                 throw new RuntimeException("Problem uploading file! HTTP Code: " + response.getStatus());
             }
         } else {
             throw new IllegalStateException("Resource is not a directory!");
         }
     }
 
     @Override
     public InputStream download(User user, FileMetaData currentServerMetaData) {
         final String urlEncodedPath = normalizePath(currentServerMetaData.getPath());
         // create request
         final WebResource request = preparedResource(currentServerMetaData.getProjectId(), user).path("file").path(urlEncodedPath).queryParam("zip", "true");
 
         ClientResponse response = request.get(ClientResponse.class);
 
         // on success
         if (response.getStatus() == 200) {
             final ZipInputStream inStream = new ZipInputStream(response.getEntityInputStream());
             try {
                 inStream.getNextEntry();
             } catch (IOException e) {
                 throw new RuntimeException("Problem with zipfile download! unknown error occured...  ", e);
             }
             return inStream;
         } else
             return null;
     }
 
     @Override
     public FileMetaData delete(User user, Project project, FileMetaData fileMetaData) {
         final WebResource resource = preparedResource(project.getId(), user).path("delete_file");
         MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
         formData.add("path", normalizePath(fileMetaData.getPath(), false));
 
         final ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).delete(ClientResponse.class, formData);
 
         if (response.getStatus() == 200) {
             final FileMetaData currentFileMetaData = serverMetadataToLocalFileMetaData(project.getId(), response.getEntity(String.class));
             return currentFileMetaData;
         } else {
             return null;
         }
     }
 
     @Override
     public ProjectResponse getProjects(User user) {
         // create request
         final WebResource request = preparedResource(user).path("user").path("projects");
 
         ClientResponse response = request.get(ClientResponse.class);
 
         // on success
         if (response.getStatus() == 200)
             return new ProjectResponse(serverProjectListToLocalProject(response.getEntity(String.class)));
         else
             return null;
     }
 
     @Override
     public Project getProject(User user, String projectId) {
         // create request
         final WebResource request = preparedResource(user).path("project").path(projectId);
 
         ClientResponse response = request.get(ClientResponse.class);
 
         // on success
         if (response.getStatus() == 200)
             try {
                 final JsonNode jsonNode = new ObjectMapper().readTree(response.getEntity(String.class));
                 final String name = jsonNode.get("name").textValue();
                 final long revision = jsonNode.get("revision").longValue();
                 final Project project = new Project(projectId,"",revision,name);
                 return project;
             } catch (JsonMappingException e) {
                 throw new RuntimeException(e);
             } catch (JsonProcessingException e) {
                 throw new RuntimeException(e);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         else
             return null;
     }
 
     @Override
     public FolderMetaData getFolderMetaData(User user, FileMetaData folderMetaData) {
         final String projectId = folderMetaData.getProjectId();
 
         final String urlEncodedFilePath = normalizePath(folderMetaData.getPath());
 
         final WebResource request = preparedResource(projectId, user).path("metadata").path(urlEncodedFilePath);
         ClientResponse response = request.get(ClientResponse.class);
 
         // on success
         if (response.getStatus() == 200)
             return serverMetadataToLocalFolderMetaData(projectId, response.getEntity(String.class));
         else {
             return null;
         }
     }
 
     @Override
     public ListenForUpdatesResponse listenForUpdates(User user, Map<String, Long> projectIdRevisionMap, ActorRef actorRef) {
         final WebResource resource = preparedResource(user).path("project").path("listen");
         MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
         for (Map.Entry<String, Long> entry : projectIdRevisionMap.entrySet()) {
             formData.add(entry.getKey(), entry.getValue().toString());
         }
 
         System.out.println(resource.getURI());
         final ClientResponse clientResponse = resource.post(ClientResponse.class, formData);
 
         if (clientResponse.getStatus() == 200) {
             try {
                 final ListenForUpdatesResponse response = new ObjectMapper().readValue(clientResponse.getEntityInputStream(), ListenForUpdatesResponse.class);
                 if (actorRef != null)
                     actorRef.tell(response, null);
                 return response;
             } catch (IOException e) {
                 throw new RuntimeException("problem parsing listenForUpdatesResult");
             }
         } else {
             return null;
         }
     }
 
     @Override
     public FileMetaData getCurrentFileMetaData(User user, FileMetaData fileMetaData) {
         final String projectId = fileMetaData.getProjectId();
 
         final String urlEncodedFilePath = normalizePath(fileMetaData.getPath());
         // create request
         final WebResource request = preparedResource(fileMetaData.getProjectId(), user).path("metadata").path(urlEncodedFilePath);
 
         ClientResponse response = request.get(ClientResponse.class);
 
         // on success
         if (response.getStatus() == 200)
             return serverMetadataToLocalFileMetaData(projectId, response.getEntity(String.class));
         else
             return null;
 
     }
 
     @Override
     public DeltaResponse delta(User user, String projectId, Long sinceRevision) {
 
         final WebResource request = preparedResource(projectId, user).path("delta");
         final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
         formData.add("projectRevision", sinceRevision + "");
 
         final ClientResponse response = request.post(ClientResponse.class, formData);
 
         if (response.getStatus() == 200) {
             return serverDeltaResponseToLocalDeltaResponse(projectId, response.getEntity(String.class));
         } else {
             return null;
         }
 
     }
 
     // Needs Config implementation
     @Override
     public void setConfig(Config config) {
         this.config = config;
         this.intialize();
     }
 
     private WebResource preparedResource(User user) {
         return restClient.resource(serviceUrl) // path
                 // authentication
                 .queryParam("username", user.getUsername()).queryParam("accessToken", user.getAccessToken());
     }
 
     // private methods
 
     private WebResource preparedResource(String projectId, User user) {
         return restClient.resource(serviceUrl).path("project").path(projectId) // path
                 // authentication
                 .queryParam("username", user.getUsername()).queryParam("accessToken", user.getAccessToken());
     }
 
     /**
      * converts "\" to "/", ensures leading "/" is present and encodes to UTF-8
      */
     private String normalizePath(String path) {
         return this.normalizePath(path, true);
     }
 
     private String normalizePath(String path, boolean urlEncode) {
         try {
             String newPath = path.replace("\\", "/");
 
             if (!newPath.startsWith("/")) {
                 newPath = "/" + newPath;
             }
 
             if (urlEncode)
                 newPath = URLEncoder.encode(newPath, "UTF-8");
 
             return newPath;
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("Problem with UTF-8 Encoding");
         }
     }
 
     private FolderMetaData serverMetadataToLocalFolderMetaData(String projectId, String metadata) {
         try {
             final JsonNode metaJson = new ObjectMapper().readTree(metadata);
             final boolean dir = metaJson.get("dir").booleanValue();
             if (!dir) {
                 throw new NoFolderException("resource is no folder");
             }
             final FileMetaData fileMetaData = serverMetadataToLocalFileMetaData(projectId, metadata);
             final List<FileMetaData> childrenData = new ArrayList<FileMetaData>();
             for (JsonNode metaNode : metaJson.get("contents")) {
                 childrenData.add(serverMetadataToLocalFileMetaData(projectId, metaNode.toString()));
             }
             return new FolderMetaData(fileMetaData, childrenData);
         } catch (JsonMappingException e) {
             throw new RuntimeException("Invalid server metadata object.", e);
         } catch (JsonProcessingException e) {
             throw new RuntimeException("Invalid server metadata object.", e);
         } catch (IOException e) {
             throw new RuntimeException("Invalid server metadata object.", e);
         }
     }
 
     private FileMetaData serverMetadataToLocalFileMetaData(String projectId, String metadata) {
         try {
 
             final JsonNode metaJson = new ObjectMapper().readTree(metadata);
             final String path = metaJson.get("path").textValue();
             final Long revision = metaJson.get("revision").longValue();
             final boolean dir = metaJson.get("dir").booleanValue();
             final boolean deleted = metaJson.get("deleted").booleanValue();
             // final Long bytes = metaJson.get("bytes").longValue();
             final String hash = metaJson.get("hash").textValue();
             return new FileMetaData(path, hash, projectId, dir, deleted, revision);
         } catch (Exception e) {
             throw new RuntimeException("Invalid server metadata object.", e);
         }
     }
 
     private DeltaResponse serverDeltaResponseToLocalDeltaResponse(String projectId, String serverDeltaResponse) {
         try {
 
             final JsonNode metaJson = new ObjectMapper().readTree(serverDeltaResponse);
             final Long revision = metaJson.get("currentRevision").longValue();
             final List<FileMetaData> changedMeta = new ArrayList<FileMetaData>();
             ObjectNode resources = (ObjectNode) metaJson.get("resources");
             final Iterator<Map.Entry<String, JsonNode>> it = resources.fields();
             while (it.hasNext()) {
                 JsonNode fileNode = it.next().getValue();
 
                 final FileMetaData fileMetaData = serverMetadataToLocalFileMetaData(projectId, fileNode.toString());
                 changedMeta.add(fileMetaData);
             }
 
             return new DeltaResponse(projectId, revision, changedMeta);
         } catch (Exception e) {
             throw new RuntimeException("Invalid server delta object.", e);
         }
     }
 
     private List<Project> serverProjectListToLocalProject(String project) {
         try {
             final List<Project> projects = new ArrayList<Project>();
             for (final JsonNode projectJson : new ObjectMapper().readTree(project)) {
                 final String id = projectJson.get("id").textValue();
                 final String name = projectJson.get("name").textValue();
                 final Long revision = projectJson.get("revision").longValue();
                 final List<String> user = new ArrayList<String>();
                 for (JsonNode node : projectJson.get("authorizedUsers")) {
                     user.add(node.toString());
                 }
 
                 projects.add(new Project(id, "", revision, name));
             }
             return projects;
         } catch (Exception e) {
             throw new RuntimeException("Invalid server metadata object.", e);
         }
     }
 
     private static class DefaultTrustManager implements X509TrustManager {
 
         @Override
         public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
         }
 
         @Override
         public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
         }
 
         @Override
         public X509Certificate[] getAcceptedIssuers() {
             return null;
         }
     }
 }
