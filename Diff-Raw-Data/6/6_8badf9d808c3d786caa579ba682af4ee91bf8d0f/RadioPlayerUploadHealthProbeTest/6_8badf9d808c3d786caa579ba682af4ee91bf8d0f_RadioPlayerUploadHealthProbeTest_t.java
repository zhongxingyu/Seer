 package org.atlasapi.feeds.radioplayer.upload;
 
 import static org.atlasapi.feeds.radioplayer.upload.FileType.PI;
 import static org.atlasapi.feeds.upload.FileUploadResult.FileUploadResultType.FAILURE;
 import static org.atlasapi.feeds.upload.FileUploadResult.FileUploadResultType.SUCCESS;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
 import org.atlasapi.feeds.upload.FileUploadResult;
 import org.atlasapi.feeds.upload.FileUploadResult.FileUploadResultType;
 import org.atlasapi.feeds.upload.persistence.MongoFileUploadResultStore;
 import org.atlasapi.media.entity.Publisher;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.health.ProbeResult;
 import com.metabroadcast.common.health.ProbeResult.ProbeResultType;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.DayRangeGenerator;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 
 public class RadioPlayerUploadHealthProbeTest {
 
     private static final Publisher PUBLISHER = Publisher.BBC;
     private static final String REMOTE_SERVICE_ID = "remote";
     private static final String DATE_FORMAT = "yyyyMMdd";
     private static final RadioPlayerService SERVICE = RadioPlayerServices.all.get("340");
     
     //TODO: remove mongo and mock RadioPlayerUploadResultStore
     private final static DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
     private final RadioPlayerUploadResultStore recorder = new UploadResultStoreBackedRadioPlayerResultStore(new MongoFileUploadResultStore(mongo));
     private final RadioPlayerUploadHealthProbe probe = new RadioPlayerUploadHealthProbe(REMOTE_SERVICE_ID, PUBLISHER, recorder, SERVICE, new DayRangeGenerator().withLookAhead(0).withLookBack(0));
     
     @BeforeClass
     public static void setup() {
         mongo.collection("uploads").ensureIndex(new BasicDBObjectBuilder().add("service",1).add("id", 1).add("time", -1).get());
     }
     
     @After
     public void tearDown() {
         mongo.collection("uploads").remove(new BasicDBObject());
     }
     
     @Test
     public void testProbe() {
 
         ProbeResult result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(3)));
         assertThat(Iterables.getFirst(result.entries(), null).getType(), is(equalTo(ProbeResultType.INFO)));
         assertThat(Iterables.getFirst(result.entries(), null).getValue(), is(equalTo("No Data")));
         
         recorder.record(successfulResult(new DateTime(DateTimeZones.UTC)));
         
         result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(3)));
         assertThat(Iterables.getFirst(result.entries(), null).getType(), is(equalTo(ProbeResultType.SUCCESS)));
         
         recorder.record(failedResult(new DateTime(DateTimeZones.UTC)));
          
         result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(3)));
         assertThat(Iterables.getFirst(result.entries(), null).getType(), is(equalTo(ProbeResultType.FAILURE)));
         
         recorder.record(successfulResult(new DateTime(DateTimeZones.UTC)));
         
         result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(3)));
         assertThat(Iterables.getFirst(result.entries(), null).getType(), is(equalTo(ProbeResultType.SUCCESS)));    
     }
 
     @Test
     public void testSuccessTimesOut() {
      
        recorder.record(successfulResult(new DateTime(DateTimeZones.UTC).minusMinutes(1065)));
         
         ProbeResult result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(3)));
         assertThat(Iterables.getFirst(result.entries(), null).getType(), is(equalTo(ProbeResultType.FAILURE)));
         
     }
     
     @Test
     public void testFutureFailureIsInfo() {
         
         RadioPlayerUploadHealthProbe probe = new RadioPlayerUploadHealthProbe(REMOTE_SERVICE_ID, PUBLISHER, recorder, SERVICE, new DayRangeGenerator().withLookAhead(4).withLookBack(0));
 
         
         DateTime futureDay = new DateTime(DateTimeZones.UTC).plusDays(4);
         FileUploadResult upResult = new FileUploadResult(REMOTE_SERVICE_ID, String.format("%s_%s_PI.xml", futureDay.toString(DATE_FORMAT), SERVICE.getRadioplayerId()), new DateTime(DateTimeZones.UTC), FAILURE);
         RadioPlayerUploadResult rpResult = new RadioPlayerUploadResult(PI, SERVICE, futureDay.toLocalDate(), upResult);
         
         recorder.record(rpResult);
         
         ProbeResult result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(11)));
         assertThat(Iterables.get(result.entries(), 10).getType(), is(equalTo(ProbeResultType.INFO)));
     }
     
     public RadioPlayerUploadResult successfulResult(DateTime successDate) {
         return result(successDate, SUCCESS);
     }
     
     public RadioPlayerUploadResult failedResult(DateTime successDate) {
         return result(successDate, FAILURE);
     }
 
     public RadioPlayerUploadResult result(DateTime successDate, FileUploadResultType type) {
        return new RadioPlayerUploadResult(PI, SERVICE, successDate.toLocalDate(), 
                new FileUploadResult(REMOTE_SERVICE_ID, String.format("%s_%s_PI.xml", successDate.toString(DATE_FORMAT), SERVICE.getRadioplayerId()), successDate, type).withRemoteProcessingResult(SUCCESS).withTransactionId("123"));
     }
 }
