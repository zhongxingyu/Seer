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
 import com.sun.javafx.tools.fxd.container.FXDContainer;
 import java.beans.PropertyChangeListener;
 import java.beans.VetoableChangeListener;
 import java.io.OutputStream;
 import java.io.Serializable;
 import javax.swing.SwingUtilities;
 import javax.swing.text.EditorKit;
 import org.netbeans.core.spi.multiview.CloseOperationHandler;
 import org.netbeans.core.spi.multiview.CloseOperationState;
 import org.netbeans.modules.javafx.fxd.composer.model.FXDFileModel;
 import org.openide.DialogDisplayer;
 import org.openide.NotifyDescriptor;
 import org.openide.awt.UndoRedo.Manager;
 import org.openide.util.Task;
 import org.openide.util.UserQuestionException;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author Pavel Benes
  */
 public final class FXZEditorSupport extends DataEditorSupport implements OpenCookie, EditorCookie, EditorCookie.Observable, EditCookie {
     public final String m_entryName;
 
     public FXZEditorSupport( FXZDataObject dObj, String entryName, boolean isBase) {
         super(dObj, new FXDEnv(dObj, entryName));
         m_entryName = entryName;
         
         if ( !isBase) {
             FXZEditorSupport baseEditorSupport = dObj.getBaseSupport();
             allEditors = baseEditorSupport.allEditors;
             // attach property change listener to be informed about loosing validity
             env.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange( (PropertyChangeListener) allEditors, env));
 
             // attach vetoable change listener to be cancel loosing validity when modified
             env.addVetoableChangeListener(org.openide.util.WeakListeners.vetoableChange(  (VetoableChangeListener) allEditors,env));              
         }
         
 //        FXZListener l = new FXZListener((Env) env);
 //        allEditors = l;
 //
 //        // attach property change listener to be informed about loosing validity
 //        env.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(l, env));
 //
 //        // attach vetoable change listener to be cancel loosing validity when modified
 //        env.addVetoableChangeListener(org.openide.util.WeakListeners.vetoableChange(l, env));        
     }
       
 //    @Override
 //    public void open() {
 //        FXZEditorSupport base = getFXZDataObject().getBaseSupport();
 //        if ( base == this) {
 //            super.open();
 //        } else {
 //            System.out.println("Trying to open entry: " + m_entryName);
 //        }
 //    }
     
     @Override
     public StyledDocument openDocument() throws IOException {
         return openDocument(true);
     }
 
     public StyledDocument openDocument( boolean changeSelection) throws IOException {
         FXZDataObject dObj = getFXZDataObject();
         if ( changeSelection) {
             String entryName = dObj.getEntryName();
             if ( !entryName.equals( m_entryName)) {
                 dObj.selectEntry(m_entryName);
             } 
         }
         return super.openDocument();
     }
 
     public Manager getUndoRedoManager() {
         return getUndoRedo();
     }
     
     @Override
     protected StyledDocument createStyledDocument(EditorKit kit) {
         StyledDocument sd = super.createStyledDocument(kit);
         FXDFileModel.setEntryPrefixForDocument(sd,FXDFileModel.createIdPrefix(m_entryName));
         return sd;
     }
     
     public void updateDisplayName() {
         final TopComponent tc = getFXZDataObject().getMVTC();
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
    protected boolean asynchronousOpen() {
        return false;
    }

    @Override
     protected Task reloadDocument() {
         final FXZDataObject fxzDO = getFXZDataObject();
         fxzDO.getDataModel().getFXDContainer().setDirty();
         final Task reloadTask = super.reloadDocument();
         Thread th = new Thread() {
             @Override
             public void run() {
                 reloadTask.waitFinished();
                 SwingUtilities.invokeLater( new Runnable() {
                     public void run() {
                         fxzDO.getDataModel().getFXDContainer().incrementChangeTicker(false);
                         fxzDO.getController().refresh();
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
         //System.out.println("Modified " + m_entryName);
         if ( super.notifyModified()) {
             addSaveCookie();
             updateDisplayName();
             return true;
         } else {
             return false; //still unmodified
         }
     }
     
 //    @Override
 //    protected CloneableTopComponent createCloneableTopComponent() {
 //        return super.createCloneableTopComponent();
 //    }
             
     private void addSaveCookie() {
         final FXZDataObject dObj = (FXZDataObject) getDataObject();
         dObj.addSaveCookie(new SaveCookie() {
             public void save() throws IOException {
                 try {
                     EditorCookie oc = dObj.getLookup().lookup( EditorCookie.class);
                     oc.saveDocument();
                 } catch( UserQuestionException e) {
                     NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                             e.getLocalizedMessage(), NotifyDescriptor.YES_NO_OPTION
                         );
                     nd.setOptions(new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION });
 
                     Object res = DialogDisplayer.getDefault().notify(nd);
 
                     if (NotifyDescriptor.OK_OPTION.equals(res)) {
                         e.confirmed();
                     } 
                 }
                 ((FXZDataObject) getDataObject()).getDataModel().getFXDContainer().setIsSaved();
             }
         });
     }
     
     private void removeSaveCookie() {
         FXZDataObject dObj = (FXZDataObject) getDataObject();
         dObj.removeSaveCookie();
     }
     
     @Override
     protected void notifyUnmodified() {
         super.notifyUnmodified();
         removeSaveCookie();
 
         //disabled since the hack seems to cause some problems with state un/modified handling
 //        FXZDataObject dObj = (FXZDataObject) getDataObject();
 //        if(dObj.getDataModel().getFXDContainer().areEntriesChanged()) {
 //            SwingUtilities.invokeLater(new Runnable() {
 //                public void run() {
 //                    notifyModified();
 //                }
 //            });
 //        }
         updateDisplayName();
     }
             
     @Override
     protected CloneableEditorSupport.Pane createPane() {
         FXZDataObject           fxzDO   = getFXZDataObject();
         MultiViewDescription [] views   = getViewDescriptions();
         int                     defView = fxzDO.getDefaultView();
 //        System.out.println("Creating pane for " + m_entryName);
         return  (CloneableEditorSupport.Pane) MultiViewFactory.createCloneableMultiView(views, views[ defView],
                 new CloseHandler(fxzDO));
     }
                 
     @Override
     public String messageHtmlName() {
         return super.messageHtmlName();
     }
 
     @Override
     public String messageName() {
         return super.messageName();        
     }
     
     protected MultiViewDescription [] getViewDescriptions() {
         FXZDataObject fxzDO = getFXZDataObject();
         return new MultiViewDescription[] {
             new PreviewViewDescription( fxzDO),        
             new SourceViewDescription( fxzDO),
             new ArchiveViewDescription( fxzDO)
         };
     }
         
     @Override
     protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
         throws IOException, BadLocationException {
         FXZDataObject dObj = getFXZDataObject();        
         assert dObj.getBaseSupport() == this;
         try {
             //TODO lock supports
             dObj.getDataModel().getFXDContainer().save( stream);
         } finally {
             //TODO unlock supports
         }
     }        
 
     protected final FXZDataObject getFXZDataObject() {
         return (FXZDataObject) getDataObject();
     }
         
     @Override
     public String toString() {
         return String.format( "FXZEditorSupport[ entry=%s] %s", m_entryName, super.toString());
     }
     
     static final class FXDEnv extends DataEditorSupport.Env {
         private static final long  serialVersionUID = 2L;
         private final String m_entryName;
         
         public FXDEnv( FXZDataObject obj, String entryName) {
             super(obj);
             m_entryName = entryName;
         }
         
         @Override
         public boolean isModified() {
             return super.isModified();
         }
         
         @Override
         public InputStream inputStream() throws IOException {
             FXDContainer container = ((FXZDataObject)getDataObject()).getDataModel().getFXDContainer();
             return container.open( m_entryName);
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
     
     private static class CloseHandler implements CloseOperationHandler, Serializable {
         private static final long serialVersionUID = 2L;
 
         private FXZDataObject m_dObj;
 
         private CloseHandler() {
             System.err.println("Serialized?");
         }
 
         public CloseHandler(final FXZDataObject dObj) {
             if (dObj == null) {
                 throw new IllegalArgumentException( "Null DataObject is not allowed!"); //NOI18N
             }
             m_dObj = dObj;
         }
 
         public boolean resolveCloseOperation(CloseOperationState[] elements) {
             boolean status = m_dObj.getEditorSupport().canClose();
             //hack or the only possible fix?
             //we need to call support.canClose() here to get the status of the
             //fxd editor. Once user chooses discard option the editor is not saved,
             //and the whole multiview gets closed. During that FXDSourceEditor.componentClosed()
             //is called. Then it runs to CloneableEditor.closeLast() which triggers
             //support.canClose() again and the dialog is here once more.
             //There doesn't seem to be a clean fix for that so setting the editor
             //state to unmodified if the canClose() returns true (save or discard)
             //so the next call to canClose() doesn't do anything (it tests the
             //editor for modified state first).
             if(status) {
                 m_dObj.getEditorSupport().notifyUnmodified();
             }   
             return status;        
         }
     }   
     
 //   /** Property change & veto listener. To react to dispose/delete of
 //    * the data object.
 //    */
 //    private final class FXZListener extends CloneableTopComponent.Ref implements PropertyChangeListener,
 //        VetoableChangeListener, Runnable {
 //        /** generated Serialized Version UID */
 //        static final long serialVersionUID = -1934890789745432531L;
 //
 //        /** environment to use as connection to outside world */
 //        private Env env;
 //
 //        /** Constructor.
 //        */
 //        public FXZListener(Env env) {
 //            this.env = env;
 //        }
 //
 //        /** Enumeration of all registered components.
 //        * @return enumeration of CloneableTopComponent
 //        */
 //        @Override
 //        public Enumeration<CloneableTopComponent> getComponents() {
 //            CloneableTopComponent.Ref delegate = getDelegate();
 //            if ( delegate == null) {
 //                return super.getComponents();
 //            } else {
 //                return delegate.getComponents();
 //            }
 //        }
 //
 //        /** Test whether there is any component in this set.
 //        * @return <CODE>true</CODE> if the reference set is empty
 //        */
 //        @Override
 //        public boolean isEmpty() {
 //            CloneableTopComponent.Ref delegate = getDelegate();
 //            if ( delegate == null) {
 //                return super.isEmpty();
 //            } else {
 //                return delegate.isEmpty();
 //            }
 //        }
 //        
 //        @Override
 //        public CloneableTopComponent getAnyComponent() {
 //            CloneableTopComponent.Ref delegate = getDelegate();
 //            if ( delegate == null) {
 //                return super.getAnyComponent();
 //            } else {
 //                return delegate.getAnyComponent();
 //            }
 //        }
 //
 //        /** Gets arbitrary component from the set. Preferrably returns currently
 //         * active component if found in the set.
 //         * @return arbitratry <code>CloneableTopComponent</code> from the set
 //         *         or <code>null</code> if the set is empty
 //         * @since 3.41 */
 //        @Override
 //        public CloneableTopComponent getArbitraryComponent() {
 //            CloneableTopComponent.Ref delegate = getDelegate();
 //            if ( delegate == null) {
 //                return super.getArbitraryComponent();
 //            } else {
 //                return delegate.getArbitraryComponent();
 //            }
 //        }
 //        
 //        /** Getter for the associated CloneableOpenSupport
 //        * @return the support or null if none was found
 //        */
 //        private CloneableOpenSupport support() {
 //            return env.findCloneableOpenSupport();
 //        }
 //
 //        private CloneableTopComponent.Ref getDelegate() {
 //            CloneableTopComponent.Ref ref = ((FXZDataObject)getDataObject()).getBaseSupport().allEditors;
 //            return  ref != this ? ref : null;
 //        }
 //        
 //        public void propertyChange(PropertyChangeEvent ev) {
 //            if (Env.PROP_VALID.equals(ev.getPropertyName())) {
 //                // do not check it if old value is not true
 //                if (Boolean.FALSE.equals(ev.getOldValue())) {
 //                    return;
 //                }
 //
 //                Mutex.EVENT.readAccess(this);
 //            }
 //        }
 //
 //        /** Closes the support in AWT thread.
 //         */
 //        public void run() {
 //            // loosing validity
 //            CloneableOpenSupport os = support();
 //
 //            if (os != null) {
 //                // mark the object as not being modified, so nobody
 //                // will ask for save
 //                env.unmarkModified();
 //
 //                ((FXZEditorSupport)os).close(false);
 //            }
 //        }
 //
 //        /** Forbids setValid (false) on data object when there is an
 //        * opened editor.
 //        *
 //        * @param ev PropertyChangeEvent
 //        */
 //        public void vetoableChange(PropertyChangeEvent ev)
 //        throws PropertyVetoException {
 //            if (Env.PROP_VALID.equals(ev.getPropertyName())) {
 //                // do not check it if old value is not true
 //                if (Boolean.FALSE.equals(ev.getOldValue())) {
 //                    return;
 //                }
 //
 //                if (env.isModified()) {
 //                    // if the object is modified 
 //                    CloneableOpenSupport os = support();
 //
 //                    if ((os != null) && !((FXZEditorSupport)os).canClose()) {
 //                        // is modified and has not been sucessfully closed
 //                        throw new PropertyVetoException(
 //                        // [PENDING] this is not a very good detail message!
 //                        "", ev // NOI18N
 //                        );
 //                    }
 //                }
 //            }
 //        }
 //
 //        /** Resolvable to connect to the right data object. This
 //        * method is used for connectiong CloneableTopComponents via
 //        * their CloneableTopComponent.Ref
 //        */
 //        public Object readResolve() {
 //            CloneableOpenSupport os = support();
 //
 //            if (os == null) {
 //                // problem! no replace!?
 //                return this;
 //            }
 //
 //            // use the editor support's CloneableTopComponent.Ref
 //            return ((FXZEditorSupport) os).allEditors;
 //        }
 //    }
     
 //    private static class SaveImpl implements AtomicAction {
 //        private static final SaveImpl DEFAULT = new SaveImpl(null);
 //        private final DataEditorSupport des;
 //
 //        public SaveImpl(DataEditorSupport des) {
 //            this.des = des;
 //        }
 //
 //        public void run() throws IOException {
 //            if (des.desEnv().isModified() && des.isEnvReadOnly()) {
 //                IOException e = new IOException("File is read-only: " + ((Env) des.env).getFileImpl()); // NOI18N
 //                UIException.annotateUser(e, null, org.openide.util.NbBundle.getMessage(org.openide.loaders.DataObject.class, "MSG_FileReadOnlySaving", new java.lang.Object[]{((org.openide.text.DataEditorSupport.Env) des.env).getFileImpl().getNameExt()}), null, null);
 //                throw e;
 //            }
 //            DataObject tmpObj = des.getDataObject();
 //            Charset c = FileEncodingQuery.getEncoding(tmpObj.getPrimaryFile());
 //            try {
 //                charsets.put(tmpObj, c);
 //                des.superSaveDoc();
 //            } finally {
 //                charsets.remove(tmpObj);
 //            }
 //        }
 //
 //        @Override
 //        public int hashCode() {
 //            return getClass().hashCode();
 //        }
 //
 //        @Override
 //        public boolean equals(Object obj) {
 //            return obj != null && getClass() == obj.getClass();
 //        }
 //    }
 }
