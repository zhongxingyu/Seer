 package sk.stuba.fiit.perconik.core.plugin;
 
 import java.util.List;
 import sk.stuba.fiit.perconik.core.listeners.DefaultListeners;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerClassesSupplier;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerManager;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerManagerFactory;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerProvider;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerProviderFactory;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerProviders;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerService;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerServiceFactory;
 import sk.stuba.fiit.perconik.core.services.listeners.ListenerServices;
 
 final class ListenerExtentionProcessor extends AbstractExtensionProcessor<ResolvedListeners>
 {
 	ListenerExtentionProcessor()
 	{
 		super(ExtensionPoints.listenersPoint, ExtensionPoints.listenersTypes);
 	}
 	
 	@Override
 	final ResolvedListeners processExtensions()
 	{
 		ListenerService service;
 		
 		boolean serviceSupplied           = this.atLeastOneSupplied(ListenerServiceFactory.class);
 		boolean serviceComponentsSupplied = this.atLeastOneSupplied(ListenerProviderFactory.class, ListenerManagerFactory.class);
 		
 		if (serviceSupplied)
 		{
 			if (serviceComponentsSupplied)
 			{
				this.console().warning("Custom %s supplied, custom supplied components ignored", "listener service");
 			}
 			
 			service = this.processListenerServiceFactories(this.getExtensions(ListenerServiceFactory.class));
 		}
 		else if (serviceComponentsSupplied)
 		{
 			ListenerService.Builder builder = ListenerServices.builder();
 			
 			builder.provider(this.processListenerProviderFactories(this.getExtensions(ListenerProviderFactory.class)));
 			builder.manager(this.processListenerManagerFactories(this.getExtensions(ListenerManagerFactory.class)));
 			
 			service = builder.build();
 		}
 		else
 		{
 			service = DefaultListeners.getDefaultListenerService();
 		}
 		
 		ListenerClassesSupplier supplier = this.processListenerClassesSuppliers(this.getExtensions(ListenerClassesSupplier.class));
 		
 		return new ResolvedListeners(service, supplier);
 	}
 	
 	private final ListenerService processListenerServiceFactories(final List<ListenerServiceFactory> factories)
 	{
 		if (this.emptyOrNotSingletonWithWarning(factories, "listener service"))
 		{
 			return DefaultListeners.getDefaultListenerService();
 		}
 		
 		return this.createListenerService(factories.get(0));
 	}
 
 	private final ListenerProvider processListenerProviderFactories(final List<ListenerProviderFactory> factories)
 	{
 		ListenerProvider provider = DefaultListeners.getDefaultListenerProvider();
 		
 		for (ListenerProviderFactory factory: factories)
 		{
 			provider = this.createListenerProvider(factory, provider);
 		}
 		
 		return provider;
 	}
 	
 	private final ListenerManager processListenerManagerFactories(final List<ListenerManagerFactory> factories)
 	{
 		if (this.emptyOrNotSingletonWithWarning(factories, "listener manager"))
 		{
 			return DefaultListeners.getDefaultListenerManager();
 		}
 		
 		return this.createListenerManager(factories.get(0));
 	}
 	
 	private final ListenerClassesSupplier processListenerClassesSuppliers(final List<ListenerClassesSupplier> suppliers)
 	{
 		if (this.emptyWithNotice(suppliers, "registered listeners"))
 		{
 			return DefaultListeners.getDefaultListenerClassesSupplier();
 		}
 		
 		return ListenerProviders.merge(suppliers);
 	}
 	
 	private final ListenerService createListenerService(final ListenerServiceFactory factory)
 	{
 		return resultOf(new SafeGet<ListenerService>(factory, ListenerService.class)
 		{
 			@Override
 			final ListenerService get()
 			{
 				return factory.create();
 			}
 		});
 	}
 
 	private final ListenerProvider createListenerProvider(final ListenerProviderFactory factory, final ListenerProvider parent)
 	{
 		return resultOf(new SafeGet<ListenerProvider>(factory, ListenerProvider.class)
 		{
 			@Override
 			final ListenerProvider get()
 			{
 				return factory.create(parent);
 			}
 		});
 	}
 	
 	private final ListenerManager createListenerManager(final ListenerManagerFactory factory)
 	{
 		return resultOf(new SafeGet<ListenerManager>(factory, ListenerManager.class)
 		{
 			@Override
 			final ListenerManager get()
 			{
 				return factory.create();
 			}
 		});
 	}
 }
