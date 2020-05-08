 package org.eclipse.emf.refactor.smells.runtime.ui;
 
 import java.util.LinkedList;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.refactor.smells.runtime.core.EObjectGroup;
 import org.eclipse.emf.refactor.smells.runtime.core.ModelSmellResult;
 import org.eclipse.emf.refactor.smells.runtime.core.ResultModel;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 public class ResultModelTreeViewerLabelProvider implements ILabelProvider {
 
 	@Override
 	public void addListener(ILabelProviderListener listener) { }
 
 	@Override
 	public void dispose() { }
 
 	@Override
 	public boolean isLabelProperty(Object element, String property) {
 		return false;
 	}
 
 	@Override
 	public void removeListener(ILabelProviderListener listener) { }
 
 	@Override
 	public Image getImage(Object element) {
 		if(element instanceof ResultModel)
 			return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
 		if(element instanceof ModelSmellResult)
 			return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
 		if(element instanceof EObjectGroup)
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
 		if(element instanceof EObject)
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		return null;
 	}
 
 	@SuppressWarnings({ "deprecation" })
 	@Override
 	public String getText(Object element) {
 		if(element instanceof LinkedList<?>) {
 //			LinkedList<ResultModel> resultModels = (LinkedList<ResultModel>)element;
 			return "All results";
 		}
 		if(element instanceof ResultModel){
 			int smellcount = 0;
 			for(ModelSmellResult result : ((ResultModel)element).getModelSmellResults()){
 //				for(EObjectGroup group : result.getEObjectGroups()){
 //					smellcount++;
 //				}
 				smellcount += result.getEObjectGroups().size();
 			}
 			if(smellcount == 1){
 				return ((ResultModel)element).getDate().toLocaleString() + " (" + smellcount + " occurence of a smell)";
 			}else
 				return ((ResultModel)element).getDate().toLocaleString() + " (" + smellcount + " occurences of smells)";
 		}
 		if(element instanceof ModelSmellResult){
 			String areaLabel;
 			int resultElementCount = ((ModelSmellResult)element).getEObjectGroups().size();
 			if(resultElementCount == 1)
 				areaLabel = "area";
 			else
 				areaLabel = "areas";
 			return 
 					((ModelSmellResult)element).getModelSmell().getName() + " (" + resultElementCount + " "  + areaLabel + " identified)";
 				//((ModelSmellResult)element).getModelSmell().getName() + " (" + ((ModelSmellResult)element).getModelSmell().getDescription() +") (" + resultElementCount + " "  + areaLabel + " identified)";
 			}
 		if(element instanceof EObjectGroup){
 			String label = "{";
 			for(EObject object : ((EObjectGroup)element).getEObjects()){
 				label += getEObjectLabel(object) + ", ";
 			}
 			if(label.endsWith(", ") && label.length() >= 2)
 				label = label.substring(0, label.length() - 2);
 			label += "}";
 			return label;
 		}
 		if(element instanceof EObject){
 			EObject eObject = (EObject)element;
 			return getEObjectLabel(eObject);
 		}
 		return "";
 			
 	}
 
 	private String getEObjectLabel(EObject eObject){
 		String name = null;
 		String label = null;
 		String id = null;
 		
 		for(EAttribute attribute : eObject.eClass().getEAllAttributes()){
			if (attribute.getName().equalsIgnoreCase("name")) {
 				name = (String) eObject.eGet(attribute);
 				break;
 			}
 		}	
 
 		for(EOperation operation : eObject.eClass().getEAllOperations()){
 			try{
 				if(operation.getName().equals("getName") && name == null)
 					name = (String)eObject.eInvoke(operation, null);
 				if(operation.getName().equals("getLabel") && label == null)
 					label = (String)eObject.eInvoke(operation, null);
 				if(operation.getName().equals("getID") && id == null)
 					id = (String)eObject.eInvoke(operation, null);
 				if(operation.getName().equals("getId") && id == null)
 					id = (String)eObject.eInvoke(operation, null);
 				} catch (Exception e) {
 				} 
 			}
 		
 		if(name != null && !name.isEmpty())
 			return name;
 		if(label != null && !label.isEmpty())
 			return label;
 		if(id != null && !id.isEmpty())
 			return id;
 		
 		if(eObject instanceof ENamedElement)
 			if(((ENamedElement) eObject).getName() != null && !((ENamedElement) eObject).getName().equals(""))
 				return ((ENamedElement)eObject).getName();
 			else
 				return eObject.toString();
 		else
 			return eObject.toString();
 		
 	}
 	
 }
