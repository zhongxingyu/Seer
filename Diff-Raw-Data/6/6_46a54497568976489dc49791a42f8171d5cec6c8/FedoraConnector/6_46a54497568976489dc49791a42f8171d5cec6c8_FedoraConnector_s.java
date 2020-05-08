 package dk.statsbiblioteket.doms.authchecker.fedoraconnector;
 
 import dk.statsbiblioteket.doms.authchecker.exceptions.InvalidCredentialsException;
 import dk.statsbiblioteket.doms.authchecker.exceptions.URLNotFoundException;
 import dk.statsbiblioteket.doms.authchecker.exceptions.FedoraException;
 import dk.statsbiblioteket.doms.authchecker.exceptions.ResourceNotFoundException;
 import dk.statsbiblioteket.doms.webservices.Base64;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.ClientResponse;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.net.URLEncoder;
 import java.io.UnsupportedEncodingException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Oct 4, 2010
  * Time: 10:54:07 AM
  * To change this template use File | Settings | File Templates.
  */
 public class FedoraConnector {
 
 
     private static Client client = Client.create();
     private WebResource restApi;
 
 
     public FedoraConnector(String location) {
         restApi = client.resource(location);
     }
 
     public void getDatastreamProfile(String pid,
                                      String dsid,
                                      String username,
                                      String password)
             throws
             InvalidCredentialsException,
             FedoraException,
             ResourceNotFoundException {
          try {
            String profile = restApi.path(URLEncoder.encode(pid, "UTF-8"))
                    .path("/objects/datastreams/")
                     .path(URLEncoder.encode(dsid, "UTF-8"))
                     .header("Authorization", credsAsBase64(username,password))
                     .get(String.class);
 
         } catch (UnsupportedEncodingException e) {
             throw new Error("UTF-8 not known",e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new InvalidCredentialsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()){
                 throw new ResourceNotFoundException("The datastream is not found",e);
             }
             else {
                 throw new FedoraException("Something went wrong with Fedora",e);
             }
         }
     }
 
     public String getObjectWithThisURL(String url,
                                        String username,
                                        String password)
             throws InvalidCredentialsException,
                    URLNotFoundException,
                    FedoraException{
         //TODO sanitize label
         try {// TODO duplicated from doms server
             String query = "select $object\n"
                            + "from <#ri>\n"
                            + "where $object <fedora-model:label> '"+url+"'"
                            + "and $object <fedora-model:state> <info:fedora/fedora-system:def/model#Active>";
 
             String objects = restApi
                     .path("/risearch")
                     .queryParam("type", "tuples")
                     .queryParam("lang", "iTQL")
                     .queryParam("format", "CSV")
                     .queryParam("flush","true")
                     .queryParam("stream","on")
                     .queryParam("query", query)
                     .header("Authorization", credsAsBase64(username,password))
                     .post(String.class);
             String[] lines = objects.split("\n");
             List<String> foundobjects = new ArrayList<String>();
             for (String line : lines) {
                 if (line.startsWith("\"")){
                     continue;
                 }
                 if (line.startsWith("info:fedora/")){
                     line = line.substring("info:fedora/".length());
                 }
                 foundobjects.add(line);
             }
             if (!foundobjects.isEmpty()){
                 return foundobjects.get(0);
             } else {
                 throw new URLNotFoundException("The provided url '"+url+"' is not found in the repository");
             }
         }  catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new InvalidCredentialsException(
                         "Invalid Credentials Supplied",
                         e);
             } else {
                 throw new FedoraException("Something went wrong with Fedora",e);
             }
         }
     }
 
 
     protected String credsAsBase64(String username, String password){
         String preBase64 = username + ":" + password;
         String base64 = Base64.encodeBytes(preBase64.getBytes());
         return "Basic "+base64;
     }
 }
