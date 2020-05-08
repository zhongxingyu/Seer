 package cz.cuni.mff.odcleanstore.webfrontend.core.models;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.model.IModel;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.EntityWithSurrogateKey;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoSortableDataProvidable;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.QueryCriteria;
 
 /**
  * A data provider to provide a collection of all registered instances of the 
  * given BO, sorted by values in a column given by name.
  * 
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  * @param <BO>
  */
 public class GenericSortableDataProvider<BO extends EntityWithSurrogateKey> extends SortableDataProvider<BO>
 {
 	private static final long serialVersionUID = 1L;
 
 	private DaoSortableDataProvidable<BO> dao;
 	private List<BO> data;
 	
 	/**
 	 * 
 	 * @param dao
 	 * @param defaultSortColumnName
 	 */
 	public GenericSortableDataProvider(DaoSortableDataProvidable<BO> dao, String defaultSortColumnName)
 	{
 		setSort(defaultSortColumnName, SortOrder.ASCENDING);
 		
 		this.dao = dao;
 	}
 	
 	/**
 	 * Loads all registered instances of the given BO in a lazy way. The data
 	 * get loaded sorted by the currently set column.
 	 * 
 	 * @return
 	 */
 	private List<BO> getData()
 	{
 		if (data == null)
 		{
 			SortParam sortParam = getSort();
 			
 			QueryCriteria criteria = new QueryCriteria();
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
		// TODO
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
