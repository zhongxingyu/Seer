 package app;
 
 import models.Fruit;
 import org.milyn.Smooks;
 import org.milyn.container.ExecutionContext;
 import org.milyn.event.report.HtmlReportGenerator;
 import org.milyn.payload.JavaSource;
 import org.milyn.payload.StringResult;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 
 /**
  * @author Ludovico Fischer
  */
 public class App {
     public static void  main(String[] ars) throws IOException, SAXException {
         Fruit apple = new Fruit();
        Smooks smooks = new Smooks("smooks-config.xml");
         try {
             JavaSource source = new JavaSource(apple);
             ExecutionContext executionContext = smooks.createExecutionContext();
             executionContext.setEventListener(new HtmlReportGenerator("target/reports/report.html"));
             smooks.filterSource(executionContext, source, new StringResult());
         } finally {
             smooks.close();
         }
     }
 }
