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
 
 package org.atlasapi.query;
 
 import org.atlasapi.equiv.query.BrandMergingQueryExecutor;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.AggregateContentListener;
 import org.atlasapi.persistence.content.ContentListener;
 import org.atlasapi.persistence.content.QueueingContentListener;
 import org.atlasapi.persistence.content.mongo.MongoDBQueryExecutor;
 import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
 import org.atlasapi.persistence.content.mongo.MongoRoughSearch;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.system.AToZUriSource;
 import org.atlasapi.query.content.ApplicationConfigurationQueryExecutor;
 import org.atlasapi.query.content.CurieResolvingQueryExecutor;
import org.atlasapi.query.content.UniqueContentForUriQueryExecutor;
 import org.atlasapi.query.content.UriFetchingQueryExecutor;
 import org.atlasapi.query.content.fuzzy.DefuzzingQueryExecutor;
 import org.atlasapi.query.content.fuzzy.InMemoryFuzzySearcher;
 import org.atlasapi.query.content.fuzzy.InMemoryIndexProbe;
 import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
 import org.atlasapi.remotesite.health.BroadcasterProbe;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.collect.ImmutableList;
 import com.metabroadcast.common.webapp.health.HealthProbe;
 
 @Configuration
 public class QueryModule {
 
 	private @Autowired @Qualifier("contentResolver") CanonicalisingFetcher localOrRemoteFetcher;
 	private @Autowired MongoRoughSearch roughSearch;
 	private @Autowired MongoDbBackedContentStore store;
 	private @Autowired AggregateContentListener aggregateContentListener;
 	private @Value("${applications.enabled}") String applicationsEnabled;
 	
 	@Bean KnownTypeQueryExecutor mongoQueryExecutor() {
 		return new MongoDBQueryExecutor(roughSearch);
 	}
 	
 	public @Bean HealthProbe c4Probe() {
 		return new BroadcasterProbe(Publisher.C4, new AToZUriSource("http://www.channel4.com/programmes/atoz/", "", true), store);
 	}
 	
 	public @Bean HealthProbe bbcProbe() {
 		return new BroadcasterProbe(Publisher.BBC, ImmutableList.of(
 				"http://www.bbc.co.uk/programmes/b006m86d", //Eastenders
 				"http://www.bbc.co.uk/programmes/b006mf4b", //Spooks
 				"http://www.bbc.co.uk/programmes/b006t1q9", //Question Time
 				"http://www.bbc.co.uk/programmes/b006qj9z", //Today
 				"http://www.bbc.co.uk/programmes/b006md2v", //Pierre Bleu
 				"http://www.bbc.co.uk/programmes/b0071b63", //L'apprentice
 				"http://www.bbc.co.uk/programmes/b007t9yb", //Match of the Day 2
 				"http://www.bbc.co.uk/programmes/b0087g39", //Helicopter Heroes
 				"http://www.bbc.co.uk/programmes/b006mk1s", //Mastermind
 				"http://www.bbc.co.uk/programmes/b006wknd" //Rob da Bank, yeh...
 		), store);
 	}
 
 	@Bean KnownTypeQueryExecutor mongoDbQueryExcutorThatFiltersUriQueries() {
 		MongoDBQueryExecutor executor = new MongoDBQueryExecutor(roughSearch);
 		executor.setFilterUriQueries(true);
 		return executor;
 	}
 
 	@Bean KnownTypeQueryExecutor queryExecutor() {
 	    UriFetchingQueryExecutor uriFetching = new UriFetchingQueryExecutor(localOrRemoteFetcher, new DefuzzingQueryExecutor(mongoQueryExecutor(), mongoDbQueryExcutorThatFiltersUriQueries(), titleSearcher()));
 		CurieResolvingQueryExecutor curieResolving = new CurieResolvingQueryExecutor(uriFetching);
 		BrandMergingQueryExecutor brandMerger = new BrandMergingQueryExecutor(curieResolving);
 	    if (Boolean.parseBoolean(applicationsEnabled)) {
 	        return new ApplicationConfigurationQueryExecutor(brandMerger);
 	    } else {
 	        return brandMerger;
 	    }
 	}
 	
 	@Bean InMemoryFuzzySearcher titleSearcher() {
 		return new InMemoryFuzzySearcher();
 	}
 	
 	@Bean InMemoryIndexProbe inMemoryIndexProbe() {
 		return new InMemoryIndexProbe(titleSearcher());
 	}
     
     @Bean ContentListener queueingContentListener() {
         QueueingContentListener queueingContentListener = queue();
         aggregateContentListener.addListener(queueingContentListener);
         return queueingContentListener;
     }
 
 	@Bean QueueingContentListener queue() {
 		QueueingContentListener listener = new QueueingContentListener(titleSearcher());
 		listener.start();
 		return listener;
 	}
 }
