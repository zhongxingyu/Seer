 package org.atlasapi.remotesite.bbc.ion;
 
 import java.util.Map;
 
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.Duration;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class BbcIonItemMerger {
 
     public Item merge(Item fetchedItem, Item existingItem) {
         if (fetchedItem instanceof Film) {
             return mergeFetchedFilm((Film) fetchedItem, existingItem);
         } else if (fetchedItem instanceof Episode) {
             return mergeFetchedEpisode((Episode) fetchedItem, existingItem);
         } else { //Fetched item is stand-alone.
             return mergeFetchedItem(fetchedItem, existingItem);
         }
     }
 
     private Item mergeFetchedItem(Item fetchedItem, Item existingItem) {
         if (existingItem instanceof Episode || existingItem instanceof Film) {
             //existing is not just an item, so copy its details to a new one.
             Item newItem = new Item();
             Item.copyTo(existingItem, newItem);
             existingItem = newItem;
         }
         return mergeItems(fetchedItem, existingItem);
     }
 
     private <T extends Item> T mergeItems(T fetchedItem, T existingItem) {
         //Identified attrs. Assume uri, curie are corrrect, equivs ignored.
         existingItem.addAliases(fetchedItem.getAliases());
         existingItem.setLastUpdated(ifNotNull(fetchedItem.getLastUpdated(), existingItem.getLastUpdated()));
         
         //Described attrs.
         existingItem.setTitle(ifNotNull(fetchedItem.getTitle(), existingItem.getTitle()));
         existingItem.setDescription(ifNotNull(fetchedItem.getDescription(), existingItem.getDescription()));
         existingItem.setMediaType(ifNotNull(fetchedItem.getMediaType(), existingItem.getMediaType()));
         existingItem.setSpecialization(ifNotNull(fetchedItem.getSpecialization(), existingItem.getSpecialization()));
         existingItem.setGenres(Iterables.concat(fetchedItem.getGenres(), existingItem.getGenres()));
         existingItem.setTags(ImmutableSet.copyOf(Iterables.concat(fetchedItem.getTags(), existingItem.getTags())));
         existingItem.setImage(ifNotNull(fetchedItem.getImage(), existingItem.getImage()));
         existingItem.setThumbnail(ifNotNull(fetchedItem.getThumbnail(), existingItem.getThumbnail()));
         existingItem.setPresentationChannel(ifNotNull(fetchedItem.getPresentationChannel(), fetchedItem.getPresentationChannel()));
         
         //Content attrs. Assume that clips themselves don't need merging.
         existingItem.setClips(ImmutableSet.copyOf(Iterables.concat(fetchedItem.getClips(), existingItem.getClips())));
         
         //Item attrs.
         existingItem.setParentRef(fetchedItem.getContainer());
         existingItem.setPeople(ImmutableList.copyOf(Sets.newLinkedHashSet(Iterables.concat(fetchedItem.getPeople(), existingItem.getPeople()))));
 
         //Versions...
         Map<String,Version> existingVersionMap = Maps.uniqueIndex(existingItem.getVersions(), Identified.TO_URI);
         for (Version fetchedVersion : fetchedItem.getVersions()) {
             Version existingVersion = existingVersionMap.get(fetchedVersion.getCanonicalUri());
             if(existingVersion != null) {
                 mergeVersions(fetchedVersion, existingVersion);
             } else {
                 existingItem.addVersion(fetchedVersion);
             }
         }
         
         return existingItem;
     }
     
     private void mergeVersions(Version fetchedVersion, Version existingVersion) {
         Integer intDuration = ifNotNull(fetchedVersion.getDuration(), existingVersion.getDuration());
        if(intDuration != null) {
            existingVersion.setDuration(Duration.standardSeconds(intDuration));
        }
         existingVersion.setBroadcasts(ImmutableSet.copyOf(Iterables.concat(fetchedVersion.getBroadcasts(), existingVersion.getBroadcasts())));
         existingVersion.setPublishedDuration(ifNotNull(fetchedVersion.getPublishedDuration(), existingVersion.getPublishedDuration()));
         existingVersion.setRestriction(ifNotNull(fetchedVersion.getRestriction(), existingVersion.getRestriction()));
         existingVersion.setManifestedAs(ImmutableSet.copyOf(Iterables.concat(fetchedVersion.getManifestedAs(), existingVersion.getManifestedAs())));
     }
 
     private <T> T ifNotNull(T preferredVal, T defautVal) {
         return preferredVal != null ? preferredVal : defautVal;
     }
 
     private Episode mergeFetchedEpisode(Episode fetchedEpisode, Item existingItem) {
         Episode existingEpisode;
         if (existingItem instanceof Episode) {
             existingEpisode = (Episode) existingItem;
         } else {
             existingEpisode = new Episode();
             Item.copyTo(existingItem, existingEpisode);
         }
         return mergeEpisodes(fetchedEpisode, existingEpisode);
     }
 
     private Episode mergeEpisodes(Episode fetchedEpisode, Episode existingEpisode) {
         Episode mergedEpisode = mergeItems(fetchedEpisode, existingEpisode);
         
         mergedEpisode.setEpisodeNumber(ifNotNull(fetchedEpisode.getEpisodeNumber(), existingEpisode.getEpisodeNumber()));
         mergedEpisode.setSeriesNumber(ifNotNull(fetchedEpisode.getSeriesNumber(), existingEpisode.getSeriesNumber()));
         mergedEpisode.setPartNumber(ifNotNull(fetchedEpisode.getPartNumber(), existingEpisode.getPartNumber()));
         mergedEpisode.setSeriesRef(fetchedEpisode.getSeriesRef());
         
         return mergedEpisode;
     }
 
     private Film mergeFetchedFilm(Film fetchedFilm, Item existingItem) {
         Film existingFilm;
         if (existingItem instanceof Film) {
             existingFilm = (Film) existingItem;
         } else {
             existingFilm = new Film();
             Item.copyTo(existingItem, existingFilm);
         }
         return mergeFilms(fetchedFilm, existingFilm);
     }
 
     private Film mergeFilms(Film fetchedFilm, Film existingFilm) {
         Film mergedFilms = mergeItems(fetchedFilm, existingFilm);
         
         mergedFilms.setYear(ifNotNull(fetchedFilm.getYear(), existingFilm.getYear()));
         mergedFilms.setWebsiteUrl(ifNotNull(fetchedFilm.getWebsiteUrl(), existingFilm.getWebsiteUrl()));
         
         return mergedFilms;
     }
     
 }
