 package pl.psnc.dl.wf4ever;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.UUID;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.HttpStatus;
 import org.junit.Assert;
 
 import pl.psnc.dl.wf4ever.hibernate.HibernateUtil;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.test.framework.JerseyTest;
 import com.sun.jersey.test.framework.WebAppDescriptor;
 
 public class W4ETest extends JerseyTest {
 
     protected WebResource webResource;
     protected final String adminCreds = StringUtils.trim(Base64.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));
     protected final String clientName = "ROSRS testing app written in Ruby";
     protected String accessToken;
     protected String accessToken2;
     protected final String clientRedirectionURI = "OOB"; // will not be used
     protected String clientId;
     protected URI ro;
     protected URI ro2;
     protected final String userId2 = "http://" + UUID.randomUUID().toString();
     protected final String userId = "http://" + UUID.randomUUID().toString();
     protected final String userIdSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
     protected final String userId2Safe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId2.getBytes()));
     protected final String username = "John Doe";
     protected final String username2 = "May Gray";
     protected static final String PROJECT_PATH = System.getProperty("user.dir");
 
 
     public W4ETest() {
         super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
     }
 
 
     public W4ETest(WebAppDescriptor webAppDescriptor) {
         super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").build());
     }
 
 
     @Override
     public void setUp()
             throws Exception {
         super.setUp();
         client().setFollowRedirects(true);
         if (resource().getURI().getHost().equals("localhost")) {
             webResource = resource();
         } else {
             webResource = resource().path("rodl/");
         }
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         clientId = createClient(clientName);
     }
 
 
     @Override
     public void tearDown()
             throws Exception {
         deleteClient(clientId);
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
         super.tearDown();
     }
 
 
     protected String createClient(String clientName) {
         ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
                 .post(ClientResponse.class, clientName + "\r\n" + clientRedirectionURI);
         Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatus());
         String clientId = response.getLocation().resolve(".").relativize(response.getLocation()).toString();
         response.close();
         return clientId;
     }
 
 
     protected ClientResponse createUserWithAnswer(String userId, String username) {
         return webResource.path("users/" + userId).header("Authorization", "Bearer " + adminCreds)
                 .put(ClientResponse.class, username);
     }
 
 
     protected void createUser(String userId, String username) {
         webResource.path("users/" + userId).header("Authorization", "Bearer " + adminCreds)
                 .put(ClientResponse.class, username).close();
 
     }
 
 
     protected String createAccessToken(String userId) {
         ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                 .post(ClientResponse.class, clientId + "\r\n" + userId);
         Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatus());
         String accessToken = response.getLocation().resolve(".").relativize(response.getLocation()).toString();
         response.close();
         return accessToken;
     }
 
 
     protected URI createRO(String accessToken) {
         String uuid = UUID.randomUUID().toString();
         return createRO(uuid, accessToken);
     }
 
 
     protected URI createRO(String uuid, String accessToken) {
         ClientResponse response = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken)
                 .header("Slug", uuid).post(ClientResponse.class);
         URI ro = response.getLocation();
         response.close();
         return ro;
     }
 
 
     protected ClientResponse addFile(URI roURI, String filePath, String accessToken) {
         return webResource.uri(roURI).header("Slug", filePath).header("Authorization", "Bearer " + accessToken)
                 .type("text/plain").post(ClientResponse.class, "lorem ipsum");
     }
 
 
     /**
      * Add an external resource to the RO.
      * 
      * @param roURI
      *            RO URI
      * @param resourceUri
      *            resource URI
      * @param accessToken
      *            access token
      * @return server response
      */
     protected ClientResponse addFile(URI roURI, URI resourceUri, String accessToken) {
         return webResource
                 .uri(roURI)
                 .header("Authorization", "Bearer " + accessToken)
                 .type("application/vnd.wf4ever.proxy")
                 .post(
                     ClientResponse.class,
                     "<rdf:RDF\n" + "   xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\n"
                             + "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" + "   <ore:Proxy>\n"
                             + "     <ore:proxyFor rdf:resource=\"" + resourceUri + "\" />\n" + "   </ore:Proxy>\n"
                             + " </rdf:RDF>");
     }
 
 
     protected ClientResponse updateFile(URI reURI, String accessToken) {
         return webResource.uri(reURI).header("Authorization", "Bearer " + accessToken).type("text/plain")
                 .put(ClientResponse.class, "modification");
     }
 
 
     protected ClientResponse removeFile(URI roURI, String filePath, String accessToken) {
         return webResource.uri(roURI).path(filePath).header("Authorization", "Bearer " + accessToken)
                 .delete(ClientResponse.class);
     }
 
 
     protected ClientResponse addRDFFile(URI roURI, String body, String rdfFilePath, String accessToken) {
        return webResource.uri(ro).header("Authorization", "Bearer " + accessToken).type("application/rdf+xml")
                .header("Slug", rdfFilePath).post(ClientResponse.class, body);
     }
 
 
     protected void deleteAccessToken(String accessToken) {
         webResource.path("accesstokens/" + accessToken).header("Authorization", "Bearer " + adminCreds).delete();
     }
 
 
     protected void deleteUser(String userIdSafe) {
         webResource.path("users/" + userIdSafe).header("Authorization", "Bearer " + adminCreds)
                 .delete(ClientResponse.class).close();
     }
 
 
     protected void deleteClient(String clientId) {
         webResource.path("clients/" + clientId).header("Authorization", "Bearer " + adminCreds).delete();
     }
 
 
     protected void deleteROs() {
         String list = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken).get(String.class);
         if (!list.isEmpty()) {
             String[] ros = list.trim().split("\r\n");
             for (String ro : ros) {
                 webResource.uri(URI.create(ro)).header("Authorization", "Bearer " + accessToken).delete();
             }
         }
     }
 
 
     /**
      * Add an annotation with an annotation body. The annotation will annotate the RO.
      * 
      * @param is
      *            annotation body in Turtle format
      * @param roURI
      *            RO URI
      * @param annotationBodyPath
      *            path of the annotation body
      * @param accessToken
      *            access token
      * @return response from the server
      */
     protected ClientResponse addAnnotation(InputStream is, URI roURI, String annotationBodyPath, String accessToken) {
         return webResource
                 .uri(roURI)
                 .header("Slug", annotationBodyPath)
                 .header("Link",
                     "<" + webResource.uri(ro).getURI().toString() + ">; rel=\"http://purl.org/ao/annotatesResource\"")
                 .header("Authorization", "Bearer " + accessToken).type("application/x-turtle")
                 .post(ClientResponse.class, is);
     }
 
 
     /**
      * Add an annotation without the body.
      * 
      * @param is
      *            annotation description
      * @param roURI
      *            RO URI
      * @param accessToken
      *            access token
      * @return response from the server
      */
     protected ClientResponse addAnnotation(InputStream is, URI roURI, String accessToken) {
         return webResource.uri(roURI).header("Authorization", "Bearer " + accessToken)
                 .type("application/vnd.wf4ever.annotation").post(ClientResponse.class, is);
     }
 
 
     /**
      * Add a folder to the RO.
      * 
      * @param is
      *            request entity
      * @param roURI
      *            RO URI
      * @param folderPath
      *            folder path
      * @param accessToken
      *            RODL access token
      * @return response from server
      */
     protected ClientResponse addFolder(InputStream is, URI roURI, String folderPath, String accessToken) {
         return webResource.uri(roURI).header("Slug", folderPath).header("Authorization", "Bearer " + accessToken)
                 .type("application/vnd.wf4ever.folder").post(ClientResponse.class, is);
     }
 
 
     /**
      * Add a folder entry to a folder.
      * 
      * @param is
      *            request entity
      * @param folderUri
      *            URI of the folder to which the entry will be added
      * @param accessToken
      *            RODL access token
      * @return response from server
      */
     protected ClientResponse addFolderEntry(InputStream is, URI folderUri, String accessToken) {
         return webResource.uri(folderUri).header("Authorization", "Bearer " + accessToken)
                 .type("application/vnd.wf4ever.folderentry").post(ClientResponse.class, is);
     }
 }
