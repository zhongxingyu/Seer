 package nz.ac.victoria.ecs.kpsmart.controller;
 
 import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 
 @UrlBinding("/")
 public final class DefaultController extends AbstractActionBean {
 	@DefaultHandler
	public Resolution homePage() {
		return new ForwardResolution("/views/home.jsp");
 	}
 }
