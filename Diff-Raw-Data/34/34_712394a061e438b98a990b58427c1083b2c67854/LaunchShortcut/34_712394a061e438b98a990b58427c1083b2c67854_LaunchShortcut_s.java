 package com.ingemark.perftest.plugin;
 
 import static com.ingemark.perftest.plugin.StressTestActivator.EVT_RUN_SCRIPT;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.debug.ui.ILaunchShortcut;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.part.FileEditorInput;
 
 import com.ingemark.perftest.plugin.ui.RequestAgeView;
 
 public class LaunchShortcut implements ILaunchShortcut
 {
   @Override public void launch(ISelection selection, String mode) {
     launch(((IFile)((StructuredSelection)selection).getFirstElement()));
   }
   @Override public void launch(IEditorPart editor, String mode) {
     launch(((FileEditorInput)editor.getEditorInput()).getFile());
   }
   void launch(IFile in) {
     final Event e = new Event();
     e.data = in.getLocation().toOSString();
     RequestAgeView.instance.statsParent.notifyListeners(EVT_RUN_SCRIPT, e);
   }
 }
