 package org.qi4j.chronos.ui.wicket.authentication;
 
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.bootstrap.LayerAssembly;
 import org.qi4j.bootstrap.ModuleAssembly;
import static org.qi4j.chronos.ui.wicket.authentication.Constants.MODULE_NAME_WICKET_AUTHENTICATION;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.spi.structure.Visibility;
 
 /**
  * {@code WicketAuthenticationModuleInitializer} initialize wicket authentication module.
  *
  * @author edward.yakop@gmail.com
  * @since 0.1.0
  */
 public final class WicketAuthenticationModuleInitializer
 {
     private WicketAuthenticationModuleInitializer()
     {
     }
 
     /**
      * Add The specified wicket authentication module to the specified {@code aLayerAssembly}.
      *
      * @param aLayerAssembly The layer assembly. This argument must not be {@code null}.
      * @throws IllegalArgumentException Thrown if the specified {@code aLayerAssembly} is {@code null}.
      * @since 0.1.0
      */
     public static void addWicketAuthenticationModule( LayerAssembly aLayerAssembly )
         throws IllegalArgumentException, AssemblyException
 
     {
         validateNotNull( "aModuleAssembly", aLayerAssembly );
 
         ModuleAssembly moduleAssembly = aLayerAssembly.newModuleAssembly();
         moduleAssembly.setName( MODULE_NAME_WICKET_AUTHENTICATION );
         moduleAssembly.addComposites( RoleHelperComposite.class ).visibleIn( Visibility.module );
     }
 }
