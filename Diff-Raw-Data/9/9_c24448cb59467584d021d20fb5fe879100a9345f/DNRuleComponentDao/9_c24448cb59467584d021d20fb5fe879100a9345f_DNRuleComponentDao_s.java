 package cz.cuni.mff.odcleanstore.webfrontend.dao.dn;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRuleComponent;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.CommittableDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForAuthorableEntity;
 
 /**
  * 
  * @author Dusan
  *
  */
 @CommittableDao(DNRuleComponentUncommittedDao.class)
 public class DNRuleComponentDao extends DaoForAuthorableEntity<DNRuleComponent>
 {
 	public static final String TABLE_NAME = TABLE_NAME_PREFIX + "DN_RULE_COMPONENTS";
 
 	private static final long serialVersionUID = 1L;
 	
 	private DNRuleComponentRowMapper rowMapper;
 	
 	public DNRuleComponentDao()
 	{
 		rowMapper = new DNRuleComponentRowMapper();
 	}
 	
 	@Override
 	public String getTableName() 
 	{
 		return TABLE_NAME;
 	}
 	
 	protected String getRuleTableName()
 	{
 		return DNRuleDao.TABLE_NAME;
 	}
 
 	@Override
 	protected ParameterizedRowMapper<DNRuleComponent> getRowMapper() 
 	{
 		return rowMapper;
 	}
 	
 	@Override
 	protected String getSelectAndFromClause()
 	{
 		String query = 
 			"SELECT C.id as id, ruleId, modification, C.description as description, " +
 			"CT.id as typeId, CT.label as typeLbl, CT.description as typeDescr " +
 			"FROM " + getTableName() + " as C " +
 			"JOIN " + DNRuleComponentTypeDao.TABLE_NAME + " as CT " +
 			"ON CT.id = C.typeId ";
 		return query;
 	}
 	
 	@Override
 	public DNRuleComponent load(Integer id)
 	{
 		return loadBy("C.id", id);
 	}
 	
 	@Override
 	protected void deleteRaw(Integer id) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot modify " + getTableName() + ", use uncommitted version table instead");
 	}
 	
 	@Override
 	public void save(DNRuleComponent item) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot modify " + getTableName() + ", use uncommitted version table instead");
 	}
 
 	public void update(DNRuleComponent item) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot modify " + getTableName() + ", use uncommitted version table instead");
 	}
 	
 	@Override
 	public int getAuthorId(Integer entityId)
 	{
 		String query = "SELECT g.authorId " +
 				"\n FROM " + getRuleTableName() + " AS r JOIN " + DNRulesGroupDao.TABLE_NAME + " AS g ON (g.id = r.groupId)" +
				"\n   JOIN " + TABLE_NAME + " AS c ON (c.ruleId = r.id)" +
 				"\n WHERE c.id = ?";
 		return jdbcQueryForInt(query, entityId);
 	}
 }
