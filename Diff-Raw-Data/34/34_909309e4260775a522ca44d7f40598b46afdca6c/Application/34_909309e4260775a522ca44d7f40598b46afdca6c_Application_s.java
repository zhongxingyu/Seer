 package org.amanzi.awe;
 
 import net.refractions.udig.internal.ui.UDIGApplication;
 import net.refractions.udig.internal.ui.UDIGWorkbenchAdvisor;
 
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.ui.application.WorkbenchAdvisor;
 
 /**
  * This is the default application for the Amanzi Wireless Explorer.
  * It is based directly on uDIG, and uses its advisor.
  * 
  * @since AWE 1.0.0
  * @author craig
  */
 public class Application extends UDIGApplication implements IApplication {
 
 	/**
 	 * Create the AWE workbench advisor by using the UDIGWorkbenchAdvisor with
 	 * only the perspective changed to match the AWE requirements.
 	 * @see net.refractions.udig.internal.ui.UDIGApplication#createWorkbenchAdvisor()
 	 */
 	@Override
 	protected WorkbenchAdvisor createWorkbenchAdvisor() {
 		return new UDIGWorkbenchAdvisor() {
 			@Override
 			public String getInitialWindowPerspectiveId() {
				return "org.amanzi.awe.perspective";
 			}
 		};
 	}
 
 }
