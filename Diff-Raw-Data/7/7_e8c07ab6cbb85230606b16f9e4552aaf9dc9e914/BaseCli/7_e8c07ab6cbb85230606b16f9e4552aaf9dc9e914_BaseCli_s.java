 /*
  * Copyright 2013 NGDATA nv
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ngdata.hbaseindexer.cli;
 
 import com.google.common.collect.Lists;
 import joptsimple.OptionParser;
 import joptsimple.OptionException;
 import joptsimple.OptionSet;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.PropertyConfigurator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.regex.Pattern;
 
 /**
  * Provides a basic framework for the various CLI tools.
  *
  * <p>Implementations can implement or override the following methods:</p>
  *
  * <ul>
  *     <li>{@link #setupOptionParser()}</li>
  *     <li>{@link #getCmdName()}</li>
  *     <li>{@link #run(OptionSet)}</li>
  *     <li>{@link #cleanup()}</li>
  * </ul>
  *
  * <p>Implementations should call the {@link #run(String[])} method from their "public static void main".</p>
  */
 public abstract class BaseCli {
     public void run(String[] args) throws Exception {
         OptionParser parser = setupOptionParser();
 
         OptionSet options = null;
         try {
             options = parser.parse(args);
         } catch (OptionException e) {
             System.out.println("Error parsing command line options:");
             System.out.println(e.getMessage());
             printHelp(parser);
             System.exit(1);
         }
 
         if (options.has("h")) {
             printHelp(parser);
             System.exit(1);
         }
 
         if (!options.has("default-log")) {
             LogManager.resetConfiguration();
             PropertyConfigurator.configure(BaseCli.class.getResource("cli-log4j.properties"));
         }
 
         try {
             run(options);
         } catch (CliException e) {
            System.out.println(e.getMessage());
             System.exit(e.getExitCode());
         } catch (Exception e) {
            e.printStackTrace();
             System.exit(1);
         } finally {
             try {
                 cleanup();
             } catch (Throwable t) {
                 System.err.println("Error during cleanup:");
                 t.printStackTrace();
             }
         }
     }
 
     /**
      * Optionally override this to add CLI options. The canonical usage is to first call super,
      * and then add your own options to it.
      */
     protected OptionParser setupOptionParser() {
         OptionParser parser =  new OptionParser();
         parser.acceptsAll(Lists.newArrayList("h", "help"), "shows help for this command");
         parser.acceptsAll(Lists.newArrayList("dl", "default-log"), "don't override log4j config");
         return parser;
     }
 
     /**
      * The name of this command, for use in informational messages.
      */
     protected abstract String getCmdName();
 
     /**
      * Any cleanup that should be done. Called from a finally block around the {@link #run(OptionSet)} method.
      */
     protected void cleanup() {
     }
 
     /**
      * This method is where the actual work should be done. The canonical usage is to first call
      * super.
      */
     protected abstract void run(OptionSet options) throws Exception;
 
     protected void printHelp(OptionParser parser) throws IOException {
         printHelpHeader();
         parser.printHelpOn(System.out);
         printHelpFooter();
     }
 
     protected void printHelpHeader() throws IOException {
         String className = getClass().getName();
         String helpHeaderPath = className.replaceAll(Pattern.quote("."), "/") + "_help_header.txt";
         InputStream is = getClass().getClassLoader().getResourceAsStream(helpHeaderPath);
         if (is != null) {
             IOUtils.copy(is, System.out);
             System.out.println();
             System.out.println();
         }
     }
 
     protected void printHelpFooter() throws IOException {
         String className = getClass().getName();
         String helpHeaderPath = className.replaceAll(Pattern.quote("."), "/") + "_help_footer.txt";
         InputStream is = getClass().getClassLoader().getResourceAsStream(helpHeaderPath);
         if (is != null) {
             System.out.println();
             IOUtils.copy(is, System.out);
             System.out.println();
         }
     }
 }
