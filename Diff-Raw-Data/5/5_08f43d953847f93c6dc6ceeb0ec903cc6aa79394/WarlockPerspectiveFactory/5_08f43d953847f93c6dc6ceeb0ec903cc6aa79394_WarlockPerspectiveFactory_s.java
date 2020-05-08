 /*
  * Created on Dec 30, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package cc.warlock.rcp.application;
 
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.IPlaceholderFolderLayout;
 
 import cc.warlock.rcp.views.ConnectionView;
 import cc.warlock.rcp.views.DebugView;
 
 /**
  * @author Marshall
  */
 public class WarlockPerspectiveFactory implements IPerspectiveFactory {
 
 	private static IPageLayout myLayout = null;
 	
 	public static final String WARLOCK_PERSPECTIVE_ID = "cc.warlock.warlockPerspective";
 	public static final String BOTTOM_FOLDER_ID = "cc.warlock.bottomFolder";
 	public static final String TOP_FOLDER_ID = "cc.warlock.topFolder";
 	public static final String RIGHT_FOLDER_ID = "cc.warlock.rightFolder";
 	public static final String LEFT_FOLDER_ID = "cc.warlock.leftFolder";
 	public static final String MAIN_FOLDER_ID = "cc.warlock.mainFolder";
 	
 	public static IPageLayout getLayout() {
 		return myLayout;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
 	 */
 	public void createInitialLayout(IPageLayout layout) {
 		myLayout = layout;
 		
 		layout.setEditorAreaVisible(false);
 		IFolderLayout mainFolder = layout.createFolder(MAIN_FOLDER_ID, IPageLayout.BOTTOM, 0.15f, layout.getEditorArea());
 		mainFolder.addView(ConnectionView.VIEW_ID);
 		
 		mainFolder.addPlaceholder("*GameView:*");
 
 //		layout.addStandaloneView(HandsView.VIEW_ID, false, IPageLayout.TOP, 0.05f, GameView.VIEW_ID);
 //		layout.addStandaloneView(StatusView.VIEW_ID, false, IPageLayout.RIGHT, .5f, HandsView.VIEW_ID);
 		
 		IPlaceholderFolderLayout topFolder =
 			layout.createPlaceholderFolder(TOP_FOLDER_ID, IPageLayout.TOP, 0.15f, MAIN_FOLDER_ID);
 		topFolder.addPlaceholder("*topView*");
 		
 //		topFolder.addPlaceholder(StreamView.DEATH_VIEW_ID);
 //		topFolder.addPlaceholder(StreamView.THOUGHTS_VIEW_ID);
 		
 		IPlaceholderFolderLayout rightFolder =
 			layout.createPlaceholderFolder(RIGHT_FOLDER_ID, IPageLayout.RIGHT, 0.75f, MAIN_FOLDER_ID);
 		rightFolder.addPlaceholder("*rightView*");
 		
 //		rightFolder.addPlaceholder(StreamView.INVENTORY_VIEW_ID);
 		rightFolder.addPlaceholder(DebugView.VIEW_ID);
 		
 		IPlaceholderFolderLayout bottomFolder =
 			layout.createPlaceholderFolder(BOTTOM_FOLDER_ID, IPageLayout.BOTTOM, 0.95f, MAIN_FOLDER_ID);
 		bottomFolder.addPlaceholder("*bottomView*");
 		
 //		IFolderLayout folder = layout.createFolder(BOTTOM_FOLDER_ID, IPageLayout.BOTTOM, 0.90f, GameView.VIEW_ID);
 //		layout.addStandaloneView(BarsView.VIEW_ID, false, IPageLayout.BOTTOM, 0.95f, GameView.VIEW_ID);
 //		folder.addView("org.eclipse.pde.runtime.LogView");
 		
 //		layout.addView(CompassView.VIEW_ID, IPageLayout.RIGHT, 0.85f, BarsView.VIEW_ID);
 	}
 
 }
