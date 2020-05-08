 package org.atlasapi.remotesite.bbc;
 
 import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;
 
 import javax.annotation.PostConstruct;
 import javax.xml.bind.JAXBException;
 
 import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.ContentWriters;
 import org.atlasapi.remotesite.bbc.atoz.BbcSlashProgrammesAtoZUpdater;
 import org.atlasapi.remotesite.bbc.ion.BbcIonEpisodeDetailItemFetcherClient;
 import org.atlasapi.remotesite.bbc.ion.BbcIonOndemandChangeUpdater;
 import org.atlasapi.remotesite.bbc.ion.BbcIonScheduleController;
 import org.atlasapi.remotesite.bbc.ion.BbcIonScheduleUpdater;
 import org.atlasapi.remotesite.bbc.ion.BbcIonScheduleUriSource;
 import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
 import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
 import org.atlasapi.remotesite.bbc.schedule.BbcScheduleController;
 import org.atlasapi.remotesite.bbc.schedule.BbcScheduledProgrammeUpdater;
 import org.atlasapi.remotesite.bbc.schedule.DatedBbcScheduleUriSource;
 import org.joda.time.Duration;
 import org.joda.time.LocalTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
 import com.metabroadcast.common.scheduling.RepetitionRules.Every;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 
 @Configuration
 public class BbcModule {
 
 	private final static Daily BRAND_UPDATE_TIME = RepetitionRules.daily(new LocalTime(4, 0, 0));
 	private final static Daily SCHEDULED_UPDATE_TIME = RepetitionRules.daily(new LocalTime(5, 0, 0));
 	private final static Daily HIGHLIGHTS_UPDATE_TIME = RepetitionRules.daily(new LocalTime(10, 0, 0));
 	private final static Every TEN_MINUTES = RepetitionRules.every(Duration.standardMinutes(10));
 	private final static Every SEVEN_MINUTES = RepetitionRules.every(Duration.standardMinutes(10));
 	private final static Every ONE_HOUR = RepetitionRules.every(Duration.standardHours(1));
 
     private @Autowired MongoDbBackedContentStore contentStore;
 	private @Autowired ContentWriters contentWriters;
 	private @Autowired AdapterLog log;
 	private @Autowired SimpleScheduler scheduler;
 	
     @PostConstruct
     public void scheduleTasks() {
         scheduler.schedule(bbcFeedsUpdater(), BRAND_UPDATE_TIME);
         scheduler.schedule(bbcHighlightsUpdater(), HIGHLIGHTS_UPDATE_TIME);
         try {
             scheduler.schedule(bbcSchedulesUpdater(), SCHEDULED_UPDATE_TIME);
         } catch (JAXBException e) {
             log.record(new AdapterLogEntry(Severity.INFO).withCause(e).withDescription("Couldn't create BBC Schedule Updater task"));
         }
         scheduler.schedule(bbcIonUpdater(0, 0).withItemFetchClient(new BbcIonEpisodeDetailItemFetcherClient(log)), TEN_MINUTES);
         scheduler.schedule(bbcIonUpdater(7, 7).withItemFetchClient(new BbcIonEpisodeDetailItemFetcherClient(log)), ONE_HOUR);
         scheduler.schedule(bbcIonOndemandChangeUpdater(), SEVEN_MINUTES);
         log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC update scheduled tasks installed"));
     }
 	
 	private BbcIonScheduleUpdater bbcIonUpdater(int lookBack, int lookAhead) {
         return new BbcIonScheduleUpdater(new BbcIonScheduleUriSource().withLookAhead(lookAhead).withLookBack(lookBack), contentStore, contentWriters, deserializerForClass(IonSchedule.class), log);
     }
 
     @Bean Runnable bbcSchedulesUpdater() throws JAXBException {
 	    DatedBbcScheduleUriSource uriSource = new DatedBbcScheduleUriSource().withLookAhead(10);
 		return new BbcScheduledProgrammeUpdater(contentStore, bbcProgrammeAdapter(), contentWriters, uriSource, log);
 	}
 	
 	@Bean BbcScheduleController bbcScheduleController() {
 	    return new BbcScheduleController(contentStore, bbcProgrammeAdapter(), contentWriters, log);
 	}
 	
 	@Bean BbcIonScheduleController bbcIonScheduleController() {
 	    return new BbcIonScheduleController(contentStore, contentWriters, log);
 	}
 	@Bean Runnable bbcHighlightsUpdater() {
 		return new BbcIplayerHightlightsAdapter(contentWriters, log);
 	}
 
 	@Bean Runnable bbcFeedsUpdater() {
 		return new BbcSlashProgrammesAtoZUpdater(contentWriters, log);
 	}
 	
 	@Bean BbcProgrammeAdapter bbcProgrammeAdapter() {
 		return new BbcProgrammeAdapter(log);
 	}
 	
 	@Bean BbcIonOndemandChangeUpdater bbcIonOndemandChangeUpdater() {
 	    return new BbcIonOndemandChangeUpdater(contentStore, contentWriters, deserializerForClass(IonOndemandChanges.class), log);
 	}
 }
