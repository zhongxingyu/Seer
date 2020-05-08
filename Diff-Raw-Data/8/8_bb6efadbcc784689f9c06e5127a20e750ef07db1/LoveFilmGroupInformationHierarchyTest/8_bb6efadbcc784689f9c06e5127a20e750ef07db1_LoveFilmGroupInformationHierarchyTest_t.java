 package org.atlasapi.feeds.youview;
 
 import static org.hamcrest.Matchers.isOneOf;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.atlasapi.feeds.tvanytime.DefaultTvAnytimeGenerator;
 import org.atlasapi.feeds.tvanytime.GroupInformationGenerator;
 import org.atlasapi.feeds.tvanytime.OnDemandLocationGenerator;
 import org.atlasapi.feeds.tvanytime.ProgramInformationGenerator;
 import org.atlasapi.feeds.tvanytime.TvAnytimeGenerator;
 import org.atlasapi.media.entity.Alias;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Certificate;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.SortKey;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.content.ResolvedContent.ResolvedContentBuilder;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 import org.junit.Test;
 
 import tva.metadata._2010.BaseMemberOfType;
 import tva.metadata._2010.BasicContentDescriptionType;
 import tva.metadata._2010.GroupInformationTableType;
 import tva.metadata._2010.GroupInformationType;
 import tva.metadata._2010.ProgramGroupTypeType;
 import tva.metadata._2010.RelatedMaterialType;
 import tva.metadata._2010.TVAMainType;
 import tva.mpeg7._2008.MediaLocatorType;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSortedMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.intl.Countries;
 
 public class LoveFilmGroupInformationHierarchyTest {
 
     private static final YouViewGenreMapping genreMapping = new YouViewGenreMapping(); 
     private static final ProgramInformationGenerator progInfoGenerator = new LoveFilmProgramInformationGenerator();
     private static final GroupInformationGenerator groupInfoGenerator = new LoveFilmGroupInformationGenerator(genreMapping);
     private static final OnDemandLocationGenerator progLocationGenerator = new LoveFilmOnDemandLocationGenerator();
     private static final DummyContentResolver contentResolver = new DummyContentResolver();
     
     private static final TvAnytimeGenerator generator = new DefaultTvAnytimeGenerator(
         progInfoGenerator, 
         groupInfoGenerator, 
         progLocationGenerator, 
         contentResolver,
         false
     );
     
     @Test
     public void testSkipsAsinIfNoItem() throws JAXBException {
         Film film = createFilm("Film Uri");
         film.setImage("Film Image");
         
         TVAMainType tvaMain = convertToXmlAndBack(film);
         
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
         
         assert(groupInfoTable.getGroupInformation().isEmpty());
     }
     
     @Test
     public void testFilmGeneration() throws JAXBException {
         Film film = createFilm("Film Uri");
         film.setImage("Film Image");
         film.addAlias(new Alias("gb:amazon:asin", "123456"));
         
         TVAMainType tvaMain = convertToXmlAndBack(film);
         
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
         
         GroupInformationType groupInfo = Iterables.getOnlyElement(groupInfoTable.getGroupInformation());
         
         assertEquals("Film Image", getImage(groupInfo));
         assertEquals("crid://lovefilm.com/product/123456", groupInfo.getGroupId());
     }
     
     @Test
     public void testBrandGeneration() throws JAXBException {
         Brand brand = createBrand("Brand Uri");
         brand.setImage("Brand Image");
         brand.addAlias(new Alias("gb:amazon:asin", "123456"));
         
         Episode episode = createEpisode("Episode Uri");
         episode.setImage("Episode Image");
         brand.setChildRefs(ImmutableList.of(episode.childRef()));
 
         contentResolver.addContent(episode);
         
        TVAMainType tvaMain = convertToXmlAndBack(brand);
        
        GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
        
        GroupInformationType groupInfo = Iterables.getOnlyElement(groupInfoTable.getGroupInformation());
        
        assertEquals("Episode Image", getImage(groupInfo));
        assertEquals("crid://lovefilm.com/product/123456", groupInfo.getGroupId());
     }
     
     @Test
     public void testSeriesGeneration() throws JAXBException {
         Series series = createSeries("Series Uri");
         series.setImage("Series Image");
         series.addAlias(new Alias("gb:amazon:asin", "123456"));
 
         Episode episode = createEpisode("Episode Uri");
         episode.setImage("Episode Image");
         series.setChildRefs(ImmutableList.of(episode.childRef()));
 
         contentResolver.addContent(episode);
 
         TVAMainType tvaMain = convertToXmlAndBack(series);
 
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
 
         GroupInformationType groupInfo = Iterables.getOnlyElement(groupInfoTable.getGroupInformation());
 
         assertEquals("Episode Image", getImage(groupInfo));
         assertEquals("crid://lovefilm.com/product/123456", groupInfo.getGroupId());
     }
     
     @Test
     public void testSeriesGenerationWithBrand() throws JAXBException {
         Series series = createSeries("Series Uri");
         series.setImage("Series Image");
         series.addAlias(new Alias("gb:amazon:asin", "seriesAsin"));
         
         Brand brand = createBrand("Brand Uri");
         brand.setImage("Brand Image");
         brand.addAlias(new Alias("gb:amazon:asin", "brandAsin"));
         
         Episode episode = createEpisode("Episode Uri");
         episode.setImage("Episode Image");
         
         series.setChildRefs(ImmutableList.of(episode.childRef()));
         series.setParent(brand);
         
         brand.setChildRefs(ImmutableList.of(episode.childRef()));
         brand.setSeriesRefs(ImmutableList.of(series.seriesRef()));
 
         contentResolver.addContent(episode);
         contentResolver.addContent(brand);
         
        TVAMainType tvaMain = convertToXmlAndBack(series);
        
        GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
        
        GroupInformationType firstGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 0);
        GroupInformationType secondGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 1);
        
        assertEquals("Episode Image", getImage(firstGroupInfo));
        assertEquals("Episode Image", getImage(secondGroupInfo));
        
        BaseMemberOfType memberOf = null;
        if (firstGroupInfo.getMemberOf().isEmpty()) {
            memberOf = Iterables.getOnlyElement(secondGroupInfo.getMemberOf());
            assertEquals("crid://lovefilm.com/product/seriesAsin", secondGroupInfo.getGroupId());
        } else {
            memberOf = Iterables.getOnlyElement(firstGroupInfo.getMemberOf());
            assertEquals("crid://lovefilm.com/product/seriesAsin", firstGroupInfo.getGroupId());
        }
        
        assertEquals("crid://lovefilm.com/product/brandAsin", memberOf.getCrid());
     }
     
     @Test
    public void testSeriesGenerationWithBrandWithNoAsinSucceedsWithNoGroupInformation() throws JAXBException {
         Series series = createSeries("Series Uri");
         series.setImage("Series Image");
         series.addAlias(new Alias("gb:amazon:asin", "seriesAsin"));
         
         Brand brand = createBrand("Brand Uri");
         brand.setImage("Brand Image");
         
         Episode episode = createEpisode("Episode Uri");
         episode.setImage("Episode Image");
         
         series.setChildRefs(ImmutableList.of(episode.childRef()));
         series.setParent(brand);
         
         brand.setChildRefs(ImmutableList.of(episode.childRef()));
        brand.setSeriesRefs(ImmutableList.of(series.seriesRef()));
 
         contentResolver.addContent(episode);
         contentResolver.addContent(brand);
         
         TVAMainType tvaMain = convertToXmlAndBack(series);
 
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
         
        assertTrue(groupInfoTable.getGroupInformation().isEmpty());
     }
     
     @Test
     public void testNonFirstSeriesGenerationWithBrand() throws JAXBException {
         Series series1 = createSeries("Series 1 Uri");
         series1.setImage("Series 1 Image");
         
         Series series2 = createSeries("Series 2 Uri");
         series2.setImage("Series 2 Image");
         series2.addAlias(new Alias("gb:amazon:asin", "series2Asin"));
         
         Brand brand = createBrand("Brand Uri");
         brand.setImage("Brand Image");
         brand.addAlias(new Alias("gb:amazon:asin", "brandAsin"));
         
         Episode episode1S1 = createEpisode("Episode 1 Series 1 Uri");
         episode1S1.setImage("Episode 1 Series 1 Image");
         episode1S1.setEpisodeNumber(1);
         episode1S1.setSeriesNumber(1);
         episode1S1.setSeries(series1);
         episode1S1.setContainer(brand);
         
         Episode episode1S2 = createEpisode("Episode 1 Series 2 Uri");
         episode1S2.setImage("Episode 1 Series 2 Image");
         episode1S2.setEpisodeNumber(1);
         episode1S2.setSeriesNumber(2);
         episode1S2.setSeries(series2);
         episode1S2.setContainer(brand);
 
         series1.setChildRefs(ImmutableList.of(episode1S1.childRef()));
         series1.setParent(brand);
         
         series2.setChildRefs(ImmutableList.of(episode1S2.childRef()));
         series2.setParent(brand);
         
         Map<String, ChildRef> sortedMap = ImmutableSortedMap
             .<String, ChildRef>orderedBy(new SortKey.SortKeyOutputComparator())
             .put(SortKey.keyFrom(episode1S1), episode1S1.childRef())
             .put(SortKey.keyFrom(episode1S2), episode1S2.childRef())
             .build();
         
         brand.setChildRefs(sortedMap.values());
         brand.setSeriesRefs(ImmutableList.of(series1.seriesRef(), series2.seriesRef()));
 
         contentResolver.addContent(episode1S1);
         contentResolver.addContent(episode1S2);
         contentResolver.addContent(brand);
         
        TVAMainType tvaMain = convertToXmlAndBack(series2);
        
        GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
        
        GroupInformationType firstGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 0);
        GroupInformationType secondGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 1);
        // check that series == ep 1 s2
        // and brand == ep 1 s1
        if (isSeries(firstGroupInfo)) {
            assertEquals("Episode 1 Series 2 Image", getImage(firstGroupInfo));
            assertEquals("Episode 1 Series 1 Image", getImage(secondGroupInfo));
        } else {
            assertEquals("Episode 1 Series 1 Image", getImage(firstGroupInfo));
            assertEquals("Episode 1 Series 2 Image", getImage(secondGroupInfo));
        }
        
        BaseMemberOfType memberOf = null;
        if (firstGroupInfo.getMemberOf().isEmpty()) {
            memberOf = Iterables.getOnlyElement(secondGroupInfo.getMemberOf());
            assertEquals("crid://lovefilm.com/product/series2Asin", secondGroupInfo.getGroupId());
        } else {
            memberOf = Iterables.getOnlyElement(firstGroupInfo.getMemberOf());
            assertEquals("crid://lovefilm.com/product/series2Asin", firstGroupInfo.getGroupId());
        }
        
        assertEquals("crid://lovefilm.com/product/brandAsin", memberOf.getCrid());
     }
     
     @Test
     public void testEpisodeGenerationNoSeriesNoBrand() throws JAXBException {
         Episode episode = createEpisode("Episode Uri");
         episode.setImage("Episode Image");
         episode.addAlias(new Alias("gb:amazon:asin", "123456"));
 
         TVAMainType tvaMain = convertToXmlAndBack(episode);
 
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
 
         GroupInformationType groupInfo = Iterables.getOnlyElement(groupInfoTable.getGroupInformation());
 
         assertEquals("Episode Image", getImage(groupInfo));
         
         assertEquals("crid://lovefilm.com/product/123456", groupInfo.getGroupId());
     }
     
     @Test
     public void testEpisodeGenerationNoBrand() throws JAXBException {
         Series series = createSeries("Series Uri");
         series.setImage("Series Image");
         series.addAlias(new Alias("gb:amazon:asin", "seriesAsin"));
         
         Episode episode = createEpisode("Episode Uri");
         episode.setImage("Episode Image");
         episode.setEpisodeNumber(1);
         episode.setSeriesRef(ParentRef.parentRefFrom(series));
         episode.addAlias(new Alias("gb:amazon:asin", "episodeAsin"));
         
         series.setChildRefs(ImmutableList.of(episode.childRef()));
         
         contentResolver.addContent(series);
         contentResolver.addContent(episode);
 
         TVAMainType tvaMain = convertToXmlAndBack(episode);
 
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
 
         GroupInformationType firstGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 0);
         GroupInformationType secondGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 1);
         
         assertEquals("Episode Image", getImage(firstGroupInfo));
         assertEquals("Episode Image", getImage(secondGroupInfo));
         
         BaseMemberOfType memberOf = null;
         if (firstGroupInfo.getMemberOf().isEmpty()) {
             memberOf = Iterables.getOnlyElement(secondGroupInfo.getMemberOf());
             assertEquals("crid://lovefilm.com/product/episodeAsin", secondGroupInfo.getGroupId());
         } else {
             memberOf = Iterables.getOnlyElement(firstGroupInfo.getMemberOf());
             assertEquals("crid://lovefilm.com/product/episodeAsin", firstGroupInfo.getGroupId());
         }
         
         assertEquals("crid://lovefilm.com/product/seriesAsin", memberOf.getCrid());
     }
     
     @Test
     public void testEpisodeGenerationNoSeries() throws JAXBException {
         Brand brand = createBrand("Brand Uri");
         brand.setImage("Brand Image");
         brand.addAlias(new Alias("gb:amazon:asin", "brandAsin"));
         
         Episode episode = createEpisode("Episode No Series Uri");
         episode.setImage("Episode No Series Image");
         episode.setContainer(brand);
         episode.setEpisodeNumber(1);
         episode.addAlias(new Alias("gb:amazon:asin", "episodeAsin"));
 
         brand.setChildRefs(ImmutableList.of(episode.childRef()));
         
         contentResolver.addContent(episode);
         contentResolver.addContent(brand);
 
         TVAMainType tvaMain = convertToXmlAndBack(episode);
         
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
 
         GroupInformationType firstGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 0);
         GroupInformationType secondGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 1);
         
         assertEquals("Episode No Series Image", getImage(firstGroupInfo));
         assertEquals("Episode No Series Image", getImage(secondGroupInfo));
         
         BaseMemberOfType memberOf = null;
         if (firstGroupInfo.getMemberOf().isEmpty()) {
             memberOf = Iterables.getOnlyElement(secondGroupInfo.getMemberOf());
             assertEquals("crid://lovefilm.com/product/episodeAsin", secondGroupInfo.getGroupId());
         } else {
             memberOf = Iterables.getOnlyElement(firstGroupInfo.getMemberOf());
             assertEquals("crid://lovefilm.com/product/episodeAsin", firstGroupInfo.getGroupId());
         }
         
         assertEquals("crid://lovefilm.com/product/brandAsin", memberOf.getCrid());
     }
     
     @Test
     public void testEpisodeGeneration() throws JAXBException {
         Brand brand = createBrand("Brand Uri");
         brand.setImage("Brand Image");
         brand.addAlias(new Alias("gb:amazon:asin", "brandAsin"));
         
         Series series = createSeries("Series Uri");
         series.setImage("Series Image");
         series.addAlias(new Alias("gb:amazon:asin", "seriesAsin"));
         
         Episode episode = createEpisode("Episode Uri");
         episode.setImage("Episode Image");
         episode.setSeriesRef(ParentRef.parentRefFrom(series));
         episode.setContainer(brand);
         episode.setEpisodeNumber(1);
         episode.addAlias(new Alias("gb:amazon:asin", "episodeAsin"));
 
         brand.setChildRefs(ImmutableList.of(episode.childRef()));
         brand.setSeriesRefs(ImmutableList.of(series.seriesRef()));
         series.setChildRefs(ImmutableList.of(episode.childRef()));
         series.setParent(brand);
         
         contentResolver.addContent(episode);
         contentResolver.addContent(series);
         contentResolver.addContent(brand);
 
         TVAMainType tvaMain = convertToXmlAndBack(episode);
 
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
 
         GroupInformationType firstGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 0);
         GroupInformationType secondGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 1);
         GroupInformationType thirdGroupInfo = Iterables.get(groupInfoTable.getGroupInformation(), 2);
         
         assertEquals("Episode Image", getImage(firstGroupInfo));
         assertEquals("Episode Image", getImage(secondGroupInfo));
         assertEquals("Episode Image", getImage(thirdGroupInfo));
         
         if (firstGroupInfo.getMemberOf().isEmpty()) {
             assertThat(Iterables.getOnlyElement(secondGroupInfo.getMemberOf()).getCrid(), isOneOf("crid://lovefilm.com/product/seriesAsin", "crid://lovefilm.com/product/brandAsin"));
             assertThat(Iterables.getOnlyElement(thirdGroupInfo.getMemberOf()).getCrid(), isOneOf("crid://lovefilm.com/product/seriesAsin", "crid://lovefilm.com/product/brandAsin"));
             assertEquals("crid://lovefilm.com/product/brandAsin", firstGroupInfo.getGroupId());
         } else if (secondGroupInfo.getMemberOf().isEmpty()) {
             assertThat(Iterables.getOnlyElement(firstGroupInfo.getMemberOf()).getCrid(), isOneOf("crid://lovefilm.com/product/seriesAsin", "crid://lovefilm.com/product/brandAsin"));
             assertThat(Iterables.getOnlyElement(thirdGroupInfo.getMemberOf()).getCrid(), isOneOf("crid://lovefilm.com/product/seriesAsin", "crid://lovefilm.com/product/brandAsin"));
             assertEquals("crid://lovefilm.com/product/brandAsin", secondGroupInfo.getGroupId());
         } else {
             assertThat(Iterables.getOnlyElement(firstGroupInfo.getMemberOf()).getCrid(), isOneOf("crid://lovefilm.com/product/seriesAsin", "crid://lovefilm.com/product/brandAsin"));
             assertThat(Iterables.getOnlyElement(secondGroupInfo.getMemberOf()).getCrid(), isOneOf("crid://lovefilm.com/product/seriesAsin", "crid://lovefilm.com/product/brandAsin"));
             assertEquals("crid://lovefilm.com/product/brandAsin", thirdGroupInfo.getGroupId());
         }
     }
     
     @Test
     public void testEpisodeGenerationFromNonFirstSeries() throws JAXBException {
         Series series1 = createSeries("Series 1 Uri");
         series1.withSeriesNumber(1);
         series1.setImage("Series 1 Image");
         series1.addAlias(new Alias("gb:amazon:asin", "series1Asin"));
         
         Series series2 = createSeries("Series 2 Uri");
         series2.withSeriesNumber(2);
         series2.setImage("Series 2 Image");
         series2.addAlias(new Alias("gb:amazon:asin", "series2Asin"));
         
         Brand brand = createBrand("Brand Uri");
         brand.setImage("Brand Image");
         brand.addAlias(new Alias("gb:amazon:asin", "brandAsin"));
 
         Episode episode1S1 = createEpisode("Episode 1 Series 1 Uri");
         episode1S1.setImage("Episode 1 Series 1 Image");
         episode1S1.setEpisodeNumber(1);
         episode1S1.setSeriesNumber(1);
         episode1S1.setSeries(series1);
         episode1S1.setContainer(brand);
         episode1S1.addAlias(new Alias("gb:amazon:asin", "episode1S1Asin"));
 
         Episode episode1S2 = createEpisode("Episode 1 Series 2 Uri");
         episode1S2.setImage("Episode 1 Series 2 Image");
         episode1S2.setEpisodeNumber(1);
         episode1S2.setSeriesNumber(2);
         episode1S2.setSeries(series2);
         episode1S2.setContainer(brand);
         episode1S2.addAlias(new Alias("gb:amazon:asin", "episode1S2Asin"));
 
         series1.setChildRefs(ImmutableList.of(episode1S1.childRef()));
         series1.setParent(brand);
 
         series2.setChildRefs(ImmutableList.of(episode1S2.childRef()));
         series2.setParent(brand);
 
         Map<String, ChildRef> sortedMap = ImmutableSortedMap
                 .<String, ChildRef>orderedBy(new SortKey.SortKeyOutputComparator())
                 .put(SortKey.keyFrom(episode1S1), episode1S1.childRef())
                 .put(SortKey.keyFrom(episode1S2), episode1S2.childRef())
                 .build();
 
         brand.setChildRefs(sortedMap.values());
         brand.setSeriesRefs(ImmutableList.of(series1.seriesRef(), series2.seriesRef()));
 
         contentResolver.addContent(episode1S1);
         contentResolver.addContent(episode1S2);
         contentResolver.addContent(series1);
         contentResolver.addContent(series2);
         contentResolver.addContent(brand);
 
         TVAMainType tvaMain = convertToXmlAndBack(episode1S2);
 
         GroupInformationTableType groupInfoTable = tvaMain.getProgramDescription().getGroupInformationTable();
 
         // episode == ep 1 s 2
         // check that series == ep 1 s 2
         // and brand == ep 1 s 1
         boolean foundEpisode = false;
         boolean foundSeries = false;
         boolean foundBrand = false;
         for (GroupInformationType groupInfo : groupInfoTable.getGroupInformation()) {
             if (isSeries(groupInfo)) {
                 assertEquals("Episode 1 Series 2 Image", getImage(groupInfo));
                 foundSeries = true;
             } else if (isBrand(groupInfo)) {
                 assertEquals("Episode 1 Series 1 Image", getImage(groupInfo));
                 foundBrand = true;
             } else {
                 assertEquals("Episode 1 Series 2 Image", getImage(groupInfo));
                 foundEpisode = true;
             }
         }
 
         assertTrue(foundEpisode);
         assertTrue(foundSeries);
         assertTrue(foundBrand);
     }
 
 
     private boolean isBrand(GroupInformationType groupInfo) {
         ProgramGroupTypeType groupType = (ProgramGroupTypeType) groupInfo.getGroupType();
         return groupType.getValue().equals("show");
     }
 
     private boolean isSeries(GroupInformationType groupInfo) {
         ProgramGroupTypeType groupType = (ProgramGroupTypeType) groupInfo.getGroupType();
         return groupType.getValue().equals("series");
     }
 
     @SuppressWarnings("unchecked")
     private TVAMainType convertToXmlAndBack(Content content) throws JAXBException {
         JAXBContext context = JAXBContext.newInstance("tva.metadata._2010");
         Unmarshaller unmarshaller = context.createUnmarshaller();
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         
         generator.generateXml(ImmutableList.<Content>of(content), baos);
         
         InputStream input = new ByteArrayInputStream(baos.toByteArray());
         
         JAXBElement<TVAMainType> tvaElem = (JAXBElement<TVAMainType>) unmarshaller.unmarshal(input);
         return tvaElem.getValue();
     }
     
     private String getImage(GroupInformationType groupInfo) {
         BasicContentDescriptionType basicDescription = groupInfo.getBasicDescription();
         RelatedMaterialType relatedMaterial = Iterables.getOnlyElement(basicDescription.getRelatedMaterial());
         MediaLocatorType locator = relatedMaterial.getMediaLocator();
         return locator.getMediaUri();   
     }
     
     private Series createSeries(String uri) {
         Series series = new Series();
         
         populateContentFields(series, uri);
         series.setSpecialization(Specialization.TV);
         
         return series;
     }
     
     private Brand createBrand(String uri) {
         Brand brand = new Brand();
         
         populateContentFields(brand, uri);
         brand.setSpecialization(Specialization.TV);
         
         return brand;
     }
     
     private Episode createEpisode(String uri) {
         Episode episode = new Episode();
         
         populateItemFields(episode, uri);
         episode.setSpecialization(Specialization.TV);
         
         return episode;
     }
 
     private Film createFilm(String uri) {
         Film film = new Film();
         
         populateItemFields(film, uri);
         film.setSpecialization(Specialization.FILM);
         
         return film;
     }
     
     private void populateContentFields(Content content, String uri) {
         
         content.setCanonicalUri(uri);
         content.setCurie("lf:e-177221");
         content.setTitle("Dr. Strangelove");
         content.setDescription("The film is set at the height of the tensions between Russia and the United States");
         content.setGenres(ImmutableList.of("http://lovefilm.com/genres/comedy"));
         content.setPublisher(Publisher.LOVEFILM);
         content.setCertificates(ImmutableList.of(new Certificate("PG", Countries.GB)));
         content.setYear(1963);
         content.setLanguages(ImmutableList.of("en"));
         content.setMediaType(MediaType.VIDEO);
         
         CrewMember georgeScott = new CrewMember();
         georgeScott.withName("George C. Scott");
         
         CrewMember stanley = new CrewMember();
         stanley.withName("Stanley Kubrick");
         
         content.setPeople(ImmutableList.of(georgeScott, stanley));
     }
     
     private void populateItemFields(Item item, String uri) {
         
         populateContentFields(item, uri);
         
         item.setCountriesOfOrigin(ImmutableSet.of(Countries.GB));
         
         Version version = new Version();
         Encoding encoding = new Encoding();
         Location location = new Location();
         Policy policy = new Policy();
 
         policy.setAvailabilityStart(new DateTime(2012, 7, 3, 0, 0, 0, DateTimeZone.UTC));
         policy.setAvailabilityEnd(new DateTime(2013, 7, 17, 0, 0, 0, DateTimeZone.UTC));
         
         location.setPolicy(policy);
         
         encoding.addAvailableAt(location);
         encoding.setVideoHorizontalSize(1280);
         encoding.setVideoVerticalSize(720);
         encoding.setVideoAspectRatio("16:9");
         encoding.setBitRate(3308);
 
         version.addManifestedAs(encoding);
         version.setDuration(Duration.standardMinutes(90));
 
         item.addVersion(version);
     }
 
     public static class DummyContentResolver implements ContentResolver {
         
         private final Map<String, Content> data = Maps.newHashMap();
         
         public void addContent(Content content) {
             data.put(content.getCanonicalUri(), content);
         }
         
         @Override
         public ResolvedContent findByCanonicalUris(Iterable<String> uris) {
             ResolvedContentBuilder builder = ResolvedContent.builder();
             for (String uri : uris) {
                 builder.put(uri, data.get(uri));
             }
             return builder.build();
         }
     };
 }
