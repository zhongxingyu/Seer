 package com.easytag.web.webservices;
 
 import com.easytag.core.entity.jpa.EasyTag;
 import com.easytag.core.managers.TagManagerLocal;
 import com.easytag.exceptions.TagException;
 import com.easytag.json.utils.JsonResponse;
 import com.easytag.json.utils.ResponseConstants;
 import com.easytag.json.utils.SimpleResponseWrapper;
 import com.easytag.json.utils.TagExceptionWrapper;
 import com.easytag.web.utils.SessionUtils;
 import com.google.gson.Gson;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 /**
  * REST Web Service
  *
  * @author Vitaly
  */
 @Path("Tag")
 @Stateless
 public class TagResource {
 
     @Context
     private UriInfo context;
     @EJB
     TagManagerLocal tagMan;
 
     /**
      * Creates a new instance of TagResource
      */
     public TagResource() {
     }
 
     @GET
     @Path("getEasyTag")
     public String getEasyTagById(@Context HttpServletRequest req, @QueryParam("tagId") Long tagId){
         HttpSession session = req.getSession(false);
         Long currentUserId = SessionUtils.getUserId(session);
         EasyTag eTag = tagMan.getEasyTagById(tagId);
         JsonResponse<EasyTag> jr = new JsonResponse<EasyTag>(ResponseConstants.OK, null, eTag);
         return SimpleResponseWrapper.getJsonResponse(jr);
     }
     
     @GET
     @Path("getEasyTags")
     public String getEasyTagsInPhoto(@Context HttpServletRequest req,  @QueryParam("photoId") Long photoId) {
         try {
             HttpSession session = req.getSession(false);
             Long currentUserId = SessionUtils.getUserId(session);
 
             if (currentUserId == null) {
                 throw new TagException("you sholud login first", ResponseConstants.NOT_AUTHORIZED_CODE);
             }
             List<EasyTag> eTags = tagMan.getEasyTagsInPhoto(photoId);
             JsonResponse<List<EasyTag>> jr = new JsonResponse<List<EasyTag>>(ResponseConstants.OK, null, eTags);
             return SimpleResponseWrapper.getJsonResponse(jr);
         } catch (TagException e) {
             return TagExceptionWrapper.wrapException(e);
         }
     }
     
     @POST
     @Path("create")
     public String createEasyTag(@Context HttpServletRequest req,@FormParam("data") String data) {
         try {
             HttpSession session = req.getSession(false);
             Long currentUserId = SessionUtils.getUserId(session);
 
             if (currentUserId == null) {
                 throw new TagException("you sholud login first", ResponseConstants.NOT_AUTHORIZED_CODE);
             }
 
             if (data == null) {
                 throw new TagException("data is null");
             }
 
             //TODO(Vitaly): wrap this string with trycatch block throwing TagException
             EasyTag eTag = new Gson().fromJson(data, EasyTag.class);
 
             if (eTag == null) {
                 throw new TagException("cannot deserialize EasyTag");
             }
            // createEasyTag(Long userId, Long photoId, String name, String description, Double x, Double y, Double width, Double height)
             eTag = tagMan.createEasyTag(currentUserId, eTag.getPhotoId(), eTag.getName(),
                     eTag.getDescription(), eTag.getX(), eTag.getY(), eTag.getWidth(), eTag.getHeight());
             JsonResponse<EasyTag> jr = new JsonResponse<EasyTag>(ResponseConstants.OK, null, eTag);
             return SimpleResponseWrapper.getJsonResponse(jr);
         } catch (TagException e) {
             return TagExceptionWrapper.wrapException(e);
         }
     }
     
     @GET
     @Path("removeEasyTag")
     public String removeEasyTagById(@Context HttpServletRequest req, @QueryParam("tagId") Long tagId){
         try {
             HttpSession session = req.getSession(false);
             Long currentUserId = SessionUtils.getUserId(session);
 
             if (currentUserId == null) {
                 throw new TagException("you sholud login first", ResponseConstants.NOT_AUTHORIZED_CODE);
             }
             
             tagMan.removeEasyTag(currentUserId, tagId);            
             JsonResponse<String> jr = new JsonResponse<String>(ResponseConstants.OK, null, ResponseConstants.YES);
             return SimpleResponseWrapper.getJsonResponse(jr);            
         } catch (TagException e) {
             return TagExceptionWrapper.wrapException(e);
         }
     }
     
     @POST
     @Path("modify")
     public String modifyEasyTag(@Context HttpServletRequest req,@FormParam("data") String data) {
         try {
             HttpSession session = req.getSession(false);
             Long currentUserId = SessionUtils.getUserId(session);
 
             if (currentUserId == null) {
                 throw new TagException("you sholud login first", ResponseConstants.NOT_AUTHORIZED_CODE);
             }
 
             if (data == null) {
                 throw new TagException("data is null");
             }
 
             //TODO(Vitaly): wrap this string with trycatch block throwing TagException
             EasyTag eTag = new Gson().fromJson(data, EasyTag.class);
 
             if (eTag == null) {
                 throw new TagException("cannot deserialize EasyTag");
             }
            // createEasyTag(Long userId, Long photoId, String name, String description, Double x, Double y, Double width, Double height)
             eTag = tagMan.modifyEasyTag(currentUserId, eTag.getPhotoId(), eTag.getName(),
                     eTag.getDescription(), eTag.getX(), eTag.getY(), eTag.getWidth(), eTag.getHeight());
             JsonResponse<EasyTag> jr = new JsonResponse<EasyTag>(ResponseConstants.OK, null, eTag);
             return SimpleResponseWrapper.getJsonResponse(jr);
         } catch (TagException e) {
             return TagExceptionWrapper.wrapException(e);
         }
     }
     
     /**
      * Retrieves representation of an instance of com.easytag.web.utils.TagResource
      * @return an instance of java.lang.String
      */
     @GET
     @Produces("application/json")
     public String getJson() {
         //TODO return proper representation object
         throw new UnsupportedOperationException();
     }
 
     /**
      * PUT method for updating or creating an instance of TagResource
      * @param content representation for the resource
      * @return an HTTP response with content of the updated or created resource.
      */
     @PUT
     @Consumes("application/json")
     public void putJson(String content) {
     }
 }
