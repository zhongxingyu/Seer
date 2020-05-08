 package kwitches.service;
 
 import java.util.Map;
 
 import org.slim3.controller.upload.FileItem;
 import org.slim3.util.BeanUtil;
 
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.images.Image;
 import com.google.appengine.api.images.ImagesService;
 import com.google.appengine.api.images.ImagesServiceFactory;
 import com.google.appengine.api.images.Transform;
 
 import kwitches.model.ImageModel;
 import kwitches.model.UserModel;
 import kwitches.service.dao.ImageModelDao;
 import kwitches.service.dao.UserModelDao;
 
 public class UploadImageService {
 
     private static ImagesService imagesService = ImagesServiceFactory.getImagesService();
 
     public static final int THUMBNAIL_WIDTH = 96;
 
     public ImageModel create(Map<String, Object> input, UserModel userModel) {
         BeanUtil.copy(input, userModel);
         ImageModel newData = new ImageModel();
         FileItem fileImage = (FileItem)input.get("fileImage");
         String isIconReset = (String) input.get("icon_reset");
 
         if (isIconReset != null && isIconReset.equals("yes")) {
             userModel.getIconRef().setModel(null);
         } else if (fileImage != null) {
             newData.setFilename((String) fileImage.getShortFileName());
             newData.setFileContentType((String) fileImage.getContentType());
 
             Image oldImage =
                 ImagesServiceFactory.makeImage(fileImage.getData());
            Image resizedImage =
                oldImage.getHeight() <= THUMBNAIL_WIDTH
                    && oldImage.getWidth() <= THUMBNAIL_WIDTH
                    ? oldImage
                    : resizeToThumbnail(oldImage);
 
             newData.setFileImage(new Blob(resizedImage.getImageData()));
             ImageModelDao.GetInstance().putImage(newData);
 
             userModel.getIconRef().setModel(newData);
         }
 
         UserModelDao.GetInstance().appendUser(userModel);
         return newData;
     }
 
     private Image resizeToThumbnail(Image oldImage) {
         double width = oldImage.getWidth();
         double height = oldImage.getHeight();
         double rate = height / width;
         Transform resize = ImagesServiceFactory.makeResize(THUMBNAIL_WIDTH, (int)(THUMBNAIL_WIDTH * rate));
         return imagesService.applyTransform(resize, oldImage);
     }
 
 }
