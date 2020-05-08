 package com.github.mytask.web.controller.photo;
 
 import com.github.mytask.domain.Photo;
 import com.github.mytask.domain.PhotoManager;
 import com.github.mytask.domain.User;
 import com.github.mytask.web.controller.BaseActionSupport;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.apache.struts2.interceptor.SessionAware;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Map;
 
 /**
  * photo upload controller
  */
 public class PhotoUploadController extends BaseActionSupport implements SessionAware {
     /**
      * photo file
      */
     private File photo;
     /**
      * content type
      */
    private String contentType;
     /**
      * file name
      */
    private String filename;
     /**
      * session
      */
     private Map<String, Object> session;
     /**
      * photo manager
      */
     private PhotoManager photoManager;
 
     /**
      * inject photo manager
      *
      * @param photoManager photo manager
      */
     public void setPhotoManager(PhotoManager photoManager) {
         this.photoManager = photoManager;
     }
 
     public void setPhoto(File photo) {
         this.photo = photo;
     }
 
    public void setContentType(String contentType) {
        this.contentType = contentType;
     }
 
    public void setFilename(String filename) {
        this.filename = filename;
     }
 
     @Override
     public void setSession(Map<String, Object> session) {
         this.session = session;
     }
 
     /**
      * upload photo
      *
      * @return post upload
      */
     public String doUpload() {
         if (photo != null) {
             Photo temp = new Photo();
             temp.setOwner((User) session.get("login_user"));
             temp.setCreatedAt(new Date());
            temp.setUrl(save(photo, filename));
             photoManager.save(temp);
         }
         return "post_upload";
     }
 
     /**
      * save photo
      *
      * @param photo    photo
      * @param filename file
      * @return photo relative path
      */
     public String save(File photo, String filename) {
         File localStore = new File(SystemUtils.getUserHome(), "photos");
         if (!localStore.exists()) {
             localStore.mkdir();
         }
         String extName = FilenameUtils.getExtension(filename).toLowerCase();
         File localFile = new File(localStore, RandomStringUtils.randomAlphabetic(8) + "." + extName);
         try {
             FileUtils.copyFile(photo, localFile);
         } catch (Exception e) {
 
         }
         return "photos/" + localFile.getName();
     }
 }
