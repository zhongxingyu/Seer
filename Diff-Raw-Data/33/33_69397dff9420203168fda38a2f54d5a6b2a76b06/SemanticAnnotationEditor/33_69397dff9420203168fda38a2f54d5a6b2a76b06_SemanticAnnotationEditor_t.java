 package org.integratedmodelling.thinkscape.editors;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.integratedmodelling.thinklab.annotation.SemanticAnnotation;
 import org.integratedmodelling.thinklab.annotation.SemanticAnnotationContainer;
 import org.integratedmodelling.thinklab.annotation.SemanticSource;
 import org.integratedmodelling.thinklab.exception.ThinklabRuntimeException;
 import org.integratedmodelling.thinkscape.ThinkScape;
 import org.integratedmodelling.thinkscape.modeleditor.model.ModelNamespace;
 import org.integratedmodelling.thinkscape.project.ThinklabProject;
 import org.integratedmodelling.thinkscape.widgets.SingleAnnotationEditor;
 import org.integratedmodelling.utils.MiscUtilities;
 
 public class SemanticAnnotationEditor extends EditorPart {
 
 	public static final String ID = "org.integratedmodelling.thinkscape.editors.SemanticAnnotationEditor"; //$NON-NLS-1$
 
 	private Composite itemEditorParent;
 
 	private SemanticAnnotation currentAnnotation = null;
 	private SingleAnnotationEditor currentEditor;
 	private Composite parent;
 	private boolean dirty = false;
 	private IEditorInput input;
 	
 	private SemanticAnnotationContainer theContainer;
 	private String namespace;
 	
 	public SemanticAnnotationEditor() {
 	}
 
 	/**
 	 * Create contents of the editor part.
 	 * @param parent
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		this.parent = parent;
 		
 		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (dirty && (e.keyCode == 's' || e.keyCode == 'S') && (e.stateMask & SWT.CTRL) != 0)
 						doSave(new NullProgressMonitor());
 			}
 		});
 		composite.setLayout(new GridLayout(1, false));
 		
 		Composite composite_1 = new Composite(composite, SWT.NONE);
 		composite_1.setLayout(new GridLayout(1, false));
 		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		itemEditorParent = new Composite(composite_1, SWT.NONE);
 		itemEditorParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		GridLayout gl_itemEditorParent = new GridLayout(1, false);
 		gl_itemEditorParent.marginWidth = 0;
 		gl_itemEditorParent.marginTop = 3;
 		gl_itemEditorParent.marginHeight = 0;
 		itemEditorParent.setLayout(gl_itemEditorParent);
 		
 		if (currentAnnotation == null) {
 			Label lblDragASource = new Label(itemEditorParent, SWT.SHADOW_NONE);
 			lblDragASource.setAlignment(SWT.CENTER);
 			lblDragASource.setText("Drag a source from the Sources view or \r\nchoose an observation to edit from the list");
 			lblDragASource.setLayoutData(
 					new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		} else {
 			this.currentEditor = new SingleAnnotationEditor(
 					itemEditorParent,
 					this,
 					theContainer,
 					currentAnnotation);
 			this.currentEditor.setLayoutData(
 					new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		}
 		
 		DropTarget dropTarget = new DropTarget(composite, DND.DROP_COPY);
 		dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
 		
 		dropTarget.addDropListener(new DropTargetListener() {
 
 			@Override
 			public void dropAccept(DropTargetEvent arg0) {
 			}
 
 			@Override
 			public void drop(DropTargetEvent arg0) {
 				handleDrop((String) (arg0.data));
 			}
 
 			@Override
 			public void dragOver(DropTargetEvent arg0) {
 			}
 
 			@Override
 			public void dragOperationChanged(DropTargetEvent arg0) {
 			}
 
 			@Override
 			public void dragLeave(DropTargetEvent arg0) {
 			}
 
 			@Override
 			public void dragEnter(DropTargetEvent arg0) {
 				arg0.detail = DND.DROP_COPY;
 			}
 		});
 
 		/*
 		 * TODO initialize table with annotations, select current if any
 		 */
 	}
 	
 	protected void handleDrop(String string) {
 		
 		if (string.startsWith("_SOURCE")) {
 			String[] ss = string.split("\\|");
 			SemanticAnnotationContainer sourceContainer = 
 				ThinkScape.getActiveProject().getSemanticSource(ss[1]);
 			SemanticSource source = sourceContainer.getSource(ss[2]);
 			currentAnnotation = sourceContainer.startAnnotation(source, source.getId() + "_1");
 			setDirty();
 			resetView();
 		}
 	}
 	
 	@Override
 	public void setFocus() {
 		// Set the focus
 	}
 
 	public void resetView() {
 		for (Control c : this.parent.getChildren())
 			c.dispose();
 		createPartControl(this.parent);
 		this.parent.layout(true);
 	}
 	
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 
		if (this.dirty) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			try {
				File f = file.getRawLocation().makeAbsolute().toFile();
				OutputStream out = new FileOutputStream(f);
				theContainer.store(out);
				out.close();
			} catch (Exception e) {
				throw new ThinklabRuntimeException(e);
			}
			firePropertyChange(IEditorPart.PROP_DIRTY);
			resetView();
			this.dirty = false;
 		}
		//setPartName((MiscUtilities.getFileBaseName(this.input.getName())));
 	}
 
 	@Override
 	public void doSaveAs() {
 		// Do the Save As operation
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		
 		setSite(site);
 		setInput(this.input = input);
 		this.namespace = MiscUtilities.getFileBaseName(input.getName());
 		
 		for (ThinklabProject proj : ThinkScape.getProjects()) {
 			for (SemanticAnnotationContainer mn : proj.getAnnotationNamespaces()) {
 				if (mn.getNamespace().equals(this.namespace)) {
 					this.theContainer = mn;
 					break;
 				}
 			}	
 			if (this.theContainer != null)
 				break;
 		}
 
 		setPartName(this.namespace);
 	}
 	
 	
 	@Override
 	public boolean isDirty() {
 		return this.dirty;
 	}
 	
 	public void setDirty() {
		if (!this.dirty)
			firePropertyChange(IEditorPart.PROP_DIRTY);
 		this.dirty = true;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 }
