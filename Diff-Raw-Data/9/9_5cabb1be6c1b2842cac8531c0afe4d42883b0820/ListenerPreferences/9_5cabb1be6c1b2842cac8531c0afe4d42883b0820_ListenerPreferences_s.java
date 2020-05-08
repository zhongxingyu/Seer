 package sk.stuba.fiit.perconik.preferences;
 
 import java.util.Set;
 import sk.stuba.fiit.perconik.core.persistence.data.ListenerPersistenceData;
 
 /**
  * Listener preferences. Supports both <i>default</i>
  * and <i>instance</i> (actually used and stored) scopes.
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class ListenerPreferences extends AbstractPreferences
 {
 	private static final String persistence = "persistence";
 	
 	private ListenerPreferences(final Scope scope)
 	{
 		super(scope, "listeners");
 	}
 
 	/**
 	 * Gets default listener preferences.
 	 */
 	public static final ListenerPreferences getDefault()
 	{
 		return new ListenerPreferences(Scope.DEFAULT);
 	}
 	
 	/**
 	 * Gets listener preferences.
 	 */
 	public static final ListenerPreferences getInstance()
 	{
 		return new ListenerPreferences(Scope.INSTANCE);
 	}
 	
 	/**
 	 * Sets listener persistence data.
 	 * @param data listener persistence data
 	 * @throws NullPointerException if {@code data} is {@code null}
 	 */
 	public final void setListenerPersistenceData(final Set<ListenerPersistenceData> data)
 	{
 		this.setValue(this.key(persistence), data);
 	}
 	
 	/**
 	 * Gets listener persistence data.
 	 */
 	public final Set<ListenerPersistenceData> getListenerPersistenceData()
 	{
 		try
 		{
 			return (Set<ListenerPersistenceData>) this.getObject(this.key(persistence));
 		}
 		catch (RuntimeException e)
 		{
 			if (this.scope != Scope.DEFAULT)
 			{
				return getDefault().getListenerPersistenceData();
 			}
 			
 			throw e;
 		}
 	}
 }
