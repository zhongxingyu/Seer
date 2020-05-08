 package controllers.origo.admin;
 
 import org.junit.Test;
 import play.mvc.Http;
 import play.test.FunctionalTest;
 
 public class AdminApplicationTest extends FunctionalTest {
 
     @Test
     public void dashboardPageWorks() {
         Http.Response response = GET("/admin");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
     }
 
     @Test
     public void pagesPageWorks() {
         Http.Response response = GET("/admin/crud/pages");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
     }
 
     @Test
    public void segmentsPageWorks() {
        Http.Response response = GET("/admin/crud/segments");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
     }
 
     @Test
     public void contentsPageWorks() {
         Http.Response response = GET("/admin/crud/contents");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
     }
 
     @Test
     public void aliasPageWorks() {
         Http.Response response = GET("/admin/crud/aliases");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
     }
 
 }
