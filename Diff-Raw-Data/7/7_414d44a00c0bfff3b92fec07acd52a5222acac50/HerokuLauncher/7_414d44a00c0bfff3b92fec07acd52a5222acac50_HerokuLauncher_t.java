 /*
  *
  *  Copyright (c) 2012 the original author or authors.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 package eu.hurion.hello.vaadin.shiro.server;
 
 import eu.hurion.hello.vaadin.shiro.application.HerokuShiroApplication;
 
 /**
  * Command line entry point for the application.
  */
 public class HerokuLauncher {
 
     public static final String PORT = "PORT";
     public static final int DEFAULT_PORT = 8080;
 
 
    public static void main(final String[] args) {
         ShiroEmbedVaadin.forApplication(HerokuShiroApplication.class)
                 .withHttpPort(getPort())
                 .withProductionMode(true)
                 .start();
     }
 
     private static int getPort() {
         final String envPort = System.getenv(PORT);
        if (null == envPort) {
             return DEFAULT_PORT;
         }
         return Integer.parseInt(envPort);
     }
 }
