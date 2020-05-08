 package cz.cuni.mff.odcleanstore.webfrontend.core.models;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.model.IModel;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.EntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoSortableDataProvidable;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.QueryCriteria;
 
 /**
  * TODO: change doc to adhere to the fact that the following is no longer
  * true (e.g. the data are not filtered by a single column anymore)
  * 
  * A data provider to provide a collection of all registered instances of the 
  * given BO, which are associated with BO with the given id through the column
  * given by name. The collection is sorted by values in a column given by name.
  * 
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  * @param <BO>
  */
 public class DependentSortableDataProvider<BO extends EntityWithSurrogateKey> extends SortableDataProvider<BO>
 {
 	private static final long serialVersionUID = 1L;
 	
 	private DaoSortableDataProvidable<BO> dao;
 	private List<BO> data;
 	private QueryCriteria criteria;
 	
 	/**
 	 * 
 	 * @param dao
 	 * @param constraints
 	 */
 	public DependentSortableDataProvider(DaoSortableDataProvidable<BO> dao, Object... constraints)
 	{
 		this(dao, "id", constraints);
 	}
 	
 	/**
 	 * 
 	 * @param dao
 	 * @param constraints
 	 */
 	public DependentSortableDataProvider(DaoForEntityWithSurrogateKey<BO> dao, String defaultSortColumnName, SortOrder defaultSortOrder)
 	{
 		this(dao, "id");
 		setSort(defaultSortColumnName, defaultSortOrder);
 	}
 	
 	/**
 	 * 
 	 * @param dao
 	 * @param defaultSortColumnName
 	 * @param constraints
 	 */
 	public DependentSortableDataProvider(DaoSortableDataProvidable<BO> dao, String defaultSortColumnName,
 		Object... constraints)
 	{
 		setSort(defaultSortColumnName, SortOrder.ASCENDING);
 		
 		this.dao = dao;
 
 		QueryCriteria criteria = new QueryCriteria();
 		
 		for (int i = 0; i < constraints.length; i += 2) {
 			criteria.addWhereClause((String)constraints[i], constraints[i + 1]);
 		}
 		
 		this.criteria = criteria;
 	}
 	
 	/**
 	 * 
 	 * @param dao
 	 * @param defaultSortColumnName
 	 * @param criteria
 	 */
 	public DependentSortableDataProvider(DaoSortableDataProvidable<BO> dao, String defaultSortColumnName,
 			QueryCriteria criteria)
 	{
 		setSort(defaultSortColumnName, SortOrder.ASCENDING);
 			
 		this.dao = dao;
 		this.criteria = criteria;
 	}
 	
 	/**
 	 * Loads the represented collection in a lazy way, sorted by the values
 	 * in a single column.
 	 * 
 	 * @return
 	 */
 	private List<BO> getData()
 	{
 		if (data == null)
 		{
 			SortParam sortParam = getSort();
 			
 			QueryCriteria criteria = (QueryCriteria)this.criteria.clone();
 			criteria.addOrderByClause(sortParam.getProperty(), sortParam.isAscending());
 			
 			data = dao.loadAllBy(criteria);
 		}
 		
 		return data;
 	}
 	
 	@Override
 	public void detach()
 	{
 		data = null;
 	}
 	
 	/**
 	 * Returns an iterator over the (sorted) sub-collection of the represented
 	 * data starting at the first-th entity and ending at the 
 	 * (first+count)-th entity.
 	 * 
 	 * @param first
 	 * @param count
 	 * @return
 	 */
 	public Iterator<? extends BO> iterator(int first, int count) 
 	{
 		// replace this with a special DAO method to only select the sub-list
 		// from the database call if necessary (instead of selecting all and 
 		// trimming)
 		return 
 			getData()
 				.subList(first, first + count)
 					.iterator();
 	}
 
 	/**
 	 * Returns the size of the represented collection.
 	 * 
 	 * @return
 	 */
 	public int size() 
 	{
 		return getData().size();
 	}
 
 	/**
 	 * Returns the given BO encapsulated into a lodable-detachable
 	 * model.
 	 * 
 	 * @param object
 	 */
 	public IModel<BO> model(BO object) 
 	{
 		return new DetachableModel<BO>(dao, object);
 	}
 }
