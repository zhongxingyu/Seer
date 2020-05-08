 package org.atlasapi.remotesite.bbc.ion;
 
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
 import org.joda.time.DateTime;
 
 import com.metabroadcast.common.time.DateTimeZones;
import org.joda.time.LocalDate;
 
 public class ScheduleBasedItemUpdatingBroadcastHandler extends DefaultBbcIonBroadcastHandler {
 
     public ScheduleBasedItemUpdatingBroadcastHandler(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
         super(resolver, writer, log);
     }
     
     @Override
     protected boolean fullFetchPermitted(IonBroadcast broadcast, String itemUri) {
        LocalDate today = new DateTime(DateTimeZones.UTC).toLocalDate();
        LocalDate broadcastDay = broadcast.getStart().toLocalDate();
         
         String mediaType = broadcast.getMediaType();
         
         // today -4 for radio, today ± 2 for tv
         // radio are more likely to publish clips after a show has been broadcast
         // so with a limited ingest window it is more important to go back as far as possible for radio
         // to ensure that clips are not missed
         if (mediaType.equals("audio")) {
             return today.plusDays(1).isAfter(broadcastDay) && today.minusDays(5).isBefore(broadcastDay);    
         } else {
             return today.plusDays(3).isAfter(broadcastDay) && today.minusDays(3).isBefore(broadcastDay);
         }
     }
     
 }
