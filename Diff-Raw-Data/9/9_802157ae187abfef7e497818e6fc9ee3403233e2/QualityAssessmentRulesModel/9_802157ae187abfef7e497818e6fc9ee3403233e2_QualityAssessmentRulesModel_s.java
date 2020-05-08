 package cz.cuni.mff.odcleanstore.qualityassessment.rules;
 
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.data.TableVersion;
 import cz.cuni.mff.odcleanstore.qualityassessment.exceptions.QualityAssessmentException;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import virtuoso.jena.driver.VirtModel;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Rules Model.
  *
  * Facilitates changes and queries for quality assessment rules.
  *
  * @author Jakub Daniel
  */
 public class QualityAssessmentRulesModel {
 
 	private static final String ruleByGroupIdQueryFormat = "SELECT id, groupId, filter, coefficient, label, description FROM " +
 			"DB.ODCLEANSTORE.QA_RULES%s WHERE groupId = ?";
 	private static final String ruleByGroupLabelQueryFormat = "SELECT rules.id AS id," +
 			"rules.groupId AS groupId," +
 			"rules.filter AS filter," +
 			"rules.coefficient AS coefficient," +
 			"rules.label AS label, " +
 			"rules.description AS description FROM " +
 			"DB.ODCLEANSTORE.QA_RULES%s AS rules JOIN " +
 			"DB.ODCLEANSTORE.QA_RULES_GROUPS AS groups ON rules.groupId = groups.id " +
 			"WHERE groups.label = ?";
 	private static final String ontologyResourceQuery = "SELECT ?s WHERE {?s ?p ?o} GROUP BY ?s";
 
 	private static final Logger LOG = LoggerFactory.getLogger(QualityAssessmentRulesModel.class);
 
 	private JDBCConnectionCredentials endpoint;
 
 	/**
 	 * Connection to dirty database (needed in all cases to work on a new graph or a copy of an existing one)
 	 */
 	private VirtuosoConnectionWrapper cleanConnection;
 	private TableVersion tableVersion;
 
 	/**
 	 * constructs new connection to the dirty database.
 	 *
 	 * @return wrapped connection to the dirty database
 	 * @throws DatabaseException
 	 */
 	private VirtuosoConnectionWrapper getCleanConnection () throws DatabaseException {
         if (cleanConnection == null) {
         	cleanConnection = VirtuosoConnectionWrapper.createConnection(endpoint);
        	}
 		return cleanConnection;
 	}
 
 	/**
 	 * makes sure the connection to the dirty database is closed and not referenced
 	 */
 	private void closeCleanConnection() {
 		try {
 			if (cleanConnection != null) {
 				cleanConnection.close();
 			}
 		} catch (DatabaseException e) {
 		} finally {
 			cleanConnection = null;
 		}
 	}
 
 	public QualityAssessmentRulesModel (JDBCConnectionCredentials endpoint) {
 		this(endpoint, TableVersion.COMMITTED);
 	}
 
 	public QualityAssessmentRulesModel (JDBCConnectionCredentials endpoint, TableVersion tableVersion) {
 		this.endpoint = endpoint;
 		this.tableVersion = tableVersion;
 	}
 
 	private Collection<QualityAssessmentRule> queryRules (String query, Object... objects) throws QualityAssessmentException {
 		Collection<QualityAssessmentRule> rules = new ArrayList<QualityAssessmentRule>();
 
 		try {
 			WrappedResultSet results = getCleanConnection().executeSelect(query, objects);
 
 			/**
 			 * Fill the collection with rule instances for all records in database.
 			 */
 			while (results.next()) {
 				Integer id = results.getInt("id");
 				Integer groupId = results.getInt("groupId");
 				String filter = results.getNString("filter");
 				Double coefficient = results.getDouble("coefficient");
 				String label = results.getNString("label");
 				String description = results.getNString("description");
 
 				rules.add(new QualityAssessmentRule(id, groupId, filter, coefficient, label, description));
 			}
 		} catch (DatabaseException e) {
 			throw new QualityAssessmentException(e);
 		} catch (SQLException e) {
 			throw new QualityAssessmentException(e);
 		} finally {
 			closeCleanConnection();
 		}
 
 		return rules;
 	}
 
 	/**
 	 * @param groupIds IDs of the rule groups from which the rules are selected
 	 */
 	public Collection<QualityAssessmentRule> getRules (Integer... groupIds) throws QualityAssessmentException {
		Set<QualityAssessmentRule> rules = new HashSet<QualityAssessmentRule>();
 
 		for (int i = 0; i < groupIds.length; ++i) {
 			Collection<QualityAssessmentRule> groupSpecific = queryRules(String.format(ruleByGroupIdQueryFormat, tableVersion.getTableSuffix()), groupIds[i]);
 
 			rules.addAll(groupSpecific);
 		}
 
 		return rules;
 	}
 
 	/**
 	 * @param groupLabels set of labels of groups from which the rules are selected
 	 */
 	public Collection<QualityAssessmentRule> getRules (String... groupLabels) throws QualityAssessmentException {
 		Set<QualityAssessmentRule> rules = new HashSet<QualityAssessmentRule>();
 
 		for (int i = 0; i < groupLabels.length; ++i) {
 			Collection<QualityAssessmentRule> groupSpecific = queryRules(String.format(ruleByGroupLabelQueryFormat, tableVersion.getTableSuffix()), groupLabels[i]);
 
 			rules.addAll(groupSpecific);
 		}
 
 		return rules;
 	}
 
 	public Collection<QualityAssessmentRule> compileOntologyToRules(String ontologyGraphURI) throws QualityAssessmentException {
 		try {
 			VirtModel ontology = VirtModel.openDatabaseModel(ontologyGraphURI,
 					endpoint.getConnectionString(),
 					endpoint.getUsername(),
 					endpoint.getPassword());
 
 			QueryExecution query = QueryExecutionFactory.create(ontologyResourceQuery, ontology);
 
 			com.hp.hpl.jena.query.ResultSet resultSet = query.execSelect();
 
 			LOG.debug("Generating QA rules for <" + ontologyGraphURI + ">");
 
 			List<QualityAssessmentRule> ruleList = new ArrayList<QualityAssessmentRule>();
 
 			while (resultSet.hasNext()) {
 				QuerySolution solution = resultSet.next();
 
 				ruleList.addAll(processOntologyResource(solution.getResource("s"), ontology, ontologyGraphURI));
 			}
 
 			return ruleList;
 		} finally {
 			closeCleanConnection();
 		}
 	}
 
 	private Collection<QualityAssessmentRule> processOntologyResource(Resource resource,
 			Model model, String ontology) throws QualityAssessmentException {
 		List<QualityAssessmentRule> ruleList = new ArrayList<QualityAssessmentRule>();
 
 		final String skosNS = "http://www.w3.org/2004/02/skos/core#";
 
 		LOG.debug("Processing: " + resource.getLocalName() + "(" + resource.getURI() + ")");
 
 		/**
 		 * Functional Property can have only 1 value
 		 */
 		if (model.contains(resource, RDF.type, OWL.FunctionalProperty)) {
 			QualityAssessmentRule rule = new QualityAssessmentFunctionalPropertyAmbiguityRule(null, null, resource);
 
 			LOG.info("Generated QA Rule for functional property: " + resource.getLocalName());
 
 			ruleList.add(rule);
 		}
 
 		/**
 		 * Value of Inverse Functional Property cannot be shared by two or more subjects
 		 */
 		if (model.contains(resource, RDF.type, OWL.InverseFunctionalProperty)) {
 			QualityAssessmentRule rule = new QualityAssessmentInverseFunctionalPropertyInjectivityRule(null, null, resource);
 
 			LOG.info("Generated QA Rule for inverse functional property: " + resource.getLocalName());
 
 			ruleList.add(rule);
 		}
 
 		/**
 		 * Enumeration check based on Concept Scheme
 		 */
 		if (model.contains(resource, RDF.type, model.createProperty(skosNS, "ConceptScheme"))) {
 			StmtIterator enumerations = model.listStatements(resource, model.createProperty(skosNS, "hasTopConcept"), model.getRDFNode(Node.ANY));
 
 			/**
 			 * Generate list of possible values
 			 */
 			Set<Resource> values = new HashSet<Resource>();
 
 			while (enumerations.hasNext()) {
 				Statement conceptStmt = enumerations.next();
 
 				values.add(conceptStmt.getObject().asResource());
 			}
 
 			/**
 			 * Generate rules for all properties with enumerated range
 			 */
 			QueryExecution queryExecution = QueryExecutionFactory.create("SELECT ?s WHERE {?s <" + RDFS.range + "> ?o. ?o <" + OWL.hasValue + "> <" + resource.getURI() + ">.}", model);
 
 			com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();
 
 			while (resultSet.hasNext()) {
 				QuerySolution solution = resultSet.nextSolution();
 
 				LOG.info("Generated QA Rule for property with enumerable range: " + solution.getResource("s").getLocalName());
 
 				QualityAssessmentRule rule = new QualityAssessmentEnumerablePropertyRule(null, solution.getResource("s"), values);
 
 				ruleList.add(rule);
 			}
 		}
 
 		return ruleList;
 	}
 }
