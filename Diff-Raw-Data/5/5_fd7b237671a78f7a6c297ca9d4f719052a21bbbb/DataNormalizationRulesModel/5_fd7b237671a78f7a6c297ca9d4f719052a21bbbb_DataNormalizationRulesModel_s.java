 package cz.cuni.mff.odcleanstore.datanormalization.rules;
 
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.data.TableVersion;
 import cz.cuni.mff.odcleanstore.datanormalization.exceptions.DataNormalizationException;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import com.hp.hpl.jena.vocabulary.XSD;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import virtuoso.jena.driver.VirtModel;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Utility class used to query rules and generate new rules from ontologies
  *
  * @author Jakub Daniel
  */
 public class DataNormalizationRulesModel {
 
 	private static final String ruleByGroupIdQueryFormat = "SELECT rules.id AS id, " +
 			"rules.groupId AS groupId, " +
 			"types.label AS type, " +
 			"components.id AS componentId, " +
 			"components.modification AS modification, " +
 			"rules.description AS description, " +
 			"rules.label AS label, " +
 			"components.description AS componentDescription FROM " +
 			"DB.ODCLEANSTORE.DN_RULES%s AS rules JOIN " +
 			"DB.ODCLEANSTORE.DN_RULE_COMPONENTS%s AS components ON components.ruleId = rules.id JOIN " +
 			"DB.ODCLEANSTORE.DN_RULE_COMPONENT_TYPES AS types ON components.typeId = types.id " +
			"WHERE groupId = ?";
 	private static final String ruleByGroupLabelQueryFormat = "SELECT rules.id AS id, " +
 			"rules.groupId AS groupId, " +
 			"types.label AS type, " +
 			"components.id AS componentId, " +
 			"components.modification AS modification, " +
 			"rules.label AS label, " +
 			"rules.description AS description, " +
 			"components.description AS componentDescription FROM " +
 			"DB.ODCLEANSTORE.DN_RULES%s AS rules JOIN " +
 			"DB.ODCLEANSTORE.DN_RULES_GROUPS AS groups ON rules.groupId = groups.id JOIN " +
 			"DB.ODCLEANSTORE.DN_RULE_COMPONENTS%s AS components ON components.ruleId = rules.id JOIN " +
 			"DB.ODCLEANSTORE.DN_RULE_COMPONENT_TYPES AS types ON components.typeId = types.id " +
			"WHERE groups.label = ?";
 
 	private static final String ontologyResourceQuery = "SELECT ?s WHERE {?s ?p ?o} GROUP BY ?s";
 
 	private static final Logger LOG = LoggerFactory.getLogger(DataNormalizationRulesModel.class);
 
 	private JDBCConnectionCredentials endpoint;
 	private TableVersion tableVersion;
 
 	/**
 	 * Connection to dirty database (needed in all cases to work on a new graph or a copy of an existing one)
 	 */
 	private VirtuosoConnectionWrapper cleanConnection;
 
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
 
 	public DataNormalizationRulesModel (JDBCConnectionCredentials endpoint) {
 		this(endpoint, TableVersion.COMMITTED);
 	}
 
 	public DataNormalizationRulesModel (JDBCConnectionCredentials endpoint, TableVersion tableVersion) {
 		this.endpoint = endpoint;
 		this.tableVersion = tableVersion;
 	}
 
 	/**
 	 * selects all rules that satisfy conditions of the query. It is required that the query is projected to
 	 * id (int), groupId (int), type (string), modification (string), description (string), componentDescription (string)
 	 * @param query the select query
 	 * @param objects the bindings to the query
 	 * @return a collection of the selected rules
 	 * @throws DataNormalizationException
 	 */
 	private Collection<DataNormalizationRule> queryRules (String query, Object... objects) throws DataNormalizationException {
 		Map<Integer, DataNormalizationRule> rules = new HashMap<Integer, DataNormalizationRule>();
 
 		try {
 			WrappedResultSet results = getCleanConnection().executeSelect(query, objects);
 
 			/**
 			 * Fill the collection with rule instances for all records in database.
 			 */
 			while (results.next()) {
 				Integer id = results.getInt("id");
 				Integer groupId = results.getInt("groupId");
 				String type = results.getNString("type");
 				Integer componentId = results.getInt("componentId");
 				String modification = results.getNString("modification");
 				String label = results.getNString("label");
 				String description = results.getNString("description");
 				String componentDescription = results.getNString("componentDescription");
 
 				if (rules.containsKey(id)) {
 					DataNormalizationRule rule = rules.get(id);
 
 					rule.addComponent(componentId, type, modification, componentDescription);
 				} else {
 					DataNormalizationRule rule = new DataNormalizationRule(id, groupId, label, description);
 
 					rule.addComponent(componentId, type, modification, componentDescription);
 
 					rules.put(id, rule);
 				}
 			}
 		} catch (DatabaseException e) {
 			throw new DataNormalizationException(e);
 		} catch (SQLException e) {
 			throw new DataNormalizationException(e);
 		} finally {
 			closeCleanConnection();
 		}
 
 		return rules.values();
 	}
 
 	/**
 	 * selects rules that belong to groups whose IDs are among groupIds
 	 * @param groupIds IDs of the rule groups from which the rules are selected
 	 * @return a collection of the selected rules
 	 */
 	public Collection<DataNormalizationRule> getRules (Integer... groupIds) throws DataNormalizationException {
 		Set<DataNormalizationRule> rules = new HashSet<DataNormalizationRule>();
 
 		for (int i = 0; i < groupIds.length; ++i) {
 			Collection<DataNormalizationRule> groupSpecific = queryRules(String.format(ruleByGroupIdQueryFormat, tableVersion.getTableSuffix(), tableVersion.getTableSuffix()), groupIds[i]);
 
 			rules.addAll(groupSpecific);
 		}
 
 		return rules;
 	}
 
 	/**
 	 * selects rules that belong to groups whose labels are among groupLabels
 	 * @param groupLabels set of labels of groups from which the rules are selected
 	 * @return a collection of the selected rules
 	 */
 	public Collection<DataNormalizationRule> getRules (String... groupLabels) throws DataNormalizationException {
 		List<DataNormalizationRule> rules = new ArrayList<DataNormalizationRule>();
 
 		for (int i = 0; i < groupLabels.length; ++i) {
 			Collection<DataNormalizationRule> groupSpecific = queryRules(String.format(ruleByGroupLabelQueryFormat, tableVersion.getTableSuffix(), tableVersion.getTableSuffix()), groupLabels[i]);
 
 			rules.addAll(groupSpecific);
 		}
 
 		return rules;
 	}
 
 	/**
 	 * Creates predefined rules for properties with range: Boolean, String, Date, Integer, NonNegativeInteger, NonPositiveInteger, Double/Decimal
 	 * @param ontologyGraphURI URI of a graph containing ontology definition (expected to already exist in the database to which the DataNormalizationRulesModel is connected)
 	 * @return set of generated rules
 	 * @throws DataNormalizationException
 	 */
 	public Collection<DataNormalizationRule> compileOntologyToRules(String ontologyGraphURI) throws DataNormalizationException {
 		try {
 			VirtModel ontology = VirtModel.openDatabaseModel(ontologyGraphURI,
 					endpoint.getConnectionString(),
 					endpoint.getUsername(),
 					endpoint.getPassword());
 
 			QueryExecution query = QueryExecutionFactory.create(ontologyResourceQuery, ontology);
 
 			com.hp.hpl.jena.query.ResultSet resultSet = query.execSelect();
 
 			LOG.debug("Generating DN rules for <" + ontologyGraphURI + ">");
 
 			List<DataNormalizationRule> ruleList = new ArrayList<DataNormalizationRule>();
 
 			/**
 			 * Process all resources in the ontology
 			 */
 			while (resultSet.hasNext()) {
 				QuerySolution solution = resultSet.next();
 
 				ruleList.addAll(processOntologyResource(solution.getResource("s"), ontology, ontologyGraphURI));
 			}
 
 			return ruleList;
 		} finally {
 			closeCleanConnection();
 		}
 	}
 
 	/**
 	 * Identifies concrete resources that may be handled by generated rules
 	 * @param resource to be processed
 	 * @param model in which the resource exists
 	 * @param ontology
 	 * @return list of rules generated for the processed resource (may be empty)
 	 * @throws DataNormalizationException
 	 */
 	private Collection<DataNormalizationRule> processOntologyResource(Resource resource, Model model, String ontology) throws DataNormalizationException {
 		List<DataNormalizationRule> ruleList = new ArrayList<DataNormalizationRule>();
 
 		if (model.contains(resource, RDFS.range, model.asRDFNode(Node.ANY))) {
 
 			/**
 			 * Correct boolean
 			 */
 			if (model.contains(resource, RDFS.range, XSD.xboolean)) {
 				DataNormalizationRule rule = new DataNormalizationBooleanRule(null, null, resource);
 
 				LOG.info("Generated DN rule for boolean");
 
 				ruleList.add(rule);
 			}
 
 			/**
 			 * Correct string
 			 */
 			if (model.contains(resource, RDFS.range, XSD.xstring)) {
 				DataNormalizationRule rule = new DataNormalizationStringRule(null, null, resource);
 
 				LOG.info("Generated DN rule for string");
 
 				ruleList.add(rule);
 			}
 			
 			/**
 			 * Convert numbers
 			 */
 			if (model.contains(resource, RDFS.range, XSD.nonNegativeInteger)) {
 				DataNormalizationRule rule = new DataNormalizationNonNegativeIntegerRule(null, null, resource);
 
 				LOG.info("Generated DN rule for non negative integer");
 
 				ruleList.add(rule);
 			}
 
 			if (model.contains(resource, RDFS.range, XSD.nonPositiveInteger)) {
 				DataNormalizationRule rule = new DataNormalizationNonPositiveIntegerRule(null, null, resource);
 
 				LOG.info("Generated DN rule for non positive integer");
 
 				ruleList.add(rule);
 			}
 			
 			if (model.contains(resource, RDFS.range, XSD.integer)) {
 				DataNormalizationRule rule = new DataNormalizationIntegerRule(null, null, resource);
 
 				LOG.info("Generated DN rule for integer");
 
 				ruleList.add(rule);
 			}
 			
 			if (model.contains(resource, RDFS.range, XSD.xdouble) || model.contains(resource, RDFS.range, XSD.decimal)) {
 				DataNormalizationRule rule = new DataNormalizationNumberRule(null, null, resource);
 
 				LOG.info("Generated DN rule for number");
 
 				ruleList.add(rule);
 			}
 
 			/**
 			 * Correct date formats ("YYYY" to "YYYY-MM-DD" etc.)
 			 */
 			if (model.contains(resource, RDFS.range, XSD.date)) {
 				DataNormalizationRule rule = new DataNormalizationDateRule(null, null, resource);
 
 				LOG.info("Generated DN rule for date");
 
 				ruleList.add(rule);
 			}
 		}
 
 		return ruleList;
 	}
 }
