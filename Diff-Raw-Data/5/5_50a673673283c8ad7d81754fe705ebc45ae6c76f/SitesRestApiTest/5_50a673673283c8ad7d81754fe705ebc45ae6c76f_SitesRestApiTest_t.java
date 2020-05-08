 package org.gatein.portal.tests.sites;
 
 import com.jayway.restassured.path.json.JsonPath;
 import org.gatein.portal.tests.AbstractRestApiTest;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.*;
 
 /**
  * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
  */
 public class SitesRestApiTest extends AbstractRestApiTest {
 
     @Test
     public void anonymous_sites() {
         JsonPath json = given().anonymous().expect().statusCode(SC_OK)
             .get("/sites").jsonPath();
 
         List<Map<String, String>> sites = json.getList("");
         for (Map<String, String> site : sites) {
             assertEquals("site", site.get("type"));
             assertNotNull(site.get("url"));
         }
     }
 
     @Test
     public void anonymous_spaces() {
         JsonPath json = given().anonymous().expect().statusCode(SC_OK)
             .get("/spaces").jsonPath();
 
         List<Map<String, String>> sites = json.getList("");
         for (Map<String, String> site : sites) {
             assertEquals("space", site.get("type"));
             assertNotNull(site.get("url"));
         }
     }
 
     @Test
     public void sites() {
         JsonPath json = given().user("root").expect().statusCode(SC_OK)
             .get("/sites").jsonPath();
 
         List<Map<String, String>> sites = json.getList("");
         for (Map<String, String> site : sites) {
             assertEquals("site", site.get("type"));
             assertNotNull(site.get("url"));
         }
     }
 
     @Test
     public void spaces() {
         JsonPath json = given().user("root").expect().statusCode(SC_OK)
             .get("/spaces").jsonPath();
 
         List<Map<String, String>> sites = json.getList("");
         for (Map<String, String> site : sites) {
             assertEquals("space", site.get("type"));
             assertNotNull(site.get("url"));
         }
     }
 
     //TODO: Find way to test dashboards, they aren't created until the user logs into the portal
 
     @Test
     public void anonymous_spaces_notauthorized() {
         given().anonymous().expect().statusCode(SC_UNAUTHORIZED)
             .get("/spaces/platform/administrators");
     }
 
     @Test
     public void site_update() throws Exception {
         // Get current displayName & description so we can set it back if tests succeed
         JsonPath json = given().anonymous().expect().statusCode(SC_OK)
             .get("sites/classic").jsonPath();
 
         String displayName = json.getString("displayName");
         String description = json.getString("description");
 
         // Update site classic
         json = given().user("root")
             .body("{\"displayName\":\"Test\", \"description\":\"REST API - Test\"}")
             .expect().statusCode(SC_OK)
             .put("/sites/classic").jsonPath();
 
         assertEquals("Test", json.getString("displayName"));
         assertEquals("REST API - Test", json.getString("description"));
 
         // root get
         Thread.sleep(300);
         json = given().user("root").expect().statusCode(SC_OK)
             .get("sites/classic").jsonPath();
 
         // Verify data
         assertEquals("Test", json.getString("displayName"));
         assertEquals("REST API - Test", json.getString("description"));
 
         // Anonymous get
         json = given().anonymous().expect().statusCode(SC_OK)
             .get("sites/classic").jsonPath();
 
         // Verify data
         assertEquals("Test", json.getString("displayName"));
         assertEquals("REST API - Test", json.getString("description"));
 
         // Update site classic back to original displayName and description
         json = given().user("root")
             .body("{\"displayName\":\"" + displayName + "\", \"description\":\"" + description + "\"}")
             .expect().statusCode(SC_OK)
             .expect().statusCode(SC_OK)
             .put("/sites/classic").jsonPath();
 
        Thread.sleep(500);

         // Verify data is set back
         assertEquals(displayName, json.getString("displayName"));
         assertEquals(description, json.getString("description"));
     }
 
     @Test
     public void site_update_not_found() {
         given().user("root")
             .body("{\"displayName\":\"Test\"}")
             .expect().statusCode(SC_NOT_FOUND)
             .put("/sites/abcdefg");
     }
 
     @Test
     public void anonymous_site_update() throws Exception {
         given().anonymous().expect().statusCode(SC_UNAUTHORIZED).put("/sites/classic");
     }
 
     @Test
     public void site_delete() throws Exception {
         // Create site
         given().user("root").expect().statusCode(SC_OK)
             .post("/sites/delete");
 
         Thread.sleep(300);
 
         // Finally delete the site
         given().user("root").expect().statusCode(SC_OK)
             .delete("/sites/delete");
 
         Thread.sleep(300);
 
         // Delete again and should get 404
         given().user("root").expect().statusCode(SC_NOT_FOUND)
             .delete("/sites/delete");
     }
 
     @Test
     public void site_delete_not_found() {
         given().user("root").expect().statusCode(SC_NOT_FOUND)
             .delete("/sites/abcdefg");
     }
 
     @Test
     public void anonymous_site_delete() throws Exception {
         given().anonymous().expect().statusCode(SC_UNAUTHORIZED)
             .delete("/sites/classic");
     }
 
     @Test
     public void site_create() throws Exception {
         // Create site
         given().user("root").expect().statusCode(SC_OK)
             .post("/sites/create");
 
         Thread.sleep(300);
 
         // Make sure it exists
         given().user("root").expect().statusCode(SC_OK)
             .get("sites/create");
 
         // Ensure anonymous access since it's available to Everyone
         given().anonymous().expect().statusCode(SC_OK)
             .get("/sites/create");
 
         // Make sure we get conflict if we try and create it again
         given().user("root").expect().statusCode(SC_CONFLICT)
             .post("/sites/create");
 
         // Finally delete the site
         given().user("root").expect().statusCode(SC_OK)
             .delete("/sites/create");

        Thread.sleep(500);
     }
 
     @Test
     public void site_create_existing() {
         given().user("root").expect().statusCode(SC_CONFLICT)
             .post("/sites/classic");
     }
 
     @Test
     public void anonymous_site_create() throws Exception {
         given().anonymous().expect().statusCode(SC_UNAUTHORIZED)
             .post("/sites/classic");
     }
 
     @Test
     public void site_update_unauthorized() throws Exception {
         // Create site
         given().user("root").expect().statusCode(SC_OK)
             .post("/sites/update");
 
         Thread.sleep(300);
 
         // Update permissions
         given().user("root")
             .body("{\"access-permissions\": [\"/platform/administrators\"]}")
             .expect().statusCode(SC_OK)
             .put("/sites/update");
 
         Thread.sleep(300);
 
         // Ensure anonymous no longer has access
         given().anonymous().expect().statusCode(SC_UNAUTHORIZED)
             .get("/sites/update");
 
         // Ensure root still has access
         given().user("root").expect().statusCode(SC_OK)
             .get("/sites/update");
 
         // Finally delete the site
         given().user("root").expect().statusCode(SC_OK)
             .delete("/sites/update");
     }
 
     @Test
     public void site_not_found() {
         given().user("root").expect().statusCode(SC_NOT_FOUND)
             .get("/sites/does-not-exist");
 
         given().anonymous().expect().statusCode(SC_NOT_FOUND)
                 .get("/sites/does-not-exist");
     }
 
     @Test
     public void site_bad_request_invalid_type() throws Exception {
         given().user("root").body("{\"displayName\":[{\"foo\":\"bar\"}]}").expect().statusCode(SC_BAD_REQUEST)
             .put("/sites/classic");
     }
 }
