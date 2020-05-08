 package org.mosaic.filewatch.impl.manager;
 
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.nio.file.attribute.DosFileAttributes;
 import java.nio.file.attribute.PosixFileAttributes;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.filewatch.WatchEvent;
 import org.mosaic.lifecycle.annotation.Root;
 import org.mosaic.util.collect.MapEx;
 import org.mosaic.util.reflection.MethodHandle;
 import org.mosaic.util.reflection.MethodParameter;
 
 /**
  * @author arik
  */
 public class WatcherParameterResolver implements MethodHandle.ParameterResolver
 {
     @Nullable
     @Override
     public Object resolve( @Nonnull MethodParameter parameter,
                            @Nonnull MapEx<String, Object> resolveContext )
     {
         if( parameter.getType().isAssignableFrom( Path.class ) )
         {
             if( parameter.getAnnotation( Root.class ) != null )
             {
                 return resolveContext.get( "root", Path.class );
             }
             else
             {
                 return resolveContext.get( "path", Path.class );
             }
         }
         else if( parameter.getType().isAssignableFrom( WatchEvent.class ) )
         {
             return resolveContext.get( "event", WatchEvent.class );
         }
        else if( parameter.getType().isAssignableFrom( BasicFileAttributes.class ) )
        {
            return resolveContext.get( "attrs", BasicFileAttributes.class );
        }
         else if( parameter.getType().isAssignableFrom( DosFileAttributes.class ) )
         {
             return resolveContext.get( "attrs", DosFileAttributes.class );
         }
         else if( parameter.getType().isAssignableFrom( PosixFileAttributes.class ) )
         {
             return resolveContext.get( "attrs", PosixFileAttributes.class );
         }
         else
         {
             return SKIP;
         }
     }
 }
