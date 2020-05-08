 package pl.psnc.dl.wf4ever.portal.pages.search;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.event.Broadcast;
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.head.CssHeaderItem;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.navigation.paging.IPageable;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.CssResourceReference;
 import org.joda.time.format.DateTimeFormat;
 import org.purl.wf4ever.rosrs.client.exception.SearchException;
 import org.purl.wf4ever.rosrs.client.search.SearchServer;
 import org.purl.wf4ever.rosrs.client.search.SearchServer.SortOrder;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.FacetValue;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.FoundRO;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.SearchResult;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.solr.FacetEntry;
 import org.purl.wf4ever.rosrs.client.search.utils.SolrQueryBuilder;
 
 import pl.psnc.dl.wf4ever.portal.PortalApplication;
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.components.pagination.BootstrapPagingNavigator;
 import pl.psnc.dl.wf4ever.portal.events.FacetValueClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.search.SearchResultsAvailableEvent;
 import pl.psnc.dl.wf4ever.portal.events.search.SortOptionChangeEvent;
 import pl.psnc.dl.wf4ever.portal.pages.BasePage;
 import pl.psnc.dl.wf4ever.portal.pages.ro.RoPage;
 
 /**
  * The home page.
  * 
  * @author piotrekhol
  * 
  */
 public class SearchResultsPage extends BasePage {
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(SearchResultsPage.class);
 
     /** Results per page. */
     public static final int RESULTS_PER_PAGE = 15;
 
     /** CSS resource for this page. */
     private static final CssResourceReference CSS_REFERENCE = new CssResourceReference(SearchResultsPage.class,
             "SearchResultsPage.css");
 
     /** Facets to display. */
     private ArrayList<FacetEntry> facetsList;
 
     /** The keywords provided by the user. */
     private String searchKeywords;
 
     /** The facet values selected and submitted by the user. */
     private List<FacetValue> selectedFacetValues = new ArrayList<FacetValue>();
 
     /** Currently selected sort option. */
    private SortOption sortOption;
 
     /** Number of results. */
     private long resultsCount;
 
     /** The component displaying the list of results. */
     IPageable searchResultsList;
 
 
     /**
      * Default constructor, displays all it can find.
      */
     public SearchResultsPage() {
         this("", null, null);
     }
 
 
     /**
      * Constructor.
      * 
      * @param searchKeywords
      *            The keywords provided by the user, unescaped
      * @param selectedFacetValues
      *            The facet values selected and submitted by the user
      * @param sortOption
      *            The field used for sorting
      */
     public SearchResultsPage(String searchKeywords, List<FacetValue> selectedFacetValues, SortOption sortOption) {
         super(new PageParameters());
 
         if (sortOption != null) {
             this.sortOption = sortOption;
         }
         if (selectedFacetValues != null) {
             this.selectedFacetValues = selectedFacetValues;
         }
         boolean emptySearch = searchKeywords == null || searchKeywords.isEmpty();
         this.searchKeywords = searchKeywords;
         String header = emptySearch ? "All research objects" : "Search results for \"" + searchKeywords + "\"";
         add(new Label("header", header));
         add(new Label("resultsCount", new PropertyModel<>(this, "resultsCount")));
         WebMarkupContainer searchResultsDiv = new WebMarkupContainer("searchResultsDiv");
         searchResultsDiv.setOutputMarkupId(true);
         add(searchResultsDiv);
         setDefaultModel(new CompoundPropertyModel<SearchResultsPage>(this));
         SearchServer searchServer = ((PortalApplication) getApplication()).getSearchServer();
 
         final MyFeedbackPanel feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         add(feedbackPanel);
         // don't let users make raw Solr queries
         String query = emptySearch ? buildQuery("*:*", selectedFacetValues) : buildQuery(
             SolrQueryBuilder.escapeString(searchKeywords), selectedFacetValues);
 
         if (searchServer.supportsPagination()) {
             LazySearchResultsView lazySearchResultsList = new LazySearchResultsView("searchResultsListView",
                     searchServer, query, RESULTS_PER_PAGE, new PropertyModel<Map<String, SearchServer.SortOrder>>(this,
                             "sortFields"));
             searchResultsList = lazySearchResultsList;
         } else {
             List<FoundRO> found;
             try {
                 SearchResult searchResult = searchServer.search(query, null, null, getSortFields());
                 send(this, Broadcast.BREADTH, new SearchResultsAvailableEvent(searchResult));
                 found = searchResult.getROsList();
             } catch (SearchException e) {
                 error(e.getMessage());
                 LOGGER.error("Can't do the search for " + searchKeywords, e);
                 found = new ArrayList<>();
             }
             searchResultsList = new SimpleSearchResultsView("searchResultsListView", found, RESULTS_PER_PAGE);
         }
         searchResultsDiv.add((Component) searchResultsList);
         searchResultsDiv.setOutputMarkupId(true);
 
         FacetsView facetsView = new FacetsView("filters", getSelected(), new PropertyModel<List<FacetEntry>>(this,
                 "facets"));
         add(facetsView);
 
         final WebMarkupContainer noResults = new WebMarkupContainer("noResults");
         add(noResults);
         //        noResults.setVisible(searchResults == null || searchResults.isEmpty());
         noResults.setVisible(false);
 
         add(new BootstrapPagingNavigator("pagination", searchResultsList));
         add(new SubmitFiltersButton("submitFilters"));
         add(new ClearFiltersButton("clearFilters"));
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         response.render(CssHeaderItem.forReference(CSS_REFERENCE));
     }
 
 
     /**
      * Create a repeater that will return a list of links for sorting.
      * 
      * @return a repeater
      */
     private DropDownChoice<SortOption> buildSortLinks() {
         List<SortOption> sortOptions = new ArrayList<>();
         for (final FacetEntry facet : facetsList) {
             if (facet.isSorteable()) {
                 sortOptions.add(new SortOption(facet, SortOrder.DESC));
                 sortOptions.add(new SortOption(facet, SortOrder.ASC));
             }
         }
         SortDropDownChoice dropDown = new SortDropDownChoice("sortListView", new PropertyModel<SortOption>(this,
                 "sortOption"), sortOptions);
         return dropDown;
     }
 
 
     public List<FacetValue> getSelected() {
         return selectedFacetValues;
     }
 
 
     public List<FacetEntry> getFacets() {
         return facetsList;
     }
 
 
     public SortOption getSortOption() {
         return sortOption;
     }
 
 
     public void setSortOption(SortOption sortOption) {
         this.sortOption = sortOption;
     }
 
 
     /**
      * Get the sort fields map as required by the search server.
      * 
      * @return a map that contains at most one sort option - the currently selected one
      */
     public Map<String, SearchServer.SortOrder> getSortFields() {
         Map<String, SearchServer.SortOrder> sortFields = new HashMap<>();
         if (sortOption != null) {
             sortFields.put(sortOption.getFacetEntry().getFieldName(), sortOption.getSortOrder());
         }
         return sortFields;
     }
 
 
     public long getResultsCount() {
         return resultsCount;
     }
 
 
     public void setResultsCount(long resultsCount) {
         this.resultsCount = resultsCount;
     }
 
 
     /**
      * Populate one item with an RO that has been found. Useful for the results view that have a different generation
      * mechanism, but want to share the rendering.
      * 
      * @param item
      *            the item to populate
      * @param index
      *            index of this result in the overall list
      */
     public static void populateItem(ListItem<FoundRO> item, long index) {
         final FoundRO result = item.getModelObject();
         item.add(new Label("index", "" + index + "."));
         BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", RoPage.class);
         link.getPageParameters().add("ro", result.getResearchObject().getUri().toString());
         link.add(new Label("researchObject.name", new PropertyModel<String>(result, "researchObject.name")));
         item.add(link);
         item.add(new Label("creators", StringUtils.join(result.getCreators(), ", ")));
         item.add(new Label("created", DateTimeFormat.shortDate().print(result.getCreated())));
         item.add(new Label("numberOfResources", new PropertyModel<String>(result, "numberOfResources")));
         item.add(new Label("numberOfAnnotations", new PropertyModel<String>(result, "numberOfAnnotations")));
         item.add(new Label("status", new PropertyModel<String>(result, "status")));
         WebMarkupContainer score = new WebMarkupContainer("searchScore");
         item.add(score);
         score.setVisible(result.getScore() >= 0);
         score.add(new Label("scoreInPercent", new PropertyModel<String>(result, "scoreInPercent")));
         Label bar = new Label("percentBar", "");
         bar.add(new Behavior() {
 
             /** id. */
             private static final long serialVersionUID = -5409800651205755103L;
 
 
             @Override
             public void onComponentTag(final Component component, final ComponentTag tag) {
                 super.onComponentTag(component, tag);
                 tag.put("style", "width: " + Math.min(100, result.getScoreInPercent()) + "%");
             }
         });
         score.add(bar);
     }
 
 
     @Override
     public void onEvent(IEvent<?> event) {
         super.onEvent(event);
         if (event.getPayload() instanceof FacetValueClickedEvent) {
             onFacetValueClicked((FacetValueClickedEvent) event.getPayload());
         }
         if (event.getPayload() instanceof SearchResultsAvailableEvent) {
             onSearchResultsAvailable((SearchResultsAvailableEvent) event.getPayload());
         }
         if (event.getPayload() instanceof SortOptionChangeEvent) {
             onSortOptionChanged((SortOptionChangeEvent) event.getPayload());
         }
     }
 
 
     /**
      * Select or deselect a facet value after it has been clicked.
      * 
      * @param event
      *            AJAX event
      */
     private void onFacetValueClicked(FacetValueClickedEvent event) {
         for (FacetValue val : selectedFacetValues) {
             if (val.getLabel().equals(event.getFacetValue().getLabel())
                     && val.getParamName().equals(event.getFacetValue().getParamName())) {
                 selectedFacetValues.remove(val);
                 return;
             }
         }
         selectedFacetValues.add(event.getFacetValue());
     }
 
 
     /**
      * Called when the search results are ready to be displayed.
      * 
      * @param event
      *            trigger
      */
     private void onSearchResultsAvailable(SearchResultsAvailableEvent event) {
         if (facetsList == null) {
             facetsList = new ArrayList<>(event.getSearchResult().getFacetsList());
             add(buildSortLinks());
         }
         resultsCount = event.getSearchResult().getNumFound();
     }
 
 
     /**
      * Reload the page when the sort option changes.
      * 
      * @param event
      *            event
      */
     private void onSortOptionChanged(SortOptionChangeEvent event) {
         setResponsePage(new SearchResultsPage(searchKeywords, selectedFacetValues, event.getNewSortOption()));
     }
 
 
     /**
      * Build a query using AND and OR predicates.
      * 
      * @param keywords
      *            the keywords to look for
      * @param facetValues
      *            the facets to filter by
      * @return the query
      */
     public String buildQuery(String keywords, List<FacetValue> facetValues) {
         Map<String, String> queryMap = new HashMap<>();
         if (facetValues != null) {
             for (FacetValue value : facetValues) {
                 if (queryMap.containsKey(value.getParamName())) {
                     String queryPart = queryMap.get(value.getParamName()) + " OR " + value.getQuery();
                     queryMap.put(value.getParamName(), queryPart);
                 } else {
                     queryMap.put(value.getParamName(), value.getQuery());
                 }
             }
         }
         String finalQuery = keywords;
         for (String key : queryMap.keySet()) {
             finalQuery += " AND (" + queryMap.get(key) + ")";
         }
         return finalQuery;
     }
 
 
     /**
      * A button for clearing all filters.
      * 
      * @author piotrekhol
      * 
      */
     private final class ClearFiltersButton extends AjaxLink<Void> {
 
         /** id. */
         private static final long serialVersionUID = -7451742937861956150L;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            markup id
          */
         private ClearFiltersButton(String id) {
             super(id);
         }
 
 
         @Override
         public void onClick(AjaxRequestTarget target) {
             setResponsePage(new SearchResultsPage(searchKeywords, null, sortOption));
         }
     }
 
 
     /**
      * A button for applying the selected filters.
      * 
      * @author piotrekhol
      * 
      */
     private final class SubmitFiltersButton extends AjaxLink<Void> {
 
         /** id. */
         private static final long serialVersionUID = 6125597609903621105L;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            markup id
          */
         private SubmitFiltersButton(String id) {
             super(id);
         }
 
 
         @Override
         public void onClick(AjaxRequestTarget target) {
             setResponsePage(new SearchResultsPage(searchKeywords, selectedFacetValues, sortOption));
         }
     }
 
 }
