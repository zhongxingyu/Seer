 package ec.ep.europarl.camel.routebuilder;
 
 import ec.ep.europarl.camel.component.FtpReceiver;
 import ec.ep.europarl.camel.component.FtpSender;
 import org.apache.camel.spring.SpringRouteBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  * @author Bruno Dusausoy
  */
 @Component
 public class SimpleRouteBuilder extends SpringRouteBuilder {
 
     @Autowired
     private FtpSender ftpSender;
 
     @Autowired
     private FtpReceiver ftpReceiver;
 
     @Override
     public void configure() throws Exception {
         from("file:src/data")
                 .bean(ftpSender)
                 .delay(10000)
                 .bean(ftpReceiver)
                 .log("End route");
     }
 }
