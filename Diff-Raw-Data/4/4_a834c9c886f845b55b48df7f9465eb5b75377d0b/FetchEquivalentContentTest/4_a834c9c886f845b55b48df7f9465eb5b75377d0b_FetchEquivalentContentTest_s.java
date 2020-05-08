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
 
 import java.io.IOException;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.AtlasFetchModule;
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.application.query.ApplicationConfigurationFetcher;
 import org.atlasapi.beans.JaxbXmlTranslator;
 import org.atlasapi.equiv.EquivModule;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Equiv;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.media.entity.simple.ContentQueryResult;
 import org.atlasapi.media.entity.simple.Description;
 import org.atlasapi.media.entity.simple.Playlist;
 import org.atlasapi.persistence.MongoContentPersistenceModule;
 import org.atlasapi.persistence.equiv.EquivalentUrlStore;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.NullAdapterLog;
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
 
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.servlet.StubHttpServletRequest;
 import com.metabroadcast.common.servlet.StubHttpServletResponse;
 import com.metabroadcast.common.webapp.properties.ContextConfigurer;
 
 public class FetchEquivalentContentTest extends TestCase {
 
 	private static final String WIKIPEDIA_URL = "http://en.wikipedia.org/glee";
 	
 	private static final Content C4_GLEE = brandWithALocation("c4/glee", "c4:glee", Publisher.C4);
 	
 	private static final Content HULU_GLEE = brandWithALocation("hulu/glee", "hulu:glee", Publisher.HULU);
 	
 	private static final Fetcher<Content> fetcher = new StubFetcher().respondTo(C4_GLEE).respondTo(HULU_GLEE);
 	
 	private ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AtlasModuleWithLocalMongoAndFakeFetchers.class);
 	
 	public void testResolvingContent() throws Exception {
 		
 		EquivalentUrlStore equivStore = applicationContext.getBean(EquivalentUrlStore.class);
 
 		equivStore.store(new Equiv(C4_GLEE.getCanonicalUri(), WIKIPEDIA_URL));
 
 		Playlist c4Brand = queryForBrand(C4_GLEE.getCanonicalUri());
 		
 		assertEquals(Sets.newHashSet(WIKIPEDIA_URL), c4Brand.getAliases());
 
 		assertEquals(c4Brand, queryForBrand(WIKIPEDIA_URL));
 		
 		equivStore.store(new Equiv(HULU_GLEE.getCanonicalUri(), WIKIPEDIA_URL));
 
 		Playlist huluBrand = queryForBrand(HULU_GLEE.getCanonicalUri());
 
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
 		brand.setContents(ImmutableList.of(item));
 		return brand;
 	}
 
 	private static Set<String> locationUrisFrom(Playlist huluBrand) {
 		Set<String> uris = Sets.newHashSet();
 		for (Description description : huluBrand.getContent()) {
 			org.atlasapi.media.entity.simple.Item item = (org.atlasapi.media.entity.simple.Item) description;
 			for (org.atlasapi.media.entity.simple.Location location : item.getLocations()) {
 				uris.add(location.getUri());
 			}
 		}
 		return uris;
 	}
 
 	private Playlist queryForBrand(String uri) throws IOException {
 		QueryController queryController = applicationContext.getBean(QueryController.class);
 		StubHttpServletResponse response = new StubHttpServletResponse();
 		queryController.content(new StubHttpServletRequest().withRequestUri("/content.xml").withParam("uri", uri), response);
 		ContentQueryResult result = new JaxbXmlTranslator().readFrom(response.getResponseAsString());
 		return (Playlist) Iterables.getOnlyElement(result.getContents());
 	}
 
 	@Configuration
	@Import({EquivModule.class, QueryModule.class, MongoContentPersistenceModule.class, AtlasFetchModule.class, RemoteSiteModule.class})
 	public static class AtlasModuleWithLocalMongoAndFakeFetchers {
 		
 	    public @Bean ContextConfigurer config() {
 			ContextConfigurer c = new ContextConfigurer();
 			c.init();
 			return c;
 		}
 	    
 		public @Bean DatabasedMongo db() {
 			return MongoTestHelper.anEmptyTestDatabase();
 		}
 		
 		public @Bean Fetcher<Content> remoteFetcher() {
 			return fetcher;
 		}
 		
 		public @Bean AdapterLog adapterLog() {
 			return new NullAdapterLog();
 		}
 
 		public @Bean ApplicationConfigurationFetcher configFetcher() {
 			return new DummyApplicationFetcher();
 		}
 	}
 	
 	public static class DummyApplicationFetcher implements ApplicationConfigurationFetcher {
 
 		@Override
 		public Maybe<ApplicationConfiguration> configurationFor(HttpServletRequest request) {
 			ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.DEFAULT_CONFIGURATION.copyWithIncludedPublishers(ImmutableSet.copyOf(Publisher.values()));
 			return Maybe.just(applicationConfiguration);
 		}
 	}
 }
