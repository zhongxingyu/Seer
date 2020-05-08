 package de.anycook.api;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.sql.SQLException;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import de.anycook.db.mysql.DBRecipe;
 import org.apache.log4j.Logger;
 import de.anycook.tag.Tag;
 import de.anycook.user.User;
 import de.anycook.utils.enumerations.ImageType;
 import de.anycook.ingredient.Ingredient;
 import de.anycook.newrecipe.NewRecipe;
 import de.anycook.recipe.Recipe;
 import de.anycook.session.Session;
 import de.anycook.step.Step;
 import org.apache.lucene.queryparser.classic.ParseException;
 
 
 @Path("/recipe")
 @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
 public class RecipeGraph {
 	Logger logger = Logger.getLogger(getClass());
 
 	@SuppressWarnings("unchecked")
 	@GET
 	public List<String> getAll(@QueryParam("userId") Integer userId){
         try{
             if(userId != null)
                 return Recipe.getRecipenamesfromUser(userId);
             else
                 return Recipe.getAll();
         } catch (SQLException e){
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	/**
 	 * Number of recipes
 	 * @return
 	 */
 	@GET
 	@Path("number")
 	public Integer getNum(){
         try {
             return Recipe.getTotal();
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	/**
 	 * returns the recipe of the day
 	 * @return
 	 */
 	@GET
 	@Path("oftheday")
	public String getRecipeOfTheDay(){
         try {
            return Recipe.getTagesRezept();
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@GET
 	@Path("{recipeName}")
 	public Recipe getRecipe(@PathParam("recipeName") String recipeName){
         try {
             return Recipe.init(recipeName);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBRecipe.RecipeNotFoundException e) {
             logger.warn(e);
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         }
     }
 	
 	@GET
 	@Path("{recipeName}/ingredients")
 	public List<Ingredient> getRecipeIngredients(@PathParam("recipeName") String recipeName){
         try {
             return Ingredient.loadByRecipe(recipeName);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@GET
 	@Path("{recipeName}/tags")
 	public List<String> getRecipeTags(@PathParam("recipeName") String recipeName){
         try {
             return Tag.loadTagsFromRecipe(recipeName);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@GET
 	@Path("{recipeName}/steps")
 	public List<Step> getRecipeSteps(@PathParam("recipeName") String recipeName){
         try {
             return Step.loadRecipeSteps(recipeName);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 
     //version
 	@GET
 	@Path("{recipeName}/{versionid}")
 	public Recipe getVersion(@PathParam("recipeName") String recipeName,
 			@PathParam("versionid") int versionid){
         try {
             return Recipe.init(recipeName, versionid);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBRecipe.RecipeNotFoundException e) {
             logger.warn(e);
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         }
     }
 
     @GET
     @Path("{recipeName}/{versionId}/ingredients")
     public List<Ingredient> getVersionIngredients(@PathParam("recipeName") String recipeName,
                                @PathParam("versionId") int versionId){
         try {
             return Ingredient.loadByRecipe(recipeName, versionId);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     @GET
     @Path("{recipeName}/{versionId}/steps")
     public List<Step> getVersionSteps(@PathParam("recipeName") String recipeName,
                                     @PathParam("versionId") int versionId){
         try {
             return Step.loadRecipeSteps(recipeName, versionId);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@GET
 	@Path("{recipeName}/image")
 	@Produces("image/png")
 	public Response getImage(@PathParam("recipeName") String recipeName,
 			@DefaultValue("small") @QueryParam("type") String typeString){
 		ImageType type = ImageType.valueOf(typeString.toUpperCase());
 		try {
 			return Response.temporaryRedirect(Recipe.getRecipeImage(recipeName, type)).build();
 		} catch (URISyntaxException e) {
 			logger.error(e);
 			throw new WebApplicationException(400);
 		} catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@GET
 	@Path("{recipeName}/schmeckt")
 	public Boolean checkSchmeckt(@PathParam("recipeName") String recipeName,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession());
 		session.checkLogin();
         try {
             return session.checkSchmeckt(recipeName);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@PUT
 	@Path("{recipeName}/schmeckt")
 	public void schmeckt(@PathParam("recipeName") String recipeName,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
         try {
             boolean schmeckt = session.checkSchmeckt(recipeName);
             if(!schmeckt)
                 session.makeSchmeckt(recipeName);
         } catch (SQLException e){
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
 		
 	}
 	
 	@DELETE
 	@Path("{recipeName}/schmeckt")
 	public void schmecktNicht(@PathParam("recipeName") String recipeName,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 
         try {
             boolean schmeckt = session.checkSchmeckt(recipeName);
             if(schmeckt)
                 session.removeSchmeckt(recipeName);
         } catch (SQLException e){
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
 
 	}
 	
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	public void saveRecipe(@Context HttpHeaders hh,
 			@Context HttpServletRequest request,			
 			NewRecipe newRecipe){
 		logger.info("want to save recipe");
 		Session  session = Session.init(request.getSession());
 		session.checkLogin(hh.getCookies());
 		
 		User user = session.getUser();
 		
 		if(newRecipe == null)
 			throw new WebApplicationException(400);
 
 
         try {
             if(!newRecipe.save(user.getId()))
                 throw new WebApplicationException(Response.Status.BAD_REQUEST);
         } catch (SQLException | IOException | ParseException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
     }
 		
 		
 //		logger.debug("recipe:"+recipeData);
 //		Session session = Session.init(request.getSession());
 //		Map<String, Cookie> cookies = hh.getCookies();
 //		JSONParser parser = new JSONParser();
 //		JSONObject json = null;
 //		
 //		if(userId != -1){
 //			if(session.checkLogin(cookies)){
 //				 User user = session.getUser();
 //				 if(user.getId() != userId)
 //					 throw new WebApplicationException(401);
 //			}
 //		}
 //		
 //		if(recipeData != null){			
 //			try {
 //				json = (JSONObject) parser.parse(recipeData);
 //			}catch (ParseException e) {
 //	//			throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
 //			}
 //			
 //			if(json != null){
 //				NewRecipe newRecipe;
 //				try {
 //					newRecipe = NewRecipe.initWithJSON(recipeName,json);
 //				} catch (ParseException | NewRecipeException e) {
 //					throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
 //				}
 //				newRecipe.saveNewVersion();
 ////				if(json.containsKey("mongoid"))
 ////					CouchDB.delete(json.get("mongoid").toString(), 
 ////							Integer.parseInt(json.get("userId").toString()));
 //			}
 //		}
 //		
 //		if(tags != null){
 //			try {
 //				JSONArray tagsJSON = (JSONArray)parser.parse(tags);
 //				
 //				for(int i = 0; i<tagsJSON.size(); i++){
 //					Recipe.suggestTag(recipeName, tagsJSON.get(i).toString(), userId);
 //				}
 //			} catch (ParseException e) {
 //				throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
 //			}
 //			
 //		}
 //		return Response.ok("true").build();			
 //		
 //		
 //	}
 }
