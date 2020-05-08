 package gov.usgs.cida.coastalhazards.rest.data;
 
 import com.google.gson.Gson;
 import gov.usgs.cida.coastalhazards.jpa.TinyGovManager;
 import gov.usgs.cida.coastalhazards.model.TinyGov;
 import gov.usgs.cida.utilities.gov.usa.go.GoUsaGovUtils;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author isuftin
  */
 @Path("/minifier")
 public class MinifyResource {
 
 	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MinifyResource.class);
 	private static final long serialVersionUID = 1L;
     private static TinyGovManager urlManager = new TinyGovManager();
     private static Gson gson = new Gson();
 
 	@GET
 	@Path("/minify/{url:.*}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response minify(@PathParam("url") String url) {
 		Map<String, String> responseMap = new HashMap<String, String>();
 		Response response;
 		try {
 			if (StringUtils.isNotBlank(url)) {
                 TinyGov tinygov = urlManager.load(url);
                 if (tinygov == null) {
                     String encodedUrl = URLEncoder.encode(url, "UTF-8");
                     String minified = GoUsaGovUtils.minify(encodedUrl);
                     String short_url = GoUsaGovUtils.getUrlFromResponse(minified);
                     if (short_url == null) {
                         throw new Exception("Cannot get url from go.usa.gov");
                     }
                     else {
                         tinygov = new TinyGov();
                         tinygov.setFullUrl(url);
                         tinygov.setTinyUrl(short_url);
                         boolean save = urlManager.save(tinygov);
                         if (!save) {
                            LOG.error("Could not save this to the database, this is probably not your biggest problem.");
                         }
                     }
                 }
 				response = Response.ok(new Gson().toJson(tinygov, TinyGov.class)).build();
 			} else {
 				responseMap.put("message", "parameter 'url' may not be missing or blank");
 				response = Response.status(Response.Status.BAD_REQUEST).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 			}
 		} catch (URISyntaxException ex) {
 			responseMap.put("message", ex.getMessage());
 			response = Response.status(Response.Status.BAD_REQUEST).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 		} catch (Exception ex) {
 			responseMap.put("message", ex.getMessage());
 			response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 		}
 		return response;
 	}
 
 	@GET
 	@Path("/expand/{url:.*}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response expand(@PathParam("url") String url) {
 		Map<String, String> responseMap = new HashMap<String, String>();
 		Response response = null;
 		try {
 			if (StringUtils.isNotBlank(url)) {
 				String encodedUrl = URLEncoder.encode(url, "UTF-8");
 				response = Response.ok(GoUsaGovUtils.expand(encodedUrl)).build();
 			} else {
 				responseMap.put("message", "parameter 'url' may not be missing or blank");
 				response = Response.status(Response.Status.BAD_REQUEST).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 			}
 		} catch (URISyntaxException ex) {
 			responseMap.put("message", ex.getMessage());
 			response = Response.status(Response.Status.BAD_REQUEST).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 		} catch (Exception ex) {
 			responseMap.put("message", ex.getMessage());
 			response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 		} finally {
 			return response;
 		}
 	}
 
 	@GET
 	@Path("/clicks/{url:.*}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response clicks(@PathParam("url") String url) {
 		Map<String, String> responseMap = new HashMap<String, String>();
 		Response response = null;
 		try {
 			if (StringUtils.isNotBlank(url)) {
 				String encodedUrl = URLEncoder.encode(url, "UTF-8");
 				response = Response.ok(GoUsaGovUtils.clicks(encodedUrl)).build();
 			} else {
 				responseMap.put("message", "parameter 'url' may not be missing or blank");
 				response = Response.status(Response.Status.BAD_REQUEST).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 			}
 		} catch (URISyntaxException ex) {
 			responseMap.put("message", ex.getMessage());
 			response = Response.status(Response.Status.BAD_REQUEST).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 		} catch (Exception ex) {
 			responseMap.put("message", ex.getMessage());
 			response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Gson().toJson(responseMap, HashMap.class)).build();
 		} finally {
 			return response;
 		}
 	}
 }
