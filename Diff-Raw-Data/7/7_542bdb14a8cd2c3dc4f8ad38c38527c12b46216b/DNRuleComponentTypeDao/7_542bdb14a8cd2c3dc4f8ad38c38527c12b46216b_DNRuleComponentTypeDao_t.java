 package cz.cuni.mff.odcleanstore.webfrontend.dao.dn;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRuleComponentType;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 
 /**
  * 
  * @author Dusan
  *
  */
 public class DNRuleComponentTypeDao extends DaoForEntityWithSurrogateKey<DNRuleComponentType>
 {
 	public static final String TABLE_NAME = TABLE_NAME_PREFIX + "DN_RULE_COMPONENT_TYPES";
 
 	private static final long serialVersionUID = 1L;
 	
 	private ParameterizedRowMapper<DNRuleComponentType> rowMapper;
 	
 	public DNRuleComponentTypeDao()
 	{
 		this.rowMapper = new DNRuleComponentTypeRowMapper();
 	}
 	
 	@Override
 	public String getTableName() 
 	{
 		return TABLE_NAME;
 	}
 
 	@Override
 	protected ParameterizedRowMapper<DNRuleComponentType> getRowMapper() 
 	{
 		return rowMapper;
 	}
 	
 	@Override
 	protected void deleteRaw(Integer item) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot delete rows from table: " + getTableName() + "."
 		);
 	}
	
	@Override
	public DNRuleComponentType loadBy(String columnName, Object value)
	{
		// make loadBy() public
		return super.loadBy(columnName, value);
	}
 }
