 package com.cloudbees.sdk.commands.app;
 
 import com.cloudbees.sdk.commands.Command;
 import com.cloudbees.sdk.utils.Helper;
 import java.io.IOException;
 
 /**
  * @author Fabian Donze
  */
 public abstract class  ApplicationBase extends Command {
     /**
      * The id of the application.
      */
     private String appid;
 
     private String account;
 
 
     public void setAppid(String appid) {
         this.appid = appid;
     }
 
     public void setAccount(String account) {
         this.account = account;
     }
 
     @Override
     protected String getUsageMessage() {
         return "APPLICATION_ID";
     }
 
     @Override
     protected boolean preParseCommandLine() {
         // add the Options
         addOption( "a", "appid", true, "CloudBees application ID", true );
 
         return true;
     }
 
     @Override
     protected boolean postParseCommandLine() {
         if (!super.postParseCommandLine()) return false;
 
         if (getParameters().size() > 0 && appid == null) {
             String str = getParameters().get(0);
             if (isParameter(str) < 0)
                 setAppid(str);
         }
 
         return true;
     }
 
     protected String getAccount() throws IOException {
         if (account == null) account = getConfigProperties().getProperty("bees.project.app.domain");
         if (account == null) account = Helper.promptFor("Account name: ", true);
         return account;
     }
 
     protected String getAppId() throws IOException
     {
         if (appid == null || appid.equals("")) {
             appid = AppHelper.getArchiveApplicationId();
         }
 
         if (appid == null || appid.equals(""))
             appid = Helper.promptForAppId();
 
         if (appid == null || appid.equals(""))
             throw new IllegalArgumentException("No application id specified");
 
        String defaultAppDomain = getAccount();
         String[] appIdParts = appid.split("/");
         if (appIdParts.length < 2) {
             if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                 appid = defaultAppDomain + "/" + appid;
             } else {
                 throw new RuntimeException("default app account could not be determined, appid needs to be fully-qualified ");
             }
         }
         return appid;
     }
 
 }
