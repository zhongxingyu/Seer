 package de.flower.rmt.ui.app;
 
import de.flower.common.ui.util.SessionUtils;
 import de.flower.common.util.Collections;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.link.AbstractLink;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.protocol.http.WebApplication;
 
 import javax.mail.internet.InternetAddress;
 import java.net.URLEncoder;
 import java.util.List;
 
 /**
  * @author flowerrrr
  */
 public class Links {
 
     /**
      * @return /das-tool
      */
     public static AbstractLink contextRoot(final String id) {
         return new ExternalLink(id, WebApplication.get().getServletContext().getContextPath());
     }
 
     public static ExternalLink mailLink(String id, String emailAddress, boolean label) {
         ExternalLink link = mailLink(id, emailAddress, label ? emailAddress : null);
         return link;
     }
 
     public static ExternalLink mailLink(final String id, String emailAddress, final String label) {
         if (label == null) {
             return new ExternalLink(id, "mailto:" + emailAddress);
         } else {
             return new ExternalLink(id, "mailto:" + emailAddress, label);
         }
     }
 
     public static ExternalLink mailLink(final String id, final IModel<List<InternetAddress>> listModel) {
         IModel<String> hrefModel = new AbstractReadOnlyModel<String>() {
             @Override
             public String getObject() {
                 List<InternetAddress> emailAddresses = listModel.getObject();
                 List<String> stringList = Collections.convert(emailAddresses,
                         new Collections.IElementConverter<InternetAddress, String>() {
                             @Override
                             public String convert(final InternetAddress ia) {
                                 // fix for RMT-614 (umlaute pose problems in mailto-links)
                                 return ia.getAddress();
                                 // return ia.toString();
                             }
                         });
                String href = StringUtils.join(stringList, getAddressDelimiter());
                 return "mailTo:" + URLEncoder.encode(href);
             }
        };
         return new ExternalLink(id, hrefModel);
     }
 
    private static String getAddressDelimiter() {
        // outlook likes ';', iphone mail client prefers ','. but according to most sources ';' is correct when used in mailto.
        // could try to detect user agent
        String userAgent = SessionUtils.getUserAgent();
        String DEFAULT = ";";
        String MAC = ",";
        if (userAgent.toLowerCase().contains("windows")) {
            return DEFAULT;
        } else if (userAgent.toLowerCase().contains("mac")) {
            return MAC;
        } else {
            return DEFAULT;
        }
    }

     public static Component logoutLink(final String id) {
         return new ExternalLink(id, "/j_spring_security_logout").setContextRelative(true);
     }
 }
