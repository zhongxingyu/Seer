 package com.ilopezluna.pages.video;
 
 import com.ilopezluna.entities.Video;
 import com.ilopezluna.services.VideoDAO;
 import com.ilopezluna.services.VideoService;
 import com.ilopezluna.utils.MailUtil;
 import org.apache.commons.lang.StringUtils;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.DateField;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.corelib.components.TextField;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import javax.mail.MessagingException;
 
 public class CreateVideo {
 
     @Inject
     private VideoService videoService;
 
     @InjectPage
     private CreateVideoSuccess createVideoSuccess;
 
     @InjectPage
     private CreateVideoError createVideoError;
 
     @Component(id = "createVideo")
     private Form form;
 
     @Component(id = "url")
     private TextField urlField;
 
     @Component(id = "date")
     private DateField dateField;
 
     @Property
     private Video video;
 
     void onPrepare() throws Exception {
         video = new Video();
     }
 
     void onValidateFromCreateVideo() {
         if (StringUtils.isEmpty(video.getUrl())) {
             form.recordError(urlField, "URL is required.");
         }
     }
 
     Object onSuccess() {
 
         videoService.create(video);
 
         try {
             MailUtil.sendValidationVideoMail(video.toString());
         } catch (MessagingException e) {
             return createVideoError;
         }
 
         return createVideoSuccess;
     }
 }
