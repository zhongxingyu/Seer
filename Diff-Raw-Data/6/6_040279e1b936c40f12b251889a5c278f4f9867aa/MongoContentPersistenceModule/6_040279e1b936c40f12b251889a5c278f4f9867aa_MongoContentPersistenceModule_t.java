 package org.atlasapi.persistence;
 
 import org.atlasapi.media.channel.ChannelGroupStore;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.channel.MongoChannelGroupStore;
 import org.atlasapi.media.channel.MongoChannelStore;
 import org.atlasapi.media.product.IdSettingProductStore;
 import org.atlasapi.media.product.ProductResolver;
 import org.atlasapi.media.product.ProductStore;
 import org.atlasapi.media.segment.IdSettingSegmentWriter;
 import org.atlasapi.media.segment.MongoSegmentResolver;
 import org.atlasapi.media.segment.MongoSegmentWriter;
 import org.atlasapi.media.segment.SegmentResolver;
 import org.atlasapi.media.segment.SegmentWriter;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.IdSettingContentWriter;
 import org.atlasapi.persistence.content.KnownTypeContentResolver;
 import org.atlasapi.persistence.content.LookupResolvingContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoContentLister;
 import org.atlasapi.persistence.content.mongo.MongoContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoContentWriter;
 import org.atlasapi.persistence.content.mongo.MongoPersonStore;
 import org.atlasapi.persistence.content.mongo.MongoProductStore;
 import org.atlasapi.persistence.content.mongo.MongoTopicStore;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.content.people.QueuingItemsPeopleWriter;
 import org.atlasapi.persistence.content.people.QueuingPersonWriter;
 import org.atlasapi.persistence.content.schedule.mongo.MongoScheduleStore;
 import org.atlasapi.persistence.ids.MongoSequentialIdGenerator;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
 import org.atlasapi.persistence.shorturls.MongoShortUrlSaver;
 import org.atlasapi.persistence.shorturls.ShortUrlSaver;
 import org.atlasapi.persistence.topic.TopicCreatingTopicResolver;
 import org.atlasapi.persistence.topic.TopicQueryResolver;
 import org.atlasapi.persistence.topic.TopicStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Primary;
 
 import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.health.MongoIOProbe;
 import com.metabroadcast.common.time.SystemClock;
 import com.mongodb.Mongo;
 import com.mongodb.MongoReplicaSetProbe;
 import com.mongodb.WriteConcern;
 import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.mongo.LastUpdatedContentFinder;
 import org.atlasapi.persistence.topic.TopicContentLister;
 
 @Configuration
 public class MongoContentPersistenceModule implements ContentPersistenceModule {
 
     private @Autowired Mongo mongo;
 	private @Autowired DatabasedMongo db;
 	private @Autowired AdapterLog log;
 	
 	private @Value("${ids.generate}") String generateIds;
 	
 	public MongoContentPersistenceModule() {}
 	
 	public MongoContentPersistenceModule(Mongo mongo, DatabasedMongo db, AdapterLog log) {
         this.mongo = mongo;
         this.db = db;
         this.log = log;
     }
 	
 	private @Autowired ChannelResolver channelResolver;
 	
 	public @Bean ContentWriter contentWriter() {
 		ContentWriter contentWriter = new MongoContentWriter(db, lookupStore(), new SystemClock());
 		if (Boolean.valueOf(generateIds)) {
 		    contentWriter = new IdSettingContentWriter(lookupStore(), new MongoSequentialIdGenerator(db, "content"), contentWriter);
 		}
         return contentWriter;
 	}
 	
 	@Primary
 	public @Bean ContentResolver contentResolver() {
 	    return new LookupResolvingContentResolver(knownTypeContentResolver(), lookupStore());
 	}
 	
 	public @Bean KnownTypeContentResolver knownTypeContentResolver() {
 	    return new MongoContentResolver(db);
 	}
 	
 	public @Bean MongoLookupEntryStore lookupStore() {
 	    return new MongoLookupEntryStore(db);
 	}
 	
 	public @Bean MongoScheduleStore scheduleStore() {
 	    try {
             return new MongoScheduleStore(db, contentResolver(), channelResolver);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 	}
 	
 	public @Bean ItemsPeopleWriter itemsPeopleWriter() {
 	    return new QueuingItemsPeopleWriter(new QueuingPersonWriter(personStore(), log), log);
 	}
 	
 	public @Bean MongoPersonStore personStore() {
 	    return new MongoPersonStore(db);
 	}
 
 	@Override
 	public @Bean ShortUrlSaver shortUrlSaver() {
 		return new MongoShortUrlSaver(db);
 	}
 	
 	public @Bean MongoContentLister mongoContentLister() {
 		return new MongoContentLister(db);
 	}
     
     public @Bean ContentLister contentLister() {
 		return mongoContentLister();
 	}
     
     public @Bean TopicContentLister topicContentLister() {
 		return mongoContentLister();
 	}
    
    public @Bean LastUpdatedContentFinder lastUpdatedContentFinder() {
		return mongoContentLister();
	}
 	
     @Bean
     MongoReplicaSetProbe mongoReplicaSetProbe() {
         return new MongoReplicaSetProbe(mongo);
     }
 
     @Bean
     MongoIOProbe mongoIoSetProbe() {
         return new MongoIOProbe(mongo).withWriteConcern(WriteConcern.REPLICAS_SAFE);
     }
 
     @Override
     public @Bean TopicStore topicStore() {
         return new TopicCreatingTopicResolver(new MongoTopicStore(db), new MongoSequentialIdGenerator(db, "topic"));
     }
 
     @Override
     public @Bean TopicQueryResolver topicQueryResolver() {
         return new MongoTopicStore(db);
     }
     
     public @Bean SegmentWriter segmentWriter() {
         return new IdSettingSegmentWriter(new MongoSegmentWriter(db, new SubstitutionTableNumberCodec()), segmentResolver(), new MongoSequentialIdGenerator(db, "segment"));
     }
 
     @Override
     public @Bean SegmentResolver segmentResolver() {
         return new MongoSegmentResolver(db, new SubstitutionTableNumberCodec());
     }
         
     @Bean
     public ChannelResolver channelResolver() {
     	return new MongoChannelStore(db);
     }
     
     @Bean
     public ChannelGroupStore channelGroupStore() {
         return new MongoChannelGroupStore(db);
     }
 
     @Bean
     public ProductStore productStore() {
         return new IdSettingProductStore((ProductStore)productResolver(), new MongoSequentialIdGenerator(db, "product"));
     }
 
     @Bean
     public ProductResolver productResolver() {
         return new MongoProductStore(db);
     }
 }
