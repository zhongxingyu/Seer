 package org.atlasapi.remotesite.seesaw;
 
 import java.util.Currency;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nu.xom.Element;
 import nu.xom.Elements;
 import nu.xom.Node;
 import nu.xom.Nodes;
 import nu.xom.XPathContext;
 
 import org.atlasapi.genres.GenreMap;
 import org.atlasapi.media.TransportSubType;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.ContentType;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Description;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Policy.RevenueContract;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.currency.Price;
 import com.metabroadcast.common.media.MimeType;
 
 public class SeesawContentProcessor {
     private static final String MEDIA = "http://search.yahoo.com/mrss/";
     private static final String SMD = "http://www.seesaw.com/api/mrss";
     private static final String DCTERMS = "http://purl.org/dc/terms/";
     
     private final XPathContext xpathContext = new XPathContext();
     private final GenreMap genreMap = new SeesawGenreMap();
     
     private Set<Brand> allBrands = Sets.newHashSet();
     private Set<Series> allSeries = Sets.newHashSet();
     private Set<Episode> allEpisodes = Sets.newHashSet();
     private Set<Item> allItems = Sets.newHashSet();
     
     private Map<String, Description> idToDescription = Maps.newHashMap();
     private Map<Description, String> descriptionToParentId = Maps.newHashMap();
     private final AdapterLog log;
     
     public SeesawContentProcessor(AdapterLog log) {
         this.log = log;
         xpathContext.addNamespace("media", MEDIA);
         xpathContext.addNamespace("smd", SMD);
     }
     
     public Set<Brand> getAllBrands() {
         return allBrands;
     }
     
     public Set<Item> getAllItems() {
         return allItems;
     }
     
     public void joinContent() {
         for (Episode episode : allEpisodes) {
             String parentId = descriptionToParentId.get(episode);
             
             Description description = idToDescription.get(parentId);
             if (description != null) {
                 if (description instanceof Series) {
                     Series series = (Series) description;
                     series.addItem(episode);
                     episode.setSeriesNumber(series.getSeriesNumber());
                     episode.setSeries(series);
                 }
                 else if (description instanceof Brand) {
                     Brand brand = (Brand) description;
                     brand.addItem(episode);
                     episode.setBrand(brand);
                 }
                 else {
                     log.record(new AdapterLogEntry(Severity.DEBUG).withSource(SeesawContentProcessor.class).withDescription("Tried to join episode (uri: " + episode.getCanonicalUri() + ") to parent, but parent ID " + parentId + " was not a series or brand"));
                 }
             }
             else {
                 log.record(new AdapterLogEntry(Severity.DEBUG).withSource(SeesawContentProcessor.class).withDescription("Tried to join episode (uri: " + episode.getCanonicalUri() + ") to parent, but parent ID " + parentId + " was not found"));
             }
         }
         
         for (Series series : allSeries) {
             String parentId = descriptionToParentId.get(series);
             
             Description description = idToDescription.get(parentId);
             if (description instanceof Brand) {
                 Brand brand = (Brand) description;
                 for (Item item : series.getItems()) {
                     if (item instanceof Episode) {
                         Episode episode = (Episode) item;
                         episode.setBrand(brand);
                         brand.addItem(episode);
                     }
                     else {
                         log.record(new AdapterLogEntry(Severity.DEBUG).withSource(SeesawContentProcessor.class).withDescription("When joining series (uri: " + series.getCanonicalUri() + ") to brand, found an Item rather than Episode in the series"));
                     }
                 }
             }
         }
     }
     
     public void processItemElement(Element itemElement) {
         Element groupElement = getGroupElement(itemElement);
         Element mediaElement = groupElement.getFirstChildElement("content", MEDIA);
         Maybe<String> parentId = getParentId(groupElement);
         
         if (mediaElement != null) {
             if (parentId.hasValue()) {
                 processAsEpisode(itemElement);
             }
             else {
                 processAsItem(itemElement);
             }
         }
         else {
             if (parentId.hasValue()) {
                 processAsSeries(itemElement);
             }
             else {
                 processAsBrand(itemElement);
             }
         }
     }
     
     private void addItemData(Item item, Element itemElement) {
         
         Element seesawMetadata = getSeesawMetadata(itemElement);
         item.setTitle(getTitle(seesawMetadata));
         
         Element groupElement = getGroupElement(itemElement);
         item.setDescription(getDescription(groupElement));
         item.setTags(getTags(groupElement));
         
         Maybe<String> thumbnail = getThumbnail(groupElement);
         if (thumbnail.hasValue()) {
             item.setThumbnail(thumbnail.requireValue());
         }
         
         Maybe<String> image = getImage(groupElement);
         if (image.hasValue()) {
             item.setImage(image.requireValue());
         }
         
         item.setGenres(getGenres(groupElement));
         
         item.addVersion(getVersion(item, groupElement));
     }
     
     private Version getVersion(Item item, Element groupElement) {
         Version version = new Version();
         version.setCanonicalUri(item.getCanonicalUri());
         version.setProvider(Publisher.SEESAW);
         version.setDuration(Duration.standardSeconds(Long.valueOf(groupElement.getFirstChildElement("content", MEDIA).getAttributeValue("duration"))));
         
         Location linkLocation = new Location();
         linkLocation.setTransportType(TransportType.LINK);
         linkLocation.setTransportSubType(TransportSubType.HTTP);
         linkLocation.setAvailable(true);
         linkLocation.setUri(item.getCanonicalUri());
         linkLocation.setPolicy(getPolicy(groupElement));
         
         
         Elements encodingElements = groupElement.getChildElements("content", MEDIA);
         for (int i = 0; i < encodingElements.size(); i++) {
             Encoding encoding = getEncoding(encodingElements.get(i));
             encoding.addAvailableAt(linkLocation);
             version.addManifestedAs(encoding);
         }
         
         Element ratingElement = groupElement.getFirstChildElement("rating", MEDIA);
         if (ratingElement != null) {
            version.setRating(ratingElement.getValue());
         }
         
         return version;
     }
     
     private Policy getPolicy(Element groupElement) {
         Policy policy = new Policy();
         
         Element priceElement = groupElement.getFirstChildElement("price", MEDIA);
         if (priceElement != null) {
             if (priceElement.getAttributeValue("type").equals("rent")) {
                 policy.setRevenueContract(RevenueContract.PAY_TO_RENT);
             }
             
             Pattern pattern = Pattern.compile("[^0-9]*([0-9]+\\.?[0-9]*)[^0-9]*");
             Matcher matcher = pattern.matcher(priceElement.getAttributeValue("price"));
             
             if (matcher.matches()) {
                 policy.setPrice(new Price(Currency.getInstance(priceElement.getAttributeValue("currency")), Float.parseFloat(matcher.group(1))));
             }
         }
         else {
             policy.setRevenueContract(RevenueContract.FREE_TO_VIEW);
         }
         
         Element availabilityElement = groupElement.getFirstChildElement("valid", DCTERMS);
         DateTimeFormatter dateParser = DateTimeFormat.forPattern(availabilityElement.getAttributeValue("scheme")).withZone(DateTimeZone.UTC);
         
         policy.setAvailabilityStart(dateParser.parseDateTime(availabilityElement.getAttributeValue("start")));
         policy.setAvailabilityEnd(dateParser.parseDateTime(availabilityElement.getAttributeValue("end")));
         policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
         
         return policy;
     }
     
     private Encoding getEncoding(Element encodingElement) {
         Encoding encoding = new Encoding();
         encoding.setVideoHorizontalSize(Integer.valueOf(encodingElement.getAttributeValue("width")));
         encoding.setVideoVerticalSize(Integer.valueOf(encodingElement.getAttributeValue("height")));
         encoding.setBitRate(Integer.valueOf(encodingElement.getAttributeValue("bitrate")));
         encoding.setVideoCoding(MimeType.fromString(encodingElement.getAttributeValue("type")));
         encoding.setAudioChannels(Integer.valueOf(encodingElement.getAttributeValue("channels")));
         encoding.setVideoFrameRate(Float.valueOf(encodingElement.getAttributeValue("framerate")));
         
         return encoding;
     }
     
     private void processAsItem(Element itemElement) {
         String uri = getUri(itemElement);
         Item item = new Item(uri, getCurie(uri), Publisher.SEESAW);
         
         addItemData(item, itemElement);
         
         allItems.add(item);
     }
     
     private void processAsEpisode(Element itemElement) {
         String uri = getUri(itemElement);
         Episode episode = new Episode(uri, getCurie(uri), Publisher.SEESAW);
         
         addItemData(episode, itemElement);
         
         Element seesawMetadata = getSeesawMetadata(itemElement);
         Element groupElement = getGroupElement(itemElement);
         
         episode.setEpisodeNumber(getSequenceNumber(seesawMetadata));
         
         allEpisodes.add(episode);
         descriptionToParentId.put(episode, getParentId(groupElement).requireValue());
         idToDescription.put(getId(groupElement), episode);
     }
 
     private void processAsSeries(Element seriesElement) {
         String uri = getUri(seriesElement);
         Series series = new Series(uri, getCurie(uri));
         series.setPublisher(Publisher.SEESAW);
         
         Element seesawMetadata = getSeesawMetadata(seriesElement);
         Element groupElement = getGroupElement(seriesElement);
         series.setTitle(getTitle(seesawMetadata));
         series.withSeriesNumber(getSequenceNumber(seesawMetadata));
         series.setDescription(getDescription(groupElement));
         series.setTags(getTags(groupElement));
         
         Maybe<String> thumbnail = getThumbnail(groupElement);
         if (thumbnail.hasValue()) {
             series.setThumbnail(thumbnail.requireValue());
         }
         
         Maybe<String> image = getImage(groupElement);
         if (image.hasValue()) {
             series.setImage(image.requireValue());
         }
         series.setGenres(getGenres(groupElement));
         
         allSeries.add(series);
         descriptionToParentId.put(series, getParentId(groupElement).requireValue());
         idToDescription.put(getId(groupElement), series);
     }
     
     private void processAsBrand(Element brandElement) {
         
         String uri = getUri(brandElement);
         
         Brand brand = new Brand(uri, getCurie(uri), Publisher.SEESAW);
         
         Element seesawMetadata = getSeesawMetadata(brandElement);
         
         brand.setTitle(getTitle(seesawMetadata));
         
         Element groupElement = getGroupElement(brandElement);
         
         brand.setDescription(getDescription(groupElement));
         brand.setTags(getTags(groupElement));
         brand.setGenres(getGenres(groupElement));
         
         Maybe<String> thumbnail = getThumbnail(groupElement);
         if (thumbnail.hasValue()) {
             brand.setThumbnail(thumbnail.requireValue());
         }
         
         Maybe<String> image = getImage(groupElement);
         if (image.hasValue()) {
             brand.setImage(image.requireValue());
         }
         
         brand.setContentType(ContentType.VIDEO);
         
         allBrands.add(brand);
         idToDescription.put(getId(groupElement), brand);
     }
     
     private int getSequenceNumber(Element seesawMetadata) {
         return Integer.valueOf(seesawMetadata.getFirstChildElement("sequenceNumber", SMD).getValue());
     }
     
     private Element getSeesawMetadata(Element itemElement) {
         return itemElement.getFirstChildElement("seesawMetadata", SMD);
     }
     
     private Element getGroupElement(Element itemElement) {
         return itemElement.getFirstChildElement("group", MEDIA);
     }
     
     private String getTitle(Element seesawMetadata) {
         return seesawMetadata.getFirstChildElement("title", SMD).getValue();
     }
     
     private String getUri(Element itemElement) {
         return itemElement.getFirstChildElement("link").getValue();
     }
     
     private String getDescription(Element groupElement) {
         return groupElement.getFirstChildElement("description", MEDIA).getValue();
     }
     
     private Set<String> getTags(Element groupElement) {
         String keywords = groupElement.getFirstChildElement("keywords", MEDIA).getValue();
         Splitter tagSplitter = Splitter.on(",").trimResults();
         return ImmutableSet.copyOf(tagSplitter.split(keywords));
     }
     
     private Set<String> getGenres(Element groupElement) {
         Set<String> seesawGenres = Sets.newHashSet();
         Nodes results = groupElement.query("media:category[@scheme='http://www.seesaw.com/api/genre']", xpathContext);
         for (int i = 0; i < results.size(); i++) {
             Node node = results.get(i);
             seesawGenres.add(node.getValue());
         }
         
         return genreMap.map(seesawGenres);
     }
     
     private String getId(Element groupElement) {
         String value = groupElement.query("media:category[@scheme='http://www.seesaw.com/api/id']/@label", xpathContext).get(0).getValue();
         
         return value;
     }
     
     private Maybe<String> getParentId(Element groupElement) {
         Nodes results = groupElement.query("media:category[@scheme='http://www.seesaw.com/api/parent_id']/@label", xpathContext);
         
         if (results.size() > 0) {
             
             return Maybe.just(results.get(0).getValue());
         }
         else {
             return Maybe.nothing();
         }
     }
     
     private Maybe<String> getImage(Element groupElement) {
         Elements imageElements = groupElement.getChildElements("thumbnails", MEDIA);
 
         int largestWidth = 0;
         Maybe<String> largestImage = Maybe.nothing();
         
         for (int i = 0; i < imageElements.size(); i++) {
             Element imageElement = imageElements.get(i);
             
             int width = Integer.valueOf(imageElement.getAttributeValue("width"));
             if (width > largestWidth) {
                 largestWidth = width;
                 largestImage = Maybe.just(imageElement.getAttributeValue("url"));
             }
         }
         
         return largestImage;
     }
     
     private Maybe<String> getThumbnail(Element groupElement) {
         Elements imageElements = groupElement.getChildElements("thumbnails", MEDIA);
 
         int smallestWidth = 0;
         Maybe<String> smallestImage = Maybe.nothing();
         
         for (int i = 0; i < imageElements.size(); i++) {
             Element imageElement = imageElements.get(i);
             
             int width = Integer.valueOf(imageElement.getAttributeValue("width"));
             if (width < smallestWidth) {
                 smallestWidth = width;
                 smallestImage = Maybe.just(imageElement.getAttributeValue("url"));
             }
         }
         
         return smallestImage;
     }
     
     private String getCurie(String uri) {
         return "seesaw:" + uri.substring("http://www.seesaw.com/".length()).replace("/", "-");
     }
 }
