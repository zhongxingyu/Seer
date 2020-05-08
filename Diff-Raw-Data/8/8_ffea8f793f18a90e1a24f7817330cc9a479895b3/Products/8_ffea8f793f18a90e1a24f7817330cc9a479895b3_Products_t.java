 package controllers;
 
 import com.amazonaws.services.s3.model.AmazonS3Exception;
 import models.Category;
 import models.Product;
 import models.User;
 import play.Logger;
 import play.cache.Cache;
 import play.data.Upload;
 import play.data.binding.Binder;
 import play.db.Model;
 import play.exceptions.TemplateNotFoundException;
 import play.i18n.Messages;
 import play.modules.s3blobs.S3Blob;
 import play.mvc.After;
 import play.mvc.Http;
 import play.mvc.With;
 import utils.EmailQueue;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
import java.util.*;
 import java.util.List;
 
 @CRUD.For(Product.class)
 @With(Secure.class)
 public class Products extends CRUD {
 
     public static final int THUMBNAIL_SIZE = 65;
    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT+4");
 
     public static void blank() throws Exception {
         renderArgs.put("blank", true);
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         Model object = (Model) constructor.newInstance();
         try {
             render(type, object);
         } catch (TemplateNotFoundException e) {
             render("CRUD/blank.html", type, object);
         }
     }
 
     // TODO: there shall be a way to pass id of new created Product to sendNotofications
     public static void create() throws Exception {
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         Product object = (Product) constructor.newInstance();
         bindAndProcessPhoto(object, false);
        object.setDate(Calendar.getInstance(DEFAULT_TIME_ZONE).getTime()); // todo: we need to receive client time
         if (Security.isConnected()) {
             User user = User.find("byEmail", Security.connected()).first();
             object.setAuthor(user);
         }
         validation.valid(object);
         if (validation.hasErrors()) {
             renderArgs.put("error", Messages.get("crud.hasErrors"));
             try {
                 render(request.controller.replace(".", "/") + "/blank.html", type, object);
             } catch (TemplateNotFoundException e) {
                 render("CRUD/blank.html", type, object);
             }
         }
         object._save();
         flash.success(Messages.get("crud.created", type.modelName));
         sendNotifications(object._key().toString());
         if (params.get("_save") != null) {
             redirect(request.controller + ".list");
         }
         if (params.get("_saveAndAddAnother") != null) {
             redirect(request.controller + ".blank");
         }
         redirect(request.controller + ".show", object._key());
     }
 
     public static void save(String id) throws Exception {
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         Product object = (Product) type.findById(id);
         notFoundIfNull(object);
         User author = object.getAuthor();
         if (author == null) {
             if (Security.isConnected()) {
                 author = User.find("byEmail", Security.connected()).first();
             }
         }
         bindAndProcessPhoto(object, true);
         object.setAuthor(author);
         validation.valid(object);
         if (validation.hasErrors()) {
             renderArgs.put("error", Messages.get("crud.hasErrors"));
             try {
                 render(request.controller.replace(".", "/") + "/show.html", type, object);
             } catch (TemplateNotFoundException e) {
                 render("CRUD/show.html", type, object);
             }
         }
         object._save();
         flash.success(Messages.get("crud.saved", type.modelName));
         if (params.get("_save") != null) {
             redirect(request.controller + ".list");
         }
         redirect(request.controller + ".show", object._key());
     }
 
     private static void bindAndProcessPhoto(Product product, boolean remove) throws IOException {
         params.checkAndParse();
         List<Upload> uploads = (List<Upload>) Http.Request.current().args.get("__UPLOADS");
         for (Upload upload : uploads) {
             if (upload.getFieldName().equals("object.photo") && upload.getSize() > 0) {
                 BufferedImage original = ImageIO.read(upload.asStream());
                 int size = Math.min(original.getWidth(), original.getHeight());
                 BufferedImage cropped = original.getSubimage((original.getWidth() - size) / 2,
                         (original.getHeight() - size) / 2,
                         size,
                         size);
                 Image thumbnailImage = cropped.getScaledInstance(THUMBNAIL_SIZE, THUMBNAIL_SIZE, Image.SCALE_SMOOTH);
                 BufferedImage thumbnail = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_RGB);
                 Graphics graphics = thumbnail.createGraphics();
                 graphics.drawImage(thumbnailImage, 0, 0, new Color(0, 0, 0), null);
                 graphics.dispose();
 
                 if (remove && product.hasPhoto() && product.getPhoto().exists()) {
                     product.getPhoto().delete();
                 }
                 if (remove && product.hasThumbnail() && product.getThumbnail().exists()) {
                     product.getThumbnail().delete();
                 }
                 if (product.getPhoto() == null) {
                     product.setPhoto(new S3Blob());
                 }
                 if (product.getThumbnail() == null) {
                     product.setThumbnail(new S3Blob());
                 }
                 final ByteArrayOutputStream originalOutput = getImageAsStream(original);
                 product.getPhoto().set(new ByteArrayInputStream(originalOutput.toByteArray(), 0, originalOutput.size()),
                         "image/png");
                 final ByteArrayOutputStream thumbnailOutput = getImageAsStream(thumbnail);
                 product.getThumbnail().set(new ByteArrayInputStream(thumbnailOutput.toByteArray(), 0, thumbnailOutput.size()),
                         "image/png");
                 params.remove("object.photo");
             }
         }
         Cache.delete("product_photo_type_" + product.id);
         Cache.delete("product_photo_bytes_" + product.id);
         Binder.bind(product, "object", params.all());
     }
 
     public static ByteArrayOutputStream getImageAsStream(BufferedImage cropped) throws IOException {
         final ByteArrayOutputStream output = new ByteArrayOutputStream() {
             @Override
             public synchronized byte[] toByteArray() {
                 return this.buf;
             }
         };
         ImageIO.write(cropped, "png", output);
         return output;
     }
 
     @After(only = {"save"})
     public static void sendNotifications(String id) throws Exception {
         Product product = Product.findById(Long.parseLong(id));
         notFoundIfNull(product);
         Category category = product.getCategory();
         if (category == null && product.getParent() != null)
             category = product.getParent().getCategory();
         if (category != null) {
             List<User> moderators = category.getModerators();
             if (moderators == null && category.getParent() != null)
                 moderators = category.getParent().getModerators();
             if (moderators != null && !moderators.isEmpty()) {
                 for (User user : moderators)
                     EmailQueue.getInstance().add(user, product);
             }
 
         }
     }
 }
