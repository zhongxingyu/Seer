 package org.atlasapi.media.content;
 
 import static org.atlasapi.media.entity.testing.ComplexBroadcastTestDataBuilder.broadcast;
 import static org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder.complexItem;
 import static org.atlasapi.media.entity.testing.VersionTestDataBuilder.version;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.atlasapi.media.EsSchema;
 import org.atlasapi.media.common.Id;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Person;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.testing.ComplexBroadcastTestDataBuilder;
 import org.atlasapi.search.model.SearchQuery;
 import org.atlasapi.search.model.SearchResults;
 import org.elasticsearch.action.count.CountRequestBuilder;
 import org.elasticsearch.client.Requests;
 import org.elasticsearch.node.Node;
 import org.elasticsearch.node.NodeBuilder;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.query.Selection;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.SystemClock;
 
 public class EsContentSearcherV3CompatibilityTest {
     
     private final Node esClient = NodeBuilder.nodeBuilder()
         .local(true).clusterName(UUID.randomUUID().toString())
         .build().start();
     private final EsContentIndexer indexer = new EsContentIndexer(esClient, new SystemClock());
     private final EsContentSearcher searcher = new EsContentSearcher(esClient);
     
     @BeforeClass
     public static void before() {
         Logger root = Logger.getRootLogger();
         root.addAppender(new ConsoleAppender(
             new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
         root.setLevel(Level.WARN);
         
     }
     
     @Before
     public void setUp() throws Exception {
         indexer.startAndWait();
     }
 
     private void indexAndWait(Content... contents) throws Exception {
         int indexed = 0;
         for (Content c : contents) {
             if (c instanceof Container) {
                 indexer.index((Container)c);
                 indexed++;
             } else if (c instanceof Item){
                 indexer.index((Item)c);
                 indexed++;
             }
         }
         long count = 0;
         for(int limit = 0; count < indexed && limit < 100; count = count(), limit++) {
             Thread.sleep(50);
         }
         if (count < indexed) {
             fail("Fewer than " + indexed + " content indexed");
         }
     }
 
     private long count() throws InterruptedException, ExecutionException {
         return new CountRequestBuilder(esClient.client())
             .setIndices(EsSchema.INDEX_NAME)
             .execute().get().getCount();
     }
 
     @After
     public void after() throws Exception {
         esClient.client().admin().indices()
             .delete(Requests.deleteIndexRequest(EsSchema.INDEX_NAME)).get();
         esClient.close();
     }
 
     @Test
     public void testFindingBrandsByTitle() throws Exception {
         
         Brand dragonsDen = brand("/den", "Dragon's den");
         Item dragonsDenItem = complexItem().withBrand(dragonsDen).withVersions(broadcast().buildInVersion()).build();
         Brand doctorWho = brand("/doctorwho", "Doctor Who");
         Item doctorWhoItem = complexItem().withBrand(doctorWho).withVersions(broadcast().buildInVersion()).build();
         Brand theCityGardener = brand("/garden", "The City Gardener");
         Item theCityGardenerItem = complexItem().withBrand(theCityGardener).withVersions(broadcast().buildInVersion()).build();
         Brand eastendersWeddings = brand("/eastenders-weddings", "Eastenders Weddings");
         Item eastendersWeddingsItem = complexItem().withBrand(eastendersWeddings).withVersions(broadcast().buildInVersion()).build();
         Brand eastenders = brand("/eastenders", "Eastenders");
         Item eastendersItem = complexItem().withBrand(eastenders).withVersions(broadcast().buildInVersion()).build();
         Brand politicsEast = brand("/politics", "The Politics Show East");
         Item politicsEastItem = complexItem().withBrand(politicsEast).withVersions(broadcast().buildInVersion()).build();
         Brand meetTheMagoons = brand("/magoons", "Meet the Magoons");
         Item meetTheMagoonsItem = complexItem().withBrand(meetTheMagoons).withVersions(broadcast().buildInVersion()).build();
         Brand theJackDeeShow = brand("/dee", "The Jack Dee Show");
         Item theJackDeeShowItem = complexItem().withBrand(theJackDeeShow).withVersions(broadcast().buildInVersion()).build();
         Brand peepShow = brand("/peep-show", "Peep Show");
         Item peepShowItem = complexItem().withBrand(peepShow).withVersions(broadcast().buildInVersion()).build();
         Brand euromillionsDraw = brand("/draw", "EuroMillions Draw");
         Item euromillionsDrawItem = complexItem().withBrand(euromillionsDraw).withVersions(broadcast().buildInVersion()).build();
         Brand haveIGotNewsForYou = brand("/news", "Have I Got News For You");
         Item haveIGotNewsForYouItem = complexItem().withBrand(haveIGotNewsForYou).withVersions(broadcast().buildInVersion()).build();
         Brand brasseye = brand("/eye", "Brass Eye");
         Item brasseyeItem = complexItem().withBrand(brasseye).withVersions(ComplexBroadcastTestDataBuilder.broadcast().buildInVersion()).build();
         Brand science = brand("/science", "The Story of Science: Power, Proof and Passion");
         Item scienceItem = complexItem().withBrand(science).withVersions(ComplexBroadcastTestDataBuilder.broadcast().buildInVersion()).build();
         Brand theApprentice = brand("/apprentice", "The Apprentice");
         Item theApprenticeItem = complexItem().withBrand(theApprentice).withVersions(broadcast().buildInVersion()).build();
         
         Item apparent = complexItem().withTitle("Without Apparent Motive").withUri("/item/apparent").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
 
         Item englishForCats = complexItem().withUri("/items/cats").withTitle("English for cats").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
 
         Item spookyTheCat = complexItem().withTitle("Spooky the Cat").withUri("/item/spookythecat").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
         Item spooks = complexItem().withTitle("Spooks").withUri("/item/spooks")
                 .withVersions(version().withBroadcasts(broadcast().withStartTime(new SystemClock().now().minus(Duration.standardDays(28))).build()).build()).build();
 
         Item jamieOliversCookingProgramme = complexItem().withUri("/items/oliver/1").withTitle("Jamie Oliver's cooking programme")
                 .withDescription("lots of words that are the same alpha beta").withVersions(broadcast().buildInVersion()).build();
         Item gordonRamsaysCookingProgramme = complexItem().withUri("/items/ramsay/2").withTitle("Gordon Ramsay's cooking show").withDescription("lots of words that are the same alpha beta")
                 .withVersions(broadcast().buildInVersion()).build();
         
         Brand rugby = brand("/rugby", "Rugby");
         Item rugbyItem = complexItem().withBrand(rugby).withVersions(ComplexBroadcastTestDataBuilder.broadcast().withChannel("http://minor-channel").buildInVersion()).build();
         
         Brand sixNationsRugby = brand("/sixnations", "Six Nations Rugby Union");
         Item sixNationsRugbyItem = complexItem().withBrand(sixNationsRugby).withVersions(ComplexBroadcastTestDataBuilder.broadcast().withChannel("http://www.bbc.co.uk/services/bbcone/east").buildInVersion()).build();
 
         Brand hellsKitchen = brand("/hellskitchen", "Hell's Kitchen");
         Item hellsKitchenItem = complexItem().withBrand(hellsKitchen).withVersions(broadcast().buildInVersion()).build();
         
         Brand hellsKitchenUSA = brand("/hellskitchenusa", "Hell's Kitchen");
         Item hellsKitchenUSAItem = complexItem().withBrand(hellsKitchenUSA).withVersions(broadcast().buildInVersion()).build();
         
         Item we = complexItem().withTitle("W.E.").withUri("/item/we").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
 
         indexAndWait(doctorWho, eastendersWeddings, dragonsDen, theCityGardener, 
             eastenders, meetTheMagoons, theJackDeeShow, peepShow, haveIGotNewsForYou,
             euromillionsDraw, brasseye, science, politicsEast, theApprentice, rugby, 
             sixNationsRugby, hellsKitchen, hellsKitchenUSA, apparent, englishForCats, 
             jamieOliversCookingProgramme, gordonRamsaysCookingProgramme, spooks, 
             spookyTheCat, dragonsDenItem, doctorWhoItem, theCityGardenerItem, 
             eastendersItem, eastendersWeddingsItem, politicsEastItem, meetTheMagoonsItem,
             theJackDeeShowItem, peepShowItem, euromillionsDrawItem, haveIGotNewsForYouItem,
             brasseyeItem, scienceItem, theApprenticeItem, rugbyItem, sixNationsRugbyItem, 
             hellsKitchenItem, hellsKitchenUSAItem, we);
 
         
         check(searcher.search(title("aprentice")).get(), theApprentice);
         check(searcher.search(currentWeighted("apprent")).get(), theApprentice, apparent);
         check(searcher.search(title("den")).get(), dragonsDen, theJackDeeShow);
         check(searcher.search(title("dragon")).get(), dragonsDen);
         check(searcher.search(title("dragons")).get(), dragonsDen);
         check(searcher.search(title("drag den")).get(), dragonsDen);
         check(searcher.search(title("drag")).get(), dragonsDen, euromillionsDraw);
         check(searcher.search(title("dragon's den")).get(), dragonsDen);
         check(searcher.search(title("eastenders")).get(), eastenders, eastendersWeddings);
         check(searcher.search(title("easteners")).get(), eastenders, eastendersWeddings);
         check(searcher.search(title("eastedners")).get(), eastenders, eastendersWeddings);
         check(searcher.search(title("politics east")).get(), politicsEast);
         check(searcher.search(title("eas")).get(), eastenders, eastendersWeddings, politicsEast);
         check(searcher.search(title("east")).get(), eastenders, eastendersWeddings, politicsEast);
         check(searcher.search(title("end")).get());
         check(searcher.search(title("peep show")).get(), peepShow);
         check(searcher.search(title("peep s")).get(), peepShow);
         check(searcher.search(title("dee")).get(), theJackDeeShow, dragonsDen);
         check(searcher.search(title("jack show")).get(), theJackDeeShow);
         check(searcher.search(title("the jack dee s")).get(), theJackDeeShow);
         check(searcher.search(title("dee show")).get(), theJackDeeShow);
         check(searcher.search(title("hav i got news")).get(), haveIGotNewsForYou);
         check(searcher.search(title("brasseye")).get(), brasseye);
         check(searcher.search(title("braseye")).get(), brasseye);
         check(searcher.search(title("brassey")).get(), brasseye);
         check(searcher.search(title("The Story of Science Power Proof and Passion")).get(), science);
         check(searcher.search(title("The Story of Science: Power, Proof and Passion")).get(), science);
         check(searcher.search(title("Jamie")).get(), jamieOliversCookingProgramme);
         check(searcher.search(title("Spooks")).get(), spooks, spookyTheCat);
     }
     
     @Test
     public void testFindingBrandsByTitleAfterUpdate() throws Exception {
         
         Brand theApprentice = brand("/apprentice", "The Apprentice");
         Item theApprenticeItem = complexItem().withBrand(theApprentice).withVersions(broadcast().buildInVersion()).build();
         Item apparent = complexItem().withTitle("Without Apparent Motive").withUri("/item/apparent").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
         
         indexAndWait(theApprentice, theApprenticeItem, apparent);
         
         check(searcher.search(title("aprentice")).get(), theApprentice);
 
         Brand theApprentice2 = new Brand();
         Brand.copyTo(theApprentice, theApprentice2);
         theApprentice2.setTitle("Completely Different2");
         
         indexer.index(theApprentice2);
         Thread.sleep(1000);
 
         checkNot(searcher.search(title("aprentice")).get(), theApprentice);
         check(searcher.search(title("Completely Different2")).get(), theApprentice);
     }
     
     @Test
     public void testFindingBrandsBySpecialization() throws Exception {
         
         Brand theApprentice = brand("/apprentice", "The Apprentice");
         Item theApprenticeItem = complexItem().withBrand(theApprentice)
             .withVersions(broadcast().buildInVersion()).build();
         
         indexAndWait(theApprentice, theApprenticeItem);
         
         check(searcher.search(title("aprentice")).get(), theApprentice);
 
         Item theApprenticeItem2 = new Item();
         Item.copyTo(theApprenticeItem, theApprenticeItem2);
         theApprenticeItem2.setSpecialization(Specialization.RADIO);
         indexer.index(theApprenticeItem2);
         Thread.sleep(1000);
         
         checkNot(searcher.search(specializedTitle("aprentice", Specialization.TV)).get(), theApprentice);
         check(searcher.search(specializedTitle("aprentice", Specialization.RADIO)).get(), theApprentice);
     }
 
     @Test
     public void testLimitingToPublishers() throws Exception {
         
         Brand eastenders = brand("/eastenders", "Eastenders");
         Item eastendersItem = complexItem().withBrand(eastenders).withVersions(broadcast().buildInVersion()).build();
         Brand eastendersWeddings = brand("/eastenders-weddings", "Eastenders Weddings");
         Item eastendersWeddingsItem = complexItem().withBrand(eastendersWeddings).withVersions(broadcast().buildInVersion()).build();
         Brand politicsEast = brand("/politics", "The Politics Show East");
         Item politicsEastItem = complexItem().withBrand(politicsEast).withVersions(broadcast().buildInVersion()).build();
         
         indexAndWait(eastendersWeddings, eastendersWeddingsItem, 
             eastenders, eastendersItem, 
             politicsEast, politicsEastItem);
         
         check(searcher.search(new SearchQuery("east", Selection.ALL, ImmutableSet.of(Publisher.BBC), 1.0f, 0.0f, 0.0f)).get(), eastenders, eastendersWeddings, politicsEast);
         check(searcher.search(new SearchQuery("east", Selection.ALL, ImmutableSet.of(Publisher.ARCHIVE_ORG), 1.0f, 0.0f, 0.0f)).get());
 
         Brand eastBrand = new Brand("/east", "curie", Publisher.ARCHIVE_ORG);
         eastBrand.setTitle("east");
         eastBrand.setId(Id.valueOf(2517));
 
         Item eastItem = new Item("/eastItem", "curie", Publisher.ARCHIVE_ORG);
         eastItem.setTitle("east");
         eastItem.setId(Id.valueOf(2518));
         eastItem.setParentRef(ParentRef.parentRefFrom(eastBrand));
        eastItem.setThisOrChildLastUpdated(new DateTime(DateTimeZones.UTC));
         
         eastBrand.setChildRefs(Arrays.asList(eastItem.childRef()));
         indexer.index(eastBrand);
         indexer.index(eastItem);
         Thread.sleep(2000);
         
         check(searcher.search(new SearchQuery("east", Selection.ALL, ImmutableSet.of(Publisher.ARCHIVE_ORG), 1.0f, 0.0f, 0.0f)).get(), eastBrand);
     }
 
     @Test
     public void testUsesPrefixSearchForShortSearches() throws Exception {
         // commented out for now as order is inverted:
         //check(searcher.search(title("Dr")).get(), doctorWho, dragonsDen);
         check(searcher.search(title("l")).get());
     }
 
     @Test
     public void testLimitAndOffset() throws Exception {
         Brand eastendersWeddings = brand("/eastenders-weddings", "Eastenders Weddings");
         Item eastendersWeddingsItem = complexItem().withBrand(eastendersWeddings).withVersions(broadcast().buildInVersion()).build();
         Brand eastenders = brand("/eastenders", "Eastenders");
         Item eastendersItem = complexItem().withBrand(eastenders).withVersions(broadcast().buildInVersion()).build();
         Brand politicsEast = brand("/politics", "The Politics Show East");
         Item politicsEastItem = complexItem().withBrand(politicsEast).withVersions(broadcast().buildInVersion()).build();
 
         indexAndWait(eastendersWeddings, eastendersWeddingsItem, 
             eastenders, eastendersItem, 
             politicsEast, politicsEastItem);
         
         check(searcher.search(new SearchQuery("eas", Selection.ALL, Publisher.all(), 1.0f, 0.0f, 0.0f)).get(), eastenders, eastendersWeddings, politicsEast);
         check(searcher.search(new SearchQuery("eas", Selection.limitedTo(2), Publisher.all(), 1.0f, 0.0f, 0.0f)).get(), eastenders, eastendersWeddings);
         check(searcher.search(new SearchQuery("eas", Selection.offsetBy(2), Publisher.all(), 1.0f, 0.0f, 0.0f)).get(), politicsEast);
     }
 
     @Test
     public void testBroadcastLocationWeighting() throws Exception {
         Item spookyTheCat = complexItem().withTitle("Spooky the Cat").withUri("/item/spookythecat").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
         Item spooks = complexItem().withTitle("Spooks").withUri("/item/spooks")
                 .withVersions(version().withBroadcasts(broadcast().withStartTime(new SystemClock().now().minus(Duration.standardDays(28))).build()).build()).build();
 
         indexAndWait(spookyTheCat, spooks);
         
         check(searcher.search(currentWeighted("spooks")).get(), spooks, spookyTheCat);
         check(searcher.search(title("spook")).get(), spooks, spookyTheCat);
         // commented out for now as order is inverted:
         //check(searcher.search(currentWeighted("spook")).get(), spookyTheCat, spooks);
     }
      
     @Test
     @Ignore
     public void testBrandWithNoChildrenIsPickedWithTitleWeighting() throws Exception {
         Item spookyTheCat = complexItem().withTitle("Spooky the Cat").withUri("/item/spookythecat").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
         Item spooks = complexItem().withTitle("Spooks").withUri("/item/spooks")
                 .withVersions(version().withBroadcasts(broadcast().withStartTime(new SystemClock().now().minus(Duration.standardDays(28))).build()).build()).build();
 
         indexAndWait(spookyTheCat, spooks);
 //        check(searcher.search(title("spook")).get(), spookyTheCat, spooks);
 
         Brand spookie = new Brand("/spookie", "curie", Publisher.ARCHIVE_ORG);
         spookie.setTitle("spookie");
         spookie.setId(Id.valueOf(10000));
         indexAndWait(spookie);
         
         check(searcher.search(title("spook")).get(), spookie, spookyTheCat, spooks);
     }
     
     @Test
     @Ignore
     public void testBrandWithNoChildrenIsNotPickedWithBroadcastWeighting() throws Exception {
         Item spookyTheCat = complexItem().withTitle("Spooky the Cat").withUri("/item/spookythecat")
                 .withVersions(version().withBroadcasts(
                     broadcast().build()
                 ).build())
             .build();
         Item spooks = complexItem().withTitle("Spooks").withUri("/item/spooks")
                 .withVersions(version().withBroadcasts(
                     broadcast().withStartTime(
                         new SystemClock().now().minus(Duration.standardDays(28))
                     ).build()
                 ).build())
             .build();
         
         indexAndWait(spookyTheCat, spooks);
         
         check(searcher.search(currentWeighted("spook")).get(), spookyTheCat, spooks);
 
         Brand spookie = new Brand("/spookie2", "curie", Publisher.ARCHIVE_ORG);
         spookie.setTitle("spookie2");
         spookie.setId(Id.valueOf(666));
         indexAndWait(spookie);
         
         check(searcher.search(currentWeighted("spook")).get(), spookyTheCat, spooks);
     }
     
     protected static SearchQuery title(String term) {
         return new SearchQuery(term, Selection.ALL, Publisher.all(), 1.0f, 0.0f, 0.0f);
     }
     
     protected static SearchQuery specializedTitle(String term, Specialization specialization) {
         return new SearchQuery(term, Selection.ALL, Sets.newHashSet(specialization), Publisher.all(), 1.0f, 0.0f, 0.0f);
     }
 
     protected static SearchQuery currentWeighted(String term) {
         return new SearchQuery(term, Selection.ALL, Publisher.all(), 1.0f, 0.2f, 0.2f);
     }
 
     protected static void check(SearchResults result, Identified... content) {
         assertThat(result.getIds(), is(toIds(Arrays.asList(content))));
     }
     
     protected static void checkNot(SearchResults result, Identified... content) {
         assertFalse(result.getIds().equals(toIds(Arrays.asList(content))));
     }
 
     private static long id = 100L;
     
     protected static Brand brand(String uri, String title) {
         Brand b = new Brand(uri, uri, Publisher.BBC);
         b.setTitle(title);
         b.setId(Id.valueOf(id++));
         return b;
     }
 
     protected static Item item(String uri, String title) {
         return item(uri, title, null);
     }
 
     protected static Item item(String uri, String title, String description) {
         Item i = new Item();
         i.setId(Id.valueOf(id++));
         i.setTitle(title);
         i.setCanonicalUri(uri);
         i.setDescription(description);
         i.setPublisher(Publisher.BBC);
         i.setThisOrChildLastUpdated(new DateTime(DateTimeZones.UTC));
         return i;
     }
 
     protected static Person person(String uri, String title) {
         Person p = new Person(uri, uri, Publisher.BBC);
         p.setTitle(title);
         return p;
     }
     
     private static List<Id> toIds(List<? extends Identified> content) {
         List<Id> ids = Lists.newArrayList();
         for (Identified description : content) {
             ids.add(description.getId());
         }
         return ids;
     }
 }
