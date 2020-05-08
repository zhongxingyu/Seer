 package net.binout.codestory2013;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @Path("/")
 public class HeinRestService {
 
     static final String Q = "q";
     static final String QUELLE_EST_TON_ADRESSE_EMAIL = "Quelle est ton adresse email";
     static final String MAIL = "binout@gmail.com";
     static final String ES_TU_ABONNE_A_LA_MAILING_LIST = "Es tu abonne a la mailing list";
     static final String OUI = "OUI";
 
 
     @GET
     public Response get(@Context UriInfo uriInfo, @QueryParam(Q) String query) {
         System.out.println("\nGET");
         logAllQueryParameters(uriInfo);
 
        if (QUELLE_EST_TON_ADRESSE_EMAIL.equals(query)) {
             return Response.ok(MAIL).build();
         }
        if (ES_TU_ABONNE_A_LA_MAILING_LIST.equals(query)) {
             return Response.ok(OUI).build();
         }
         return Response.ok("Hein binout?").build();
     }
 
     private void logAllQueryParameters(UriInfo uriInfo) {
         MultivaluedMap<String,String> queryParameters = uriInfo.getQueryParameters();
         Set<Map.Entry<String,List<String>>> entries = queryParameters.entrySet();
         for (Map.Entry<String,List<String>> entry : entries) {
             System.out.println("Parameter : " + entry.getKey());
             for (String value : entry.getValue()) {
                 System.out.println("Value : " + value);
             }
         }
     }
 
     @POST
     public Response post(String message) {
         System.out.println("\nPOST : Message : " + message);
         return Response.status(Response.Status.CREATED).build();
     }
 }
