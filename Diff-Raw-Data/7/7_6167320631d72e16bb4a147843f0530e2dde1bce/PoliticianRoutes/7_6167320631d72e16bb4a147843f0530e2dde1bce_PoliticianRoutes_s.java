 package be.noselus.service;
 
 import be.noselus.model.Person;
 import be.noselus.pictures.PictureManager;
 import be.noselus.repository.PoliticianRepository;
 import org.apache.commons.io.IOUtils;
 import spark.Request;
 import spark.Response;
 import spark.Route;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.io.IOException;
 import java.io.InputStream;
 
 import static be.noselus.service.RoutesHelper.NOT_FOUND;
 import static spark.Spark.get;
 
 @Singleton
 public class PoliticianRoutes implements Routes {
 
     public static final String IMAGE_JPEG_CHARSET_UTF_8 = "image/jpeg;charset=utf-8";
     private final PictureManager pictureManager;
     private final PoliticianRepository politicianRepository;
     private final RoutesHelper helper;
 
     @Inject
     public PoliticianRoutes(final PictureManager pictureManager, final PoliticianRepository politicianRepository, final RoutesHelper helper) {
         this.pictureManager = pictureManager;
         this.politicianRepository = politicianRepository;
         this.helper = helper;
     }
 
     public void setup() {
         get(new JsonTransformer("/politicians") {
             @Override
             public Object myHandle(final Request request, final Response response) {
                 return helper.resultAs("politicians", politicianRepository.getPoliticians());
             }
         });
 
         get(new JsonTransformer("/politicians/:id") {
             @Override
             public Object myHandle(final Request request, final Response response) {
                 final String params = request.params(":id");
                 final Person politicianById = politicianRepository.getPoliticianById(Integer.parseInt(params));
                 if (politicianById == null) {
                     response.status(NOT_FOUND);
                     return null;
                 }
                 return helper.resultAs("politician", politicianById);
             }
         });
 
        get(new Route("/politicians/picture/:id") {
             @Override
             public Object handle(final Request request, final Response response) {
                 try {
                     final String id = request.params(":id");
                     byte[] out;
                     InputStream is = pictureManager.get(Integer.valueOf(id));
 
                     if (is == null) {
                         response.status(NOT_FOUND);
                         return null;
                     } else {
                         out = IOUtils.toByteArray(is);
                         response.raw().setContentType(IMAGE_JPEG_CHARSET_UTF_8);
                         response.raw().getOutputStream().write(out, 0, out.length);
                         response.header("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");
                         return out;
                     }
                 } catch (NumberFormatException | IOException e) {
                     response.status(NOT_FOUND);
                     return null;
                 }
             }
         });
 
        get(new Route("/politicians/picture/:id/*/*") {
             @Override
             public Object handle(final Request request, final Response response) {
                 try {
                     final String id = request.params(":id");
                     int width = Integer.valueOf(request.splat()[0]);
                     int height = Integer.valueOf(request.splat()[1]);
 
                     response.raw().setContentType(IMAGE_JPEG_CHARSET_UTF_8);
                     pictureManager.get(Integer.valueOf(id), width, height, response.raw().getOutputStream());
                     return null;
                 } catch (NumberFormatException | IOException e) {
                     response.status(NOT_FOUND);
                     return null;
                 }
             }
         });
     }
 
 }
