 package org.atlasapi.media.entity;
 
 import java.util.Comparator;
 import java.util.Set;
 
 import org.atlasapi.content.rdf.annotations.RdfProperty;
 import org.atlasapi.media.vocabulary.DCTERMS;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 
 public class Container<T extends Item> extends Content {
 
 	protected ImmutableList<T> contents = ImmutableList.of();
 	protected final Ordering<Identified> lastUpdatedOrdering = Ordering.from(DESCENDING_LAST_UPDATED);
 
 	public Container(String uri, String curie, Publisher publisher) {
 		super(uri, curie, publisher);
 	}
     
     public Container() {}
 
     @RdfProperty(relation = true, namespace = DCTERMS.NS, uri = "hasPart")
     public ImmutableList<T> getContents() {
 		return contents;
 	}
     
     public final void setContentsByResolvingChildRefs(Iterable<? extends T> contents) {
 		Set<T> deduped = ImmutableSet.copyOf(lastUpdatedOrdering.immutableSortedCopy(contents));
         this.contents = ImmutableList.copyOf(seriesAndEpisodeOrdering.immutableSortedCopy(deduped));
 		for (T content : this.contents) {
			contentAdded(content);
 		}
     }

    protected void contentAdded(T content) {
    	content.setContainer(this);
	}
     
     public Container<T> toSummary() {
         Container<T> summary = new Container<T>(this.getCanonicalUri(), this.getCurie(), this.getPublisher());
         summary.setTitle(this.getTitle());
         summary.setDescription(this.getDescription());
         return summary;
     }
     
     private final Comparator<T> seriesAndEpisodeFallBack = new Comparator<T>() {
         @Override
         public int compare(T item1, T item2) {
             if (item1 instanceof Episode && item2 instanceof Episode) {
                 return compareEpisodes((Episode) item1, (Episode) item2);
             }
             
             int comparison = DESCENDING_LAST_UPDATED.compare(item1, item2);
             if (comparison == 0) {
                 comparison = defaultOrder(item1, item2);
             }
             return comparison;
         }
         
         private int compareEpisodes(Episode item1, Episode item2) {
             if (item1.getSeriesNumber() == null && item2.getSeriesNumber() == null) {
                  return defaultOrder(item1, item2);
             }
             if (item1.getSeriesNumber() == null) {
                 return 1;
             }
             if (item2.getSeriesNumber() == null) {
                 return -1;
             }
             int seriesComparison = item1.getSeriesNumber().compareTo(item2.getSeriesNumber());
             if (seriesComparison != 0) {
                 return seriesComparison;
             }
             return compareEpisodeNumbers(item1, item2);
             
         }
 
         private int compareEpisodeNumbers(Episode item1, Episode item2) {
             if (item1.getEpisodeNumber() == null && item2.getEpisodeNumber() == null) {
                  return defaultOrder(item1, item2);
             }
             if (item1.getEpisodeNumber() == null) {
                 return 1;
             }
             if (item2.getEpisodeNumber() == null) {
                 return -1;
             }
             int episodeComparison = item1.getEpisodeNumber().compareTo(item2.getEpisodeNumber());
             if (episodeComparison != 0) {
                 return episodeComparison;
             }
             return defaultOrder(item1, item2);
         }
 
         private int defaultOrder(Item item1, Item item2) {
             return item1.getCanonicalUri().compareTo(item2.getCanonicalUri());
         }
     };
     
     protected final Ordering<T> seriesAndEpisodeOrdering = Ordering.from(seriesAndEpisodeFallBack);
     
     public Container<T> copy() {
         Container<T> copy = new Container<T>();
         copyTo(this, copy);
         return copy;
     }
     
     @SuppressWarnings("unchecked")
     public final static <T extends Item> void copyTo(Container<T> from, Container<T> to) {
         Content.copyTo(from, to);
         to.contents = (ImmutableList<T>) ImmutableList.copyOf(Iterables.transform(from.contents, Item.COPY));
     }
 }
