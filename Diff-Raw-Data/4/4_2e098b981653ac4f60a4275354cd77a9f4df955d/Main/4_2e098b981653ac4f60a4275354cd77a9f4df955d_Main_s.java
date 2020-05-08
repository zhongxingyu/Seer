 /*
  *  Copyright 2010 Johannes Th&ouml;nes <johannes.thoenes@googlemail.com>.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package de.bergischweb.ips;
 
 import org.jruby.embed.PathType;
 import org.jruby.embed.ScriptingContainer;
 
 import javax.swing.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 /**
  * The Main class for starting the scripts.
  *
  * @author Copyright 2010 Johannes Th&ouml;nes <johannes.thoenes@googlemail.com>
  */
 public class Main {
 
 
     public static class FatalExceptionHandler implements Thread.UncaughtExceptionHandler {
         /**
          * Method invoked when the given thread terminates due to the
          * given uncaught exception.
          * <p>Any exception thrown by this method will be ignored by the
          * Java Virtual Machine.
          *
          * @param t the thread
          * @param e the exception
          */
         @Override
         public void uncaughtException(Thread t, Throwable e) {
             try {
                File f = File.createTempFile("error", "log");
                 PrintWriter writer = new PrintWriter(f);
                 e.printStackTrace(writer);
 
                 message("Unexpected Error", "An error occured. Please see the log file " + f.getPath());
                 e.printStackTrace();
 
             } catch (Exception e1) {
                 StringWriter result = new StringWriter();
                 PrintWriter writer = new PrintWriter(result);
                 e1.printStackTrace(writer);
 
                 message("Fatal Error", "StackTrace: \n\n" + result.toString());
                 e1.printStackTrace();
             }
         }
 
         private void message(String title, String message) {
             JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * The entry point into the simulation.
      * <p/>
      * Executes the Ruby Start Scripts. The first parameter is the name of the script.
      * The rest ist passed as parameter to the script.
      *
      * @param args Ignored
      */
     public static void main(String... args) throws IOException {
         Thread.setDefaultUncaughtExceptionHandler(new FatalExceptionHandler());
         
         ClassLoader classLoader = Main.class.getClassLoader();
 
         ScriptingContainer container = new ScriptingContainer();
         container.put("$CLASS_LOADER", container.getProvider().getRuntime().getJRubyClassLoader());
         container.runScriptlet(PathType.CLASSPATH, "scripts/run_gui.rb");
     }
 }
