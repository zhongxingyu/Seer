 package com.xhills.golf_party.controller.api.v1.me;
 
 import java.util.List;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
 import com.google.appengine.repackaged.org.json.JSONArray;
 import com.xhills.golf_party.common.Const;
 import com.xhills.golf_party.common.Me;
 import com.xhills.golf_party.model.common.User;
 import com.xhills.golf_party.model.me.Group;
 import com.xhills.golf_party.service.me.GroupService;
 
 public class GetController extends Controller {
     
     @Override
     public Navigation run() throws Exception {
 
         response.setContentType(Const.charEncoding);
 
         String service = asString("service");
         if (service != null) {
 
             Me me = (Me) sessionScope("me");
             if (me != null) {
 
                 if (service.equals("me")) {
 
                     User user = me.getUser();
                     user.toJSONObject().write(response.getWriter());
 
                     return null;
 
                 } else if (service.equals("my_friends")) {
 
                     JSONArray friends = new JSONArray();
                     for (User friend : me.getFriends()) {
                         friends.put(friend.toJSONObject());
                     }
                     friends.write(response.getWriter());
                     
                     return null;
                 } else if (service.equals("my_groups")) {
                     
                    // groups
                     GroupService gs = new GroupService();
                     List<Group> groupList = 
                             gs.getMyGroups(me.getUser().getUserid());
                     JSONArray groups = new JSONArray();
                     for (Group group : groupList) {
                         groups.put(group.toJSONObject());
                     }
                     groups.write(response.getWriter());
                     
                 }
             }
         }        
         return null;
     }
 
 }
