 package sk.stuba.fiit.perconik.preferences;
 
import java.util.Collections;
 import java.util.Set;

import sk.stuba.fiit.perconik.core.persistence.data.ListenerPersistenceData;
 import sk.stuba.fiit.perconik.core.persistence.data.ResourcePersistenceData;
 
 /**
  * Resource preferences. Supports both <i>default</i>
  * and <i>instance</i> (actually used and stored) scopes.
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class ResourcePreferences extends AbstractPreferences
 {
 	private static final String persistence = "persistence";
 	
 	private ResourcePreferences(final Scope scope)
 	{
 		super(scope, "resources");
 	}
 
 	/**
 	 * Gets default resource preferences.
 	 */
 	public static final ResourcePreferences getDefault()
 	{
 		return new ResourcePreferences(Scope.DEFAULT);
 	}
 	
 	/**
 	 * Gets resource preferences.
 	 */
 	public static final ResourcePreferences getInstance()
 	{
 		return new ResourcePreferences(Scope.INSTANCE);
 	}
 
 	/**
 	 * Sets resource persistence data.
 	 * @param data resource persistence data
 	 * @throws NullPointerException if {@code data} is {@code null}
 	 */
 	public final void setResourcePersistenceData(final Set<ResourcePersistenceData> data)
 	{
 		this.setValue(this.key(persistence), data);
 	}
 
 	/**
 	 * Gets resource persistence data.
 	 */
 	public final Set<ResourcePersistenceData> getResourcePersistenceData()
 	{		
 		try
 		{
 			return (Set<ResourcePersistenceData>) this.getObject(this.key(persistence));
 		}
 		catch (RuntimeException e)
 		{
 			if (this.scope != Scope.DEFAULT)
 			{
 				return ResourcePersistenceData.defaults();
 			}
 			
 			throw e;
 		}
 	}
 }
