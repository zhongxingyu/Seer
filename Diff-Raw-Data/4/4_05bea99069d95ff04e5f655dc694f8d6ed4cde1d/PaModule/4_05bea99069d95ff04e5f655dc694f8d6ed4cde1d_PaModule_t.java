 package org.atlasapi.remotesite.pa;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
 import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
 import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
 import org.atlasapi.remotesite.pa.film.PaFilmModule;
 import org.atlasapi.s3.DefaultS3Client;
 import org.atlasapi.s3.S3Client;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 
 import com.metabroadcast.common.scheduling.RepetitionRule;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import com.metabroadcast.common.security.UsernameAndPassword;
 
 @Configuration
 @Import(PaFilmModule.class)
 public class PaModule {
     private final static RepetitionRule RECENT_FILE_INGEST = RepetitionRules.every(Duration.standardHours(2)).withOffset(Duration.standardHours(1));
     private final static RepetitionRule RECENT_FILE_DOWNLOAD = RepetitionRules.every(Duration.standardHours(2));
     private final static RepetitionRule COMPLETE_INGEST = RepetitionRules.NEVER;//weekly(DayOfWeek.FRIDAY, new LocalTime(22, 0, 0));
     
     private @Autowired SimpleScheduler scheduler;
     private @Autowired ContentWriter contentWriter;
     private @Autowired ContentResolver contentResolver;
     private @Autowired AdapterLog log;
     private @Autowired ScheduleResolver scheduleResolver;
     private @Autowired ItemsPeopleWriter peopleWriter;
     
     private @Value("${pa.ftp.username}") String ftpUsername;
     private @Value("${pa.ftp.password}") String ftpPassword;
     private @Value("${pa.ftp.host}") String ftpHost;
     private @Value("${pa.ftp.path}") String ftpPath;
     private @Value("${pa.filesPath}") String localFilesPath;
     private @Value("${s3.access}") String s3access;
     private @Value("${s3.secret}") String s3secret;
     
     @PostConstruct
     public void startBackgroundTasks() {
         scheduler.schedule(paFileUpdater(), RECENT_FILE_DOWNLOAD);
         log.record(new AdapterLogEntry(Severity.INFO).withDescription("PA update scheduled task installed").withSource(PaCompleteUpdater.class));
     }
     
     @Bean PaFtpFileUpdater ftpFileUpdater() {
         return new PaFtpFileUpdater(ftpHost, new UsernameAndPassword(ftpUsername, ftpPassword), ftpPath, paProgrammeDataStore(), log);
     }
     
     @Bean PaProgrammeDataStore paProgrammeDataStore() {
         S3Client s3client = new DefaultS3Client(s3access, s3secret, "pa-data");
         return new DefaultPaProgrammeDataStore(localFilesPath, s3client);
     }
     
     @Bean PaProgDataProcessor paProgrammeProcessor() {
         return new PaProgrammeProcessor(contentWriter, contentResolver, peopleWriter, log);
     }
     
     @Bean PaCompleteUpdater paCompleteUpdater() {
         PaEmptyScheduleProcessor processor = new PaEmptyScheduleProcessor(paProgrammeProcessor(), scheduleResolver);
        PaChannelProcessor channelProcessor = new PaChannelProcessor(processor, broadcastTrimmer(), log);
	PaCompleteUpdater updater = new PaCompleteUpdater(channelProcessor, paProgrammeDataStore(), log);
         scheduler.schedule(updater, COMPLETE_INGEST);
         return updater;
     }
     
     @Bean PaRecentUpdater paRecentUpdater() {
         PaChannelProcessor channelProcessor = new PaChannelProcessor(paProgrammeProcessor(), broadcastTrimmer(), log);
         PaRecentUpdater updater = new PaRecentUpdater(channelProcessor, paProgrammeDataStore(), log);
         scheduler.schedule(updater, RECENT_FILE_INGEST);
         return updater;
     }
     
     @Bean BroadcastTrimmer broadcastTrimmer() {
         return new BroadcastTrimmer(Publisher.PA, scheduleResolver, contentResolver, contentWriter, log);
     }
     
     @Bean PaFileUpdater paFileUpdater() {
         return new PaFileUpdater(ftpFileUpdater(), log);
     }
     
     public @Bean PaSingleDateUpdatingController paUpdateController() {
         PaChannelProcessor channelProcessor = new PaChannelProcessor(paProgrammeProcessor(), broadcastTrimmer(), log);
         return new PaSingleDateUpdatingController(channelProcessor, scheduleResolver, log, paProgrammeDataStore());
     }
 }
