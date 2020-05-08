 package sk.stuba.fiit.perconik.core.services.listeners;
 
 import java.util.Set;
 import sk.stuba.fiit.perconik.core.Listener;
 import sk.stuba.fiit.perconik.core.Listeners;
 import sk.stuba.fiit.perconik.core.Resource;
 import sk.stuba.fiit.perconik.core.ResourceNotRegistredException;
 import sk.stuba.fiit.perconik.core.resources.DefaultResources;
 import sk.stuba.fiit.perconik.core.services.AbstractManager;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceManager;
 
 /**
  * An abstract implementation of {@link ListenerManager}. This class
  * implements the listener registration mechanism based on the underlying
  * {@link ResourceManager}.
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public abstract class AbstractListenerManager extends AbstractManager implements ListenerManager
 {
 	// TODO add javadocs
 	
 	/**
 	 * Constructor for use by subclasses.
 	 */
 	protected AbstractListenerManager()
 	{
 	}
 
 	protected abstract ResourceManager manager();
 
 	private final <L extends Listener> Set<Resource<? super L>> registrables(final L listener)
 	{
 		Set<Resource<? super L>> resources = this.manager().registrables((Class<L>) listener.getClass());
 		
 		for (Class<? extends Listener> type: Listeners.resolveTypes(listener))
 		{
 			if (this.manager().registrables(type).isEmpty())
 			{
				throw new ResourceNotRegistredException("No registred resources for listener type " + listener.getClass().getName() + " as " + type.getName());
 			}
 		}
 
 		return resources;
 	}
 	
 	public final <L extends Listener> void register(final L listener)
 	{
 		DefaultResources.registerTo(this.registrables(listener), listener);
 	}
 
 	public final <L extends Listener> void unregister(final L listener)
 	{
 		DefaultResources.unregisterFrom(this.registrables(listener), listener);
 	}
 }
