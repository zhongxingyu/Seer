 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.csm.language;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 import org.modelcc.*;
 
 import org.modelcc.metamodel.Model;
 import org.modelcc.metamodel.ModelElement;
 
 /**
  * Element Mapping Component class.
  * @author elezeta
  * @serial
  */
 public class ElementMappingComponent extends MappingComponent implements IModel,Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
 	private ElementID elementId;
 	
 	private ElementConstraint constraint;
 	
     @Override
     public void apply(Model model) {
     	ModelElement me = null;
     	for (Iterator<ModelElement> ite = model.getElements().iterator();ite.hasNext();) {
     		ModelElement mec = ite.next();
    		if (mec.getClass().getCanonicalName().endsWith(elementId.getElementName().getName())) {
     			if (me == null) {
     				me = mec;
     			}
     			else {
     				//TODO error conflict
     			}
     		}
     	}
     	if (me != null) { 
         	constraint.apply(model, me);
         	return;
     	}
     	for (Iterator<ModelElement> ite = model.getElements().iterator();ite.hasNext();) {
     		ModelElement mec = ite.next();
    		if (mec.getClass().getName().endsWith(elementId.getElementName().getName())) {
     			if (me == null) {
     				me = mec;
     			}
     			else {
     				//TODO error conflict
     			}
     		}
     	}
     	if (me != null) { 
         	constraint.apply(model, me);
         	return;
     	}
     	for (Iterator<ModelElement> ite = model.getElements().iterator();ite.hasNext();) {
     		ModelElement mec = ite.next();
    		if (mec.getClass().getSimpleName().endsWith(elementId.getElementName().getName())) {
     			if (me == null) {
     				me = mec;
     			}
     			else {
     				//TODO error conflict
     			}
     		}
     	}
     	if (me != null) { 
         	constraint.apply(model, me);
         	return;
     	}
     	else {
     		//TODO error not found
     	}
     }
     
 }
