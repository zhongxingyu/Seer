 package naholyr.eclipse.sf.tploverride;
 
 import naholyr.eclipse.sf.Activator;
 import naholyr.eclipse.sf.SymfonyTools;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ILightweightLabelDecorator;
 
 public class TemplateOverrideDecorator implements ILightweightLabelDecorator {
 
 	public static final ImageDescriptor ICON = Activator
 			.getImageDescriptor("icons/tpl_override.gif"); //$NON-NLS-1$
 
 	@Override
 	public void addListener(ILabelProviderListener listener) {
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	@Override
 	public boolean isLabelProperty(Object element, String property) {
 		return false;
 	}
 
 	@Override
 	public void removeListener(ILabelProviderListener listener) {
 	}
 
 	@Override
 	public void decorate(Object element, IDecoration decoration) {
 		if (element instanceof IFile && shouldDecorate((IFile) element)) {
			decoration.addOverlay(ICON);
 		}
 	}
 
 	private boolean shouldDecorate(IFile file) {
 		return SymfonyTools.isTemplateOverride(file);
 	}
 }
