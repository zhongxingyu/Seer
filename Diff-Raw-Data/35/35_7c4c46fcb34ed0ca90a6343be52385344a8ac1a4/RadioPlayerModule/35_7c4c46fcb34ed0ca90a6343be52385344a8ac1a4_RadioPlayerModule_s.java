 package org.atlasapi.feeds.radioplayer;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.feeds.radioplayer.upload.FTPCredentials;
 import org.atlasapi.feeds.radioplayer.upload.FTPUploadResultRecorder;
 import org.atlasapi.feeds.radioplayer.upload.MongoFTPUploadResultRecorder;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerUploadHealthProbe;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerUploadTask;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerUploadTaskRunner;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerXMLValidator;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.RepetitionRules.RepetitionInterval;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import com.metabroadcast.common.webapp.health.HealthController;
 
 @Configuration
 public class RadioPlayerModule {
 
 	private static final RepetitionInterval UPLOAD_EVERY_HOUR = RepetitionRules.atInterval(Duration.standardHours(1));
 
 	private @Autowired @Qualifier("mongoDbQueryExcutorThatFiltersUriQueries") KnownTypeQueryExecutor queryExecutor;
 	
 	private @Value("${rp.ftp.enabled}") String upload;
     private @Value("${rp.ftp.username}") String ftpUsername;
     private @Value("${rp.ftp.password}") String ftpPassword;
     private @Value("${rp.ftp.host}") String ftpHost;
     private @Value("${rp.ftp.port}") Integer ftpPort;
 	
 	private @Autowired SimpleScheduler scheduler;
 	private @Autowired AdapterLog log;
 	private @Autowired DatabasedMongo mongo;
 	private @Autowired HealthController health;
 
 	public @Bean RadioPlayerController radioPlayerController() {
 		return new RadioPlayerController();
 	}
 	
 	@PostConstruct 
 	public void scheduleTasks() {
 	    RadioPlayerFeedCompiler.init(queryExecutor);
 	    health.addProbes(Iterables.concat(Iterables.transform(RadioPlayerServices.services, serviceHealthProbe()), ImmutableList.of(new RadioPlayerUploadHealthProbe(mongo, "FTP", ftpHost+":"+ftpPort))));
 		if (Boolean.parseBoolean(upload)) {
 			FTPCredentials credentials = FTPCredentials.forServer(ftpHost).withPort(ftpPort).withUsername(ftpUsername).withPassword(ftpPassword).build();
			RadioPlayerXMLValidator validator = createValidator();
 			
			RadioPlayerUploadTask uploader = new RadioPlayerUploadTask(radioPlayerUploadTaskRunner(), RadioPlayerServices.services)
 			    .withLookAhead(7).withLookBack(7)
 			    .withResultRecorder(uploadResultRecorder())
			    .withValidator(validator)
 			    .withLog(log);
             scheduler.schedule(uploader, UPLOAD_EVERY_HOUR);
 
             log.record(new AdapterLogEntry(Severity.INFO).withDescription("Radioplayer uploader scheduled task installed for:" + credentials).withSource(getClass()));
 		} else {
 			log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass())
 			.withDescription("Not installing Radioplayer uploader"));
 		}
 	}
 
 	private Function<? super RadioPlayerService, ? extends HealthProbe> serviceHealthProbe() {
         return new Function<RadioPlayerService, HealthProbe>(){
             @Override
             public HealthProbe apply(RadioPlayerService service) {
                 return new RadioPlayerUploadHealthProbe(mongo, service.getName(), "%1$tY%1$tm%1$td_"+service.getRadioplayerId()+"_PI.xml");
             }
         };
     }
 
     private RadioPlayerXMLValidator createValidator() {
 		try {
 			return RadioPlayerXMLValidator.forSchemas(ImmutableSet.of(
 				Resources.getResource("epgSI_10.xsd").openStream(), 
 				Resources.getResource("epgSchedule_10.xsd").openStream()
 			));
 		} catch (Exception e) {
 			log.record(new AdapterLogEntry(Severity.WARN).withDescription("Couldn't load schemas for RadioPlayer XML validation").withCause(e));
 			return null;
 		}
 	}
     
     public @Bean FTPUploadResultRecorder uploadResultRecorder() {
         return new MongoFTPUploadResultRecorder(mongo);
     }
    
    public @Bean RadioPlayerUploadTaskRunner radioPlayerUploadTaskRunner() {
        FTPCredentials credentials = FTPCredentials.forServer(ftpHost).withPort(ftpPort).withUsername(ftpUsername).withPassword(ftpPassword).build();
        return new RadioPlayerUploadTaskRunner(credentials, uploadResultRecorder(), log);
    }
 	
 	public @Bean RadioPlayerHealthController radioPlayerHealthController() {
 	    return new RadioPlayerHealthController(health);
 	}
 	
 }
