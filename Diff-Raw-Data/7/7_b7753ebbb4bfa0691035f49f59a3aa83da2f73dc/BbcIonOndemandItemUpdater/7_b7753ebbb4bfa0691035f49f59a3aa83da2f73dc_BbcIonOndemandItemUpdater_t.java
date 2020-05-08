 package org.atlasapi.remotesite.bbc.ion;
 
 import java.util.Set;
 
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
 import org.atlasapi.remotesite.bbc.BbcProgrammeGraphExtractor;
 import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
 import org.joda.time.Interval;
 
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.SystemClock;
 
 public class BbcIonOndemandItemUpdater {
 
     private final Clock clock;
     private BbcProgrammeEncodingAndLocationCreator encodingCreator;
     
     public BbcIonOndemandItemUpdater() {
         this(new SystemClock());
     }
 
     public BbcIonOndemandItemUpdater(Clock clock) {
         this.clock = clock;
         this.encodingCreator = new BbcProgrammeEncodingAndLocationCreator(clock);
     }
 
     public void updateItemDetails(Item item, IonOndemandChange change) {
         boolean revoked = "revoked".equals(change.getRevocationStatus());
         Maybe<Version> version = version(item.getVersions(), BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE + change.getVersionId());
 
         if (version.hasValue()) {
             processVersion(change, revoked, version.requireValue());
         }
     }
 
     private void processVersion(IonOndemandChange change, boolean revoked, Version version) {
         Maybe<Encoding> encoding = encoding(version.getManifestedAs(), BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE + change.getId());
         if (encoding.hasValue()) {
             Maybe<Location> location = location(encoding.requireValue().getAvailableAt(),
                     BbcProgrammeGraphExtractor.iplayerPageFrom(BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE + change.getEpisodeId()));
             if (location.hasValue()) {
                 if (!revoked) {
                     updateAvailability(location.requireValue(), change);
                 } else {
                     removeLocation(encoding.requireValue(), location.requireValue());
                 }
             } else if (!revoked) {
                 Location newLocation = encodingCreator.location(change);
                 encoding.requireValue().addAvailableAt(newLocation);
             }
         } else if (!revoked) {
             Encoding newEncoding = encodingCreator.createEncoding(change);
            if (newEncoding != null) {
                 version.addManifestedAs(newEncoding);
             }
         }
     }
 
     private void removeLocation(Encoding encoding, Location location) {
         if (location.getUri() != null) {
             Set<Location> locations = Sets.newHashSet();
 
             for (Location loc : encoding.getAvailableAt()) {
                 if (!loc.getUri().equals(location.getUri())) {
                     locations.add(loc);
                 }
             }
 
             encoding.setAvailableAt(locations);
         }
     }
 
     private Maybe<Version> version(Set<Version> versions, String versionId) {
         for (Version version : versions) {
             if (versionId.equals(version.getCanonicalUri())) {
                 return Maybe.just(version);
             }
         }
         return Maybe.nothing();
     }
 
     private Maybe<Location> location(Set<Location> locations, String uri) {
         for (Location location : locations) {
             if (uri.equals(location.getUri())) {
                 return Maybe.just(location);
             }
         }
         return Maybe.nothing();
     }
 
     private void updateAvailability(Location location, IonOndemandChange change) {
         Policy policy = location.getPolicy();
         policy.setAvailabilityStart(change.getScheduledStart());
         policy.setAvailabilityEnd(change.getDiscoverableEnd());
 
         location.setAvailable(availableNow(policy));
     }
 
     private boolean availableNow(Policy policy) {
         return new Interval(policy.getAvailabilityStart(), policy.getAvailabilityEnd()).contains(clock.now());
     }
 
     private Maybe<Encoding> encoding(Set<Encoding> encodings, String encodingUri) {
         for (Encoding encoding : encodings) {
             if (encodingUri.equals(encoding.getCanonicalUri())) {
                 return Maybe.just(encoding);
             }
         }
         return Maybe.nothing();
     }
 }
