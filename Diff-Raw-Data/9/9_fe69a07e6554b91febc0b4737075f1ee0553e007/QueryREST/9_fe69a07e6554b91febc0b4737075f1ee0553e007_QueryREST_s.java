 package fr.webmarket.backend.rest;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.google.common.collect.Lists;
 
 import fr.webmarket.backend.datasource.MemoryDataSource;
 import fr.webmarket.backend.features.search.ISearchCriterion;
 import fr.webmarket.backend.features.search.ItemSearchEngine;
 import fr.webmarket.backend.features.search.SearchAccuracy;
 import fr.webmarket.backend.features.search.SearchStrategy;
 import fr.webmarket.backend.features.search.criteria.ItemBrandCriterion;
 import fr.webmarket.backend.features.search.criteria.ItemDescriptionCriterion;
 import fr.webmarket.backend.features.search.criteria.ItemNameCriterion;
 import fr.webmarket.backend.marshalling.JSONMarshaller;
 import fr.webmarket.backend.model.Item;
 import fr.webmarket.backend.model.ItemTag;
 
 @Path("/query")
 public class QueryREST {
 
 	private static final String ALL_SEARCH_STRATEGY = "all";
 	private static final String ONE_OF_SEARCH_STRATEGY = "oneOf";
 	private static final ItemSearchEngine engine = new ItemSearchEngine();
 
 	@GET
	@Path("by-data/{search-strategy}")
 	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
 	public String queryItemsByData(
			@QueryParam("search-strategy") String searchStrategy,
 			@Context UriInfo uri) throws JsonGenerationException,
 			JsonMappingException, IOException {
 
 		// Get parameters
 		MultivaluedMap<String, String> params = uri.getQueryParameters();
 
 		// Create criteria
 		List<ISearchCriterion> criteria = Lists.newArrayList();
 		if (params.containsKey("name")) {
 			ISearchCriterion nameCriterion = new ItemNameCriterion(params.get(
 					"name").get(0), SearchAccuracy.FLEXIBLE);
 			criteria.add(nameCriterion);
 		}
 		if (params.containsKey("brand")) {
 			ISearchCriterion brandCriterion = new ItemBrandCriterion(params
 					.get("brand").get(0), SearchAccuracy.FLEXIBLE);
 			criteria.add(brandCriterion);
 		}
 		if (params.containsKey("description")) {
 			ISearchCriterion descriptionCriterion = new ItemDescriptionCriterion(
 					params.get("description").get(0), SearchAccuracy.FLEXIBLE);
 			criteria.add(descriptionCriterion);
 		}
 
 		// Launch research
 		SearchStrategy strategy = SearchStrategy.NONE;
 		if (ALL_SEARCH_STRATEGY.equalsIgnoreCase(searchStrategy)) {
 			strategy = SearchStrategy.ALL_CRITERIA;
 		}
 		if (ONE_OF_SEARCH_STRATEGY.equalsIgnoreCase(searchStrategy)) {
 			strategy = SearchStrategy.ONE_OF_CRITERION;
 		}
 		List<Item> result = engine.searchFor(MemoryDataSource.getInstance()
 				.getItemsCatalog(), criteria, strategy);
 
 		return JSONMarshaller.getDefaultMapper().writeValueAsString(result);
 	}
 
 	@GET
 	@Path("by-tags")
 	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
 	public String queryItemsByTags(@Context UriInfo uri)
 			throws JsonGenerationException, JsonMappingException, IOException {
 
 		// Get parameters
 		MultivaluedMap<String, String> params = uri.getQueryParameters();
 
 		// Retrieve tags list
 		if (params.containsKey("tags") == false) {
 			return "";
 		}
 
 		// The item tag catalog
 		Map<Integer, ItemTag> tags = MemoryDataSource.getInstance()
 				.getTagsCatalog().getTags();
 
 		// The item list
 		Collection<Item> items = MemoryDataSource.getInstance()
 				.getItemsCatalog().getItems().values();
 
 		// The result list
 		List<Item> result = Lists.newArrayList();
 
 		// Iterate over tags id list
 		String[] tagsIdList = params.get("tags").get(0).split("\\s");
 		for (String tagId : tagsIdList) {
 
 			// Retrieve the tag object
 			ItemTag tag = tags.get(Integer.parseInt(tagId));
 			if (tag == null) {
 				continue;
 			}
 
 			// Check if the tag is linked to an item
 			for (Item item : items) {
 				if (item.getTags().contains(tag) == false) {
 					continue;
 				}
 				result.add(item);
 			}
 		}
 
 		return new ObjectMapper().writeValueAsString(result);
 	}
 }
