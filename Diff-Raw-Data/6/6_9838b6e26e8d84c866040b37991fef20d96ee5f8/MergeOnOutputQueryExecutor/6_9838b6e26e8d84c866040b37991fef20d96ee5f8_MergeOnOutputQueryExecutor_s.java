 package org.atlasapi.equiv.query;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Clip;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 
 public class MergeOnOutputQueryExecutor implements KnownTypeQueryExecutor {
 
 	private static final Ordering<Episode> SERIES_ORDER = Ordering.from(new SeriesOrder());
 	
 	private final KnownTypeQueryExecutor delegate;
 
 	public MergeOnOutputQueryExecutor(KnownTypeQueryExecutor delegate) {
 		this.delegate = delegate;
 	}
 	
 	@Override
 	public Map<String, List<Identified>> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
 		ApplicationConfiguration config = query.getConfiguration();
 		if (!config.precedenceEnabled()) {
 			return delegate.executeUriQuery(uris, query);
 		}
 		return Maps.transformValues(delegate.executeUriQuery(uris, query), new Function<List<Identified>,List<Identified>>(){
             @Override
             public List<Identified> apply(List<Identified> input) {
                 
                 List<Content> content = Lists.newArrayList();
                 List<Identified> ids  = Lists.newArrayList();
                 
                 for (Identified ided : input) {
                     if(ided instanceof Content) {
                         content.add((Content)ided);
                     } else {
                         ids.add(ided);
                     }
                 }
 
                 return ImmutableList.copyOf(Iterables.concat(mergeDuplicates(query.getConfiguration(), content), ids));
             }});
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends Content> List<T> mergeDuplicates(ApplicationConfiguration config, List<T> contents) {
 		Comparator<Content> contentComparator = toContentOrdering(config.publisherPrecedenceOrdering());
 
 		List<T> merged = Lists.newArrayListWithCapacity(contents.size());
 		Set<T> processed = Sets.newHashSet();
 		
 		for (T content : contents) {
 			if (processed.contains(content)) {
 				continue;
 			}
 			List<T> same = findSame(content, contents);
 			processed.addAll(same);
 			
 			Collections.sort(same, contentComparator);
 			
 			T chosen = same.get(0);
 			
 			// defend against broken transitive equivalence
 			if (merged.contains(chosen)) {
 				continue;
 			}
 			
 			List<T> notChosen = same.subList(1, same.size());
 			
 			if (chosen instanceof Container) {
 				mergeIn(config, (Container) chosen, (List<Container>) notChosen);
 			}
 			if (chosen instanceof Item) {
 				mergeIn(config, (Item) chosen, (List<Item>) notChosen);
 			}
 			merged.add(chosen);
 		}
 		return merged;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T extends Content> List<T> findSame(T brand, Iterable<T> contents) {
 		List<T> same = Lists.newArrayList(brand);
 		for (T possiblyEquivalent : contents) {
 			if (!brand.equals(possiblyEquivalent) && possiblyEquivalent.isEquivalentTo(brand)) {
 				same.add(possiblyEquivalent);
 			}
 		}
 		return same;
 	}
 
 	private static Ordering<Content> toContentOrdering(final Ordering<Publisher> byPublisher) {
 		return new Ordering<Content>() {
 			@Override
 			public int compare(Content o1, Content o2) {
				return byPublisher.compare(o1.getPublisher(), o2.getPublisher());
 			}
 		};
 	}
 	
 
 	public <T extends Item> void mergeIn(ApplicationConfiguration config, T chosen, Iterable<T> notChosen) {
 		for (Item notChosenItem : notChosen) {
 			for (Clip clip : notChosenItem.getClips()) {
 				chosen.addClip(clip);
 			}
 		}
 		applyImagePrefs(config, chosen, notChosen);
 		mergeVersions(chosen, notChosen);
 	}
 
 	private <T extends Content> void applyImagePrefs(ApplicationConfiguration config, T chosen, Iterable<T> notChosen) {
 		if (config.imagePrecedenceEnabled()) {
 			Iterable<T> all = Iterables.concat(ImmutableList.of(chosen), notChosen);
 			List<T> topImageMatches = toContentOrdering(config.imagePrecedenceOrdering()).leastOf(Iterables.filter(all, HAS_IMAGE_FIELD_SET), 1);
 			
 			if (!topImageMatches.isEmpty()) {
 				T top = topImageMatches.get(0);
 				chosen.setImage(top.getImage());
 				chosen.setThumbnail(top.getThumbnail());
 			}
 		}
 	}
 	
     private <T extends Item> void mergeVersions(T chosen, Iterable<T> notChosen) {
         for (T notChosenItem : notChosen) {
             for (Version version : notChosenItem.getVersions()) {
                 // TODO When we have more granular precedence this broadcast masking can be removed
                 version.setBroadcasts(Sets.<Broadcast>newHashSet());
                 chosen.addVersion(version);
             }
         }
     }
 	
 	private static final Predicate<Content> HAS_IMAGE_FIELD_SET = new Predicate<Content>() {
 		@Override
 		public boolean apply(Content content) {
 			return content.getImage() != null;
 		}
 	};
 	
 	public <T extends Item> void mergeIn(ApplicationConfiguration config, Container chosen, List<Container> notChosen) {
 		applyImagePrefs(config, chosen, notChosen);
 	}
 	
 	enum ItemIdStrategy {
 		SERIES_EPISODE_NUMBER {
 			@Override
 			public Predicate<Item> match() {
 				return new Predicate<Item>() {
 					@Override
 					public boolean apply(Item item) {
 						if (item instanceof Episode) {
 							Episode episode = (Episode) item;
 							return episode.getSeriesNumber() != null && episode.getEpisodeNumber() != null;
 						}
 						return false;
 					}
 				};
 			}
 
 			@Override
 			@SuppressWarnings("unchecked")
 			public <T extends Item> Iterable<T> merge(List<T> items, List<T> matches) {
 				Map<SeriesAndEpisodeNumber, Episode> chosenItemLookup = Maps.newHashMap();
 				for (T item : Iterables.concat(items, matches)) {
 					Episode episode = (Episode) item;
 					SeriesAndEpisodeNumber se = new SeriesAndEpisodeNumber(episode);
 					if (!chosenItemLookup.containsKey(se)) {
 						chosenItemLookup.put(se, episode);
 					} else {
 						Item chosen = chosenItemLookup.get(se);
 						for (Clip clip : item.getClips()) {
 							chosen.addClip(clip);
 						}
 					}
 				}
 				
 				return (Iterable<T>) SERIES_ORDER.immutableSortedCopy(chosenItemLookup.values());
 			}
 		};
 		
 
 		protected abstract Predicate<Item> match();
 		
 		static ItemIdStrategy findBest(Iterable<? extends Item> items) {
 			if (Iterables.all(items, ItemIdStrategy.SERIES_EPISODE_NUMBER.match())) {
 				return SERIES_EPISODE_NUMBER;
 			}
 			return null;
 		}
 		
 		public abstract <T extends Item> Iterable<T> merge(List<T> items, List<T> matches);
 	}
 }
