 
 package edu.wustl.query.util.querysuite;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.query.AbstractQuery;
 import edu.wustl.common.query.factory.AbstractQueryFactory;
 import edu.wustl.common.query.factory.AbstractQueryManagerFactory;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.exceptions.SqlException;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.query.querymanager.AbstractQueryManager;
 import edu.wustl.query.querymanager.Count;
 import edu.wustl.query.util.global.Constants;
 
 /**
  *   This class is default implementation of AbstractQueryUIManager.
  */
 public abstract class QueryUIManager extends AbstractQueryUIManager
 {
 
 	private AbstractQuery abstractQuery;
 	protected IQuery query;
 	public QueryUIManager()
 	{
 	}
 
 	/**
 	 * @param request
 	 * @param query
 	 */
 	public QueryUIManager(HttpServletRequest request, IQuery query) throws QueryModuleException
 	{
 
 		HttpSession session = request.getSession();
 		SessionDataBean sessionDataBean = ((SessionDataBean) session
 				.getAttribute(Constants.SESSION_DATA));
 		long user_id = sessionDataBean.getUserId();
 		this.abstractQuery = AbstractQueryFactory.getDefaultAbstractQuery();
 		this.abstractQuery.setQuery(query);
 		this.abstractQuery.setUserId(user_id);
 		isSavedQuery = Boolean.valueOf((String) session.getAttribute(Constants.IS_SAVED_QUERY));
 		queryDetailsObj = new QueryDetails(session);
 	}
 
 	/**
 	 * This method Processes the Query by calling execute method of AbstractQueryManager.
 	 * 
 	 * @throws QueryModuleException
 	 */
 	public int processQuery() throws QueryModuleException
 	{
 		AbstractQueryManager queryManager = AbstractQueryManagerFactory
 				.getDefaultAbstractQueryManager();
 		QueryModuleException queryModExp;
 		try
 		{
 			queryManager.execute(abstractQuery);
 
 		}
 		catch (MultipleRootsException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.MULTIPLE_ROOT);
 			throw queryModExp;
 		}
 		catch (SqlException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.SQL_EXCEPTION);
 			throw queryModExp;
 		}
 		return 0;
 	}
 
 	@Override
	public Count getCount(int query_execution_id) throws QueryModuleException
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * This method inserts defined queries.
 	 * @throws QueryModuleException
 	 */
 	public void insertDefinedQueries(SessionDataBean sessionDataBean) throws QueryModuleException
 	{
 		QueryModuleException queryModExp;
 		//inserts Defined Query
 		DefinedQueryUtil definedQueryUtil = new DefinedQueryUtil();
 		try
 		{
 			definedQueryUtil.insertQuery(query, sessionDataBean, false);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.DAO_EXCEPTION);
 			throw queryModExp;
 		}
 		catch (BizLogicException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.DAO_EXCEPTION);
 			throw queryModExp;
 		}
 		catch (DAOException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.DAO_EXCEPTION);
 			throw queryModExp;
 		}
 	}
 
 	/**
 	 * This method updates the defined queries in database.
 	 * @throws QueryModuleException
 	 */
 	public void updateDefinedQueries(SessionDataBean sessionDataBean) throws QueryModuleException
 	{
 		QueryModuleException queryModExp;
 		//inserts Defined Query
 		DefinedQueryUtil definedQueryUtil = new DefinedQueryUtil();
 		try
 		{
 			definedQueryUtil.updateQuery(query,sessionDataBean, false);
 		}
 		catch (UserNotAuthorizedException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.DAO_EXCEPTION);
 			throw queryModExp;
 		}
 		catch (BizLogicException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.DAO_EXCEPTION);
 			throw queryModExp;
 		}
 		catch (DAOException e)
 		{
 			queryModExp = new QueryModuleException(e.getMessage(), QueryModuleError.DAO_EXCEPTION);
 			throw queryModExp;
 		}
 	}
 
 	/**
 	 * Method to get object of AbstractQuery
 	 * @return abstractQuery object of AbstractQuery
 	 */
 	public AbstractQuery getAbstractQuery()
 	{
 		return abstractQuery;
 	}
 
 	/**
 	 * Method to set object of AbstractQuery
 	 * @param abstractQuery object of AbstractQuery
 	 */
 	public void setAbstractQuery(AbstractQuery abstractQuery)
 	{
 		this.abstractQuery = abstractQuery;
 	}
}
