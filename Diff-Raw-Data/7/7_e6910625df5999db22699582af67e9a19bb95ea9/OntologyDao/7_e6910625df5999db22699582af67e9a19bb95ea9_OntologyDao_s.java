 package cz.cuni.mff.odcleanstore.webfrontend.dao.onto;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.data.EnumDatabaseInstance;
 import cz.cuni.mff.odcleanstore.data.GraphLoader;
 import cz.cuni.mff.odcleanstore.datanormalization.rules.DataNormalizationRule;
 import cz.cuni.mff.odcleanstore.datanormalization.rules.DataNormalizationRulesModel;
 import cz.cuni.mff.odcleanstore.qualityassessment.rules.QualityAssessmentRule;
 import cz.cuni.mff.odcleanstore.qualityassessment.rules.QualityAssessmentRulesModel;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCSInternal;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRule;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRuleComponent;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRuleComponentType;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.onto.Ontology;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.qa.QARule;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRuleComponentDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRuleComponentTypeDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRuleDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRulesGroupDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.qa.QARuleDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.qa.QARulesGroupDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.users.UserDao;
 import cz.cuni.mff.odcleanstore.webfrontend.util.CodeSnippet;
 
 /**
  * The Ontology Dao.
  * 
  * @author Tomáš Soukup
  *
  */
 public class OntologyDao extends DaoForEntityWithSurrogateKey<Ontology>
 {
 	public static final String TABLE_NAME = TABLE_NAME_PREFIX + "ONTOLOGIES";
 	private static final String RULE_GROUP_PREFIX = "Generated from ontology: ";
 	private static final String QA_MAPPING_TABLE_NAME = "QA_RULES_GROUPS_TO_ONTOLOGIES_MAP";
 	private static final String DN_MAPPING_TABLE_NAME = "DN_RULES_GROUPS_TO_ONTOLOGIES_MAP";
 
 	protected static Logger logger = Logger.getLogger(OntologyDao.class);
 
 	private static final long serialVersionUID = 1L;
 
 	private ParameterizedRowMapper<Ontology> rowMapper;
 
 	/**
 	 * 
 	 */
 	public OntologyDao()
 	{
 		this.rowMapper = new OntologyRowMapper();
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return TABLE_NAME;
 	}
 
 	@Override
 	protected ParameterizedRowMapper<Ontology> getRowMapper()
 	{
 		return rowMapper;
 	}
 
 	@Override
 	protected String getSelectAndFromClause()
 	{
 		String query = "SELECT u.username, o.* " +
 			" FROM " + getTableName() + " AS o LEFT JOIN " + UserDao.TABLE_NAME + " AS u ON (o.authorId = u.id)";
 		return query;
 	}
 	
 	@Override
 	public Ontology load(Integer id)
 	{
 		return loadBy("o.id", id);
 	}
 	
 	/** Load Ontology without retrieving its definition. */
 	private Ontology loadRaw(Integer id) 
 	{
 		return super.loadBy("o.id", id);
 	}
 
 	@Override
 	public void save(final Ontology item) throws Exception
 	{
 		executeInTransaction(new CodeSnippet()
 		{
 			@Override
 			public void execute() throws Exception
 			{
 				item.setGraphName(getGraphName(item));
 
 				logger.debug("label: " + item.getLabel());
 				logger.debug("description: " + item.getDescription());
 				logger.debug("graphName" + item.getGraphName());
 
 				// to be able to drop a graph in Virtuoso, it has to be explicitly created before
 				createGraph(item.getGraphName());
 
 				GraphLoader graphLoader = new GraphLoader(EnumDatabaseInstance.CLEAN);
 				graphLoader.importGraph(item.getDefinition(), item.getGraphName());
 
 				// Call after working with RDF in case it fails
 				if (item.getId() == null) {
 					String query = "INSERT INTO " + TABLE_NAME 
 							+ " (label, description, graphName, authorId, definition) VALUES (?, ?, ?, ?, ?)";
 					jdbcUpdate(query, item.getLabel(), item.getDescription(), item.getGraphName(), item.getAuthorId(), 
 							item.getDefinition());
 					item.setId(getLastInsertId());
 				} else {
 					String query = "UPDATE " + TABLE_NAME 
 						+ " SET label = ?, description = ?, graphName = ?, authorId = ?, definition = ? WHERE id = ?";
 					jdbcUpdate(query, item.getLabel(), item.getDescription(), item.getGraphName(), item.getAuthorId(),
 							item.getDefinition(), item.getId());
 				}
 
 				generateRules(item);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * @param item
 	 * @return
 	 */
 	private String getGraphName(Ontology item) 
 	{
 		try
 		{
 			return ODCSInternal.ontologyGraphUriPrefix + URLEncoder.encode(item.getLabel(), Utils.DEFAULT_ENCODING);
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			logger.error(e.getMessage(), e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param graphName
 	 * @throws Exception
 	 */
 	private void createGraph(String graphName) throws Exception
 	{
 		String query = "SPARQL CREATE SILENT GRAPH ??";
 
 		Object[] params = { graphName };
 
 		jdbcUpdate(query, params);
 	}
 
 	/**
 	 * 
 	 * @param item
 	 * @throws Exception
 	 */
 	public void update(final Ontology item) throws Exception
 	{
 		executeInTransaction(new CodeSnippet()
 		{
 
 			@Override
 			public void execute() throws Exception
 			{
 				Ontology onto = loadRaw(item.getId());
 				deleteGraph(onto.getGraphName());
 				
 				item.setQaRulesGroup(getGroupId(QARulesGroupDao.TABLE_NAME, item.getId()));
 				item.setDnRulesGroup(getGroupId(DNRulesGroupDao.TABLE_NAME, item.getId()));
 				
 				save(item);
 			}
 		});
 	}
 
 	@Override
 	protected void deleteRaw(final Integer id) throws Exception
 	{
 		executeInTransaction(new CodeSnippet()
 		{
 			@Override
 			public void execute() throws Exception
 			{
 				Ontology onto = loadRaw(id);
 				deleteGraph(onto.getGraphName());
 				deleteMappings(id);
 				OntologyDao.super.deleteRaw(id);
 			}
 		});
 	}
 	
 	/**
 	 * 
 	 * @param graphName
 	 * @throws Exception
 	 */
 	private void deleteGraph(String graphName) throws Exception
 	{
		String query = "SPARQL DROP SILENT GRAPH ??";
 
 		Object[] params = { graphName };
 
 		jdbcUpdate(query, params);
 	}
 	
 	/**
 	 * 
 	 * @param ontology
 	 * @throws Exception
 	 */
 	private void generateRules(Ontology ontology) throws Exception {
 		String groupLabel = createGroupLabel(ontology.getLabel());
 		String ontologyGraphName = ontology.getGraphName();
 
 		logger.info("About to start generating Quality Assessment rules.");
 		generateQualityAssessmentRules(ontology.getId(), ontology.getQaRulesGroup(), groupLabel, ontologyGraphName, ontology.getAuthorId());
 		
 		logger.info("About to start generating Data Normalization rules.");
 		generateDataNormalizationRules(ontology.getId(), ontology.getDnRulesGroup(), groupLabel, ontologyGraphName, ontology.getAuthorId());
 	}
 	
 	/**
 	 * 
 	 * @param ontologyId
 	 * @param existingGroupId
 	 * @param groupLabel
 	 * @param ontologyGraphName
 	 * @param authorId
 	 * @throws Exception
 	 */
 	private void generateQualityAssessmentRules(final Integer ontologyId, final Integer existingGroupId,
 		final String groupLabel, final String ontologyGraphName, final Integer authorId) throws Exception
 	{
 		executeInTransaction(new CodeSnippet()
 		{
 			@Override
 			public void execute() throws Exception
 			{
 				try
 				{
 					JDBCConnectionCredentials connectionCredentials = ConfigLoader.getConfig().getBackendGroup()
 						.getCleanDBJDBCConnectionCredentials();
 					QualityAssessmentRulesModel rulesModel = new QualityAssessmentRulesModel(connectionCredentials);
 
 					QARuleDao ruleDao = OntologyDao.this.getLookupFactory().getDao(QARuleDao.class, true);
 
 					Collection<QualityAssessmentRule> rules = rulesModel.compileOntologyToRules(ontologyGraphName);
 
 					Integer groupId = existingGroupId;
 
 					// DROP OUTDATED RULES
 					if (groupId != null)
 					{
 						logger.info("Dropping old generated rules assossiated with the ontology.");
 						ruleDao.deleteByGroup(groupId);
 					}
 
 					// DO NOT CREATE NEW GROUP IF NO RULES WERE GENERATED
 					if (rules.size() == 0)
 						return;
 
 					// CREATE NEW GROUP IF NECESSARY
 					if (groupId == null)
 					{
 						logger.info("Creating new group for the ontology generated rules.");
 						groupId = ensureGroupPresence(QARulesGroupDao.TABLE_NAME, groupLabel, authorId);
 					}
 
 					createMapping(createMappingTableName(QARulesGroupDao.TABLE_NAME), groupId, ontologyId);
 
 					for (QualityAssessmentRule rule : rules)
 					{
 						rule.setGroupId(groupId);
 
 						ruleDao.save(new QARule(rule));
 					}
 
 					QARulesGroupDao ruleGroupDao = OntologyDao.this.getLookupFactory().getDao(QARulesGroupDao.class);
 					ruleGroupDao.commitChanges(groupId);
 				}
 				catch (Exception e)
 				{
 					logger.error(e.getMessage());
 					throw e;
 				}
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * @param ontologyId
 	 * @param existingGroupId
 	 * @param groupLabel
 	 * @param ontologyGraphName
 	 * @param authorId
 	 * @throws Exception
 	 */
 	private void generateDataNormalizationRules(final Integer ontologyId, final Integer existingGroupId,
 		final String groupLabel, final String ontologyGraphName, final Integer authorId) throws Exception
 	{
 		executeInTransaction(new CodeSnippet()
 		{
 			@Override
 			public void execute() throws Exception
 			{
 				try
 				{
 					JDBCConnectionCredentials connectionCredentials = ConfigLoader.getConfig().getBackendGroup()
 						.getCleanDBJDBCConnectionCredentials();
 					DataNormalizationRulesModel rulesModel = new DataNormalizationRulesModel(connectionCredentials);
 
 					DNRuleDao ruleDao = OntologyDao.this.getLookupFactory().getDao(DNRuleDao.class, true);
 					DNRuleComponentDao ruleComponentDao = OntologyDao.this.getLookupFactory().getDao(DNRuleComponentDao.class,
 						true);
 					DNRuleComponentTypeDao ruleComponentTypeDao = OntologyDao.this.getLookupFactory()
 						.getDao(DNRuleComponentTypeDao.class);
 
 					Collection<DataNormalizationRule> rules = rulesModel.compileOntologyToRules(ontologyGraphName);
 
 					Integer groupId = existingGroupId;
 
 					// DROP OUTDATED RULES
 					if (groupId != null)
 					{
 						logger.info("Dropping old generated rules assossiated with the ontology.");
 						ruleDao.deleteByGroup(groupId);
 					}
 
 					// DO NOT CREATE NEW GROUP IF NO RULES WERE GENERATED
 					if (rules.size() == 0)
 						return;
 
 					// CREATE NEW GROUP IF NECESSARY
 					if (groupId == null)
 					{
 						logger.info("Creating new group for the ontology generated rules.");
 						groupId = ensureGroupPresence(DNRulesGroupDao.TABLE_NAME, groupLabel, authorId);
 					}
 
 					createMapping(createMappingTableName(DNRulesGroupDao.TABLE_NAME), groupId, ontologyId);
 
 					for (DataNormalizationRule rule : rules)
 					{
 						rule.setGroupId(groupId);
 
 						Integer ruleId = ruleDao.saveAndGetKey(new DNRule(rule));
 
 						for (DataNormalizationRule.Component component : rule.getComponents())
 						{
 							DNRuleComponentType type = ruleComponentTypeDao.loadBy("label", component.getType().toString());
 							DNRuleComponent componentWithRuleId = new DNRuleComponent(null,
 								ruleId, type, component.getModification(), component.getDescription());
 
 							componentWithRuleId.setRuleId(ruleId);
 
 							ruleComponentDao.save(componentWithRuleId);
 						}
 					}
 
 					DNRulesGroupDao ruleGroupDao = OntologyDao.this.getLookupFactory().getDao(DNRulesGroupDao.class);
 					ruleGroupDao.commitChanges(groupId);
 				}
 				catch (Exception e)
 				{
 					logger.error(e.getMessage());
 					throw e;
 				}
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * @param tableName
 	 * @param groupLabel
 	 * @param authorId
 	 * @return
 	 * @throws Exception
 	 */
 	private Integer ensureGroupPresence(String tableName, String groupLabel, Integer authorId) throws Exception {
 		try
 		{
 			return getGroupId(tableName, groupLabel);
 		} catch (EmptyResultDataAccessException e) {
 			createRulesGroup(tableName, groupLabel, authorId);
 			
 			return getGroupId(tableName, groupLabel);
 		}
 	}
 
 	/**
 	 * 
 	 * @param ontologyLabel
 	 * @return
 	 */
 	private String createGroupLabel(String ontologyLabel)
 	{
 		return RULE_GROUP_PREFIX + ontologyLabel;
 	}
 
 	/**
 	 * 
 	 * @param tableName
 	 * @param groupLabel
 	 * @param authorId
 	 * @throws Exception
 	 */
 	private void createRulesGroup(String tableName, String groupLabel, Integer authorId) throws Exception
 	{
 		String query = "INSERT INTO " + tableName + " (label, authorId) VALUES (?, ?)";
 
 		Object[] params = { groupLabel, authorId };
 
 		logger.debug("groupName" + groupLabel);
 
 		jdbcUpdate(query, params);
 	}
 
 	/**
 	 * 
 	 * @param groupTableName
 	 * @return
 	 */
 	private String createMappingTableName(String groupTableName)
 	{
 		if (QARulesGroupDao.TABLE_NAME.equals(groupTableName))
 		{
 			return QA_MAPPING_TABLE_NAME;
 		}
 		else if (DNRulesGroupDao.TABLE_NAME.equals(groupTableName))
 		{
 			return DN_MAPPING_TABLE_NAME;
 		}
 		else
 		{
 			throw new AssertionError("Unexpected rules-group table name provided: " + groupTableName);
 		}
 	}
 
 	/**
 	 * 
 	 * @param tableName
 	 * @param groupLabel
 	 * @return
 	 */
 	private Integer getGroupId(String tableName, String groupLabel)
 	{
 		String query = "SELECT id FROM " + tableName + " WHERE label = ?";
 
 		Object[] params = { groupLabel };
 
 		return jdbcQueryForObject(query, params, Integer.class);
 	}
 	
 	/**
 	 * 
 	 * @param tableName
 	 * @param ontologyId
 	 * @return
 	 */
 	private Integer getGroupId(String tableName, Integer ontologyId)
 	{
 		String query = "SELECT groupId FROM " + TABLE_NAME_PREFIX + createMappingTableName(tableName) + " WHERE ontologyId = ?";
 
 		Object[] params =
 		{
 			ontologyId
 		};
 
 		try
 		{
 			return jdbcQueryForObject(query, params, Integer.class);
 		} catch (Exception e) {
 			logger.info("No related rule group found.");
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param tableName
 	 * @param groupId
 	 * @param ontologyId
 	 * @throws Exception
 	 */
 	private void createMapping(String tableName, Integer groupId, Integer ontologyId) throws Exception
 	{
 		String query = "INSERT REPLACING " + TABLE_NAME_PREFIX + tableName + " (groupId, ontologyId) VALUES (?, ?)";
 
 		Object[] params =
 		{
 			groupId,
 			ontologyId
 		};
 
 		logger.debug("groupId" + groupId);
 		logger.debug("ontologyId" + ontologyId);
 
 		jdbcUpdate(query, params);
 	}
 	
 	/**
 	 * 
 	 * @param ontologyId
 	 * @throws Exception
 	 */
 	private void deleteMappings(Integer ontologyId) throws Exception
 	{
 		String query = "SPARQL CLEAR GRAPH ??";
 		
 		Object[] params = { OntologyMappingDao.createGraphName(ontologyId) };
 		
 		jdbcUpdate(query, params);
 	}
 }
