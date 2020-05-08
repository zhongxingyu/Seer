 package org.jboss.test.faces.htmlunit;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import org.jboss.test.faces.ApplicationServer;
 import org.jboss.test.faces.FacesRule;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.Page;
 
 /**
  * 
  */
 
 /**
  * @author asmirnov
  *
  */
 public class HtmlUnitRule extends FacesRule {
 
 
 	private final HtmlUnitEnvironment environment;
 
 	protected HtmlUnitRule(HtmlUnitEnvironment environment) {
 		super(environment);
 		this.environment = environment;
 	}
 
 	public static HtmlUnitRule create() {
 		return new HtmlUnitRule(new HtmlUnitEnvironment());
 	}
 
 	public static HtmlUnitRule create(ApplicationServer server) {
 		return new HtmlUnitRule(new HtmlUnitEnvironment(server));
 	}
 
 	public <P extends Page> P getPage(String url) throws FailingHttpStatusCodeException,
 			MalformedURLException, IOException {
		return environment.<P>getPage(url);
 	}
 
 
 }
