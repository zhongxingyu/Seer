 package org.mosaic.util.weaving.impl;
 
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.LoaderClassPath;
 import javassist.NotFoundException;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.osgi.framework.hooks.weaving.WeavingException;
 import org.osgi.framework.hooks.weaving.WovenClass;
 import org.osgi.framework.wiring.BundleWiring;
 
 import static java.util.Arrays.asList;
 
 /**
  * @author arik
  */
 public class JavassistClassPoolManager
 {
     @Nonnull
     private static final List<String> FORBIDDEN_BUNDLES = asList(
            "api",
            "lifecycle"
     );
 
     /**
      * @todo replace with guava cache
      */
     @Nonnull
     private final Map<BundleWiring, ClassPool> classPools = new WeakHashMap<>( 500 );
 
     @Nullable
     public synchronized CtClass findCtClassFor( @Nonnull WovenClass wovenClass )
     {
         BundleWiring bundleWiring = wovenClass.getBundleWiring();
 
         // don't weave the system bundle or a no-weaving bundle
         if( !shouldWeaveBundle( bundleWiring ) )
         {
             return null;
         }
         else
         {
             try
             {
                 return getClassPool( bundleWiring ).get( wovenClass.getClassName() );
             }
             catch( NotFoundException e )
             {
                 throw new WeavingException( "Could not find weaving target class '" + wovenClass.getClassName() + "': " + e.getMessage(), e );
             }
         }
     }
 
     @Nullable
     public synchronized CtClass findCtClassFor( @Nonnull WovenClass wovenClass, @Nonnull String className )
     {
         BundleWiring bundleWiring = wovenClass.getBundleWiring();
 
         // don't weave the system bundle or a no-weaving bundle
         if( !shouldWeaveBundle( bundleWiring ) )
         {
             return null;
         }
         else
         {
             try
             {
                 return getClassPool( bundleWiring ).get( className );
             }
             catch( NotFoundException e )
             {
                 throw new WeavingException( "Could not find weaving target class '" + wovenClass.getClassName() + "': " + e.getMessage(), e );
             }
         }
     }
 
     private boolean shouldWeaveBundle( BundleWiring bundleWiring )
     {
         return bundleWiring.getBundle().getBundleId() != 0 && !FORBIDDEN_BUNDLES.contains( bundleWiring.getBundle().getSymbolicName() );
     }
 
     @Nonnull
     private ClassPool getClassPool( @Nonnull final BundleWiring bundleWiring )
     {
         ClassPool classPool = this.classPools.get( bundleWiring );
         if( classPool == null )
         {
             classPool = new ClassPool( false );
             classPool.appendClassPath( new LoaderClassPath( getClass().getClassLoader() ) );
             classPool.appendClassPath( new LoaderClassPath( bundleWiring.getClassLoader() ) );
             this.classPools.put( bundleWiring, classPool );
         }
         return classPool;
     }
 }
