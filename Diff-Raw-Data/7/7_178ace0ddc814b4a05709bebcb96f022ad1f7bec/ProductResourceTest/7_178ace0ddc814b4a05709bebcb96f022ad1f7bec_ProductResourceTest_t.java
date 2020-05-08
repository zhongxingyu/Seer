 package net.binout;
 
 import com.github.kevinsawicki.http.HttpRequest;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.File;
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 
 
 @RunWith(Arquillian.class)
 public class ProductResourceTest {
 
     @Deployment
     public static WebArchive createDeployment() {
         return ShrinkWrap.create(WebArchive.class)
                 .addClasses(Product.class, ProductApplication.class, ProductRepository.class, ProductResource.class)
                 .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                 .setWebXML(webXML());
     }
 
     private static File webXML() {
         File file = new File("glassfish-jersey/src/main/webapp/WEB-INF/web.xml");
         if (!file.exists()) {
             file = new File("src/main/webapp/WEB-INF/web.xml");
         }
         return file;
     }
 
     @ArquillianResource
    URL baseUrl;
 
     @Test
     @RunAsClient
     public void get_and_add_product() throws Exception {
         int getCode = HttpRequest.get(new URL(baseUrl, "product/toto")).code();
         assertEquals(Response.Status.NO_CONTENT.getStatusCode(), getCode);
 
         String json = "{\"name\":\"toto\"}";
         int postCode = HttpRequest.post(new URL(baseUrl, "product")).contentType(MediaType.APPLICATION_JSON).send(json.getBytes()).code();
         assertEquals(Response.Status.CREATED.getStatusCode(), postCode);
 
         HttpRequest httpGetProductRequest = HttpRequest.get(new URL(baseUrl, "product/toto"));
         getCode = httpGetProductRequest.code();
         assertEquals(Response.Status.OK.getStatusCode(), getCode);
         assertEquals(json, httpGetProductRequest.body());
     }
 
 }
