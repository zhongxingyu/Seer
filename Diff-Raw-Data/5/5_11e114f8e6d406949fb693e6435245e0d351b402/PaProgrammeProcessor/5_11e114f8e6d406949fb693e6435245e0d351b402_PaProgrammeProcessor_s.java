 package org.atlasapi.remotesite.pa;
 
 import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
 import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.genres.GenreMap;
 import org.atlasapi.media.entity.Actor;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.pa.bindings.Attr;
 import org.atlasapi.remotesite.pa.bindings.Billing;
 import org.atlasapi.remotesite.pa.bindings.CastMember;
 import org.atlasapi.remotesite.pa.bindings.Category;
 import org.atlasapi.remotesite.pa.bindings.PictureUsage;
 import org.atlasapi.remotesite.pa.bindings.ProgData;
 import org.atlasapi.remotesite.pa.bindings.StaffMember;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 import org.joda.time.format.DateTimeFormat;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Function;
import com.google.common.base.Objects;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.inject.internal.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.text.MoreStrings;
 import com.metabroadcast.common.time.Timestamp;
 
 public class PaProgrammeProcessor implements PaProgDataProcessor {
     
     private static final String PA_BASE_IMAGE_URL = "http://images.atlasapi.org/pa/";
     public static final String BROADCAST_ID_PREFIX = "pa:";
     private static final String YES = "yes";
     private static final String CLOSED_BRAND = "http://pressassociation.com/brands/8267";
     private static final String CLOSED_EPISODE = "http://pressassociation.com/episodes/closed";
     private static final String CLOSED_CURIE = "pa:closed";
     private static final List<String> IGNORED_BRANDS = ImmutableList.of("70214", "84575");    // 70214 is 'TBA' brand, 84575 is 'Film TBA'
     
     private final ContentWriter contentWriter;
     private final ContentResolver contentResolver;
     private final AdapterLog log;
     private final PaChannelMap channelMap = new PaChannelMap();
     private final PaCountryMap countryMap = new PaCountryMap();
     
     private final GenreMap genreMap = new PaGenreMap();
     
     private final ItemsPeopleWriter personWriter;
 
     public PaProgrammeProcessor(ContentWriter contentWriter, ContentResolver contentResolver, ItemsPeopleWriter itemsPeopleWriter, AdapterLog log) {
         this.contentWriter = contentWriter;
         this.contentResolver = contentResolver;
         this.log = log;
         this.personWriter = itemsPeopleWriter;
     }
 
     @Override
     public ItemRefAndBroadcast process(ProgData progData, Channel channel, DateTimeZone zone, Timestamp updatedAt) {
         try {
             if (! Strings.isNullOrEmpty(progData.getSeriesId()) && IGNORED_BRANDS.contains(progData.getSeriesId())) {
                 return null;
             }
             
             Maybe<Brand> possibleBrand = getBrand(progData, channel);
             if (possibleBrand.hasValue()) {
                 Brand brand = possibleBrand.requireValue();
                 if (isClosedBrand(possibleBrand)) {
                     brand.setScheduleOnly(true);
                 }
                 brand.setLastUpdated(updatedAt.toDateTimeUTC());
             	contentWriter.createOrUpdate(brand);
             }
             
             Maybe<Series> series = getSeries(progData, channel, possibleBrand.hasValue());
             if (series.hasValue()) {
             	if (possibleBrand.hasValue()) {
             		series.requireValue().setParent(possibleBrand.requireValue());
             	}
             	series.requireValue().setLastUpdated(updatedAt.toDateTimeUTC());
             	contentWriter.createOrUpdate(series.requireValue());
             }
             
             Maybe<ItemAndBroadcast> itemAndBroadcast = isClosedBrand(possibleBrand) ? getClosedEpisode(possibleBrand.requireValue(), progData, channel, zone, updatedAt) : getFilmOrEpisode(progData, channel, zone, possibleBrand.hasValue() || series.hasValue(), updatedAt);
 
             if (itemAndBroadcast.hasValue()) {
             	Item item = itemAndBroadcast.requireValue().getItem();
                 if (series.hasValue() && item instanceof Episode) {
                 	Episode episode = (Episode) item;
                     episode.setSeries(series.requireValue());
                 }
                 if (possibleBrand.hasValue()) {
                 	item.setContainer(possibleBrand.requireValue());
                 } else if (series.hasValue()) {
                 	item.setContainer(series.requireValue());
                 }
                 item.setLastUpdated(updatedAt.toDateTimeUTC());
                 contentWriter.createOrUpdate(item);
                 personWriter.createOrUpdatePeople(item);
             }
             return new ItemRefAndBroadcast(itemAndBroadcast.requireValue().getItem(), itemAndBroadcast.requireValue().getBroadcast());
         } catch (Exception e) {
         	e.printStackTrace();
         	log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaProgrammeProcessor.class).withDescription(e.getMessage()));
         }
         return null;
     }
     
     private boolean isClosedBrand(Maybe<Brand> brand) {
         return brand.hasValue() && CLOSED_BRAND.equals(brand.requireValue().getCanonicalUri());
     }
     
     private Maybe<ItemAndBroadcast> getClosedEpisode(Brand brand, ProgData progData, Channel channel, DateTimeZone zone, Timestamp updatedAt) {
         String uri = CLOSED_EPISODE+getClosedPostfix(channel);
         Maybe<Identified> resolvedContent = contentResolver.findByCanonicalUris(ImmutableList.of(uri)).getFirstValue();
 
         Episode episode;
         if (resolvedContent.hasValue() && resolvedContent.requireValue() instanceof Episode) {
             episode = (Episode) resolvedContent.requireValue();
         } else {
             episode = (Episode) getBasicEpisode(progData, true);
         }
         episode.setCanonicalUri(uri);
         episode.setCurie(CLOSED_CURIE+getClosedPostfix(channel));
         episode.setTitle(progData.getTitle());
         episode.setScheduleOnly(true);
         
         Version version = findBestVersion(episode.getVersions());
 
         Broadcast broadcast = broadcast(progData, channel, zone, updatedAt);
         addBroadcast(version, broadcast);
 
         return Maybe.just(new ItemAndBroadcast(episode, broadcast));
     }
     
     private String getClosedPostfix(Channel channel) {
         return "_"+channel.key();
     }
     
     private Maybe<Brand> getBrand(ProgData progData, Channel channel) {
         String brandId = progData.getSeriesId();
         if (Strings.isNullOrEmpty(brandId) || Strings.isNullOrEmpty(brandId.trim())) {
             return Maybe.nothing();
         }
 
         String brandUri = PaHelper.getBrandUri(brandId);
         
         Maybe<Identified> possiblePrevious = contentResolver.findByCanonicalUris(ImmutableList.of(brandUri)).getFirstValue();
         
         Brand brand = possiblePrevious.hasValue() ? (Brand) possiblePrevious.requireValue() : new Brand(brandUri, "pa:b-" + brandId, Publisher.PA);
         
         brand.setTitle(progData.getTitle());
         brand.setDescription(progData.getSeriesSynopsis());
         brand.setSpecialization(specialization(progData, channel));
         setGenres(progData, brand);
 
         if (progData.getPictures() != null) {
             for (PictureUsage picture : progData.getPictures().getPictureUsage()) {
                 if (picture.getType().equals("season") && brand.getImage() == null){
                     brand.setImage(PA_BASE_IMAGE_URL + picture.getvalue());
                 }
                 if (picture.getType().equals("series")){
                     brand.setImage(PA_BASE_IMAGE_URL + picture.getvalue());
                     break;
                 }
             }
         }
 
         return Maybe.just(brand);
     }
 
     private Maybe<Series> getSeries(ProgData progData, Channel channel, boolean hasBrand) {
         if (Strings.isNullOrEmpty(progData.getSeriesNumber()) || Strings.isNullOrEmpty(progData.getSeriesId())) {
             return Maybe.nothing();
         }
         String seriesUri = PaHelper.getSeriesUri(progData.getSeriesId(), progData.getSeriesNumber());
         
         Maybe<Identified> possiblePrevious = contentResolver.findByCanonicalUris(ImmutableList.of(seriesUri)).getFirstValue();
         
         Series series = possiblePrevious.hasValue() ? (Series) possiblePrevious.requireValue() : new Series(seriesUri, "pa:s-" + progData.getSeriesId() + "-" + progData.getSeriesNumber(), Publisher.PA);
         
         if(progData.getEpisodeTotal() != null && progData.getEpisodeTotal().trim().length() > 0) {
             try {
                 series.setTotalEpisodes(Integer.parseInt(progData.getEpisodeTotal().trim()));
             } catch (NumberFormatException e) {
                 log.record(warnEntry().withCause(e).withSource(getClass()).withDescription("Couldn't parse episode_total %s", progData.getEpisodeTotal().trim()));
             }
         }
         
         if(progData.getSeriesNumber() != null && progData.getSeriesNumber().trim().length() > 0) {
             try {
                 series.withSeriesNumber(Integer.parseInt(progData.getSeriesNumber().trim()));
             } catch (NumberFormatException e) {
                 log.record(warnEntry().withCause(e).withSource(getClass()).withDescription("Couldn't parse series_number %s", progData.getSeriesNumber().trim()));
             }
         }
     
         series.setPublisher(Publisher.PA);
         series.setSpecialization(specialization(progData, channel));
         setGenres(progData, series);
         
         if (progData.getPictures() != null) {
             for (PictureUsage picture : progData.getPictures().getPictureUsage()) {
                 if (picture.getType().equals("series") && series.getImage() == null){
                     series.setImage(PA_BASE_IMAGE_URL + picture.getvalue());
                 }
                 if (picture.getType().equals("season")){
                     series.setImage(PA_BASE_IMAGE_URL + picture.getvalue());
                     break;
                 }
             }
         }
 
         return Maybe.just(series);
     }
     
     private Maybe<ItemAndBroadcast> getFilmOrEpisode(ProgData progData, Channel channel, DateTimeZone zone, boolean isEpisode, Timestamp updatedAt) {
         return specialization(progData, channel) == Specialization.FILM ? getFilm(progData, channel, zone, updatedAt) : getEpisode(progData, channel, zone, isEpisode, updatedAt);
     }
     
     private Maybe<ItemAndBroadcast> getFilm(ProgData progData, Channel channel, DateTimeZone zone, Timestamp updatedAt) {
         String filmUri = PaHelper.getFilmUri(programmeId(progData));
         Maybe<Identified> possiblePreviousData = contentResolver.findByCanonicalUris(ImmutableList.of(filmUri)).getFirstValue();
         
         Film film;
         if (possiblePreviousData.hasValue()) {
         	Identified previous = possiblePreviousData.requireValue();
             if (previous instanceof Film) {
                 film = (Film) previous;
             }
             else {
                 film = new Film();
                 Item.copyTo((Episode) previous, film);
             }
         } else {
             film = getBasicFilm(progData);
         }
         
         Broadcast broadcast = setCommonDetails(progData, channel, zone, film, updatedAt);
         
         if (progData.getFilmYear() != null && MoreStrings.containsOnlyAsciiDigits(progData.getFilmYear())) {
             film.setYear(Integer.parseInt(progData.getFilmYear()));
         }
         
         return Maybe.just(new ItemAndBroadcast(film, broadcast));
     }
     
     private Broadcast setCommonDetails(ProgData progData, Channel channel, DateTimeZone zone, Item episode, Timestamp updatedAt) {
         
         //currently Welsh channels have Welsh titles/descriptions 
         // which flip the English ones, resulting in many writes. We'll only take the Welsh title if we don't
     	// already have a title from another channel
         if (episode.getTitle() == null || !channel.uri().contains("wales")) {
             if (progData.getEpisodeTitle() != null) {
                 episode.setTitle(progData.getEpisodeTitle());
             } else {
                 episode.setTitle(progData.getTitle());
             }
         }
 
         if (progData.getBillings() != null) {
             for (Billing billing : progData.getBillings().getBilling()) {
                 if (billing.getType().equals("synopsis")) {
                 	if(episode.getDescription() == null || !channel.uri().contains("wales")) {
 	                    episode.setDescription(billing.getvalue());
 	                    break;
                 	}
                 }
             }
         }
 
 
         episode.setMediaType(channelMap.isRadioChannel(channel) ? MediaType.AUDIO : MediaType.VIDEO);
         episode.setSpecialization(specialization(progData, channel));
         setGenres(progData, episode);
         
         if (progData.getCountry() != null) {
             episode.setCountriesOfOrigin(countryMap.parseCountries(progData.getCountry()));
         }
         
         if (progData.getAttr() != null) {
             episode.setBlackAndWhite(getBooleanValue(progData.getAttr().getBw()));
         }
 
         if (progData.getPictures() != null) {
             for (PictureUsage picture : progData.getPictures().getPictureUsage()) {
                 if (picture.getType().equals("series") && episode.getImage() == null){
                     episode.setImage(PA_BASE_IMAGE_URL + picture.getvalue());
                 }
                 if (picture.getType().equals("season") && episode.getImage() == null){
                     episode.setImage(PA_BASE_IMAGE_URL + picture.getvalue());
                 }
                 if (picture.getType().equals("episode")){
                     episode.setImage(PA_BASE_IMAGE_URL + picture.getvalue());
                     break;
                 }
             }
         }
         
         episode.setPeople(people(progData));
 
         Version version = findBestVersion(episode.getVersions());
 
         Broadcast broadcast = broadcast(progData, channel, zone, updatedAt);
         addBroadcast(version, broadcast);
         return broadcast;
     }
 
     public static Function<Category, String> TO_GENRE_URIS = new Function<Category, String>() {
         @Override
         public String apply(Category from) {
             return "http://pressassociation.com/genres/" + from.getCategoryCode();
         }
     };
     
     private void setGenres(ProgData progData, Content content) {
         Set<String> extractedGenres = genreMap.map(ImmutableSet.copyOf(Iterables.transform(progData.getCategory(), TO_GENRE_URIS)));
         extractedGenres.remove("http://pressassociation.com/genres/BE00");
         if(!extractedGenres.isEmpty()) {
             content.setGenres(extractedGenres);
         }
     }
 
     private Maybe<ItemAndBroadcast> getEpisode(ProgData progData, Channel channel, DateTimeZone zone, boolean isEpisode, Timestamp updatedAt) {
         
         String episodeUri = PaHelper.getEpisodeUri(programmeId(progData));
         Maybe<Identified> possiblePrevious = contentResolver.findByCanonicalUris(ImmutableList.of(episodeUri)).getFirstValue();
 
         Item item;
         if (possiblePrevious.hasValue()) {
             item = (Item) possiblePrevious.requireValue();
             if (!(item instanceof Episode) && isEpisode) {
                 log.record(warnEntry().withSource(getClass()).withDescription("%s resolved as %s being ingested as Episode", episodeUri, item.getClass().getSimpleName()));
                 item = convertItemToEpisode(item);
             } else if(item instanceof Episode && !isEpisode) {
                 log.record(errorEntry().withSource(getClass()).withDescription("%s resolved as %s being ingested as Item", episodeUri, item.getClass().getSimpleName()));
             }
         } else {
             item = getBasicEpisode(progData, isEpisode);
         }
         
         Broadcast broadcast = setCommonDetails(progData, channel, zone, item, updatedAt);
         
         try {
             if (item instanceof Episode) {
                 if (progData.getEpisodeNumber() != null) {
                     ((Episode) item).setEpisodeNumber(Integer.valueOf(progData.getEpisodeNumber()));
                 }
                 if (progData.getSeriesNumber() != null) {
                 	 ((Episode) item).setSeriesNumber(Integer.valueOf(progData.getSeriesNumber()));
                 }
             }
         } catch (NumberFormatException e) {
             // sometimes we don't get valid numbers
         }
         
         return Maybe.just(new ItemAndBroadcast(item, broadcast));
     }
 
     private Item convertItemToEpisode(Item item) {
         Episode episode = new Episode(item.getCanonicalUri(), item.getCurie(),item.getPublisher());
         episode.setAliases(item.getAliases());
         episode.setBlackAndWhite(item.isBlackAndWhite());
         episode.setClips(item.getClips());
         episode.setParentRef(item.getContainer());
         episode.setCountriesOfOrigin(item.getCountriesOfOrigin());
         episode.setDescription(item.getDescription());
         episode.setFirstSeen(item.getFirstSeen());
         episode.setGenres(item.getGenres());
         episode.setImage(item.getImage());
         episode.setIsLongForm(item.getIsLongForm());
         episode.setLastFetched(item.getLastFetched());
         episode.setLastUpdated(item.getLastUpdated());
         episode.setMediaType(item.getMediaType());
         episode.setPeople(item.getPeople());
         episode.setScheduleOnly(item.isScheduleOnly());
         episode.setSpecialization(item.getSpecialization());
         episode.setTags(item.getTags());
         episode.setThisOrChildLastUpdated(item.getThisOrChildLastUpdated());
         episode.setThumbnail(item.getThumbnail());
         episode.setTitle(item.getTitle());
         episode.setVersions(item.getVersions());
         return episode;
     }
 
     private Broadcast broadcast(ProgData progData, Channel channel, DateTimeZone zone, Timestamp updateAt) {
         Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));
 
         DateTime transmissionTime = getTransmissionTime(progData.getDate(), progData.getTime(), zone);
         
         Broadcast broadcast = new Broadcast(channel.uri(), transmissionTime, duration).withId(PaHelper.getBroadcastId(progData.getShowingId()));
         
         if (progData.getAttr() != null) {
             broadcast.setRepeat(isRepeat(channel, progData.getAttr()));
             broadcast.setSubtitled(getBooleanValue(progData.getAttr().getSubtitles()));
             broadcast.setSigned(getBooleanValue(progData.getAttr().getSignLang()));
             broadcast.setAudioDescribed(getBooleanValue(progData.getAttr().getAudioDes()));
             broadcast.setHighDefinition(getBooleanValue(progData.getAttr().getHd()));
             broadcast.setWidescreen(getBooleanValue(progData.getAttr().getWidescreen()));
             broadcast.setLive(getBooleanValue(progData.getAttr().getLive()));
             broadcast.setSurround(getBooleanValue(progData.getAttr().getSurround()));
             broadcast.setPremiere(getBooleanValue(progData.getAttr().getPremiere()));
             broadcast.setNewSeries(getBooleanValue(progData.getAttr().getNewSeries()));
         }
         broadcast.setLastUpdated(updateAt.toDateTimeUTC());
         return broadcast;
     }
 
     private static final Set<Channel> TERRESTRIAL_CHANNELS = ImmutableSet.<Channel>builder()
         .add(Channel.BBC_ONE_EAST)
         .add(Channel.BBC_ONE)
         .add(Channel.BBC_HD)
         .add(Channel.BBC_ONE_WEST_MIDLANDS)
         .add(Channel.BBC_ONE_EAST_MIDLANDS)
         .add(Channel.BBC_ONE_YORKSHIRE)
         .add(Channel.BBC_ONE_NORTH_EAST)
         .add(Channel.BBC_ONE_NORTH_WEST)
         .add(Channel.BBC_ONE_NORTHERN_IRELAND)
         .add(Channel.BBC_ONE_WALES)
         .add(Channel.BBC_ONE_SCOTLAND)
         .add(Channel.BBC_ONE_SOUTH)
         .add(Channel.BBC_ONE_SOUTH_WEST)
         .add(Channel.BBC_ONE_WEST)
         .add(Channel.BBC_ONE_SOUTH_EAST)
         .add(Channel.BBC_TWO)
         .add(Channel.BBC_TWO_NORTHERN_IRELAND)
         .add(Channel.BBC_TWO_SCOTLAND)
         .add(Channel.BBC_TWO_WALES)
         .add(Channel.CBBC)
         .add(Channel.CBEEBIES)
         .add(Channel.ITV1_ANGLIA)
         .add(Channel.ITV1_BORDER_SOUTH)
         .add(Channel.ITV1_LONDON)
         .add(Channel.ITV1_CARLTON_CENTRAL)
         .add(Channel.ITV1_CHANNEL)
         .add(Channel.ITV1_GRANADA)
         .add(Channel.ITV1_MERIDIAN)
         .add(Channel.ITV1_TYNE_TEES)
         .add(Channel.ITV1_HD)
         .add(Channel.ITV2_HD)
         .add(Channel.ITV3_HD)
         .add(Channel.ITV4_HD)
         .add(Channel.YTV)
         .add(Channel.ITV1_CARLTON_WESTCOUNTRY)
         .add(Channel.ITV1_WALES)
         .add(Channel.ITV1_WEST)
         .add(Channel.STV_CENTRAL)
         .add(Channel.ULSTER)
         .add(Channel.ITV1_BORDER_NORTH)
         .add(Channel.CHANNEL_FOUR)
         .add(Channel.S4C)
         .add(Channel.FIVE)
         .add(Channel.BBC_RADIO_RADIO1)
         .add(Channel.BBC_RADIO_RADIO2)
         .add(Channel.BBC_RADIO_RADIO3)
         .add(Channel.BBC_RADIO_RADIO4)
         .add(Channel.BBC_RADIO_RADIO7)
         .add(Channel.BBC_RADIO_RADIO4_LW)
         .add(Channel.BBC_RADIO_5LIVE)
         .add(Channel.BBC_RADIO_5LIVESPORTSEXTRA)
         .add(Channel.BBC_RADIO_6MUSIC)
         .add(Channel.BBC_RADIO_1XTRA)
         .add(Channel.BBC_RADIO_ASIANNETWORK)
         .add(Channel.BBC_RADIO_WORLDSERVICE)
         .add(Channel.BBC_THREE)
         .add(Channel.BBC_FOUR)
         .add(Channel.ITV2)
         .add(Channel.ITV3)
         .add(Channel.ITV4)
         .add(Channel.FIVE)
         .add(Channel.FIVE_HD)
         .add(Channel.FIVE_USA)
         .add(Channel.E4_HD)
         .add(Channel.E_FOUR)
         .add(Channel.MORE_FOUR)
         .add(Channel.FILM_4)
         .add(Channel.STV_HD)
         .add(Channel.Channel_4_HD)
         .add(Channel.FILM4_HD)
     .build();
     
     private Boolean isRepeat(Channel channel, Attr attr) {
         if (!TERRESTRIAL_CHANNELS.contains(channel)) {
             return true;
         }
         return getBooleanValue(attr.getRepeat());
     }
 
     private void addBroadcast(Version version, Broadcast broadcast) {
         if (! Strings.isNullOrEmpty(broadcast.getId())) {
             Set<Broadcast> broadcasts = Sets.newHashSet();
             Maybe<Interval> broadcastInterval = broadcast.transmissionInterval();
             
             for (Broadcast currentBroadcast: version.getBroadcasts()) {
                 // I know this is ugly, but it's easier to read.
                 if (Strings.isNullOrEmpty(currentBroadcast.getId())) {
                     continue;
                 }
                 if (broadcast.getId().equals(currentBroadcast.getId())) {
                     continue;
                 }
                 if (currentBroadcast.transmissionInterval().hasValue() && broadcastInterval.hasValue()) {
                     Interval currentInterval = currentBroadcast.transmissionInterval().requireValue();
                     if (currentBroadcast.getBroadcastOn().equals(broadcast.getBroadcastOn()) && currentInterval.overlaps(broadcastInterval.requireValue())) {
                         continue;
                     }
                 }
                 broadcasts.add(currentBroadcast);
             }
             broadcasts.add(broadcast);
             
             version.setBroadcasts(broadcasts);
         }
     }
     
     private List<CrewMember> people(ProgData progData) {
         List<CrewMember> people = Lists.newArrayList();
         
         for (CastMember cast: progData.getCastMember()) {
             if (!Strings.isNullOrEmpty(cast.getActor().getPersonId())) {
                 Actor actor = Actor.actor(cast.getActor().getPersonId(), cast.getActor().getvalue(), cast.getCharacter(), Publisher.PA);
                 if (! people.contains(actor)) {
                     people.add(actor);
                 }
             }
         }
         
         for (StaffMember staffMember: progData.getStaffMember()) {
             if (!Strings.isNullOrEmpty(staffMember.getPerson().getPersonId())) {
                 String roleKey = staffMember.getRole().toLowerCase().replace(' ', '_');
                 CrewMember crewMember = CrewMember.crewMember(staffMember.getPerson().getPersonId(), staffMember.getPerson().getvalue(), roleKey, Publisher.PA);
                 if (! people.contains(crewMember)) {
                     people.add(crewMember);
                 }
             }
         }
         
         return people;
     }
 
     private Version findBestVersion(Iterable<Version> versions) {
         for (Version version : versions) {
             if (version.getProvider() == Publisher.PA) {
                 return version;
             }
         }
 
         return versions.iterator().next();
     }
     
     private Film getBasicFilm(ProgData progData) {
         Film film = new Film(PaHelper.getFilmUri(programmeId(progData)), PaHelper.getFilmCurie(programmeId(progData)), Publisher.PA);
         
         setBasicDetails(progData, film);
         
         return film;
     }
 
     private Item getBasicEpisode(ProgData progData, boolean isEpisode) {
         Item item = isEpisode ? new Episode() : new Item();
         item.setCanonicalUri(PaHelper.getEpisodeUri(programmeId(progData)));
         item.setCurie("pa:e-" + programmeId(progData));
         item.setPublisher(Publisher.PA);
         setBasicDetails(progData, item);
         return item;
     }
     
     private void setBasicDetails(ProgData progData, Item item) {
         Version version = new Version();
         version.setProvider(Publisher.PA);
         item.addVersion(version);
 
         Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));
         version.setDuration(duration);
 
         item.addVersion(version);
     }
     
     protected Specialization specialization(ProgData progData, Channel channel) {
         if (channelMap.isRadioChannel(channel)) {
             return Specialization.RADIO;
         }
         return Strings.isNullOrEmpty(progData.getRtFilmnumber()) ? Specialization.TV : Specialization.FILM;
     }
 
     protected static DateTime getTransmissionTime(String date, String time, DateTimeZone zone) {
         String dateString = date + "-" + time;
         return DateTimeFormat.forPattern("dd/MM/yyyy-HH:mm").withZone(zone).parseDateTime(dateString);
     }
     
     protected static String programmeId(ProgData progData) {
         return ! Strings.isNullOrEmpty(progData.getRtFilmnumber()) ? progData.getRtFilmnumber() : progData.getProgId();
     }
     
     private static Boolean getBooleanValue(String value) {
         if (value != null) {
             return value.equalsIgnoreCase(YES);
         }
         return null;
     }
     
     public static final class ItemAndBroadcast {
     	
     	private final Item item;
     	private final Broadcast broadcast;
     	
     	public ItemAndBroadcast(Item item, Broadcast broadcast) {
     		this.item = item;
     		this.broadcast = broadcast;
     	}
     	
     	@Override 
     	public int hashCode() {
     		return Objects.hashCode(item, broadcast);
     	}
     	
     	@Override
     	public boolean equals(Object obj) {
     		if(obj instanceof ItemAndBroadcast) {
     			ItemAndBroadcast other = (ItemAndBroadcast) obj;
     			return item.equals(other.item) && broadcast.equals(other.broadcast);
     		}
     		return false;
     	}
     	
     	public Broadcast getBroadcast() {
     		return broadcast;
     	}
     	
     	public Item getItem() {
     		return item;
     	}
     }
 }
