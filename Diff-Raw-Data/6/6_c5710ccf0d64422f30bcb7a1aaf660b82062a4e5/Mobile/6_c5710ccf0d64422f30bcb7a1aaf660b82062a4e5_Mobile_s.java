 package org.motechproject.ghana.national.functional.framework;
 
 import org.motechproject.MotechException;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.springframework.stereotype.Component;
 
 import java.util.Map;
 
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

 @Component
 public class Mobile {
     public XformHttpClient.XformResponse upload(MobileForm mobileForm, Map<String, String> templateData) {
         try {
             final XformHttpClient.XformResponse response = XformHttpClient.execute("http://localhost:8080/ghana-national-web/formupload",
                     mobileForm.getStudyName(), XformHttpClient.XFormParser.parse(mobileForm.getXmlTemplateName(), templateData));
            assertThat(response.getErrors().size(), is(equalTo(0)));
             return response;
 
         } catch (Exception e) {
             throw new MotechException("Encountered error while uploading mobile form", e);
         }
 
     }
 }
