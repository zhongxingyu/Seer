 package org.eclipse.gmf.ecore.providers;
 
import java.util.Map;

 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
 import org.eclipse.gmf.runtime.common.core.service.IOperation;
 import org.eclipse.gmf.runtime.diagram.ui.internal.services.palette.IPaletteProvider;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.gmf.ecore.part.EcorePaletteFactory;
 
 /**
  * @generated
  */
 public class EcorePaletteProvider extends AbstractProvider implements IPaletteProvider {
 
 	/**
 	 * @generated
 	 */
	public void contributeToPalette(IEditorPart editor, Object content, PaletteRoot root, Map predefinedEntries) {
 		EcorePaletteFactory factory = new EcorePaletteFactory();
 		factory.fillPalette(root);
 	}
 
 	/**
 	 * @generated
 	 */
 	public void setContributions(IConfigurationElement configElement) {
 		// no configuration
 	}
 
 	/**
 	 * @generated
 	 */
 	public boolean provides(IOperation operation) {
 		return false; // all logic is done in the service
 	}
 }
