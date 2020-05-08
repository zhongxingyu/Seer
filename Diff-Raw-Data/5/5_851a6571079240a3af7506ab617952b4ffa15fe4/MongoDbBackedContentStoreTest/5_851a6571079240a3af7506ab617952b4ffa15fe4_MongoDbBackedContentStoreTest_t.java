 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.persistence.content.mongo;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.ContentGroup;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.testing.DummyContentData;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.time.TimeMachine;
 
 public class MongoDbBackedContentStoreTest extends TestCase {
 	
 	private final TimeMachine clock = new TimeMachine();
 	
 	private MongoDbBackedContentStore store;
 	private DummyContentData data ;
     
     @Override
     protected void setUp() throws Exception {
     	super.setUp();
     	this.store = new MongoDbBackedContentStore(MongoTestHelper.anEmptyTestDatabase(), clock);
     	data = new DummyContentData();
     }
     
     public void testSavesAliasesForItems() throws Exception {
     	
     	Item item = new Item("item", "item", Publisher.YOUTUBE);
     	
     	item.setAliases(ImmutableSet.of("c"));
     	
         store.createOrUpdate(item);
         
         store.addAliases(item.getCanonicalUri(), ImmutableSet.of("a", "b"));
         
         assertEquals(ImmutableSet.of("a", "b", "c"), store.findByCanonicalUri(item.getCanonicalUri()).getAliases()); 
 	}
     
     public void testThatWhenFindingByUriContentThatIsACanonicalUriMatchIsUsed() throws Exception {
         
     	Item a = new Item("a", "curie:a", Publisher.BBC);
     	a.addAlias("b");
     	Item b = new Item("b", "curie:b", Publisher.C4);
     	b.addAlias("a");
     	
     	store.createOrUpdate(a);
     	store.createOrUpdate(b);
     	
     	assertEquals("a", store.findByCanonicalUri("a").getCanonicalUri());
     	assertEquals("b", store.findByCanonicalUri("b").getCanonicalUri());
 	}
     
     public void testSavesAliasesForPlaylists() throws Exception {
         store.createOrUpdate(data.eastenders, true);
         store.addAliases(data.eastenders.getCanonicalUri(), ImmutableSet.of("a", "b"));
         assertTrue(store.findByCanonicalUri(data.eastenders.getCanonicalUri()).getAliases().containsAll(ImmutableSet.of("a", "b"))); 
     }
 
     public void testShouldCreateAndRetrieveItem() throws Exception {
         store.createOrUpdate(data.eggsForBreakfast);
         store.createOrUpdate(data.englishForCats);
 
         List<Identified> items = store.findByCanonicalUri(ImmutableList.of(data.eggsForBreakfast.getCanonicalUri()));
         assertNotNull(items);
         assertEquals(1, items.size());
         Item item = (Item) items.get(0);
 		assertEquals(data.eggsForBreakfast.getTitle(), item.getTitle());
         assertNotNull(item.getLastFetched());
 
         items = store.findByCanonicalUri(ImmutableList.of(data.eggsForBreakfast.getCanonicalUri(), data.englishForCats.getCanonicalUri()));
         assertEquals(2, items.size());
 
         store.createOrUpdate(data.eggsForBreakfast);
 
         items = store.findByCanonicalUri(ImmutableList.of(data.eggsForBreakfast.getCanonicalUri()));
         assertNotNull(items);
         assertEquals(1, items.size());
         assertEquals(data.eggsForBreakfast.getTitle(), ((Item) items.get(0)).getTitle());
         assertEquals(data.eggsForBreakfast.getCurie(), items.get(0).getCurie());
         
         
         Set<String> aliases = items.get(0).getAliases();
         for (String alias: aliases) {
             assertNotSame(items.get(0).getCanonicalUri(), alias);
         }
     }
     
     public void testEpisodesAreAddedToBrands() throws Exception {
        
     	store.createOrUpdate(data.eastenders, false);
 
         Episode nextWeeksEastenders = new Episode("next-week", "bbc:next-week", Publisher.BBC);
         nextWeeksEastenders.setContainer(new Brand(data.eastenders.getCanonicalUri(), "wrong curie", Publisher.BBC));
        
         store.createOrUpdate(nextWeeksEastenders);
         
         Brand foundBrand = (Brand) store.findByCanonicalUri(data.eastenders.getCanonicalUri());
 
         assertEquals(data.eastenders.getCurie(), foundBrand.getCurie());
 
         assertEquals(data.eastenders.getContents().size() + 1, foundBrand.getContents().size());
         assertTrue(foundBrand.getContents().contains(nextWeeksEastenders)); 
 	}
 
     public void testShouldCreateAndRetrievePlaylist() throws Exception {
         store.createOrUpdate(data.eastenders, true);
     	store.createOrUpdateSkeleton(data.goodEastendersEpisodes);
 
         List<String> itemUris = Lists.newArrayList();
         for (Content item : data.goodEastendersEpisodes.getContents()) {
             itemUris.add(item.getCanonicalUri());
         }
         List<Identified> items = store.findByCanonicalUri(itemUris);
         assertNotNull(items);
         assertEquals(1, items.size());
         assertEquals(data.dotCottonsBigAdventure.getTitle(), ((Item) items.get(0)).getTitle());
 
         store.createOrUpdateSkeleton(data.goodEastendersEpisodes);
 
         List<Identified> playlists = store.findByCanonicalUri(ImmutableList.of(data.goodEastendersEpisodes.getCanonicalUri()));
         assertNotNull(playlists);
         assertEquals(1, playlists.size());
         
         ContentGroup groupFound = (ContentGroup) playlists.get(0);
         
 		assertEquals(data.goodEastendersEpisodes.getTitle(), groupFound.getTitle());
         assertEquals(data.goodEastendersEpisodes.getCurie(), groupFound.getCurie());
         assertNotNull(groupFound.getLastFetched());
         
         Set<String> aliases = groupFound.getAliases();
         for (String alias: aliases) {
             assertNotSame(groupFound.getCanonicalUri(), alias);
         }
 
         Collection<String> uris = groupFound.getContentUris();
         assertTrue(uris.size() > 0);
         assertEquals(data.goodEastendersEpisodes.getContentUris().size(), uris.size());
 
         List<Content> playlistItems = groupFound.getContents();
         assertTrue(playlistItems.size() > 0);
         Item firstItem = (Item) data.goodEastendersEpisodes.getContents().iterator().next();
         assertEquals(firstItem.getTitle(), playlistItems.iterator().next().getTitle());
     }
     
     public void testShouldNotAllowCreationOfPlaylistWhenContentNotExist() throws Exception {
         try {
             store.createOrUpdateSkeleton(data.goodEastendersEpisodes);
             fail("Should have thrown exception");
         } catch (GroupContentNotExistException e) {
             assertTrue(e.getMessage().contains(data.goodEastendersEpisodes.getCanonicalUri()));
         }
     }
 
 	public void testShouldMarkUnavailableItemsAsUnavailable() throws Exception {
 
 		Episode eggs = new Episode("eggs", "eggs", Publisher.C4);
 		eggs.addVersion(DummyContentData.versionWithEmbeddableLocation(new DateTime(), Duration.standardDays(1), TransportType.LINK));
 		
 		Episode eel = new Episode("eel", "eel", Publisher.C4);
 		eel.addVersion(DummyContentData.versionWithEmbeddableLocation(new DateTime(), Duration.standardDays(1), TransportType.LINK));
 		
 		Episode english = new Episode("english", "english", Publisher.C4);
 		english.addVersion(DummyContentData.versionWithEmbeddableLocation(new DateTime(), Duration.standardDays(1), TransportType.LINK));
 		
 		data.eastenders.addContents(eggs);
 
 		store.createOrUpdate(data.eastenders, false);
 
 		List<Identified> playlists = store.findByCanonicalUri(Lists.newArrayList(data.eastenders.getCanonicalUri()));
 		
 		assertEquals(1, playlists.size());
 		assertEquals(3, ((Container<?>) playlists.get(0)).getContents().size());
 
 		data.eastenders.setContents(ImmutableList.of(eggs, english));
 
 		store.createOrUpdate(data.eastenders, true);
 
 		playlists = store.findByCanonicalUri(Lists.newArrayList(data.eastenders.getCanonicalUri()));
 		assertEquals(1, playlists.size());
 		List<? extends Item> items = ((Container<?>) playlists.get(0)).getContents();
 		assertEquals(4, items.size());
 
 		for (Item item : items) {
 			if (item.equals(eggs) || item.equals(english)) {
 				assertTrue(isAvailable(item));
 			} else {
 				assertFalse(isAvailable(item));
 			}
 		}
 
 		data.eastenders.setContents(ImmutableList.of(eel, english));
 
 		store.createOrUpdate(data.eastenders, true);
 
 		playlists = store.findByCanonicalUri(ImmutableList.of(data.eastenders.getCanonicalUri()));
 		assertEquals(1, playlists.size());
 		
 		items = ((Container<?>) playlists.get(0)).getContents();
 		assertEquals(5, items.size());
 
 		for (Item item : items) {
 			if (item.equals(eel) || item.equals(english)) {
 				assertTrue(isAvailable(item));
 			} else {
 				assertFalse(isAvailable(item));
 			}
 		}
 	}
 
     private boolean isAvailable(Item item) {
         assertFalse(item.getVersions().isEmpty());
         for (Version version : item.getVersions()) {
             assertFalse(version.getManifestedAs().isEmpty());
             for (Encoding encoding : version.getManifestedAs()) {
                 assertFalse(encoding.getAvailableAt().isEmpty());
                 for (Location location : encoding.getAvailableAt()) {
                     return location.getAvailable();
                 }
             }
         }
         return false;
     }
 
     public void testShouldIncludeEpisodeBrandSummary() throws Exception {
         store.createOrUpdate(data.theCreditCrunch);
 
         List<Identified> items = store.findByCanonicalUri(ImmutableList.of(data.theCreditCrunch.getCanonicalUri()));
         assertNotNull(items);
         assertEquals(1, items.size());
         assertTrue(items.get(0) instanceof Episode);
         
         Episode episode = (Episode) items.get(0);
         assertEquals(data.theCreditCrunch.getTitle(), episode.getTitle());
         
         Container<?> brandSummary = episode.getContainer();
         assertNotNull(brandSummary);
         assertEquals(data.dispatches.getCanonicalUri(), brandSummary.getCanonicalUri());
     }
     
     public void testShouldGetBrandOrPlaylist() throws Exception {
     	store.createOrUpdate(data.eastenders, true);
         store.createOrUpdateSkeleton(data.goodEastendersEpisodes);
         
         List<Identified> playlists = store.findByCanonicalUri(ImmutableList.of(data.goodEastendersEpisodes.getCanonicalUri()));
         assertEquals(1, playlists.size());
         assertFalse(playlists.get(0) instanceof Brand);
         
         store.createOrUpdate(data.dispatches, false);
         playlists = store.findByCanonicalUri(Lists.newArrayList(data.dispatches.getCanonicalUri()));
         assertEquals(1, playlists.size());
         assertTrue(playlists.get(0) instanceof Brand);
     }
     
     public void testShouldGetEpisodeOrItem() throws Exception {
         store.createOrUpdate(data.englishForCats);
         
         List<Identified> items = store.findByCanonicalUri(ImmutableList.of(data.englishForCats.getCanonicalUri()));
         assertEquals(1, items.size());
         assertFalse(items.get(0) instanceof Episode);
         
         store.createOrUpdate(data.brainSurgery);
         items = store.findByCanonicalUri(ImmutableList.of(data.brainSurgery.getCanonicalUri()));
         assertEquals(1, items.size());
         assertTrue(items.get(0) instanceof Episode);
     }
     
     public void testShouldGetEpisodeThroughAnonymousMethods() throws Exception {
         store.createOrUpdate(data.brainSurgery);
         
         Identified episode = store.findByCanonicalUri(data.brainSurgery.getCanonicalUri());
         assertNotNull(episode);
         assertTrue(episode instanceof Episode);
     }
     
     public void ignoreShouldPreserveAliases() throws Exception {
         data.theCreditCrunch.setAliases(Sets.newHashSet("somealias"));
         store.createOrUpdate(data.theCreditCrunch);
         
         data.theCreditCrunch.setAliases(Sets.newHashSet("anotheralias", "blah"));
         store.createOrUpdate(data.theCreditCrunch);
         
         List<Identified> items = store.findByCanonicalUri(ImmutableList.of(data.theCreditCrunch.getCanonicalUri()));
         assertNotNull(items);
         assertEquals(1, items.size());
         assertEquals(3, items.get(0).getAliases().size());
     }
     
     public void ignoreShouldAddAliases() throws Exception {
         data.theCreditCrunch.setAliases(Sets.newHashSet("somealias"));
         store.createOrUpdate(data.theCreditCrunch);
         
         store.addAliases(data.theCreditCrunch.getCanonicalUri(), Sets.newHashSet("anotherAlias"));
         
         List<Identified> items = store.findByCanonicalUri(ImmutableList.of(data.theCreditCrunch.getCanonicalUri()));
         assertNotNull(items);
         assertEquals(1, items.size());
         assertEquals(2, items.get(0).getAliases().size());
     }
     
     public void testShouldProcessSubPlaylists() throws Exception {
     	store.createOrUpdate(data.eastenders, true);
     	store.createOrUpdate(data.neighbours, true);
 
     	store.createOrUpdateSkeleton(data.goodEastendersEpisodes);
         
         data.goodEastendersEpisodes.addContents(ImmutableList.of(data.neighbours));
         store.createOrUpdateSkeleton(data.goodEastendersEpisodes);
         
         List<Identified> playlists = store.findByCanonicalUri(ImmutableList.of(data.goodEastendersEpisodes.getCanonicalUri()));
         assertEquals(1, playlists.size());
         
         ContentGroup group = (ContentGroup) playlists.get(0);
         assertEquals(ImmutableList.of(data.dotCottonsBigAdventure, data.neighbours), group.getContents());
     }
     
     public void testShouldListAllItems() throws Exception {
         Item item1 = new Item("1", "1", Publisher.BLIP);
         Item item2 = new Item("2", "2", Publisher.BLIP);
         Item item3 = new Item("3", "3", Publisher.BLIP);
 		store.createOrUpdate(item1);
 		store.createOrUpdate(item2);
 		store.createOrUpdate(item3);
         
         ImmutableList<Content> items = ImmutableList.copyOf(store.listAllRoots(null, 2));
         
         assertEquals(ImmutableList.of(item1, item2), items);
         
         items = ImmutableList.copyOf(store.listAllRoots(item2.getCanonicalUri(), 2));
         
         assertEquals(ImmutableList.of(item3), items);
     }
     
     public void testShouldListAllPlaylists() throws Exception {
         store.createOrUpdate(data.dispatches, false);
         store.createOrUpdate(data.eastenders, false);
         
         List<Content> items = ImmutableList.copyOf(store.listAllRoots(null, 2));
         assertEquals(ImmutableList.of(data.eastenders, data.dispatches), items);
     }
     
     public void testThatItemsAreNotRemovedFromTheirBrands() throws Exception {
     	String itemUri = "itemUri";
     	String brandUri = "brandUri";
 
 		Brand brand = new Brand(brandUri, "brand:curie", Publisher.BBC);
 		brand.setContents(new Episode(itemUri, "item:curie", Publisher.BBC));
 		
     	store.createOrUpdate(brand, true);
     	
     	assertThat((Brand) ((Episode) store.findByCanonicalUri(itemUri)).getContainer(), is(brand)); 
     	
     	store.createOrUpdate((new Episode(itemUri, "item:curie", Publisher.BBC)));
 
     	assertThat((Brand) ((Item) store.findByCanonicalUri(itemUri)).getContainer(), is(brand)); 
 	}
     
     public void testThatSavingASkeletalPlaylistThatContainsSubElementsThatArentInTheDBThrowsAnException() throws Exception {
     	Item item = new Item("1", "1", Publisher.BLIP);
     	Item item2 = new Item("2", "2", Publisher.BLIP);
 
     	ContentGroup playlist = new ContentGroup();
 		playlist.setCanonicalUri("playlist");
 		playlist.setContents(item, item2, item2);
 		
 		try { 
 			store.createOrUpdateSkeleton(playlist);
 			fail();
		} catch (GroupContentNotExistException e) {
 			// expected
 		}
 		
 		store.createOrUpdate(item);
 		store.createOrUpdate(item2);
 		
 		// should be ok now because the items are in the db
 		store.createOrUpdateSkeleton(playlist);
 		
 		Container<Item> subplaylist = new Container<Item>("3", "3", Publisher.YOUTUBE);
 
 		playlist.setContents(item, item2, item2, subplaylist);
 		
 		try { 
 			store.createOrUpdateSkeleton(playlist);
 			fail();
		} catch (GroupContentNotExistException e) {
 			// expected
 		}
 	}
     
     public void testPersistingASkeletalPlaylist() throws Exception {
     	
     	Item item1 = new Item("i1", "1", Publisher.BLIP);
     	Item item2 = new Item("i2", "2", Publisher.BLIP);
     	Container<Item> subplaylist = new Container<Item>("subplaylist", "subplaylist", Publisher.BBC);
 
     	store.createOrUpdate(item1);
 		store.createOrUpdate(item2);
 		store.createOrUpdate(subplaylist, false);
     	
     	ContentGroup playlist = new ContentGroup();
 		playlist.setCanonicalUri("playlist");
 		playlist.setContents(item1, item2, subplaylist);
 		
 		store.createOrUpdateSkeleton(playlist);
 		
 		ContentGroup found = (ContentGroup) store.findByCanonicalUri("playlist");
 		
 		assertEquals(clock.now(), found.getLastFetched());
 		assertEquals(clock.now(), found.getFirstSeen());
 		
 		assertEquals(3, found.getContents().size());
 	}
     
     public void testPersistingTopLevelSeries() throws Exception {
     	Episode ep2 = new Episode("2", "2", Publisher.BBC);
 
     	Series series = new Series("1", "1", Publisher.BBC).withSeriesNumber(10);
 		series.addContents(ep2);
 		
 		store.createOrUpdate(series, true);
     	
 		Series found = (Series) store.findByCanonicalUri(series.getCanonicalUri());
 		
 		assertEquals(10, (int) found.getSeriesNumber());
 		
 		assertEquals(ImmutableList.of(ep2), found.getContents());
 		
 		Episode foundEpisode = (Episode) store.findByCanonicalUri(ep2.getCanonicalUri());
 		assertEquals(series, foundEpisode.getContainer());
 	}
     
     public void testPersistingSeriesThatArePartOfABrand() throws Exception {
     	Episode ep2 = new Episode("2", "2", Publisher.BBC);
 
     	Brand brand = new Brand("brand", "brand", Publisher.BBC);
     	brand.setContents(ep2);
     	
     	Series series = new Series("series", "series", Publisher.BBC).withSeriesNumber(10);
     	series.setContents(ep2);
     	
 		store.createOrUpdate(brand, true);
 		
 		assertEquals(ImmutableList.of(brand), store.discover(ContentQuery.MATCHES_EVERYTHING));
 		
 		Episode foundEpisode = (Episode) store.findByCanonicalUri(ep2.getCanonicalUri());
 		
 		assertEquals(series, foundEpisode.getSeries());
 		assertEquals(brand, foundEpisode.getContainer());
 		
 		Series found = (Series) store.findByCanonicalUri(series.getCanonicalUri());
 		assertEquals(10, (int) found.getSeriesNumber());
 	}
 }
