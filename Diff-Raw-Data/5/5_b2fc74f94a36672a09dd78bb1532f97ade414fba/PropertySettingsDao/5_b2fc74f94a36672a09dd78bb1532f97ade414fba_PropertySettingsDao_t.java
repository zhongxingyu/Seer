 package cz.cuni.mff.odcleanstore.webfrontend.dao.cr;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.cr.PropertySettings;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.QueryCriteria;
 
 import java.util.List;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 public class PropertySettingsDao extends DaoForEntityWithSurrogateKey<PropertySettings>
 {	
 	public static final String TABLE_NAME = TABLE_NAME_PREFIX + "CR_PROPERTIES";
 
 	private static final long serialVersionUID = 1L;
 	
 	private ParameterizedRowMapper<PropertySettings> rowMapper;
 	
 	public PropertySettingsDao()
 	{
 		this.rowMapper = new PropertySettingsRowMapper();
 	}
 	
 	@Override
 	public String getTableName() 
 	{
 		return TABLE_NAME;
 	}
 
 	@Override
 	protected ParameterizedRowMapper<PropertySettings> getRowMapper() 
 	{
 		return rowMapper;
 	}
 	
 	@Override
 	public void save(PropertySettings item) 
 	{
 		String query = 
 			"INSERT INTO " + TABLE_NAME + " (property, multivalueTypeId, aggregationTypeId) " +
 			"VALUES (?, ?, ?)";
 		
 		Object[] arguments =
 		{
 			item.getProperty(),
 			item.getMultivalueType().getId(),
 			item.getAggregationType().getId()
 		};
 		
 		logger.debug("property: " + item.getProperty());
 		logger.debug("multivalueTypeId: " + item.getMultivalueType().getId());
 		logger.debug("aggregationTypeId: " + item.getAggregationType().getId());
 		
 		getJdbcTemplate().update(query, arguments);
 	}
 	
 	@Override
 	public void update(PropertySettings item)
 	{
 		String query = 
 			"UPDATE " + TABLE_NAME + " SET property = ?, multivalueTypeId = ?, aggregationTypeId = ? " +
 			"WHERE id = ?";
 		
 		Object[] arguments =
 		{
 			item.getProperty(),
 			item.getMultivalueType().getId(),
 			item.getAggregationType().getId(),
 			item.getId()
 		};
 		
 		logger.debug("property: " + item.getProperty());
 		logger.debug("multivalueTypeId: " + item.getMultivalueType().getId());
 		logger.debug("aggregationTypeId: " + item.getAggregationType().getId());
 		logger.debug("id: " + item.getId());
 		
 		getJdbcTemplate().update(query, arguments);
 	}
 
 	@Override
 	public List<PropertySettings> loadAllBy(QueryCriteria criteria)
 	{
 		String query = 
 			"SELECT " +
 			"	P.id as id, P.property as property, " +
 			"	AT.id as atid, AT.label as atlbl, AT.description as atdescr, " +
 			"	MT.id as mtid, MT.label as mtlbl, MT.description as mtdescr " +
 			"FROM " + PropertySettingsDao.TABLE_NAME + " as P " +
 			"JOIN " + AggregationTypeDao.TABLE_NAME + " as AT " +
 			"ON P.aggregationTypeId = AT.id " +
 			"JOIN " + MultivalueTypeDao.TABLE_NAME + " as MT " +
 			"ON P.multivalueTypeId = MT.id " +
 			criteria.buildWhereClause() +
 			criteria.buildOrderByClause();
 		
 		Object[] params = criteria.buildWhereClauseParams();
 		
 		return getJdbcTemplate().query(query, params, getRowMapper());
 	}
 
 	@Override
 	public PropertySettings load(Long id)
 	{
 		String query = 
			"SELECT " +
			"	P.id as id, P.property as property, " +
			"	AT.id as atid, AT.label as atlbl, AT.description as atdescr, " +
			"	MT.id as mtid, MT.label as mtlbl, MT.description as mtdescr " +
 			"FROM " + PropertySettingsDao.TABLE_NAME + " as P " +
 			"JOIN " + AggregationTypeDao.TABLE_NAME + " as AT " +
 			"ON P.aggregationTypeId = AT.id " +
 			"JOIN " + MultivalueTypeDao.TABLE_NAME + " as MT " +
 			"ON P.multivalueTypeId = MT.id " +
 			"WHERE P.id = ?";
 		
 		Object[] params = { id };
 			
 		return getJdbcTemplate().queryForObject(query, params, getRowMapper());
 	}
 }
