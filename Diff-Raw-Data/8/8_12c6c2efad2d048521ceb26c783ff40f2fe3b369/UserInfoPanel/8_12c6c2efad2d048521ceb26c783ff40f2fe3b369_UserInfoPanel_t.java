 package ua.dp.primat.portlet.userinform.app;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Group;
 import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Team;
 import com.liferay.portal.model.User;
 import com.liferay.portal.model.UserGroup;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.panel.Panel;
 
 /**
  *
  * @author fdevelop
  */
 public class UserInfoPanel extends Panel {
 
     public UserInfoPanel(String id, User user) {
         super(id);
         add(new Label("fullname", user.getFullName()));
         add(new Label("email", user.getDisplayEmailAddress()));
         add(new Label("screenname", user.getScreenName()));
         add(new Label("birthday", getUserBirthday(user)));
         add(new Label("group", getUserGroups(user)));
 
         Image ava = new Image("avatar");
         ava.add(new SimpleAttributeModifier("src", getAvatarPath(user)));
         add(ava);
     }
 
     private String getAvatarPath(User user) {
         return String.format("/image/user_%s_portrait?img_id=%d", "male",
                 user.getPortraitId());
     }
 
     private String getUserGroups(User user) {
         try {
             StringBuilder groups = new StringBuilder();
            for (Group t : user.getGroups()) {
                groups.append(t.getName());
                 groups.append(" | ");
             }
             for (Organization o : user.getOrganizations()) {
                 groups.append(o.getName());
                 groups.append(" | ");
             }
             return groups.toString();
         } catch (SystemException se) {
             return "-";
         } catch (PortalException se) {
             return "-";
         }
     }
 
     private String getUserBirthday(User user) {
         try {
             return DateFormat.getDateInstance(DateFormat.MEDIUM).format(user.getBirthday());
         } catch (PortalException pe) {
             return "-";
         } catch (SystemException se) {
             return "-";
         }
     }
 
     private static final long serialVersionUID = 1L;
 
 }
