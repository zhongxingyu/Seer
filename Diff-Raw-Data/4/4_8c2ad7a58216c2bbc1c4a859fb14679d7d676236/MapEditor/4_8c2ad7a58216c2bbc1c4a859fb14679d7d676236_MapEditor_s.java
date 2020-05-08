 package org.tomale.id.gis.editor.ui;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.tomale.id.gis.editor.ui.internal.MapImage;
 
 public class MapEditor extends EditorPart {
 
 	public final static String EDITOR_ID = "org.tomale.id.gis.editor.map";
 	
 	MapImage _map;
 	Canvas _canvas;
 	
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isDirty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		_canvas = new Canvas(parent, SWT.NO_BACKGROUND);
 		/*
 		_map = new MapImage();
 		_map.setBounds(parent.getBounds());
 		*/
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
