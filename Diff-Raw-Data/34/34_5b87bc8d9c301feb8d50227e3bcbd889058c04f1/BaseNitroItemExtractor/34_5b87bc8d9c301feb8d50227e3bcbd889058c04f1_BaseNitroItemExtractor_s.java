 package org.atlasapi.remotesite.bbc.nitro.extract;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.remotesite.bbc.BbcFeeds;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSetMultimap;
 import com.google.common.collect.ImmutableSetMultimap.Builder;
 import com.google.common.collect.Sets;
 import com.metabroadcast.atlas.glycerin.model.Availability;
 import com.metabroadcast.atlas.glycerin.model.PidReference;
 import com.metabroadcast.atlas.glycerin.model.Programme;
 
 /**
  * Base extractor for extracting common properties of {@link Item}s from a
  * {@link NitroItemSource}.
  * 
  * @param <SOURCE> - the type of {@link Programme}.
 * @param <CONTENT> - the {@link Item} type extracted.
  */
public abstract class BaseNitroItemExtractor<SOURCE, CONTENT extends Item>
    extends NitroContentExtractor<NitroItemSource<SOURCE>, CONTENT> {
 
     private final NitroBroadcastExtractor broadcastExtractor
         = new NitroBroadcastExtractor();
     private final NitroAvailabilityExtractor availabilityExtractor
         = new NitroAvailabilityExtractor();
     
     @Override
    protected void extractAdditionalFields(NitroItemSource<SOURCE> source, CONTENT content) {
         ImmutableSetMultimap<String, Broadcast> broadcasts = extractBroadcasts(source.getBroadcasts());
         ImmutableSetMultimap<String, Encoding> encodings = extractEncodings(source.getAvailabilities());
         
         ImmutableSet.Builder<Version> versions = ImmutableSet.builder();
         for (String versionPid : Sets.union(broadcasts.keySet(), encodings.keySet())) {
             Version version = new Version();
             version.setCanonicalUri(BbcFeeds.nitroUriForPid(versionPid));
             version.setBroadcasts(broadcasts.get(versionPid));
             version.setManifestedAs(encodings.get(versionPid));
         }
        content.setVersions(versions.build());
     }
 
     private ImmutableSetMultimap<String, Encoding> extractEncodings(List<Availability> availabilities) {
         Builder<String, Encoding> encodings = ImmutableSetMultimap.builder();
         for (Availability availability : availabilities) {
             encodings.put(checkNotNull(NitroUtil.versionPid(availability)), availabilityExtractor.extract(availability));
         }
         return encodings.build();
     }
 
     private ImmutableSetMultimap<String, Broadcast> extractBroadcasts(
             List<com.metabroadcast.atlas.glycerin.model.Broadcast> nitroBroadcasts) {
         Builder<String, Broadcast> broadcasts = ImmutableSetMultimap.builder();
         for (com.metabroadcast.atlas.glycerin.model.Broadcast broadcast : nitroBroadcasts) {
             broadcasts.put(versionPid(broadcast), broadcastExtractor.extract(broadcast));
         }
         return broadcasts.build();
     }
 
     private String versionPid(com.metabroadcast.atlas.glycerin.model.Broadcast broadcast) {
         for (PidReference pidRef : broadcast.getBroadcastOf()) {
             if ("version".equals(pidRef.getResultType())) {
                 return pidRef.getPid();
             }
         }
         throw new IllegalArgumentException(String.format("No version ref for %s %s",
                 broadcast.getClass().getSimpleName(), broadcast.getPid()));
     }
     
 }
