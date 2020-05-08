 /**
  * Semantic XWiki Extension
  * Copyright (c) 2010, 2011, 2012, 2014 ObjectSecurity Ltd.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *
  * The research leading to these results has received funding
  * from the European Union Seventh Framework Programme (FP7/2007-2013)
  * under grant agreement No FP7-242474.
  * 
  * The research leading to these results has received funding
  * from the European Union Seventh Framework Programme (FP7/2007-2013)
  * under grant agreement No FP7-608142.
  *
  * Partially funded by the European Space Agengy as part of contract
  * 4000101353 / 10 / NL / SFe
  *
  * Written by Karel Gardas, <kgardas@objectsecurity.com>
  */
 package com.objectsecurity.jena;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.xwiki.bridge.event.DocumentDeletedEvent;
 import org.xwiki.bridge.event.DocumentUpdatingEvent;
 import org.xwiki.component.manager.ComponentLookupException;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.observation.EventListener;
 import org.xwiki.observation.ObservationManager;
 import org.xwiki.observation.event.Event;
 
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.SimpleSelector;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.sdb.SDBFactory;
 import com.hp.hpl.jena.sdb.Store;
 import com.hp.hpl.jena.sdb.store.StoreFactory;
 import com.hp.hpl.jena.tdb.TDBFactory;
 
 import com.objectsecurity.xwiki.util.DocumentUtil;
 import com.objectsecurity.xwiki.util.SymbolMapper;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.web.Utils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Context implements EventListener {
 	
     public enum Mode {
         ADD,
         MODIFY
     }
 
     enum BackendImpl {
         UNKNOWN,
         SDB,
         TDB_DIRECTORY,
         TDB_ASSEMBLER
     }
 	
     private static Context INSTANCE = null;
     private static final Logger logger = LoggerFactory.getLogger(Context.class);
 
     //private OntModel model_;
     private Model model_;
     private static final String storeFileName = "sdb.ttl";
 
     private static BackendImpl jena_backend_default = BackendImpl.SDB;
     private static String jena_backend_db_default = "sdb.ttl";
 
     private static final String SDB = "sdb";
     private static final String TDB = "tdb";
     private static final String ASSEMBLER = "assembler";
     private static final String DIRECTORY = "directory";
 
     private BackendImpl jena_backend = BackendImpl.UNKNOWN;
     private String jena_backend_db = "";
 
     private ComponentManager componentManager;
 
     /**
      * The observation manager that will be use to fire user creation events. Note: We can't have the OM as a
      * requirement, since it would create an infinite initialization loop, causing a stack overflow error (this event
      * listener would require an initialized OM and the OM requires a list of initialized event listeners)
      */
     private ObservationManager observationManager;
 
     // Private constructor prevents instantiation from other classes
     private Context() {
     	// Initialize Rendering components and allow getting instances
     	//EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
     	//componentManager.initialize(this.getClass().getClassLoader());
 
     	componentManager = Utils.getComponentManager();
     	ObservationManager om = this.getObservationManager();
         logger.info("XWiki Context: registering observation listener.");
     	om.addListener(this);
     }
 
     public static Context getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new Context();
         }
         return INSTANCE;
     }
 
     public void initJenaBackendFromEnv() {
         String jback = System.getenv("JENA_BACKEND");
         String backend_name = "";
         String backend_family = "";
         String backend_db_name = "";
         if (jback != null && !jback.equals("")) {
             StringTokenizer st = new StringTokenizer(jback, ":");
             if (st.hasMoreTokens()) {
                 backend_name = st.nextToken();
             }
             if (st.hasMoreTokens() && backend_name.equals(TDB)) {
                 backend_family = st.nextToken();
             }
             if (st.hasMoreTokens()) {
                 backend_db_name = st.nextToken();
             }
             if (backend_name.equals(SDB)) {
                 jena_backend = BackendImpl.SDB;
                 jena_backend_db = backend_db_name;
             }
             else if (backend_name.equals(TDB) && backend_family.equals(DIRECTORY)) {
                 jena_backend = BackendImpl.TDB_DIRECTORY;
                 jena_backend_db = backend_db_name;
             }
             else if (backend_name.equals(TDB) && backend_family.equals(ASSEMBLER)) {
                 jena_backend = BackendImpl.TDB_ASSEMBLER;
                 jena_backend_db = backend_db_name;
             }
             else {
                 logger.info("UNKNOWN backend! => will use default option...");
                 jena_backend = jena_backend_default;
                 jena_backend_db = jena_backend_db_default;
             }
             logger.info("backend: " + jena_backend);
             logger.info("backend param: " + backend_db_name);
         }
         else {
             logger.info("default option!");
             jena_backend = jena_backend_default;
             jena_backend_db = jena_backend_db_default;
         }
     }
 
     synchronized public Model getModel() {
     	if (model_ == null) {
             this.initJenaBackendFromEnv();
             logger.info("BACKEND: " + jena_backend);
             logger.info("DB: " + jena_backend_db);
             if (jena_backend == BackendImpl.SDB) {
     		Store store = StoreFactory.create(storeFileName);
     		model_ = SDBFactory.connectDefaultModel(store);
             }
             else if (jena_backend == BackendImpl.TDB_DIRECTORY) {
                 Dataset ds = TDBFactory.createDataset(jena_backend_db);
                 model_ = ds.getDefaultModel();
             }
             else if (jena_backend == BackendImpl.TDB_ASSEMBLER) {
                 Dataset ds = TDBFactory.assembleDataset(jena_backend_db);
                 model_ = ds.getDefaultModel();
             }
             else {
                 logger.error("ERROR: unknown Jena Backend!");
             }
             //Model m = SDBFactory.connectDefaultModel(store);
             //Model m = SDBFactory.connectNamedModel(store, iri);
             //model_ = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
             logger.info("Context: created ontology model: " + model_);
     	}
     	return model_;
     }
 
     synchronized public void begin() {
         //    	if (this.getModel().supportsTransactions()) {
         this.getModel().begin();
         //    	}
     }
 
     synchronized public void commit() {
         //    	if (this.getModel().supportsTransactions()) {
         this.getModel().commit();
         //    	}
     }
 
     synchronized public void abort() {
     	this.getModel().abort();
     }
 
     synchronized public String query(String str) {
     	// getModel is also initializer of model_ variable!
     	String outstr = "";
     	Model m = this.getModel();
         Query query = QueryFactory.create(str) ;
         QueryExecution qexec = QueryExecutionFactory.create(query, m) ;
     	try {
             ResultSet results = qexec.execSelect() ;
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             ResultSetFormatter.out(out, results, query);
             outstr = out.toString();
             //    		//    		    fmt.printAll(System.out) ;
     	} finally {
             qexec.close() ;
     	}
         return outstr;
     }
 
     synchronized public Vector<Vector<PairNameLink>> query(String str, String[] header, String[] linksAttrs, String[] linksValuesRemapping) {
     	Vector<Vector<PairNameLink>> retval = new Vector<Vector<PairNameLink>>();
     	Model m = this.getModel();
         Query query = QueryFactory.create(str) ;
         QueryExecution qexec = QueryExecutionFactory.create(query, m);
         HashMap<String, String> name_link_map = new HashMap<String, String>();
         HashMap<String, String> link_value_remap = new HashMap<String, String>();
         logger.debug("header len: " + header.length);
         logger.debug("linksAttrs len: " + linksAttrs.length);
         for (int i = 0; i<linksAttrs.length; i++) {
             String tmp = linksAttrs[i];
             String link = tmp.substring(0, tmp.indexOf(">"));
             String name = tmp.substring(tmp.indexOf(">") + 1, tmp.length());
             logger.debug("link: " + link);
             logger.debug("name: " + name);
             name_link_map.put(name, link);
         }
         for (int i = 0; i<linksValuesRemapping.length; i++) {
             String tmp = linksValuesRemapping[i];
             String res = tmp.substring(0, tmp.indexOf(">"));
             String link = tmp.substring(tmp.indexOf(">") + 1, tmp.length());
             logger.debug("resource: " + res);
             logger.debug("link: " + link);
             link_value_remap.put(res, link);
         }
         String[] variable_names;
     	try {
             ResultSet results = qexec.execSelect() ;
             logger.debug("result set hasNext?: " + results.hasNext());
             for ( ; results.hasNext(); ) {
                 QuerySolution sol = results.next();
                 System.out.println(sol);
                 if (header.length != 0) {
                     variable_names = header;
                 }
                 else {
                     System.out.println("header.length == 0 -> generating variable names...");
                     Iterator<String> names = sol.varNames();
                     Vector<String> vec = new Vector<String>();
                     while (names.hasNext()) {
                         vec.add(names.next());
                     }
                     variable_names = vec.toArray(new String[0]);
                 }
                 Vector<PairNameLink> row = new Vector<PairNameLink>();
                 for (int i = 0; i < variable_names.length; i++) {
                     if (sol.contains(variable_names[i])) {
                         RDFNode x = sol.get(variable_names[i]);
                         String field = name_link_map.get(variable_names[i]);
                         PairNameLink pair = new PairNameLink();
                         RDFNode r = null;
                         if (field != null && !field.equals("")) {
                             r = sol.get(field);
                         }
                         if (x.isLiteral()) {
                             pair.name = x.asLiteral().toString();
                         }
                         else if (x.isResource()) {
                             pair.name = x.asResource().toString();
                         }
                         else {
                             pair.name = "<not literal type>";
                         }
                         logger.debug("pair.name: " + pair.name);
                         logger.debug("r: " + r);
                         if (r != null) {
                             if (r.isLiteral()) {
                                 pair.link = r.asLiteral().toString();
                             }
                             else if (r.isResource()) {
                                 pair.link = r.asResource().toString();
                             }
                             else {
                                 pair.link = "<not literal type>";
                             }
                             logger.debug("pair.link: " + pair.link);
                             // need to process link if linksValuesRemapping is used
                             logger.debug("empty remap?: " + link_value_remap.isEmpty());
                             if (!link_value_remap.isEmpty()) {
                                 Set<String> keys = link_value_remap.keySet();
                                 logger.debug("keys: " + keys.toString());
                                 for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
                                     String key = it.next();
                                     logger.debug("key: " + key);
                                     logger.debug("link: " + pair.link);
                                     if (pair.link.contains(key)) {
                                         // link contains the resource URL which needs to be re-mapped
                                         String tmp = pair.link.replace(key, link_value_remap.get(key));
                                         logger.debug("remapping link : " + pair.link + " to " + tmp);
                                         pair.link = tmp;
                                     }
                                 }
                             }
                         }
                         row.add(pair);
                     }
                 }
                 retval.add(row);
             }
     	} finally { qexec.close() ; }
     	return retval;    	
     }
 
     synchronized public Vector<Vector<String>> query(String str, String[] header) {
     	Vector<Vector<String>> retval = new Vector<Vector<String>>();
     	Vector<Vector<PairNameLink>> tmp = query(str, header, new String[0], new String[0]);
         Iterator<Vector<PairNameLink>> i = tmp.iterator();
         while (i.hasNext()) {
             Vector<PairNameLink> x = i.next();
             Iterator<PairNameLink> j = x.iterator();
             Vector<String> row = new Vector<String>();
             while (j.hasNext()) {
                 row.add(j.next().name);
             }
             retval.add(row);
         }
 
     	return retval;
     }
     
     synchronized public String query(String str, String[] header, boolean literalsAsLinks) {
     	// getModel is also initializer of model_ variable!
     	String retval = "";
     	for (int i = 0; i<header.length; i++) {
             if (i == 0)
                 retval = retval + "|";
             retval = retval + header[i];
             //if (i < header.length - 1)
             retval = retval + "|";
     	}
     	retval = retval + "\n";
     	Model m = this.getModel();
         Query query = QueryFactory.create(str) ;
         QueryExecution qexec = QueryExecutionFactory.create(query, m) ;
     	try {
             ResultSet results = qexec.execSelect() ;
             //    		ByteArrayOutputStream out = new ByteArrayOutputStream();
             //    		ResultSetFormatter.out(out, results, query);
             //    		String outstr = out.toString();
             //    		return outstr;
             //    		//    		    fmt.printAll(System.out) ;
             for ( ; results.hasNext(); ) {
                 QuerySolution sol = results.next();
                 System.out.println(sol);
                 for (int i = 0; i < header.length; i++) {
                     if (i == 0)
                         retval = retval + "|";
                     if (sol.contains(header[i])) {
                         RDFNode x = sol.get(header[i]);
                         if (x.isLiteral()) {
                             if (literalsAsLinks)
                                 retval = retval + "[[";
                             String lit = x.asLiteral().toString();
                             retval = retval + lit.replace(".", "\\.");
                             if (literalsAsLinks)
                                 retval = retval + "]]";
                         }
                     }
                     //if (i < header.length - 1)
                     retval = retval + "|";
                 }
                 retval = retval + "\n";
             }
     	} finally { qexec.close() ; }
     	return retval;
     }
 
     public String query(String str, String header, String literalsAsLinks) {
     	if (header == null || header.equals(""))
             return this.query(str);
     	StringTokenizer st = new StringTokenizer(header, ",");
     	Vector<String >vec = new Vector<String>();
     	while(st.hasMoreElements()) {
             vec.add(st.nextToken());
     	}
     	boolean links = false;
     	if ("true".equals(literalsAsLinks)) {
             links = true;
     	}
         return this.query(str, vec.toArray(new String[0]), links);
     }
 
     synchronized public String getPropertyTableForResource(String res, String literalsAsLinks) {
     	String tres = SymbolMapper.transform(res, SymbolMapper.MappingDirection.XWIKI_URL_TO_PHYSICAL_URL, SymbolMapper.MappingStrategy.SYMBOLIC_NAME_TRANSLATION);
     	String retval = "";
         StmtIterator iter = this.getModel().listStatements(new SimpleSelector(this.getModel().createResource(tres), null, (RDFNode)null) {
                 public boolean selects(Statement s) {
                     return true;
                 }
             });
         if (iter.hasNext()) {
             retval = "|property|value|\n";
         }
         boolean links = false;
         if ("true".equals(literalsAsLinks))
             links = true;
         while (iter.hasNext()) {
             Statement stmt = iter.next();
             retval = retval + "|"
                 + stmt.getPredicate().toString()
                 + "|"
                 + (links ? "[[" : "")
                 + (links ? /* stmt.getLiteral().toString().replace(".", "\\.") */ SymbolMapper.transform(stmt.getLiteral().toString(), SymbolMapper.MappingDirection.XWIKI_URL_TO_PHYSICAL_URL, SymbolMapper.MappingStrategy.SYMBOLIC_NAME_TRANSLATION) : stmt.getLiteral().toString()) 
                 + (links ? "]]" : "")
                 + "|\n";
         }
         return retval;
     }
 
     synchronized public Vector<Vector<String>> getPropertyTableForResource(String res) {
         logger.debug("Context: table for resource: " + res);
     	String tres = SymbolMapper.transform(res, SymbolMapper.MappingDirection.XWIKI_URL_TO_PHYSICAL_URL, SymbolMapper.MappingStrategy.SYMBOLIC_NAME_TRANSLATION);
         logger.debug("Context: resource translated to: " + tres);
     	Vector<Vector<String>> retval = new Vector<Vector<String>>();
         StmtIterator iter = this.getModel().listStatements(new SimpleSelector(this.getModel().createResource(tres), null, (RDFNode)null) {
                 public boolean selects(Statement s) {
                     return true;
                 }
             });
         while (iter.hasNext()) {
             Statement stmt = iter.next();
             Vector<String> row = new Vector<String>();
             row.add(stmt.getPredicate().toString());
             //row.add(stmt.getPredicate().getLocalName());
             row.add(stmt.getLiteral().toString());
             logger.debug("adding row: " + row.toString());
             retval.add(row);
         }
         return retval;
     }
 
     synchronized public void setProperty(String resource, String property_prefix, String property_name, String property_value, Mode mode) {
     	// createProperty reuses existing property
     	Property property = this.getModel().createProperty(property_prefix, property_name);
     	this.setProperty(resource, property, property_value, mode);
     }
 
     synchronized public void setProperty(String resource, Property property, String property_value, Mode mode) {
         logger.debug("Context: set property on resource: " + resource);
     	String tres = SymbolMapper.transform(resource, SymbolMapper.MappingDirection.XWIKI_URL_TO_PHYSICAL_URL, SymbolMapper.MappingStrategy.SYMBOLIC_NAME_TRANSLATION);
         logger.debug("Context: resource translated to: " + tres);
     	Resource res = this.getModel().getResource(tres);
     	if (res == null)
             res = this.getModel().createResource();
     	if (mode == Mode.MODIFY) {
             this.removeProperty(resource, property);
     	}
     	res.addProperty(property, property_value);
     }
     
     public void removeProperty(String resource, String property_prefix, String property_name) {
     	Property property = this.getModel().createProperty(property_prefix, property_name);
     	this.removeProperty(resource, property);
     }
 
     public void removeProperty(String resource, Property property) {
     	String tres = SymbolMapper.transform(resource, SymbolMapper.MappingDirection.XWIKI_URL_TO_PHYSICAL_URL, SymbolMapper.MappingStrategy.SYMBOLIC_NAME_TRANSLATION);
     	Resource res = this.getModel().getResource(tres);
     	if (res == null)
             res = this.getModel().createResource(resource);
         logger.debug("removeAll properties: `" + property.toString() + "' on resource: `" + res.toString() + "'");
     	res.removeAll(property);
     }
     
     public String getProperty(String resource, String property_prefix, String property_name) {
     	Property property = this.getModel().createProperty(property_prefix, property_name);
     	return this.getProperty(resource, property);
     }
 
     public String getProperty(String resource, Property property) {
     	String tres = SymbolMapper.transform(resource, SymbolMapper.MappingDirection.XWIKI_URL_TO_PHYSICAL_URL, SymbolMapper.MappingStrategy.SYMBOLIC_NAME_TRANSLATION);
     	Resource res = this.getModel().getResource(tres);
     	if (res == null)
             res = this.getModel().createResource(resource);
     	Statement p = res.getProperty(property);
     	return p != null ? p.getString() : null;
     }
 
     @Override
	public List<Event> getEvents() {
         logger.debug("XWiki Context: getEvents called");
         return Arrays.<Event>asList(new DocumentDeletedEvent(), new DocumentUpdatingEvent());
     }
 
     @Override
	public String getName() {
         return "XWiki Semantics: Context interceptor code";
     }
 
     @Override
	public void onEvent(Event arg0, Object arg1, Object arg2) {
         // TODO Auto-generated method stub
         logger.debug("XWiki: EVENT: " + arg0.toString() + ", arg1: " + arg1 + ", arg2: " + arg2);
         logger.debug("XWiki: classes: " + arg0.getClass().getName() + ", arg1: " + arg1.getClass().getName() + ", arg2: " + arg2.getClass().getName());
         XWikiDocument doc = (XWikiDocument)arg1;
         //String res = doc.getURL("view", (XWikiContext)arg2);
         String name = DocumentUtil.computeFullDocName(doc.getDocumentReference());
         String tres = SymbolMapper.transform(name, SymbolMapper.MappingDirection.XWIKI_URL_TO_PHYSICAL_URL, SymbolMapper.MappingStrategy.SYMBOLIC_NAME_TRANSLATION);
         logger.debug("tres: " + tres);
     	Resource res = this.getModel().getResource(tres);
     	res.removeProperties();
         logger.debug("props after delete: " + this.query("SELECT ?prop WHERE { <" + tres + "> ?prop ?prop_value }", new String[] {"prop"}, false));
     	StmtIterator it = res.listProperties();
     	while (it.hasNext()) {
             Statement st = it.next();
             logger.debug("prop: " + st);
     	}
         //		//Query query = QueryFactory.create("SELECT ?prop WHERE { ?ref <http://www.objectsecurity.com/NextGenRE/XWikiPage_properties_for_deletion> ?prop }") ;
         //		Query query = QueryFactory.create("SELECT ?prop WHERE { <" + res + "> ?prop ?prop_value }");
         //		QueryExecution qexec = QueryExecutionFactory.create(query, this.getModel()) ;
         //		String[] header = new String[] {"prop" };
         //		this.begin();
         //    	try {
         //    		ResultSet results = qexec.execSelect() ;
         //    		for ( ; results.hasNext(); ) {
         //    			QuerySolution sol = results.next();
         //    			System.out.println(sol);
         //    			for (int i = 0; i < header.length; i++) {
         //    				if (sol.contains(header[i])) {
         //    					RDFNode x = sol.get(header[i]);
         //    					if (x.isLiteral()) {
         //    						String lit = x.asLiteral().toString();
         //    						System.err.println("deleting property: " + lit);
         //    						if (lit != null && !lit.equals("")) {
         //    							int pos = lit.lastIndexOf('/');
         //    							String prefix = lit.substring(0, pos + 1);
         //    							String name = lit.substring(pos + 1);
         //    							System.err.println("property prefix `" + prefix + "'");
         //    							System.err.println("property name `" + name + "'");
         //    							this.removeProperty(res, prefix, name);
         //    						}
         //    					}
         //    				}
         //    			}
         //    		}
         //    	} finally { qexec.close() ; }
         //    	this.removeProperty(res, "http://www.objectsecurity.com/NextGenRE/", "XWikiPage_properties_for_deletion");
         //    	this.commit();
         //    	System.err.println("props after delete: " + this.query("SELECT ?prop WHERE { ?ref ?prop_name ?prop }", new String[] {"prop"}, false));
     }
 	
     private ObservationManager getObservationManager() {
         if (this.observationManager == null) {
             try {
                 this.observationManager = componentManager.getInstance(ObservationManager.class);
             } catch (ComponentLookupException e) {
                 throw new RuntimeException("Cound not retrieve an Observation Manager against the component manager");
             }
         }
         return this.observationManager;
     }
 
 }
