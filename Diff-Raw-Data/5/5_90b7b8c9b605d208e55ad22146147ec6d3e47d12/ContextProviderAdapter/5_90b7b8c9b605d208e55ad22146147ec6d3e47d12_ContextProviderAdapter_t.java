 package org.mosaic.web.handler.impl.adapter;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.lifecycle.MethodEndpoint;
 import org.mosaic.util.collect.HashMapEx;
 import org.mosaic.util.collect.MapEx;
 import org.mosaic.util.convert.ConversionService;
 import org.mosaic.util.expression.Expression;
 import org.mosaic.util.expression.ExpressionParser;
 import org.mosaic.util.reflection.MethodHandle;
 import org.mosaic.util.reflection.MethodParameter;
 import org.mosaic.web.handler.annotation.Context;
 import org.mosaic.web.handler.annotation.WebAppFilter;
 import org.mosaic.web.handler.impl.action.MethodEndpointWrapper;
 import org.mosaic.web.handler.impl.parameters.*;
 import org.mosaic.web.request.WebRequest;
 
 /**
  * @author arik
  */
 public class ContextProviderAdapter extends MethodEndpointWrapper implements Comparable<ContextProviderAdapter>
 {
     private final long id;
 
     private final int rank;
 
     @Nonnull
     private final String name;
 
     @Nonnull
     private final ConversionService conversionService;
 
     @Nullable
     private final Expression webAppFilterExpression;
 
     public ContextProviderAdapter( @Nonnull ConversionService conversionService,
                                    @Nonnull ExpressionParser expressionParser,
                                    long id,
                                    int rank,
                                    @Nonnull MethodEndpoint endpoint )
     {
         super( endpoint );
 
         this.id = id;
         this.rank = rank;
         this.conversionService = conversionService;
 
         Context ann = endpoint.requireAnnotation( Context.class );
         this.name = ann.value().isEmpty() ? endpoint.getName() : ann.value();
 
        WebAppFilter webAppFilterAnn = endpoint.getAnnotation( WebAppFilter.class, true, true );
         this.webAppFilterExpression = webAppFilterAnn != null ? expressionParser.parseExpression( webAppFilterAnn.value() ) : null;
 
         addParameterResolvers(
                 new CookieParameterResolver( this.conversionService ),
                 new HeaderParameterResolver( this.conversionService ),
                 new QueryValueParameterResolver( this.conversionService ),
                 new MethodHandle.ParameterResolver()
                 {
                     @Nullable
                     @Override
                     public Object resolve( @Nonnull MethodParameter parameter,
                                            @Nonnull MapEx<String, Object> resolveContext )
                             throws Exception
                     {
                         if( parameter.getType().isAssignableFrom( MapEx.class ) )
                         {
                             return resolveContext.require( "parameters" );
                         }
                         return SKIP;
                     }
                 },
                 new UserParameterResolver(),
                 new UriValueParameterResolver( this.conversionService ),
                 new WebApplicationParameterResolver(),
                 new WebDeviceParameterResolver(),
                 new WebPartParameterResolver(),
                 new WebRequestParameterResolver(),
                 new WebResponseParameterResolver(),
                 new WebSessionParameterResolver(),
                 new WebRequestUriParameterResolver(),
                 new WebRequestHeadersParameterResolver(),
                 new WebRequestBodyParameterResolver( this.conversionService )
         );
     }
 
     public final long getId()
     {
         return this.id;
     }
 
     public final int getRank()
     {
         return this.rank;
     }
 
     @Nonnull
     public String getName()
     {
         return this.name;
     }
 
     @Override
     public int compareTo( @Nullable ContextProviderAdapter o )
     {
         if( o == null )
         {
             return -1;
         }
         else if( getId() == o.getId() )
         {
             return 0;
         }
         else if( getRank() > o.getRank() )
         {
             return -1;
         }
         else if( getRank() < o.getRank() )
         {
             return 1;
         }
         else if( getId() < o.getId() )
         {
             return -1;
         }
         else if( getId() > o.getId() )
         {
             return 1;
         }
         throw new IllegalStateException( "should not happen" );
     }
 
     public boolean matches( @Nonnull WebRequest request )
     {
         if( this.webAppFilterExpression != null )
         {
             if( !this.webAppFilterExpression.createInvoker().withRoot( request ).expect( Boolean.class ).require() )
             {
                 return false;
             }
         }
         return true;
     }
 
     @Nullable
     public Object provide( @Nonnull MapEx<String, Object> context, @Nonnull MapEx<String, String> parameters )
             throws Exception
     {
         MapEx<String, Object> providerInvocationContext = new HashMapEx<>( context, this.conversionService );
         providerInvocationContext.put( "parameters", parameters );
        return invoke( providerInvocationContext );
     }
 }
