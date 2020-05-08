 package org.atlasapi.feeds.interlinking.www;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.content.criteria.ContentQueryBuilder;
 import org.atlasapi.content.criteria.attribute.Attributes;
 import org.atlasapi.feeds.interlinking.DelegatingPlaylistToInterlinkAdapter;
 import org.atlasapi.feeds.interlinking.PlaylistToInterlinkFeed;
 import org.atlasapi.feeds.interlinking.PlaylistToInterlinkFeedAdapter;
 import org.atlasapi.feeds.interlinking.outputting.InterlinkFeedOutputter;
 import org.atlasapi.feeds.interlinking.validation.InterlinkOutputValidator;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.media.MimeType;
 import com.metabroadcast.common.time.DateTimeZones;
 
 @Controller
 public class InterlinkController {
     
    private final static String FEED_ID = "https://www.channel4.com/linking/";
     private final ContentResolver resolver;
     private final InterlinkFeedOutputter outputter = new InterlinkFeedOutputter();
     private final PlaylistToInterlinkFeed adapter;
     private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZones.LONDON);
     private final KnownTypeQueryExecutor executor;
     private final InterlinkOutputValidator validator = new InterlinkOutputValidator();
 
     public InterlinkController(ContentResolver resolver, KnownTypeQueryExecutor executor, Map<Publisher, PlaylistToInterlinkFeed> delegates) {
         this.resolver = resolver;
         this.executor = executor;
         this.adapter = new DelegatingPlaylistToInterlinkAdapter(delegates, new PlaylistToInterlinkFeedAdapter());
     }
 
     @RequestMapping("/feeds/bbc-interlinking")
     public void showFeed(HttpServletResponse response, @RequestParam String uri) throws IOException {
         response.setContentType(MimeType.APPLICATION_ATOM_XML.toString());
         response.setStatus(HttpServletResponse.SC_OK);
         
         Playlist playlist = (Playlist) resolver.findByUri(uri);
         List<Brand> brands = Lists.newArrayList();
         for (Playlist subPlaylist: playlist.getPlaylists()) {
             if (subPlaylist instanceof Brand) {
                 brands.add((Brand) subPlaylist);
             }
         }
         
         outputter.output(adapter.fromBrands(playlist.getCanonicalUri(), playlist.getPublisher(), null, null, brands), response.getOutputStream());
     }
 
     @RequestMapping("/feeds/bbc-interlinking/{date}")
     public void updatedFeed(HttpServletResponse response, @PathVariable String date) throws IOException {
         DateTime from = fmt.parseDateTime(date);
         DateTime to = from.plusDays(1);
         response.setContentType(MimeType.APPLICATION_ATOM_XML.toString());
         response.setStatus(HttpServletResponse.SC_OK);
 
         ContentQuery query = ContentQueryBuilder.query()
                 .equalTo(Attributes.BRAND_PUBLISHER, Publisher.C4.key())
                 .after(Attributes.BRAND_THIS_OR_CHILD_LAST_UPDATED, from)
                 .before(Attributes.BRAND_THIS_OR_CHILD_LAST_UPDATED, to)
                 .build();
         List<Brand> brands = executor.executeBrandQuery(query);
         
         outputter.output(adapter.fromBrands(FEED_ID+date, Publisher.C4, from, to, brands), response.getOutputStream());
     }
     
     @RequestMapping("/feeds/bbc-interlinking/validate")
    public void valiudateFeed(HttpServletResponse response, @RequestParam String uri) throws Exception {
         response.setStatus(HttpServletResponse.SC_OK);
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         
         Playlist playlist = (Playlist) resolver.findByUri(uri);
         List<Brand> brands = Lists.newArrayList();
         for (Playlist subPlaylist: playlist.getPlaylists()) {
             if (subPlaylist instanceof Brand) {
                 brands.add((Brand) subPlaylist);
             }
         }
         
         outputter.output(adapter.fromBrands(playlist.getCanonicalUri(), playlist.getPublisher(), null, null, brands), out);
         
         validator.validatesAgainstSchema(out.toString(Charsets.UTF_8.toString()), response.getOutputStream());
     }
 }
