 package org.eclipse.emf.refactor.metrics.runtime.ui;
 
 
 import java.math.BigDecimal;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.refactor.metrics.runtime.core.Result;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 class MetricResultsViewLabelProvider extends LabelProvider implements ITableLabelProvider {
 
 	public String getColumnText(Object obj, int index) {
 		switch(index){
 		case 0:
 			return ((Result)obj).getTimeStamp();
 		case 1:
 			EObject contextElement = ((Result)obj).getContext().get(0);
 			String name = getEObjectLabel(contextElement);
 			String contextString = ((Result)obj).getMetric().getContext();
 			contextString = contextString.replace(".", " ");
 			String[] s = contextString.split(" ");
 			if(s.length > 0)
 				return s[s.length -1] + " " + name;
 			else
 				return name;
 		case 2:
 			return ((Result)obj).getMetric().getName();
 		case 3:
 			return ((Result)obj).getMetric().getDescription();
 		case 4:
 			Double value = ((Result)obj).getResultValue();
 			if (value.isNaN()) return "NaN";
 			BigDecimal myDec = new BigDecimal(value);
 			myDec = myDec.setScale( 2, BigDecimal.ROUND_HALF_UP );
 			return "" + myDec;
 		}
 		return "";
 	}
 	
 	public Image getColumnImage(Object obj, int index) {
 		switch(index){
 		case 1:
 			return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
 		case 2:
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		case 3:
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
 		case 4:
 //			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD);
 		}
 		return null;
 	}
 
 	public Image getImage(Object obj) {
 		return null;
 	}
 	
 	public static String getEObjectLabel(EObject eObject){
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
