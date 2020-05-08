 package wicket.contrib.phonebook.web;
 
 import java.util.Iterator;
 
 import wicket.contrib.phonebook.Contact;
 import wicket.contrib.phonebook.ContactDao;
 import wicket.contrib.phonebook.QueryParam;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.model.IModel;
 
 /**
 * 
 * @author igor
 * 
  * note: it is important that the dao passed to the data provider be a proxy
  * from wicket-contrib-spring when used in non-testing environment. this is
  * because the dataprovider might get serialized for versioning or for
  * replication among the cluster and that would mean that the dao will also be
  * serialized. this is usually undesirable because the dao might have references
  * to other objects and thus might cause a lot more to be serialized then is
  * needed. wicket-contrib-spring provides proxies to fix just this, the proxy
  * only serializes information it needs to locate the dao when it is
  * deserialized instead of serializing the dao itself.
  */
 public class ContactsDataProvider extends SortableDataProvider implements
 		IFilterStateLocator {
 
 	/** dao that will be used to retrieve the list of contacts */
 	private ContactDao dao;
 
 	/** reuse the contact entity to store filter information */
 	private Contact filter = new Contact();
 
     private QueryParam queryParam;
     
     public void setQueryParam(QueryParam queryParam) {
         this.queryParam = queryParam;
     }
     
     public Object getFilterState() {
         return filter;
     }
 
 	public void setFilterState(Object state) {
 		filter = (Contact) state;
 	}
 
 	public ContactsDataProvider(ContactDao dao) {
 		this.dao = dao;
 
 		// set the default sort
 		setSort("firstname", true);
 	}
 
 	/**
 	 * Gets an iterator for the subset of contacts.
 	 * 
 	 * @param first
 	 *            offset for the first row of data to retrieve
 	 * @param count
 	 *            number of rows to retrieve
 	 * @return iterator capable of iterating over {first, first+count} contacts
 	 */
 	public Iterator iterator(int first, int count) {
 		SortParam sp = getSort();
         if (queryParam == null) {
             queryParam = new QueryParam(first, count, sp.getProperty(), sp.isAscending());
         } else {
             queryParam.setFirst(first);
             queryParam.setCount(count);
             queryParam.setSort(sp.getProperty());
             queryParam.setSortAsc(sp.isAscending());
         }
 		return dao.find(queryParam, filter);
 	}
 
 	/**
 	 * Gets total number of items in the collection.
 	 * 
 	 * @return total item count
 	 */
 	public int size() {
 		return dao.count(filter);
 	}
 
 	/**
 	 * Converts the object in the collection to its model representation. A good
 	 * place to wrap the object in a detachable model.
 	 * 
 	 * @param object
 	 *            The object that needs to be wrapped
 	 * @return The model representation of the object
 	 */
 	public IModel model(Object object) {
 		return new DetachableContactModel((Contact) object, dao);
 	}
 
 }
