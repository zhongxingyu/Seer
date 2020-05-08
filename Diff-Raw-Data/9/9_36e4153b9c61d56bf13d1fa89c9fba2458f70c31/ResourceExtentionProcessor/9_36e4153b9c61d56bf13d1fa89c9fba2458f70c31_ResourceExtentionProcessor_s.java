 package sk.stuba.fiit.perconik.core.plugin;
 
 import java.util.List;
 import sk.stuba.fiit.perconik.core.resources.DefaultResources;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceManager;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceManagerFactory;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceNamesSupplier;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceProvider;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceProviderFactory;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceProviders;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceService;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceServiceFactory;
 import sk.stuba.fiit.perconik.core.services.resources.ResourceServices;
 
 final class ResourceExtentionProcessor extends AbstractExtensionProcessor<ResolvedResources>
 {
 	ResourceExtentionProcessor()
 	{
 		super(ExtensionPoints.resourcesPoint, ExtensionPoints.resourcesTypes);
 	}
 	
 	@Override
 	final ResolvedResources processExtensions()
 	{
 		ResourceService service;
 		
 		boolean serviceSupplied           = this.atLeastOneSupplied(ResourceServiceFactory.class);
 		boolean serviceComponentsSupplied = this.atLeastOneSupplied(ResourceProviderFactory.class, ResourceManagerFactory.class);
 		
 		if (serviceSupplied)
 		{
 			if (serviceComponentsSupplied)
 			{
				this.console().warning("Custom %s supplied, custom supplied %s components ignored", "resource service");
 			}
 			
 			service = this.processResourceServiceFactories(this.getExtensions(ResourceServiceFactory.class));
 		}
 		else if (serviceComponentsSupplied)
 		{
 			ResourceService.Builder builder = ResourceServices.builder();
 			
 			builder.provider(this.processResourceProviderFactories(this.getExtensions(ResourceProviderFactory.class)));
 			builder.manager(this.processResourceManagerFactories(this.getExtensions(ResourceManagerFactory.class)));
 			
 			service = builder.build();
 		}
 		else
 		{
 			service = DefaultResources.getDefaultResourceService();
 		}
 		
 		ResourceNamesSupplier supplier = this.processResourceNamesSuppliers(this.getExtensions(ResourceNamesSupplier.class));
 		
 		return new ResolvedResources(service, supplier);
 	}
 	
 	private final ResourceService processResourceServiceFactories(final List<ResourceServiceFactory> factories)
 	{
 		if (this.emptyOrNotSingletonWithWarning(factories, "resource service"))
 		{
 			return DefaultResources.getDefaultResourceService();
 		}
 		
 		return this.createResourceService(factories.get(0));
 	}
 
 	private final ResourceProvider processResourceProviderFactories(final List<ResourceProviderFactory> factories)
 	{
 		ResourceProvider provider = DefaultResources.getDefaultResourceProvider();
 		
 		for (ResourceProviderFactory factory: factories)
 		{
 			provider = this.createResourceProvider(factory, provider);
 		}
 		
 		return provider;
 	}
 	
 	private final ResourceManager processResourceManagerFactories(final List<ResourceManagerFactory> factories)
 	{
 		if (this.emptyOrNotSingletonWithWarning(factories, "resource manager"))
 		{
 			return DefaultResources.getDefaultResourceManager();
 		}
 		
 		return this.createResourceManager(factories.get(0));
 	}
 	
 	private final ResourceNamesSupplier processResourceNamesSuppliers(final List<ResourceNamesSupplier> suppliers)
 	{
 		if (this.emptyWithNotice(suppliers, "registered resources"))
 		{
 			return DefaultResources.getDefaultResourceNamesSupplier();
 		}
 		
 		return ResourceProviders.merge(suppliers);
 	}
 	
 	private final ResourceService createResourceService(final ResourceServiceFactory factory)
 	{
 		return resultOf(new SafeGet<ResourceService>(factory, ResourceService.class)
 		{
 			@Override
 			final ResourceService get()
 			{
 				return factory.create();
 			}
 		});
 	}
 
 	private final ResourceProvider createResourceProvider(final ResourceProviderFactory factory, final ResourceProvider parent)
 	{
 		return resultOf(new SafeGet<ResourceProvider>(factory, ResourceProvider.class)
 		{
 			@Override
 			final ResourceProvider get()
 			{
 				return factory.create(parent);
 			}
 		});
 	}
 	
 	private final ResourceManager createResourceManager(final ResourceManagerFactory factory)
 	{
 		return resultOf(new SafeGet<ResourceManager>(factory, ResourceManager.class)
 		{
 			@Override
 			final ResourceManager get()
 			{
 				return factory.create();
 			}
 		});
 	}
 }
