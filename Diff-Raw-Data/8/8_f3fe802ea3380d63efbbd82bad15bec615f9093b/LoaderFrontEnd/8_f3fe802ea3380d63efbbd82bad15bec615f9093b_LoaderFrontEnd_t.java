 /*
 Copyright 2012 Portland Transport
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package org.transitappliance.loader;
 
 import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.core.io.FileSystemResource;
 
 /**
  * Load the GTFS specified to the database
  */
 public class LoaderFrontEnd
 {
     /**
      * The main CLI of the program. Just loads the config file and spins up Spring.
      * @param args The command line arguments. Uses varargs so this can be called from a script
      */
     public static void main( String... args )
     {
         // benchmarking
         long startTime = System.currentTimeMillis();
         long totalTime;
 
         // Modeled after the main method in OTP Graph Builder
         
         // arg checking
        if (args.length == 0) {
             System.out.println("usage: loader config.xml");
             System.exit(1);
         }
 
         System.out.println( "Transit Appliance Stop Loader" );
 
         // Load Spring
         GenericApplicationContext ctx = new GenericApplicationContext();
         XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ctx);
 
        // Load config file for this agency and database adapter
        for (String file : args)
           reader.loadBeanDefinitions(new FileSystemResource(file));
 
         // get the loader, config'd for this agency
         TransitStopLoader loader = (TransitStopLoader) ctx.getBean("transitStopLoader");
         
         loader.loadStops();
     }
 }
