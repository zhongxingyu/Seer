 package org.eclipse.jst.jsf.ui.internal.project.facet;
 
 import org.eclipse.jst.common.project.facet.ui.libprov.FacetLibraryPropertyPage;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public final class JSFLibraryPropertyPage
 
     extends FacetLibraryPropertyPage
     
 {
     @Override
     public IProjectFacetVersion getProjectFacetVersion()
     {
         final IProjectFacet jsfFacet = ProjectFacetsManager.getProjectFacet( "jst.jsf" ); //$NON-NLS-1$
         final IFacetedProject fproj = getFacetedProject();
         return fproj.getInstalledVersion( jsfFacet );
     }
     
 }
