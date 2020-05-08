 package cz.cuni.mff.odcleanstore.webfrontend.dao.oi;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.oi.OIOutput;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.CommittableDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForAuthorableEntity;
 
 @CommittableDao(OIOutputUncommittedDao.class)
 public class OIOutputDao extends DaoForAuthorableEntity<OIOutput>
 {
 	public static final String TABLE_NAME = TABLE_NAME_PREFIX + "OI_OUTPUTS";
 
 	private static final long serialVersionUID = 1L;
 	
 	private ParameterizedRowMapper<OIOutput> rowMapper;
 	
 	public OIOutputDao()
 	{
 		this.rowMapper = new OIOutputRowMapper();
 	}
 	
 	@Override
 	public String getTableName() 
 	{
 		return TABLE_NAME;
 	}
 	
 	protected String getRuleTableName()
 	{
 		return OIRuleDao.TABLE_NAME;
 	}
 	
 	@Override
 	protected ParameterizedRowMapper<OIOutput> getRowMapper() 
 	{
 		return this.rowMapper;
 	}
 	
 	@Override
 	protected String getSelectAndFromClause()
 	{
 		String query = "SELECT " +
 			"O.id as oid, ruleId, minConfidence, maxConfidence, fileName, " +
 			"OT.id as otid, OT.label as otlbl, OT.description as otdescr, " +
 			"FF.id as ffid, FF.label as fflbl, FF.description as ffdescr " +
 			"FROM " + getTableName() + " AS O " +
 			"JOIN " + OIOutputTypeDao.TABLE_NAME + " AS OT ON (O.outputTypeId = OT.id) " +
 			"LEFT OUTER JOIN " + OIFileFormatDao.TABLE_NAME + " AS FF ON (O.fileFormatId = FF.id) ";
 		return query;
 	}
 
 	@Override
 	public OIOutput load(Integer id)
 	{
 		return loadBy("O.id", id);
 	}
 	
 	@Override
 	protected void deleteRaw(Integer id) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot modify " + getTableName() + ", use uncommitted version table instead");
 	}
 	
 	@Override
 	public void save(OIOutput output)  throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot modify " + getTableName() + ", use uncommitted version table instead");
 	}
 	
 	public void update(OIOutput output) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot modify " + getTableName() + ", use uncommitted version table instead");
 	}
 
 	@Override
 	public int getAuthorId(Integer entityId)
 	{
 		String query = "SELECT g.authorId " +
 			"\n FROM " + getRuleTableName() + " AS r JOIN " + OIRulesGroupDao.TABLE_NAME + " AS g ON (g.id = r.groupId)" +
			"\n   JOIN " + TABLE_NAME + " AS o ON (o.ruleId = r.id) " +
 			"\n WHERE o.id = ?";
 		return jdbcQueryForInt(query, entityId);
 	}
 }
