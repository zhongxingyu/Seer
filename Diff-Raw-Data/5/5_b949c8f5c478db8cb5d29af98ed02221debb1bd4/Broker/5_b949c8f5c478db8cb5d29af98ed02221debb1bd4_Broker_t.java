 package com.meancat.bronzethistle.broker;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.util.StatusPrinter;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.util.SystemPropertyUtils;
 
 import java.io.File;
 
 public class Broker {
     private static final Logger log = LoggerFactory.getLogger(Broker.class);
 
 
     private static class ApplicationArguments {
         @Option(name = "-h", aliases = {"--home"}, usage = "The home directory of the application.", required = true)
         private String home;
     }
 
     public static void main(String[] args) throws InterruptedException {
         ApplicationArguments appArgs = new ApplicationArguments();
         CmdLineParser commandLineParser = new CmdLineParser(appArgs);
         commandLineParser.setUsageWidth(80);
         try {
             commandLineParser.parseArgument(args);
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             System.err.println();
             commandLineParser.printUsage(System.err);
             return;
         }
         System.setProperty("app.home", appArgs.home);
         LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
         try {
             JoranConfigurator configurator = new JoranConfigurator();
             configurator.setContext(loggerContext);
             loggerContext.reset();
             configurator.doConfigure(new File(SystemPropertyUtils.resolvePlaceholders("${app.home}/conf/logback.xml")));
         } catch(JoranException ignored) {
             // We'll print an error later if something blows up
         }
         // printing if theres a problem:
         StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
         System.out.println("Starting Broker");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
         applicationContext.register(BrokerConfiguration.class);
         applicationContext.refresh();
         System.out.println("Started Broker");
         while(applicationContext.isActive()) {
             Thread.sleep(5000);
         }
         System.out.println("Stopped Broker");
     }
 }
