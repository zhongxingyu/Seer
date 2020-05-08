 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 package org.netbeans.modules.javafx.fxd.composer.preview;
 
 import com.sun.javafx.geom.Bounds2D;
 import com.sun.javafx.geom.transform.BaseTransform;
 import com.sun.javafx.tk.Toolkit;
 import com.sun.javafx.tk.swing.SwingScene;
 import java.net.URL;
 
 import org.openide.util.NbBundle;
 
 import org.netbeans.modules.javafx.fxd.composer.misc.ActionLookup;
 import org.netbeans.modules.javafx.fxd.composer.misc.ActionLookupUtils;
 import org.netbeans.modules.javafx.fxd.composer.model.actions.AbstractFXDAction;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 import org.netbeans.modules.javafx.fxd.composer.model.*;
 
 import com.sun.javafx.tk.swing.SwingScene.SwingScenePanel;
 import com.sun.javafx.tools.fxd.PreviewLoader;
 import com.sun.javafx.tools.fxd.container.ContainerEntry;
 import com.sun.javafx.tools.fxd.loader.Profile;
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import javafx.fxd.FXDLoader;
 import javafx.geometry.Bounds;
 import javafx.scene.Node;
 import javafx.scene.Scene;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import org.netbeans.modules.javafx.fxd.composer.misc.FXDComposerUtils;
 import org.netbeans.modules.javafx.fxd.composer.model.actions.ActionController;
 import org.netbeans.modules.javafx.fxd.composer.model.actions.HighlightActionFactory;
 import org.netbeans.modules.javafx.fxd.composer.model.actions.SelectActionFactory;
 import org.openide.awt.MouseUtils;
 import org.openide.util.actions.Presenter;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author Pavel Benes
  */
 final class PreviewImagePanel extends JPanel implements ActionLookup {
     private static final float       ZOOM_STEP = (float) 1.1;
     
     private final FXZDataObject m_dObj;
     private final Action []     m_actions;
     private final Color         m_defaultBackground;
     //private       JSGPanel      m_sgPanel = null;
     //private       SwingScenePanel m_scenePanel = null;
     private       Scene         m_fxScene = null;
     private       int           m_changeTickerCopy = -1;
     private       Profile m_previewProfileCopy = null;
     private       String        m_selectedEntryCopy = null;
         
     PreviewImagePanel(final FXZDataObject dObj) {
         m_dObj = dObj;
     
         m_actions = new Action[] {
             new ZoomToFitAction(),
             new ZoomInAction(),
             new ZoomOutAction()
         };
         
         setLayout(new BorderLayout());
         m_defaultBackground = getBackground();                
         setBackground( Color.WHITE);
     }
 
     public Scene getScene(){
         return m_fxScene;
     }
     
     public SwingScenePanel getScenePanel() {
         //return m_scenePanel;
         return getScenePanel(m_fxScene);
     }
     
     private static SwingScenePanel getScenePanel(Scene scene) {
         //return m_scenePanel;
         return ((SwingScene) scene.$javafx$scene$Scene$impl_peer).scenePanel;
     }
 
 //    public JSGPanel getJSGPanel() {
 //        return m_sgPanel;
 //    }
 
     protected JLabel createWaitPanel() {
         URL url = PreviewImagePanel.class.getClassLoader().getResource("org/netbeans/modules/javafx/fxd/composer/resources/clock.gif"); //NOI18N
         ImageIcon icon = new ImageIcon( url);
         JLabel label = new JLabel( icon);
         label.setHorizontalTextPosition(JLabel.CENTER);
         label.setVerticalTextPosition( JLabel.BOTTOM);
         return label;        
     }
     
     synchronized void refresh() {
         FXZArchive fxzArchive = m_dObj.getDataModel().getFXDContainer(); 
         if (  fxzArchive != null) {
             final int tickerCopy = fxzArchive.getChangeTicker();
             final Profile profileCopy = m_dObj.getDataModel().getPreviewProfile();
             final String  selectedEntryCopy = m_dObj.getDataModel().getSelectedEntry();
             if ( tickerCopy != m_changeTickerCopy || 
                  profileCopy != m_previewProfileCopy ||
                  !FXDComposerUtils.safeEquals( selectedEntryCopy,m_selectedEntryCopy)) {
                 removeAll();
                 setBackground( Color.WHITE);
                 final JLabel label = createWaitPanel();
                 label.setText( NbBundle.getMessage( PreviewImagePanel.class, "LBL_PARSING")); //NOI18N            
 
                 add( label, BorderLayout.CENTER);
                 m_fxScene = null;
 
                 Thread th = new Thread() {
                     @Override
                     public void run() {
                         final FXZArchive fxz = m_dObj.getDataModel().getFXDContainer();
                         final FXDFileModel fModel = fxz.getFileModel(selectedEntryCopy);
                         fModel.updateModel();
 
                         m_changeTickerCopy = tickerCopy;
                         m_previewProfileCopy = profileCopy;
                         m_selectedEntryCopy = selectedEntryCopy;
 
                         SwingUtilities.invokeLater( new Runnable() {
                             public void run() {
                                 if ( fModel.isError()) {
                                     setBackground( m_defaultBackground);
                                     label.setIcon(null);
                                     label.setText( NbBundle.getMessage( PreviewImagePanel.class, "MSG_CANNOT_SHOW", //NOI18N
                                             fModel.getErrorMsg()));
                                     return;
                                 }
                                 
                                 label.setText( NbBundle.getMessage( PreviewImagePanel.class, "LBL_RENDERING")); //NOI18N
 
                                 Thread loadThread = new Thread(){
 
                                     @Override
                                     public void run() {
                                         try {
                                             final Node node;
                                             //final Node nodeOld;
                                             try {
                                                 fModel.readLock();
                                                 //nodeOld = PreviewLoader.load( fxz, selectedEntryCopy, profileCopy, new PreviewStatistics());
                                                 FXDLoader loader = PreviewLoader.loadOnBackground(ContainerEntry.create(fxz, selectedEntryCopy), null);
                                                 while (!loader.loc$done().getAsBoolean()) {
                                                     Thread.sleep(100);
                                                 }
                                                 node = loader.get$content().get$javafx$fxd$FXDContent$_root();
                                             } finally {
                                                 fModel.readUnlock();
                                             }
 
                                             showImagePanel(node);
 
                                         } catch( OutOfMemoryError oom) {
                                             oom.printStackTrace();
                                             setBackground( m_defaultBackground);
                                             label.setText( NbBundle.getMessage( PreviewImagePanel.class, "MSG_CANNOT_SHOW_OOM", //NOI18N
                                                 oom.getLocalizedMessage()));
                                             label.setIcon(null);
                                         } catch( Exception e) {
                                             e.printStackTrace();
                                             setBackground( m_defaultBackground);
                                             label.setText( NbBundle.getMessage( PreviewImagePanel.class, "MSG_CANNOT_SHOW", //NOI18N
                                                 e.getLocalizedMessage()));
                                             label.setIcon(null);
                                         } finally {
                                             System.gc();
                                         }
                                     }
 
                                 };
                                 loadThread.setName("FXDPreviewLoader-Thread");
                                 loadThread.start();
 
                             }
                         });                    
                     }
                 };
                 th.setName("ModelUpdate-Thread");  //NOI18N
                 th.start();            
             } else {
                 updateZoom();
             }
         } else {
             Exception error = m_dObj.getDataModel().getFXDContainerLoadError();
             showError( error.getLocalizedMessage());
         }
     }
 
     private void showImagePanel(final Node node) {
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 Scene fxScene = new Scene(true);
                 fxScene.addTriggers$();
                 fxScene.applyDefaults$();
                 //fxScene.loc$content.insert(nodeOld);
                 fxScene.loc$content.insert(node);
                 fxScene.complete$();
                 Toolkit.getToolkit().addSceneTkPulseListener(fxScene.get$javafx$scene$Scene$scenePulseListener());
 
                 m_fxScene = fxScene;
 
                 SwingScenePanel scenePanel = getScenePanel(fxScene);
 
                 removeAll();
                 add(new ImageHolder(scenePanel, m_dObj), BorderLayout.CENTER);
 
                 MouseEventCollector mec = new MouseEventCollector();
                 scenePanel.addMouseListener(mec);
                 scenePanel.addMouseMotionListener(mec);
                 scenePanel.addMouseWheelListener(mec);

                 PopupListener popupL = new PopupListener();
                 scenePanel.addMouseListener(popupL);
                 addMouseListener(popupL);
 
                 revalidate();
                 updateZoom();
             }
         });
     }
 
     private void showError( final String msg) {
         removeAll();
         setBackground( m_defaultBackground);
 
         JLabel label = new JLabel( 
             NbBundle.getMessage( PreviewImagePanel.class, "MSG_CANNOT_SHOW", msg), //NOI18N                                
             JLabel.CENTER);
         add( label, BorderLayout.CENTER);
     }
     
     private void updateZoom() {
         SwingScenePanel scenePanel = getScenePanel();
         if (scenePanel != null){
             float zoom = m_dObj.getDataModel().getZoomRatio();
             Node node = m_fxScene.impl_getRoot();
 
             Bounds2D bounds = new Bounds2D();
             node.getLocalBounds(bounds, BaseTransform.IDENTITY_TRANSFORM);
 
             node.set$scaleX(zoom);
             node.set$scaleY(zoom);
             float cx = (bounds.x1 + bounds.x2) / 2;
             float cy = (bounds.y1 + bounds.y2) / 2;
             node.set$translateX(cx * zoom - cx);
             node.set$translateY(cy * zoom - cy);
 
             scenePanel.invalidate();
             if (scenePanel.getParent() != null){
                 scenePanel.getParent().validate();
             }
         }
     }
 
     private Action[] getPopupActions() {
         ActionLookup lookup = ActionLookupUtils.merge(new ActionLookup[]{
                     PreviewImagePanel.this,
                     m_dObj.getController().getActionController()
                 });
         Action[] actions = new Action[]{
             lookup.get(SelectActionFactory.PreviousSelectionAction.class),
             lookup.get(SelectActionFactory.NextSelectionAction.class),
             lookup.get(SelectActionFactory.ParentSelectionAction.class),
             null,
             lookup.get(PreviewImagePanel.ZoomToFitAction.class),
             lookup.get(PreviewImagePanel.ZoomInAction.class),
             lookup.get(PreviewImagePanel.ZoomOutAction.class),
             null,
             lookup.get(HighlightActionFactory.ToggleTooltipAction.class),
             lookup.get(HighlightActionFactory.ToggleHighlightAction.class),
             null,
             lookup.get(ActionController.GenerateUIStubAction.class)
         };
         return actions;
     }
 
     final class ZoomToFitAction extends AbstractFXDAction {
         private static final long serialVersionUID = 2L;
 
         ZoomToFitAction() {
             super("zoom_fit"); //NOI18N
         }
 
         public void actionPerformed(ActionEvent e) {
             Bounds2D bounds = new Bounds2D();
             // TODO enable zoom
             //m_sgPanel.getScene().getCompleteBounds(bounds, null);
             
             Dimension panelSize = getParent().getSize();
             
             double xRatio = (panelSize.getWidth() - 2 * ImageHolder.CROSS_SIZE) / bounds.getWidth();
             double yRatio = (panelSize.getHeight() - 2 * ImageHolder.CROSS_SIZE) / bounds.getHeight();
             
             m_dObj.getController().setZoomRatio((float) Math.min( xRatio, yRatio));
         }
     }
     
     final class ZoomInAction extends AbstractFXDAction {
         private static final long serialVersionUID = 2L;
 
         ZoomInAction() {
             super("zoom_in"); //NOI18N
         }
 
         public void actionPerformed(ActionEvent e) {
             float zoom = m_dObj.getDataModel().getZoomRatio() * ZOOM_STEP;
             m_dObj.getController().setZoomRatio(zoom);
         }
     }
 
     final class ZoomOutAction extends AbstractFXDAction {
         private static final long serialVersionUID = 2L;
 
         ZoomOutAction() {
             super("zoom_out"); //NOI18N
         }
 
         public void actionPerformed(ActionEvent e) {
             float zoom = m_dObj.getDataModel().getZoomRatio() / ZOOM_STEP;
             m_dObj.getController().setZoomRatio(zoom);
         }
     }
     
     private final class MouseEventCollector implements MouseListener, MouseMotionListener, MouseWheelListener {
         public void mouseClicked(MouseEvent e) {
             processEvent(e);
         }
 
         public void mousePressed(MouseEvent e) {
             if (!e.isPopupTrigger()){
                 processEvent(e);
             }
         }
 
         public void mouseReleased(MouseEvent e) {
             if (!e.isPopupTrigger()){
                 processEvent(e);
             }
         }
 
         public void mouseEntered(MouseEvent e) {
             processEvent(e);
         }
 
         public void mouseExited(MouseEvent e) {
             processEvent(e);
             getStatusBar().setText(PreviewStatusBar.CELL_POSITION, "[-,-]");  //NOI18N
         }
 
         public void mouseDragged(MouseEvent e) {
             processEvent(e);
         }
 
         public void mouseMoved(MouseEvent e) {
             processEvent(e);
             float zoom = m_dObj.getDataModel().getZoomRatio();
             
             getStatusBar().setText( PreviewStatusBar.CELL_POSITION, String.format("[%d,%d]", Math.round(e.getX()/zoom), Math.round(e.getY()/zoom))); //NOI18N
         }
 
         public void mouseWheelMoved(MouseWheelEvent e) {
             if (e.getWheelRotation() > 0){
                 PreviewImagePanel.this.get(ZoomInAction.class).actionPerformed(null);
             } else {
                 PreviewImagePanel.this.get(ZoomOutAction.class).actionPerformed(null);
             }
             processEvent(e);
         }
 
         
         
         protected void processEvent( AWTEvent event) {
             m_dObj.getController().getActionController().processEvent(event);
         }
     }
 
     private final class PopupListener extends MouseUtils.PopupMouseAdapter {
 
         private JPopupMenu m_popup;
 
         public PopupListener() {
             TopComponent tc = m_dObj.getController().getPreviewComponent();
 
             Action[] actions = getPopupActions();
 
             m_popup = new JPopupMenu();
             for (int i = 0; i < actions.length; i++) {
                 if (actions[i] instanceof Presenter.Popup) {
                     m_popup.add(((Presenter.Popup) actions[i]).getPopupPresenter());
                 } else if (actions[i] == null){
                     m_popup.addSeparator();
                 } else {
                     m_popup.add(actions[i]);
                 }
                 if (actions[i] instanceof AbstractFXDAction) {
                     ((AbstractFXDAction) actions[i]).registerAction(tc);
                 }
             }
         }
 
         @Override
         protected void showPopup(MouseEvent e) {
             m_popup.show(e.getComponent(), e.getX(), e.getY());
         }
     }
 
     public Action get(final Class clazz) {
         return ActionLookupUtils.get(m_actions, clazz);
     }
 
     protected PreviewStatusBar getStatusBar() {
         return m_dObj.getController().getPreviewComponent().getStatusBar();
     }
 }
