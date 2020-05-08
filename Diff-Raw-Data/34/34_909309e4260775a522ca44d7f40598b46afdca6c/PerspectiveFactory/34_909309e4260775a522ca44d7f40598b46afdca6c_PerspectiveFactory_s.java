 package org.amanzi.awe;
 
 import net.refractions.udig.internal.ui.MapPerspective;
 
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 public class PerspectiveFactory implements IPerspectiveFactory {
	private static final String BOOKMARKS = 
		"org.tcat.citd.sim.udig.bookmarks.internal.ui.BookmarksView";
	private static final String PROJECTS = 
		"net.refractions.udig.project.ui.projectExplorer";
	private static final String LAYERS = 
		"net.refractions.udig.project.ui.layerManager";
 
     /** 
      * Creates the initial layout for a page.
      * <p>
      * Implementors of this method may add additional views to a
      * perspective.  The perspective already contains an editor folder
      * identified by the result of <code>IPageLayout.getEditorArea()</code>.  
      * Additional views should be added to the layout using this value as 
      * the initial point of reference.  
      * </p>
      *
      * @param layout the page layout
      */
     public void createInitialLayout(IPageLayout layout) {
         // Get the editor area.
         String editorArea = layout.getEditorArea();
         
        layout.addView( "net.refractions.udig.project.ui.projectExplorer", IPageLayout.LEFT, 0.25f, editorArea ); //$NON-NLS-1$
        layout.addView( "net.refractions.udig.project.ui.layerManager", IPageLayout.BOTTOM, 0.25f, //$NON-NLS-1$
                "net.refractions.udig.project.ui.projectExplorer" ); //$NON-NLS-1$
      
        layout.addView("net.refractions.udig.catalog.ui.CatalogView", IPageLayout.BOTTOM, 0.65f, editorArea);         //$NON-NLS-1$
 
         layout.addPerspectiveShortcut(MapPerspective.ID_PERSPECTIVE);
     }
 
 //	public void createInitialLayout(IPageLayout layout) {
 //		layout.addFastView(PROJECTS);
//		layout.addView(LAYERS, IPageLayout.LEFT, 0.3f,
//		        IPageLayout.ID_EDITOR_AREA);
 //		layout.addView(BOOKMARKS, IPageLayout.BOTTOM, 0.7f, LAYERS);
 //	}
 
 }
