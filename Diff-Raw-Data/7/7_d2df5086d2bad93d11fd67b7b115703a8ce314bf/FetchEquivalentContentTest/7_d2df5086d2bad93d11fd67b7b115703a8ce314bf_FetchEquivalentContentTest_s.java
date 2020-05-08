 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.systest;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.AtlasFetchModule;
 import org.atlasapi.equiv.EquivModule;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Equiv;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.MongoContentPersistenceModule;
 import org.atlasapi.persistence.equiv.EquivalentUrlStore;
 import org.atlasapi.persistence.system.Fetcher;
 import org.atlasapi.query.QueryModule;
 import org.atlasapi.query.v2.QueryController;
 import org.atlasapi.remotesite.RemoteSiteModule;
 import org.atlasapi.remotesite.StubFetcher;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.context.annotation.ImportResource;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.servlet.StubHttpServletRequest;
 import com.mongodb.Mongo;
 
 public class FetchEquivalentContentTest extends TestCase {
 
 	private static final String WIKIPEDIA_URL = "http://en.wikipedia.org/glee";
 	
 	private static final Content C4_GLEE = brandWithALocation("c4/glee", "c4:glee", Publisher.C4);
 	
 	private static final Content HULU_GLEE = brandWithALocation("hulu/glee", "hulu:glee", Publisher.HULU);
 	
 	private static final Fetcher<Content> fetcher = new StubFetcher().respondTo(C4_GLEE).respondTo(HULU_GLEE);
 	
 	private ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AtlasModuleWithLocalMongoAndFakeFetchers.class);
 	
 	public void testResolvingContent() throws Exception {
 		
 		EquivalentUrlStore equivStore = applicationContext.getBean(EquivalentUrlStore.class);
 
 		equivStore.store(new Equiv(C4_GLEE.getCanonicalUri(), WIKIPEDIA_URL));
 
 		Brand c4Brand = queryForBrand(C4_GLEE.getCanonicalUri());
 		
 		assertEquals(Sets.newHashSet(WIKIPEDIA_URL), c4Brand.getAliases());
 
 		assertEquals(c4Brand, queryForBrand(WIKIPEDIA_URL));
 		
 		equivStore.store(new Equiv(HULU_GLEE.getCanonicalUri(), WIKIPEDIA_URL));
 
 		Brand huluBrand = queryForBrand(HULU_GLEE.getCanonicalUri());
 
 		assertEquals(Sets.newHashSet(WIKIPEDIA_URL, C4_GLEE.getCanonicalUri()), huluBrand.getAliases());
 		
 		// check that the hulu brand has been merged with the C4 brand
 		// commented out as there is currently no overlap
 		assertFalse(Iterables.isEmpty(Iterables.filter(locationUrisFrom(huluBrand), Predicates.contains(Pattern.compile("c4.*")))));
 	}
 
 	private static Content brandWithALocation(String uri, String curie, Publisher pubsliher) {
 		Brand brand = new Brand(uri, curie, pubsliher);
 		Episode item = new Episode(uri + "/" + 1, curie + ":1", pubsliher);
 
 		Location location = new Location();
 		location.setUri("c4/1");
 		
 		Encoding encoding = new Encoding();
 		encoding.addAvailableAt(location);
 		
 		Version version = new Version();
 		version.addManifestedAs(encoding);
 		item.addVersion(version);
 		brand.addItem(item);
 		
 		return brand;
 	}
 
 	private static Set<String> locationUrisFrom(Brand huluBrand) {
 		Set<String> uris = Sets.newHashSet();
 		for (Item item : huluBrand.getItems()) {
 			for (Version version : item.getVersions()) {
 				for (Encoding encoding : version.getManifestedAs()) {
 					for (Location location : encoding.getAvailableAt()) {
 						uris.add(location.getUri());
 					}
 				}
 			}
 		}
 		return uris;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Brand queryForBrand(String uri) {
 		QueryController queryController = applicationContext.getBean(QueryController.class);
 		ModelAndView modelAndView = queryController.brand(new StubHttpServletRequest().withParam("uri", uri));
 		Collection<Brand> brands = (Collection<Brand>) modelAndView.getModel().get("graph");
 		return Iterables.getOnlyElement(brands);
 	}
 
 	@Configuration
 	@ImportResource("classpath:atlas.xml")
 	@Import({EquivModule.class, QueryModule.class, MongoContentPersistenceModule.class, AtlasFetchModule.class, RemoteSiteModule.class})
 	public static class AtlasModuleWithLocalMongoAndFakeFetchers {
 		
 		public @Bean Mongo mongo() {
 			return MongoTestHelper.anEmptyMongo();
 		}
 		
 		public @Bean Fetcher<Content> remoteFetcher() {
 			return fetcher;
 		}
 	}
 }
