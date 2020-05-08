 package org.atlasapi.remotesite.lovefilm;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import com.metabroadcast.common.intl.Countries;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import nu.xom.Document;
 import nu.xom.Node;
 import nu.xom.Nodes;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormat;
 
 /**
  */
 public class LoveFilmFilmProcessor {
 
     public void process(Document content, SimpleHttpClient client, ContentResolver contentResolver, ContentWriter contentWriter) throws Exception {
         Node root = content.getRootElement();
         String uri = querySingleValue(root, "id", null);
 
         Film film = (Film) contentResolver.findByCanonicalUris(ImmutableSet.of(uri)).getFirstValue().valueOrNull();
         if (film == null) {
             film = new Film();
         }
 
         film.setCanonicalUri(uri);
         film.setYear(Integer.parseInt(querySingleValue(root, "production_year", null)));
         film.setCurie("lovefilm:b-" + uri);
         film.setPublisher(Publisher.LOVEFILM);
 
         film.setTitle(querySingleValue(root, "title/@clean", null));
 
         Nodes synopsisLink = root.query("link[@title='synopsis']/@href");
         if (synopsisLink.size() == 1) {
             Node synopsis = client.get(new SimpleHttpRequest<Document>(synopsisLink.get(0).getValue(), new XmlHttpResponseTransformer())).getRootElement();
             film.setDescription(querySingleValue(synopsis, "synopsis_text", null));
         }
 
         film.setPeople(new ArrayList<CrewMember>());
 
         Nodes actorsLink = root.query("link[@title='actors']/@href");
         if (actorsLink.size() == 1) {
             Nodes actors = client.get(new SimpleHttpRequest<Document>(actorsLink.get(0).getValue(), new XmlHttpResponseTransformer())).getRootElement().query("link");
             for (int i = 0; i < actors.size(); i++) {
                 Node current = actors.get(i);
                 CrewMember actor = new CrewMember();
                actor.withRole(CrewMember.Role.ACTOR).withName(querySingleValue(current, "@title", null));
                 film.addPerson(actor);
             }
         }
 
         Nodes directorsLink = root.query("link[@title='directors']/@href");
         if (directorsLink.size() == 1) {
             Nodes directors = client.get(new SimpleHttpRequest<Document>(directorsLink.get(0).getValue(), new XmlHttpResponseTransformer())).getRootElement().query("link");
             for (int i = 0; i < directors.size(); i++) {
                 Node current = directors.get(i);
                 CrewMember director = new CrewMember();
                director.withRole(CrewMember.Role.DIRECTOR).withName(querySingleValue(current, "@title", null));
                 film.addPerson(director);
             }
         }
 
         Nodes artworksLink = root.query("link[@title='artworks']/@href");
         if (artworksLink.size() == 1) {
             Node artworks = client.get(new SimpleHttpRequest<Document>(artworksLink.get(0).getValue(), new XmlHttpResponseTransformer())).getRootElement();
             Nodes thumbnails = artworks.query("artwork[@type='hero']/image[@size='small']/@href");
             if (thumbnails.size() > 0) {
                 film.setThumbnail(thumbnails.get(0).getValue());
             }
             Nodes images = artworks.query("artwork[@type='hero']/image[@size='large']/@href");
             if (images.size() > 0) {
                 film.setImage(images.get(0).getValue());
             }
         }
 
         Nodes genres = root.query("category[@scheme='http://openapi.lovefilm.com/categories/genres']");
         Set<String> sourceGenres = new HashSet<String>();
         LoveFilmGenreMap destinationGenres = new LoveFilmGenreMap();
         for (int i = 0; i < genres.size(); i++) {
             Node current = genres.get(i);
             sourceGenres.add(querySingleValue(current, "@term", null));
         }
         film.setGenres(destinationGenres.mapRecognised(sourceGenres));
 
         Version version = new Version();
         version.setDuration(Duration.standardSeconds(Integer.parseInt(querySingleValue(root, "run_time", "0")) * 60));
         version.setProvider(Publisher.LOVEFILM);
 
         boolean rentable = Boolean.parseBoolean(querySingleValue(root, "can_rent", "false"));
         if (rentable) {
             Encoding encoding = new Encoding();
             Location location = new Location();
             Policy policy = new Policy();
             encoding.addAvailableAt(location);
             location.setAvailable(true);
             location.setTransportType(TransportType.LINK);
             location.setUri(querySingleValue(root, "link[@rel='alternate']/@href", null));
             location.setPolicy(policy);
             policy.setRevenueContract(Policy.RevenueContract.SUBSCRIPTION);
             policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
             if (querySingleValue(root, "release_date", null) != null) {
                 policy.setAvailabilityStart(DateTimeFormat.forPattern("YYYY-MM-dd").parseDateTime(querySingleValue(root, "release_date", null)));
             }
             version.addManifestedAs(encoding);
         }
 
         film.setVersions(new HashSet<Version>());
         film.addVersion(version);
 
         contentWriter.createOrUpdate(film);
     }
 
     private String querySingleValue(Node parent, String xpath, String defaultValue) {
         Nodes nodes = parent.query(xpath);
         if (nodes.size() == 1) {
             return nodes.get(0).getValue();
         } else {
             return defaultValue;
         }
 
     }
 }
