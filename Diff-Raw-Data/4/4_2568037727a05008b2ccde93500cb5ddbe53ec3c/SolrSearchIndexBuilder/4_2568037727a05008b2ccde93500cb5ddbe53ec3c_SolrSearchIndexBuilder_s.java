 package uk.ac.ox.oucs.search.solr;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrRequest;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
 import org.apache.solr.client.solrj.request.UpdateRequest;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.common.SolrInputField;
 import org.apache.solr.common.util.ContentStreamBase;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.event.api.Notification;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchIndexBuilder;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.model.SearchBuilderItem;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import uk.ac.ox.oucs.search.solr.producer.BinaryEntityContentProducer;
 import uk.ac.ox.oucs.search.solr.util.UpdateRequestReader;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * @author Colin Hebert
  */
 public class SolrSearchIndexBuilder implements SearchIndexBuilder {
     public static final String COMMON_TOOL_ID = "sakai.search";
     public static final String LITERAL = "literal.";
     private final Collection<EntityContentProducer> entityContentProducers = new HashSet<EntityContentProducer>();
     private SiteService siteService;
     private SolrServer solrServer;
 
     @Override
     public void addResource(Notification notification, Event event) {
         String resourceName = event.getResource();
         //Set the resource name to empty instead of null
         if (resourceName == null)
             resourceName = "";
 
         EntityContentProducer entityContentProducer = newEntityContentProducer(event);
         //If there is no matching entity content producer or no associated site, return
         if (entityContentProducer == null || entityContentProducer.getSiteId(resourceName) == null)
             return;
 
         String siteId = entityContentProducer.getSiteId(resourceName);
 
         //If the indexing is only enabled on sites with search tool, check that the tool is actually enabled
         if (isOnlyIndexSearchToolSites()) {
             try {
                 if (siteService.getSite(siteId).getToolForCommonId(COMMON_TOOL_ID) == null)
                     return;
             } catch (Exception ex) {
                 return;
             }
         }
 
         try {
             ItemAction action = ItemAction.getAction(entityContentProducer.getAction(event));
             SolrRequest request;
             switch (action) {
                 case ADD:
                     request = toSolrRequest(resourceName, entityContentProducer);
                     break;
                 case DELETE:
                     request = new UpdateRequest().deleteById(entityContentProducer.getId(resourceName));
                     break;
                 default:
                     throw new UnsupportedOperationException(action + " is not yet supported");
             }
             solrServer.request(request);
             solrServer.commit();
         } catch (SolrServerException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Override
     public void registerEntityContentProducer(EntityContentProducer ecp) {
         entityContentProducers.add(ecp);
     }
 
     @Override
     public EntityContentProducer newEntityContentProducer(String ref) {
         for (EntityContentProducer ecp : entityContentProducers) {
             if (ecp.matches(ref)) {
                 return ecp;
             }
         }
         return null;
     }
 
     @Override
     public EntityContentProducer newEntityContentProducer(Event event) {
         for (EntityContentProducer ecp : entityContentProducers) {
             if (ecp.matches(event)) {
                 return ecp;
             }
         }
         return null;
     }
 
     @Override
     public List<EntityContentProducer> getContentProducers() {
         return new ArrayList<EntityContentProducer>(entityContentProducers);
     }
 
     @Override
     public void refreshIndex(String currentSiteId) {
         try {
             //TODO: Obtain actual resources names!
             Collection<String> resourceNames = getResourceNames(currentSiteId);
             removeSiteIndexContent(currentSiteId);
             for (String resourceName : resourceNames) {
                 EntityContentProducer entityContentProducer = newEntityContentProducer(resourceName);
 
                 //If there is no matching entity content producer or no associated site, skip the resource
                 //it is either not available anymore, or the corresponding entityContentProducer doesn't exist anymore
                 if (entityContentProducer == null || entityContentProducer.getSiteId(resourceName) == null)
                     continue;
 
                 try {
                     solrServer.request(toSolrRequest(resourceName, entityContentProducer));
                 } catch (Exception e) {
                     //Ignore document
                 }
             }
 
             solrServer.commit();
         } catch (Exception e) {
         }
 
     }
 
     private Collection<String> getResourceNames(String currentSiteId) {
         try {
             SolrQuery query = new SolrQuery()
                     .setQuery(SearchService.FIELD_SITEID + ':' + currentSiteId)
                    .addField(SearchService.FIELD_SITEID);
             SolrDocumentList results = solrServer.query(query).getResults();
             Collection<String> resourceNames = new ArrayList<String>(results.size());
             for (SolrDocument document : results) {
                 resourceNames.add((String) document.getFieldValue(SearchService.FIELD_REFERENCE));
             }
             return resourceNames;
         } catch (SolrServerException e) {
             return Collections.emptyList();
         }
     }
 
     @Override
     public void rebuildIndex(final String currentSiteId) {
         removeSiteIndexContent(currentSiteId);
 
         for (final EntityContentProducer entityContentProducer : getContentProducers()) {
             try {
                 Iterable<String> resourceNames = new Iterable<String>() {
                     @Override
                     public Iterator<String> iterator() {
                         return entityContentProducer.getSiteContentIterator(currentSiteId);
                     }
                 };
 
                 for (String resourceName : resourceNames) {
                     try {
                         solrServer.request(toSolrRequest(resourceName, entityContentProducer));
                     } catch (Exception e) {
                         //Ignore document
                     }
                 }
 
                 solrServer.commit();
             } catch (Exception e) {
                 //Oops?
             }
         }
     }
 
     private void removeSiteIndexContent(String currentSiteId) {
         try {
             solrServer.request(new UpdateRequest().deleteByQuery(SearchService.FIELD_SITEID + ':' + currentSiteId));
         } catch (Exception e) {
         }
     }
 
     @Override
     public void refreshIndex() {
         for (Site s : siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)) {
             if (isSiteIndexable(s)) {
                 refreshIndex(s.getId());
             }
         }
     }
 
     @Override
     public boolean isBuildQueueEmpty() {
         return getPendingDocuments() == 0;
     }
 
     @Override
     public void rebuildIndex() {
         for (Site s : siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)) {
             if (isSiteIndexable(s)) {
                 rebuildIndex(s.getId());
             }
         }
     }
 
     private boolean isSiteIndexable(Site s) {
         // Do not index:
         //  - Special sites
         //  - Sites without the search tool (if the option is enabled)
         //  - User sites (if the option is enabled)
         return !(siteService.isSpecialSite(s.getId()) ||
                 (isOnlyIndexSearchToolSites() && s.getToolForCommonId("sakai.search") == null) ||
                 (isExcludeUserSites() && siteService.isUserSite(s.getId())));
     }
 
     @Override
     public void destroy() {
         //TODO: Nope, we don't kill the search that easily
     }
 
     @Override
     public int getPendingDocuments() {
         //TODO: If documents are handled by SolR we don't have any pending document
         return 0;
     }
 
     @Override
     public boolean isExcludeUserSites() {
         //TODO: In the first time, user sites aren't a part of rebuilds
         return true;
     }
 
     /**
      * Enable indexing only on sites with the search tool enabled
      */
     @Override
     public boolean isOnlyIndexSearchToolSites() {
         return true;
     }
 
     @Override
     public List<SearchBuilderItem> getGlobalMasterSearchItems() {
         //TODO: Don't return any item now as the indexing is handled by solr
         return Collections.emptyList();
     }
 
     @Override
     public List<SearchBuilderItem> getAllSearchItems() {
         //TODO: Don't return any item now as the indexing is handled by solr
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public List<SearchBuilderItem> getSiteMasterSearchItems() {
         //TODO: Don't return any item now as the indexing is handled by solr
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public static enum ItemAction {
         /**
          * Action Unknown, usually because the record has just been created
          */
         UNKNOWN(SearchBuilderItem.ACTION_UNKNOWN),
 
         /**
          * Action ADD the record to the search engine, if the doc ID is set, then
          * remove first, if not set, check its not there.
          */
         ADD(SearchBuilderItem.ACTION_ADD),
 
         /**
          * Action DELETE the record from the search engine, once complete delete the
          * record
          */
         DELETE(SearchBuilderItem.ACTION_DELETE),
 
         /**
          * The action REBUILD causes the indexer thread to rebuild the index from
          * scratch, re-fetching all entities This should only ever appear on the
          * master record
          */
         REBUILD(SearchBuilderItem.ACTION_REBUILD),
 
         /**
          * The action REFRESH causes the indexer thread to refresh the search index
          * from the current set of entities. If a Rebuild is in progress, the
          * refresh will not override the rebuild
          */
         REFRESH(SearchBuilderItem.ACTION_REFRESH);
 
         private final int itemAction;
 
         private ItemAction(int itemAction) {
             this.itemAction = itemAction;
         }
 
         public static ItemAction getAction(int itemActionId) {
             for (ItemAction itemAction : values()) {
                 if (itemAction.getItemAction() == itemActionId)
                     return itemAction;
             }
 
             return null;
         }
 
         public int getItemAction() {
             return itemAction;
         }
     }
 
     private SolrRequest toSolrRequest(final String resourceName, EntityContentProducer contentProducer) {
         SolrRequest request;
         SolrInputDocument document = new SolrInputDocument();
 
         //TODO: Solr handles dates, use that instead of a string timestamp...
         document.addField(SearchService.DATE_STAMP, String.valueOf(System.currentTimeMillis()));
         document.addField(SearchService.FIELD_CONTAINER, contentProducer.getContainer(resourceName));
         document.addField(SearchService.FIELD_ID, contentProducer.getId(resourceName));
         document.addField(SearchService.FIELD_TYPE, contentProducer.getType(resourceName));
         document.addField(SearchService.FIELD_SUBTYPE, contentProducer.getSubType(resourceName));
         document.addField(SearchService.FIELD_REFERENCE, resourceName);
         document.addField(SearchService.FIELD_CONTEXT, contentProducer.getSiteId(resourceName));
         document.addField(SearchService.FIELD_TITLE, contentProducer.getTitle(resourceName));
         document.addField(SearchService.FIELD_TOOL, contentProducer.getTool());
         document.addField(SearchService.FIELD_URL, contentProducer.getUrl(resourceName));
         document.addField(SearchService.FIELD_SITEID, contentProducer.getSiteId(resourceName));
 
         // add the custom properties
         Map<String, ?> m = contentProducer.getCustomProperties(resourceName);
         if (m != null) {
             for (Map.Entry<String, ?> entry : m.entrySet()) {
                 String key = entry.getKey();
                 Object value = entry.getValue();
                 Collection<String> values;
 
                 if (value instanceof String)
                     values = Collections.singleton((String) value);
                 else if (value instanceof String[])
                     values = Arrays.asList((String[]) value);
                 else if (value instanceof Collection)
                     values = (Collection<String>) value;
                 else
                     values = Collections.emptyList();
 
                 document.addField("property_" + key, values);
             }
         }
 
         if (contentProducer instanceof BinaryEntityContentProducer) {
             final BinaryEntityContentProducer binaryContentProducer = (BinaryEntityContentProducer) contentProducer;
             //Send to tika
             ContentStreamUpdateRequest contentStreamUpdateRequest = new ContentStreamUpdateRequest("/update/extract");
             contentStreamUpdateRequest.setParam("fmap.content", SearchService.FIELD_CONTENTS);
             contentStreamUpdateRequest.addContentStream(new ContentStreamBase() {
                 @Override
                 public InputStream getStream() throws IOException {
                     return binaryContentProducer.getContentStream(resourceName);
                 }
             });
             for (SolrInputField field : document)
                 for (Object o : field) {
                     //The "sakai_" part is due to SOLR-3386, this fix should be temporary
                     contentStreamUpdateRequest.setParam(LITERAL + "sakai_" + field.getName(), o.toString());
                     contentStreamUpdateRequest.setParam("fmap.sakai_" + field.getName(), field.getName());
                 }
             request = contentStreamUpdateRequest;
         } else if (contentProducer.isContentFromReader(resourceName)) {
             document.setField(SearchService.FIELD_CONTENTS, contentProducer.getContentReader(resourceName));
             request = new UpdateRequestReader().add(document);
         } else {
             document.setField(SearchService.FIELD_CONTENTS, contentProducer.getContent(resourceName));
             request = new UpdateRequest().add(document);
         }
 
         return request;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     public void setSolrServer(SolrServer solrServer) {
         this.solrServer = solrServer;
     }
 }
