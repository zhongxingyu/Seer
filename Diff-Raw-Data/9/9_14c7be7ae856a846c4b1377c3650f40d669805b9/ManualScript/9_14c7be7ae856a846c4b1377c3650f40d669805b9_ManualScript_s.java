 package dexels.apachecon;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.component.annotations.Activate;
 import org.osgi.service.component.annotations.Component;
 import org.osgi.service.component.annotations.ConfigurationPolicy;
 
 import dexels.apachecon.api.Evaluator;
 import dexels.apachecon.api.base.BaseEvaluator;
 
 @Component(immediate=true,configurationPolicy=ConfigurationPolicy.IGNORE,property={"name=ManualScript"})
 public class ManualScript extends BaseEvaluator implements Evaluator {
 
 	public ManualScript() {
 		super(ManualScript.class.getSimpleName(),"ruby",".rb");
 	}
 	
 	@Activate
 	public void activate(BundleContext bc) {
 		super.activate(bc);
 	}
 
 }
 
 
