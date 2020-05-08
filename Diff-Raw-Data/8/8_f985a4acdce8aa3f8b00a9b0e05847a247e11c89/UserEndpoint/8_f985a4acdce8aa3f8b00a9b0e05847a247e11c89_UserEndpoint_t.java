 package de.roman.meter;
 
 import de.roman.meter.PMF;
 
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.api.server.spi.config.ApiMethod.HttpMethod;
 import com.google.api.server.spi.config.ApiNamespace;
 import com.google.api.server.spi.response.CollectionResponse;
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.datanucleus.query.JDOCursorHelper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.Nullable;
 import javax.inject.Named;
 import javax.persistence.EntityExistsException;
 import javax.persistence.EntityNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 @Api(name = "userendpoint", namespace = @ApiNamespace(ownerDomain = "roman.de", ownerName = "roman.de", packagePath = "meter"))
 public class UserEndpoint
 {
 
 	/**
 	 * This method lists all the entities inserted in datastore. It uses HTTP
 	 * GET method and paging support.
 	 * 
 	 * @return A CollectionResponse class containing the list of all entities
 	 *         persisted and a cursor to the next page.
 	 */
 	@SuppressWarnings(
 	{ "unchecked", "unused" })
 	@ApiMethod(name = "listUser")
 	public CollectionResponse<User> listUser(
 			@Nullable @Named("cursor") String cursorString,
 			@Nullable @Named("limit") Integer limit)
 	{
 
 		PersistenceManager mgr = null;
 		Cursor cursor = null;
 		List<User> execute = null;
 
 		try
 		{
 			mgr = getPersistenceManager();
 			Query query = mgr.newQuery(User.class);
 			if (cursorString != null && cursorString != "")
 			{
 				cursor = Cursor.fromWebSafeString(cursorString);
 				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
 				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
 				query.setExtensions(extensionMap);
 			}
 
 			if (limit != null)
 			{
 				query.setRange(0, limit);
 			}
 
 			execute = (List<User>) query.execute();
 			cursor = JDOCursorHelper.getCursor(execute);
 			if (cursor != null)
 				cursorString = cursor.toWebSafeString();
 
 			// Tight loop for fetching all entities from datastore and
 			// accomodate
 			// for lazy fetch.
 			for (User obj : execute)
 				;
 		} finally
 		{
 			mgr.close();
 		}
 
 		return CollectionResponse.<User> builder().setItems(execute)
 				.setNextPageToken(cursorString).build();
 	}
 
 	/**
 	 * This method gets the entity having primary key id. It uses HTTP GET
 	 * method.
 	 * 
 	 * @param id
 	 *            the primary key of the java bean.
 	 * @return The entity with primary key id.
 	 */
 	@ApiMethod(name = "getUser")
 	public User getUser(@Named("id") Long id)
 	{
 		PersistenceManager mgr = getPersistenceManager();
 		User user = null;
 		try
 		{
 			user = mgr.getObjectById(User.class, id);
 		} finally
 		{
 			mgr.close();
 		}
 		return user;
 	}
 
 	/**
 	 * This method gets the entity having the name. It uses HTTP GET method.
 	 * 
 	 * @param email
 	 *            email address of user.
 	 * @return The entity with primary key id.
 	 */
 	@ApiMethod(name = "getUserByEmail", path = "userbyemail", httpMethod = HttpMethod.GET)
 	public User getUserByEmail(@Named("email") String email)
 	{
 		PersistenceManager mgr = getPersistenceManager();
 		User user = null;
 
 		try
 		{
 
 			Query query = mgr.newQuery(User.class, "email == " + email);
 			@SuppressWarnings("unchecked")
 			List<User> foundUsers = (List<User>) query.execute();
 			
 			Iterator<User> iterator = foundUsers.iterator();
 			while (iterator.hasNext())
 			{
 				user = iterator.next();
 
 				if (user.getEmail().equals(email))
 				{
					
 					break;
				} else
				{
					user = null;
 				}
 			}
 
 		} finally
 		{
 			mgr.close();
 		}
 		return user;
 	}
 
 	/**
 	 * This method gets the meter list from the entity having primary key id. It
 	 * uses HTTP GET method.
 	 * 
 	 * @param id
 	 *            the primary key of the java bean.
 	 * @return List of meters.
 	 */
 	@ApiMethod(name = "getMeterListWithUserId", path = "getMeterListWithUserId", httpMethod = HttpMethod.GET)
 	public List<Meter> getMeterListWithUserId(@Named("id") Long id)
 	{
 		PersistenceManager mgr = getPersistenceManager();
 		User user = null;
 		Meter meter = null;
 		List<Meter> meterkeys;
 		List<Meter> meters = new ArrayList<Meter>();
 		Key meterKey;
 
 		try
 		{
 			user = mgr.getObjectById(User.class, id);
 			meterkeys = user.getMeters();
 		
 			for (int i = 0; i < meterkeys.size(); i++) {
 				meterKey = meterkeys.get(i).getKey();
 				meter = mgr.getObjectById(Meter.class, meterKey);
 				meters.add(meter);
 			}
 		} finally
 		{
 			mgr.close();
 		}
 		return meters;
 	}
 
 	/**
 	 * This inserts a new entity into App Engine datastore. If the entity
 	 * already exists in the datastore, an exception is thrown. It uses HTTP
 	 * POST method.
 	 * 
 	 * @param user
 	 *            the entity to be inserted.
 	 * @return The inserted entity.
 	 */
 	@ApiMethod(name = "insertUser")
 	public User insertUser(User user)
 	{
 		PersistenceManager mgr = getPersistenceManager();
 		try
 		{
 			if (containsUser(user))
 			{
 				throw new EntityExistsException("Object already exists");
 			}
 			mgr.makePersistent(user);
 		} finally
 		{
 			mgr.close();
 		}
 		return user;
 	}
 
 	/**
 	 * This inserts a new Meter entity to an existing User into App Engine
 	 * datastore. If the entity already exists in the datastore, an exception is
 	 * thrown. It uses HTTP POST method.
 	 * 
 	 * @param id
 	 *            user id to which the Meter should be added.
 	 * @param desc
 	 *            meterdescription
 	 * @param name
 	 *            meter name
 	 * @return The inserted entity.
 	 */
 	@ApiMethod(name = "insertMeterToUser", path = "insertMeterToUser", httpMethod = HttpMethod.POST)
 	public Meter insertMeterToUser(@Named("UserId") Long id,
 			@Named("MeterDesc") String desc, @Named("MeterName") String name, @Named("MeterType") MeterTypes type, @Named("MeterUnit") Units unit)
 	{
 		PersistenceManager mgr = getPersistenceManager();
 		User user = null;
 		Meter meter = new Meter();
 
 		try
 		{
 			user = mgr.getObjectById(User.class, id);
 			meter = new Meter();
 			meter.setDescription(desc);
 			meter.setName(name);
 			meter.setType(type);
 			meter.setUnit(unit);
			meter.setLastCount(0);
			meter.setLastCountDate(null);
 			user.getMeters().add(meter);
 			mgr.makePersistent(user);
 			mgr.currentTransaction().begin();
 			mgr.currentTransaction().commit();
 		} finally
 		{
 			if (mgr.currentTransaction().isActive())
 			{
 				mgr.currentTransaction().rollback();
 			}
 			mgr.close();
 		}
 		return meter;
 	}
 
 	/**
 	 * This method is used for updating an existing entity. If the entity does
 	 * not exist in the datastore, an exception is thrown. It uses HTTP PUT
 	 * method.
 	 * 
 	 * @param user
 	 *            the entity to be updated.
 	 * @return The updated entity.
 	 */
 	@ApiMethod(name = "updateUser")
 	public User updateUser(User user)
 	{
 		PersistenceManager mgr = getPersistenceManager();
 		try
 		{
 			if (!containsUser(user))
 			{
 				throw new EntityNotFoundException("Object does not exist");
 			}
 			mgr.makePersistent(user);
 		} finally
 		{
 			mgr.close();
 		}
 		return user;
 	}
 
 	/**
 	 * This method removes the entity with primary key id. It uses HTTP DELETE
 	 * method.
 	 * 
 	 * @param id
 	 *            the primary key of the entity to be deleted.
 	 * @return The deleted entity.
 	 */
 	@ApiMethod(name = "removeUser")
 	public User removeUser(@Named("id") Long id)
 	{
 		PersistenceManager mgr = getPersistenceManager();
 		User user = null;
 		try
 		{
 			user = mgr.getObjectById(User.class, id);
 			mgr.deletePersistent(user);
 		} finally
 		{
 			mgr.close();
 		}
 		return user;
 	}
 
 	private boolean containsUser(User user)
 	{
 		if (user.getId() == null)
 		{
 			return false;
 		}
 		PersistenceManager mgr = getPersistenceManager();
 		boolean contains = true;
 		try
 		{
 			mgr.getObjectById(User.class, user.getId());
 		} catch (javax.jdo.JDOObjectNotFoundException ex)
 		{
 			contains = false;
 		} finally
 		{
 			mgr.close();
 		}
 		return contains;
 	}
 
 	private static PersistenceManager getPersistenceManager()
 	{
 		return PMF.get().getPersistenceManager();
 	}
 
 }
