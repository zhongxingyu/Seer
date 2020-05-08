 package org.atlasapi.remotesite.channel4;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.atlasapi.remotesite.support.atom.AtomClient;
 
 import com.google.common.base.Optional;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.HttpStatusCode;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.url.Urls;
 import com.sun.syndication.feed.atom.Feed;
 
 public class C4AtomApiClient {
     
     private final Log log = LogFactory.getLog(getClass());
 
     private final AtomClient client;
     private final String apiUrlBase;
     private final Optional<String> platform;
 
     private final C4LinkBrandNameExtractor linkExtractor = new C4LinkBrandNameExtractor();
 
     public C4AtomApiClient(SimpleHttpClient client, String apiUrlBase, Optional<String> platform) {
         this.client = new AtomClient(client);
         this.apiUrlBase = apiUrlBase;
         this.platform = platform;
     }
     
     public Optional<Feed> brandFeed(String canonicalBrandUri) {
         return getFeed(canonicalBrandUri, "");
     }
 
     public Optional<Feed> brand4oDFeed(String canonicalBrandUri) {
         return getFeed(canonicalBrandUri, "/4od");
     }
     
     public Optional<Feed> brandEpisodeGuideFeed(String canonicalBrandUri) {
         return getFeed(canonicalBrandUri, "/episode-guide");
     }
     
     public Optional<Feed> seriesEpisodeGuideFeed(String canonicalBrandUri, int seriesNumber) {
         return getFeed(canonicalBrandUri, "/episode-guide/series-" + seriesNumber);
     }
 
     public Optional<Feed> brandEpgFeed(String canonicalBrandUri) {
         return getFeed(canonicalBrandUri, "/epg");
     }
 
     public Optional<Feed> brandVideoFeed(String canonicalBrandUri) {
         return getFeed(canonicalBrandUri, "/video");
     }
 
     private Optional<Feed> getFeed(String brandUri, String suffix) {
         String baseUri = String.format("%s%s%s.atom", apiUrlBase, brandName(brandUri), suffix);
         return get(optionallyAppendPlatform(baseUri));
     }
     
    private Optional<String> brandName(String canonicalBrandUri) {
        return linkExtractor.brandNameFrom(canonicalBrandUri);
     }
     
     private Optional<Feed> get(String uri) {
         Feed feed = null;
         try {
             feed = client.get(uri);
         } catch (HttpException e) {
             if (HttpStatusCode.NOT_FOUND.code() == e.getResponse().statusCode()) {
                 log.warn(uri + " not found");
             } else {
                 log.error(e.getResponse().statusCode() + " exception fetching " + uri, e);
             }
         } catch (Exception e) {
             log.error("Exception fetching " + uri, e);
         }
         return Optional.fromNullable(feed);
     }
     
     private String appendPlatform(String url) {
         return Urls.appendParameters(url, "platform", platform.get());
     }
     
     private String optionallyAppendPlatform(String url) {
         return platform.isPresent() ? appendPlatform(url) : url;
     }
 
 }
