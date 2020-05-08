 package org.atlasapi.remotesite.hulu;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.remotesite.hulu.HuluItemAdapter;
 import org.atlasapi.remotesite.hulu.HuluItemAdapter.HuluItemCanonicaliser;
 
 public class HuluItemAdapterTest extends TestCase {
     
     String episodeUrl = "http://www.hulu.com/watch/153788";
     String vanityUrl = episodeUrl+"/glee-journey-to-regionals";
     HuluItemAdapter adapter = new HuluItemAdapter();
 
     public void testShouldRetrieveHuluItem() throws Exception {
         Episode item = (Episode) adapter.fetch(vanityUrl);
         assertNotNull(item);
 
         Brand brand = item.getBrand();
         assertNotNull(brand);
         assertEquals("Glee", brand.getTitle());
         assertEquals("http://www.hulu.com/glee", brand.getCanonicalUri());
         assertNotNull(brand.getDescription());
 
         assertEquals("Journey to Regionals", item.getTitle());
         assertEquals(episodeUrl, item.getCanonicalUri());
         assertFalse(item.getTags().isEmpty());
         assertNotNull(item.getDescription());
         assertNotNull(item.getThumbnail());
         assertNotNull(item.getImage());
         assertEquals(Integer.valueOf(1), item.getSeriesNumber());
         assertEquals(Integer.valueOf(22), item.getEpisodeNumber());
 
         Version version = item.getVersions().iterator().next();
         assertNotNull(version);
         assertTrue(version.getDuration() > 0);
 
         Encoding encoding = version.getManifestedAs().iterator().next();
         assertNotNull(encoding);
 
         assertEquals(2, encoding.getAvailableAt().size());
         boolean foundLink = false;
         boolean foundEmbed = false;
         for (Location location: encoding.getAvailableAt()) {
            if (location.getEmbedCode() != null) {
                 foundEmbed = true;
                 assertEmbedLocation(location);
             } else {
                 foundLink = true;
                 assertLinkLocation(location);
             }
         }
         assertTrue(foundLink);
         assertTrue(foundEmbed);
     }
     
     private void assertEmbedLocation(Location location) {
         assertNotNull(location);
        assertNotNull(location.getEmbedCode());
         assertEquals(TransportType.EMBED, location.getTransportType());
         assertEquals(true, location.getAvailable());
         assertEquals(Boolean.valueOf(true), location.getTransportIsLive());
         assertNotNull(location.getPolicy());
         assertFalse(location.getPolicy().getAvailableCountries().isEmpty());
         assertEquals(Countries.US, location.getPolicy().getAvailableCountries().iterator().next());
     }
     
     private void assertLinkLocation(Location location) {
         assertNotNull(location);
         assertNotNull(location.getUri()); 
         assertNull(location.getEmbedCode());
         assertEquals(TransportType.LINK, location.getTransportType());
         assertEquals(true, location.getAvailable());
         assertEquals(Boolean.valueOf(true), location.getTransportIsLive());
         assertNotNull(location.getPolicy());
         assertFalse(location.getPolicy().getAvailableCountries().isEmpty());
         assertEquals(Countries.US, location.getPolicy().getAvailableCountries().iterator().next());
     }
 
     public void testShouldBeAbleToFetch() throws Exception {
         assertTrue(adapter.canFetch("http://www.hulu.com/watch/152348/glee-funk"));
         assertTrue(adapter.canFetch("http://www.hulu.com/watch/152348"));
         assertFalse(adapter.canFetch("http://www.hulu.com/glee"));
     }
 
     public void testCanonicaliser() throws Exception {
         assertEquals("http://www.hulu.com/watch/152348", new HuluItemCanonicaliser().canonicalise("http://www.hulu.com/watch/152348/glee-funk"));
         assertEquals("http://www.hulu.com/watch/152348", new HuluItemCanonicaliser().canonicalise("http://www.hulu.com/watch/152348"));
         assertNull(new HuluItemCanonicaliser().canonicalise("http://www.hulu.com/glee"));
     }
 }
