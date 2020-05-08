 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.uriplay.persistence.content.mongodb;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jherd.util.Maybe;
 import org.jherd.util.Selection;
 import org.uriplay.content.criteria.ConjunctiveQuery;
 import org.uriplay.content.criteria.ContentQuery;
 import org.uriplay.content.criteria.MatchesNothing;
 import org.uriplay.content.criteria.Queries;
 import org.uriplay.content.criteria.QueryVisitorAdapter;
 import org.uriplay.content.criteria.StringAttributeQuery;
 import org.uriplay.content.criteria.attribute.Attribute;
 import org.uriplay.content.criteria.attribute.Attributes;
 import org.uriplay.content.criteria.attribute.StringValuedAttribute;
 import org.uriplay.media.entity.Brand;
 import org.uriplay.media.entity.Description;
 import org.uriplay.media.entity.Episode;
 import org.uriplay.media.entity.Item;
 import org.uriplay.media.entity.Playlist;
 import org.uriplay.persistence.content.MongoDbBackedContentStore;
 import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;
 
 import com.google.common.collect.Sets;
 import com.google.soy.common.collect.Lists;
 import com.google.soy.common.collect.Maps;
 
 public class MongoDBQueryExecutor implements KnownTypeQueryExecutor {
 
 	private static final Set<Class<? extends Description>> playlistAttributes = (Set) Sets.newHashSet(Playlist.class);
 	private static final Set<Class<? extends Description>> brandAndPlaylistAttributes = (Set) Sets.newHashSet(Brand.class, Playlist.class);
 	
 	private final QuerySplitter splitter = new QuerySplitter();
 	private final QueryResultTrimmer trimmer = new QueryResultTrimmer();
 	private final MongoDbBackedContentStore roughSearch;
 	
 	public MongoDBQueryExecutor(MongoDbBackedContentStore roughSearch) {
 		this.roughSearch = roughSearch;
 	}
 	
 	@Override
 	public List<Item> executeItemQuery(ContentQuery query) {
 
 		if (MatchesNothing.isEquivalentTo(query)) {
 			return Collections.emptyList();
 		}
 		
 		Maybe<ContentQuery> playlistQuery = splitter.retain(query, brandAndPlaylistAttributes);
 
 		// The query doesn't make any playlist or brand constraints: just perform a standard item query
 		if (playlistQuery.isNothing()) {
 			return executeItemQueryInternal(query);		
 		}
 		
 		// Find the playlists
 		List<Playlist> playlists = roughSearch.dehydratedPlaylistsMatching(playlistQuery.requireValue());
 		
 		
 		if (playlists.isEmpty()) {
 			return Collections.emptyList();
 		}
 		
 		Maybe<ContentQuery> everythingElse = splitter.discard(query, brandAndPlaylistAttributes);
 		
 		// Execute the item query but constrain results to the 
 		return executeItemQueryInternal(createContainedInPlaylistQuery(everythingElse, playlists, Attributes.PLAYLIST_URI, query.getSelection()));
 	}
 
 	private List<Item> executeItemQueryInternal(ContentQuery query) {
 		// Extract exact item match query fragments (item by uri or curie)
 		Maybe<ContentQuery> byUriOrCurie = mentions(query, Sets.<Attribute<String>>newHashSet(Attributes.ITEM_URI, Attributes.ITEM_CURIE));
 		// If the request is for an exact match query then query for the item by uri or curie and filter the result
 		if (byUriOrCurie.hasValue()) {
			// Preserve any 'contained in' constraints
			Maybe<ContentQuery> containedIn = mentions(query, Sets.<Attribute<String>>newHashSet(Attributes.PLAYLIST_URI, Attributes.BRAND_URI));
			ContentQuery unfilteredItemQuery = containedIn.hasValue() ? new ConjunctiveQuery(Lists.newArrayList(byUriOrCurie.requireValue(), containedIn.requireValue())) : byUriOrCurie.requireValue(); 
			return filter(query, roughSearch.itemsMatching(unfilteredItemQuery), false);
 		}
 		return filter(query, roughSearch.itemsMatching(query), true);
 	}
 	
 	@Override
 	public List<Brand> executeBrandQuery(ContentQuery query) {
 		
 		if (MatchesNothing.isEquivalentTo(query)) {
 			return Collections.emptyList();
 		}
 		
 		Maybe<ContentQuery> playlistQuery = splitter.retain(query, playlistAttributes);
 		
 		if (playlistQuery.isNothing()) {
 			return executeBrandQueryInternal(query);
 		}
 		
 		List<Playlist> playlists = roughSearch.dehydratedPlaylistsMatching(playlistQuery.requireValue());
 		
 		if (playlists.isEmpty()) {
 			return Collections.emptyList();
 		}
 		
 		Maybe<ContentQuery> everythingElse = splitter.discard(query, playlistAttributes);
 
 		return executeBrandQueryInternal(createContainedInPlaylistQuery(everythingElse, playlists, Attributes.PLAYLIST_URI, query.getSelection()));
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<Brand> executeBrandQueryInternal(ContentQuery query) {
 		
 		Maybe<ContentQuery> brandQuery = splitter.retain(query, (Set) Sets.newHashSet(Brand.class, Playlist.class));
 		Maybe<ContentQuery> itemQuery = splitter.discard(query, (Set) Sets.newHashSet(Brand.class, Playlist.class));
 
 		if (brandQuery.isNothing() && itemQuery.isNothing()) {
 			throw new IllegalArgumentException("Query is too broad");
 		}
 		
 		List<Brand> brands;
 		List<Item> items;
 		
 		if (brandQuery.isNothing()) {
 			items = executeItemQueryInternal(itemQuery.requireValue());
 			if (items.isEmpty()) {
 				return Lists.newArrayList();
 			}
 			brands = Lists.newArrayList();
 			for (Item item : items) {
 				if (item instanceof Episode) {
 					Episode episode = (Episode) item;
 					if (episode.getBrand() != null) {
 						Brand brand = episode.getBrand();
 						brands.add(brand);
 					}
 				}
 			}
 		} else {
 			brands = roughSearch.dehydratedBrandsMatching(brandQuery.requireValue());
 			if (brands.size() > 500) {
 				throw new IllegalArgumentException("Query is too broad");
 			}
 			
 			if (brands.isEmpty()) {
 				return Lists.newArrayList();
 			}
 			items = executeItemQueryInternal(createContainedInPlaylistQuery(itemQuery, brands, Attributes.BRAND_URI, null));
 		}
 		
 		hydratePlaylists(brands, items, null);
 
 		
 		// Filter out subplaylists that don't match if there was a constraint on a sub element
 		if (itemQuery.hasValue() && mentions(query, Sets.<Attribute<String>>newHashSet(Attributes.BRAND_URI, Attributes.BRAND_CURIE)).isNothing()) {
 			return filterEmpty(brands);
 		}
 		
 		return brands;
 	}
 
 
 	@Override
 	public List<Playlist> executePlaylistQuery(ContentQuery query) {
 		if (MatchesNothing.isEquivalentTo(query)) {
 			return Collections.emptyList();
 		}
 		
 		Maybe<ContentQuery> playlistQuery = splitter.retain(query, playlistAttributes);
 		Maybe<ContentQuery> subElementQuery = splitter.discard(query, playlistAttributes);
 		
 		if (playlistQuery.isNothing()) {
 			throw new IllegalArgumentException("Query is too broad");
 		}
 		
 		List<Playlist> playlists = roughSearch.dehydratedPlaylistsMatching(playlistQuery.requireValue());
 
 		if (playlists.isEmpty()) {
 			return Collections.emptyList();
 		}
 		
 		ContentQuery subElementsContainedIn = createContainedInPlaylistQuery(subElementQuery, playlists, Attributes.PLAYLIST_URI, null);
 		
 		
 		List<Item> items = executeItemQueryInternal(subElementsContainedIn);
 		List<Brand> brands = executeBrandQueryInternal(subElementsContainedIn);
 		
 		hydratePlaylists(playlists, items, brands);
 		
 		
 		return playlists;
 	}
 
 	private List<Brand> filterEmpty(List<Brand> brands) {
 		List<Brand> filtered = Lists.newArrayList();
 		for (Brand brand : brands) {
 			if (!brand.getPlaylists().isEmpty() || !brand.getItems().isEmpty()) {
 				filtered.add(brand);
 			}
 		}
 		return filtered;
 	}
 
 	private void hydratePlaylists(List<? extends Playlist> playlists, List<Item> subItems, List<? extends Playlist> subPlaylists) {
 		Map<String, Item> itemLookup = toMapByUri(subItems);
 		
 		Map<String, ? extends Playlist> playlistLookup = null;
 		if (subPlaylists != null) {
 			 playlistLookup = toMapByUri(subPlaylists);
 		}
 		
 		for (Playlist playlist : playlists) {
 			List<String> itemUris = Lists.newArrayList(playlist.getItemUris());
 			playlist.setItems(Lists.<Item>newArrayList());
 			for (String itemUri : itemUris) {
 				Item item = itemLookup.get(itemUri);
 				if (item != null) {
 					playlist.addItem(item);
 				}
 			}
 			
 			if (subPlaylists != null) {
 				List<String> subPlaylistUris = Lists.newArrayList(playlist.getPlaylistUris());
 				playlist.setPlaylists(Lists.<Playlist>newArrayList());
 				for (String subPlaylistUri : subPlaylistUris) {
 					Playlist subPlaylist = playlistLookup.get(subPlaylistUri);
 					if (subPlaylist != null) {
 						playlist.addPlaylist(subPlaylist);
 					}
 				}
 			}
 		}
 	}
 
 	private ContentQuery createContainedInPlaylistQuery(Maybe<ContentQuery> subElementQuery, List<? extends Playlist> playlists, Attribute<String> attribute, Selection selection) {
 		List<ContentQuery> operands = Lists.newArrayListWithCapacity(playlists.size());
 		Set<String> playlistUris = Sets.newHashSet();
 		for (Playlist playlist : playlists) {
 			playlistUris.add(playlist.getCanonicalUri());
 		}
 		operands.add(Queries.equalTo(attribute, playlistUris));
 		
 		if (subElementQuery.hasValue()) {
 			operands.add(subElementQuery.requireValue().withSelection(null));
 		}
 		return new ConjunctiveQuery(operands).withSelection(selection);
 	}
 	
 		
 	/**
 	 * Extracts the part of the query that concern {@link StringValuedAttribute}s passed
 	 * Used to extract URI and CURIE query parts.
 	 */
 	private Maybe<ContentQuery> mentions(ContentQuery query, final Set<Attribute<String>> attributes) {
 		return query.accept(new QueryVisitorAdapter<Maybe<ContentQuery>>() {
 			
 			@Override
 			public  Maybe<ContentQuery> visit(StringAttributeQuery query) {
 				if (attributes.contains(query.getAttribute())) {
 					return Maybe.<ContentQuery>just(query);
 				} 
 				return Maybe.nothing();
 				
 			}
 			
 			@Override
 			public  Maybe<ContentQuery> visit(ConjunctiveQuery conjunctiveQuery) {
 				List<ContentQuery> matchingQueries = Lists.newArrayList();
 				
 				for (ContentQuery operand : conjunctiveQuery.operands()) {
 					Maybe<ContentQuery> subMatch = operand.accept(this);
 					if (subMatch.hasValue()) {
 						matchingQueries.add(subMatch.requireValue());
 					}
 				}
 				if (matchingQueries.isEmpty()) {
 					return Maybe.nothing();
 				}
 				return Maybe.<ContentQuery>just(conjunctiveQuery.copyWithOperands(matchingQueries));
 			}
 			
 			@Override
 			protected Maybe<ContentQuery> defaultValue() {
 				return Maybe.nothing();
 			}
 		});
 	}
 	
 	/** 
 	 * Util method for converting a list of Descriptions into a lookup-by-uri map.
 	 */
 	private <T extends Description> Map<String, T> toMapByUri(List<T> elems) {
 		Map<String, T> itemLookup = Maps.newHashMap();
 		for (T elem : elems) {
 			itemLookup.put(elem.getCanonicalUri(), elem);
 		}
 		return itemLookup;
 	}
 	
 	private List<Item> filter(ContentQuery query, List<Item> items, boolean removeItemsThatDontMatch) {
 		return trimmer.trim(items, query, removeItemsThatDontMatch);
 	}
 }
