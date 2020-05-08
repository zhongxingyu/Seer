 import org.junit.*;
import org.junit.Before;

 import play.test.*;
 import play.mvc.*;
 import play.mvc.Http.*;
 import models.*;
 
 public class ApplicationTest extends FunctionalTest {
 
    @Before
    public void setup() {
        Fixtures.deleteAll();
    }
	
     @Test
     public void testThatIndexPageWorks() {
         Response response = GET("/");
         assertIsOk(response);
         assertContentType("text/html", response);
        assertCharset("utf-8", response);
    }
    
    @Test
    public void testAdminSecurity() {
        Response response = GET("/admin");
        assertStatus(302, response);
        assertHeaderEquals("Location", "/secure/login", response);
     }
     
 }
