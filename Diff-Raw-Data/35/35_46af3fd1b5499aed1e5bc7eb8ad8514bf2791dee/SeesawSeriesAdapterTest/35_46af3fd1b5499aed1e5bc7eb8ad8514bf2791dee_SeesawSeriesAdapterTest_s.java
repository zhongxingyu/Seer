 package org.atlasapi.remotesite.seesaw;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 
 public class SeesawSeriesAdapterTest extends TestCase {
     SiteSpecificAdapter<Series> adapter = new SeesawSeriesAdapter(HttpClients.webserviceClient());
 
     public void testShouldGetSeries() {
        Series thirtyRock = adapter.fetch("http://www.seesaw.com/TV/Comedy/s-29158-30-Rock");
        assertEquals("Series 2", thirtyRock.getTitle());
        assertTrue(thirtyRock.getContents().size() > 0);
        assertTrue(thirtyRock.getGenres().contains("http://www.seesaw.com/TV/Comedy"));
        Item firstItem = (Item) thirtyRock.getContents().get(0);
         assertTrue(firstItem instanceof Episode);
         Episode firstEpisode = (Episode) firstItem;
        assertEquals("SeinfeldVision", firstEpisode.getTitle());
     }
 }
