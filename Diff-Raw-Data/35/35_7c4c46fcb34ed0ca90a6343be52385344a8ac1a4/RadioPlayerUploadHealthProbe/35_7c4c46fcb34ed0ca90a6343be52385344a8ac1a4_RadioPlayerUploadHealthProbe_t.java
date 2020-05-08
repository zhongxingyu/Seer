 package org.atlasapi.feeds.radioplayer.upload;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.feeds.radioplayer.upload.FTPUploadResult.FTPUploadResultType;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.health.ProbeResult;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class RadioPlayerUploadHealthProbe implements HealthProbe {
 
     private static final String DATE_TIME = "dd/MM/yy HH:mm:ss";
 
     private final DBCollection results;
     private final FTPUploadResultTranslator translator;
     private final String title;
     private final String filenamePattern;
 
     private int lookBack = 7;
     private int lookAhead = 7;
 
     private static final ImmutableList<FTPUploadResultType> RESULT_TYPES = ImmutableList.copyOf(FTPUploadResultType.values());
     
     public RadioPlayerUploadHealthProbe(DatabasedMongo mongo, String title, String filenamePattern) {
         this.results = mongo.collection("radioplayer");
         this.title = title;
         this.filenamePattern = filenamePattern;
         this.translator = new FTPUploadResultTranslator();
     }
 
     @Override
     public ProbeResult probe() {
         ProbeResult result = new ProbeResult(title);
         
         Set<String> filenames = Sets.newHashSetWithExpectedSize((lookAhead + lookBack + 1));
         
         DateTime day = new LocalDate().toInterval(DateTimeZones.UTC).getStart().minusDays(lookBack);
         for(int i = 0; i < (lookAhead + lookBack + 1); i++, day = day.plusDays(1)) {
             filenames.add(String.format(filenamePattern, day.toDate()));
         }
         for (String filename : Ordering.natural().immutableSortedCopy(filenames)) {
             addEntry(result, filename);
         }
         
         return result;
     }
     
     public RadioPlayerUploadHealthProbe withLookBack(int lookBack) {
         this.lookBack = lookBack;
         return this;
     }
 
     public RadioPlayerUploadHealthProbe withLookAhead(int lookAhead) {
         this.lookAhead = lookAhead;
         return this;
     }
 
     private void addEntry(ProbeResult result, final String filename) {
         Iterable<FTPUploadResult> fileResults = Iterables.filter(Iterables.transform(RESULT_TYPES, new Function<FTPUploadResultType, FTPUploadResult>() {
             @Override
             public FTPUploadResult apply(FTPUploadResultType input) {
                 DBObject dboResult = results.findOne(input+":"+filename);
                 if(dboResult != null) {
                     return translator.fromDBObject(dboResult);
                 }
                 return null;
             }
         }), Predicates.notNull());
         addEntry(result, filename, sortByType(fileResults));
     }
 
     private List<FTPUploadResult> sortByType(Iterable<FTPUploadResult> fileResults) {
         return Ordering.from(new Comparator<FTPUploadResult>() {
             @Override
             public int compare(FTPUploadResult r1, FTPUploadResult r2) {
                 return r1.type().compareTo(r2.type());
             }
         }).immutableSortedCopy(fileResults);
     }
 
     private void addEntry(ProbeResult result, String key, List<FTPUploadResult> results) {
         if(results.isEmpty()) {
             result.addInfo(key, "No Data.");
             return;
         }
         String value = buildValue(results);
         FTPUploadResultType first = sortByDate(results).get(0).type();
         if(FTPUploadResultType.UNKNOWN.equals(first)) {
             result.addInfo(key, value);
         } else {
             result.add(key, value, FTPUploadResultType.SUCCESS.equals(first));
         }
     }
 
     private List<FTPUploadResult> sortByDate(Iterable<FTPUploadResult> results) {
         return Ordering.from(new Comparator<FTPUploadResult>() {
             @Override
             public int compare(FTPUploadResult r1, FTPUploadResult r2) {
                 return r2.uploadTime().compareTo(r1.uploadTime());
             }
         }).immutableSortedCopy(results);
     }
 
     private String buildValue(List<FTPUploadResult> results) {
         StringBuilder builder = new StringBuilder("<table>");
        for(FTPUploadResult result : Iterables.limit(results,2)) {
             builder.append("<tr><td>Last ");
             builder.append(result.type().toNiceString());
             builder.append(": ");
             builder.append(result.uploadTime().toString(DATE_TIME));
             builder.append("</td><td>");
             if(result.message() != null) {
                 builder.append(result.message());
             }
             builder.append("</td></tr>");
         }
         return builder.append("</table>").toString();
     }
     
     @Override
     public String title() {
         return title;
     }
 
     @Override
     public String slug() {
         return "ukrp"+title;
     }
 
 }
