 package org.mosaic.lifecycle.impl.dependency;
 
 import com.google.common.reflect.TypeToken;
 import java.util.HashMap;
 import java.util.Map;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.lifecycle.annotation.ServiceProperty;
 import org.mosaic.lifecycle.impl.ModuleImpl;
 import org.mosaic.util.reflection.MethodHandle;
 import org.mosaic.util.reflection.MethodParameter;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.mosaic.lifecycle.impl.util.FilterUtils.createFilter;
 
 /**
  * @author arik
  */
 public abstract class AbstractSingleServiceDependency extends AbstractBeanDependency
         implements ServiceTrackerCustomizer<Object, Object>
 {
     protected final Logger LOG = LoggerFactory.getLogger( getClass() );
 
     @Nonnull
     protected final ModuleImpl module;
 
     @Nullable
     protected final Class<?> serviceType;
 
     @Nullable
     protected final String filter;
 
     @Nonnull
     protected final String beanName;
 
     @Nonnull
     protected final MethodHandle methodHandle;
 
     @Nonnull
     protected final MethodHandle.Invoker invoker;
 
     @Nullable
     protected ServiceTracker<?, ?> tracker;
 
     public AbstractSingleServiceDependency( @Nonnull ModuleImpl module,
                                             @Nullable String filterSpec,
                                             @Nonnull String beanName,
                                             @Nonnull MethodHandle methodHandle )
     {
         this.module = module;
         this.filter = filterSpec;
         this.beanName = beanName;
         this.methodHandle = methodHandle;
         this.serviceType = detectServiceType();
         this.invoker = this.methodHandle.createInvoker( new ServicePropertyParameterResolver(),
                                                         new ServiceIdParameterResolver(),
                                                         new ServiceInstanceResolver() );
     }
 
     @Nonnull
     @Override
     public final String getBeanName()
     {
         return this.beanName;
     }
 
     @Override
     public void start()
     {
         if( this.serviceType != null )
         {
             Filter filter = createFilter( this.serviceType, this.filter );
             this.tracker = new ServiceTracker<>( this.module.getBundleContext(), filter, this );
             this.tracker.open();
         }
     }
 
     @Override
     public final boolean isSatisfied()
     {
         return this.tracker != null && isSatisfiedInternal( this.tracker );
     }
 
     public abstract boolean isSatisfiedInternal( @Nonnull ServiceTracker<?, ?> tracker );
 
     @Override
     public void stop()
     {
         if( this.tracker != null )
         {
             this.tracker.close();
             this.tracker = null;
         }
     }
 
     @Nullable
     @Override
     public final Object addingService( @Nonnull ServiceReference<Object> reference )
     {
         Object service = null;
 
         BundleContext bundleContext = this.module.getBundleContext();
         if( bundleContext != null )
         {
             service = bundleContext.getService( reference );
             if( service != null )
             {
                 onServiceAdded( reference, service );
             }
         }
 
         return service;
     }
 
     @Override
     public final void modifiedService( @Nonnull ServiceReference<Object> reference, @Nonnull Object service )
     {
         onServiceModified( reference, service );
     }
 
     @Override
     public final void removedService( @Nonnull ServiceReference<Object> reference, @Nonnull Object service )
     {
         onServiceRemoved( reference, service );
     }
 
     protected final void inject( @Nonnull ServiceReference<Object> reference, @Nullable Object service )
     {
         Object bean = this.module.getBean( this.beanName );
         if( bean != null )
         {
             inject( bean, reference, service );
         }
     }
 
     protected final void inject( @Nonnull Object bean,
                                  @Nonnull ServiceReference<?> reference,
                                  @Nullable Object service )
     {
         Map<String, Object> context = new HashMap<>();
         context.put( "serviceReference", reference );
         context.put( "service", service );
         try
         {
             this.invoker.resolve( context ).invoke( bean );
         }
         catch( Exception e )
         {
             LOG.error( "Could not inject service '{}' to method '{}' in bean '{}' of module '{}': {}",
                        service, this.methodHandle, bean, this.module, e.getMessage(), e );
         }
     }
 
     protected void onServiceAdded( @Nonnull ServiceReference<Object> reference, @Nonnull Object service )
     {
         // no-op
     }
 
     protected void onServiceModified( @Nonnull ServiceReference<Object> reference, @Nonnull Object service )
     {
         // no-op
     }
 
     protected void onServiceRemoved( @Nonnull ServiceReference<Object> reference, @Nullable Object service )
     {
         // no-op
     }
 
     @Nullable
     private Class<?> detectServiceType()
     {
         TypeToken<?> type = null;
         for( MethodParameter parameter : this.methodHandle.getParameters() )
         {
            ServiceProperty servicePropertyAnn = parameter.getAnnotation( ServiceProperty.class );
            if( servicePropertyAnn == null )
             {
                 if( type != null )
                 {
                     // only one parameter can be without @ServiceProperty - don't satisfy this dependency
                     return null;
                 }
                 type = parameter.getType();
             }
         }
         return type == null ? null : type.getRawType();
     }
 }
