 package org.mosaic.web.handler.impl.parameters;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.util.collect.MapEx;
 import org.mosaic.util.reflection.MethodParameter;
 
 /**
  * @author arik
  */
public class ExceptionParameterResolver extends CheckedParameterResolver<Throwable>
 {
     @Nullable
     @Override
    protected Throwable resolveToType( @Nonnull MethodParameter parameter, @Nonnull MapEx<String, Object> context )
             throws Exception
     {
        return context.require( "throwable", Throwable.class );
     }
 }
