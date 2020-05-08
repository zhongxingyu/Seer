 package org.atlasapi.feeds.radioplayer.upload;
 
 import java.util.Comparator;
 import java.util.List;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.upload.FTPUploadResult.FTPUploadResultType;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.health.ProbeResult;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 public class RadioPlayerUploadHealthProbe implements HealthProbe {
 
     private static final String DATE = "yyyyMMdd";
     private static final String DATE_TIME = "dd/MM/yy HH:mm:ss";
 
     private DBCollection results;
     private FTPUploadResultTranslator translator;
     private final Iterable<RadioPlayerService> services;
     private int lookBack = 2;
     private int lookAhead = 7;
 
     public RadioPlayerUploadHealthProbe(DatabasedMongo mongo, Iterable<RadioPlayerService> services) {
         this.services = services;
         this.results = mongo.collection("radioplayer");
         this.translator = new FTPUploadResultTranslator();
     }
 
     public RadioPlayerUploadHealthProbe withLookBack(int lookBack) {
         this.lookBack = lookBack;
         return this;
     }
 
     public RadioPlayerUploadHealthProbe withLookAhead(int lookAhead) {
         this.lookAhead = lookAhead;
         return this;
     }
 
     @Override
     public ProbeResult probe() {
         ProbeResult result = new ProbeResult("UK Radioplayer");
 
         for(RadioPlayerService service : services) {
            DateTime day = new LocalDate().toInterval(DateTimeZones.UTC).getStart().minusDays(lookBack);
             for(int i = 0; i < (lookAhead + lookBack + 1); i++, day = day.plusDays(1)) {
                 addEntry(result, service, day);
             }
         }
 
         return result;
     }
 
     private void addEntry(ProbeResult result, RadioPlayerService service, DateTime day) {
         boolean success = true, failure = true, unknown = true;
         DBCursor resultsForServiceDay = results.find(new BasicDBObject("filename", filenameFor(service, day))).sort(new BasicDBObject("time", -1));
         List<FTPUploadResult> results = Lists.newArrayList();
         for(DBObject dbo : resultsForServiceDay) {
             FTPUploadResult translated = translator.fromDBObject(dbo);
             if(FTPUploadResultType.UNKNOWN.equals(translated.type()) && unknown) {
                 results.add(translated);
                 unknown = false;
             } else if(FTPUploadResultType.SUCCESS.equals(translated.type()) && success) {
                 results.add(translated);
                 success = false;
             } else if(FTPUploadResultType.FAILURE.equals(translated.type()) && failure) {
                 results.add(translated);
                 failure = false;
             }
             if(!success && !failure && !unknown) {
                 break;
             }
         }
         addEntry(result, String.format("%s %s", service.getName(), day.toString("dd/MM/yyyy")), results);
     }
 
     private void addEntry(ProbeResult result, String key, List<FTPUploadResult> results) {
         if(results.isEmpty()) {
             result.addInfo(key, "No Data.");
             return;
         }
         String value = buildValue(results);
         FTPUploadResultType first = results.get(0).type();
         if(FTPUploadResultType.UNKNOWN.equals(first)) {
             result.addInfo(key, value);
         } else {
             result.add(key, value, FTPUploadResultType.SUCCESS.equals(first));
         }
     }
 
     private String buildValue(List<FTPUploadResult> results) {
         StringBuilder builder = new StringBuilder("<table>");
         for(FTPUploadResult result : Iterables.limit(sort(results),2)) {
             builder.append("<tr><td>");
             builder.append(result.uploadTime().toString(DATE_TIME));
            builder.append(" ");
            builder.append(result.type().toNiceString());
             builder.append("</td><td>");
             if(result.message() != null) {
                 builder.append(result.message());
             }
             builder.append("</td></tr>");
         }
         return builder.append("</table>").toString();
     }
 
     private List<FTPUploadResult> sort(List<FTPUploadResult> results) {
         return Ordering.from(new Comparator<FTPUploadResult>(){
             @Override
             public int compare(FTPUploadResult r1, FTPUploadResult r2) {
                 int c = r1.type().compareTo(r2.type());
                 if(c == 0) {
                     c = r1.uploadTime().compareTo(r2.uploadTime());
                 }
                 return c;
             }}).immutableSortedCopy(results);
     }
 
     private String filenameFor(RadioPlayerService service, DateTime day) {
         return String.format("%s_%s_PI.xml", day.toString(DATE), service.getRadioplayerId());
     }
 
     @Override
     public String title() {
         return "UK Radioplayer";
     }
 
 }
