 package org.mosaic.modules.impl;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.util.*;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.modules.*;
 import org.mosaic.modules.ServiceReference;
 import org.mosaic.util.collections.HashMapEx;
 import org.mosaic.util.collections.MapEx;
 import org.mosaic.util.osgi.FilterBuilder;
 import org.mosaic.util.pair.Pair;
 import org.osgi.framework.*;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
import static java.util.Arrays.asList;

 /**
  * @author arik
  */
 @SuppressWarnings("unchecked")
 final class ComponentFieldServiceListLifecycle extends ComponentField implements ServiceTrackerCustomizer,
                                                                                  List,
                                                                                  ModuleWiring.ServiceRequirement
 {
     @Nonnull
     private final Class<?> requiredServiceType;
 
     @Nullable
     private final String requiredFilter;
 
     @Nonnull
     private final ServiceTracker serviceTracker;
 
     @Nullable
     private List services;
 
     ComponentFieldServiceListLifecycle( @Nonnull ComponentDescriptorImpl<?> componentDescriptor, @Nonnull Field field )
     {
         super( componentDescriptor, field );
 
         Service serviceAnn = field.getAnnotation( Service.class );
         if( serviceAnn.value().length > 0 )
         {
             String msg = "field '" + field.getName() + "' of component " + componentDescriptor + " defines the 'value' attribute on its @Service annotation (it should not)";
             throw new ComponentDefinitionException( msg, componentDescriptor.getComponentType(), componentDescriptor.getModule() );
         }
 
         java.lang.reflect.Type listType = field.getGenericType();
         if( !( listType instanceof ParameterizedType ) )
         {
             String msg = "field '" + field.getName() + "' of component " + componentDescriptor + " does not specify type for List";
             throw new ComponentDefinitionException( msg, componentDescriptor.getComponentType(), componentDescriptor.getModule() );
         }
 
         ParameterizedType parameterizedListType = ( ParameterizedType ) listType;
         java.lang.reflect.Type[] listTypeArguments = parameterizedListType.getActualTypeArguments();
         if( listTypeArguments.length == 0 )
         {
             String msg = "field '" + field.getName() + "' of component " + componentDescriptor + " does not specify type for List";
             throw new ComponentDefinitionException( msg, componentDescriptor.getComponentType(), componentDescriptor.getModule() );
         }
 
         Pair<Class<?>, FilterBuilder> pair = ComponentDescriptorImpl.getServiceAndFilterFromType( componentDescriptor.getModule(), componentDescriptor.getComponentType(), listTypeArguments[ 0 ] );
         this.requiredServiceType = pair.getKey();
 
         FilterBuilder filterBuilder = pair.getRight();
         for( Service.P property : serviceAnn.properties() )
         {
             filterBuilder.addEquals( property.key(), property.value() );
         }
         this.requiredFilter = filterBuilder.toString();
 
         try
         {
             BundleContext bundleContext = componentDescriptor.getModule().getBundle().getBundleContext();
             if( bundleContext == null )
             {
                 throw new IllegalStateException( "no bundle context for module " + componentDescriptor.getModule() );
             }
             Filter filter = FrameworkUtil.createFilter( this.requiredFilter );
             this.serviceTracker = new ServiceTracker( bundleContext, filter, this );
         }
         catch( InvalidSyntaxException e )
         {
             String msg = "field '" + field.getName() + "' of component " + componentDescriptor + " defines illegal filter: " + this.requiredFilter;
             throw new ComponentDefinitionException( msg, componentDescriptor.getComponentType(), componentDescriptor.getModule() );
         }
     }
 
     @Override
     public Object addingService( @Nonnull org.osgi.framework.ServiceReference reference )
     {
         BundleContext bundleContext = this.componentDescriptor.getModule().getBundle().getBundleContext();
         if( bundleContext == null )
         {
             throw new IllegalStateException( "no bundle context for module " + this.componentDescriptor.getModule() );
         }
         Object service = bundleContext.getService( reference );
         this.services = null;
         return service;
     }
 
     @Override
     public void modifiedService( @Nonnull org.osgi.framework.ServiceReference reference,
                                  @Nonnull Object service )
     {
         // no-op
     }
 
     @Override
     public void removedService( @Nonnull org.osgi.framework.ServiceReference reference,
                                 @Nonnull Object service )
     {
         this.services = null;
     }
 
     @Override
     public int size()
     {
         return getServices().size();
     }
 
     @Override
     public boolean isEmpty()
     {
         return getServices().isEmpty();
     }
 
     @Override
     public boolean contains( Object o )
     {
         return getServices().contains( o );
     }
 
     @Nonnull
     @Override
     public Iterator iterator()
     {
         return getServices().iterator();
     }
 
     @Nonnull
     @Override
     public Object[] toArray()
     {
         return getServices().toArray();
     }
 
     @Nonnull
     @Override
     public Object[] toArray( @Nonnull Object[] a )
     {
         return getServices().toArray( a );
     }
 
     @Override
     public boolean add( Object o )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean remove( Object o )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean containsAll( @Nonnull Collection c )
     {
         return getServices().containsAll( c );
     }
 
     @Override
     public boolean addAll( @Nonnull Collection c )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean addAll( int index, @Nonnull Collection c )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean removeAll( @Nonnull Collection c )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean retainAll( @Nonnull Collection c )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void clear()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Object get( int index )
     {
         return getServices().get( index );
     }
 
     @Override
     public Object set( int index, Object element )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void add( int index, Object element )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Object remove( int index )
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public int indexOf( Object o )
     {
         return getServices().indexOf( o );
     }
 
     @Override
     public int lastIndexOf( Object o )
     {
         return getServices().lastIndexOf( o );
     }
 
     @Nonnull
     @Override
     public ListIterator listIterator()
     {
         return getServices().listIterator();
     }
 
     @Nonnull
     @Override
     public ListIterator listIterator( int index )
     {
         return getServices().listIterator( index );
     }
 
     @Nonnull
     @Override
     public List subList( int fromIndex, int toIndex )
     {
         return getServices().subList( fromIndex, toIndex );
     }
 
     @Nonnull
     @Override
     public Module getConsumer()
     {
         return this.componentDescriptor.getModule();
     }
 
     @Nonnull
     @Override
     public Class<?> getType()
     {
         return this.requiredServiceType;
     }
 
     @Nullable
     @Override
     public String getFilter()
     {
         return this.requiredFilter;
     }
 
     @Nonnull
     @Override
     public List<ServiceReference<?>> getReferences()
     {
         org.osgi.framework.ServiceReference[] tracked = this.serviceTracker.getServiceReferences();
         if( tracked == null )
         {
             return Collections.emptyList();
         }
         else
         {
             List<ServiceReference<?>> serviceReferences = new LinkedList<>();
             for( org.osgi.framework.ServiceReference reference : tracked )
             {
                 serviceReferences.add( new ServiceReferenceImpl( reference ) );
             }
             return serviceReferences;
         }
     }
 
     @Nullable
     @Override
     protected String toStringInternal()
     {
         return "@Service '" + super.toStringInternal() + "'";
     }
 
     @Nonnull
     private List<?> getServices()
     {
         List services = this.services;
         if( services == null )
         {
            Object[] serviceArray = this.serviceTracker.getServices();
            services = serviceArray == null ? Collections.emptyList() : asList( serviceArray );
             this.services = services;
         }
         return services;
     }
 
     @Override
     protected synchronized void onAfterStart()
     {
         this.serviceTracker.open();
     }
 
     @Override
     protected synchronized void onBeforeStop()
     {
         this.serviceTracker.close();
     }
 
     @Override
     protected synchronized void onAfterDeactivate()
     {
         this.services = null;
     }
 
     @Nonnull
     @Override
     protected Object getValue()
     {
         return this;
     }
 
     private class ServiceReferenceImpl implements ServiceReference
     {
         @Nonnull
         private final org.osgi.framework.ServiceReference<?> reference;
 
         private ServiceReferenceImpl( @Nonnull org.osgi.framework.ServiceReference<?> reference )
         {
             this.reference = reference;
         }
 
         @Override
         public long getId()
         {
             return ( Long ) this.reference.getProperty( Constants.SERVICE_ID );
         }
 
         @Nonnull
         @Override
         public Class getType()
         {
             return ComponentFieldServiceListLifecycle.this.requiredServiceType;
         }
 
         @Nullable
         @Override
         public Module getProvider()
         {
             return Activator.getModuleManager().getModule( this.reference.getBundle().getBundleId() );
         }
 
         @Nonnull
         @Override
         public MapEx<String, Object> getProperties()
         {
             String[] propertyKeys = this.reference.getPropertyKeys();
 
             MapEx<String, Object> properties = new HashMapEx<>( propertyKeys.length );
             for( String key : propertyKeys )
             {
                 properties.put( key, this.reference.getProperty( key ) );
             }
             return properties;
         }
 
         @Nullable
         @Override
         public Object get()
         {
             return ComponentFieldServiceListLifecycle.this.serviceTracker.getService( this.reference );
         }
 
         @Nonnull
         @Override
         public Object require()
         {
             Object service = get();
             if( service != null )
             {
                 return service;
             }
             else
             {
                 String typeName = ComponentFieldServiceListLifecycle.this.requiredServiceType.getName();
                 throw new IllegalStateException( "service of type '" + typeName + "' is not available" );
             }
         }
     }
 }
