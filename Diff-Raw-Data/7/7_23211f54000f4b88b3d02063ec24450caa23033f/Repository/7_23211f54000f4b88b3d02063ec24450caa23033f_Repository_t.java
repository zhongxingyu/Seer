 package org.virtualrepository.impl;
 
 import static java.util.Arrays.*;
 import static org.virtualrepository.Utils.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualrepository.Asset;
 import org.virtualrepository.AssetType;
 import org.virtualrepository.RepositoryService;
 import org.virtualrepository.VirtualRepository;
 import org.virtualrepository.spi.Importer;
 import org.virtualrepository.spi.MutableAsset;
 import org.virtualrepository.spi.Publisher;
 
 /**
  * Default {@link VirtualRepository} implementation.
  * 
  * @author Fabio Simeoni
  * 
  */
 public class Repository implements VirtualRepository {
 
 	private static final int DEFAULT_DISCOVERY_TIMEOUT = 30;
 
 	private final static Logger log = LoggerFactory.getLogger(VirtualRepository.class);
 
 	private final Services services;
 
 	private Map<String, Asset> assets = new HashMap<String, Asset>();
 
 	private static ExecutorService executor = Executors.newCachedThreadPool();
 	
 	
 	/**
 	 * Replaces the default {@link ExecutorService} used to parallelise and/or time-control discovery, retrieval, and publication tasks. 
 	 * @param service the service
 	 */
 	public static void setExecutor(ExecutorService service) {
 		executor=service;
 	}
 
 	/**
 	 * Creates an instance over all the {@link RepositoryService}s available on the classpath.
 	 * 
 	 * @see Services#load()
 	 */
 	public Repository() {
 
 		services = new Services();
 		services.load();
 
 	}
 
 	/**
 	 * Creates an instance over given {@link RepositoryService}s.
 	 * 
 	 * @param services the services
 	 */
 	public Repository(RepositoryService... services) {
 		
 		this(new Services(services));
 	}
 
 	/**
 	 * Creates an instance over a collection of {@link RepositoryService}s
 	 * 
 	 * @param services the collection
 	 */
 	public Repository(Services services) {
 
 		notNull("services", services);
 
 		this.services = services;
 	}
 
 	@Override
 	public Services services() {
 		return services;
 	}
 	
 	@Override
 	public Collection<RepositoryService> sinks(AssetType... types) {
 		
 		List<RepositoryService> matching = new ArrayList<RepositoryService>();
 		for (RepositoryService service : services) {
 			ServiceInspector inspector = new ServiceInspector(service);
 			if (!inspector.taken(types).isEmpty())
 				matching.add(service);
 		}
 		return matching;
 			
 	}
 	
 	@Override
 	public Collection<RepositoryService> sources(AssetType... types) {
 		
 		List<RepositoryService> matching = new ArrayList<RepositoryService>();
 		for (RepositoryService service : this.services) {
 			ServiceInspector inspector = new ServiceInspector(service);
 			if (!inspector.returned(types).isEmpty())
 				matching.add(service);
 		}
 		return matching;
 			
 	}
 
 	@Override
 	public int discover(AssetType... types) {
 
 		return discover(DEFAULT_DISCOVERY_TIMEOUT,types);
 	}
 	
 	@Override
 	public int discover(long timeout,AssetType... types) {
 
 		notNull(types);
 
 		final List<AssetType> typeList = asList(types);
 
 		log.info("discovering assets of types {}", typeList);
 
 		final AtomicInteger newAssets = new AtomicInteger(0);
 		final AtomicInteger refreshedAssets = new AtomicInteger(0);
 
 		CompletionService<Void> completed = new ExecutorCompletionService<Void>(executor);
 		int submittedTasks=0;
 		
 		long time = System.currentTimeMillis();
 		
 		for (final RepositoryService service : services) {
 			
 			final ServiceInspector inspector = new ServiceInspector(service);
 			
 			final Collection<AssetType> importTypes = inspector.returned(types);
 
 			if (importTypes.isEmpty()) {
 				log.info("service {} does not support types {} and will be ignored for discovery",service,typeList);
 				continue;
 			}
 			
 			DiscoveryTask task = new DiscoveryTask(service,importTypes,newAssets,refreshedAssets);
 			completed.submit(task, null);
 			submittedTasks++;
 		
 		}
 
 		for (int i = 0; i < submittedTasks; i++)
 			try {
 				//wait at most 30 secs for the slowest to finish
				if (completed.poll(timeout, TimeUnit.SECONDS) == null) {
 					log.warn("asset discovery timed out after succesful interaction with {} service(s)", i);
					break;
				}
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt(); // be a good citizen
 				log.warn("asset discovery was interrupted after succesful interaction with {} service(s)", i);
 			}
 
 		log.info("discovered {} new assets of types {}, refreshed {}, total {} in {} ms.", newAssets, typeList, refreshedAssets,
 				assets.size(),System.currentTimeMillis()-time);
 
 		return newAssets.get();
 	}
 
 	@Override
 	public Iterator<Asset> iterator() {
 		return assets.values().iterator();
 	}
 
 	@Override
 	public Asset lookup(String id) {
 
 		notNull("identifier", id);
 
 		Asset asset = assets.get(id);
 
 		if (asset == null)
 			throw new IllegalStateException("unknown asset " + id);
 		else
 			return asset;
 
 	}
 
 	@Override
 	public <A> A retrieve(final Asset asset, Class<A> api) {
 
 		notNull(asset);
 		notNull(api);
 		
 		if (asset.service()==null)
 			throw new IllegalArgumentException("asset "+asset.id()+" has no target service, please set it");
 
 		ServiceInspector inspector = new ServiceInspector(asset.service());
 
 		final Importer<Asset, A> reader = inspector.importerFor(asset.type(), api);
 
 		Callable<A> task = new Callable<A>() {
 			
 			@Override
 			public A call() throws Exception {
 				return reader.retrieve(asset);
 			}
 		};
 		
 		try {
 			log.info("retrieving data for asset {} ({})",asset.id(),asset.name());
 			long time = System.currentTimeMillis();
 			Future<A> future = executor.submit(task);
 			A result = future.get(3,TimeUnit.MINUTES);
 			log.info("retrieved data for asset {} ({}) in {} ms.",asset.id(),asset.name(),System.currentTimeMillis()-time);
 			return result;
 		}
 		catch(InterruptedException e) {
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(e);
 		}
 		catch(TimeoutException e) {
 			throw new RuntimeException("timeout retrieving content for asset \n" + asset + "\n from repository service "
 					+ asset.service().name(), e);
 		}
 		catch (ExecutionException e) {
 			throw new RuntimeException("error retrieving content for asset \n" + asset + "\n from repository service "
 					+ asset.service().name(), e.getCause());
 		}
 
 	}
 
 	@Override
 	public void publish(final Asset asset, final Object content) {
 
 		if (asset.service()==null)
 			throw new IllegalArgumentException("asset "+asset.id()+" has no target service, please set it");
 		
 		ServiceInspector inspector = new ServiceInspector(asset.service());
 		
 		final Publisher<Asset, Object> writer = inspector.publisherFor(asset.type(), content.getClass());
 
 		Runnable task = new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					writer.publish(asset, content);
 				}
 				catch (Exception e) {
 					throw new RuntimeException(e);
 				}
 			}
 		};
 		
 		try {
 			log.info("publishing asset {} to {}",asset.name(),asset.service().name());
 			long time = System.currentTimeMillis();
 			Future<?> future = executor.submit(task);
 			future.get(3,TimeUnit.MINUTES);
 			log.info("published asset {} to {} in {} ms.",asset.name(),asset.service().name(),System.currentTimeMillis()-time);
 		}
 		catch(InterruptedException e) {
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(e);
 		}
 		catch(TimeoutException e) {
 			throw new RuntimeException("timeout publishing asset \n" + asset + "\n from repository service "
 					+ asset.service().name(), e);
 		}
 		catch (ExecutionException e) {
 			throw new RuntimeException("error publishing asset \n" + asset + "\n through repository service "
 					+ asset.service().name(), e.getCause());
 		}
 
 	}
 
 	
 	private class DiscoveryTask implements Runnable {
 		
 		private final RepositoryService service;
 		private final AtomicInteger newAssets;
 		private final AtomicInteger refreshedAssets;
 		private final Collection<AssetType> types;
 		
 		DiscoveryTask(RepositoryService service, Collection<AssetType> types, AtomicInteger newAssets, AtomicInteger refreshedAssets) {
 			this.service=service;
 			this.newAssets=newAssets;
 			this.refreshedAssets=refreshedAssets;
 			this.types=types;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				
 				log.info("discovering assets of types {} from {}", types, service.name());
 				
 				long time = System.currentTimeMillis();
 				
 				Iterable<? extends MutableAsset> discoveredAssets = service.proxy().browser().discover(types);
 				
 				int newAssetsByThisTask=0;
 				int refreshedAssetsByThisTask=0;
 				for (MutableAsset asset : discoveredAssets) {
 					asset.setService(service);
 					if (assets.put(asset.id(), asset) == null)
 						newAssetsByThisTask++;
 					else
 						refreshedAssetsByThisTask++;
 				}
 				
 				newAssets.addAndGet(newAssetsByThisTask);
 				refreshedAssets.addAndGet(refreshedAssetsByThisTask);
 				
 				log.info("discovered {} assets of types {} ({} new, {} refreshedAssets) from {} in {} ms. ",  newAssets.get()+refreshedAssets.get(), types, newAssets, refreshedAssets, service.name(), System.currentTimeMillis()-time);
 				
 			} catch (Exception e) {
 				log.warn("cannot discover assets from repository service " + service.name(), e);
 			}
 		}
 	}
 }
