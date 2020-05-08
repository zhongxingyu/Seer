 /**
  * 
  */
 package project.efg.util.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ObjectInputStream;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.springframework.jdbc.core.BatchPreparedStatementSetter;
 import org.springframework.jdbc.core.PreparedStatementSetter;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 
 import project.efg.templates.taxonPageTemplates.TaxonPageTemplates;
 import project.efg.util.factory.SpringFactory;
 import project.efg.util.interfaces.EFGImportConstants;
 import project.efg.util.interfaces.EFGQueueObjectInterface;
 
 /**
  * @author kasiedu
  * 
  */
 public abstract class TemplateModelHandler{
 	
 
 	protected String templateName;
 	public TemplateModelHandler() {
 		this.templateName = 
 			EFGImportConstants.TEMPLATE_TABLE.toLowerCase();
 		
 	}
 	/**
 	 *  Remove this key form the database
 	 *  @param datasourceName - Remove all keys created for this data source
 	 *  
 	 * @see project.efg.util.utils.TemplateModelHandler#removeFromDB(java.lang.String)
 	 */
 	public boolean removeFromDB(final String datasourceName) {
 		if(!doChecks(datasourceName)){
 			return false;
 		}
 		StringBuffer query = new StringBuffer();
 		query.append("DELETE FROM ");
 		query.append(this.templateName);
 		query.append(" WHERE ");
 		query.append(EFGImportConstants.DATASOURCE_NAME);
 		query.append("=");
 		query.append("?");
 		
 		try{
 			
 			  PreparedStatementSetter p = new PreparedStatementSetter(){
 					public void setValues(PreparedStatement ps) throws SQLException {
 						ps.setString(1,datasourceName.toLowerCase());
 					}
 				  };
 				  this.executePreparedStatement(query.toString(), p);
 		
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	/**
 	 *  Remove this key form the database
 	 *  @param datasourceName - Remove all keys created for this data source
 	 *  
 	 * @see project.efg.util.utils.TemplateModelHandler#removeFromDB(java.lang.String)
 	 */
 	public boolean removeTemplateFromDB(final String datasourceName, 
 			final String templateDisplayName) {
 		if(!doChecks(datasourceName)){
 			return false;
 		}
 		StringBuffer query = new StringBuffer();
 		query.append("DELETE FROM ");
 		query.append(this.templateName.toLowerCase());
 		query.append(" WHERE ");
 		query.append(EFGImportConstants.DATASOURCE_NAME);
 		query.append("=");
 		query.append("?");
 		query.append(" and ");
 		query.append(EFGImportConstants.DISPLAY_NAME);
 		query.append("=");
 		query.append("?");
 		
 		try{
 			  PreparedStatementSetter p = new PreparedStatementSetter(){
 					public void setValues(PreparedStatement ps) throws SQLException {
 						ps.setString(1,datasourceName.toLowerCase());
 						ps.setString(2, templateDisplayName);
 					}
 				  };
 				  this.executePreparedStatement(query.toString(), p);
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Update this key with this object
 	 * @param key
 	 * @param displayName
 	 * @return true if update was successful
 	 */
 	public boolean changeDisplayName(final String datasourceName, 
 			final String displayName){
 		
 		if(!doChecks(datasourceName)){
 			return false;
 		}
 		if(!doChecks(displayName)){
 			return false;
 		}
 		//StringBuffer updateStatement = new StringBuffer(
 				//this.getDisplayNameChangePreparedStatement(this.templateName.toLowerCase()));
 	
 		try {
 			
 			 /* PreparedStatementSetter p = new PreparedStatementSetter(){
 				public void setValues(PreparedStatement ps) throws SQLException {
 					ps.setString(1,displayName);
 				    ps.setString(2, datasourceName.toLowerCase());	
 				}
 			  };
 			  this.executePreparedStatement(updateStatement.toString(), p);
 			*/
 			  replaceKeysColumn(this.templateName.toLowerCase(),displayName, datasourceName);
 			  
 			return true;
 		
 		} catch (Exception e) {
 			//System.err.println("Error: " + e.getMessage());
 		}
 		return false;
 	}
 	/**
 	 * @param string
 	 * @param displayName
 	 * @param datasourceName
 	 */
 	private void replaceKeysColumn(
 			String tableName, 
 			String displayName, 
 			String datasourceName) {
 		StringBuffer query = new StringBuffer();
 	
 		
 		List list;
 		try {
 			query.append("SELECT DISTINCT ");
 			query.append(EFGImportConstants.DISPLAY_NAME);
 			query.append(" FROM ");
 			query.append(tableName);
 			query.append(" where ");
 			query.append(EFGImportConstants.DATASOURCE_NAME);
			query.append(" ='");
 			query.append(datasourceName);
			query.append("'");
 			
 			list = this.executeQueryForList(query.toString(),1);
 			for(int i = 0; i < list.size(); i ++){
 				
 				String oldDisplayName = 
 					((EFGQueueObjectInterface) list.get(i)).getObject(0);
 				StringBuffer replace = new StringBuffer();
 				//MySQL Query
 				replace.append("UPDATE ");
 				replace.append(tableName);
 				
 				replace.append(" SET EFGKey = replace (EFGKey,'");
 				replace.append(oldDisplayName);
 				replace.append("','");
 				replace.append(displayName);
 				replace.append("')");
 				this.executeStatement(replace.toString());
 				
 				replace = new StringBuffer();
 				//MySQL Query
 				replace.append("UPDATE ");
 				replace.append(tableName);
 				replace.append(" SET querystring = replace (querystring,'");
 				replace.append(oldDisplayName);
 				replace.append("','");
 				replace.append(displayName);
 				replace.append("')");
 				this.executeStatement(replace.toString());
 				
 				
 				//use a real sql query here instead of a mysql
 				//Update efg.efg_template_tables set displayName='Trees' 
 				//where displayName='PITreesFinalJennEditDecember';
 				replace = new StringBuffer();
 				//MySQL Query
 				replace.append("UPDATE ");
 				replace.append(tableName);
 				replace.append(" SET ");
 				replace.append( EFGImportConstants.DISPLAY_NAME);
 				replace.append(" = '");
 				replace.append(displayName);
 				replace.append( "' where ");
 				replace.append( EFGImportConstants.DISPLAY_NAME);
 				replace.append(" = '");
 				replace.append(oldDisplayName);
 				replace.append("'");
 				this.executeStatement(replace.toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Associate this key to this template in the database
 	 *@param key  A unique key
 	 *@param templateObject - The object associated with this key
 	 *@return true if successful
 	 * @see project.efg.util.utils.TemplateModelHandler#add2DB(java.lang.String, project.efg.util.utils.TemplateObject)
 	 */
 	public boolean add2DB(final String key, TemplateObject templateObject) {
 		if(!doChecks(key)){
 			
 			return false;
 		}
 		
 		if(templateObject == null){
 			
 			return false;
 		}
 		
 		EFGDisplayObject displayObject = templateObject.getDisplayObject();
 		
 		if(displayObject == null){
 			
 			return false;
 		}
 		final String datasourceName = displayObject.getDatasourceName();
 		if((datasourceName == null) || (datasourceName.trim().equals(""))){
 			
 			return false;
 		}
 		String guidx = templateObject.getGUID();
 		if(guidx == null){
 			guidx = "";
 		}
 		final String guid = guidx;
 		String tempName = templateObject.getTemplateName();
 		if(tempName == null){
 			tempName = "";
 		}
 		
 		String displayName = displayObject.getDisplayName();
 		if(displayName == null){
 			displayName = "";
 		}
 		createEFG2TemplatesTable();
 		return insertIntoDB(key,guid, 
 				displayName, 
 				datasourceName.toLowerCase(), 
 				tempName);
 		
 	}
 	/* (non-Javadoc)
 	 * @see project.efg.util.utils.TemplateModelHandler#getTemplateConfigFromDB(project.efg.util.utils.DBObject, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public TaxonPageTemplates getTemplateConfigFromDB(DBObject dbObject, 
 			String displayName, String datasourceName, String dbTableName) {
 	
 		if(displayName == null && datasourceName == null){
 			return null;
 		}
 		
 			if("".equals(displayName) && 
 					"".equals(datasourceName)){
 				return null;
 			}
 		
 	
 		StringBuffer queryString = new StringBuffer("Select ");
 		queryString.append(EFGImportConstants.TEMPLATE_OBJECT_FIELD);
 		queryString.append(" FROM ");
 		queryString.append(dbTableName);
 		queryString.append(" WHERE ");
 		if(displayName != null && !displayName.equals("")){
 			queryString.append(" DISPLAY_NAME='");
 			queryString.append(displayName);
 			queryString.append("'");
 		}
 		if(displayName != null && datasourceName != null){
 			if(!displayName.trim().equals("") && 
 					!datasourceName.trim().equals("")){
 				queryString.append(" and ");
 			}
 			
 		}
 		if(datasourceName != null && !datasourceName.equals("")){
 			queryString.append(" DS_DATA='");
 			queryString.append(datasourceName);
 			queryString.append("'");
 		}
 		try {
 			
 		SqlRowSet rs = this.executeQueryForRowSet(queryString.toString());
 		rs.next();
 		TaxonPageTemplates tps  = null;
 		Object binStream  =  rs.getObject(EFGImportConstants.TEMPLATE_OBJECT_FIELD);
 		if(binStream != null){
 			
 			byte[] byteArray = (byte[])binStream;
 		    final ByteArrayInputStream stream = new ByteArrayInputStream(byteArray);
 			
 		    ObjectInputStream objS = new ObjectInputStream(stream);
 		    Object obj = objS.readObject();
 		    tps  = (TaxonPageTemplates)obj;
 			
 		}
 		
 			return tps;
 		} catch (Exception e) {
 			
 		}
 		return null;
 	}
 
 	/**
 	 * @param updateStatement
 	 * @param p
 	 */
 	public abstract boolean executeBatchPreparedStatement(String updateStatement, BatchPreparedStatementSetter p);
 
 
 	/**
 	 * Associate this key to this template in the database
 	 *@param key  A unique key
 	 *@param templateObject - The object associated with this key
 	 *@return true if successful
 	 * @see project.efg.util.utils.TemplateModelHandler#add2DB(java.lang.String, project.efg.util.utils.TemplateObject)
 	 */
 	public boolean bacthUpdateTemplateObject(
 			String tableName, 
 			final String[] datasourceName,
 			final TaxonPageTemplates[] tps) {
 		if(datasourceName == null || datasourceName.length == 0 ||
 				tps == null || tps.length == 0){
 			return false;
 		}
 		if(tps.length != datasourceName.length){
 			return false;
 		}
 	
 		try{
 			StringBuffer updateStatement = new StringBuffer(
 					this.getUpdatePreparedStatement(tableName));
 			BatchPreparedStatementSetter p = 
 				new EFGBatchUpdatePreparedStatement(datasourceName,tps);
 			return this.executeBatchPreparedStatement(updateStatement.toString(), p);
 		} catch (Exception e) {
 			
 
 		}
 	
 		return false;
 	}
 	/**
 	 * Associate this key to this template in the database
 	 * @param key  A unique key
 	 * @param templateObject - The object associated with this key
 	 *@return true if successful
 	 * @see project.efg.util.utils.TemplateModelHandler#add2DB(java.lang.String, project.efg.util.utils.TemplateObject)
 	 */
 	public boolean updateTemplateObject(
 			String datasourceName, 
 			String tableName, 
 			TaxonPageTemplates tps) {
 		
 		if(!doChecks(datasourceName)){
 			return false;
 		}
 		
 		if(tps == null){
 			
 			return false;
 		}
 		
 	
 		if((datasourceName == null) || (datasourceName.trim().equals(""))){	
 			return false;
 		}
 		
 		StringBuffer updateString = new StringBuffer(
 				this.getUpdatePreparedStatement(tableName));
 		try {
 		
 		    PreparedStatementSetter p = 
 				new EFGUpdatePreparedStatement(datasourceName,tps);
 			return this.executePreparedStatement(updateString.toString(), p);
 		} catch (Exception e) {
 			
 
 		}
 		return false;
 	}
 
 	public boolean removeGuidFromTable(final String guid){
 
 		
 		StringBuffer query = new StringBuffer();
 		query.append("DELETE FROM ");
 		query.append(this.templateName.toLowerCase());
 		query.append(" WHERE ");
 		query.append(EFGImportConstants.GUID);
 		query.append("=");
 		query.append("?");
 	
 		
 		try {
 			
 			  PreparedStatementSetter p = new PreparedStatementSetter(){
 				public void setValues(PreparedStatement ps) throws SQLException {
 					ps.setString(1,guid);
 				}
 			  };
 			  this.executePreparedStatement(query.toString(), p);
 			
 			return true;
 	
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	/**
 	 * Return all of the keys in the table
 	 * @return
 	 */
 	public Hashtable getAll() {
 		
 		StringBuffer query = new StringBuffer();
 		query.append("SELECT ");
 		query.append(getHeaderQuery());
 		query.append(" FROM ");
 		query.append(this.templateName.toLowerCase());
 		query.append(" ORDER BY ");
 		query.append(EFGImportConstants.DISPLAY_NAME);
 		Hashtable table = new Hashtable();
 		try{
 			
 			List list =  this.executeQueryForList(query.toString(),6);
 			
 			for(int i = 0; i < list.size(); i ++){
 			
 				String key = 
 					((EFGQueueObjectInterface) list.get(i)).getObject(0);
 				String guid = 
 					((EFGQueueObjectInterface) list.get(i)).getObject(1);
 				String displayName = 
 					((EFGQueueObjectInterface) list.get(i)).getObject(2);
 				String dsName = 
 					((EFGQueueObjectInterface) list.get(i)).getObject(3);			
 				String tempName = 
 					((EFGQueueObjectInterface) list.get(i)).getObject(4);
 				
 				String queryString = 
 					((EFGQueueObjectInterface) list.get(i)).getObject(5);
 				
 				TemplateObject tempObject = SpringFactory.getTemplateObject();
 				tempObject.setGUID(guid);
 				tempObject.setTemplateName(tempName);
 				 
 				EFGDisplayObject dop = SpringFactory.getDisplayObject();
 				dop.setDatasourceName(dsName);
 				dop.setDisplayName(displayName);
 				tempObject.setDisplayObject(dop);
 				
 				if(queryString != null && !key.equals(queryString)){
 					table.put(queryString,tempObject);
 				}
 				else{
 					table.put(key,tempObject);
 				}
 		
 			}
 			return table;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	/**
 	 * Return a list of keys that contains te given key
 	 * 
 	 * @param key
 	 * @return - a list of keys that contains the given key.
 	 */
 	public List getKeys(String datasourceName) {
 		if(!doChecks(datasourceName)){
 			return null ;
 		}
 		StringBuffer query = new StringBuffer();
 		query.append("SELECT ");
 		query.append(EFGImportConstants.TEMPLATE_KEY);
 		query.append(" FROM ");
 		query.append(this.templateName);
 		query.append(" WHERE ");
 		query.append(EFGImportConstants.DATASOURCE_NAME);
 		query.append("=");
 		query.append("\"");
 		query.append(datasourceName.toLowerCase());
 		query.append("\"");
 		
 		
 		try{
 			return this.executeQueryForList(query.toString(),1);
 			
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 /**
  * Get the TemplateObject associated with this key
  * @param key
  * @return the TemplateObject associated with this key
  */
 	public TemplateObject getFromDB(String key) {
 		
 		if(!doChecks(key)){
 			return null ;
 		}
 		StringBuffer query = new StringBuffer();
 		query.append("SELECT ");
 		query.append(EFGImportConstants.GUID);
 		query.append(",");
 		query.append(EFGImportConstants.DISPLAY_NAME);
 		query.append(",");
 		query.append(EFGImportConstants.DATASOURCE_NAME);
 		query.append(",");
 		query.append(EFGImportConstants.TEMPLATE_NAME);
 		query.append(" FROM ");
 		query.append(this.templateName);
 		query.append(" WHERE ");
 		query.append(EFGImportConstants.TEMPLATE_KEY);
 		query.append("=");
 		query.append("\"");
 		query.append(key);
 		query.append("\"");
 		
 		
 		
 		try{
 			List list =  this.executeQueryForList(query.toString(),4);
 			if(list.size() > 0){
 				String guid = ((EFGQueueObjectInterface) list.get(0)).getObject(0);
 				String displayName = ((EFGQueueObjectInterface) list.get(1)).getObject(0);
 				String dsName = ((EFGQueueObjectInterface) list.get(2)).getObject(0);
 				
 				String tempName = ((EFGQueueObjectInterface) list.get(3)).getObject(0);
 				 TemplateObject tempObject = SpringFactory.getTemplateObject();
 				 tempObject.setGUID(guid);
 				 tempObject.setTemplateName(tempName);
 				 
 				 EFGDisplayObject dop = SpringFactory.getDisplayObject();
 				 dop.setDatasourceName(dsName);
 				 dop.setDisplayName(displayName);
 				 tempObject.setDisplayObject(dop);
 				 return tempObject;
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	public abstract SqlRowSet executeQueryForRowSet(String queryString) throws Exception;
 	public abstract boolean executePreparedStatement(
 String updateStatement,
 PreparedStatementSetter p) throws Exception;
 	public abstract int executeStatement(String query)throws Exception;
 	
 
 
 	public abstract java.util.List executeQueryForList(String query, 
 			int numberOfColumns)throws Exception;
 	
 	/**
 	 * @param key
 	 * @param templateObject
 	 * @param dbObject
 	 * @return
 	 */
 	public boolean updateTemplateTable(
 			String skey, TemplateObject to) {
 		 	
 		final String guid = to.getGUID();
 		final String templateName = to.getTemplateName();
 			final String textKey = skey;
 			if(skey.length() > 250){//we  want to fit this into a database
 				skey = skey.substring(0,250);
 			}
 			else{
 				skey = skey;
 			}
 			final String key = skey;
 			StringBuffer query = new StringBuffer();
 			
 			query.append("UPDATE ");
 			query.append(this.templateName.toLowerCase()); 
 			query.append(" SET ");
 			query.append(EFGImportConstants.TEMPLATE_KEY);
 			query.append("=");
 			query.append("?,");
 			query.append(EFGImportConstants.TEMPLATE_NAME);
 			query.append("=");
 			query.append("?,");
 			query.append(EFGImportConstants.QUERY_STR);
 			query.append("=? WHERE ");
 			query.append(EFGImportConstants.GUID);
 			query.append("=");
 			query.append("?");
 			
 			
 		
 		
 			try{
 				  PreparedStatementSetter p = new PreparedStatementSetter(){
 						public void setValues(PreparedStatement ps) throws SQLException {
 							ps.setString(1,key);
 						    ps.setString(2,templateName);	
 						    ps.setString(3,textKey);
 						    ps.setString(4,guid);
 						}
 					  };
 					  this.executePreparedStatement(query.toString(), p);
 				
 				return true;
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 			return false;
 		
 	}	
 	private boolean insertIntoDB(String skey, final String guid, 
 			final String displayName, 
 			final String datasourceName, 
 			final String templateName){
 		
 		
 		final String textKey = skey;
 		if(skey.length() > 250){//we  want to fit this into a database
 			skey = skey.substring(0,250);
 		}
 		else{
 			skey = skey;
 		}
 		final String key = skey;
 		StringBuffer query = new StringBuffer();
 		query.append("INSERT INTO ");
 		query.append(this.templateName.toLowerCase()); 
 		query.append(" "); 
 		query.append("( ");
 		query.append(getHeaderQuery());
 		query.append(")");
 		query.append(" VALUES(");
 		query.append("?");
 		query.append(",");
 		query.append("?");
 		query.append(",");
 		query.append("?");
 		query.append(",");
 		query.append("?");
 		query.append(",");
 		query.append("?");
 		query.append(",");
 		query.append("?");
 		query.append(")");
 	
 		try{
 			  PreparedStatementSetter p = new PreparedStatementSetter(){
 					public void setValues(PreparedStatement ps) throws SQLException {
 						
 						ps.setString(1,key);
 					    ps.setString(2,guid);	
 					    ps.setString(3,displayName);
 					    ps.setString(4,datasourceName);
 					    ps.setString(5,templateName);
 					    
 					    ps.setString(6,textKey);
 					}
 				  };
 				  this.executePreparedStatement(query.toString(), p);
 			
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	private boolean createEFG2TemplatesTable() {
 			
 			if((this.templateName == null) || 
 					(this.templateName.trim().equals(""))){
 				
 				return false;
 			}
 				
 				StringBuffer query = new StringBuffer();
 				
 				
 				query.append("CREATE TABLE IF NOT EXISTS ");
 				query.append(templateName.toLowerCase());
 			
 				query.append("( ");
 				query.append(EFGImportConstants.TEMPLATE_KEY);
 				query.append(" VARCHAR(255) not null,");
 				query.append(EFGImportConstants.GUID);
 				query.append(" VARCHAR (255), ");
 				query.append(EFGImportConstants.DISPLAY_NAME);
 				query.append(" VARCHAR(255), ");
 				query.append(EFGImportConstants.DATASOURCE_NAME);
 				query.append(" VARCHAR(255), ");
 				query.append(EFGImportConstants.TEMPLATE_NAME);
 				query.append(" VARCHAR(255), "); 
 				query.append(EFGImportConstants.QUERY_STR);
 				query.append(" TEXT "); 
 				query.append(")");
 				try{
 					this.executeStatement(query.toString());
 					return true;
 				}
 				catch (Exception e) {
 					
 				}
 				return true;
 	}
 	/*private final String getDisplayNameChangePreparedStatement(String tableName){
 		
 		
 			
 		
 		StringBuffer query = new StringBuffer();
 		query.append("UPDATE ");
 		query.append(tableName);
 		query.append(" SET ");
 		query.append(EFGImportConstants.DISPLAY_NAME);
 		query.append("=");
 		query.append("? WHERE ");
 		query.append(EFGImportConstants.DATASOURCE_NAME);
 		query.append("=");
 		query.append("?");
 	
 		return query.toString();
 	}*/
 	/**
 	 * @return
 	 */
 	private static String getHeaderQuery() {
 		
 		StringBuffer query = new StringBuffer();
 		
 		query.append(EFGImportConstants.TEMPLATE_KEY);
 		query.append(",");
 		query.append(EFGImportConstants.GUID);
 		query.append(",");
 		query.append(EFGImportConstants.DISPLAY_NAME);
 		query.append(",");
 		query.append(EFGImportConstants.DATASOURCE_NAME);
 		query.append(",");
 		query.append(EFGImportConstants.TEMPLATE_NAME);
 		query.append(",");
 		query.append(EFGImportConstants.QUERY_STR);
 		return query.toString();
 	}
 	/**
 	 * @return true if the key is a valid key
 	 */
 	private final boolean doChecks(String key) {
 		if((this.templateName == null) || 
 				(this.templateName.trim().equals(""))){
 			
 			return false;
 		}
 		if((key == null) || (key.trim().equals(""))){ 
 			
 			return false;
 		}
 		return true;
 	}
 	private final String getUpdatePreparedStatement(String tableName){
 		StringBuffer updateString = new StringBuffer("UPDATE ");
 		updateString.append(tableName);
 		updateString.append(" SET ");
 		updateString.append(EFGImportConstants.TEMPLATE_OBJECT_FIELD);
 		updateString.append("=");
 		updateString.append("? WHERE ");
 		updateString.append(EFGImportConstants.DS_DATA_COL);
 		updateString.append("=?");
 		return updateString.toString();
 	}
 	
 }
