 /*
  * Copyright 2008-2010 Microarray Informatics Team, EMBL-European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * For further details of the Gene Expression Atlas project, including source code,
  * downloads and documentation, please see:
  *
  * http://gxa.github.com/gxa
  */
 
 package ae3.service.structuredquery;
 
 import ae3.util.HtmlHelper;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.FacetField;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.params.FacetParams;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.DisposableBean;
 import uk.ac.ebi.gxa.dao.AtlasDAO;
 import uk.ac.ebi.gxa.index.builder.IndexBuilder;
 import uk.ac.ebi.gxa.index.builder.IndexBuilderEventHandler;
 import uk.ac.ebi.gxa.index.builder.listener.IndexBuilderEvent;
 import uk.ac.ebi.gxa.properties.AtlasProperties;
 import uk.ac.ebi.gxa.properties.AtlasPropertiesListener;
 import uk.ac.ebi.gxa.utils.EscapeUtil;
 import uk.ac.ebi.gxa.utils.SequenceIterator;
 
 import java.util.*;
 
 /**
  * Gene properties values listing and autocompletion helper implementation
  * @author pashky
  * @see AutoCompleter
  */
 public class AtlasGenePropertyService implements AutoCompleter,
         IndexBuilderEventHandler,
         AtlasPropertiesListener,
         DisposableBean {
     private SolrServer solrServerAtlas;
     private AtlasProperties atlasProperties;
     private IndexBuilder indexBuilder;
 
     private final Set<String> allProperties = new HashSet<String>();
     private Set<String> idProperties;
     private Set<String> descProperties;
     private Set<String> drillDownProperties;
     private Set<String> nameProperties;
     private List<String> nameFields;
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private final Map<String,PrefixNode> prefixTrees = new HashMap<String,PrefixNode>();
 
     public SolrServer getSolrServerAtlas() {
         return solrServerAtlas;
     }
 
     public void setSolrServerAtlas(SolrServer solrServiceAtlas) {
         this.solrServerAtlas = solrServiceAtlas;
     }
 
     public void setAtlasProperties(AtlasProperties atlasProperties) {
         this.atlasProperties = atlasProperties;
         atlasProperties.registerListener(this);
     }
 
     public void setIndexBuilder(IndexBuilder indexBuilder) {
         this.indexBuilder = indexBuilder;
         indexBuilder.registerIndexBuildEventHandler(this);
     }
 
     public void onAtlasPropertiesUpdate(AtlasProperties atlasProperties) {
         loadProperties();
         prefixTrees.clear();
     }
 
     private void loadProperties()
     {
         Set<String> available = getAllProperties();
 
         this.idProperties = new HashSet<String>(atlasProperties.getGeneAutocompleteIdFields());
         this.idProperties.retainAll(available);
         this.descProperties = new HashSet<String>(atlasProperties.getGeneAutocompleteDescFields());
         this.descProperties.retainAll(available);
         this.drillDownProperties = new HashSet<String>(atlasProperties.getQueryDrilldownGeneFields());
         this.drillDownProperties.retainAll(available);
         this.nameProperties = new HashSet<String>(atlasProperties.getGeneAutocompleteNameFields());
         this.nameProperties.retainAll(available);
 
         this.nameFields = new ArrayList<String>();
         nameFields.add("identifier");
         nameFields.add("name_f");
         for(String nameProp : nameProperties)
             nameFields.add("property_f_" + nameProp);
     }
 
     private Collection<GeneAutoCompleteItem> treeAutocomplete(final String property, final String prefix, final int limit) {
         PrefixNode root = treeGetOrLoad("property_f_" + property);
 
         final List<GeneAutoCompleteItem> result = new ArrayList<GeneAutoCompleteItem>();
         if(root != null) {
             root.walk(prefix, 0, "", new PrefixNode.WalkResult() {
                 public void put(String name, int count) {
                     result.add(new GeneAutoCompleteItem(property, name, (long)count, null, null, null));
                 }
                 public boolean enough() {
                     return limit >=0 && result.size() >= limit;
                 }
             });
         }
         return result;
     }
 
     private PrefixNode treeGetOrLoad(String field) {
         PrefixNode root;
         synchronized(prefixTrees) {
             if(!prefixTrees.containsKey(field)) {
                 log.info("Loading gene property values and counts for " + field);
                 SolrQuery q = new SolrQuery("*:*");
                 q.setRows(0);
                 q.setFacet(true);
                 q.setFacetMinCount(1);
                 q.setFacetLimit(-1);
                 q.setFacetSort(FacetParams.FACET_SORT_COUNT);
                 q.addFacetField(field);
                 
                 try {
                     QueryResponse qr = solrServerAtlas.query(q);
                     root = new PrefixNode();
                     if(qr.getFacetFields() != null && qr.getFacetFields().get(0) != null
                             && qr.getFacetFields().get(0).getValues() != null) {
                         for(FacetField.Count ffc : qr.getFacetFields().get(0).getValues())
                             if(ffc.getName().length() > 0) {
                                 root.add(ffc.getName(), (int)ffc.getCount());
                             }
                     }
                     prefixTrees.put(field, root);
                 } catch (SolrServerException e) {
                     throw new RuntimeException(e);
                 }
                 log.info("Done loading gene property values and counts for " + field);
             }
             root = prefixTrees.get(field);
         }
         return root;
     }
 
     public Collection<AutoCompleteItem> autoCompleteValues(String property, String query, int limit, Map<String,String> filters) {
         if(idProperties == null)
             loadProperties();
 
         boolean hasPrefix = query != null && !"".equals(query);
         if(hasPrefix)
             query = query.toLowerCase();
 
         int speciesFilter = -1;
         try {
             speciesFilter = Integer.valueOf(filters.get("species"));
         } catch(Exception e) {
             // okay
         }
 
         boolean anyProp = property == null || property.equals("");
 
         List<AutoCompleteItem> result = new ArrayList<AutoCompleteItem>();
         if(anyProp) {
 
             for(String p : idProperties)
                 result.addAll(treeAutocomplete(p, query, atlasProperties.getGeneAutocompleteIdLimit()));
             Collections.sort(result);
             if(result.size() > atlasProperties.getGeneAutocompleteIdLimit())
                 result = result.subList(0, atlasProperties.getGeneAutocompleteIdLimit());
 
             result.addAll(0, joinGeneNames(query, speciesFilter, atlasProperties.getGeneAutocompleteNameLimit()));
 
             for(String p : descProperties)
                 result.addAll(treeAutocomplete(p, query, limit > 0 ? limit - result.size() : -1));
 
             result = result.subList(0, Math.min(result.size(), limit));
         } else {
             if(Constants.GENE_PROPERTY_NAME.equals(property)) {
                 result.addAll(joinGeneNames(query, -1, limit));
             } else if(idProperties.contains(property) || descProperties.contains(property)) {
                 result.addAll(treeAutocomplete(property, query, limit));
             }
             Collections.sort(result);
         }
 
         return result;
     }
 
     private List<AutoCompleteItem> joinGeneNames(final String query, final int speciesFilter, final int limit) {
         final List<AutoCompleteItem> res = new ArrayList<AutoCompleteItem>();
         final Set<String> ids = new HashSet<String>();
 
         final StringBuffer sb = new StringBuffer();
 
         for(final String field : nameFields) {
             treeGetOrLoad(field).walk(query, 0, "", new PrefixNode.WalkResult() {
                 public void put(String name, int count) {
                     if(sb.length() > 0)
                         sb.append(" ");
                     sb.append(field).append(":").append(EscapeUtil.escapeSolr(name));
                     if(sb.length() > 800) {
                         if(speciesFilter >= 0)
                             sb.insert(0, "(").append(") AND species_id:").append(speciesFilter);
                         findAutoCompleteGenes(sb.toString(), query, res, ids);
                         sb.setLength(0);
                     }
                 }
                 public boolean enough() {
                     return limit >= 0 && (res.size() >= limit);
                 }
             });
         }
 
         if(sb.length() > 0) {
             if(speciesFilter >= 0)
                 sb.insert(0, "(").append(") AND species_id:").append(speciesFilter);
             findAutoCompleteGenes(sb.toString(), query, res, ids);
         }
 
         Collections.sort(res);
         return res.subList(0, Math.min(limit >= 0 ? limit : res.size(), res.size()));
     }
 
     private void findAutoCompleteGenes(final String query, final String prefix, List<AutoCompleteItem> res, Set<String> ids) {
         SolrQuery q = new SolrQuery(query);
         q.setStart(0);
         q.setRows(50);
        for(String field : nameProperties)
            q.addField("property_" + field);
         q.addField("species");
         q.addField("identifier");
         q.addField("name");
         try {
             QueryResponse qr = solrServerAtlas.query(q);
             for(SolrDocument doc : qr.getResults())
             {
                 String name = null;
 
                 String species = (String)doc.getFieldValue("species");
                 if(species == null)
                     species = "";
                 else
                     species = HtmlHelper.upcaseFirst(species.replace("$",""));
 
                 String geneId = (String)doc.getFieldValue("identifier");
 
                 List<String> names = new ArrayList<String>();
                 for(String s : doc.getFieldNames())
                     if(!s.equals("species")) {
                         @SuppressWarnings("unchecked")
                         Collection<String> c = (Collection)doc.getFieldValues(s);
                         if(c != null)
                             for(String v : c) {
                                 if(name == null && v.toLowerCase().startsWith(prefix)) {
                                     name = v;
                                 } else if(name != null && v.toLowerCase().startsWith(prefix) && v.toLowerCase().length() < name.length()) {
                                     names.add(name);
                                     name = v;
                                 } else if(!v.equals(name) && !names.contains(v))
                                     names.add(v);
                             }
                     }
 
                 if(name != null && !ids.contains(geneId)) {
                     ids.add(geneId);
                     res.add(new GeneAutoCompleteItem(Constants.GENE_PROPERTY_NAME, name, 1L, species, geneId, names));
                 }
             }
         } catch(SolrServerException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void onIndexBuildFinish(IndexBuilder builder, IndexBuilderEvent event) {
         prefixTrees.clear();
         allProperties.clear();
         loadProperties();
     }
 
     public void onIndexBuildStart(IndexBuilder builder) {
         
     }
 
     public void destroy() throws Exception {
         if(indexBuilder != null)
             indexBuilder.unregisterIndexBuildEventHandler(this);
         if(atlasProperties != null)
             atlasProperties.unregisterListener(this);
     }
 
     public Collection<String> getDrilldownProperties() {
         if(idProperties == null)
             loadProperties();
         return drillDownProperties;
     }
 
     public Collection<String> getIdProperties() {
         if(idProperties == null)
             loadProperties();
         return idProperties;
     }
 
     public Collection<String> getDescProperties() {
         if(idProperties == null)
             loadProperties();
         return descProperties;
     }
 
     public Collection<String> getNameProperties() {
         if(idProperties == null)
             loadProperties();
         return nameProperties;
     }
 
     public Iterable<String> getIdNameDescProperties() {
         if(idProperties == null)
             loadProperties();
         return new Iterable<String>() {
             public Iterator<String> iterator() {
                 return new SequenceIterator<String>(
                         nameProperties.iterator(),
                         descProperties.iterator(),
                         idProperties.iterator()
                 );
             }
         };
     }
 
     public Set<String> getAllProperties() {
         if(allProperties.isEmpty()) {
             SolrQuery q = new SolrQuery("*:*");
             q.setRows(0);
             q.addFacetField("properties");
             q.setFacet(true);
             q.setFacetLimit(-1);
             q.setFacetMinCount(1);
             try {
                 QueryResponse qr = solrServerAtlas.query(q);
                 if(qr.getFacetFields().get(0).getValues() != null)
                     for(FacetField.Count ffc : qr.getFacetFields().get(0).getValues()) {
                         allProperties.add(ffc.getName());
                     }
             } catch(SolrServerException e) {
                 throw new RuntimeException("Can't fetch all factors", e);
             }
         }
         return allProperties;
     }
 }
