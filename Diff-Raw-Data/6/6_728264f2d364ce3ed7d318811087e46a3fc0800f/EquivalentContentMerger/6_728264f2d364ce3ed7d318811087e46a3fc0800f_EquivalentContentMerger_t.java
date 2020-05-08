 package org.atlasapi.persistence.equiv;
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.equiv.EquivalentContentFinder.EquivalentContent;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.base.Strings;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public class EquivalentContentMerger {
 
 	private final EquivalentContentFinder finder;
 
 	public EquivalentContentMerger(EquivalentContentFinder finder) {
 		this.finder = finder;
 	}
 	
 	public Item merge(Item item) {
 		// for now don't merge items directly
 		return item;
 	}
 	
 	public Playlist merge(Playlist playlist) {
 		if (playlist instanceof Brand) {
 			merge((Brand) playlist);
 		} else {
 			List<Playlist> mergedPlaylists = Lists.newArrayList();
 			for (Playlist subPlaylist : playlist.getPlaylists()) {
 				mergedPlaylists.add(merge(subPlaylist));
 			}
 			playlist.setPlaylists(mergedPlaylists);
 		}
 		return playlist;
 	}
 
 	private void merge(Brand brand) {
 		EquivalentContent equiv = finder.equivalentTo(brand);
 		Iterable<? extends Content> equivContent = equiv.equivalent();
 		for (Content content : equivContent) {
 			if (content instanceof Brand) {
 				mergeSubItemsOf(brand, (Brand) content);
 			}
 			brand.addEquivalentTo(content);
 		}
 		for (String alias : equiv.probableAliases()) {
 			brand.addAlias(alias);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void mergeSubItemsOf(Brand original, Brand equivTo) {
 		for (Item item : original.getItems()) {
 			Item sameItem = findSameItem((Episode) item, (List) equivTo.getItems());
 			if (sameItem != null) {
 				// remove previously merged versions
				Set<Version> versions = Sets.newHashSet(Iterables.filter(item.getVersions(), Predicates.not(isProvidedBy(sameItem.getPublisher()))));
				versions.addAll(sameItem.nativeVersions());
 				item.setVersions(versions);
				item.addEquivalentTo(sameItem);
 			}
 		}
 	}
 	
 	private static Predicate<Version> isProvidedBy(final Publisher publisher) {
 		return new Predicate<Version>() {
 	
 			@Override
 			public boolean apply(Version input) {
 				return input.getProvider() != null && input.getProvider().equals(publisher);
 			}
 			
 		};
 	}
 
 	private Item findSameItem(Episode needle, List<Episode> haystack) {
 		for (Episode item : haystack) {
 			if (areSame(needle, item)) {
 				return item;
 			}
 		}
 		return null;
 	}
 
 	private boolean areSame(Episode ep1, Episode ep2) {
 		return sameSeriesAndEpisodeNumber(ep1, ep2) || sameTitle(ep1, ep2);
 	}
 
 	private boolean sameTitle(Item ep1, Item ep2) {
 		return Strings.nullToEmpty(ep1.getTitle()).equals(ep2.getTitle());
 	}
 
 	private boolean sameSeriesAndEpisodeNumber(Episode ep1, Episode ep2) {
 		if (ep1.getEpisodeNumber() == null || ep1.getSeriesNumber() == null) {
 			return false;
 		}
 		return ep1.getSeriesNumber().equals(ep2.getSeriesNumber()) && ep1.getEpisodeNumber().equals(ep2.getEpisodeNumber());
 	}
 }
