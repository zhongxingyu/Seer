 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package org.netbeans.modules.javafx.fxd.dataloader.fxz;
 
 import java.io.IOException;
 import java.io.InputStream;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.StyledDocument;
 import org.netbeans.core.spi.multiview.MultiViewDescription;
 import org.netbeans.core.spi.multiview.MultiViewFactory;
 import org.netbeans.modules.javafx.fxd.composer.archive.ArchiveViewDescription;
 import org.netbeans.modules.javafx.fxd.composer.preview.PreviewViewDescription;
 import org.netbeans.modules.javafx.fxd.composer.source.SourceViewDescription;
 import org.openide.cookies.EditCookie;
 import org.openide.cookies.EditorCookie;
 import org.openide.cookies.OpenCookie;
 import org.openide.cookies.SaveCookie;
 import org.openide.filesystems.FileLock;
 import org.openide.filesystems.FileObject;
 import org.openide.text.CloneableEditorSupport;
 import org.openide.text.DataEditorSupport;
 import org.openide.windows.CloneableTopComponent;
 import com.sun.javafx.tools.fxd.container.FXDContainer;
 import java.io.OutputStream;
 import java.io.Serializable;
 import javax.swing.SwingUtilities;
 import javax.swing.text.EditorKit;
 import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.javafx.fxd.composer.model.FXZArchive;
 import org.openide.util.Task;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author Pavel Benes
  */
 public final class FXZEditorSupport extends DataEditorSupport implements Serializable, OpenCookie, EditorCookie, EditCookie {
     private static final long  serialVersionUID = 1L;
         
     protected CloneableTopComponent    m_mvtc  = null;
     protected  MultiViewDescription [] m_views = null;
     
     public FXZEditorSupport( FXZDataObject dObj) {
         super(dObj, new FXDEnv(dObj));
     }
         
     @Override
     public StyledDocument openDocument() throws IOException {
         StyledDocument doc = super.openDocument();
         synchronized(this) {
             ((FXZDataObject) getDataObject()).getDataModel().getFXDContainer().documentOpened(doc);
         }
         return doc;
     }
     
     public void updateDisplayName() {
         final TopComponent tc = m_mvtc;
         if (tc == null) {
             return;
         }
         
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 //ProjectTypeInfo projectTypeInfo = ProjectTypeInfo.getProjectTypeInfoFor (IOSupport.getDataObjectContext (dataObject).getProjectType ());
                 //tc.setIcon (projectTypeInfo != null ? ImageUtilities.loadImage (projectTypeInfo.getIconResource ()) : null);
 
                 String displayName = messageName();
                 if (! displayName.equals(tc.getDisplayName()))
                     tc.setDisplayName(displayName);
                 tc.setToolTipText(getDataObject().getPrimaryFile().getPath());
             }
         });
     }
 
     @Override
     protected Task reloadDocument() {
         System.err.println("Reloading document.");
         final Task reloadTask = super.reloadDocument();
         Thread th = new Thread() {
             @Override
             public void run() {
                 reloadTask.waitFinished();
                 SwingUtilities.invokeLater( new Runnable() {
                     public void run() {
                         FXZDataObject dObj = (FXZDataObject) getDataObject();
                         dObj.getDataModel().getFXDContainer().incrementChangeTicker(false);
                         dObj.getController().reload();
                     }                    
                 });
             }
         };
         th.setName("ReloadDocument-Thread"); //NOI18N
         th.setPriority( Thread.MIN_PRIORITY);
         th.start();
         return reloadTask;
     }
     
     @Override
     protected boolean notifyModified() {
         boolean retValue = super.notifyModified();
         if ( retValue) {
             FXZDataObject dObj = (FXZDataObject) getDataObject();
             dObj.m_ic.add(env);
         }
         updateDisplayName();
         return retValue;
     }
     
     @Override
     protected void notifyUnmodified() {
         FXZDataObject dObj = (FXZDataObject) getDataObject();
         super.notifyUnmodified();
         dObj.m_ic.remove(env);
        FXZArchive fxz = dObj.getDataModel().getFXDContainer();
        if ( fxz != null && fxz.areEntriesChanged()) {
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                     notifyModified();
                 }
             });
         } else {
             updateDisplayName();
         }
     }
         
     @Override
     protected CloneableEditorSupport.Pane createPane() {
         MultiViewDescription [] views = getViewDescriptions();
         int                     defView = ((FXZDataObject) getDataObject()).getDefaultView();
         m_mvtc = MultiViewFactory.createCloneableMultiView(views, views[ defView]);
         return (CloneableEditorSupport.Pane)m_mvtc;
     }
     
     public CloneableTopComponent getMVTC() {
         return m_mvtc;
     }
     
     @Override
     public String messageHtmlName() {
         return super.messageHtmlName();
     }
 
     @Override
     public String messageName() {
         return super.messageName();        
     }
     
     FXDEnv getEnv() {
         return (FXDEnv) env;
     }
     
     protected synchronized MultiViewDescription [] getViewDescriptions() {
         if ( m_views == null) {
             m_views = new MultiViewDescription[] {
                 new PreviewViewDescription(this),        
                 new SourceViewDescription(this),
                 new ArchiveViewDescription(this)
             };
         }
         return m_views;
     }
         
     @Override
     protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
         throws IOException, BadLocationException {
         ((FXZDataObject)getDataObject()).getDataModel().getFXDContainer().save((BaseDocument)doc, kit, stream);
     }
     
     static final class FXDEnv extends DataEditorSupport.Env implements SaveCookie {
         private static final long  serialVersionUID = 1L;
         
         public FXDEnv( FXZDataObject obj) {
             super(obj);
         }
 
         public void save() throws IOException {
             FXZEditorSupport ed = (FXZEditorSupport)this.findCloneableOpenSupport();
             ed.saveDocument();
             FXZDataObject dObj = (FXZDataObject) getDataObject();
             dObj.getDataModel().getFXDContainer().setIsSaved();
         }
         
         @Override
         public InputStream inputStream() throws IOException {
             FXDContainer container = ((FXZDataObject)getDataObject()).getDataModel().getFXDContainer();
             return container.open();
         }        
                 
         @Override
         protected FileObject getFile() {
             return getDataObject().getPrimaryFile();
         }
 
         @Override
         protected FileLock takeLock() throws IOException {
             return ((FXZDataObject)getDataObject()).getPrimaryEntry().takeLock();
         }
     }
 }
