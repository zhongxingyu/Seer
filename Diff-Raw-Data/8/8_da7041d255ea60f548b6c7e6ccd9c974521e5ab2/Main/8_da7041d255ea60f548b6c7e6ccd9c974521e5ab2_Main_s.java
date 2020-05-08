 package com.robusta.pdc;
 
 import com.robusta.pdc.command.line.CommandLineParser;
 import com.robusta.pdc.command.line.ParseCommandLineArgumentHasFailed;
 import com.robusta.pdc.command.line.UserHasAskedForHelp;
 import com.robusta.pdc.domain.CommandLineArguments;
 import com.robusta.pdc.domain.ImportTracking;
 
 import static com.robusta.pdc.command.line.CLAParserFactory.parser;
 import static com.robusta.pdc.reporting.ReportingFactory.errorReporter;
 import static com.robusta.pdc.reporting.ReportingFactory.trackingVisitor;
 import static com.robusta.pdc.scanner.ScannerFactory.importStatementScanner;
 import static com.robusta.pdc.tracking.ImportTrackingFactory.violationTracking;
 
 public class Main {
     public static void main(String[] args) {
         bootstrap(args);
         try {
             CommandLineArguments cla = parser(errorReporter()).parseCommandLine(args);
             new PackageDependencyChecker().doWork(cla);
         } catch (ParseCommandLineArgumentHasFailed parseCommandLineArgumentHasFailed) {
             System.exit(-1);
         } catch (UserHasAskedForHelp userHasAskedForHelp) {
             System.exit(0);
         }
         System.exit(0);
     }
 
     private static void bootstrap(String[] args) {
         // Must be done as bootstrap. this will ensure that the logging subsystem is
         // properly setup.
 
         // If log system is initialized prior to this step, the DomConfigurator needs to
         // be invoked. Adds a nasty dependency to log4j from the app, but there is no
         // slf4j way to reinit the log system.
         new LoggingInterceptor().interceptAndSetup(args);
     }
 
     public static class PackageDependencyChecker {
         public PackageDependencyChecker() {
         }
 
         public void doWork(CommandLineArguments cla) throws ParseCommandLineArgumentHasFailed, UserHasAskedForHelp {
             ImportTracking tracking = violationTracking(cla.allowedPackages());
             importStatementScanner(tracking).scan(cla.sourceFolders(), cla.packageNames());
             tracking.allowVisitor(trackingVisitor());
         }
     }
 
     private static class LoggingInterceptor {
         public void interceptAndSetup(String[] args) {
             boolean isVerbose = false;
             for (String arg : args) {
                if(CommandLineParser.OPTION_VERBOSE.equals(arg)) {
                     isVerbose = true;
                 }
             }
             System.setProperty("log.level", isVerbose ? "DEBUG": "INFO");
         }
     }
 }
