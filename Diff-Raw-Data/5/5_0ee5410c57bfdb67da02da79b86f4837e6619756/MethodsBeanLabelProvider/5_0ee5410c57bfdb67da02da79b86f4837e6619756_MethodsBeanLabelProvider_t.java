 package com.vectorsf.jvoice.ui.edit.provider;
 
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 
 import com.vectorsf.jvoice.model.operations.ComponentBean;
 
 public class MethodsBeanLabelProvider implements ILabelProvider {
 
 	AdapterFactoryLabelProvider aflp;
 
 	public MethodsBeanLabelProvider() {
 		ComposedAdapterFactory factorylabel = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		aflp = new AdapterFactoryLabelProvider(factorylabel);
 
 	}
 
 	@Override
 	public void addListener(ILabelProviderListener listener) {
 		aflp.addListener(listener);
 
 	}
 
 	@Override
 	public void dispose() {
 		aflp.dispose();
 	}
 
 	@Override
 	public boolean isLabelProperty(Object element, String property) {
 		return aflp.isLabelProperty(element, property);
 	}
 
 	@Override
 	public void removeListener(ILabelProviderListener listener) {
 		aflp.removeListener(listener);
 	}
 
 	@Override
 	public Image getImage(Object element) {
 		return aflp.getImage(element);
 	}
 
 	@Override
 	public String getText(Object element) {
 		try {
 			if (element instanceof ComponentBean) {
 				return ((ComponentBean) element).getName();
 			} else if (element instanceof IMethod) {
 				IMethod elementMethod = (IMethod) element;
 				return getParameters(elementMethod);
 			}
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 		return aflp.getText(element);
 	}
 
 	private String getParameters(IMethod method) throws JavaModelException {
 		StringBuilder signature = new StringBuilder(method.getElementName());
		signature.append("(");

		String join = "";
 		for (String par : method.getParameterNames()) {
 			signature.append(join).append(par);
 			join = ",";
 		}
 		signature.append(')');
 		return signature.toString();
 	}
 }
