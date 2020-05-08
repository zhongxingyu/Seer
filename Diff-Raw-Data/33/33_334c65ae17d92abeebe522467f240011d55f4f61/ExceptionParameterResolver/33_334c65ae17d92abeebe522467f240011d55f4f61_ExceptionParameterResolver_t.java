 package org.mosaic.web.handler.impl.parameters;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.util.collect.MapEx;
import org.mosaic.util.convert.ConversionService;
 import org.mosaic.util.reflection.MethodParameter;
 
 /**
  * @author arik
  */
public class ExceptionParameterResolver extends AbstractWebParameterResolver
 {
    @Nonnull
    private final Class<? extends Throwable> exceptionType;

    public ExceptionParameterResolver( @Nullable ConversionService conversionService,
                                       @Nonnull Class<? extends Throwable> exceptionType )
    {
        super( conversionService );
        this.exceptionType = exceptionType;
    }

     @Nullable
     @Override
    public Object resolve( @Nonnull MethodParameter parameter, @Nonnull MapEx<String, Object> resolveContext )
             throws Exception
     {
        if( parameter.getType().isAssignableFrom( this.exceptionType ) )
        {
            return resolveContext.require( "throwable", Throwable.class );
        }
        return SKIP;
     }
 }
