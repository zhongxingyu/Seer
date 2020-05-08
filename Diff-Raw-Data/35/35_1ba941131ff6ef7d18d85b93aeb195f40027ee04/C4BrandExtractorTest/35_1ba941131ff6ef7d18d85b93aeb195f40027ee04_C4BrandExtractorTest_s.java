 package org.atlasapi.remotesite.channel4;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.is;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.StubContentResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.HttpResponse;
 import com.metabroadcast.common.http.HttpStatusCodeException;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.sun.syndication.feed.atom.Feed;
 
 public class C4BrandExtractorTest extends TestCase {
 
 	private final AtomFeedBuilder rknSeries3Feed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-series-3.atom"));
 	private final AtomFeedBuilder rknSeries4Feed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-series-4.atom"));
 	private final AtomFeedBuilder rknBrandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares.atom"));
 	private final AtomFeedBuilder rknEpsiodeGuideFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-episode-guide.atom"));
 	private final AtomFeedBuilder rknFourOdFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-4od.atom"));
 	private final AtomFeedBuilder rknEpgFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-epg.atom"));
 	private final AtomFeedBuilder uglyBettyClipFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ugly-betty-video.atom"));
 
 	private final AtomFeedBuilder dispatchesBrandFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "dispatches.atom"));
 	private final AtomFeedBuilder dispatchesEpisodeGuideFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "dispatches-episode-guide.atom"));
 
 	
 	private final RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/4od.atom", rknFourOdFeed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", rknEpsiodeGuideFeed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries3Feed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4.atom", rknSeries4Feed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/epg.atom", rknEpgFeed.build());
 	
 	private final static AdapterLog nullLog = new NullAdapterLog();
 	private final StubContentResolver contentResolver = new StubContentResolver();
 	
 	public void testExtractingABrand() throws Exception {
 		Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		
 		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));
 
 		Item firstItem = brand.getItems().get(0);
 		
 		assertThat(firstItem.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1"));
 
 		assertThat(firstItem.getAliases(), is((Set<String>) ImmutableSet.of("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2921983", "tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1")));
 		
 		Version firstItemVersion = Iterables.getOnlyElement(firstItem.getVersions());
 		
 		assertThat(firstItemVersion.getDuration(), is(2949));
 
 		Encoding firstItemEncoding = Iterables.getOnlyElement(firstItemVersion.getManifestedAs());
 		Location firstItemLocation = Iterables.getOnlyElement(firstItemEncoding.getAvailableAt());
 		assertThat(firstItemLocation.getUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2921983"));
 		
 		Episode episodeNotOn4od = (Episode) find("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-5", brand.getItems());
 		assertThat(episodeNotOn4od.getVersions().size(), is(0));
 	}
 	
 	public void testThatBroadcastIsExtractedFromEpg() throws Exception {
 	    Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 	    
 	    boolean found = false;
 	    for (Item item: brand.getItems()) {
 	        if (item.getCanonicalUri().equals("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4/episode-5")) {
 	            assertFalse(item.getVersions().isEmpty());
 	            Version version = item.getVersions().iterator().next();
 	            
 	            assertEquals(2, version.getBroadcasts().size());
 	            for (Broadcast broadcast: version.getBroadcasts()) {
 	                if (broadcast.getBroadcastDuration() == 60*55) {
 	                    assertTrue(broadcast.getAliases().contains("tag:www.channel4.com,2009:slot/39861"));
 	                    assertEquals(new DateTime("2010-08-11T14:06:33.341Z", DateTimeZones.UTC), broadcast.getLastUpdated());
 	                    found = true;
 	                } else {
	                    assertEquals(new DateTime("2010-04-27T09:49:40.803Z", DateTimeZones.UTC), broadcast.getLastUpdated());
 	                    assertTrue(broadcast.getAliases().isEmpty());
 	                }
 	            }
 	        }
 	    }
 	    
 	    assertTrue(found);
 	}
 	
 	public void testOldEpisodeWithBroadcast() throws Exception {
 	    Episode episode = new Episode("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4/episode-5", "c4:ramsays-kitchen-nightmares_series-4_episode-5", Publisher.C4);
 	    Version version = new Version();
 	    episode.addVersion(version);
 	    Broadcast oldBroadcast = new Broadcast("some channel", new DateTime(), new DateTime());
 	    oldBroadcast.addAlias("tag:www.channel4.com:someid");
 	    version.addBroadcast(oldBroadcast);
 	    contentResolver.respondTo(episode);
 	    
 	    Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
         
         boolean found = false;
         boolean foundOld = false;
         for (Item item: brand.getItems()) {
             if (item.getCanonicalUri().equals("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-4/episode-5")) {
                 assertFalse(item.getVersions().isEmpty());
                 version = item.getVersions().iterator().next();
                 
                 assertEquals(3, version.getBroadcasts().size());
                 for (Broadcast broadcast: version.getBroadcasts()) {
                     if (broadcast.getBroadcastDuration() == 60*55) {
                         assertTrue(broadcast.getAliases().contains("tag:www.channel4.com,2009:slot/39861"));
                         assertEquals(new DateTime("2010-08-11T14:06:33.341Z", DateTimeZones.UTC), broadcast.getLastUpdated());
                         found = true;
                     } else if (broadcast.getAliases().contains("tag:www.channel4.com:someid")) {
                         assertFalse(broadcast.isActivelyPublished());
                         foundOld = true;
                     } else {
                        assertEquals(new DateTime("2010-04-27T09:49:40.803Z", DateTimeZones.UTC), broadcast.getLastUpdated());
                         assertTrue(broadcast.getAliases().isEmpty());
                     }
                 }
             }
         }
         
         assertTrue(found);
         assertTrue(foundOld);
 	}
 	
 	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries1IsAssumed() throws Exception {
 	    HttpResponse response = new HttpResponse("error", 403).withFinalUrl("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom");
 		
 	    RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response))
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-1.atom", rknSeries3Feed.build());
 
 		Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		assertThat(brand.getItems().size(), is(greaterThan(1)));
 	}
 	
 	public void testThatWhenTheEpisodeGuide404sSeries1IsAssumed() throws Exception {
 	   HttpResponse response = new HttpResponse("error", 404);
 		
 	    RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response))
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-1.atom", rknSeries3Feed.build());
 
 		Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		assertThat(brand.getItems().size(), is(greaterThan(1)));
 	}
 	
 	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries3IsReturned() throws Exception {
 	    HttpResponse response = new HttpResponse("error", 403).withFinalUrl("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom");
         RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
             .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
             .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response))
             .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries4Feed.build());
 
         Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
         assertThat(brand.getItems().size(), is(greaterThan(1)));
     }
 	
 	
 	public void testFlattenedBrandsItemsAreNotPutIntoSeries() throws Exception {
 		 RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
          .respondTo("http://api.channel4.com/programmes/dispatches.atom", dispatchesBrandFeed.build())
          .respondTo("http://api.channel4.com/programmes/dispatches/episode-guide.atom", dispatchesEpisodeGuideFeed.build());
 		 
 	     Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/dispatches");
 	     assertThat(brand.getItems().size(), is(greaterThan(1)));
 	     
 	     for (Item item : brand.getItems()) {
 			assertThat(item.getVersions().size(), is(0));
 		}
 	}
 	
 	public void testThatWhenTheEpisodeGuideRedirectsToAnEpisodeFeedTheSeriesIsFetched() throws Exception {
 	   
 		Feed episodeFeed = new Feed();
 		episodeFeed.setId("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-5");
 		 
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
            .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
            .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", episodeFeed)
            .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries3Feed.build());
 
        Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
        assertThat(brand.getItems().size(), is(greaterThan(1)));
 	}
 	
 	public void testThatWhenTheEpisodeGuideRedirectsToSeries1TheSeriesIsRead() throws Exception {
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 			.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", rknSeries3Feed.build());
 
 		Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		assertThat(brand.getItems().size(), is(greaterThan(1)));
 	}
 
 	public void testThatClipsAreAddedToBrands() throws Exception {
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", rknSeries3Feed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/video.atom", uglyBettyClipFeed.build());
 		
 		Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 		assertThat(brand.getClips().size(), is(greaterThan(1)));
 	}
 	
 	public void testThatOldLocationsAndBroadcastsAreCopied() {
 		RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
 		.respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide.atom", rknSeries3Feed.build())
         .respondTo("http://api.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries3Feed.build());
 
 		
 		Episode series3Ep1 = new Episode("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-1", "curie", Publisher.C4);
 		
 		Version c4Version = new Version();
 		c4Version.setCanonicalUri("v1");
 
 		// this version shouldn't be merged because it's not from C4
 		Version otherPublisherVersion = new Version();
 		otherPublisherVersion.setProvider(Publisher.YOUTUBE);
 		otherPublisherVersion.setCanonicalUri("v2");
 
 		series3Ep1.addVersion(c4Version);
 		series3Ep1.addVersion(otherPublisherVersion);
 		
 		contentResolver.respondTo(series3Ep1);
 		
 		Brand brand = new C4AtomBackedBrandAdapter(feedClient, contentResolver, nullLog).fetch("http://www.channel4.com/programmes/ramsays-kitchen-nightmares");
 	
 		Item series3Ep1Parsed = Iterables.get(brand.getItems(), 0);
 		
 		assertTrue(Iterables.getOnlyElement(series3Ep1Parsed.getVersions()) == c4Version);
 	}
 	
 	private static class StubC4AtomClient implements RemoteSiteClient<Feed> {
 
 		private Map<String, Object> respondsTo = Maps.newHashMap();
 
 		@Override
 		public Feed get(String uri) throws Exception {
 			// Remove API key
 			uri = removeQueryString(uri);
 			Object response = respondsTo.get(uri);
 			if (response == null) {
 				throw new HttpStatusCodeException(404, "Not found: " + uri);
 			} else if (response instanceof HttpException) {
 			    throw (HttpException) response;
 			}
 			return (Feed) response;
 		}
 
 		private String removeQueryString(String url) throws MalformedURLException {
 			String queryString = "?" + new URL(url).getQuery();
 			return url.replace(queryString, "");
 		}
 		
 		StubC4AtomClient respondTo(String url, Feed feed) {
 			respondsTo.put(url, feed);
 			return this;
 		}
 		
 		StubC4AtomClient respondTo(String url, HttpException exception) {
 		    respondsTo.put(url, exception);
 		    return this;
 		}
 	}
 	
 	private final <T extends Content> T find(String uri, Iterable<T> episodes) {
 		for (T episode : episodes) {
 			if (episode.getCanonicalUri().equals(uri)) {
 				return episode;
 			}
 		}
 		throw new IllegalStateException("Not found");
 	}
 }
