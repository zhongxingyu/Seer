 package org.atlasapi.remotesite.channel4.epg;
 
 import static org.hamcrest.Matchers.endsWith;
 
 import java.io.InputStreamReader;
 
 import junit.framework.TestCase;
 import nu.xom.Builder;
 import nu.xom.Document;
 import nu.xom.Element;
 
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.channel4.epg.C4EpgBrandlessEntryProcessor;
 import org.atlasapi.remotesite.channel4.epg.C4EpgElementFactory;
 import org.atlasapi.remotesite.channel4.epg.C4EpgEntryProcessor;
 import org.atlasapi.remotesite.channel4.epg.C4EpgUpdater;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.DayRangeGenerator;
 
 public class C4EpgUpdaterTest extends TestCase {
 
     public void testRun() throws Exception {
         
         Mockery context = new Mockery();
         
         Builder builder = new Builder(new C4EpgElementFactory());
         final Document c4EpgFeed = builder.build(new InputStreamReader(Resources.getResource("c4-epg-2011-01-07.atom").openStream()));
         
         @SuppressWarnings("unchecked")
         final RemoteSiteClient<Document> c4AtomFetcher = context.mock(RemoteSiteClient.class);
         final ContentWriter contentWriter = context.mock(ContentWriter.class);
         final ContentResolver contentStore = context.mock(ContentResolver.class);
         final KnownTypeQueryExecutor queryExecutor = context.mock(KnownTypeQueryExecutor.class);
         
         AdapterLog log = new NullAdapterLog();
         
         BroadcastTrimmer trimmer = new BroadcastTrimmer(Publisher.C4, queryExecutor, contentWriter, log);
         
         C4EpgUpdater updater = new C4EpgUpdater(c4AtomFetcher, new C4EpgEntryProcessor(contentWriter, contentStore, log), new C4EpgBrandlessEntryProcessor(contentWriter, contentStore, log), trimmer, log, new DayRangeGenerator());
         
         context.checking(new Expectations() {{
             one(c4AtomFetcher).get(with(endsWith(String.format("%s/C4.atom", new DateTime(DateTimeZones.UTC).toString("yyyy/MM/dd")))));
                 will(returnValue(c4EpgFeed));
             allowing(c4AtomFetcher).get(with(any(String.class)));
                 will(returnValue(new Document(new Element("feed"))));
             allowing(contentStore).findByCanonicalUri(with(any(String.class)));
                 will(returnValue(null));
             allowing(contentWriter).createOrUpdate(with(any(Container.class)), with(true));
            allowing(queryExecutor).schedule(with(any(ContentQuery.class))); will(returnValue(Schedule.fromItems(new Interval(new DateTime(),new DateTime()), ImmutableList.<Item>of())));
         }});
         
         updater.run();
         
     }
 }
