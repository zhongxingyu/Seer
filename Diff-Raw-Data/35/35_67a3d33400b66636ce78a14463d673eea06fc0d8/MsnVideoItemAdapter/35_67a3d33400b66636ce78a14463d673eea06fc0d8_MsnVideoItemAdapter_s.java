 package org.atlasapi.remotesite.msnvideo;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.TransportSubType;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Policy.RevenueContract;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 import org.atlasapi.remotesite.html.HtmlNavigator;
 import org.jdom.Element;
 
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 public class MsnVideoItemAdapter implements SiteSpecificAdapter<Episode> {
     private static final Pattern itemUriPattern = Pattern.compile("http://video.uk.msn.com/watch/video/([\\w\\-\\+%]+)/(\\w+)");
     private final SimpleHttpClient client;
     private final AdapterLog log;
     
     public MsnVideoItemAdapter(SimpleHttpClient client, AdapterLog log) {
         this.client = client;
         this.log = log;
     }
     
     @Override
     public Episode fetch(String uri) {
         String content;
         try {
             content = client.getContentsOf(uri);
         
             HtmlNavigator navigator = new HtmlNavigator(content);
             
             Element infoPaneElement = navigator.firstElementOrNull("//div[@data-type='infoPane']//div[@class='leftInfoPanel']");
             
             List<Element> rows = infoPaneElement.getChildren("div");
             Iterator<Element> rowsIterator = rows.iterator();
             
             List<Element> titleElements = rowsIterator.next().getChildren();
             
             Matcher uriMatcher = itemUriPattern.matcher(uri);
             uriMatcher.matches();
             String itemKey = uriMatcher.group(1);
             String id = uriMatcher.group(2);
             
             Episode episode = new Episode(uri, "msn:" + id + "-" + itemKey, Publisher.MSN_VIDEO);
             
             String episodeTitle = titleElements.get(2).getText();
             episode.setTitle(episodeTitle);
             
             List<Element> numbersElements = rowsIterator.next().getChildren();
             Integer seriesNumber = Integer.valueOf(numbersElements.get(0).getText());
             Integer episodeNumber = Integer.valueOf(numbersElements.get(1).getText());
             episode.setSeriesNumber(seriesNumber);
             episode.setEpisodeNumber(episodeNumber);
             
             String description = rowsIterator.next().getTextTrim();
             episode.setDescription(description);
             
             Version version = new Version();
             Encoding encoding = new Encoding();
             Location linkLocation = new Location();
             linkLocation.setUri(uri);
             linkLocation.setTransportType(TransportType.LINK);
             linkLocation.setTransportSubType(TransportSubType.HTTP);
             linkLocation.setPolicy(new Policy().withAvailableCountries(Countries.GB).withRevenueContract(RevenueContract.FREE_TO_VIEW));
             encoding.addAvailableAt(linkLocation);
             version.addManifestedAs(encoding);
             episode.addVersion(version);
             
             return episode;
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(MsnVideoItemAdapter.class).withUri(uri));
         }
         
         return null;
     }
 
     @Override
     public boolean canFetch(String uri) {
         return itemUriPattern.matcher(uri).matches();
     }
 
 }
