 package org.atlasapi.remotesite.hbo;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 import org.atlasapi.remotesite.xml.SimpleXmlNavigator;
 import org.jdom.Element;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 public class HboBrandAdapter implements SiteSpecificAdapter<Brand> {
     
     private final Pattern canFetchPattern = Pattern.compile("http://www.hbo.com/([a-z\\-A-Z0-9]+)/index.html");
     
     Set<String> categories = ImmutableSet.of("sports", "boxing", "documentaries", "movies", "comedy");
 
     private final SimpleHttpClient client;
     private final AdapterLog log;
     private final SiteSpecificAdapter<Episode> episodeAdapter;
 
     private final HboAdapterHelper helper;
     
     public HboBrandAdapter(SiteSpecificAdapter<Episode> episodeAdapter, SimpleHttpClient client, AdapterLog log, HboAdapterHelper helper) {
         this.episodeAdapter = episodeAdapter;
         this.client = client;
         this.log = log;
         this.helper = helper;
     }
     
     @Override
     public Brand fetch(String uri) {
         try {
             String brandContents = client.getContentsOf(helper.getXmlUri(uri));
             SimpleXmlNavigator brandNavigator = new SimpleXmlNavigator(brandContents);
             
             String episodesUri = helper.getNavigationLink("episodes", brandNavigator);
             String aboutUri = helper.getNavigationLink("about", brandNavigator);
             
             String episodesContents = client.getContentsOf(helper.getXmlUri(episodesUri));
             String aboutContents = client.getContentsOf(helper.getXmlUri(aboutUri));
         
             SimpleXmlNavigator episodesNavigator = new SimpleXmlNavigator(episodesContents);
             SimpleXmlNavigator aboutNavigator = new SimpleXmlNavigator(aboutContents);
             
             Matcher brandTitleMatcher = canFetchPattern.matcher(uri);
             brandTitleMatcher.matches();
             String brandKey = brandTitleMatcher.group(1);
             Brand brand = new Brand(uri, getCurie(brandKey), Publisher.HBO);
             
             String programTitle = episodesNavigator.firstElementOrNull("//metadata/program").getValue();
             brand.setTitle(programTitle);
             
             String formattedDescription = aboutNavigator.firstElementOrNull("//content/Article/article/body").getValue();
             brand.setDescription(helper.removeFormatting(formattedDescription));
             
             List<Element> episodeElements = episodesNavigator.allElementsMatching("//group[@id='sequentialNav']/group"); 
             
             for (Element episodeElement : episodeElements) {
                 extractEpisode(episodesNavigator, brand, episodeElement);
             }
             
             correctEpisodeNumbers(brand);
             
             return brand;
         }
         catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(uri).withSource(HboBrandAdapter.class)); 
         }
         return null;
     }
 
     private void extractEpisode(SimpleXmlNavigator episodesNavigator, Brand brand, Element episodeElement) {
         Element titleElement = episodesNavigator.firstElementOrNull("item[@id='title']/data", episodeElement);
         Pattern titlePattern = Pattern.compile("([0-9]+): (.*)");
         
         Element linkElement = episodesNavigator.firstElementOrNull("nav", episodeElement);
         String episodeUrl = linkElement.getValue().trim();
         String fullEpisodeUrl = helper.getAbsoluteUrl(episodeUrl);
         
         if (episodeAdapter.canFetch(fullEpisodeUrl)) {
             Episode episode = episodeAdapter.fetch(fullEpisodeUrl);
             if (episode != null) {
                 Element imageElement = episodesNavigator.firstElementOrNull("item[@id='image']/data", episodeElement);
                 String thumbnailUrl = imageElement.getValue().trim();
                 episode.setThumbnail(thumbnailUrl);
                 
                 brand.addItem(episode);
                 
                 Matcher titleMatcher = titlePattern.matcher(titleElement.getValue().trim());
                 titleMatcher.matches();
                 Integer episodeNumber = Integer.valueOf(titleMatcher.group(1));
                 
                 if (episodeNumber.equals(1)) {
                     brand.setImage(episode.getImage());
                     brand.setThumbnail(episode.getThumbnail());
                 }
             }
         }
     }
    
    private void addSeries(Brand brand, String brandKey) {
        Map<Integer, Series> numberToSeries = Maps.newHashMap();
        
        for (Item item : brand.getItems()) {
            Episode episode = (Episode) item;
            
            Series series = numberToSeries.get(episode.getSeriesNumber());
            if (series == null) {
                series = new Series(HboAdapterHelper.HBO_HOST + "/" + brandKey + "/episodes/" + episode.getSeriesNumber() , "hbo:" + brandKey + "-" + episode.getSeriesNumber());
            }
            
            series.addItem(episode);
        }
    }
 
     private void correctEpisodeNumbers(Brand brand) {
         Map<Integer, Integer> seriesToHighestEpisode = Maps.newHashMap();
         for (Item item : brand.getItems()) {
             Episode episode = (Episode) item;
             
             Integer currentHighest = seriesToHighestEpisode.get(episode.getSeriesNumber());
             
             if (currentHighest == null || currentHighest < episode.getEpisodeNumber()) {
                 seriesToHighestEpisode.put(episode.getSeriesNumber(), episode.getEpisodeNumber());
             }
         }
         
         for (Item item : brand.getItems()) {
             Episode episode = (Episode) item;
             
             if (episode.getSeriesNumber() > 1) {
                 Integer episodesToSubtract = seriesToHighestEpisode.get(episode.getSeriesNumber() - 1);
                 episode.setEpisodeNumber(episode.getEpisodeNumber() - episodesToSubtract);
             }
         }
     }
 
     @Override
     public boolean canFetch(String uri) {
         Matcher matcher = canFetchPattern.matcher(uri);
         
         if (matcher.matches()) {
             String showName = matcher.group(1);
             if (!isCategory(showName)) {
                 return true;
             }
         }
         return false;
     }
     
     private boolean isCategory(String showName) {
         for (String category : categories) {
             if (category.equalsIgnoreCase(showName)) {
                 return true;
             }
         }
         return false;
     }
     
     private String getCurie(String brandTitle) {
         return "hbo:" + brandTitle;
     }
 }
