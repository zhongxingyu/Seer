 package example;
 
 
 import org.apache.cxf.wsn.jaxws.GetResourcePropertyService;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Utilistateur
  * Date: 30/07/13
  * Time: 14:19
  * To change this template use File | Settings | File Templates.
  */
 public class HelloWorldClient {
   public static void main(String[] argv) {
      com.DocumentService service = new GetResourcePropertyService().getPort();
      //invoke business method
      service.renew();
   }
 }
