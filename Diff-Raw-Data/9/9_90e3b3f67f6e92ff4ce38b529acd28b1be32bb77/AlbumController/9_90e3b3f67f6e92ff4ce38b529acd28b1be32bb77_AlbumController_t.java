 package org.guiceae.main.web;
 
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.sun.jersey.api.view.Viewable;
 import org.guiceae.main.model.Album;
 import org.guiceae.main.model.Photo;
 import org.guiceae.main.repositories.AlbumRepository;
 import org.guiceae.main.repositories.PhotoRepository;
 
 import javax.annotation.security.RolesAllowed;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: boui
  * Date: 7/3/12
  */
 @Path("app/album")
 public class AlbumController {
 
     @Inject
     AlbumRepository albumRepository;
     @Inject
     PhotoRepository photoRepository;
 
     @GET
     @Path("/get/{albumId}")
     @Produces(MediaType.APPLICATION_JSON)
     public Album getAlbumById(@PathParam("albumId") Long albumId) {
         return albumRepository.getById(albumId);
     }
 
     @GET
     @Path("/browse/{albumId}")
     @RolesAllowed("cm")
     public Viewable browse(@PathParam("albumId") Long albumId,
                            @QueryParam("mode") String mode,
                            @Context HttpServletRequest request) {
         Map<String, Object> map = new HashMap<String, Object>();
         Collection<Photo> photos = photoRepository.getByAlbumId(albumId);
         if ("ckeditor".equals(mode)) {
             map.put("callbackCode", "window.opener.CKEDITOR.tools.callFunction(" + request.getParameter("CKEditorFuncNum") + ", src);");
             map.put("funcNum", request.getParameter("CKEditorFuncNum"));
         } else {
             map.put("callbackCode", "window.opener.imageSelected(src);");
         }
         map.put("uploadUrl", BlobstoreServiceFactory.getBlobstoreService().createUploadUrl("/app/ckupload"));
         map.put("photos", photos);
         map.put("mode", mode);
 
         return new Viewable("/ckBrowse.jsp", map);
     }
 
     @GET
     @Path("/new")
     @RolesAllowed("cm")
     public Viewable newAlbumPage() {
        Album album = new Album();
        album.setId(0L);
        return new Viewable("/album.jsp", album);
     }
 
     @GET
     @Path("/update/{id}")
     @RolesAllowed("cm")
     @Consumes(MediaType.APPLICATION_JSON)
     public Viewable updateAlbumPage(@PathParam("id") Long id) {
         return new Viewable("/album.jsp", albumRepository.getById(id));
     }
 
     @DELETE
     @Path("/delete/{id}")
     @RolesAllowed("cm")
     public Response deletePage(@PathParam("id") Long id) throws URISyntaxException {
         Collection<Photo> photosInALbum = photoRepository.getByAlbumId(id);
         if (photosInALbum.isEmpty()) {
             albumRepository.deleteById(id);
         }
         return Response.seeOther(new URI("/app/album/gallery")).build();
     }
 
     @POST
     @Path("/addNew")
     @RolesAllowed("cm")
     public Response mergeAlbum(@FormParam("id") Long id,
                                @FormParam("title") String title,
                                @FormParam("description") String desc) throws URISyntaxException {
         Album info = new Album(title, desc);
        if (!id.equals(0L)){
            info.setId(id);
        }
         albumRepository.mergeAlbum(info);
         return Response.seeOther(new URI("/app/album/gallery")).build();
     }
 
     @GET
     @Path("/gallery")
     public Viewable getGallery() {
         Map<String, List<? extends Object>> map = new HashMap<String, List<? extends Object>>();
         map.put("albums", albumRepository.getAll());
         return new Viewable("/gallery.jsp", map);
     }
 
     @GET
     @Path("/photos/{albumId}")
     @Produces(MediaType.APPLICATION_JSON)
     public Collection<Photo> getByAlbumId(@PathParam("albumId") Long albumId) {
         return photoRepository.getByAlbumId(albumId);
     }
 
 }
