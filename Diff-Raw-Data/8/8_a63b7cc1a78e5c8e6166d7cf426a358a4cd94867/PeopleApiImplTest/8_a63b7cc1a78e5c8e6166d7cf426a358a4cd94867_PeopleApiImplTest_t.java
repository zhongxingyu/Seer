 package org.mymediadb.api.tmdb.internal.api;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mymediadb.api.tmdb.api.PeopleApi;
 import org.mymediadb.api.tmdb.api.TmdbApi;
 import org.mymediadb.api.tmdb.model.Person;
 
 import java.util.List;
 
 import static junit.framework.Assert.*;
 import static org.junit.Assume.assumeNotNull;
 
 public class PeopleApiImplTest {
 
     private PeopleApi peopleApi;
     private TmdbApi tmdbApi;
 
     @Before
     public void setup() {
         tmdbApi = TmdbApiImpl.getInstance();
         peopleApi = tmdbApi.getPeopleApi();
         assumeNotNull(tmdbApi.getApiKey());
     }
 
     @Test
     public void testSearch() {
        List person = peopleApi.search("Pitt");
         assertNotNull(person);
         assertTrue(person.size() > 0);
     }
 
     @Test
     public void testSearchWithEmptyQuery() {
         List person = peopleApi.search("");
         assertNull(person);
     }
 
     @Test
     public void testSearchWithNullQuery() {
         List person = peopleApi.search(null);
         assertNull(person);
     }
 
     @Test
     public void testGetInfo() {
         Person person = peopleApi.getInfo(287);
         assertNotNull(person);
     }
 
     @Test
     public void testGetInfoWithNonExistantId() {
         Person person = peopleApi.getInfo(0);
         assertNull(person);
     }
 
     @Test
     public void testGetLatest() {
         Person person = peopleApi.getLatest();
         assertNotNull(person);
     }
 
     @Test
     public void testGetVersion() {
         List people = peopleApi.getVersion(287, 288);
         assertNotNull(people);
     }
 
     @Test
     public void testGetVersionWithEmptyString() {
         List people = peopleApi.getVersion("");
         assertNull(people);
     }
 
     @Test
     public void testGetVersionWithNullValue() {
         String id = null;
         List people = peopleApi.getVersion(id);
         assertNull(people);
     }
 
     @Test
     public void testGetVersionWithNonExistantId() {
         List people = peopleApi.getVersion(0);
         assertNull(people);
     }
 
 }
