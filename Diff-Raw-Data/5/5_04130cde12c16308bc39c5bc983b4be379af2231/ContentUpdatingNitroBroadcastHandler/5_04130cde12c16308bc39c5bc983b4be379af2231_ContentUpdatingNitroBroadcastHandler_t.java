 package org.atlasapi.remotesite.bbc.nitro;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.remotesite.bbc.BbcFeeds;
 import org.atlasapi.remotesite.bbc.nitro.extract.NitroBroadcastExtractor;
 import org.atlasapi.remotesite.bbc.nitro.extract.NitroUtil;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
 import com.metabroadcast.atlas.glycerin.model.PidReference;
 import com.metabroadcast.common.time.Clock;
 
 /**
  * {@link NitroBroadcastHandler} which fetches, updates and writes relevant
  * content for the {@link Broadcast}.
  */
 public class ContentUpdatingNitroBroadcastHandler implements NitroBroadcastHandler<ItemRefAndBroadcast> {
 
     private final ContentWriter writer;
     private final LocalOrRemoteNitroFetcher localOrRemoteFetcher;
     
     private final NitroBroadcastExtractor broadcastExtractor
         = new NitroBroadcastExtractor();
 
     public ContentUpdatingNitroBroadcastHandler(ContentResolver resolver, ContentWriter writer, NitroContentAdapter contentAdapter, Clock clock) {
         this.writer = writer;
         this.localOrRemoteFetcher = new LocalOrRemoteNitroFetcher(resolver, contentAdapter, clock);
     }
     
     @Override
     public ItemRefAndBroadcast handle(com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast) throws NitroException {
         
         Item item = localOrRemoteFetcher.resolveOrFetchItem(nitroBroadcast);
         Optional<Series> series = localOrRemoteFetcher.resolveOrFetchSeries(item);
         Optional<Brand> brand = localOrRemoteFetcher.resolveOrFetchBrand(item);
         
         Broadcast broadcast = broadcastExtractor.extract(nitroBroadcast);
         addBroadcast(item, versionUri(nitroBroadcast), broadcast);
         if (brand.isPresent()) {
             writer.createOrUpdate(brand.get());
         } 
         if (series.isPresent()) {
             writer.createOrUpdate(series.get());
         }
         writer.createOrUpdate(item);
         
         return new ItemRefAndBroadcast(item, broadcast);
     }
 
     private void addBroadcast(Item item, String versionUri, Broadcast broadcast) {
         Version version = Objects.firstNonNull(getVersion(item, versionUri), newVersion(versionUri));
        version.setBroadcasts(Sets.union(ImmutableSet.of(broadcast), version.getBroadcasts()));
         item.addVersion(version);
     }
 
     private Version getVersion(Item item, String versionUri) {
         for (Version version : item.getVersions()) {
             if (versionUri.equals(version.getCanonicalUri())) {
                 return version;
             }
         }
         return null;
     }
 
     private Version newVersion(String versionUri) {
         Version version = new Version();
         version.setCanonicalUri(versionUri);
         return version;
     }
     
     private String versionUri(com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast) {
         PidReference pidRef = NitroUtil.versionPid(nitroBroadcast);
         checkArgument(pidRef != null,"Broadcast %s has no version ref", nitroBroadcast.getPid());
         return BbcFeeds.nitroUriForPid(pidRef.getPid());
     }
 
 
 }
