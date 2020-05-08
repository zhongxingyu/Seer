 package ch.hsr.wolski.omc.services.album;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.hsr.wolski.omc.model.Album;
 import ch.hsr.wolski.omc.model.Artist;
 import ch.hsr.wolski.omc.services.Service;
 import ch.hsr.wolski.omc.services.ServiceEnum;
 import ch.hsr.wolski.omc.services.ServiceImpl;
 import ch.hsr.wolski.omc.services.album.impl.AlbumServiceImpl;
 import ch.hsr.wolski.omc.services.utils.ObjectParser;
 import ch.hsr.wolski.omc.services.utils.impl.shiva.ShivaObjectParserImpl;
 
 /**
  * Created with IntelliJ IDEA.
 * User: Michael Wolski
  * Date: 9/23/13
  * Time: 8:57 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AlbumServiceImplTest {
     ObjectParser parser;
     AlbumServiceImpl service;
     
     @Before
     public void setUp() throws Exception {
         parser = new ShivaObjectParserImpl();
         service = new AlbumServiceImpl();
     }
 
     @After
     public void tearDown() throws Exception {
 
     }
 
     @Test
     public void testGetAllAlbums() throws Exception {
 
     }
 
     @Test
     public void testGetAlbumsByArtist() throws Exception {
 
     }
 
     @Test
     public void testGetAlbumById() throws Exception {
         String jsonString = "{\n" +
                 "    \"artists\": [\n" +
                 "        {\n" +
                 "            \"id\": 2,\n" +
                 "            \"uri\": \"/artist/2\"\n" +
                 "        },\n" +
                 "        {\n" +
                 "            \"id\": 5,\n" +
                 "            \"uri\": \"/artist/5\"\n" +
                 "        }\n" +
                 "    ],\n" +
                 "    \"name\": \"NOFX & Rancid - BYO Split Series (Vol. III)\",\n" +
                 "    \"year\": 2002,\n" +
                 "    \"uri\": \"/album/9\",\n" +
                 "    \"cover\": \"http://userserve-ak.last.fm/serve/300x300/72986694.jpg\",\n" +
                 "    \"id\": 9,\n" +
                 "    \"slug\": \"nofx-rancid-byo-split-series-vol-iii\"\n" +
                 "}";
         JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(jsonString);
         Album album = parser.parseAlbum(jsonObject);
         assertTrue(album.getName().equals("NOFX & Rancid - BYO Split Series (Vol. III)"));
         assertTrue(album.getYear() == 2002);
         assertTrue(album.getUriString().equals("/album/9"));
         assertTrue(album.getCoverUrlString().equals("http://userserve-ak.last.fm/serve/300x300/72986694.jpg"));
         assertTrue(album.getId() == 9);
         assertTrue(album.getSlug().equals("nofx-rancid-byo-split-series-vol-iii"));
 
         Artist artistOne = new Artist(2,"/artist/2");
         Artist artistTwo = new Artist(5,"/artist/5");
 
         assertTrue(album.getArtist().getId() == (artistOne.getId()));
         assertTrue(album.getArtist().getUriString().equals((artistOne.getUriString())));
 
         assertTrue(album.getArtist().getId() == (artistTwo.getId()));
         assertTrue(album.getArtist().getUriString().equals((artistTwo.getUriString())));
     }
 
     private Album parseAlbum(JSONObject json){
         Album result = new Album();
         result.setId(json.getInt("id"));
         result.setName(json.getString("name"));
         result.setUriString(json.getString("uri"));
         result.setYear(json.getInt("year"));
         result.setSlug(json.getString("slug"));
         result.setCoverUrlString(json.getString("cover"));
         return result;
     }
     
     @Test
     public void testGetAlbumByFromWebd() throws Exception {
     
     	List<Album> albums = new LinkedList<Album>();
         Service web = new ServiceImpl();
         JSONArray jsonArray =  (JSONArray) JSONSerializer.toJSON(web.doGETRequestGetArray(ServiceEnum.SERVICE_ALBUMS));
         for(Object o : jsonArray){
             JSONObject jsonObject = (JSONObject)JSONSerializer.toJSON(o.toString());
             Album album = parseAlbum(jsonObject);
             if(album != null){
                 albums.add(album);
             }
             assertTrue("Fehler", album.getArtist().getName().equals("ELO's Greatest Hits"));
         }
         
         assertTrue("Fehler", albums.size() == 1);
     }
     
     
 }
