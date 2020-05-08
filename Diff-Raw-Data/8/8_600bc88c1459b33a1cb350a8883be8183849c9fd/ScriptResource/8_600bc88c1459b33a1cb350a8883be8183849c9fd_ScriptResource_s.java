 package cue.hegemon.example;
 
import com.cueup.hegemon.LoadError;
import com.cueup.hegemon.LoadPath;
import com.cueup.hegemon.Script;
import com.cueup.hegemon.ScriptCache;
 import com.google.common.io.ByteStreams;
 
 import javax.script.ScriptException;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 @Path("/script/{name}")
 @Produces(MediaType.APPLICATION_JSON)
 public class ScriptResource {
   private static ScriptCache scriptCache = new ScriptCache(new LoadPath());
 
   @POST
   public Object post(@PathParam("name") String name, @Context UriInfo uriInfo, InputStream request)
       throws IOException {
     return run(name, "POST", uriInfo.getQueryParameters(), ByteStreams.toByteArray(request));
   }
 
   @GET
   public Object get(@PathParam("name") String name, @Context UriInfo uriInfo) {
     return run(name, "GET", uriInfo.getQueryParameters(), null);
   }
 
   private Object run(String name, String method, Map<String, List<String>> params, final byte[] postData) {
     try {
       final Script script = scriptCache.get("script/" + name + ".js", params.containsKey("reload"));
       script.load("common");
       Object paramsObject = script.run("getParamsAsObject", params);
       return script.run(method, paramsObject, postData);
     } catch (LoadError loadError) {
       throw new WebApplicationException(Response.Status.NOT_FOUND);
     } catch (ScriptException e) {
       // TODO(kevinclark): Return proper status code.
       return e.getMessage() + "(" + e.getLineNumber() + ":" + e.getColumnNumber() + ")";
     }
   }
 }
