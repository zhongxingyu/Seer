 package com.laboki.eclipse.plugin.jcolon.inserter;
 
 import java.util.List;
 
 import lombok.ToString;
 
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.IWorkbenchPart;
 
 import com.google.common.collect.Lists;
 
 @ToString
 public final class Factory implements Runnable {
 
	private final List<IEditorPart> editorParts = Lists.newArrayList();
 	private static final IPartService PART_SERVICE = EditorContext.getPartService();
 	private final PartListener partListener = new PartListener();
 
 	public Factory() {
 		EditorContext.instance();
 		Factory.PART_SERVICE.addPartListener(this.partListener);
 	}
 
 	@Override
 	public void run() {
 		this.enableAutomaticInserterFor(Factory.PART_SERVICE.getActivePart());
 	}
 
	public void enableAutomaticInserterFor(final IWorkbenchPart part) {
 		if (this.isInvalidPart(part)) return;
 		if (!EditorContext.isAJavaEditor((IEditorPart) part)) return;
		this.editorParts.add((IEditorPart) part);
 		EditorContext.asyncExec(new SemiColonInserterServices());
 	}
 
 	private boolean isInvalidPart(final IWorkbenchPart part) {
 		return !this.isValidPart(part);
 	}
 
 	private boolean isValidPart(final IWorkbenchPart part) {
 		if (part == null) return false;
		if (this.editorParts.contains(part)) return false;
 		if (part instanceof IEditorPart) return true;
 		return false;
 	}
 
 	private final class PartListener implements IPartListener {
 
 		public PartListener() {}
 
 		@Override
 		public void partActivated(final IWorkbenchPart part) {
 			Factory.this.enableAutomaticInserterFor(part);
 		}
 
 		@Override
 		public void partClosed(final IWorkbenchPart part) {
			if (Factory.this.editorParts.contains(part)) Factory.this.editorParts.remove(part);
 		}
 
 		@Override
		public void partBroughtToTop(final IWorkbenchPart part) {}
 
 		@Override
		public void partDeactivated(final IWorkbenchPart part) {}
 
 		@Override
		public void partOpened(final IWorkbenchPart part) {}
 	}
 }
