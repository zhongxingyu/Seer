 /*
  * Copyright 2010-2013, CloudBees Inc.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package com.cloudbees.sdk.commands.bg;
 
 import com.cloudbees.api.ApplicationInfo;
 import com.cloudbees.api.BeesClient;
 import com.cloudbees.sdk.cli.BeesCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.commands.app.ApplicationDeploy;
 import com.cloudbees.sdk.utils.Helper;
 import com.staxnet.appserver.config.AppConfig;
 
 import java.io.IOException;
 
 @BeesCommand(group="Application", description = "Deploy an application using the Blue-Green process")
 @CLICommand("app:bg:deploy")
 public class ApplicationBlueGreenDeploy extends ApplicationDeploy {
 
     private String name;
 
     @Override
     protected boolean preParseCommandLine() {
         if (super.preParseCommandLine()) {
             addOption( "n", "name", true, "Blue-Green application name" );
             return true;
         }
 
         return false;
     }
 
     public String getName() throws IOException {
         if (name == null) name = Helper.promptFor("Blue-Green application name: ", true);
         return name;
     }
 
     public void setName(String name) {
        String[] parts = name.split("/");
        if (parts.length == 2) {
            this.name = parts[1];
            setAccount(parts[0]);
        } else {
            this.name = name;
        }
     }
 
     protected String initAppId(String appid, AppConfig appConfig) throws IOException {
         try {
             String appId = null;
             BeesClient client = getBeesClient(BeesClient.class);
             BlueGreenSettings blueGreenSettings = BlueGreenSettings.getInstance(client, getAccount(), getName());
 
             // Get the app1 alias
             ApplicationInfo applicationInfo = client.applicationInfo(blueGreenSettings.getApplication1());
             String aliases = applicationInfo.getSettings().get("aliases");
             ApplicationAlias application1 = new ApplicationAlias(getAccount(), blueGreenSettings.getApplication1(), aliases);
 
             // If the app1 has the alias already, deploy to the other app
             if (application1.containAliases(blueGreenSettings.getActiveAliases())) {
                 appId = blueGreenSettings.getApplication2();
             // If the app1 does not have the alias, deploy to it
             } else {
                 appId = blueGreenSettings.getApplication1();
             }
             System.out.println("Blue-Green deployment enabled, using secondary application: " + appId);
 
             return appId;
         } catch (IllegalArgumentException e) {
             throw e;
         } catch (Exception e) {
             throw new IllegalArgumentException("Application not configured for Blue-Green deployment.", e);
         }
     }
 
 }
