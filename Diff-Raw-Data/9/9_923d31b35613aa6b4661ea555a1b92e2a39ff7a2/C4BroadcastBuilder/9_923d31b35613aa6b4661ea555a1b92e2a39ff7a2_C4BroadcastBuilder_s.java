 package org.atlasapi.remotesite.channel4;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.entity.Broadcast;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.ImmutableBiMap;
 
 public class C4BroadcastBuilder {
 
     private final static BiMap<String, String> CHANNEL_MAP = ImmutableBiMap.<String,String>builder()
         .put("http://www.channel4.com", "C4")
         .put("http://www.channel4.com/more4", "M4")
         .put("http://film4.com", "F4")
         .put("http://www.e4.com", "E4")
         .put("http://www.4music.com", "4M")
        .put("www.channel4.com/4seven", "4S").build();
     
     private final static Pattern ID_PATTERN = Pattern.compile("tag:([^,]+),(\\d{4}):slot/(\\d+)");
     
     private String channelUri;
     private DateTime start;
     private Duration duration;
     private String atomId;
 
     public static C4BroadcastBuilder broadcast() {
         return new C4BroadcastBuilder();
     }
     
     private C4BroadcastBuilder() {
         
     }
     
     public C4BroadcastBuilder withChannel(String channelUri) {
         checkArgument(CHANNEL_MAP.keySet().contains(channelUri), "Unknown channel " + channelUri);
         this.channelUri = channelUri;
         return this;
     }
     
     public C4BroadcastBuilder withTransmissionStart(DateTime start) { 
         this.start = start;
         return this;
     }
     
     public C4BroadcastBuilder withDuration(Duration duration) {
         this.duration = duration;
         return this;
     }
     
     public C4BroadcastBuilder withAtomId(String atomId) {
         this.atomId = atomId;
         return this;
     }
     
     public Broadcast build() {
         checkState(channelUri != null, "Can't build broadcast without channel URI");
         checkState(start != null, "Can't build broadcast without transmission start");
         checkState(duration != null, "Can't build broadcast without duration");
         checkState(atomId != null, "Can't build broadcast without Atom ID");
         checkArgument(ID_PATTERN.matcher(atomId).matches());
         
         Broadcast broadcast = new Broadcast(channelUri, start, duration).withId(idFrom(channelUri, atomId));
         broadcast.addAlias(aliasFrom(channelUri, atomId));
         return broadcast;
     }
 
     public static String aliasFrom(String channelUri, String id) {
         int slashIndex = id.lastIndexOf("/")+1;
         return id.substring(0, slashIndex) + CHANNEL_MAP.get(channelUri) + id.substring(slashIndex);
     }
     
     public static String idFrom(String channelUri, String atomId) {
         Matcher matcher = C4AtomApi.SLOT_PATTERN.matcher(atomId);
         if(matcher.matches()) {
             return CHANNEL_MAP.get(channelUri).toLowerCase() + ":" + matcher.group(1);
         }
         throw new IllegalStateException("Couldn't extract slot id from " + atomId);
     }
 }
