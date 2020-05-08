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
 * @param <ITEM> - the {@link Item} type extracted.
  */
public abstract class BaseNitroItemExtractor<SOURCE, ITEM extends Item>
    extends NitroContentExtractor<NitroItemSource<SOURCE>, ITEM> {
 
     private final NitroBroadcastExtractor broadcastExtractor
         = new NitroBroadcastExtractor();
     private final NitroAvailabilityExtractor availabilityExtractor
         = new NitroAvailabilityExtractor();
     
     @Override
    protected final void extractAdditionalFields(NitroItemSource<SOURCE> source, ITEM item) {
         ImmutableSetMultimap<String, Broadcast> broadcasts = extractBroadcasts(source.getBroadcasts());
         ImmutableSetMultimap<String, Encoding> encodings = extractEncodings(source.getAvailabilities());
         
         ImmutableSet.Builder<Version> versions = ImmutableSet.builder();
         for (String versionPid : Sets.union(broadcasts.keySet(), encodings.keySet())) {
             Version version = new Version();
             version.setCanonicalUri(BbcFeeds.nitroUriForPid(versionPid));
             version.setBroadcasts(broadcasts.get(versionPid));
             version.setManifestedAs(encodings.get(versionPid));
            versions.add(version);
         }
        item.setVersions(versions.build());
        extractAdditionalItemFields(source, item);
    }

    /**
     * Concrete implementations can override this method to perform additional
     * configuration of the extracted content from the source.
     * 
     * @param source - the source data.
     * @param item - the extracted item.
     */
    protected void extractAdditionalItemFields(NitroItemSource<SOURCE> source, ITEM item) {
        
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
