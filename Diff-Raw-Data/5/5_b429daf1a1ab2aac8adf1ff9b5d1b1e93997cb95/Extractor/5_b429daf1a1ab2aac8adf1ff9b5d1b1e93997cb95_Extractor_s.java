 package cz.opendata.linked.metadata.form;
 
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.openrdf.model.URI;
 import org.openrdf.model.vocabulary.DCTERMS;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.SKOS;
 import org.openrdf.query.QueryEvaluationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
 import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
 import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
 import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
 import cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException;
 import cz.cuni.mff.xrg.odcs.rdf.help.MyTupleQueryResultIf;
 import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
 
 @AsTransformer
 public class Extractor 
 extends ConfigurableBase<ExtractorConfig> 
 implements DPU, ConfigDialogProvider<ExtractorConfig> {
 
 	/**
 	 * DPU's configuration.
 	 */
 
 	private Logger logger = LoggerFactory.getLogger(DPU.class);
 	
 	@InputDataUnit(name = "Statistics", optional = true)
 	public RDFDataUnit stats;	
 
	@InputDataUnit(name = "Input data", optional = true)
 	public RDFDataUnit in;	
 
 	@OutputDataUnit(name = "Metadata")
 	public RDFDataUnit out;	
 	
 	public Extractor() {
 		super(ExtractorConfig.class);
 	}
 
 	@Override
 	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
 		return new ExtractorDialog();
 	}
 
 	@Override
 	public void execute(DPUContext ctx) throws DPUException
 	{
 		java.util.Date date = new java.util.Date();
 		long start = date.getTime();
 
 		//Void dataset and DCAT dataset
 		String ns_dcat = "http://www.w3.org/ns/dcat#";
 		String ns_foaf = "http://xmlns.com/foaf/0.1/";
 		String ns_void = "http://rdfs.org/ns/void#";
 		String ns_qb = "http://purl.org/linked-data/cube#";
 
 		URI foaf_agent = out.createURI(ns_foaf + "Agent");
 		URI qb_DataSet = out.createURI(ns_qb + "DataSet");
 		URI dcat_keyword = out.createURI(ns_dcat + "keyword");
 		URI dcat_distribution = out.createURI(ns_dcat + "distribution");
 		URI dcat_downloadURL = out.createURI(ns_dcat + "downloadURL");
 		URI dcat_mediaType = out.createURI(ns_dcat + "mediaType");
 		URI dcat_theme = out.createURI(ns_dcat + "theme");
 		URI xsd_date = out.createURI("http://www.w3.org/2001/XMLSchema#date");
 		URI xsd_integer = out.createURI("http://www.w3.org/2001/XMLSchema#integer");
 		URI dcat_distroClass = out.createURI(ns_dcat + "Distribution");
 		URI dcat_datasetClass = out.createURI(ns_dcat + "Dataset");
 		URI void_datasetClass = out.createURI(ns_void + "Dataset");
 		URI void_triples = out.createURI(ns_void + "triples");
 		URI void_entities = out.createURI(ns_void + "entities");
 		URI void_classes = out.createURI(ns_void + "classes");
 		URI void_properties = out.createURI(ns_void + "properties");
 		URI void_dSubjects = out.createURI(ns_void + "distinctSubjects");
 		URI void_dObjects = out.createURI(ns_void + "distinctObjects");
 
 		URI datasetURI = out.createURI(config.getDatasetURI().toString());
 		URI distroURI = out.createURI(config.getDistroURI().toString());
 		URI exResURI = out.createURI(ns_void + "exampleResource");
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 		
 		out.addTriple(datasetURI, RDF.TYPE, void_datasetClass);
 		out.addTriple(datasetURI, RDF.TYPE, dcat_datasetClass);
		if (config.isIsQb()) out.addTriple(datasetURI, RDF.TYPE, out.createLiteral(config.getDesc_cs(), "cs"));
 		if (config.getDesc_cs() != null)	out.addTriple(datasetURI, DCTERMS.DESCRIPTION, out.createLiteral(config.getDesc_cs(), "cs"));
 		if (config.getDesc_en() != null) out.addTriple(datasetURI, DCTERMS.DESCRIPTION, out.createLiteral(config.getDesc_en(), "en"));
 		if (config.getTitle_cs() != null) out.addTriple(datasetURI, DCTERMS.TITLE, out.createLiteral(config.getTitle_cs(), "cs"));
 		if (config.getTitle_en() != null) out.addTriple(datasetURI, DCTERMS.TITLE, out.createLiteral(config.getTitle_en(), "en"));
 		if (config.getDataDump() != null) out.addTriple(datasetURI, out.createURI(ns_void + "dataDump"), out.createURI(config.getDataDump().toString()));
 		if (config.getSparqlEndpoint() != null) out.addTriple(datasetURI, out.createURI(ns_void + "sparqlEndpoint"), out.createURI(config.getSparqlEndpoint().toString()));
 		
 		for (URL u : config.getAuthors()) { out.addTriple(datasetURI, DCTERMS.CREATOR, out.createURI(u.toString()));	}
 		for (URL u : config.getPublishers())	{ 
 			URI publisherURI = out.createURI(u.toString());
 			out.addTriple(datasetURI, DCTERMS.PUBLISHER, publisherURI); 
 			out.addTriple(publisherURI, RDF.TYPE, foaf_agent);
 			//TODO: more publisher data?
 		}
 		for (URL u : config.getLicenses()) { out.addTriple(datasetURI, DCTERMS.LICENSE, out.createURI(u.toString())); }
 		for (URL u : config.getExampleResources()) { out.addTriple(datasetURI, exResURI, out.createURI(u.toString())); }
 		for (URL u : config.getSources()) { out.addTriple(datasetURI, DCTERMS.SOURCE, out.createURI(u.toString())); }
 		for (String u : config.getKeywords()) { out.addTriple(datasetURI, dcat_keyword, out.createLiteral(u.toString())); }
 		for (URL u : config.getLanguages()) { out.addTriple(datasetURI, DCTERMS.LANGUAGE, out.createURI(u.toString())); }
 		for (URL u : config.getThemes()) { 
 			URI themeURI = out.createURI(u.toString());
 			out.addTriple(datasetURI, dcat_theme, themeURI);
 			out.addTriple(themeURI, RDF.TYPE, SKOS.CONCEPT);
 			out.addTriple(themeURI, SKOS.IN_SCHEME, out.createURI("http://linked.opendata.cz/resource/catalog/Themes"));
 		}
 
 		if (config.isUseNow()) {
 			out.addTriple(datasetURI, DCTERMS.MODIFIED, out.createLiteral(df.format(new Date()), xsd_date));
 		}
 		else out.addTriple(datasetURI, DCTERMS.MODIFIED, out.createLiteral(df.format(config.getModified()), xsd_date));
 
 		out.addTriple(datasetURI, dcat_distribution, distroURI);
 
 		//DCAT Distribution
 		
 		out.addTriple(distroURI, RDF.TYPE, dcat_distroClass);
 		if (config.getDesc_cs() != null)	out.addTriple(distroURI, DCTERMS.DESCRIPTION, out.createLiteral(config.getDesc_cs(), "cs"));
 		if (config.getDesc_en() != null) out.addTriple(distroURI, DCTERMS.DESCRIPTION, out.createLiteral(config.getDesc_en(), "en"));
 		if (config.getTitle_cs() != null) out.addTriple(distroURI, DCTERMS.TITLE, out.createLiteral(config.getTitle_cs(), "cs"));
 		if (config.getTitle_en() != null) out.addTriple(distroURI, DCTERMS.TITLE, out.createLiteral(config.getTitle_en(), "en"));
 		if (config.getDataDump() != null) out.addTriple(distroURI, dcat_downloadURL, out.createURI(config.getDataDump().toString()));
 		if (config.getDataDump() != null) out.addTriple(distroURI, dcat_mediaType, out.createLiteral(config.getMime()));
 		for (URL u : config.getLicenses()) { out.addTriple(distroURI, DCTERMS.LICENSE, out.createURI(u.toString())); }
 		
 		if (config.isUseNow()) {
 			out.addTriple(distroURI, DCTERMS.MODIFIED, out.createLiteral(df.format(new Date()), xsd_date));
 		}
 		else out.addTriple(distroURI, DCTERMS.MODIFIED, out.createLiteral(df.format(config.getModified()), xsd_date));
 		
 		if (stats != null) {
 			ctx.sendMessage(MessageType.INFO, "Found statistics on input - copying");
 			stats.copyAllDataToTargetDataUnit(out);
 		}
 		else if (in != null) {
 			//Now compute statistics on input data
 			ctx.sendMessage(MessageType.INFO, "Starting statistics computation");
 			executeCountQuery("SELECT (COUNT (*) as ?count) WHERE {?s ?p ?o}", void_triples, datasetURI);
 			executeCountQuery("SELECT (COUNT (distinct ?s) as ?count) WHERE {?s a ?t}", void_entities, datasetURI);
 			executeCountQuery("SELECT (COUNT (distinct ?t) as ?count) WHERE {?s a ?t}", void_classes, datasetURI);
 			executeCountQuery("SELECT (COUNT (distinct ?p) as ?count) WHERE {?s ?p ?o}", void_properties, datasetURI);
 			executeCountQuery("SELECT (COUNT (distinct ?s) as ?count) WHERE {?s ?p ?o}", void_dSubjects, datasetURI);
 			executeCountQuery("SELECT (COUNT (distinct ?o) as ?count) WHERE {?s ?p ?o}", void_dObjects, datasetURI);
 			ctx.sendMessage(MessageType.INFO, "Statistics computation done");
 			//Done computing statistics
 		}
 		
 		java.util.Date date2 = new java.util.Date();
 		long end = date2.getTime();
 		ctx.sendMessage(MessageType.INFO, "Done in " + (end-start) + "ms");
 
 	}
 	
 	void executeCountQuery(String countQuery, URI property, URI datasetURI)
 	{
 		URI xsd_integer = out.createURI("http://www.w3.org/2001/XMLSchema#integer");
 		MyTupleQueryResultIf res;
 		int number;
 		
 		try {
 			res = in.executeSelectQueryAsTuples(countQuery);
 			number = Integer.parseInt(res.next().getValue("count").stringValue());
 			out.addTriple(datasetURI, property, out.createLiteral(Integer.toString(number), xsd_integer));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void cleanUp() {	}
 
 }
