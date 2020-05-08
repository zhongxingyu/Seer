 package com.xhills.golf_party.controller.api.v1.me;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
 import com.google.appengine.repackaged.org.json.JSONObject;
 import com.xhills.golf_party.common.Const;
 import com.xhills.golf_party.common.Me;
 import com.xhills.golf_party.common.utils.JSONUtil;
 import com.xhills.golf_party.model.me.Group;
 import com.xhills.golf_party.service.me.GroupService;
 
 public class UpdateController extends Controller {
 
     private Logger log = Logger.getLogger(getClass().getName());
 
     
     @Override
     public Navigation run() throws Exception {
         
         response.setContentType(Const.charEncoding);
         
         if (!isGet()) {
             
             Me me = (Me) sessionScope("me");
             if (me != null) {
                 if (isPost()) {
                     // create
                     JSONObject obj = 
                             JSONUtil.inputStreamToJSONObject(request.getInputStream());
                     log.info("Create new group. " + obj.getString("name"));
 
                     Group group = new Group();
                     GroupService gs = new GroupService();
                     group.setOwnerid(me.getUser().getUserid());
                     group.setName(obj.getString("name"));
                     group.setUserids(new HashSet<String>());
                     for (int i = 0; i < obj.getJSONArray("userids").length(); i++) {
                         String userid = obj.getJSONArray("userids").getString(i);
                         group.getUserids().add(userid);
                     }
                     group = gs.createGroup(group);
                     group.toJSONObject().write(response.getWriter());
                    
                     return null;
                     
                 } else if (isPut()) {
                     // update
                     JSONObject obj = 
                             JSONUtil.inputStreamToJSONObject(request.getInputStream());
                     log.info("Update group. " + obj.getString("name"));
 
                     GroupService gs = new GroupService();
                     Set<String> userids = new HashSet<String>();
                     for (int i = 0; i < obj.getJSONArray("userids").length(); i++) {
                         String userid = obj.getJSONArray("userids").getString(i);
                         userids.add(userid);
                     }
                     Group group =
                             gs.updateGroup(
                                 obj.getLong("id"), 
                                me.getUser().getUserid(),
                                 obj.getString("name"),
                                 userids);
                     group.toJSONObject().write(response.getWriter());
                     
                     return null;
                     
                 } else if (isDelete()) {
                     // delete
                     JSONObject obj = 
                             JSONUtil.inputStreamToJSONObject(request.getInputStream());
                     long id = obj.getLong("id");
                     log.info("Delete group. " + id);
                     GroupService gs = new GroupService();
                     gs.deleteGroup(id);
                     
                     response.getWriter().write("null");
                     return null;
                 }
             }
         }
         return null;
 
     }
 }
