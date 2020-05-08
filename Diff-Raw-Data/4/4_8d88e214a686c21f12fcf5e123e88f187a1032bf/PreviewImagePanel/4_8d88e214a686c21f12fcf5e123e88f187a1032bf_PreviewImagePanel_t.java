 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 package org.netbeans.modules.javafx.fxd.composer.preview;
 
 import com.sun.javafx.geom.AffineTransform;
 import com.sun.javafx.geom.Bounds2D;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.net.URL;
 
 import org.openide.util.NbBundle;
 
 import org.netbeans.modules.javafx.fxd.composer.misc.ActionLookup;
 import org.netbeans.modules.javafx.fxd.composer.misc.ActionLookupUtils;
 import org.netbeans.modules.javafx.fxd.composer.model.actions.AbstractFXDAction;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 import org.netbeans.modules.javafx.fxd.composer.model.*;
 
 import javafx.scene.Node;
 import com.sun.scenario.scenegraph.SGNode;
 import com.sun.scenario.scenegraph.JSGPanel;
 import com.sun.javafx.sg.PGNode;
 
 import com.sun.javafx.tools.fxd.*;
 import com.sun.scenario.scenegraph.SGGroup;
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
     private       JSGPanel      m_sgPanel = null;
     private       int           m_changeTickerCopy = -1;
     private       TargetProfile m_previewProfileCopy = null;
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
     
     public JSGPanel getJSGPanel() {
         return m_sgPanel;
     }
     
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
             final TargetProfile profileCopy = m_dObj.getDataModel().getPreviewProfile();
             final String  selectedEntryCopy = m_dObj.getDataModel().getSelectedEntry();
             if ( tickerCopy != m_changeTickerCopy || 
                  profileCopy != m_previewProfileCopy ||
                  !FXDComposerUtils.safeEquals( selectedEntryCopy,m_selectedEntryCopy)) {
                 removeAll();
                 setBackground( Color.WHITE);
                 final JLabel label = createWaitPanel();
                 label.setText( NbBundle.getMessage( PreviewImagePanel.class, "LBL_PARSING")); //NOI18N            
 
                 add( label, BorderLayout.CENTER);
                 m_sgPanel  = null;
 
                 Thread th = new Thread() {
                     @Override
                     public void run() {
                         final FXZArchive fxz = m_dObj.getDataModel().getFXDContainer();
                         final FXDFileModel fModel = fxz.getFileModel(selectedEntryCopy);
                         fModel.updateModel();
 
                         m_changeTickerCopy = tickerCopy;
                         m_previewProfileCopy = profileCopy;
                         m_selectedEntryCopy = selectedEntryCopy;
 
                         //DocumentModelUtils.dumpElementStructure( fxz.getFileModel().getDocumentModel().getRootElement());
                         
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
                                 SwingUtilities.invokeLater( new Runnable() {
                                     public void run() {
                                         try {
                                             Node node;
                                             try {
                                                 fModel.readLock();
                                                 PreviewStatistics stats = new PreviewStatistics();
                                                 //System.out.println("Selected entry: " + selectedEntryCopy);
 
                                                 node = PreviewLoader.load( fxz, selectedEntryCopy, profileCopy, stats);
                                             } finally {
                                                 fModel.readUnlock();
                                             }
                                             //Method   m      = fxNode.getClass().getDeclaredMethod("getSGGroup");
                                             //Object   group  = m.invoke(fxNode);
                                             PGNode fxNode = node.impl_getPGNode();
 
                                             if (fxNode != null) {
                                                 m_sgPanel = new JSGPanel() {
                                                     @Override
                                                     public void paintComponent(java.awt.Graphics g) {
                                                         super.paintComponent(g);
                                                         m_dObj.getController().paintActions(g);
                                                     }
                                                 };
                                                 m_sgPanel.setBackground(Color.WHITE);
                                                 m_sgPanel.setScene( (SGNode) fxNode);
 
                                                 removeAll();
                                                 add( new ImageHolder(m_sgPanel), BorderLayout.CENTER);
 
                                                 MouseEventCollector mec = new MouseEventCollector();
                                                 m_sgPanel.addMouseListener(mec);
                                                 m_sgPanel.addMouseMotionListener(mec);
                                                 m_sgPanel.addMouseWheelListener(mec);
 
                                                 PopupListener popupL = new PopupListener();
                                                 m_sgPanel.addMouseListener(popupL);
                                                 PreviewImagePanel.this.addMouseListener(popupL);
 
                                                 updateZoom();
                                             } else {
                                                 setBackground( m_defaultBackground);
                                                 label.setText( NbBundle.getMessage( PreviewImagePanel.class, "MSG_EMPTY_DOCUMENT")); //NOI18N
                                                 label.setIcon(null);
                                             }
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
                                 });                            
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
     
     private void showError( final String msg) {
         removeAll();
         setBackground( m_defaultBackground);
 
         JLabel label = new JLabel( 
             NbBundle.getMessage( PreviewImagePanel.class, "MSG_CANNOT_SHOW", msg), //NOI18N                                
             JLabel.CENTER);
         add( label, BorderLayout.CENTER);
     }
     
     private void updateZoom() {
         if ( m_sgPanel != null) {
             float zoom = m_dObj.getDataModel().getZoomRatio();
             SGGroup node = m_sgPanel.getSceneGroup();
 //            fxNode.setTranslateX( 0);
 //            fxNode.setTranslateY( 0);
 //            fxNode.setScaleX(zoom);
 //            fxNode.setScaleY(zoom);
 //            Rectangle2D bounds = fxNode.getTransformedBounds();
 //
 //            fxNode.setTranslateX( (float) -bounds.getX());
 //            fxNode.setTranslateY( (float) -bounds.getY());
 //
 //            m_sgPanel.invalidate();
 
 //            AffineTransform at = new AffineTransform();
 //            at.scale( zoom, zoom);
 
 //            at.translate(-bounds.getX(), -bounds.getY());
 //            fxNode.setTransform(at);
             AffineTransform at = new AffineTransform();
             at.scale( zoom, zoom);
             node.setTransformMatrix(at);
 
             m_sgPanel.invalidate();
            if (m_sgPanel.getParent() != null) {
                m_sgPanel.getParent().validate();
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
             m_sgPanel.getScene().getCompleteBounds(bounds, null);
             
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
