 package com.gratex.perconik.activity;
 
 import static com.gratex.perconik.activity.ActivityDefaults.console;
 import java.net.URL;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import javax.annotation.Nullable;
 import javax.xml.namespace.QName;
 import com.google.common.collect.ClassToInstanceMap;
 import com.google.common.collect.MutableClassToInstanceMap;
 import com.gratex.perconik.activity.plugin.Activator;
 import com.gratex.perconik.services.activity.IVsActivityWatcherService;
 import com.gratex.perconik.services.activity.VsActivityWatcherService;
 
 public final class ActivityServices
 {
 	private static final Object lock = new Object();
 
 	private static final ClassToInstanceMap<Object> services = MutableClassToInstanceMap.create();
 	
 	private static final Executor executor = Executors.newCachedThreadPool();
 
 	private ActivityServices()
 	{
 		throw new AssertionError();
 	}
 	
 	static final IVsActivityWatcherService newWatcherService()
 	{
 		return newWatcherService(ActivityDefaults.watcherUrl);
 	}
 
 	static final IVsActivityWatcherService newWatcherService(final URL url)
 	{
 		QName name = ActivityDefaults.watcherName;
 		
 		VsActivityWatcherService service = new VsActivityWatcherService(url, name);
 
 		return service.getBasicHttpBindingIVsActivityWatcherService();
 	}
 	
 	static final IVsActivityWatcherService prepareWatcherService()
 	{
 		synchronized (lock)
 		{
 			IVsActivityWatcherService service = services.getInstance(IVsActivityWatcherService.class);
 			
 			if (service == null)
 			{
 				try
 				{
 					service = newWatcherService(ActivityDefaults.watcherUrl);
 					
 					services.putInstance(IVsActivityWatcherService.class, service);
 				}
 				catch (Exception failure)
 				{
 					console().error("Unable to construct activity watcher service", failure);
 				}
 			}
 			
 			return service;
 		}
 	}
 	
 	static final void reportWatcherServiceFailure(@Nullable final Exception failure)
 	{
 		synchronized (lock)
 		{
 			services.remove(IVsActivityWatcherService.class);
 		}
 		
 		Activator.getDefault().getConsole().error("Unexpected failure of activity watcher service", failure);
 	}
 
 	public static final void performWatcherServiceOperation(final WatcherServiceOperation operation)
 	{
 		executor.execute(new Runnable()
 		{
 			public final void run()
 			{
 				IVsActivityWatcherService service = null;
 				
 				try
 				{
 					service = prepareWatcherService();
 					
 					if (service != null)
 					{
 						operation.perform(service);
 					}
 					else
 					{
 						console().notice("Unable to perform activity watcher service operation, service not available");
 					}
 				}
 				catch (Exception e)
 				{
 					reportWatcherServiceFailure(e);
 				}
 			}
 		});
 	}
 
	static interface ServiceOperation<S extends Object>
 	{
 		public void perform(S service);
 	}
 	
 	public static interface WatcherServiceOperation extends ServiceOperation<IVsActivityWatcherService>
 	{
 	}
 }
