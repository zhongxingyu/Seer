 package org.atlasapi.remotesite.channel4.pmlsd;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.atLeast;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy.Platform;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.testing.StubContentResolver;
 import org.atlasapi.remotesite.channel4.RecordingContentWriter;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.http.FixedResponseHttpClient;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 @RunWith(MockitoJUnitRunner.class)
 public class C4BrandExtractorTest extends TestCase {
     
 
 	private final SimpleHttpClient httpClient = new FixedResponseHttpClient(
 	    ImmutableMap.<String, String>builder()
 	    .put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", fileContentsFromResource("ramsays-kitchen-nightmares.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/4od.atom", fileContentsFromResource("ramsays-kitchen-nightmares-4od.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/video.atom", fileContentsFromResource("ugly-betty-video.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", fileContentsFromResource("ramsays-kitchen-nightmares-episode-guide.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-1.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-1.atom"))
         .put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-2.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-2.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-3.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-4.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-4.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-5.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-5.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/epg.atom", fileContentsFromResource("ramsays-kitchen-nightmares-epg.atom"))
 		.put("https://pmlsc.channel4.com/pmlsd/dispatches.atom", fileContentsFromResource("dispatches.atom"))
         .put("https://pmlsc.channel4.com/pmlsd/dispatches/4od.atom", fileContentsFromResource("dispatches-4od.atom"))
         .put("https://pmlsc.channel4.com/pmlsd/dispatches/episode-guide.atom", fileContentsFromResource("dispatches-episode-guide.atom"))
         .put("https://pmlsc.channel4.com/pmlsd/dispatches/episode-guide/series-6.atom", fileContentsFromResource("dispatches-series-6.atom"))
 		.build());
 		
 	private final C4AtomApiClient atomApiClient = new C4AtomApiClient(httpClient, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
 	
 	private final ContentWriter writer = mock(ContentWriter.class);
 	private final ContentResolver resolver = mock(ContentResolver.class);
 	private final ChannelResolver channelResolver = new C4DummyChannelResolver();
 	
 	private C4BrandExtractor pcExtractor;
 	private C4AtomBackedBrandUpdater pcUpdater;
 	
 	private String fileContentsFromResource(String resourceName)  {
 	    try {
 	        return Resources.toString(Resources.getResource(getClass(), resourceName), Charsets.UTF_8);
         } catch (IOException e) {
             Throwables.propagate(e);
         }
 	    return null;
 	}
 	
 	@Before
 	public void setUp() {
 		pcExtractor = new C4BrandExtractor(atomApiClient, Optional.<Platform> absent(), channelResolver);
 		pcUpdater = new C4AtomBackedBrandUpdater(atomApiClient, Optional.<Platform> absent(), resolver, writer, pcExtractor);
 	}
 
     @Test
 	public void testExtractingABrand() throws Exception {
         
         when(resolver.findByCanonicalUris(anyUris())).thenReturn(ResolvedContent.builder().build());
 		
 		pcUpdater.createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
 
 		ArgumentCaptor<Container> containerCapturer = ArgumentCaptor.forClass(Container.class);
 		verify(writer, atLeast(1)).createOrUpdate(containerCapturer.capture());
 		Map<String, Container> containers = Maps.uniqueIndex(containerCapturer.getAllValues(), Identified.TO_URI);
 		
 		assertThat(containers.keySet(), hasItem("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares"));
 
 		ArgumentCaptor<Item> itemCapturer = ArgumentCaptor.forClass(Item.class);
 		verify(writer, atLeast(1)).createOrUpdate(itemCapturer.capture());
 		
 		Map<String, Item> items = Maps.uniqueIndex(itemCapturer.getAllValues(), Identified.TO_URI);
 		
 		Item firstItem = items.get("http://pmlsc.channel4.com/pmlsd/36423/001");
 		
 		assertThat(firstItem.getCanonicalUri(), is("http://pmlsc.channel4.com/pmlsd/36423/001"));
 
 		// TODO new alias
        assertThat(firstItem.getAliasUrls().size(), is(2));
 		assertThat(firstItem.getAliasUrls(), is((Set<String>) ImmutableSet.of(
 	        "http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045", 
	        "http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-1/episode-1"
         )));
 		
 		assertThat(firstItem.getTitle(), is(("Series 1 Episode 1")));
 		
 		Version firstItemVersion = Iterables.getOnlyElement(firstItem.getVersions());
 		
 		assertThat(firstItemVersion.getDuration(), is(2935));
 
 		Encoding firstItemEncoding = Iterables.getOnlyElement(firstItemVersion.getManifestedAs());
 		Location firstItemLocation = Iterables.getOnlyElement(firstItemEncoding.getAvailableAt());
 		assertThat(firstItemLocation.getUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045"));
 		
 		Item episodeNotOn4od = items.get("http://pmlsc.channel4.com/pmlsd/41337/005");
 		assertThat(episodeNotOn4od.getVersions().size(), is(0));
 	}
 
     @Test
 	public void testThatBroadcastIsExtractedFromEpg() throws Exception {
         when(resolver.findByCanonicalUris(anyUris())).thenReturn(ResolvedContent.builder().build());
 		
 		pcUpdater.createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
 	    
 	    ArgumentCaptor<Item> itemCapturer = ArgumentCaptor.forClass(Item.class);
         verify(writer, atLeast(1)).createOrUpdate(itemCapturer.capture());
 	    
 	    boolean found = false;
 	    for (Item item : itemCapturer.getAllValues()) {
 	        if (item.getCanonicalUri().equals("http://pmlsc.channel4.com/pmlsd/43065/005")) {
 	            assertFalse(item.getVersions().isEmpty());
 	            Version version = item.getVersions().iterator().next();
 	            
 	            assertEquals(1, version.getBroadcasts().size());
 	            for (Broadcast broadcast: version.getBroadcasts()) {
 	                if (broadcast.getBroadcastDuration() == 60*55) {
 	                    assertTrue(broadcast.getAliasUrls().contains("tag:www.channel4.com,2009:slot/E439861"));
 	                    assertThat(broadcast.getSourceId(), is("e4:39861"));
 	                    found = true;
 	                }
 	            }
 	        }
 	    }
 	    
 	    assertTrue(found);
 	}
 
     @SuppressWarnings("unchecked")
     private Iterable<String> anyUris() {
         return (Iterable<String>)any();
     }
 
     @Test
 	public void testOldEpisodeWithBroadcast() throws Exception {
         when(resolver.findByCanonicalUris(anyUris())).thenReturn(ResolvedContent.builder().build());
 		
 	    Episode episode = C4PmlsdModule.contentFactory().createEpisode();
 	    episode.setCanonicalUri("http://pmlsc.channel4.com/pmlsd/43065/005");
 	    Version version = new Version();
 	    episode.addVersion(version);
 	    Broadcast oldBroadcast = new Broadcast("some channel", new DateTime(), new DateTime());
 	    // TODO new alias
 	    oldBroadcast.addAliasUrl("tag:www.channel4.com:someid");
 	    version.addBroadcast(oldBroadcast);
 	    
 	    when(resolver.findByCanonicalUris(argThat(hasItem(episode.getCanonicalUri()))))
 	        .thenReturn(ResolvedContent.builder().put(episode.getCanonicalUri(), episode).build());
 	    
 	    pcUpdater.createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
         
 	    ArgumentCaptor<Item> itemCapturer = ArgumentCaptor.forClass(Item.class);
 	    verify(writer, atLeast(1)).createOrUpdate(itemCapturer.capture());
 	    
         boolean found = false;
         boolean foundOld = false;
         for (Item item: itemCapturer.getAllValues()) {
             if (item.getCanonicalUri().equals("http://pmlsc.channel4.com/pmlsd/43065/005")) {
                 assertFalse(item.getVersions().isEmpty());
                 version = item.getVersions().iterator().next();
 
                 assertEquals(2, version.getBroadcasts().size());
                 for (Broadcast broadcast: version.getBroadcasts()) {
                     if (broadcast.getBroadcastDuration() == 60*55) {
                         assertTrue(broadcast.getAliasUrls().contains("tag:www.channel4.com,2009:slot/E439861"));
                         assertThat(broadcast.getSourceId(), is("e4:39861"));
                         found = true;
                     } else if (broadcast.getAliasUrls().contains("tag:www.channel4.com:someid")) {
                         foundOld = true;
                     }
                 }
             }
         }
         
         assertTrue(found);
         assertTrue(foundOld);
 	}
 
     /* The API no longer exhibits this behavior; leaving these tests here for a record of the behaviour temporarily
       but they can be removed in July 2012
      
      
     @Test
 	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries1IsAssumed() throws Exception {
 		
 	    SimpleHttpClient client = new FixedResponseHttpClient(
 	        ImmutableMap.<String, String>builder()	
 			.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", fileContentsFromResource("ramsays-kitchen-nightmares.atom"))
 			.put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-1.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-1.atom"))
 			.build(),
 			ImmutableMap.<String, Integer>of("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", 403));
 
 	    C4AtomApiClient apiClient = new C4AtomApiClient(client, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		new C4AtomBackedBrandUpdater(apiClient, Optional.<Platform>absent(), contentResolver, recordingWriter, channelResolver).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
 		assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
 	}
 
     @Test
 	public void testThatWhenTheEpisodeGuide404sSeries1IsAssumed() throws Exception {
         SimpleHttpClient client = new FixedResponseHttpClient(
                 ImmutableMap.<String, String>builder()  
                 .put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", fileContentsFromResource("ramsays-kitchen-nightmares.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-1.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-1.atom"))
                 .build(),
                 ImmutableMap.<String, Integer>of("https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", 404));
 
         C4AtomApiClient apiClient = new C4AtomApiClient(client, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
 
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
 		new C4AtomBackedBrandUpdater(apiClient, Optional.<Platform>absent(), contentResolver, recordingWriter, channelResolver).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
 		assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
 	}
 
     @Test
 	public void testThatWhenTheEpisodeGuideReturnsABadStatusCodeSeries3IsReturned() throws Exception {
 	    HttpResponsePrologue response = new HttpResponsePrologue(403, "error").withFinalUrl("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom");
         RemoteSiteClient<Feed> feedClient = new StubC4AtomClient()
             .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", rknBrandFeed.build())
             .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", new HttpStatusCodeException(response, "403"))
             .respondTo("http://api.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom", rknSeries4Feed.build());
 
 		RecordingContentWriter recordingWriter = new RecordingContentWriter();
         new C4AtomBackedBrandUpdater(feedClient, Optional.<Platform>absent(), contentResolver, recordingWriter, channelResolver).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
         assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
     }
     
     @Test
     public void testThatWhenTheEpisodeGuideRedirectsToSeries1TheSeriesIsRead() throws Exception {
         
         SimpleHttpClient client = new FixedResponseHttpClient(
                 ImmutableMap.<String,String>of(
                         "https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", fileContentsFromResource("ramsays-kitchen-nightmares.atom"), 
                         "https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-3.atom")));
 
         C4AtomApiClient apiClient = new C4AtomApiClient(client, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
 
         RecordingContentWriter recordingWriter = new RecordingContentWriter();
         new C4AtomBackedBrandUpdater(apiClient, Optional.<Platform>absent(), contentResolver, recordingWriter, channelResolver).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
         assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
     }
     
     @Test
     public void testThatWhenTheEpisodeGuideRedirectsToAnEpisodeFeedTheSeriesIsFetched() throws Exception {
        
         Feed episodeFeed = new Feed();
         episodeFeed.setFeedType("atom_1.0");
         episodeFeed.setId("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-3/episode-5");
         WireFeedOutput output = new WireFeedOutput();
         StringWriter os = new StringWriter();
         output.output(episodeFeed, os);
         String redirectingFeed = os.getBuffer().toString();
         
         SimpleHttpClient client = new FixedResponseHttpClient(
                 ImmutableMap.<String,String>of(
                         "https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares.atom", fileContentsFromResource("ramsays-kitchen-nightmares.atom"), 
                         "https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide.atom", redirectingFeed,
                         "https://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3.atom", fileContentsFromResource("ramsays-kitchen-nightmares-series-3.atom")));
         
         C4AtomApiClient apiClient = new C4AtomApiClient(client, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
 
         RecordingContentWriter recordingWriter = new RecordingContentWriter();
         new C4AtomBackedBrandUpdater(apiClient, Optional.<Platform>absent(), contentResolver, recordingWriter, channelResolver).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
         assertThat(recordingWriter.updatedItems.size(), is(greaterThan(1)));
     }
 
     */
 	
     @Test
 	public void testFlattenedBrandsItemsAreNotPutIntoSeries() throws Exception {
         when(resolver.findByCanonicalUris(anyUris())).thenReturn(ResolvedContent.builder().build());
 
         pcUpdater.createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/dispatches");
 
         ArgumentCaptor<Item> itemCapturer = ArgumentCaptor.forClass(Item.class);
         verify(writer, atLeast(1)).createOrUpdate(itemCapturer.capture());
 
         Map<String, Item> items = Maps.uniqueIndex(itemCapturer.getAllValues(), Identified.TO_URI);
 
         Item item = items.get("http://pmlsc.channel4.com/pmlsd/47560/001");
         assertThat(item.getVersions().size(), is(1));
     }
 
  
     @Test
 	public void testThatClipsAreAddedToBrands() throws Exception {
         when(resolver.findByCanonicalUris(anyUris())).thenReturn(ResolvedContent.builder().build());
 
         C4AtomApiClient apiClient = new C4AtomApiClient(httpClient, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
         
 		C4BrandExtractor extractor = new C4BrandExtractor(atomApiClient, Optional.<Platform>absent(), channelResolver);
 		new C4AtomBackedBrandUpdater(apiClient, Optional.<Platform>absent(), resolver, writer, extractor).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares");
 		
 		ArgumentCaptor<Container> containerCapturer = ArgumentCaptor.forClass(Container.class);
         verify(writer, atLeast(1)).createOrUpdate(containerCapturer.capture());
         
         ArgumentCaptor<Item> itemCapturer = ArgumentCaptor.forClass(Item.class);
         verify(writer, atLeast(1)).createOrUpdate(itemCapturer.capture());
 		
         int clipsCount = 0;
         for (Content content : Iterables.concat(containerCapturer.getAllValues(), itemCapturer.getAllValues())) {
             clipsCount += content.getClips().size();
         }
         assertThat(clipsCount, is(8));
 	}
     
     @Test
     public void testPlatformLocation() {
         when(resolver.findByCanonicalUris(anyUris())).thenReturn(ResolvedContent.builder().build());
         
         SimpleHttpClient client = new FixedResponseHttpClient(
             ImmutableMap.<String, String>builder()  
             .put("https://pmlsc.channel4.com/pmlsd/jamie-does.atom?platform=xbox", fileContentsFromResource("jamie-does-xbox.atom"))
             .put("https://pmlsc.channel4.com/pmlsd/jamie-does/4od.atom?platform=xbox", fileContentsFromResource("jamie-does-4od-xbox.atom"))
             .put("https://pmlsc.channel4.com/pmlsd/jamie-does/epg.atom?platform=xbox", fileContentsFromResource("jamie-does-epg-xbox.atom"))
             .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide.atom?platform=xbox", fileContentsFromResource("jamie-does-episode-guide-xbox.atom"))
             .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide/series-1.atom?platform=xbox", fileContentsFromResource("jamie-does-series-1-xbox.atom"))
             .build()
         );
 
         C4AtomApiClient apiClient = new C4AtomApiClient(client, "https://pmlsc.channel4.com/pmlsd/", Optional.of("xbox"));
 
         RecordingContentWriter recordingWriter = new RecordingContentWriter();
         C4BrandExtractor extractor = new C4BrandExtractor(apiClient, Optional.of(Platform.XBOX), channelResolver);
         new C4AtomBackedBrandUpdater(apiClient, Optional.of(Platform.XBOX), resolver, recordingWriter, extractor).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/jamie-does");
         
         Item item = findLast("http://pmlsc.channel4.com/pmlsd/48367/006", recordingWriter.updatedItems);
         Episode episode = (Episode) item;
         
         Version version = Iterables.getOnlyElement(episode.getVersions());
         Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
         Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
         assertThat(location.getPolicy().getPlatform(), is(Platform.XBOX));
         assertThat(location.getUri(), is("https://ais.channel4.com/asset/3262609"));
     }
     
     @Test 
     public void testMultipleLocationsOnDifferentPlatforms() {
         SimpleHttpClient xboxClient = new FixedResponseHttpClient(
                 ImmutableMap.<String, String>builder()  
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does.atom?platform=xbox", fileContentsFromResource("jamie-does-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/4od.atom?platform=xbox", fileContentsFromResource("jamie-does-4od-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/epg.atom?platform=xbox", fileContentsFromResource("jamie-does-epg-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide.atom?platform=xbox", fileContentsFromResource("jamie-does-episode-guide-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide/series-1.atom?platform=xbox", fileContentsFromResource("jamie-does-series-1-xbox.atom"))
                 .build());
         
         SimpleHttpClient client = new FixedResponseHttpClient(
                 ImmutableMap.<String, String>builder()  
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does.atom", fileContentsFromResource("jamie-does.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/4od.atom", fileContentsFromResource("jamie-does-4od.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/epg.atom", fileContentsFromResource("jamie-does-epg.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide.atom", fileContentsFromResource("jamie-does-episode-guide.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide/series-1.atom", fileContentsFromResource("jamie-does-series-1.atom"))
                 .build());
 
         C4AtomApiClient xboxApiClient = new C4AtomApiClient(xboxClient, "https://pmlsc.channel4.com/pmlsd/", Optional.of("xbox"));
         C4AtomApiClient apiClient = new C4AtomApiClient(client, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
 
         RecordingContentWriter recordingWriter = new RecordingContentWriter();
         C4BrandExtractor extractor = new C4BrandExtractor(xboxApiClient, Optional.of(Platform.XBOX), channelResolver);
         StubContentResolver stubResolver = new StubContentResolver();
         new C4AtomBackedBrandUpdater(xboxApiClient, Optional.of(Platform.XBOX), stubResolver, recordingWriter, extractor)
             .createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/jamie-does");
         
         stubResolver.respondTo(findLast("http://pmlsc.channel4.com/pmlsd/48367/006", recordingWriter.updatedItems));
         
         extractor = new C4BrandExtractor(apiClient, Optional.<Platform>absent(), channelResolver);
         new C4AtomBackedBrandUpdater(apiClient, Optional.<Platform>absent(), stubResolver, recordingWriter, extractor)
             .createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/jamie-does");
         
         
         Item item = findLast("http://pmlsc.channel4.com/pmlsd/48367/006", recordingWriter.updatedItems);
         Episode episode = (Episode) item;
         
         Version version = Iterables.getOnlyElement(episode.getVersions());
         Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
         Set<Location> locations = encoding.getAvailableAt();
 
         assertThat(locations.size(), is(2));
         
         boolean foundPCLocation = false;
         boolean foundXboxLocation = false;
         
         for(Location location: locations) {
             if(location.getUri().equals("https://ais.channel4.com/asset/3262609")) {
                 foundXboxLocation = true;
                 assertThat(location.getPolicy().getPlatform(), is(Platform.XBOX));
             } else if(location.getUri().equals("http://www.channel4.com/programmes/jamie-does/4od#3073178")) {
                 foundPCLocation = true;
                 assertNull(location.getPolicy().getPlatform());
             }
             else {
                 throw new IllegalStateException("Unexpected location");
             }
         }
         assertTrue(foundPCLocation);
         assertTrue(foundXboxLocation);
       
     }
 
     @Test 
     /**
      * The description may differ on a platform-by-platform basis. Therefore 
      * we only ingest the description from a platform feed if we don't have
      * a description already.
      */
     public void testPlatformDoesNotUpdateDescription() {
         SimpleHttpClient xboxClient = new FixedResponseHttpClient(
                 ImmutableMap.<String, String>builder()  
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does.atom?platform=xbox", fileContentsFromResource("jamie-does-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/4od.atom?platform=xbox", fileContentsFromResource("jamie-does-4od-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/epg.atom?platform=xbox", fileContentsFromResource("jamie-does-epg-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide.atom?platform=xbox", fileContentsFromResource("jamie-does-episode-guide-xbox.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide/series-1.atom?platform=xbox", fileContentsFromResource("jamie-does-series-1-xbox-dummy-description.atom"))
                 .build());
         
         SimpleHttpClient client = new FixedResponseHttpClient(
                 ImmutableMap.<String, String>builder()
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does.atom", fileContentsFromResource("jamie-does.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/4od.atom", fileContentsFromResource("jamie-does-4od.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/epg.atom", fileContentsFromResource("jamie-does-epg.atom"))             
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide.atom", fileContentsFromResource("jamie-does-episode-guide.atom"))
                 .put("https://pmlsc.channel4.com/pmlsd/jamie-does/episode-guide/series-1.atom", fileContentsFromResource("jamie-does-series-1.atom"))
                 .build());
 
         C4AtomApiClient apiClient = new C4AtomApiClient(client, "https://pmlsc.channel4.com/pmlsd/", Optional.<String>absent());
         C4AtomApiClient xboxApiClient = new C4AtomApiClient(xboxClient, "https://pmlsc.channel4.com/pmlsd/", Optional.of("xbox"));
 
         RecordingContentWriter recordingWriter = new RecordingContentWriter();
         C4BrandExtractor extractor = new C4BrandExtractor(apiClient, Optional.<Platform>absent(), channelResolver);
         StubContentResolver stubResolver = new StubContentResolver();
         new C4AtomBackedBrandUpdater(apiClient, Optional.<Platform>absent(), stubResolver, recordingWriter, extractor).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/jamie-does");
 
         stubResolver.respondTo(findLast("http://pmlsc.channel4.com/pmlsd/48367/001", recordingWriter.updatedItems));
 
         extractor = new C4BrandExtractor(xboxApiClient, Optional.of(Platform.XBOX), channelResolver);
         new C4AtomBackedBrandUpdater(xboxApiClient, Optional.of(Platform.XBOX), stubResolver, recordingWriter, extractor).createOrUpdateBrand("http://pmlsc.channel4.com/pmlsd/jamie-does");
         
         Item item = findLast("http://pmlsc.channel4.com/pmlsd/48367/001", recordingWriter.updatedItems);
         Episode episode = (Episode) item;
          
         assertThat(episode.getDescription(), is("Jamie's in Morocco, dodging snake charmers to try out the street food of Marrakesh, like slow-roasted lamb in cumin, and almond and rose water cakes. Later he joins a family for some Moroccan home cooking, and makes his own versions of chicken and lemon tagine, Moroccan roast lamb, and a `snakey cake' made of filo pastry, almonds and rose petals."));
     }
 	
     private <C extends Content> C findLast(String uri, List<C> content) {
         for(int i = content.size()-1; i >= 0; i--) {
             C c = content.get(i);
             if (c.getCanonicalUri().equals(uri)) {
                 return c;
             }
         }
         throw new IllegalStateException("Not found");
     }
 }
