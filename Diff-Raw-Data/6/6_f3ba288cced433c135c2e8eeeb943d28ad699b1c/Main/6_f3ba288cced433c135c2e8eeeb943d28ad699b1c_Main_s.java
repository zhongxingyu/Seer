 /*
  * Copyright 2011 Gregory P. Moyer
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
 package org.syphr.mythtv.monitor.cli;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.syphr.mythtv.monitor.Monitor;
 import org.syphr.mythtv.monitor.MonitorException;
 import org.syphr.mythtv.monitor.QuartzMonitor;
 import org.syphr.mythtv.monitor.config.MonitorConfig;
 import org.syphr.mythtv.monitor.config.MonitorConfigException;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 public class Main
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
 
     private static final String LOG_CONFIG_RESOURCE = "logback-config.xml";
 
     private static final String LOG_PROPERTY_FILE = "org.syphr.mythtv.monitor.logfile";
     private static final String LOG_PROPERTY_LEVEL = "org.syphr.mythtv.monitor.loglevel";
     private static final String LOG_PROPERTY_NOCONSOLE = "org.syphr.mythtv.monitor.noconsole";
 
     public static void main(String[] args)
     {
         CommandLineParser parser = new PosixParser();
         CommandLine cl;
         try
         {
             cl = parser.parse(CliOption.getOptions(), args);
         }
         catch (ParseException e)
         {
             LOGGER.error(e.getMessage(), e);
             dumpUsage("Failed to parse command line: " + e.getMessage());
 
             return; // appease javac
         }
 
         if (CliOption.HELP.hasOption(cl))
         {
             dumpUsage(null);
         }
 
         try
         {
             configureLogging(cl);
         }
         catch (IOException e)
         {
             dumpUsage(e.getMessage());
         }
 
         MonitorConfig config = new MonitorConfig();
         try
         {
             if (CliOption.CONFIG.hasOption(cl))
             {
                 config.load(new File(CliOption.CONFIG.getValue(cl)));
             }
             else
             {
                 config.load();
             }
         }
         catch (IOException e)
         {
             LOGGER.error(e.getMessage(), e);
             dumpUsage("Failed to read config file: " + e.getMessage());
 
             return; // appease javac
         }
         catch (MonitorConfigException e)
         {
             LOGGER.error(e.getMessage(), e);
             dumpUsage("Failed to parse config file: " + e.getMessage());
 
             return; // appease javac
         }
 
         LOGGER.info("Starting monitor");
         Monitor monitor = new QuartzMonitor(config);
         try
         {
             monitor.start();
         }
         catch (MonitorException e)
         {
             LOGGER.error(e.getMessage(), e);
 
             System.out.println("MythTV Monitor failed to start. See log for details.");
             System.exit(1);
         }
     }
 
     private static void dumpUsage(String errorMsg)
     {
         boolean error = errorMsg != null;
 
         if (error)
         {
             System.out.println(errorMsg);
             System.out.println();
         }
 
         new HelpFormatter().printHelp(Main.class.getName() + " [OPTIONS]", CliOption.getOptions());
 
         System.exit(error ? 1 : 0);
     }
 
     private static void configureLogging(CommandLine cl) throws IOException
     {
         InputStream configStream;
         if (CliOption.LOG_CONFIG.hasOption(cl))
         {
             configStream = new FileInputStream(CliOption.LOG_CONFIG.getValue(cl));
         }
         else
         {
             configStream = Main.class.getResourceAsStream(LOG_CONFIG_RESOURCE);
             if (configStream == null)
             {
                 throw new IOException("Failed to find logging configuration. The application library is corrupted.");
             }
         }
 
         try
         {
             LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
 
             JoranConfigurator jc = new JoranConfigurator();
             jc.setContext(context);
 
             context.reset();
 
             if (CliOption.LOG_FILE.hasOption(cl))
             {
                 context.putProperty(LOG_PROPERTY_FILE, CliOption.LOG_FILE.getValue(cl));
             }
             if (CliOption.LOG_LEVEL.hasOption(cl))
             {
                 context.putProperty(LOG_PROPERTY_LEVEL, CliOption.LOG_LEVEL.getValue(cl));
             }
             if (CliOption.QUIET.hasOption(cl))
             {
                 context.putProperty(LOG_PROPERTY_NOCONSOLE, Boolean.TRUE.toString());
             }
 
             try
             {
                 jc.doConfigure(configStream);
             }
            catch (JoranException je)
             {
                throw new IOException("Failed to read logging configuration. The application library is corrupted.");
             }
         }
         finally
         {
             configStream.close();
         }
     }
 }
