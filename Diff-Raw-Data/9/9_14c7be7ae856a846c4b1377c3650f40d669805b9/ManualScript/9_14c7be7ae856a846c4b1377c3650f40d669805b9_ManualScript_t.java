 package dexels.apachecon;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.component.annotations.Activate;
 import org.osgi.service.component.annotations.Component;
 import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
 
 import dexels.apachecon.api.Evaluator;
 import dexels.apachecon.api.base.BaseEvaluator;
import dexels.apachecon.billboard.Billboard;
 
 @Component(immediate=true,configurationPolicy=ConfigurationPolicy.IGNORE,property={"name=ManualScript"})
 public class ManualScript extends BaseEvaluator implements Evaluator {
 
	@Reference
	public void setLogger(Billboard s) {
		s.show("Manual script present");
	}
	
 	public ManualScript() {
 		super(ManualScript.class.getSimpleName(),"ruby",".rb");
 	}
 	
 	@Activate
 	public void activate(BundleContext bc) {
 		super.activate(bc);
 	}
 
 }
 
 
