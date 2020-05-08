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
        Series inbetweeners = adapter.fetch("http://www.seesaw.com/TV/Comedy/b-5353-The-Inbetweeners");
        assertEquals("Series 1", inbetweeners.getTitle());
        assertTrue(inbetweeners.getContents().size() > 0);
        assertTrue(inbetweeners.getGenres().contains("http://www.seesaw.com/TV/Comedy"));
        Item firstItem = (Item) inbetweeners.getContents().get(0);
         assertTrue(firstItem instanceof Episode);
         Episode firstEpisode = (Episode) firstItem;
        assertEquals("Episode 1", firstEpisode.getTitle());
     }
 }
