 package org.atlasapi.remotesite.five;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.concurrent.TimeUnit;
 
 import nu.xom.Builder;
 import nu.xom.Element;
 import nu.xom.NodeFactory;
 import nu.xom.Nodes;
 
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.channel4.RequestLimitingRemoteSiteClient;
 
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.HttpResponse;
 import com.metabroadcast.common.http.HttpResponsePrologue;
 import com.metabroadcast.common.http.HttpResponseTransformer;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpClientBuilder;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.metabroadcast.common.time.SystemClock;
 import com.metabroadcast.common.time.Timestamp;
 import com.metabroadcast.common.time.Timestamper;
 
 public class FiveUpdater extends ScheduledTask {
     
     private static final String BASE_API_URL = "https://pdb.five.tv/internal";
     
     private final AdapterLog log;
     private final FiveBrandProcessor processor;
     private final Timestamper timestamper = new SystemClock();
 
     private final Builder parser = new Builder(new ShowProcessingNodeFactory());
     private SimpleHttpClient streamHttpClient;
 
     public FiveUpdater(ContentWriter contentWriter, AdapterLog log) {
         this.log = log;
         this.streamHttpClient = buildFetcher(log);
         this.processor = new FiveBrandProcessor(contentWriter, log, BASE_API_URL, new RequestLimitingRemoteSiteClient<HttpResponse>(new HttpRemoteSiteClient(HttpClients.webserviceClient()), 4));
     }
 
     private SimpleHttpClient buildFetcher(final AdapterLog log) {
         return new SimpleHttpClientBuilder()
             .withUserAgent(HttpClients.ATLAS_USER_AGENT)
             .withSocketTimeout(30, TimeUnit.SECONDS)
             .withRetries(3)
             .build();
     }
     
     private final HttpResponseTransformer<Void> TRANSFORMER = new HttpResponseTransformer<Void>() {
         @Override
         public Void transform(HttpResponsePrologue response, InputStream in) throws HttpException, IOException {
             try {
                 parser.build(in);
             } catch (Exception e) {
                 log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing shows document"));
             }
             return null;
         }
     };
 
     @Override
     public void runTask() {
         try {
             Timestamp start = timestamper.timestamp();
             log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update started from " + BASE_API_URL).withSource(getClass()));
             
             streamHttpClient.get(new SimpleHttpRequest<Void>(BASE_API_URL + "/shows", TRANSFORMER));
             
             Timestamp end = timestamper.timestamp();
             log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
         }
         catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing shows document"));
         }
     }
     
     private class ShowProcessingNodeFactory extends NodeFactory {
 
         int processed = 0, failed = 0;
         
         @Override
         public Nodes finishMakingElement(Element element) {
             if (element.getLocalName().equalsIgnoreCase("show")) {
                 try {
                     processor.processShow(element);
                 }
                 catch (Exception e) {
                     log.record(new AdapterLogEntry(Severity.ERROR).withSource(FiveUpdater.class).withCause(e).withDescription("Exception when processing show"));
                     failed++;
                 }
                 reportStatus(String.format("%s processed. %s failed", ++processed, failed));
             } else if (element.getLocalName().equalsIgnoreCase("shows")){
                 processed = 0;
                 failed = 0;
             }
             
             return super.finishMakingElement(element);
         }
     }
 }
