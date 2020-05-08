 package org.ollabaca.on.lang.ui;
 
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
 import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;
 import org.ollabaca.on.Import;
 import org.ollabaca.on.Instance;
 import org.ollabaca.on.Slot;
 import org.ollabaca.on.Units;
 
 public class TextHoverProvider extends DefaultEObjectHoverProvider  {
 
 	Units units = new Units();
 
 	@Override
 	protected String getFirstLine(EObject o) {
 		EModelElement element = null;
 		if (o instanceof Import) {
 			Import self = (Import) o;
 			element = units.getPackage(self);
 		} else if (o instanceof Instance) {
 			Instance self = (Instance) o;
 			element = units.getClassifier(self);
		} else if (o instanceof Slot) {
			Slot self = (Slot) o;
 			element = units.getFeature(self);
 		}
 
 		if (element != null) {
 			String doc = units.getDocumentation(element);
 			if (doc != null) {
 				return doc;
 			}
 
 		}
 		return super.getFirstLine(o);
 	}
 
 }
