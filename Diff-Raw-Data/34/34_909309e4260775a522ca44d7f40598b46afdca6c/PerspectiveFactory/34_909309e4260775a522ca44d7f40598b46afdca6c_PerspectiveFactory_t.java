 package org.amanzi.awe;
 
 import net.refractions.udig.internal.ui.MapPerspective;
 
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 public class PerspectiveFactory implements IPerspectiveFactory {
    public static final String AWE_PERSPECTIVE = "org.amanzi.awe.perspective"; //$NON-NLS-1$
	private static final String BOOKMARKS = "org.tcat.citd.sim.udig.bookmarks.internal.ui.BookmarksView"; //$NON-NLS-1$
    private static final String PROJECTS = "net.refractions.udig.project.ui.projectExplorer"; //$NON-NLS-1$
    private static final String LAYERS = "net.refractions.udig.project.ui.layerManager"; //$NON-NLS-1$
    private static final String CATALOG = "net.refractions.udig.catalog.ui.CatalogView"; //$NON-NLS-1$
 
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
         
        layout.addView(PROJECTS, IPageLayout.LEFT, 0.25f, editorArea);
        layout.addView(LAYERS, IPageLayout.BOTTOM, 0.25f, PROJECTS);
        layout.addView(CATALOG, IPageLayout.BOTTOM, 0.65f, editorArea);
        //layout.addView(BOOKMARKS, IPageLayout.RIGHT, 0.5f, CATALOG);
 
        layout.addPerspectiveShortcut(AWE_PERSPECTIVE);
         layout.addPerspectiveShortcut(MapPerspective.ID_PERSPECTIVE);
     }
 
 //	public void createInitialLayout(IPageLayout layout) {
 //		layout.addFastView(PROJECTS);
//		layout.addView(LAYERS, IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);
 //		layout.addView(BOOKMARKS, IPageLayout.BOTTOM, 0.7f, LAYERS);
 //	}
 
 }
