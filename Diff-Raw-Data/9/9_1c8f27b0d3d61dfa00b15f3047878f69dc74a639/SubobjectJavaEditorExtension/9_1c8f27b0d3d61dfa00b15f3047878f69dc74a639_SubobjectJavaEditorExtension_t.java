 package subobjectjava.eclipse;
 
 import subobjectjava.model.component.ComponentRelation;
 import chameleon.core.element.Element;
 import jnome.editor.JavaEditorExtension;
 
 public class SubobjectJavaEditorExtension extends JavaEditorExtension {
 
 	@Override
 	public String getLabel(Element element) {
 		try {
//		if(element instanceof ComponentRelation) {
//			return ((ComponentRelation)element).signature().name();
//		} else {
 			return super.getLabel(element);
//		}
 		} catch(NullPointerException exc) {
 			return "";
 		}
 	}
 
 }
