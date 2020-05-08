 /*
  * $Id$
  * $Revision$
  * $Date$
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package wicket.contrib.data.model.hibernate;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import wicket.WicketRuntimeException;
 import wicket.contrib.data.model.ISelectCountAndListAction;
 
 /**
  * Implements a count and list selection that can be used for eg a pageable list. To work,
  * this list needs a (named) select- and a count query with the same number and types of
  * parameters. The queries will be loaded from the Hibernate configuration. Clients should
  * override this class in order to provide specific parameters to the queries.
  * <p>
  * As these queries are static (from the configuration files), you could best use a
  * pattern like this:
  * 
  * <pre>
  * 
  *   &lt;query name=&quot;cdapp.model.SearchCD&quot;&gt;
  *       &lt;![CDATA[
  * 		  SELECT cd FROM cdapp.model.CD as cd
  *           WHERE (:title is null or upper(cd.title) like :title)
  *           AND (:performers is null or upper(cd.performers) like :performers)
  *           AND (:year = -1 or cd.year = :year)
  *       ]]&gt;
  *   &lt;/query&gt;
  *  
  * </pre>
  * 
  * Take for example: 'WHERE (:title is null or upper(cd.title) like :title)'; if parameter
  * 'title' is set to null (note that you MUST explicitly set it to null with Hibernate),
  * it will be discarded on query execution time. Hence, with this method, you can use
  * static queries while still having flexible where clauses.
  * </p>
  * @author Eelco Hillenius
  */
 public class HibernateCountAndListAction implements ISelectCountAndListAction,
 		Serializable
 {
 	/** delegate that provides instances of {@link org.hibernate.Session}. */
 	private final IHibernateSessionDelegate sessionDelegate;
 
 	/** list for ordering columns. */
 	private List orderColumns = new ArrayList();
 
 	/** name of select query. */
 	private final String queryName;
 
 	/** name of count query. */
 	private final String countQueryName;
 
 	/**
 	 * Construct.
 	 * @param sessionDelegate delegate that provides instances of
 	 *            {@link org.hibernate.Session}.
 	 */
 	public HibernateCountAndListAction(IHibernateSessionDelegate sessionDelegate)
 	{
 		this(null, null, sessionDelegate);
 	}
 
 	/**
 	 * Construct.
 	 * @param queryName name of select query
 	 * @param countQueryName name of count query
 	 * @param sessionDelegate delegate that provides instances of
 	 *            {@link org.hibernate.Session}.
 	 */
 	public HibernateCountAndListAction(String queryName, String countQueryName,
 			IHibernateSessionDelegate sessionDelegate)
 	{
 		this.sessionDelegate = sessionDelegate;
 		this.queryName = queryName;
 		this.countQueryName = countQueryName;
 	}
 
 	/**
 	 * @see wicket.contrib.data.model.ISelectListAction#execute(java.lang.Object, int,
 	 *      int)
 	 */
 	public List execute(Object queryObject, int startFromRow, int numberOfRows)
 	{
 		return load(startFromRow, numberOfRows, queryObject);
 	}
 
 	/**
 	 * @see wicket.contrib.data.model.ISelectObjectAction#execute(java.lang.Object)
 	 */
 	public Object execute(Object queryObject)
 	{
 		return getCount(queryObject);
 	}
 
 	/**
 	 * Gets/ executes the count.
 	 * @param queryObject the query object (possibly null)
 	 * @return the count
 	 */
 	protected Integer getCount(Object queryObject)
 	{
 		try
 		{
 			Query query = getCountQuery(sessionDelegate.getSession());
 			setParameters(query, queryObject);
 			List countResult = query.list();
 			return (Integer) countResult.get(0);
 		}
 		catch (HibernateException e)
 		{
 			throw new WicketRuntimeException(e);
 		}
 	}
 
 	/**
 	 * Gets the result list.
 	 * @param startFromRow row to start from
 	 * @param numberOfRows number of rows to load
 	 * @param queryObject the query object (possibly null)
 	 * @return the (partial) list
 	 */
 	protected List load(int startFromRow, int numberOfRows, Object queryObject)
 	{
 		try
 		{
 			Query query = getQuery(sessionDelegate.getSession());
 			setParameters(query, queryObject);
 			query.setFirstResult(startFromRow);
 			query.setMaxResults(numberOfRows);
 			return query.list();
 		}
 		catch (HibernateException e)
 		{
 			throw new WicketRuntimeException(e);
 		}
 	}
 
 	/**
 	 * Sets the query parameters. This method can be provided by clients in order to
 	 * provide specific parameters for the queries. Beware that this method will be called
 	 * for both the count query and the select query, so the number and type of parameters
 	 * of both queries should match. Does nothing by default and is thus only useable for
 	 * queries without parameters.
 	 * @param query the query to set the parameters on
 	 * @param queryObject the query object (possibly null)
 	 * @throws HibernateException
 	 */
 	protected void setParameters(Query query, Object queryObject)
 			throws HibernateException
 	{
 	}
 
 	/**
 	 * Gets the selection query.
 	 * @param session the hibernate session
 	 * @return the selection query
 	 */
 	protected Query getQuery(Session session)
 	{
 		Query query;
 		try
 		{
 			query = session.getNamedQuery(queryName);
 			List orderColumns = getOrderColumns();
 			if (!orderColumns.isEmpty())
 			{
 				StringBuffer b = new StringBuffer(query.getQueryString());
 				b.append(" ORDER BY ");
 				for (Iterator i = orderColumns.iterator(); i.hasNext();)
 				{
 					Order order = (Order) i.next();
 					String direction = (order.ascending) ? "ASC" : "DESC";
					b.append("upper(").append(order.field).append(") ").append(
 							direction);
 					if (i.hasNext())
 					{
 						b.append(", ");
 					}
 				}
 				query = session.createQuery(b.toString());
 			}
 		}
 		catch (HibernateException e)
 		{
 			throw new WicketRuntimeException(e);
 		}
 		return query;
 	}
 
 	/**
 	 * Gets the count query.
 	 * @param session the hibernate session
 	 * @return the count query
 	 */
 	protected Query getCountQuery(Session session)
 	{
 		Query query;
 		try
 		{
 			query = session.getNamedQuery(countQueryName);
 		}
 		catch (HibernateException e)
 		{
 			throw new WicketRuntimeException(e);
 		}
 		return query;
 	}
 
 	/**
 	 * Get queryName.
 	 * @return queryName.
 	 */
 	public final String getQueryName()
 	{
 		return queryName;
 	}
 
 	/**
 	 * Get countQueryName.
 	 * @return countQueryName.
 	 */
 	public final String getCountQueryName()
 	{
 		return countQueryName;
 	}
 
 	/**
 	 * Add ordering for select query. Appends the given ordering at the front of the
 	 * ordering list and removes the current if found
 	 * @param colName column name
 	 */
 	public void addOrdering(String colName)
 	{
 		boolean ascending = true;
 		Order order = new Order(colName);
 		int i = orderColumns.indexOf(order);
 		if (i != -1)
 		{
 			Order current = (Order) orderColumns.remove(i); // remove current
 			ascending = !current.ascending; // switch
 		}
 		order.ascending = ascending;
 		orderColumns.add(0, order);
 	}
 
 	/**
 	 * Remove ordering for select query.
 	 * @param colName column name
 	 */
 	public void removeOrdering(String colName)
 	{
 		Order order = new Order(colName);
 		int i = orderColumns.indexOf(order);
 		if (i != -1)
 		{
 			Order current = (Order) orderColumns.remove(i); // remove
 		}
 	}
 
 	/**
 	 * Gets orderColumns.
 	 * @return orderColumns.
 	 */
 	protected final List getOrderColumns()
 	{
 		return orderColumns;
 	}
 
 	/**
 	 * Sets orderColumns.
 	 * @param orderColumns orderColumns.
 	 */
 	protected final void setOrderColumns(List orderColumns)
 	{
 		this.orderColumns = orderColumns;
 	}
 
 	/** Wrapper class for ordering. */
 	protected static class Order implements Serializable
 	{
 		/** order by field. */
 		String field;
 
 		/** ascending (true) or descending (false). */
 		boolean ascending;
 
 		/**
 		 * Construct.
 		 * @param field order by field
 		 */
 		public Order(String field)
 		{
 			this.field = field;
 		}
 
 		/** @see java.lang.Object#equals(java.lang.Object) */
 		public boolean equals(Object obj)
 		{
 			if (!(obj instanceof Order))
 				return false;
 			Order that = (Order) obj;
 			return this.field.equals(that.field);
 		}
 	}
 }
