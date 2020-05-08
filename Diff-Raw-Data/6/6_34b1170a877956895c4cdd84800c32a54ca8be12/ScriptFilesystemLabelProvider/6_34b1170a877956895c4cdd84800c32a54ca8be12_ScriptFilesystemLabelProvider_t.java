 package cc.warlock.rcp.ui.script;
 
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 import cc.warlock.core.script.IScriptFileInfo;
import cc.warlock.rcp.application.WarlockApplication;
 
 public class ScriptFilesystemLabelProvider implements ILabelProvider
 {
 	public Image getImage(Object element) {
 		if (element instanceof IScriptFileInfo)
 		{
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
 		}
 		if (element.equals(ScriptFilesystemContentProvider.LOCAL_SCRIPTS))
 		{
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
 		}
 		return null;
 	}
 
 	public String getText(Object element) {
		if (element instanceof WarlockApplication)
		{
			return "Warlock Script Navigator";
		}
 		if (element instanceof IScriptFileInfo)
 		{
 			return ((IScriptFileInfo)element).getScriptFile().getName();
 		}
 		if (element.equals(ScriptFilesystemContentProvider.LOCAL_SCRIPTS))
 		{
 			return "Local Scripts";
 		}
 		
 		return element.toString();
 	}
 
 	public void addListener(ILabelProviderListener listener) {}
 
 	public void dispose() {	}
 
 	public boolean isLabelProperty(Object element, String property) {
 		return true;
 	}
 
 	public void removeListener(ILabelProviderListener listener) {	}
 	
 }
