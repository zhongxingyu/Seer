 
 package com.psddev.dari.db;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.response.FacetField;
 
 import com.psddev.dari.util.HtmlObject;
 import com.psddev.dari.util.HtmlWriter;
 import com.psddev.dari.util.PaginatedResult;
 import com.psddev.dari.util.Settings;
 import com.psddev.dari.util.StringUtils;
 
 /**
  * Paginated result for Solr that provides access to
  * faceted results.
  *
  * @author jcollins
  */
 public class SolrPaginatedResult<E> extends PaginatedResult<E> implements HtmlObject {
 
     final Class<?> _klass;
     final List<FacetField> _facetedFields;
 
     public SolrPaginatedResult(
             long offset, int limit, long count, List<E> items, List<FacetField> facetedFields,
             Class<?> klass) {
         super(offset, limit, count, items);
 
         _klass = klass;
         _facetedFields = facetedFields;
     }
 
     public SolrPaginatedResult(
             long offset, int limit, long count, List<E> items, List<FacetField> facetedFields,
             Class<?> klass, SolrQuery solrQuery) {
         super(offset, limit, count, items);
 
         _klass = klass;
         _facetedFields = facetedFields;
         this.solrQuery = solrQuery;
     }
 
     public List<DariFacetField> getFacetedFields() {
         List<DariFacetField> fields = new ArrayList<DariFacetField>();
         if (_facetedFields != null) {
             for(FacetField field : _facetedFields) {
                 fields.add(new DariFacetField(_klass, field));
             }
         }
 
         return fields;
     }
 
     private transient SolrQuery solrQuery;
 
     public SolrQuery getSolrQuery() {
         return solrQuery;
     }
 
     public void setSolrQuery(SolrQuery solrQuery) {
         this.solrQuery = solrQuery;
     }
 
     @Override
     public void format(HtmlWriter writer) throws IOException {
         writer.writeStart("p");
             writer.writeStart("code").writeHtml(this.getClass().getName()).writeEnd();
             writer.writeHtml(' ');
             writer.writeStart("strong").writeHtml(this.getFirstItemIndex()).writeEnd();
             writer.writeHtml(" to ");
             writer.writeStart("strong").writeHtml(this.getLastItemIndex()).writeEnd();
             writer.writeHtml(" of ");
             writer.writeStart("strong").writeHtml(this.getCount()).writeEnd();
         writer.writeEnd();
 
         if (Settings.isDebug() && this.getSolrQuery() != null) {
             String solrFullQuery = this.getSolrQuery().toString();
             String solrQuery = this.getSolrQuery().getQuery();
             String solrSort = StringUtils.join(this.getSolrQuery().getSortFields(), ",");
 
             // Use a form instead of a link if the URL will be too long.
             if (solrFullQuery.length() > 2000) {
                 writer.writeStart("span", "class", "solr-query");
                     writer.writeHtml("Solr Query: ");
                     writer.writeHtml(StringUtils.decodeUri(solrFullQuery));
 
                     writer.writeStart("form",
                             "class", "solrQueryDebugForm",
                             "method", "post",
                             "action", "/_debug/db-solr",
                             "target", "query");
                         writer.writeTag("input", "type", "hidden", "name", "query", "value", StringUtils.decodeUri(solrQuery));
                         writer.writeTag("input", "type", "hidden", "name", "sort", "value", StringUtils.decodeUri(solrSort));
                         writer.writeTag("input", "class", "btn", "type", "submit", "value", "Execute");
                     writer.writeEnd();
                 writer.writeEnd();
 
             } else {
                 writer.writeHtml("Solr Query: ");
                 writer.writeHtml(StringUtils.decodeUri(solrFullQuery));
                 writer.writeHtml(" (");
                     writer.writeStart("a",
                             "href", StringUtils.addQueryParameters("/_debug/db-solr", "query", solrQuery, "sort", solrSort),
                             "target", "query");
                         writer.writeHtml("Execute");
                     writer.writeEnd();
                 writer.writeHtml(")");
             }
         }
 
         writer.writeStart("ol");
             for (Object item : this.getItems()) {
                 writer.writeStart("li").writeObject(item).writeHtml(" Solr Score: " + SolrDatabase.Static.getScore(item)).writeEnd();
             }
         writer.writeEnd();
     }
 
     static public class DariFacetField {
 
         private final Class<?> _klass;
         private final FacetField _field;
 
         public DariFacetField(Class<?> klass, FacetField field) {
             this._klass = klass;
             this._field = field;
         }
 
         public String getName() {
             return _field.getName();
         }
 
         public Long getCount() {
            return Long.valueOf(_field.getValueCount());
         }
 
         public <T> List<T> getObjects() {
             Map<String, FacetField.Count> index = new HashMap<String, FacetField.Count>();
 
             List<String> ids = new ArrayList<String>();
             for(FacetField.Count c : _field.getValues()) {
                 index.put(c.getName(), c);
                 ids.add(c.getName());
             }
 
             @SuppressWarnings("unchecked")
             List<T> objects = (List<T>) (_klass == null || _klass == Query.class ?
                     Query.fromAll().where("id = ?", ids).selectAll() :
                     Query.from(_klass).where("id = ?", ids).selectAll());
 
             if (objects != null) {
                 for (Object o : objects) {
                     Record record = (Record)o;
                     FacetField.Count c = index.get(record.getId().toString());
                    record.getState().getExtras().put("count", Long.valueOf(c.getCount()));
                 }
             }
 
             return objects;
         }
     }
 }
