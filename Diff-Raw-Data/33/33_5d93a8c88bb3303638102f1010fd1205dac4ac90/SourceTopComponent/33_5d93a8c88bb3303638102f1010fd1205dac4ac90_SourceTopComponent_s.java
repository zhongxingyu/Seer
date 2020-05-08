 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package org.netbeans.modules.javafx.fxd.composer.source;
 
 import javax.swing.Box;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 import javax.swing.text.Document;
 
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import org.netbeans.modules.javafx.fxd.composer.misc.FXDToolbar;
 import org.netbeans.modules.javafx.fxd.composer.navigator.SelectionCookie;
 import org.netbeans.modules.javafx.fxd.composer.preview.PreviewToolbar;
 import org.netbeans.modules.javafx.fxd.dataloader.FXDZDataObject;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZEditorSupport;
 import org.openide.cookies.EditCookie;
 import org.openide.cookies.EditorCookie;
 import org.openide.cookies.OpenCookie;
 import org.openide.text.CloneableEditor;
 import org.openide.text.NbDocument;
 import org.openide.util.Lookup;
 import org.openide.util.Mutex;
 import org.openide.util.lookup.Lookups;
 import org.openide.util.lookup.ProxyLookup;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author Pavel Benes
  */
 public final class SourceTopComponent extends CloneableEditor implements Runnable  {    
     private transient JToolBar  m_toolbar = null;
     private transient JComboBox m_entryCombo = null;
     
     public SourceTopComponent( FXZDataObject dObj) {
         super( dObj.getEditorSupport());
         //System.out.println("*** Creating TC for " + getEditorSupport().m_entryName);
     }
     
     public FXZEditorSupport getEditorSupport() {
         return (FXZEditorSupport) cloneableEditorSupport();
     }
     
     public synchronized JToolBar getToolbar() {
         if (m_toolbar == null) {
             //System.out.println("*** Creating toolbar for " + getEditorSupport().m_entryName);
             final JEditorPane editorPane = getEditorPane();
             if (editorPane!= null) {
                 final Document doc = editorPane.getDocument();
                 if (doc instanceof NbDocument.CustomToolbar) {
                     m_toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(editorPane);                    
                     m_entryCombo = PreviewToolbar.createEntryCombo( getDataObject(), "Source-" + getEditorSupport().m_entryName);      
                     
                     m_toolbar.add( Box.createHorizontalStrut(3), 0);
                     m_toolbar.add( PreviewToolbar.createToolBarSeparator(), 1);
                     m_toolbar.add( Box.createHorizontalStrut(5), 2);
                     m_toolbar.add( FXDToolbar.createLabel("Scene:"), 3);
                    m_toolbar.add( Box.createHorizontalStrut(2), 4);
                     FXDToolbar.addCombo(m_toolbar, m_entryCombo, 5, false);
                 }
             }
             if (m_toolbar == null) {
                 // attempt to create own toolbar??
                 m_toolbar = new javax.swing.JToolBar();
             }
         }
         
         return m_toolbar;
     }
             
     public void refresh() {
         if ( m_entryCombo != null) {
             PreviewToolbar.updateEntryCombo( getDataObject(), m_entryCombo);
         }
     }
     
     @Override
     public void updateName() {
         Mutex.EVENT.readAccess(this);
     }
 
     public void run() {
         getDataObject().updateTCName();
     }
 
     protected FXZDataObject getDataObject() {
         return (FXZDataObject) ((FXZEditorSupport) cloneableEditorSupport()).getDataObject();
     }
     
     @Override
     public Lookup getLookup() {
         return new ProxyLookup(new org.openide.util.Lookup[] {                
             getDataObject().getNodeDelegate().getLookup(),
             Lookups.singleton( new SelectionCookie() {
                 public void updateSelection(FXDZDataObject doj, DocumentElement de, boolean doubleClick) {
                     selectElement(doj, de.getStartOffset(), doubleClick);
                 }
             })
         });
     }    
     
     @Override    
     public void componentOpened() {
         getDataObject().init();            
         super.componentOpened();
         if ( getDataObject().getDataModel().getFXDContainer() == null) {
             getEditorPane().setEnabled(false);
         }
     }
 
     @Override
     public void componentClosed() {
         super.componentClosed();
         getDataObject().reset();
     }
 
     @Override
     public void componentShowing() {
         super.componentShowing();
         refresh();
     }
     
     @Override
     public void componentHidden() {
         super.componentHidden();
     }
 
     @Override
     public void componentActivated() {
         super.componentActivated();
     }
 
     @Override
     public void componentDeactivated() {
         super.componentDeactivated();
     }
     
     public static void selectElement( final FXDZDataObject dDoj, int startOffset, final boolean requestFocus) {
         if ( startOffset != -1) {
             selectPosition(dDoj, startOffset, requestFocus);
         }
     }
     
     public static void selectPosition( final FXDZDataObject dObj, final int position, final boolean requestFocus) {
         if ( requestFocus) {
             openFileInEditor(dObj);
         }
 
         SwingUtilities.invokeLater( new Runnable() {
             @SuppressWarnings({"deprecation"})
             public void run() {
                 EditorCookie ed = (EditorCookie) dObj.getEditorSupport();
                 try {
                     if (ed != null) {
                         ed.openDocument();
                         JEditorPane [] opened = ed.getOpenedPanes();
                         if ( opened != null && opened.length > 0) {
 
                             final JEditorPane  pane = opened[0];
                             //m_callback.requestVisible();
                             if (position >= 0) {
                                 pane.setSelectionStart(position);
                                 pane.setSelectionEnd(position);
                             }
                             
                             if ( requestFocus) {
                                 //System.err.println("Requesting focus ...");
                                 //m_callback.requestActive();
                                 TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass( TopComponent.class, pane);
                                 if (tc != null) {
                                     tc.requestActive();
                                     // the requestActive itself does not work
                                     //tc.requestFocus();
                                 }
                                 pane.requestFocusInWindow();
                             }
                         }
                     } else {
                         System.err.println("Editor cookie not found!");
                     }            
                 } catch( Exception e) {
                     e.printStackTrace();
                     //SceneManager.error("Select in editor failed.", e); //NOI18N
                 }
             }            
         });        
     }    
         
     private static boolean openFileInEditor(FXDZDataObject dObj) {  
         if ( dObj instanceof FXZDataObject) {
             ((FXZDataObject)dObj).selectView( FXZDataObject.TEXT_VIEW_INDEX);
         }
         
         EditCookie ck = (EditCookie) dObj.getEditorSupport();
         if (ck != null) {
             //System.err.println("Edit cookie found, editing ...");
             ck.edit();
             return true;
         }
 
         OpenCookie oc = (OpenCookie) dObj.getEditorSupport();
         if (oc != null) {
             //System.err.println("Open cookie found, opening ...");
             oc.open();
             return true;
         }
         
         return false;
     }        
 }
