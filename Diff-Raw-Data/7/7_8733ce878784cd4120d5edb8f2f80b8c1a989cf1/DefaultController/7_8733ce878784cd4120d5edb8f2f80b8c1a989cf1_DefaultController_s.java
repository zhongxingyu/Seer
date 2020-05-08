 package nz.ac.victoria.ecs.kpsmart.controller;
 
 import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 
 @UrlBinding("/")
 public final class DefaultController extends AbstractActionBean {
 	@DefaultHandler
	public Resolution homeRedirest() {
		return new RedirectResolution("/dashboard");
 	}
 }
