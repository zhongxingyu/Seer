 /**
  * Copyright (C) 2010 ZeroTurnaround OU
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License v2 as published by
  * the Free Software Foundation, with the additional requirement that
  * ZeroTurnaround OU must be prominently attributed in the program.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You can find a copy of GNU General Public License v2 from
  *   http://www.gnu.org/licenses/gpl-2.0.txt
  */
 
 package com.polopoly.javarebel;
 
 import org.zeroturnaround.javarebel.ClassResourceSource;
 import org.zeroturnaround.javarebel.Integration;
 import org.zeroturnaround.javarebel.IntegrationFactory;
 import org.zeroturnaround.javarebel.LoggerFactory;
 import org.zeroturnaround.javarebel.Plugin;
 
 import com.polopoly.javarebel.cfg.ConfigurationProvider;
 import com.polopoly.javarebel.cfg.ConfigurationProvider.Cfg;
 import com.polopoly.javarebel.contentfiles.ContentBaseProcessor;
 import com.polopoly.javarebel.staticfiles.StaticFileFilterProcessor;
 
 public class PolopolyJRebelPlugin implements Plugin {
 
   public void preinit() {
     // Register the CBP
     Integration i = IntegrationFactory.getInstance();
     ClassLoader cl = PolopolyJRebelPlugin.class.getClassLoader();
     i.addIntegrationProcessor(cl, "com.polopoly.cm.client.impl.service2client.ContentBase", new ContentBaseProcessor());
     Cfg cfg = ConfigurationProvider.instance().getConfiguration();
     if (cfg == null) {
         throw new RuntimeException("pp-rebel could not find pp-rebel.xml, please specify a valid PP_HOME property");
     } else if (cfg.configuration == null) {
         LoggerFactory.getInstance().echo("pp-rebel.INFO: Not patching servlet filters," +
                                          " static file processing will be disabled until restart" +
                                         " (no configuration)");
     } else if (cfg.configuration.enableFilterProcessing() || cfg.configuration.hasFilterFiles()) {
         i.addIntegrationProcessor(cl, new StaticFileFilterProcessor());
     } else {
         LoggerFactory.getInstance().echo("pp-rebel.INFO: Not patching servlet filters," +
                                          " static file processing will be disabled until restart");
     }
 //    
 //    // Set up the reload listener
 //    ReloaderFactory.getInstance().addClassReloadListener(
 //      new ClassEventListener() {
 //        public void onClassEvent(int eventType, Class klass) {
 //
 //          try {
 //            Class abstractCanvasClass = Class.forName("org.zeroturnaround.javarebel.sdkDemo.AbstractCanvas");
 //          
 //            // Check if it is child of AbstractCanvas
 //            if (abstractCanvasClass.isAssignableFrom(klass)) {
 //              System.out.println("An AbstractCanvas implementation class was reloaded .. re-painting the canvas");
 //              DemoAppConfigReloader.repaint();
 //              LoggerFactory.getInstance().echo("Repainted the canvas");
 //            }
 //            
 //          } catch (Exception e) {
 //            LoggerFactory.getInstance().error(e);
 //            System.out.println(e);
 //          }
 //        }
 //
 //        public int priority() {
 //          return 0;
 //        }
 //      }
 //    );
 
   }
 
   public String getId() {
     return "pp-rebel";
   }
 
   public String getName() {
     return "Polopoly JRebel Plugin";
   }
 
   public String getDescription() {
     return "Loads Polopoly content files directly from the file system";
   }
 
   public String getAuthor() {
     return null;
   }
 
   public String getWebsite() {
     return null;
   }
 
   public boolean checkDependencies(ClassLoader classLoader, ClassResourceSource classResourceSource) {
     return classResourceSource.getClassResource("com.polopoly.cm.client.impl.service2client.ContentBase") != null;
   }
 
   public String getSupportedVersions()
   {
     return null;
   }
 
   public String getTestedVersions()
   {
     return null;
   }
 }
