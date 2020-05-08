 package org.atlasapi.remotesite.metabroadcast;
 
 import static org.atlasapi.remotesite.HttpClients.webserviceClient;
 
 import java.text.ParseException;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.topic.TopicQueryResolver;
 import org.atlasapi.persistence.topic.TopicStore;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.security.AWSCredentials;
 import org.joda.time.Duration;
 import org.joda.time.LocalTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.net.HostSpecifier;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 
 @Configuration
 public class MetaBroadcastModule {
 	
     private @Value("${cannon.host.name}") String cannonHostName;
     private @Value("${cannon.host.port}") Integer cannonHostPort;
 	private @Value("${s3.access}") String s3access;
 	private @Value("${s3.secret}") String s3secret;
 	private @Value("${magpie.s3.bucket}") String s3Bucket;
     private @Autowired ContentResolver contentResolver;
     private @Autowired ContentWriter contentWriter;
    private @Autowired TopicStore topicStore;
    private @Autowired TopicQueryResolver topicResolver;
     private @Autowired SimpleScheduler scheduler;
     private @Autowired AdapterLog log;
     
     @PostConstruct
     public void scheduleTasks() {
         scheduler.schedule(twitterUpdaterTask(), RepetitionRules.every(Duration.standardHours(12)).withOffset(Duration.standardHours(7)));
         scheduler.schedule(magpieUpdaterTask(), RepetitionRules.daily(new LocalTime(3, 0 , 0)));
     }
 
     @Bean
     CannonTwitterTopicsUpdater twitterUpdaterTask() {
 		return new CannonTwitterTopicsUpdater(cannonTopicsClient(), twitterUpdater());
 	}
     

 	MagpieUpdaterTask magpieUpdaterTask() {
 		return new MagpieUpdaterTask(magpieUpdater());
 	}
 
 	@Bean
 	MetaBroadcastTwitterTopicsUpdater twitterUpdater() {
 		return new MetaBroadcastTwitterTopicsUpdater(cannonTopicsClient(), contentResolver,
 				topicStore, topicResolver, contentWriter, log);
 	}
 	
     @Bean
     MetaBroadcastMagpieUpdater magpieUpdater() {
 		return new MetaBroadcastMagpieUpdater(contentResolver, topicStore, 
 				topicResolver, contentWriter, awsService(), s3Bucket, log);
 	}
 
 	@Bean 
     CannonTwitterTopicsClient cannonTopicsClient() {
         try {
             return new CannonTwitterTopicsClient(webserviceClient(), HostSpecifier.from(cannonHostName), Optional.fromNullable(cannonHostPort), log);
         } catch (ParseException e) {
             throw Throwables.propagate(e);
         }
     }
     
     @Bean
     S3Service awsService() {
 		try {
 			return new RestS3Service(new AWSCredentials(s3access, s3secret));
 		} catch (S3ServiceException e) {
             throw Throwables.propagate(e);
 		}
     }
     
 }
