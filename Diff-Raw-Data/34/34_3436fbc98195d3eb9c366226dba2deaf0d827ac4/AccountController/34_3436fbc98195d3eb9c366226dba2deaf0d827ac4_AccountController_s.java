 package org.tsinghua.omedia.controller;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.tsinghua.omedia.model.Account;
 
 @Controller
 public class AccountController extends BaseController {
     private static final Logger logger = Logger.getLogger(AccountController.class);
 
     @Value("${omedia.version}")
     private  String OMEDIA_VERSION;
     
     @RequestMapping(value="/register.do", method=RequestMethod.GET)
     @ResponseBody
     public String register(@RequestParam("username") String username
             ,@RequestParam("password") String password
             ,@RequestParam("email") String email
             ,@RequestParam("omediaVersion") String omediaVersion) {
         logger.debug("register username="+username + ",password="+password + ",email=" + email);
         try {
             if(!omediaVersion.equals(OMEDIA_VERSION)) {
                 return "{\"result\":4}";
             }
             if(accountService.isUsernameExist(username)) {
                 return "{\"result\":2}";
             }
             accountService.createAccount(username, password, email);
             return "{\"result\":1}";
         } catch (IOException e) {
             logger.error("register failed! username="+username+",email="+email+",password="+password, e);
             return "{\"result\":-1}";
         }
     }
 
     @RequestMapping(value="/login.do", method=RequestMethod.GET)
     @ResponseBody
     public String login(@RequestParam("username") String username
             ,@RequestParam("password") String password
             ,@RequestParam("omediaVersion") String omediaVersion) {
        logger.debug("login username="+username + ",password="+password );
         try {
             if(!omediaVersion.equals(OMEDIA_VERSION)) {
                 return "{\"result\":4}";
             }
             Account account = accountService.login(username, password);
             if(account == null) {
                 return "{\"result\":2}";
             }
             //update token
             long token = accountService.updateToken(account.getAccountId());
             account.setToken(token);
             JsonLoginSuccess json = new JsonLoginSuccess(account);
             return objectMapper.writeValueAsString(json);
         } catch (IOException e) {
             logger.error("login failed! username="+username+",password="+password, e);
             return "{\"result\":-1}";
         }
     }
     
     @RequestMapping(value="/setting.do", method=RequestMethod.GET)
     @ResponseBody
     public String setting(@RequestParam("accountId") long accountId,
             @RequestParam("token") long token,
             @RequestParam("oldPassword") String oldPassword,
             @RequestParam("newPassword") String newPassword,
             @RequestParam("email") String email,
             @RequestParam("realName") String realName,
             @RequestParam("phone") String phone,
             @RequestParam("address") String address) {
         logger.info("setting accountId="+accountId+",token="+token
                 +", realName=" + realName);
         try {
             Account dbAccount = accountService.getAccount(accountId);
             if(dbAccount==null || dbAccount.getToken()!=token) {
                 return "{\"result\":3}";
             }
             Account account = new Account();
             account.setAccountId(accountId);
             account.setAddress(address);
             account.setEmail(email);
             account.setPhone(phone);
             account.setRealName(realName);
             account.setToken(token);
             account.setUsername(dbAccount.getUsername());
             account.setVersion(System.currentTimeMillis());
             account.setPassword(dbAccount.getPassword());
             if (!newPassword.equals("")) {
                 account.setPassword(accountUtils.encryptPassword(newPassword));
                 if(!dbAccount.getPassword().equals(accountUtils.encryptPassword(oldPassword))) {
                     return "{\"result\":2}";
                 }
             }
             accountService.updateAccount(account);
             return "{\"result\":1, \"version\":"+account.getVersion()+"}";
         } catch (Exception e) {
             logger.error("setting failed", e);
             return "{\"result\":-1}";
         }
     }
     @RequestMapping(value="/getAccount.do", method=RequestMethod.GET)
     @ResponseBody
     public String getAccount(@RequestParam("accountId") long accountId,
             @RequestParam("token") long token) {
         Account account;
         try {
             account = accountService.getAccount(accountId);
             if(account==null || account.getToken()!=token) {
                 return "{\"result\":3}";
             }
             String r = objectMapper.writeValueAsString(new JsonAccount(account));
             logger.info("getAccount.do response="+r);
             return r;
         } catch (Exception e) {
             logger.error("get account failed", e);
             return "{\"result\":-1}";
         }
     }
 
     private static class JsonLoginSuccess {
         private int result;
         private long accountId;
         private long token;
         
         public JsonLoginSuccess(Account account) {
             setAccountId(account.getAccountId());
             setResult(1);
             setToken(account.getToken());
         }
 
         @SuppressWarnings("unused")
         public int getResult() {
             return result;
         }
         public void setResult(int result) {
             this.result = result;
         }
         @SuppressWarnings("unused")
         public long getAccountId() {
             return accountId;
         }
         public void setAccountId(long accountId) {
             this.accountId = accountId;
         }
         @SuppressWarnings("unused")
         public long getToken() {
             return token;
         }
         public void setToken(long token) {
             this.token = token;
         }
     }
     
     @SuppressWarnings("unused")
     private static class JsonAccount {
         private int result = 1;
         private String email;
         private String realName;
         private String address;
         private String phone;
         private long version;
         
         public JsonAccount(Account account) throws UnsupportedEncodingException {
             this.email = account.getEmail();
             this.realName = new String(account.getRealName().getBytes("utf8"),"ISO8859_1");
             this.address = new String(account.getAddress().getBytes("utf8"),"ISO8859_1");
             this.phone = new String(account.getPhone().getBytes("utf8"),"ISO8859_1");
             this.version = account.getVersion();
         }
 
         public int getResult() {
             return result;
         }
 
         public void setResult(int result) {
             this.result = result;
         }
 
         public String getEmail() {
             return email;
         }
 
         public void setEmail(String email) {
             this.email = email;
         }
 
         public String getRealName() {
             return realName;
         }
 
         public void setRealName(String realName) {
             this.realName = realName;
         }
 
         public String getAddress() {
             return address;
         }
 
         public void setAddress(String address) {
             this.address = address;
         }
 
         public String getPhone() {
             return phone;
         }
 
         public void setPhone(String phone) {
             this.phone = phone;
         }
 
         public long getVersion() {
             return version;
         }
 
         public void setVersion(long version) {
             this.version = version;
         }
         
     }
   
 }
