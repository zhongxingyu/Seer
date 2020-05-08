 package cz.cuni.mff.odcleanstore.datanormalization.rules;
 
 import cz.cuni.mff.odcleanstore.configuration.BackendConfig;
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.EnumLogLevel;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.data.TableVersion;
 import cz.cuni.mff.odcleanstore.datanormalization.exceptions.DataNormalizationException;
 import cz.cuni.mff.odcleanstore.datanormalization.rules.DataNormalizationRule.Component;
 import cz.cuni.mff.odcleanstore.vocabulary.XPathFunctions;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.QueryException;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import com.hp.hpl.jena.vocabulary.XSD;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import virtuoso.jdbc3.VirtuosoDataSource;
 import virtuoso.jena.driver.VirtModel;
 
 import java.sql.Blob;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 public class DataNormalizationRulesModel {
 	public static void main(String[] args) {
 		try {
 			ConfigLoader.loadConfig();
 			BackendConfig config = ConfigLoader.getConfig().getBackendGroup();
 
 			JDBCConnectionCredentials credentials = config.getCleanDBJDBCConnectionCredentials();
 			VirtuosoDataSource dataSource = new VirtuosoDataSource();
 			dataSource.setServerName(credentials.getConnectionString());
 			dataSource.setUser(credentials.getUsername());
 			dataSource.setPassword(credentials.getPassword());
 
 			new DataNormalizationRulesModel(dataSource).compileOntologyToRules("public-contracts", "Group 1");
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 
 			e.printStackTrace();
 		}
 	}
 
 	private static final String ruleByGroupIdQueryFormat = "SELECT rules.id AS id, " +
 			"rules.groupId AS groupId, " +
 			"types.label AS type, " +
 			"components.id AS componentId, " +
 			"components.modification AS modification, " +
 			"rules.description AS description, " +
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
 			"rules.description AS description, " +
 			"components.description AS componentDescription FROM " +
 			"DB.ODCLEANSTORE.DN_RULES%s AS rules JOIN " +
 			"DB.ODCLEANSTORE.DN_RULES_GROUPS AS groups ON rules.groupId = groups.id JOIN " +
 			"DB.ODCLEANSTORE.DN_RULE_COMPONENTS%s AS components ON components.ruleId = rules.id JOIN " +
 			"DB.ODCLEANSTORE.DN_RULE_COMPONENT_TYPES AS types ON components.typeId = types.id " +
 			"WHERE groups.label = ?";
 	private static final String groupIdQuery = "SELECT id FROM DB.ODCLEANSTORE.QA_RULES_GROUPS WHERE label = ?";
 	private static final String ontologyIdQuery = "SELECT id FROM DB.ODCLEANSTORE.ONTOLOGIES WHERE label = ?";
 	private static final String ontologyGraphURIQuery = "SELECT graphName FROM DB.ODCLEANSTORE.ONTOLOGIES WHERE id = ?";
 	private static final String ontologyResourceQuery = "SELECT ?s WHERE {?s ?p ?o} GROUP BY ?s";
 	private static final String deleteRulesByOntology = "DELETE FROM DB.ODCLEANSTORE.DN_RULES%s WHERE groupId IN " +
 			"(SELECT groupId FROM DB.ODCLEANSTORE.DN_RULES_GROUPS_TO_ONTOLOGIES_MAP WHERE ontologyId = ?)";
 	private static final String deleteMapping = "DELETE FROM DB.ODCLEANSTORE.DN_RULES_GROUPS_TO_ONTOLOGIES_MAP WHERE groupId = ? AND ontologyId = ? ";
 
 	private static final String boolTruePattern = "?o = '1' OR lcase(str(?o)) = 'true' OR lcase(str(?o)) = 'yes' OR lcase(str(?o)) = 't' OR lcase(str(?o)) = 'y'";
 	private static final String boolFalsePattern = "?o = '0' OR lcase(str(?o)) = 'false' OR lcase(str(?o)) = 'no' OR lcase(str(?o)) = 'f' OR lcase(str(?o)) = 'n'";
 	private static final String insertConvertedTruePropertyValueFormat = "{?s <%s> ?t} WHERE {GRAPH $$graph$$ {SELECT ?s <%s>(1) AS ?t WHERE {?s <%s> ?o. FILTER (" + boolTruePattern + ")}}}";
 	private static final String insertConvertedFalsePropertyValueFormat = "{?s <%s> ?f} WHERE {GRAPH $$graph$$ {SELECT ?s <%s>(0) AS ?f WHERE {?s <%s> ?o. FILTER (" + boolFalsePattern + ")}}}";
 	private static final String deleteUnconvertedBoolPropertyValueFormat = "{?s <%s> ?o} WHERE {GRAPH $$graph$$ {?s <%s> ?o. FILTER (" + boolTruePattern + " OR " + boolFalsePattern + ")}}";
 
 	private static final String insertConvertedStringPropertyValueFormat = "{?s <%s> ?x} WHERE {GRAPH $$graph$$ {SELECT ?s <%s>(str(?o)) AS ?x WHERE {?s <%s> ?o}}}";
 	private static final String deleteUnconvertedStringPropertyValueFormat = "{?s <%s> ?o} WHERE {GRAPH $$graph$$ {?s <%s> ?o. FILTER (?o != <%s>(str(?o)))}}";
 
 	private static final String insertConvertedDatePropertyValueFormat = "{?s <%s> ?x} WHERE {GRAPH $$graph$$ {SELECT ?s <%s>(str(?o)) AS ?x WHERE {?s <%s> ?o}}}";
 	private static final String deleteUnconvertedDatePropertyValueFormat = "{?s <%s> ?o} WHERE {GRAPH $$graph$$ {?s <%s> ?o. FILTER (?o != <%s>(str(?o)))}}";
 
 	private static final String insertRule = "INSERT INTO DB.ODCLEANSTORE.DN_RULES%s (groupId, description) VALUES (?, ?)";
 	private static final String lastIdQuery = "SELECT identity_value() AS id";
 	private static final String insertComponent = "INSERT INTO DB.ODCLEANSTORE.DN_RULE_COMPONENTS%s (ruleId, typeId, modification, description) " +
 			"SELECT ? AS ruleId, id AS typeId, ? AS modification, ? AS description FROM DB.ODCLEANSTORE.DN_RULE_COMPONENT_TYPES WHERE label = ?";
 	private static final String mapGroupToOntology = "INSERT INTO DB.ODCLEANSTORE.DN_RULES_GROUPS_TO_ONTOLOGIES_MAP (groupId, ontologyId) VALUES (?, ?)";
 
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
 
 	public DataNormalizationRulesModel (VirtuosoDataSource dataSource) {
 		this.endpoint = new JDBCConnectionCredentials(
 				dataSource.getServerName(),
 				dataSource.getUser(),
 				dataSource.getPassword());
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
 				ResultSet result = results.getCurrentResultSet();
 
 				Integer id = result.getInt("id");
 
 				Integer groupId = result.getInt("groupId");
 
 				Blob typeBlob = result.getBlob("type");
 				String type = new String(typeBlob.getBytes(1, (int)typeBlob.length()));
 				
 				Integer componentId = result.getInt("componentId");
 
 				Blob modificationBlob = result.getBlob("modification");
 				String modification = new String(modificationBlob.getBytes(1, (int)modificationBlob.length()));
 
 				Blob descriptionBlob = result.getBlob("description");
 				String description;
 
 				if (descriptionBlob != null && !result.wasNull()) {
 					description = new String(descriptionBlob.getBytes(1, (int)descriptionBlob.length()));
 				} else {
 					description = null;
 				}
 
 				Blob componentDescriptionBlob = result.getBlob("componentDescription");
 				String componentDescription;
 				
 				if (componentDescriptionBlob != null && !result.wasNull()) {
 					componentDescription = new String(componentDescriptionBlob.getBytes(1, (int)componentDescriptionBlob.length()));
 				} else {
 					componentDescription = null;
 				}
 
 				if (rules.containsKey(id)) {
 					DataNormalizationRule rule = rules.get(id);
 
 					rule.addComponent(componentId, type, modification, componentDescription);
 				} else {
 					DataNormalizationRule rule = new DataNormalizationRule(id, groupId, description);
 					
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
			Collection<DataNormalizationRule> groupSpecific = queryRules(String.format(ruleByGroupIdQueryFormat, tableVersion, tableVersion), groupIds[i]);
 
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
 		Set<DataNormalizationRule> rules = new HashSet<DataNormalizationRule>();
 
 		for (int i = 0; i < groupLabels.length; ++i) {
			Collection<DataNormalizationRule> groupSpecific = queryRules(String.format(ruleByGroupLabelQueryFormat, tableVersion, tableVersion), groupLabels[i]);
 
 			rules.addAll(groupSpecific);
 		}
 
 		return rules;
 	}
 
 	private Integer getGroupId(String groupLabel) throws DataNormalizationException {
 		try {
 			WrappedResultSet resultSet = getCleanConnection().executeSelect(groupIdQuery, groupLabel);
 
 			if (!resultSet.next()) throw new DataNormalizationException("No '" + groupLabel + "' QA Rule group.");
 
 			return resultSet.getCurrentResultSet().getInt("id");
 		} catch (DatabaseException e) {
 			throw new DataNormalizationException(e);
 		} catch (SQLException e) {
 			throw new DataNormalizationException(e);
 		}
 	}
 
 	private Integer getOntologyId(String ontologyLabel) throws DataNormalizationException {
 		try {
 			WrappedResultSet resultSet = getCleanConnection().executeSelect(ontologyIdQuery, ontologyLabel);
 
 			if (!resultSet.next()) throw new DataNormalizationException("No '" + ontologyLabel + "' ontology.");
 
 			return resultSet.getCurrentResultSet().getInt("id");
 		} catch (DatabaseException e) {
 			throw new DataNormalizationException(e);
 		} catch (SQLException e) {
 			throw new DataNormalizationException(e);
 		}
 	}
 
 	private String getOntologyGraphURI(Integer ontologyId) throws DataNormalizationException {
 		try {
 			WrappedResultSet resultSet = getCleanConnection().executeSelect(ontologyGraphURIQuery, ontologyId);
 
 			if (!resultSet.next()) throw new DataNormalizationException("No ontology with id " + ontologyId + ".");
 
 			return resultSet.getCurrentResultSet().getString("graphName");
 		} catch (DatabaseException e) {
 			throw new DataNormalizationException(e);
 		} catch (SQLException e) {
 			throw new DataNormalizationException(e);
 		}
 	}
 
 	private void mapGroupToOntology(Integer groupId, Integer ontologyId) throws DataNormalizationException {
 		try {
 			getCleanConnection().execute(mapGroupToOntology, groupId, ontologyId);
 		} catch (DatabaseException e) {
 			throw new DataNormalizationException(e);
 		}
 	}
 
 	public void compileOntologyToRules(String ontologyLabel, String groupLabel) throws DataNormalizationException {
 		try {
 			Integer groupId = getGroupId(groupLabel);
 			Integer ontologyId = getOntologyId(ontologyLabel);
 
 			compileOntologyToRules(ontologyId, groupId);
 		} finally {
 			closeCleanConnection();
 		}
 	}
 
 	/**
 	 * creates rules that verify properties of the input ontology (stored in the clean database)
 	 * @param ontologyUri the uri of the ontology whose properties should be verified by the ouput rules
 	 * @param groupId the ID of a rule group to which the new rules should be stored
 	 * @throws DataNormalizationException
 	 */
 	public void compileOntologyToRules(Integer ontologyId, Integer groupId) throws DataNormalizationException {
 		try {
 			String ontologyGraphURI = getOntologyGraphURI(ontologyId);
 
 			VirtModel ontology = VirtModel.openDatabaseModel(ontologyGraphURI,
 					endpoint.getConnectionString(),
 					endpoint.getUsername(),
 					endpoint.getPassword());
 
 			QueryExecution query = QueryExecutionFactory.create(ontologyResourceQuery, ontology);
 
 			com.hp.hpl.jena.query.ResultSet resultSet = query.execSelect();
 
 			/**
 			 * Remove all the rules generated from this ontology
 			 */
 			dropRules(groupId, ontologyId);
 
 			mapGroupToOntology(groupId, ontologyId);
 
 			/**
 			 * Process all resources in the ontology
 			 */
 			while (resultSet.hasNext()) {
 				QuerySolution solution = resultSet.next();
 
 				processOntologyResource(solution.getResource("s"), ontology, ontologyGraphURI, groupId);
 			}
 		} finally {
 			closeCleanConnection();
 		}
 	}
 
 	/**
 	 * removes all rules that were created according to this ontology
 	 * @param ontology the uri of the ontology to which the deleted rules are to be mapped
 	 * @throws DataNormalizationException
 	 */
 	private void dropRules(Integer groupId, Integer ontologyId) throws DataNormalizationException {
 		try {
 			getCleanConnection().execute(String.format(deleteRulesByOntology, TableVersion.COMMITTED.getTableSuffix()), ontologyId);
 			getCleanConnection().execute(String.format(deleteRulesByOntology, TableVersion.UNCOMMITTED.getTableSuffix()), ontologyId);
 			getCleanConnection().execute(deleteMapping, groupId, ontologyId);
 		} catch (DatabaseException e) {
 			throw new DataNormalizationException(e);
 		}
 	}
 
 	/**
 	 * examines one resource and creates rule(s) for it
 	 * @param resource the resource to be examined
 	 * @param model the ontology model
 	 * @param ontology the name of the ontology (URI)
 	 * @param groupId the ID of the rule group to store the rules to
 	 * @throws DataNormalizationException
 	 */
 	private void processOntologyResource(Resource resource, Model model, String ontology, Integer groupId) throws DataNormalizationException {
 		if (model.contains(resource, RDFS.range, model.asRDFNode(Node.ANY))) {
 
 			/**
 			 * Correct boolean
 			 */
 			if (model.contains(resource, RDFS.range, XSD.xboolean)) {
 				DataNormalizationRule rule = new DataNormalizationRule(null, groupId, "Convert " + resource.getLocalName() + " into " + XSD.xstring.getLocalName(),
 						"INSERT",
 						String.format(Locale.ROOT, insertConvertedTruePropertyValueFormat, resource.getURI(), XPathFunctions.boolFunction, resource.getURI()),
 						"Create proper " + XSD.xboolean.getLocalName() + " value for the property " + resource.getURI() + " (\"1\", \"true\", ...)",
 
 						"INSERT",
 						String.format(Locale.ROOT, insertConvertedFalsePropertyValueFormat, resource.getURI(), XPathFunctions.boolFunction, resource.getURI()),
 						"Create proper " + XSD.xboolean.getLocalName() + " value for the property " + resource.getURI() + " (\"0\", \"false\", ...)",
 
 						"DELETE",
 						String.format(Locale.ROOT, deleteUnconvertedBoolPropertyValueFormat, resource.getURI(), resource.getURI()),
 						"Remove all improper values of the property " + resource.getURI());
 
 				storeRule(rule, ontology);
 			}
 
 			/**
 			 * Correct string
 			 */
 			if (model.contains(resource, RDFS.range, XSD.xstring)) {
 				DataNormalizationRule rule = new DataNormalizationRule(null, groupId, "Convert " + resource.getLocalName() + " into " + XSD.xstring.getLocalName(),
 						"INSERT",
 						String.format(Locale.ROOT, insertConvertedStringPropertyValueFormat, resource.getURI(), XPathFunctions.stringFunction, resource.getURI()),
 						"Create proper " + XSD.xstring.getLocalName() + " value for the property " + resource.getURI(),
 
 						"DELETE",
 						String.format(Locale.ROOT, deleteUnconvertedStringPropertyValueFormat, resource.getURI(), resource.getURI(), XPathFunctions.stringFunction),
 						"Remove all improper values of the property " + resource.getURI());
 
 				storeRule(rule, ontology);
 			}
 
 			/**
 			 * Correct date formats ("YYYY" to "YYYY-MM-DD" etc.)
 			 */
 			if (model.contains(resource, RDFS.range, XSD.date)) {
 				DataNormalizationRule rule = new DataNormalizationRule(null, groupId, "Convert " + resource.getLocalName() + " into " + XSD.date.getLocalName(),
 						"INSERT",
 						String.format(Locale.ROOT, insertConvertedDatePropertyValueFormat, resource.getURI(), XPathFunctions.dateFunction, resource.getURI()),
 						"Create proper " + XSD.date.getLocalName() + " value for the property " + resource.getURI(),
 
 						"DELETE",
 						String.format(Locale.ROOT, deleteUnconvertedDatePropertyValueFormat, resource.getURI(), resource.getURI(), XPathFunctions.dateFunction),
 						"Remove all improper values of the property " + resource.getURI());
 
 				storeRule(rule, ontology);
 			}
 		}
 	}
 
 	/**
 	 * stores a generated rule and maps it to the ontology to be able to track its origin and dependence on the ontology
 	 * @param rule the rule to be stored
 	 * @param ontology the ontology the rule should be mapped to
 	 * @throws DataNormalizationException
 	 */
 	private void storeRule (DataNormalizationRule rule, String ontology) throws DataNormalizationException {
 		try {
 			getCleanConnection().adjustTransactionLevel(EnumLogLevel.TRANSACTION_LEVEL, false);
 
 			getCleanConnection().execute(String.format(insertRule, TableVersion.COMMITTED.getTableSuffix()), rule.getGroupId(), rule.getDescription());
 
 			WrappedResultSet result;
 			
 			Integer id = 0;
 			result = getCleanConnection().executeSelect(lastIdQuery);
 			
 			if (result.next()) {
 				id = result.getInt("id");
 			} else {
 				throw new DataNormalizationException("Failed to bind rule component to rule.");
 			}
 			
 			getCleanConnection().execute(String.format(insertRule, TableVersion.UNCOMMITTED.getTableSuffix()), rule.getGroupId(), rule.getDescription());
 			
 			Integer idUncommitted = 0;
 			result = getCleanConnection().executeSelect(lastIdQuery);
 			
 			if (result.next()) {
 				idUncommitted = result.getInt("id");
 			} else {
 				throw new DataNormalizationException("Failed to bind rule component to rule (Uncommitted).");
 			}
 
 			Component[] components = rule.getComponents();
 
 			for (int i = 0; i < components.length; ++i) {
 				getCleanConnection().execute(String.format(insertComponent, TableVersion.COMMITTED.getTableSuffix()),
 						id, components[i].getModification(), components[i].getDescription(), components[i].getType().toString());
 				getCleanConnection().execute(String.format(insertComponent, TableVersion.UNCOMMITTED.getTableSuffix()),
 						idUncommitted, components[i].getModification(), components[i].getDescription(), components[i].getType().toString());
 			}
 
 			getCleanConnection().commit();
 
 			LOG.info("Generated data normalization rule from ontology " + ontology);
 		} catch (DatabaseException e) {
 			LOG.error("Could not store Data Normalization rule generated from ontology");
 			throw new DataNormalizationException(e);
 		} catch (QueryException e) {
 			LOG.error("Could not store Data Normalization rule generated from ontology");
 			throw new DataNormalizationException(e);
 		} catch (SQLException e) {
 			LOG.error("Could not store Data Normalization rule generated from ontology");
 			throw new DataNormalizationException(e);
 		}
 	}
 }
