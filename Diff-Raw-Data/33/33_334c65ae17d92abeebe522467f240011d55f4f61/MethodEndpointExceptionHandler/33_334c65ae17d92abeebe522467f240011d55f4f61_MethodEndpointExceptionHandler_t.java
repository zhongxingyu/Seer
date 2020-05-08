 package org.mosaic.web.handler.impl.action;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import java.util.concurrent.TimeUnit;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.lifecycle.MethodEndpoint;
 import org.mosaic.util.collect.MapEx;
 import org.mosaic.util.convert.ConversionService;
 import org.mosaic.util.reflection.MethodParameter;
 import org.mosaic.web.handler.impl.RequestExecutionPlan;
 import org.mosaic.web.handler.impl.parameters.*;
 import org.mosaic.web.request.WebRequest;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author arik
  */
 public class MethodEndpointExceptionHandler extends MethodEndpointWrapper implements ExceptionHandler
 {
     private static final Logger LOG = LoggerFactory.getLogger( MethodEndpointExceptionHandler.class );
 
     @Nullable
     private final Class<? extends Throwable> exceptionType;
 
     @Nonnull
     private final LoadingCache<Class<? extends Throwable>, Integer> distanceCache;
 
     public MethodEndpointExceptionHandler( @Nonnull MethodEndpoint endpoint,
                                            @Nonnull ConversionService conversionService )
     {
         super( endpoint );
 
         Class<? extends Throwable> exceptionType = null;
         for( MethodParameter parameter : endpoint.getParameters() )
         {
             Class<?> parameterType = parameter.getType().getRawType();
             if( Throwable.class.isAssignableFrom( parameterType ) )
             {
                 exceptionType = parameterType.asSubclass( Throwable.class );
                 break;
             }
         }
         if( exceptionType == null )
         {
             LOG.warn( "Exception handler {} has no exception parameter - it will not be activated on any error", endpoint );
             this.exceptionType = null;
         }
         else
         {
             this.exceptionType = exceptionType;
            addParameterResolvers( new ExceptionParameterResolver( conversionService, this.exceptionType ) );
         }
 
         this.distanceCache = CacheBuilder
                 .newBuilder()
                 .concurrencyLevel( 20 )
                 .expireAfterWrite( 5, TimeUnit.MINUTES )
                 .weakKeys()
                 .build( new CacheLoader<Class<? extends Throwable>, Integer>()
                 {
                     @Override
                     public Integer load( Class<? extends Throwable> key ) throws Exception
                     {
                         Class<? extends Throwable> supportedExceptionType = MethodEndpointExceptionHandler.this.exceptionType;
                         if( supportedExceptionType == null || !supportedExceptionType.isAssignableFrom( key ) )
                         {
                             return -1;
                         }
 
                         Class<?> currentClass = key;
                         int distance = 0;
                         while( currentClass != null && !currentClass.equals( supportedExceptionType ) )
                         {
                             distance++;
                             currentClass = currentClass.getSuperclass();
                         }
                         return distance;
                     }
                 } );
 
         addParameterResolvers(
                 new CookieParameterResolver( conversionService ),
                 new HeaderParameterResolver( conversionService ),
                 new QueryValueParameterResolver( conversionService ),
                 new WebApplicationParameterResolver(),
                 new WebDeviceParameterResolver(),
                 new WebPartParameterResolver(),
                 new WebRequestParameterResolver(),
                 new UserParameterResolver(),
                 new UriValueParameterResolver( conversionService ),
                 new WebResponseParameterResolver(),
                 new WebSessionParameterResolver(),
                 new WebRequestUriParameterResolver(),
                 new WebRequestHeadersParameterResolver(),
                 new WebRequestBodyParameterResolver( conversionService )
         );
     }
 
     @Override
     public int getDistance( @Nonnull Throwable throwable )
     {
         return this.distanceCache.getUnchecked( throwable.getClass() );
     }
 
     @Override
     public void apply( @Nonnull RequestExecutionPlan plan, @Nonnull MapEx<String, Object> context )
     {
         plan.addExceptionHandler( this, context );
     }
 
     @Nullable
     @Override
     public Object handle( @Nonnull WebRequest request,
                           @Nonnull Throwable throwable,
                           @Nonnull MapEx<String, Object> context ) throws Exception
     {
         context.put( "throwable", throwable );
         return invoke( context );
     }
 }
