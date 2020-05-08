 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.core;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 
import de.cosmocode.commons.State;

 /**
  * Application entry point.
  *
  * @author Willi Schoenborn
  * @author Tobias Sarnowski
  */
 public final class Main {
 
     private static final Logger LOG = LoggerFactory.getLogger(Main.class);
 
     @Option(name = "-s", required = false, aliases = "--state-file", usage = "Path to state file")
     private File stateFile;
 
     @Option(name = "-n", required = false, aliases = "--no-auto-shutdown", 
         usage = "If the framework should shut down as soon as possible after boot")
     private boolean noAutoShutdown;
 
     @Argument
     private List<String> arguments = Lists.newArrayList();
 
 
     private final Framework framework;
 
     private Main(String[] args) {
         final CmdLineParser parser = new CmdLineParser(this);
 
         try {
             parser.parseArgument(args);
 
             if (arguments.size() == 0) {
                 throw new CmdLineException("no configuration files given");
             }
         } catch (CmdLineException e) {
             System.err.println("Usage:  java [options] configuration-files...");
             parser.printUsage(System.err);
             throw new IllegalArgumentException(e);
         }
 
         // merge configuration files
         final Properties properties = new Properties();
         for (String config : arguments) {
             final File configFile = new File(config);
             Preconditions.checkState(configFile.exists(), "Configuration file %s does not exist", 
                 configFile.getAbsolutePath()
             );
 
             mergeProperties(properties, configFile);
         }
 
         try {
             framework = Palava.createFramework(properties);
         } finally {
             persistState();
         }
     }
 
     private void mergeProperties(Properties properties, File configFile) {
         final Properties props = new Properties();
         final Reader reader;
 
         try {
             LOG.trace("loading config " + configFile.getAbsolutePath());
             reader = new FileReader(configFile);
         } catch (FileNotFoundException e) {
             throw new IllegalArgumentException(e);
         }
 
         try {
             properties.load(reader);
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         } finally {
             try {
                 reader.close();
             } catch (IOException e) {
                 throw new IllegalStateException(e);
             }
         }
 
         for (Map.Entry<Object, Object> entry : props.entrySet()) {
             properties.setProperty((String) entry.getKey(), (String) entry.getValue());
         }
     }
 
     private void start() {
         try {
             framework.start();
             persistState();
         /* CHECKSTYLE:OFF */
         } catch (RuntimeException e) {
         /* CHECKSTYLE:ON */
             LOG.error("startup failed", e);
             stop();
         }
     }
 
     private void stop() {
         try {
             framework.stop();
         } finally {
             persistState();
         }
     }
 
     private void persistState() {
         if (stateFile == null) return;
 
         try {
             final Writer writer = new FileWriter(stateFile);
            writer.write((framework == null ? State.FAILED : framework.currentState()).name() + "\n");
             writer.close();
         } catch (IOException e) {
             throw new IllegalArgumentException("cannot persist state to file", e);
         }
     }
 
     private boolean isNoAutoShutdown() {
         return noAutoShutdown;
     }
 
     /**
      * Application entry point.
      *
      * @param args command line arguments
      * @throws CmdLineException if command line parsing failed
      */
     public static void main(String[] args) throws CmdLineException {
         logAsciiArt();
 
         final Main main;
         
         try {
             main = new Main(args);
         /* CHECKSTYLE:OFF */
         } catch (RuntimeException e) {
         /* CHECKSTYLE:ON */
             LOG.error("configuration error", e);
             throw e;
         }
 
         main.start();
 
         LOG.debug("adding shutdown hook");
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 
             @Override
             public void run() {
                 main.stop();
             }
 
         }));
 
         if (main.isNoAutoShutdown()) {
             LOG.debug("automatic shutdown disabled; running until someone else triggers the shutdown");
 
             // block until someone else triggeres the jvm shutdown
             synchronized (Thread.currentThread()) {
                 try {
                     Thread.currentThread().wait();
                 } catch (InterruptedException e) {
                     // it's ok, shut down
                     LOG.debug("main thread interrupted", e);
                 }
             }
         }
     }
 
     private static void logAsciiArt() {
         InputStream asciiStream;
 
         final File localFile = new File("lib/palava-ascii.txt");
         int size = 4096;
         try {
             asciiStream = new FileInputStream(localFile);
             size = (int) localFile.length();
         } catch (FileNotFoundException e) {
             final URL resource = Main.class.getClassLoader().getResource("palava-ascii.txt");
             if (resource == null) {
                 LOG.info("===== PALAVA =====");
                 return;
             }
             try {
                 asciiStream = resource.openStream();
             } catch (IOException e1) {
                 LOG.info("===== PALAVA =====");
                 return;
             }
         }
 
         final byte[] buffer = new byte[size];
         int length;
         try {
             length = asciiStream.read(buffer);
         } catch (IOException e) {
             LOG.info("===== PALAVA =====");
             return;
         }
         final String message = new String(buffer, 0, length, Charset.forName("UTF-8"));
         LOG.info("welcome to\n{}", message);
     }
 
 }
