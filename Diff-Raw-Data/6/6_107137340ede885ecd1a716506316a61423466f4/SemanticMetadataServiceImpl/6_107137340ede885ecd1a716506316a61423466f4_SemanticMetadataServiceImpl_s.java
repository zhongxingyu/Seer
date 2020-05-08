 /**
  * 
  */
 package pl.psnc.dl.wf4ever.sms;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
 import pl.psnc.dl.wf4ever.dlibra.UserProfile;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.shared.PrefixMapping;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.ng4j.sparql.NamedGraphDataset;
 
 /**
  * @author piotrhol
  * 
  */
 public class SemanticMetadataServiceImpl
 	implements SemanticMetadataService
 {
 
 	private static final Logger log = Logger.getLogger(SemanticMetadataServiceImpl.class);
 
 	private static final Syntax sparqlSyntax = Syntax.syntaxARQ;
 
 	private static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";
 
 	private static final String RO_NAMESPACE = "http://purl.org/wf4ever/ro#";
 
 	private static final String FOAF_NAMESPACE = "http://xmlns.com/foaf/0.1/";
 
 	private static final String AO_NAMESPACE = "http://purl.org/ao/";
 
 	private static final URI DEFAULT_NAMED_GRAPH_URI = URI.create("sms");
 
 	private static final PrefixMapping standardNamespaces = PrefixMapping.Factory.create()
 			.setNsPrefix("ore", ORE_NAMESPACE).setNsPrefix("ro", RO_NAMESPACE).setNsPrefix("dcterms", DCTerms.NS)
 			.setNsPrefix("foaf", FOAF_NAMESPACE).lock();
 
 	private static final String SERVICE_NAME = "RODL";
 
 	private final NamedGraphSet graphset;
 
 	private static final OntModel defaultModel = ModelFactory.createOntologyModel(
 		new OntModelSpec(OntModelSpec.OWL_MEM), ModelFactory.createDefaultModel().read(RO_NAMESPACE));
 
 	private static final OntClass researchObjectClass = defaultModel.getOntClass(RO_NAMESPACE + "ResearchObject");
 
 	private static final OntClass manifestClass = defaultModel.getOntClass(RO_NAMESPACE + "Manifest");
 
 	private static final OntClass resourceClass = defaultModel.getOntClass(RO_NAMESPACE + "Resource");
 
 	private static final OntClass roFolderClass = defaultModel.getOntClass(RO_NAMESPACE + "Folder");
 
 	private static final OntClass foafAgentClass = defaultModel.getOntClass(FOAF_NAMESPACE + "Agent");
 
 	private static final OntClass foafPersonClass = defaultModel.getOntClass(FOAF_NAMESPACE + "Person");
 
 	private static final Property describes = defaultModel.getProperty(ORE_NAMESPACE + "describes");
 
 	private static final Property isDescribedBy = defaultModel.getProperty(ORE_NAMESPACE + "isDescribedBy");
 
 	private static final Property aggregates = defaultModel.getProperty(ORE_NAMESPACE + "aggregates");
 
 	private static final Property foafName = defaultModel.getProperty(FOAF_NAMESPACE + "name");
 
 	private static final Property name = defaultModel.getProperty(RO_NAMESPACE + "name");
 
 	private static final Property filesize = defaultModel.getProperty(RO_NAMESPACE + "filesize");
 
 	private static final Property checksum = defaultModel.getProperty(RO_NAMESPACE + "checksum");
 
 	private static final Property body = defaultModel.getProperty(AO_NAMESPACE + "body");
 
 	private final String getResourceQueryTmpl = "DESCRIBE <%s> WHERE { }";
 
 	private final String findResearchObjectsQueryTmpl = "PREFIX ro: <" + RO_NAMESPACE + "> SELECT ?ro "
 			+ "WHERE { ?ro a ro:ResearchObject. FILTER regex(str(?ro), \"^%s\") . }";
 
 	private final Connection connection;
 
 	private final UserProfile user;
 
 
 	public SemanticMetadataServiceImpl(UserProfile user)
 		throws IOException, NamingException, SQLException, ClassNotFoundException
 	{
 		this.user = user;
 		connection = getConnection("connection.properties");
 		if (connection == null) {
 			throw new RuntimeException("Connection could not be created");
 		}
 
 		graphset = new NamedGraphSetDB(connection, "sms");
 
 		defaultModel.setNsPrefixes(standardNamespaces);
 	}
 
 
 	private Connection getConnection(String filename)
 		throws IOException, NamingException, SQLException, ClassNotFoundException
 	{
 		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
 		Properties props = new Properties();
 		props.load(is);
 
 		if (props.containsKey("datasource")) {
 			String datasource = props.getProperty("datasource");
 			if (datasource != null) {
 				InitialContext ctx = new InitialContext();
 				DataSource ds = (DataSource) ctx.lookup(datasource);
 				return ds.getConnection();
 			}
 		}
 		else {
 			String driver_class = props.getProperty("driver_class");
 			String url = props.getProperty("url");
 			String username = props.getProperty("username");
 			String password = props.getProperty("password");
 			if (driver_class != null && url != null && username != null && password != null) {
 				Class.forName(driver_class);
 				return DriverManager.getConnection(url, username, password);
 			}
 		}
 
 		return null;
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#createResearchObject(java
 	 * .net.URI)
 	 */
 	@Override
 	public void createResearchObject(URI researchObjectURI)
 	{
 		URI manifestURI = getManifestURI(researchObjectURI.normalize());
 		OntModel manifestModel = createOntModelForNamedGraph(manifestURI);
 		Individual manifest = manifestModel.getIndividual(manifestURI.toString());
 		if (manifest != null) {
 			throw new IllegalArgumentException("URI already exists: " + manifestURI);
 		}
 		manifest = manifestModel.createIndividual(manifestURI.toString(), manifestClass);
 		Individual ro = manifestModel.createIndividual(researchObjectURI.toString(), researchObjectClass);
 
 		manifestModel.add(ro, isDescribedBy, manifest);
 		manifestModel.add(ro, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 		Individual agent = manifestModel.createIndividual(foafAgentClass);
 		manifestModel.add(agent, foafName, user.getName());
 		manifestModel.add(ro, DCTerms.creator, agent);
 
 		manifestModel.add(manifest, describes, ro);
 		manifestModel.add(manifest, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 		agent = manifestModel.createIndividual(foafAgentClass);
 		manifestModel.add(agent, foafName, SERVICE_NAME);
 		manifestModel.add(manifest, DCTerms.creator, agent);
 	}
 
 
 	@Override
 	public void updateManifest(URI manifestURI, InputStream is, RDFFormat rdfFormat)
 	{
 		// TODO validate the manifest?
 		addNamedGraph(manifestURI, is, rdfFormat);
 
 		//
 		// // leave only one dcterms:created - the earliest
 		// Individual manifest = manifestModel.getIndividual(manifestURI.toString());
 		// NodeIterator it = manifestModel.listObjectsOfProperty(manifest,
 		// DCTerms.created);
 		// Calendar earliest = null;
 		// while (it.hasNext()) {
 		// Calendar created = ((XSDDateTime)
 		// it.next().asLiteral().getValue()).asCalendar();
 		// if (earliest == null || created.before(earliest))
 		// earliest = created;
 		// }
 		// if (earliest != null) {
 		// manifestModel.removeAll(manifest, DCTerms.created, null);
 		// manifestModel.add(manifest, DCTerms.created,
 		// manifestModel.createTypedLiteral(earliest));
 		// }
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#getManifest(java.net.URI,
 	 * pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation)
 	 */
 	@Override
 	public InputStream getManifest(URI manifestURI, RDFFormat rdfFormat)
 	{
 		return getNamedGraph(manifestURI, rdfFormat);
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#addResource(java.net.URI,
 	 * java.net.URI, pl.psnc.dl.wf4ever.dlibra.ResourceInfo)
 	 */
 	@Override
 	public void addResource(URI roURI, URI resourceURI, ResourceInfo resourceInfo)
 	{
 		resourceURI = resourceURI.normalize();
 		OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(roURI.normalize()));
 		Individual ro = manifestModel.getIndividual(roURI.toString());
 		if (ro == null) {
 			throw new IllegalArgumentException("URI not found: " + roURI);
 		}
 		Individual resource = manifestModel.createIndividual(resourceURI.toString(), resourceClass);
 		manifestModel.add(ro, aggregates, resource);
 		if (resourceInfo != null) {
 			if (resourceInfo.getName() != null) {
 				manifestModel.add(resource, name, manifestModel.createTypedLiteral(resourceInfo.getName()));
 			}
 			manifestModel.add(resource, filesize, manifestModel.createTypedLiteral(resourceInfo.getSizeInBytes()));
 			if (resourceInfo.getChecksum() != null && resourceInfo.getDigestMethod() != null) {
 				manifestModel.add(resource, checksum, manifestModel.createResource(String.format("urn:%s:%s",
 					resourceInfo.getDigestMethod(), resourceInfo.getChecksum())));
 			}
 		}
 		manifestModel.add(resource, DCTerms.created, manifestModel.createTypedLiteral(Calendar.getInstance()));
 		Individual agent = manifestModel.createIndividual(foafAgentClass);
 		manifestModel.add(agent, foafName, user.getName());
 		manifestModel.add(resource, DCTerms.creator, agent);
 
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#removeResource(java.net .URI,
 	 * java.net.URI)
 	 */
 	@Override
 	public void removeResource(URI roURI, URI resourceURI)
 	{
 		resourceURI = resourceURI.normalize();
 		OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(roURI.normalize()));
 		Individual ro = manifestModel.getIndividual(roURI.toString());
 		if (ro == null) {
 			throw new IllegalArgumentException("URI not found: " + roURI);
 		}
 		Individual resource = manifestModel.getIndividual(resourceURI.toString());
 		if (resource == null) {
 			throw new IllegalArgumentException("URI not found: " + resourceURI);
 		}
 		manifestModel.remove(ro, aggregates, resource);
 
 		StmtIterator it = manifestModel.listStatements(null, aggregates, resource);
 		if (!it.hasNext()) {
 			manifestModel.removeAll(resource, null, null);
 		}
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#getResource(java.net.URI,
 	 * pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation)
 	 */
 	@Override
 	public InputStream getResource(URI roURI, URI resourceURI, RDFFormat rdfFormat)
 	{
 		resourceURI = resourceURI.normalize();
 		OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(roURI.normalize()));
 		Individual resource = manifestModel.getIndividual(resourceURI.toString());
 		if (resource == null) {
 			return null;
 		}
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 		String queryString = String.format(getResourceQueryTmpl, resourceURI.toString());
 		Query query = QueryFactory.create(queryString);
 		QueryExecution qexec = QueryExecutionFactory.create(query, manifestModel);
 		Model resultModel = qexec.execDescribe();
 		qexec.close();
 
 		resultModel.removeNsPrefix("xml");
 
 		resultModel.write(out, rdfFormat.getName().toUpperCase(), roURI.toString());
 		return new ByteArrayInputStream(out.toByteArray());
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pl.psnc.dl.wf4ever.sms.SemanticMetadataService#getAnnotationBody(java
 	 * .net.URI, pl.psnc.dl.wf4ever.sms.SemanticMetadataService.Notation)
 	 */
 	@Override
 	public InputStream getNamedGraph(URI namedGraphURI, RDFFormat rdfFormat)
 	{
 		namedGraphURI = namedGraphURI.normalize();
 		if (!graphset.containsGraph(namedGraphURI.toString())) {
 			return null;
 		}
 		NamedGraphSet tmpGraphSet = new NamedGraphSetImpl();
 		if (rdfFormat.supportsContexts()) {
 			addGraphsRecursively(tmpGraphSet, namedGraphURI);
 		}
 		else {
 			tmpGraphSet.addGraph(graphset.getGraph(namedGraphURI.toString()));
 		}
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		tmpGraphSet.write(out, rdfFormat.getName().toUpperCase(), null);
 		return new ByteArrayInputStream(out.toByteArray());
 	}
 
 
 	@Override
 	public InputStream getNamedGraphWithRelativeURIs(URI namedGraphURI, URI researchObjectURI, RDFFormat rdfFormat)
 	{
 		if (rdfFormat != RDFFormat.RDFXML) {
 			throw new RuntimeException("Format " + rdfFormat + " is not supported");
 		}
 		namedGraphURI = namedGraphURI.normalize();
 		if (!graphset.containsGraph(namedGraphURI.toString())) {
 			return null;
 		}
 		NamedGraphSet tmpGraphSet = new NamedGraphSetImpl();
 		if (rdfFormat.supportsContexts()) {
 			addGraphsRecursively(tmpGraphSet, namedGraphURI);
 		}
 		else {
 			tmpGraphSet.addGraph(graphset.getGraph(namedGraphURI.toString()));
 		}
 		URI manifestURI = getManifestURI(researchObjectURI.normalize());
 
		List<URI> namedGraphsURIs = Arrays.asList(manifestURI);
 		OntModel annotationModel = createOntModelForNamedGraph(manifestURI);
 		NodeIterator it = annotationModel.listObjectsOfProperty(body);
 		while (it.hasNext()) {
 			RDFNode annotationBodyRef = it.next();
 			namedGraphsURIs.add(URI.create(annotationBodyRef.asResource().getURI()));
 		}
 
 		RO_RDFXMLWriter writer = new RO_RDFXMLWriter();
 		writer.setResearchObjectURI(researchObjectURI);
 		writer.setBaseURI(namedGraphURI);
 		writer.setNamedGraphs(namedGraphsURIs);
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		writer.write(tmpGraphSet.asJenaModel(namedGraphURI.toString()), out, null);
 		return new ByteArrayInputStream(out.toByteArray());
 	}
 
 
 	private void addGraphsRecursively(NamedGraphSet tmpGraphSet, URI namedGraphURI)
 	{
 		tmpGraphSet.addGraph(graphset.getGraph(namedGraphURI.toString()));
 
 		OntModel annotationModel = createOntModelForNamedGraph(namedGraphURI);
 		NodeIterator it = annotationModel.listObjectsOfProperty(body);
 		while (it.hasNext()) {
 			RDFNode annotationBodyRef = it.next();
 			URI childURI = URI.create(annotationBodyRef.asResource().getURI());
 			if (graphset.containsGraph(childURI.toString()) && !tmpGraphSet.containsGraph(childURI.toString())) {
 				addGraphsRecursively(tmpGraphSet, childURI);
 			}
 		}
 	}
 
 
 	@Override
 	public Set<URI> findResearchObjects(URI partialURI)
 	{
 		String queryString = String.format(findResearchObjectsQueryTmpl, partialURI != null ? partialURI.normalize()
 				.toString() : "");
 		Query query = QueryFactory.create(queryString);
 
 		// Execute the query and obtain results
 		QueryExecution qe = QueryExecutionFactory.create(query, createOntModelForAllNamedGraphs());
 		ResultSet results = qe.execSelect();
 		Set<URI> uris = new HashSet<URI>();
 		while (results.hasNext()) {
 			QuerySolution solution = results.nextSolution();
 			Resource manifest = solution.getResource("ro");
 			uris.add(URI.create(manifest.getURI()));
 		}
 
 		// Important - free up resources used running the query
 		qe.close();
 		return uris;
 	}
 
 
 	/**
 	 * @param namedGraphURI
 	 * @return
 	 */
 	private OntModel createOntModelForNamedGraph(URI namedGraphURI)
 	{
 		NamedGraph namedGraph = getOrCreateGraph(graphset, namedGraphURI);
 		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
 			ModelFactory.createModelForGraph(namedGraph));
 		ontModel.addSubModel(defaultModel);
 		return ontModel;
 	}
 
 
 	private NamedGraph getOrCreateGraph(NamedGraphSet graphset, URI namedGraphURI)
 	{
 		return graphset.containsGraph(namedGraphURI.toString()) ? graphset.getGraph(namedGraphURI.toString())
 				: graphset.createGraph(namedGraphURI.toString());
 	}
 
 
 	@Override
 	public void close()
 	{
 		graphset.close();
 	}
 
 
 	@Override
 	public boolean isRoFolder(URI researchObjectURI, URI resourceURI)
 	{
 		resourceURI = resourceURI.normalize();
 		OntModel manifestModel = createOntModelForNamedGraph(getManifestURI(researchObjectURI.normalize()));
 		Individual resource = manifestModel.getIndividual(resourceURI.toString());
 		if (resource == null) {
 			return false;
 		}
 		return resource.hasRDFType(roFolderClass);
 	}
 
 
 	@Override
 	public void addNamedGraph(URI graphURI, InputStream inputStream, RDFFormat rdfFormat)
 	{
 		OntModel namedGraphModel = createOntModelForNamedGraph(graphURI);
 		namedGraphModel.removeAll();
 		namedGraphModel.read(inputStream, graphURI.resolve(".").toString(), rdfFormat.getName().toUpperCase());
 	}
 
 
 	@Override
 	public boolean isROMetadataNamedGraph(URI researchObjectURI, URI graphURI)
 	{
 		Node manifest = Node.createURI(getManifestURI(researchObjectURI).toString());
 		Node bodyNode = Node.createURI(body.getURI());
 		Node annBody = Node.createURI(graphURI.toString());
 		boolean isManifest = getManifestURI(researchObjectURI).equals(graphURI);
 		boolean isAnnotationBody = graphset.containsQuad(new Quad(manifest, Node.ANY, bodyNode, annBody));
 		return isManifest || isAnnotationBody;
 	}
 
 
 	@Override
 	public void removeNamedGraph(URI researchObjectURI, URI graphURI)
 	{
 		graphURI = graphURI.normalize();
 		if (!graphset.containsGraph(graphURI.toString())) {
 			throw new IllegalArgumentException("URI not found: " + graphURI);
 		}
 		OntModel manifestModel = createOntModelForNamedGraph(graphURI);
 
 		NodeIterator it = manifestModel.listObjectsOfProperty(body);
 		while (it.hasNext()) {
 			RDFNode annotationBodyRef = it.next();
 			// TODO make sure that this named graph is internal
 			if (graphset.containsGraph(annotationBodyRef.asResource().getURI())) {
 				removeNamedGraph(researchObjectURI, URI.create(annotationBodyRef.asResource().getURI()));
 			}
 		}
 		graphset.removeGraph(graphURI.toString());
 	}
 
 
 	private OntModel createOntModelForAllNamedGraphs()
 	{
 		return createOntModelForAllNamedGraphs(OntModelSpec.OWL_MEM);
 	}
 
 
 	private OntModel createOntModelForAllNamedGraphs(OntModelSpec spec)
 	{
 		OntModel ontModel = ModelFactory.createOntologyModel(spec,
 			graphset.asJenaModel(DEFAULT_NAMED_GRAPH_URI.toString()));
 		ontModel.addSubModel(defaultModel);
 		return ontModel;
 	}
 
 
 	/**
 	 * 
 	 * @param roURI
 	 *            must end with /
 	 * @return
 	 */
 	private URI getManifestURI(URI roURI)
 	{
 		return roURI.resolve(".ro/manifest");
 	}
 
 
 	@Override
 	public void removeResearchObject(URI researchObjectURI)
 	{
 		removeNamedGraph(researchObjectURI, getManifestURI(researchObjectURI));
 	}
 
 
 	@Override
 	public boolean containsNamedGraph(URI graphURI)
 	{
 		return graphset.containsGraph(graphURI.toString());
 	}
 
 
 	@Override
 	public QueryResult executeSparql(String queryS, RDFFormat rdfFormat)
 	{
 		if (queryS == null)
 			throw new NullPointerException("Query cannot be null");
 		Query query = null;
 		try {
 			query = QueryFactory.create(queryS, sparqlSyntax);
 		}
 		catch (Exception e) {
 			throw new IllegalArgumentException("Wrong query syntax: " + e.getMessage());
 		}
 
 		switch (query.getQueryType()) {
 			case Query.QueryTypeSelect:
 				return processSelectQuery(query, rdfFormat);
 			case Query.QueryTypeConstruct:
 				return processConstructQuery(query, rdfFormat);
 			case Query.QueryTypeDescribe:
 				return processDescribeQuery(query, rdfFormat);
 			case Query.QueryTypeAsk:
 				return processAskQuery(query, rdfFormat);
 			default:
 				return null;
 		}
 	}
 
 
 	private QueryResult processSelectQuery(Query query, RDFFormat rdfFormat)
 	{
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		RDFFormat outputFormat;
 		QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
 		if (SemanticMetadataService.SPARQL_JSON.equals(rdfFormat)) {
 			outputFormat = SemanticMetadataService.SPARQL_JSON;
 			ResultSetFormatter.outputAsJSON(out, qexec.execSelect());
 		}
 		else {
 			outputFormat = SemanticMetadataService.SPARQL_XML;
 			ResultSetFormatter.outputAsXML(out, qexec.execSelect());
 		}
 		qexec.close();
 
 		return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
 	}
 
 
 	private QueryResult processAskQuery(Query query, RDFFormat rdfFormat)
 	{
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		RDFFormat outputFormat;
 		QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
 		if ("application/sparql-results+json".equals(rdfFormat.getDefaultMIMEType())) {
 			outputFormat = SemanticMetadataService.SPARQL_JSON;
 			ResultSetFormatter.outputAsJSON(out, qexec.execAsk());
 		}
 		else {
 			outputFormat = SemanticMetadataService.SPARQL_XML;
 			ResultSetFormatter.outputAsXML(out, qexec.execAsk());
 		}
 		qexec.close();
 
 		return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
 	}
 
 
 	private QueryResult processConstructQuery(Query query, RDFFormat rdfFormat)
 	{
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
 		Model resultModel = qexec.execConstruct();
 		qexec.close();
 
 		RDFFormat outputFormat;
 		if (RDFFormat.values().contains(rdfFormat)) {
 			outputFormat = rdfFormat;
 		}
 		else {
 			outputFormat = RDFFormat.RDFXML;
 		}
 
 		resultModel.write(out, outputFormat.getName().toUpperCase());
 		return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
 	}
 
 
 	private QueryResult processDescribeQuery(Query query, RDFFormat rdfFormat)
 	{
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		QueryExecution qexec = QueryExecutionFactory.create(query, new NamedGraphDataset(graphset));
 		Model resultModel = qexec.execDescribe();
 		qexec.close();
 
 		RDFFormat outputFormat;
 		if (RDFFormat.values().contains(rdfFormat)) {
 			outputFormat = rdfFormat;
 		}
 		else {
 			outputFormat = RDFFormat.RDFXML;
 		}
 
 		resultModel.write(out, outputFormat.getName().toUpperCase());
 		return new QueryResult(new ByteArrayInputStream(out.toByteArray()), outputFormat);
 	}
 
 
 	@Override
 	public Multimap<URI, Object> getAllAttributes(URI subjectURI)
 	{
 		Multimap<URI, Object> attributes = HashMultimap.<URI, Object> create();
 		// This could be an inference model but it slows down the lookup process and
 		// generates super-attributes
 		OntModel model = createOntModelForAllNamedGraphs(OntModelSpec.OWL_MEM);
 		Resource subject = model.getResource(subjectURI.toString());
 		if (subject == null)
 			return attributes;
 		StmtIterator it = model.listStatements(subject, null, (RDFNode) null);
 		while (it.hasNext()) {
 			Statement s = it.next();
 			try {
 				URI propURI = new URI(s.getPredicate().getURI());
 				Object object;
 				if (s.getObject().isResource()) {
 					// Need to check both because the model has no inference
 					if (s.getObject().as(Individual.class).hasRDFType(foafAgentClass)
 							|| s.getObject().as(Individual.class).hasRDFType(foafPersonClass)) {
 						object = s.getObject().asResource().getProperty(foafName).getLiteral().getValue();
 					}
 					else {
 						if (s.getObject().isURIResource()) {
 							object = new URI(s.getObject().asResource().getURI());
 						}
 						else {
 							continue;
 						}
 					}
 				}
 				else {
 					object = s.getObject().asLiteral().getValue();
 				}
 				if (object instanceof XSDDateTime) {
 					object = ((XSDDateTime) object).asCalendar();
 				}
 				attributes.put(propURI, object);
 			}
 			catch (URISyntaxException e) {
 				log.error(e);
 			}
 		}
 		return attributes;
 	}
 
 }
