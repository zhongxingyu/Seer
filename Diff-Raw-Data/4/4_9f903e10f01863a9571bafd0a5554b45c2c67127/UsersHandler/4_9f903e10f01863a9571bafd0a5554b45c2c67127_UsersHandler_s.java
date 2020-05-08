 package net.thumbtack.updateNotifierBackend.resourceHandlers;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.inject.Singleton;
 import javax.ws.rs.BadRequestException;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.NotFoundException;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 import org.eclipse.jetty.http.HttpStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonSyntaxException;
 
 import static net.thumbtack.updateNotifierBackend.UpdateNotifierBackend.getDatabaseService;
 import net.thumbtack.updateNotifierBackend.database.DatabaseException;
 import net.thumbtack.updateNotifierBackend.database.entities.Resource;
 import net.thumbtack.updateNotifierBackend.database.entities.Tag;
 import net.thumbtack.updateNotifierBackend.updateChecker.UpdateChecker;
 
 @Path("/users")
 @Singleton
 public class UsersHandler {
 
 	// TODO Why do not I use this and create new gSon object every time?
 	private static final Gson GSON = new GsonBuilder().setDateFormat(
 			"yyyy-MM-dd hh:mm:ss.S").create();
 	private static final Logger log = LoggerFactory
 			.getLogger(UsersHandler.class);
 
 	@Path("signin")
 	@GET
 	public long signIn(@QueryParam("email") String userEmail) {
 		log.trace("Sign in: " + userEmail);
 		if (userEmail == null) {
 			throw new BadRequestException(
 					"Missing 'email' parameter in the url");
 		}
 		Long userId = null;
 		try {
 			userId = getDatabaseService().getUserIdByEmailOrAdd(userEmail);
 		} catch (DatabaseException e) {
 			log.error("Database request failed. Sign in failed");
 			throw (new WebApplicationException("Database get account error"));
 		}
 		return userId;
 	}
 
 	@Path("/{id}/resources")
 	@POST
 	@Consumes({ "application/json" })
 	public Response addUserResource(@PathParam("id") long userId,
 			String resourceJson) {
 		log.trace("Add resource");
 		Resource resource = parseResource(resourceJson);
 		if (resource == null) {
 			log.debug("Database add request failed: resource expected in request body");
 			throw new BadRequestException(
 					"Database add request failed: resource expected in request body");
 		}
 		try {
 			getDatabaseService().addResource(userId, resource);
 		} catch (DatabaseException e) {
 			log.debug("Database add request failed. Add resources bad request");
 			throw (new BadRequestException("Incorrect params"));
 		}
 
 		return Response.status(HttpStatus.CREATED_201)
 				.entity(resource.getId().toString()).build();
 	}
 
 	@Path("/{id}/resources")
 	@GET
 	@Produces({ "application/json" })
 	public String getUserResources(@PathParam("id") long userId,
 			@DefaultValue("") @QueryParam("tags") String tagsString) {
 		log.trace("Get resources");
 		Long[] tags = parseTags(tagsString);
 		List<Resource> resources = null;
 		try {
 			resources = getDatabaseService().getResourcesByIdAndTags(userId,
 					tags);
 		} catch (DatabaseException e) {
 			log.debug("Database get request failed. Get resources bad request");
 			throw (new BadRequestException("Incorrect userId"));
 		}
 		return GSON.toJson(resources);
 	}
 
 	@Path("/{id}/resources/{resourceId}")
 	@GET
 	@Produces({ "application/json" })
 	public String getUserResource(@PathParam("id") long userId,
 			@PathParam("resourceId") long resourceId) {
 		log.trace("Get resource");
 		Resource res = getDatabaseService().getResource(userId, resourceId);
 		if (res == null) {
 			log.debug("Database get request failed. Get resource not found");
 			throw (new NotFoundException("Resource not exist"));
 		}
 		return GSON.toJson(res);
 	}
 	
 	@Path("/{id}/resources")
 	@DELETE
	public void deleteUserResources(@PathParam("id") long userId,
 			@DefaultValue("") @QueryParam("tags") String tagsString) {
 		log.trace("Delete resources");
 		Long[] tags = parseTags(tagsString);
 		try {
 			if (!getDatabaseService().deleteResourcesByIdAndTags(userId, tags)) {
 				log.debug("Database delete request failed. Delete resources bnot found");
 				throw (new NotFoundException());
 			}
 			//TODO make it beautiful
 		} catch (DatabaseException e) {
 			log.debug("Database delete request failed. Delete resources bnot found");
 			throw (new NotFoundException());
 		}
 	}
 
 	@Path("/{id}/resources/{resourceId}")
 	@DELETE
 	@Produces({ "application/json" })
 	public void deleteUserResource(@PathParam("id") long userId,
 			@PathParam("resourceId") long resourceId) {
 		log.trace("Get resource");
 
 		try {
 			if (!getDatabaseService().deleteResource(userId, resourceId)) {
 				log.debug("Database delete request failed. Delete resource not found");
 				throw (new NotFoundException("Resource not exist"));
 			}
 			//TODO Make it beautiful
 		} catch (DatabaseException e) {
 			log.debug("Database delete request failed. Delete resource not found");
 			throw (new NotFoundException("Resource not exist"));
 		}
 	}
 
 	@Path("/{id}/resources")
 	@PUT
 	@Consumes({ "application/json" })
 	public void editUserResource(@PathParam("id") long userId,
 			String resourceJson) {
 		log.trace("Edit resource");
 		Resource resource = parseResource(resourceJson);
 
 		try {
 			if (!getDatabaseService().editResource(userId, resource)) {
 				log.debug("Database edit request failed. Edit resources bad request");
 				throw (new BadRequestException());
 			}
 			// TODO make it beautiful
 		} catch (DatabaseException e) {
 			log.debug("Database edit request failed. Edit resources bad request");
 			throw (new BadRequestException());
 		}
 
 	}
 
 	@Path("/{id}/tags")
 	@GET
 	@Produces({ "application/json" })
 	public String getUserTags(@PathParam("id") long userId) {
 		log.trace("Get tags");
 		Set<Tag> tags = getDatabaseService().getTags(userId);
 		if (tags == null) {
 			log.debug("Database get request failed. Get tags not found");
 			throw (new NotFoundException());
 		}
 		return GSON.toJson(tags);
 	}
 
 	@Path("/{id}/tags")
 	@POST
 	@Consumes({ "application/json" })
 	public Response addTag(@PathParam("id") long userId, String tagNameJson) {
 		log.trace("Add tag");
 		String tagName = parseTagName(tagNameJson);
 		Long id = getDatabaseService().addTag(userId, tagName);
 		if (id == null) {
 			log.debug("Database add request failed. Edit resources bad request");
 			throw (new BadRequestException());
 		}
 		return Response.status(HttpStatus.CREATED_201).entity(id.toString())
 				.build();
 	}
 	@Path("/{id}/tags/{tagId}")
 	@PUT
 	@Consumes({ "application/json" })
 	public void editTag(@PathParam("id") long userId,
 			@PathParam("tagId") long tagId, String tagNameJson) {
 		log.trace("Edit tag");
 		String tagName = null;
 		try {
 			tagName = GSON.fromJson(tagNameJson, String.class);
 		} catch (JsonSyntaxException e) {
 			throw new BadRequestException("Invalid json param: tag name");
 		}
 		if (tagName == null) {
 			log.debug("Database put request failed. Tag name can't be null.");
 			throw new BadRequestException(
 					"Database put request failed. Tag name can't be null.");
 		}
 		if (!getDatabaseService().editTag(userId, tagId, tagName)) {
 			log.debug("Database add request failed. Edit resources bad request");
 			throw (new BadRequestException());
 		}
 	}
 
 	private static Resource parseResource(String resourceJson) {
 		try {
 			Resource res = GSON.fromJson(resourceJson, Resource.class);
 			if (res == null || res.getDomPath() == null || res.getSheduleCode() < 0
 					|| res.getSheduleCode() > UpdateChecker.MAGIC_NUMBER
 					|| res.getUrl() == null) {
 				log.debug("Resource parsing error: bad or expecting params");
 				throw (new BadRequestException("Json parsing error"));
 			}
 			return res;
 		} catch (JsonSyntaxException ex) {
 			log.debug("Resource parsing error");
 			throw (new BadRequestException("Json parsing error"));
 		}
 	}
 
 
 	private String parseTagName(String tagNameJson) {
 		try{
 			String tagName = GSON.fromJson(tagNameJson, String.class);
 			if (tagName == null) {
 				log.debug("Database post request failed. Tag name can't be null.");
 				throw new BadRequestException(
 						"Database post request failed. Tag name can't be null.");
 			}
 			return tagName;
 		} catch (JsonSyntaxException ex) {
 			log.debug("Tag name parsing error");
 			throw (new BadRequestException("Tag name parsing error"));
 		}
 	}
 
 	private static Long[] parseTags(String tagsString) {
 		Long[] tags;
 		if ("".equals(tagsString) || tagsString == null) {
 			tags = null;
 		} else {
 			String[] tagsStrings = tagsString.split(",");
 			tags = new Long[tagsStrings.length];
 			try {
 				for (int i = 0; i < tagsStrings.length; i += 1) {
 					tags[i] = Long.parseLong(tagsStrings[i]);
 				}
 			} catch (NumberFormatException ex) {
 				log.debug("Tags id parsing error");
 				throw (new BadRequestException("Tags id parsing error"));
 			}
 		}
 		return tags;
 	}
 }
