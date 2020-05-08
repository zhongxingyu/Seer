 package org.atlasapi.media.entity;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.content.rdf.annotations.RdfProperty;
 import org.atlasapi.media.vocabulary.OWL;
 import org.atlasapi.media.vocabulary.PLAY_USE_IN_RDF_FOR_BACKWARD_COMPATIBILITY;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.common.primitives.Ints;
 
 /**
  * Base type for descriptions of resources.
  *
  * @author Robert Chatley
  * @author Lee Denison
  */
 public class Identified {
 
 	private Long id;
 	
 	private String canonicalUri;
 
 	private String curie;
 
 	private Set<String> aliases = Sets.newHashSet();
 	
 	private Set<LookupRef> equivalentTo = Sets.newHashSet();
 	
 	/**
 	 * Records the time that the 3rd party reported that the
 	 * {@link Identified} was last updated
 	 */
 	private DateTime lastUpdated;
 	
 	public Identified(String uri, String curie) {
 		this.canonicalUri = uri;
 		this.curie = curie;
 	}
 	
 	public Identified() { 
 		/* allow anonymous entities */ 
 		this.canonicalUri = null;
 		this.curie = null;
 	}
 	
 	public Identified(String uri) { 
 		this(uri, null);
 	}
 	
 	
 	@RdfProperty(relation = true, namespace=OWL.NS, uri="sameAs")
 	public Set<String> getAliases() {
 		return aliases;
 	}
 	
 	public void setCanonicalUri(String canonicalUri) {
 		this.canonicalUri = canonicalUri;
 	}
 	
 	public void setCurie(String curie) {
 		this.curie = curie;
 	}
 	
 	public void setAliases(Iterable<String> uris) {
 		this.aliases = ImmutableSortedSet.copyOf(uris);
 	}
 	
 	public void addAlias(String uri) {
 		addAliases(ImmutableList.of(uri));
 	}
 	
 	public void addAliases(Iterable<String> uris) {
 		setAliases(Iterables.concat(this.aliases, ImmutableList.copyOf(uris)));
 	}
 	
 	public String getCanonicalUri() {
 		return canonicalUri;
 	}
 	
 	@RdfProperty(relation = false, namespace=PLAY_USE_IN_RDF_FOR_BACKWARD_COMPATIBILITY.NS, uri="curie")
 	public String getCurie() {
 		return curie;
 	}
 
 	public Set<String> getAllUris() {
 		Set<String> allUris = Sets.newHashSet(getAliases());
 		allUris.add(getCanonicalUri());
 		return Collections.unmodifiableSet(allUris);
 	}
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + "(uri:"  + canonicalUri + ")";
 	}
 	
 	@Override
 	public int hashCode() {
 		if (canonicalUri == null) {
 			return super.hashCode();
 		}
 		return canonicalUri.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (canonicalUri != null && obj instanceof Identified) {
 			return canonicalUri.equals(((Identified) obj).canonicalUri);
 		}
 		return false;
 	}
 	
 	public void setLastUpdated(DateTime lastUpdated) {
 		this.lastUpdated = lastUpdated;
 	}
 	
 	public DateTime getLastUpdated() {
 		return lastUpdated;
 	}
 	
 	public void addEquivalentTo(Described content) {
 		checkNotNull(content.getCanonicalUri());
 		this.equivalentTo.add(LookupRef.from(content));
 	}
 	
 	public Set<LookupRef> getEquivalentTo() {
 		return equivalentTo;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	public static final Function<Identified, String> TO_URI = new Function<Identified, String>() {
 
 		@Override
 		public String apply(Identified description) {
 			return description.getCanonicalUri();
 		}
 	};
 	
 	public static final Function<Identified, Long> TO_ID = new Function<Identified, Long>() {
         @Override
         public Long apply(Identified input) {
             return input.getId();
         }
     };
 
 	public void setEquivalentTo(Set<LookupRef> uris) {
 		this.equivalentTo = uris;
 	}
 	
 	public static final Comparator<Identified> DESCENDING_LAST_UPDATED = new Comparator<Identified>() {
         @Override
         public int compare(final Identified s1, final Identified s2) {
             if (s1.getLastUpdated() == null && s2.getLastUpdated() == null) {
                 return 0;
             }
             if (s2.getLastUpdated() == null) {
                 return -1;
             }
             if (s1.getLastUpdated() == null) {
                 return 1;
             }
             
             return s2.getLastUpdated().compareTo(s1.getLastUpdated());
         }
     };
 	
 	 /**
      * This method attempts to preserve symmetry of
      * equivalence (since content is persisted independently
      * there is often a window of inconsistency)
      */
	public boolean isEquivalentTo(Described content) {
		return equivalentTo.contains(LookupRef.from(content))
	        || Iterables.contains(Iterables.transform(content.getEquivalentTo(), LookupRef.TO_ID), canonicalUri);
 	}
 	
 	public static void copyTo(Identified from, Identified to) {
 	    to.aliases = Sets.newHashSet(from.aliases);
 	    to.canonicalUri = from.canonicalUri;
 	    to.curie = from.curie;
 	    to.equivalentTo = Sets.newHashSet(from.equivalentTo);
 	    to.lastUpdated = from.lastUpdated;
 	}
 	
 	public static <T extends Identified> List<T> sort(List<T> content, final Iterable<String> orderIterable) {
         
         final ImmutableList<String> order = ImmutableList.copyOf(orderIterable);
         
         Comparator<Identified> byPositionInList = new Comparator<Identified>() {
 
             @Override
             public int compare(Identified c1, Identified c2) {
                 return Ints.compare(indexOf(c1), indexOf(c2));
             }
 
             private int indexOf(Identified content) {
                 for (String uri : content.getAllUris()) {
                     int idx = order.indexOf(uri);
                     if (idx != -1) {
                         return idx;
                     }
                 }
                 if (content.getCurie() != null) {
                     return order.indexOf(content.getCurie());
                 }
                 return -1;
             }
         };
         
         List<T> toSort = Lists.newArrayList(content);
         Collections.sort(toSort, byPositionInList);
         return toSort;
     }
 }
