 package com.ifedorenko.m2e.nexusdev.internal.launch;
 
 import static com.ifedorenko.m2e.nexusdev.internal.launch.NexusExternalLaunchDelegate.ATTR_SELECTED_PROJECTS;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.m2e.core.project.IMavenProjectFacade;
 
 public class SelectedProjects
 {
     private boolean selectAll;
 
     private final Set<String> selectedProjects;
 
     private SelectedProjects( Set<String> selectedProjects )
     {
         this.selectAll = selectedProjects == null;
         this.selectedProjects =
             selectedProjects != null ? new HashSet<String>( selectedProjects ) : new HashSet<String>();
     }
 
     public SelectedProjects( IProject project )
     {
         this( Collections.singleton( project.getName() ) );
     }
 
     public SelectedProjects()
     {
         this( Collections.<String> emptySet() );
     }
 
     public boolean isSelected( IMavenProjectFacade facade )
     {
         return selectAll || selectedProjects == null || selectedProjects.contains( facade.getProject().getName() );
     }
 
     public void setSelectAll( boolean select )
     {
         this.selectAll = select;
     }
 
     public boolean isSelectAll()
     {
         return selectAll;
     }
 
     public void setSelect( IMavenProjectFacade facade, boolean select )
     {
         final String name = facade.getProject().getName();
         if ( select )
         {
             selectedProjects.add( name );
         }
         else
         {
             selectedProjects.remove( name );
         }
     }
 
    @SuppressWarnings( { "unchecked", "rawtypes" } )
     public static SelectedProjects fromLaunchConfig( ILaunchConfiguration config )
     {
         Set<String> selectedProjects;
         try
         {
            selectedProjects = config.getAttribute( ATTR_SELECTED_PROJECTS, (Set) null );
         }
         catch ( CoreException e )
         {
             selectedProjects = null;
         }
         return new SelectedProjects( selectedProjects );
     }
 
     public void toLaunchConfig( ILaunchConfigurationWorkingCopy config )
     {
         if ( selectAll )
         {
             config.removeAttribute( ATTR_SELECTED_PROJECTS );
         }
         else
         {
             config.setAttribute( ATTR_SELECTED_PROJECTS, selectedProjects );
         }
     }
 }
