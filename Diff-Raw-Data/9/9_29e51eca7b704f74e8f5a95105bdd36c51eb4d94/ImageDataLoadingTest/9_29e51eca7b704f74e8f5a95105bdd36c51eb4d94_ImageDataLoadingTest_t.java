 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParser;
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.routing.Router;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.junit.*;
 
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertNotEquals;
 
 public class ImageDataLoadingTest {
 
     private static Logger log = Logger.getLogger(ImageDataLoadingTest.class);
 
     @Before
     public void setUp() throws Exception {
         BasicConfigurator.configure();
     }
 
     @Test
     public void testCards() {
         JsonParser parser = new JsonParser();
         JsonArray cardJSON = parser
                 .parse(new InputStreamReader(ImageDataLoadingTest.class.getResourceAsStream(SevenWonders.PATH_CARDS)))
                 .getAsJsonObject()
                 .get(Router.PROP_ROUTE_CARDS)
                 .getAsJsonArray();
         JsonArray wonderJSON = parser
                 .parse(new InputStreamReader(ImageDataLoadingTest.class.getResourceAsStream(SevenWonders.PATH_WONDERS)))
                 .getAsJsonObject()
                 .get(Router.PROP_ROUTE_WONDERS)
                 .getAsJsonArray();
 
         List<Card> cards = Card.parseDeck(cardJSON);
         Map<String, Wonder> wonders = Wonder.parseWonders(wonderJSON);
         URL blankurl = null;
         try {
             blankurl = new URL("file://");
         } catch (MalformedURLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
        log.info("Testing " + cards.size() + "/148 cards.");
         for(Card c : cards) {
             assertNotEquals(c.getId()+" image not found",c.getImagePath().getPath(), blankurl.getPath());
         }
 
         log.info("Testing " + wonders.size() + "/7 wonders.");
         for(Wonder w : wonders.values()) {
             w.setSide(Wonder.PROP_WONDER_SIDE_A);
             assertNotEquals(w.getId()+" image not found",w.getImagePath().getPath(), blankurl.getPath());
             w.setSide(Wonder.PROP_WONDER_SIDE_B);
             assertNotEquals(w.getId()+" image not found", w.getImagePath().getPath(), blankurl.getPath());
         }
     }
 }
