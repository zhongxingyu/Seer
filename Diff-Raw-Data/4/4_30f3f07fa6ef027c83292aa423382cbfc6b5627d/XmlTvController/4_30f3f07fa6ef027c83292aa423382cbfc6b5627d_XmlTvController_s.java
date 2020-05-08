 package org.atlasapi.feeds.xmltv;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.feeds.xmltv.XmlTvChannelLookup.XmlTvChannel;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Strings;
 import com.google.common.collect.Range;
 import com.google.common.collect.Ranges;
 import com.metabroadcast.common.http.HttpStatusCode;
 import com.metabroadcast.common.media.MimeType;
 import com.metabroadcast.common.webapp.health.HealthController;
 
 @Controller
 public class XmlTvController {
 
     private final DateTimeFormatter dateFormat = ISODateTimeFormat.basicDate();
     private final XmlTvFeedCompiler feedCompiler;
     private final XmlTvChannelsCompiler channelsCompiler;
     private final Map<Integer, XmlTvChannel> channelLookup;
     private final HealthController health;
 
     public XmlTvController(XmlTvFeedCompiler feedCompiler, Map<Integer, XmlTvChannel> channelLookup, HealthController health) {
         this.feedCompiler = feedCompiler;
         this.channelsCompiler = new XmlTvChannelsCompiler(channelLookup);
         this.channelLookup = channelLookup;
         this.health = health;
     }
     
     @RequestMapping("/feeds/xmltv/health")
     public String health(HttpServletResponse response) throws IOException {
        return health.showHealthPageForSlugs(response, "xmltv");
     }
     
     @RequestMapping("/feeds/xmltv/{id}.dat")
     public void getFeed(HttpServletResponse response, @PathVariable Integer id, @RequestParam(value="from",required=false) String startDay) throws IOException {
         
         XmlTvChannel channel = channelLookup.get(id);
         
         if (channel == null) {
             response.sendError(HttpStatusCode.NOT_FOUND.code(), String.format("Channel %s not found", id));
         }
         
         LocalDate startDate;
         if(Strings.isNullOrEmpty(startDay)) {
             startDate = new LocalDate();
         } else {
             if(startDay.matches("\\d{8}")) {
                 startDate = dateFormat.parseDateTime(startDay).toLocalDate();
             } else {
                 response.sendError(HttpStatusCode.BAD_REQUEST.code(), String.format("Invalid start date: %s",startDay));
                 return;
             }
         }
 
         response.setContentType(MimeType.TEXT_PLAIN.toString());
         response.setCharacterEncoding(Charsets.UTF_8.displayName());
         feedCompiler.compileChannelFeed(daysFrom(startDate), channel.channel(), response.getOutputStream());
         
     }
 
     private Range<LocalDate> daysFrom(LocalDate startDay) {
         return Ranges.closed(startDay, startDay.plusWeeks(2));
     }
     
     @RequestMapping("/feeds/xmltv/channels.dat")
     public void getChannels(HttpServletResponse response) throws IOException {
         response.setContentType(MimeType.TEXT_PLAIN.toString());
         response.setCharacterEncoding(Charsets.UTF_8.displayName());
         channelsCompiler.compileChannelsFeed(response.getOutputStream());
     }
 }
