 package org.atlasapi.remotesite.itv;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.remotesite.ContentExtractor;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.url.UrlEncoding;
 
 public class ItvMercuryBrandExtractor implements ContentExtractor<Map<String, Object>, Brand> {
 
     private final ItvMercuryEpisodeExtractor episodeExtractor = new ItvMercuryEpisodeExtractor();
     private final static ItvGenreMap genreMap = new ItvGenreMap();
    private final static Pattern IMAGE = Pattern.compile(".*\\d+x\\d+(/.+.jpg)");
 
     @SuppressWarnings("unchecked")
     @Override
     public Brand extract(Map<String, Object> values) {
         Brand brand = null;
 
         if (values.containsKey("Result")) {
             List<Map<String, Object>> results = (List<Map<String, Object>>) values.get("Result");
             for (Map<String, Object> result : results) {
 
                 if (result.containsKey("Details")) {
                     List<Map<String, Object>> programmes = (List<Map<String, Object>>) result.get("Details");
 
                     for (Map<String, Object> programme : programmes) {
                         Map<String, Object> progInfo = (Map<String, Object>) programme.get("Programme");
 
                         String title = null;
                         if (progInfo.containsKey("Programme")) {
                             Map<String, Object> info = (Map<String, Object>) progInfo.get("Programme");
                             title = (String) info.get("Title");
                         }
                         
                         if (title == null) {
                             return null;
                         }
                         title = title.trim();
                         
                         String id = UrlEncoding.encode(title).replace("+", "%20");
                         String uri = ItvMercuryBrandAdapter.BASE_URL + id;
                         String curie = "itv:" + id;
                         brand = new Brand(uri, curie, Publisher.ITV);
                         brand.setTitle(title);
                        brand.addAlias(UrlEncoding.decode(uri));
                         
                         brand.setDescription((String) progInfo.get("ShortSynopsis"));
                         brand.setGenres(genres((String) progInfo.get("Genres")));
                         if (progInfo.containsKey("AdditionalContentUri")) {
                             brand.addAlias((String) progInfo.get("AdditionalContentUri"));
                         }
                        brand.setImage(getImage((String) progInfo.get("ImageUri")));
                        brand.setThumbnail(getThumbnail((String) progInfo.get("ImageUri")));
 
                         List<Map<String, Object>> episodes = (List<Map<String, Object>>) programme.get("Episodes");
                         for (Map<String, Object> episodeInfo : episodes) {
                             Episode episode = episodeExtractor.extract(episodeInfo);
                             if (episode != null) {
                                 brand.addItem(episode);
                             }
                         }
                     }
                 }
             }
         }
 
         return brand;
     }
 
     static protected Set<String> genres(String genre) {
         if (genre != null && genre.contains(".")) {
             String[] genres = genre.split("\\.", 2);
             if (genres.length > 0) {
                 return genreMap.map(ImmutableSet.of(genres[0].toLowerCase()));
             }
         }
         return ImmutableSet.of();
     }
    
    static protected String getImage(String imageUrl) {
        Matcher matcher = IMAGE.matcher(imageUrl);
        if (matcher.matches()) {
            return "http://www.itv.com//img/217x122"+matcher.group(1);
        }
        return null;
    }
    
    static protected String getThumbnail(String imageUrl) {
        Matcher matcher = IMAGE.matcher(imageUrl);
        if (matcher.matches()) {
            return "http://www.itv.com//img/118x65"+matcher.group(1);
        }
        return null;
    }
 }
