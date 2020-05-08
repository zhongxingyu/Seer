 package org.tsinghua.omedia.controller;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 import org.tsinghua.omedia.model.Account;
 import org.tsinghua.omedia.model.Config;
 import org.tsinghua.omedia.utils.ConfigManager;
 
 @Controller
 public class CommonsController extends BaseController {
     private static final Logger logger = Logger.getLogger(CommonsController.class);
 
     @Value("${apk.path}")
     private String APK_PATH;
 
     @Value("${omedia.version}")
     private  String OMEDIA_VERSION;
     
     @RequestMapping(value="/checkDataVersion.do", method=RequestMethod.GET)
     @ResponseBody
     public String checkDataVersion(@RequestParam("accountId") long accountId,
             @RequestParam("token") long token,
             @RequestParam("accountVersion") long accountVersion,
             @RequestParam("friendRequestVersion") long friendRequestVersion,
             @RequestParam("friendsVersion") long friendsVersion,
             @RequestParam("ccnFileVersion") long ccnFileVersion,
             @RequestParam("configVersion") long configVersion,
             @RequestParam("groupVersion") long groupVersion) {
         Account account;
         try {
             account = accountService.getAccount(accountId);
             if(account==null || account.getToken()!=token) {
                 return "{\"result\":3}";
             }
             JsonCheckDataVersion json = new JsonCheckDataVersion();
             json.setResult(1);
             if(account.getVersion() == accountVersion) {
                 json.setAccount(0);
             } else {
                 json.setAccount(1);
             }
             if(account.getFriendRequestVersion() == friendRequestVersion) {
                 json.setFriendRequest(0);
             } else {
                 json.setFriendRequest(1);
             }
             if(account.getFriendsVersion() == friendsVersion) {
                 json.setFriends(0);
             } else {
                 json.setFriends(1);
             }
             if(CcnController.ccnFileVersion == ccnFileVersion) {
                 json.setCcnFile(0);
             } else {
                 json.setCcnFile(1);
             }
             if(ConfigManager.getConfigVersion() == configVersion) {
                 json.setConfig(0);
             } else {
                 json.setConfig(1);
             }
             if(account.getGroupVersion()==groupVersion) {
                 json.group=0;
             } else {
                 json.group=1;
             }
            return objectMapper.writeValueAsString(json);
         } catch (Exception e) {
             logger.error("check data version failed", e);
             return "{\"result\":-1}";
         }
     }
 
     @RequestMapping(value="/getConfig.do", method=RequestMethod.GET)
     @ResponseBody
     public String getConfig() {
         long version = ConfigManager.getConfigVersion();
         Config config = ConfigManager.getConfig();
         JsonConfig json = new JsonConfig();
         json.setConfig(config);
         json.setVersion(version);
         try {
             return objectMapper.writeValueAsString(json);
         } catch (Exception e) {
             logger.error("", e);
             return "{\"result\":-1}";
         }
     }
     
     @RequestMapping(value="/download-omedia.do",method=RequestMethod.GET)
     public void downloadOmedia(HttpServletResponse response) {
         try {
             File file = new File(APK_PATH);
             response.setContentType("application/octet-stream");
             response.setHeader("Content-Disposition",
                     "attachment;filename="
                             + new String(file.getName().getBytes("gb2312"),
                                     "ISO8859-1"));
             FileInputStream fis = new FileInputStream(file);
             IOUtils.copy(fis, response.getOutputStream());
             response.flushBuffer();
         } catch (Exception e) {
             logger.error("", e);
         }
     }
 
     @RequestMapping(value = "/download-file.do", method = RequestMethod.GET)
     public void downloadOmedia(@RequestParam("path") String path,
             HttpServletResponse response) {
         try {
             File file = new File(path);
             response.setContentType("application/octet-stream");
             response.setHeader("Content-Disposition",
                     "attachment;filename="
                             + new String(file.getName().getBytes("gb2312"),
                                     "ISO8859-1"));
             FileInputStream fis = new FileInputStream(file);
             IOUtils.copy(fis, response.getOutputStream());
             response.flushBuffer();
         } catch (Exception e) {
             logger.error("", e);
         }
     }
 
     @RequestMapping(value = "/login-vm.do", method = RequestMethod.GET)
     public ModelAndView login_vm(ModelAndView mav, HttpServletResponse response) {
         mav.setViewName("login");
         mav.addObject("omediaVersion", OMEDIA_VERSION);
         response.setContentType("text/html;charset=UTF-8");
         return mav;
     }
 
     @RequestMapping(value = "/register-vm.do", method = RequestMethod.GET)
     public ModelAndView register_vm(ModelAndView mav, HttpServletResponse response) {
         mav.setViewName("register");
         mav.addObject("omediaVersion", OMEDIA_VERSION);
         response.setContentType("text/html;charset=UTF-8");
         return mav;
     }
 
     @SuppressWarnings("unused")
     private static class JsonCheckDataVersion {
         private int account;//==1 means accountData need to update
         private int friendRequest;
         private int friends;
         private int config;
         private int ccnFile;
         private int result;
         private int group;
 
         public int getFriends() {
             return friends;
         }
 
 
         public void setFriends(int friends) {
             this.friends = friends;
         }
 
         public int getCcnFile() {
             return ccnFile;
         }
 
 
         public void setCcnFile(int ccnFile) {
             this.ccnFile = ccnFile;
         }
 
 
         public int getResult() {
             return result;
         }
         
 
         public int getFriendRequest() {
             return friendRequest;
         }
 
 
         public void setFriendRequest(int friendRequest) {
             this.friendRequest = friendRequest;
         }
 
         public int getConfig() {
             return config;
         }
 
 
         public void setConfig(int config) {
             this.config = config;
         }
 
 
         public void setResult(int result) {
             this.result = result;
         }
 
         public int getAccount() {
             return account;
         }
 
         public void setAccount(int account) {
             this.account = account;
         }
     }
 
     @SuppressWarnings("unused")
     private static class JsonConfig {
         private Config config;
         private long version;
         private int result = 1;
         
         public Config getConfig() {
             return config;
         }
         public void setConfig(Config config) {
             this.config = config;
         }
         public long getVersion() {
             return version;
         }
         public void setVersion(long version) {
             this.version = version;
         }
         public int getResult() {
             return result;
         }
         public void setResult(int result) {
             this.result = result;
         }
         
     }
 }
