 package org.atlasapi.search;
 
 
 import com.google.common.base.Splitter;
 
 import org.atlasapi.persistence.cassandra.CassandraSchema;
 import org.atlasapi.persistence.content.mongo.MongoContentLister;
 import org.atlasapi.persistence.content.mongo.MongoContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoPersonStore;
 import org.atlasapi.search.searcher.LuceneSearcherProbe;
 import org.atlasapi.search.searcher.ReloadingContentBootstrapper;
 import org.atlasapi.search.view.JsonSearchResultsView;
 import org.atlasapi.search.www.HealthController;
 import org.atlasapi.search.www.WebAwareModule;
 import org.springframework.context.annotation.Bean;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.properties.Configurer;
 import com.mongodb.Mongo;
 import java.io.File;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
 import org.atlasapi.search.loader.ContentBootstrapper;
 import org.atlasapi.search.searcher.LuceneContentIndex;
 
 public class AtlasSearchModule extends WebAwareModule {
 
 	private final String mongoHost = Configurer.get("mongo.host").get();
 	private final String mongoDbName = Configurer.get("mongo.dbName").get();
 	private final String cassandraEnv = Configurer.get("cassandra.env").get();
     private final String cassandraSeeds = Configurer.get("cassandra.seeds").get();
     private final String cassandraPort = Configurer.get("cassandra.port").get();
     private final String cassandraConnectionTimeout = Configurer.get("cassandra.connectionTimeout").get();
     private final String cassandraRequestTimeout = Configurer.get("cassandra.requestTimeout").get();
     private final String luceneDir = Configurer.get("lucene.contentDir").get();
     private final String luceneIndexAtStartup = Configurer.get("lucene.indexAtStartup", "").get();
 	private final String enablePeople = Configurer.get("people.enabled").get();
 
 	@Override
 	public void configure() {        
         LuceneContentIndex index = new LuceneContentIndex(new File(luceneDir), new MongoContentResolver(mongo()));
         
         ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
         ReloadingContentBootstrapper mongoBootstrapper = new ReloadingContentBootstrapper(index, mongoBootstrapper(), scheduler, Boolean.valueOf(luceneIndexAtStartup), 180, TimeUnit.MINUTES);
 	    ReloadingContentBootstrapper cassandraBootstrapper = new ReloadingContentBootstrapper(index, cassandraBootstrapper(), scheduler, Boolean.valueOf(luceneIndexAtStartup), 7, TimeUnit.DAYS);
         
 		bind("/system/health", new HealthController(ImmutableList.<HealthProbe>of(
                 new LuceneSearcherProbe("mongo-lucene", mongoBootstrapper), 
                 new LuceneSearcherProbe("cassandra-lucene", cassandraBootstrapper))));
 		bind("/titles", new SearchServlet(new JsonSearchResultsView(), index));
 		
 		mongoBootstrapper.start();
         cassandraBootstrapper.start();
 	}
 	
     @Bean
     ContentBootstrapper mongoBootstrapper() {
         ContentBootstrapper bootstrapper = new ContentBootstrapper();
         bootstrapper.withContentListers(new MongoContentLister(mongo()));
         if (Boolean.valueOf(enablePeople)) {
             bootstrapper.withPeopleListers(new MongoPersonStore(mongo()));
         }
         return bootstrapper;
     }
     
     @Bean
     ContentBootstrapper cassandraBootstrapper() {
         ContentBootstrapper bootstrapper = new ContentBootstrapper();
         bootstrapper.withContentListers(cassandra());
         return bootstrapper;
     }
 
 	public @Bean DatabasedMongo mongo() {
 		try {
 			Mongo mongo = new Mongo(mongoHost);
 			mongo.slaveOk();
             return new DatabasedMongo(mongo, mongoDbName);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
     
     public @Bean CassandraContentStore cassandra() {
 		try {
 			CassandraContentStore cassandraContentStore = new CassandraContentStore(
			    CassandraSchema.getKeyspace(cassandraEnv),
 			    Lists.newArrayList(Splitter.on(',').split(cassandraSeeds)), 
                 Integer.parseInt(cassandraPort), 
                 Runtime.getRuntime().availableProcessors() * 10, 
                 Integer.parseInt(cassandraConnectionTimeout), 
                 Integer.parseInt(cassandraRequestTimeout));
             cassandraContentStore.init();
             return cassandraContentStore;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
