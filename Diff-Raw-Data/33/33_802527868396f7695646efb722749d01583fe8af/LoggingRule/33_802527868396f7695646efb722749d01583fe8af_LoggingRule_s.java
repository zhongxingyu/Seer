 package net.praqma.util.test.junit;
 
 import net.praqma.logging.LoggingUtil;
 import net.praqma.logging.PraqmaticLogFormatter;
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 import java.util.LinkedList;
 import java.util.List;
import java.util.logging.Handler;
 import java.util.logging.Level;
 
 public class LoggingRule implements TestRule {
 
     private Level level;
    private List<Handler> handlers;
     private List<String> loggerNames = new LinkedList<String>();
     private String format = PraqmaticLogFormatter.NORMAL_FORMAT;
 
     public LoggingRule( Level level ) {
         this.level = level;
     }
 
     public LoggingRule( Level level, String ... loggerNames ) {
         this.level = level;
 
         for( String ln : loggerNames ) {
             this.loggerNames.add( ln );
         }
     }
 
     public LoggingRule( String ... loggerNames ) {
         checkLoggingOption();
 
         for( String ln : loggerNames ) {
             this.loggerNames.add( ln );
         }
     }
 
     public LoggingRule setFormat( String format ) {
         this.format = format;
 
         return this;
     }
 
    public void checkLoggingOption() {
        String level = System.getProperty( "loggingLevel", "INFO" ).toUpperCase();
        this.level = Level.parse( level );
     }
 
     private void before() {
         LoggingUtil.setPraqmaticHandler( level, loggerNames, format );
     }
 
     private void after() {
 
     }
 
     @Override
     public Statement apply( final Statement base, final Description description ) {
         return new Statement() {
 
             @Override
             public void evaluate() throws Throwable {
                 System.out.println( " ===== Setting up logger =====" );
                 before();
                 try {
                     base.evaluate();
                 } finally {
                     after();
                 }
             }
         };
     }
 }
