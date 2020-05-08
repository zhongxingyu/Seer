 package kanbannow;
 
 
 import ch.qos.logback.classic.Level;
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 import kanbannow.health.BacklogItemHealthCheck;
 
 import ch.qos.logback.classic.Logger;
 import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
 import kanbannow.logback.LogLevelTripwireAppender;
import kanbannow.resources.HelloWorldResource;
 import org.slf4j.LoggerFactory;
 import ch.qos.logback.classic.LoggerContext;
 
 
 public class BacklogItemService extends Service<BacklogItemServiceConfiguration> {
 
 
     private LogLevelTripwireAppender appender;
 
 
     public static void main(String[] args) throws Exception {
         new BacklogItemService().run(args);
     }
 
 
     @Override
     public void initialize(Bootstrap<BacklogItemServiceConfiguration> bootstrap) {
 
     }
 
     // Test change
     @Override
     public void run(BacklogItemServiceConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new HelloWorldResource());
         environment.addHealthCheck(new BacklogItemHealthCheck());
         setupExceptionThrowingLogger();
     }
 
     private void setupExceptionThrowingLogger() {
         LoggerContext loggerContext = createAppender();
         Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
         logbackLogger.addAppender(appender);
     }
 
     private LoggerContext createAppender() {
         LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
         appender = new LogLevelTripwireAppender(Level.WARN);
         appender.setContext(loggerContext);
         PatternLayoutEncoder encoder = createEncoder(loggerContext);
         appender.setEncoder(encoder);
         appender.start();
         return loggerContext;
     }
 
     private PatternLayoutEncoder createEncoder(LoggerContext loggerContext) {
         PatternLayoutEncoder encoder = new PatternLayoutEncoder();
         encoder.setContext(loggerContext);
         encoder.setPattern("%-4relative [%thread] %-5level %logger{35} - %msg%n");
         encoder.start();
         return encoder;
     }
 
 
     public boolean warningOrErrorWasLogged() {
         return appender.wasThresholdReached();
     }
 
 
 
 }
