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
 
	private final List<String> editorParts = Lists.newArrayList();
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
 
	private void enableAutomaticInserterFor(final IWorkbenchPart part) {
 		if (this.isInvalidPart(part)) return;
 		if (!EditorContext.isAJavaEditor((IEditorPart) part)) return;
		this.editorParts.add(((IEditorPart) part).getTitleToolTip());
 		EditorContext.asyncExec(new SemiColonInserterServices());
 	}
 
 	private boolean isInvalidPart(final IWorkbenchPart part) {
 		return !this.isValidPart(part);
 	}
 
 	private boolean isValidPart(final IWorkbenchPart part) {
 		if (part == null) return false;
		if (this.editorParts.contains(part.getTitleToolTip())) return false;
 		if (part instanceof IEditorPart) return true;
 		return false;
 	}
 
	private void disableAutomaticInserterFor(final IWorkbenchPart part) {
		if (Factory.this.editorParts.contains(part.getTitleToolTip())) Factory.this.editorParts.remove(part.getTitleToolTip());
	}

 	private final class PartListener implements IPartListener {
 
 		public PartListener() {}
 
 		@Override
 		public void partActivated(final IWorkbenchPart part) {
 			Factory.this.enableAutomaticInserterFor(part);
 		}
 
 		@Override
 		public void partClosed(final IWorkbenchPart part) {
			Factory.this.disableAutomaticInserterFor(part);
 		}
 
 		@Override
		public void partBroughtToTop(final IWorkbenchPart part) {
			Factory.this.enableAutomaticInserterFor(part);
		}
 
 		@Override
		public void partDeactivated(final IWorkbenchPart part) {
			Factory.this.disableAutomaticInserterFor(part);
		}
 
 		@Override
		public void partOpened(final IWorkbenchPart part) {
			Factory.this.enableAutomaticInserterFor(part);
		}
 	}
 }
