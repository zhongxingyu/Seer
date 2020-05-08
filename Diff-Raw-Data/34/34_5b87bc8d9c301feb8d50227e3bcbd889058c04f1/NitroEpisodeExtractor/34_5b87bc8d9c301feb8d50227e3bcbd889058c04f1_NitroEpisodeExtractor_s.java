 package org.atlasapi.remotesite.bbc.nitro.extract;
 
 import java.math.BigInteger;
 
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.remotesite.bbc.BbcFeeds;
 
 import com.metabroadcast.atlas.glycerin.model.AncestorsTitles;
 import com.metabroadcast.atlas.glycerin.model.Episode;
 import com.metabroadcast.atlas.glycerin.model.Image;
 import com.metabroadcast.atlas.glycerin.model.Synopses;
 
 /**
  * <p>
  * A {@link BaseNitroItemExtractor} for extracting {@link Item}s from
  * {@link Episode} sources.
  * </p>
  * 
  * <p>
  * Creates and {@link Item} or {@link org.atlasapi.media.entity.Episode Atlas
  * Episode} and sets the parent and episode number fields as necessary.
  * </p>
  * 
  * @see BaseNitroItemExtractor
  * @see NitroContentExtractor
  */
 public final class NitroEpisodeExtractor extends BaseNitroItemExtractor<Episode, Item> {
 
     @Override
     protected Item createContent(NitroItemSource<Episode> source) {
         if (source.getProgramme().getEpisodeOf() == null) {
             return new Item();
         }
         return new org.atlasapi.media.entity.Episode();
     }
 
     @Override
     protected String extractPid(NitroItemSource<Episode> source) {
         return source.getProgramme().getPid();
     }
 
     @Override
     protected String extractTitle(NitroItemSource<Episode> source) {
         return source.getProgramme().getTitle();
     }
 
     @Override
     protected Synopses extractSynopses(NitroItemSource<Episode> source) {
         return source.getProgramme().getSynopses();
     }
 
     @Override
     protected Image extractImage(NitroItemSource<Episode> source) {
         return source.getProgramme().getImage();
     }
 
     @Override
    protected void extractAdditionalFields(NitroItemSource<Episode> source, Item content) {
         Episode episode = source.getProgramme();
         if (content.getTitle() == null) {
             content.setTitle(episode.getPresentationTitle());
         }
         if (episode.getEpisodeOf() != null) {
             BigInteger position = episode.getEpisodeOf().getPosition();
             org.atlasapi.media.entity.Episode episodeContent = (org.atlasapi.media.entity.Episode) content;
             if (position != null) {
                 episodeContent.setEpisodeNumber(position.intValue());
             }
             if ("series".equals(episode.getEpisodeOf().getResultType())) {
                 ParentRef parent = new ParentRef(
                         BbcFeeds.nitroUriForPid(episode.getEpisodeOf().getPid())
                         );
                 episodeContent.setSeriesRef(parent);
                 episodeContent.setParentRef(parent);
             }
         }
         if (episode.getAncestorsTitles() != null) {
             AncestorsTitles ancestors = episode.getAncestorsTitles();
             if (ancestors.getBrand() != null) {
                 String brandUri = BbcFeeds.nitroUriForPid(ancestors.getBrand().getPid());
                 content.setParentRef(new ParentRef(brandUri));
             }
         }
     }
 }
