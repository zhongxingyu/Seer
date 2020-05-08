 package sk.stuba.fiit.perconik.core.services.resources;
 
 import java.util.Set;
 import java.util.Map.Entry;
 import sk.stuba.fiit.perconik.core.Listener;
 import sk.stuba.fiit.perconik.core.Resource;
import sk.stuba.fiit.perconik.utilities.reflection.MoreReflection;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.SetMultimap;
 import com.google.common.collect.Sets;
 
 final class StandardResourceManager extends AbstractResourceManager
 {
 	private final SetMultimap<Class<? extends Listener>, Resource<?>> multimap;
 	
 	StandardResourceManager()
 	{
 		this.multimap = HashMultimap.create();
 	}
 	
 	@Override
 	protected final SetMultimap<Class<? extends Listener>, Resource<?>> multimap()
 	{
 		return this.multimap;
 	}
 	
 	public final <L extends Listener> void unregisterAll(final Class<L> type)
 	{
 		for (Entry<Class<? extends L>, Resource<? extends L>> entry: this.assignablesAsSetMultimap(type).entries())
 		{
 			this.unregister(entry.getKey(), Unsafe.cast(type, entry.getValue()));
 		}
 	}
 	
 	public final <L extends Listener> Set<Resource<? extends L>> assignables(final Class<L> type)
 	{
 		return Sets.newHashSet(this.assignablesAsSetMultimap(type).values());
 	}
 
 	private final <L extends Listener> SetMultimap<Class<? extends L>, Resource<? extends L>> assignablesAsSetMultimap(final Class<L> type)
 	{
 		SetMultimap<Class<? extends L>, Resource<? extends L>> result = HashMultimap.create();
 		
 		for (Entry<Class<? extends Listener>, Resource<?>> entry: this.multimap.entries())
 		{
 			if (type.isAssignableFrom(entry.getKey()))
 			{
 				result.put((Class<? extends L>) entry.getKey(), (Resource<? extends L>) entry.getValue());
 			}
 		}
 		
 		return result;
 	}
 	
 	public final <L extends Listener> Set<Resource<? super L>> registrables(final Class<L> type)
 	{
 		Set<Resource<? super L>> result = Sets.newHashSet();
 		
 		for (Entry<Class<? extends Listener>, Resource<?>> entry: this.multimap.entries())
 		{
 			boolean matched = type == entry.getKey();
 			
 			if (!matched)
 			{
				for (Class<?> supertype: MoreReflection.getInterfaces(type))
 				{
 					if (supertype == entry.getKey())
 					{
 						matched = true;
 						
 						break;
 					}
 				}
 			}
 			
 			if (matched)
 			{
 				result.add((Resource<? super L>) entry.getValue());
 			}
 		}
 		
 		return result;
 	}
 
 	public final SetMultimap<Class<? extends Listener>, Resource<?>> registrations()
 	{
 		return HashMultimap.create(this.multimap);
 	}
 
 	public final boolean registered(final Class<? extends Listener> type, final Resource<?> resource)
 	{
 		return this.multimap.containsEntry(type, resource);
 	}
 }
