 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.launch;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import com.quartercode.disconnected.Main;
 
 /**
  * This class launches another main method inside the same jar using a second vm.
  * This allows to set vm arguments internally, e.g. for a natives path.
  */
 public class Launcher {
 
     private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());
 
     /**
      * The main method which creates and calls a new launcher.
      * This is not part of the utility.
      * 
      * @param args The command line arguments.
      */
     public static void main(String[] args) {
 
         String mainClass = Main.class.getName();
         List<String> vmArguments = Arrays.asList("-Djava.library.path=lib/natives");
 
         Launcher launcher = new Launcher(mainClass, vmArguments);
         launcher.launch();
     }
 
     private String       mainClass;
     private List<String> vmArguments;
 
     /**
      * Creates a new empty launcher.
      */
     public Launcher() {
 
     }
 
     /**
      * Creates a new launcher and initalizes the main class.
      * 
      * @param mainClass The main class which get called on launch.
      */
     public Launcher(String mainClass) {
 
         this.mainClass = mainClass;
     }
 
     /**
      * Creates a new launcher and initalizes the main class and vm arguments.
      * 
      * @param mainClass The main class which get called on launch.
      * @param vmArguments The vm arguments which gets set on launch.
      */
     public Launcher(String mainClass, List<String> vmArguments) {
 
         this.mainClass = mainClass;
         this.vmArguments = vmArguments;
     }
 
     /**
      * Returns the main class which get called on launch.
      * 
      * @return The main class which get called on launch.
      */
     public String getMainClass() {
 
         return mainClass;
     }
 
     /**
      * Sets the main class which get called on launch.
      * 
      * @param mainClass The main class which get called on launch.
      */
     public void setMainClass(String mainClass) {
 
         this.mainClass = mainClass;
     }
 
     /**
      * Returns the vm arguments which gets set on launch.
      * 
      * @return The vm arguments which gets set on launch.
      */
     public List<String> getVmArguments() {
 
        return Collections.unmodifiableList(vmArguments);
     }
 
     /**
      * Sets the vm arguments which gets set on launch.
      * 
      * @param vmArguments The vm arguments which gets set on launch.
      */
     public void setVmArguments(List<String> vmArguments) {
 
         this.vmArguments = vmArguments;
     }
 
     /**
      * Launches the given main class using a new vm with the given vm arguments.
      */
     public void launch() {
 
         LOGGER.info("Launching main class " + mainClass + " ...");
 
         try {
             File file = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
 
             List<String> arguments = new ArrayList<String>();
             arguments.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
 
             if (vmArguments != null) {
                 LOGGER.info("VM Arguments: " + vmArguments);
                 for (String vmArgument : vmArguments) {
                     arguments.add(vmArgument);
                 }
             }
 
             arguments.add("-cp");
             arguments.add(file.getAbsolutePath());
             arguments.add(mainClass);
 
             ProcessBuilder processBuilder = new ProcessBuilder(arguments);
             processBuilder.redirectErrorStream(true);
             Process process = processBuilder.start();
 
             StreamGobbler outputGobbler = new StreamGobbler("> ", process.getInputStream(), System.out);
             outputGobbler.start();
             StreamGobbler errorGobbler = new StreamGobbler("> ", process.getErrorStream(), System.err);
             errorGobbler.start();
 
             process.waitFor();
 
             outputGobbler.interrupt();
             errorGobbler.interrupt();
         }
         catch (URISyntaxException e) {
             LOGGER.log(Level.SEVERE, "Can't find jar file", e);
         }
         catch (IOException e) {
             LOGGER.log(Level.SEVERE, "Can't build process/read process output", e);
         }
         catch (InterruptedException e) {
             LOGGER.log(Level.SEVERE, "Interrupted while waiting for launched process", e);
         }
 
         LOGGER.info("Launcher terminated");
     }
 
 }
