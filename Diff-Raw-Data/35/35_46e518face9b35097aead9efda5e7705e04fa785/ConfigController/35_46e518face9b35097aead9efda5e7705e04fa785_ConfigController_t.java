 package cn.mobiledaily.web.controller;
 
 import cn.mobiledaily.common.validation.NotBlank;
 import cn.mobiledaily.module.logger.domain.ClientLog;
 import cn.mobiledaily.module.logger.domain.ClientType;
 import cn.mobiledaily.common.exception.InternalServerError;
 import cn.mobiledaily.common.exception.InvalidValueException;
 import cn.mobiledaily.common.exception.LogException;
 import cn.mobiledaily.common.exception.ValidationException;
 import cn.mobiledaily.common.service.ApplicationService;
 import cn.mobiledaily.module.logger.service.ClientLogService;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("configurations")
@Validated
 public class ConfigController {
     static Logger logger = LoggerFactory.getLogger(ConfigController.class);
     @Autowired
     ClientLogService clientLogService;
     @Autowired
     ApplicationService applicationService;
 
     @RequestMapping(value = "get", method = RequestMethod.GET)
     @ResponseBody
     public ConfigWrapper getConfig(
             @NotBlank String type,
             String osVer,
             String ver,
             String token
     ) {
         token = StringUtils.remove(token, ':');
         ConfigWrapper wrapper = new ConfigWrapper();
         try {
             ClientType clientType = ClientType.parseCode(type);
             if (clientType == ClientType.UNKNOWN) {
                 throw new InvalidValueException("type", "client platform", type);
             }
             wrapper.setAssetServer(applicationService.assetServer());
             wrapper.setTel(applicationService.applyPhone());
             wrapper.setToken(token);
             wrapper.setUpgrade("");
             wrapper.setUpgradeNote("");
             try {
                 ClientLog clientLog = new ClientLog();
                 clientLog.setClientType(clientType);
                 clientLog.setOsVer(osVer);
                 clientLog.setVer(ver);
                 clientLog.setToken(token);
                 clientLogService.save(clientLog);
             } catch (Exception e) {
                 throw new LogException("/configurations/get", e);
             }
         } catch (LogException e) {
             logger.error("/configurations/get", e);
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/configurations/get", e);
             throw new InternalServerError("/configurations/get", e);
         }
         return wrapper;
     }
 
     public static class ConfigWrapper {
         String assetServer;
         String tel;
         String token;
         String upgrade;
         String upgradeNote;
 
         public String getAssetServer() {
             return assetServer;
         }
 
         public void setAssetServer(String assetServer) {
             this.assetServer = assetServer;
         }
 
         public String getTel() {
             return tel;
         }
 
         public void setTel(String tel) {
             this.tel = tel;
         }
 
         public String getToken() {
             return token;
         }
 
         public void setToken(String token) {
             this.token = token;
         }
 
         public String getUpgrade() {
             return upgrade;
         }
 
         public void setUpgrade(String upgrade) {
             this.upgrade = upgrade;
         }
 
         public String getUpgradeNote() {
             return upgradeNote;
         }
 
         public void setUpgradeNote(String upgradeNote) {
             this.upgradeNote = upgradeNote;
         }
     }
 }
