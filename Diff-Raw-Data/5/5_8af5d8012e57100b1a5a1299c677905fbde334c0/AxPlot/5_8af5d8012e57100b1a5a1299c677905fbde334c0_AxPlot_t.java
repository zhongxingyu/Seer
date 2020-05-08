 
 package com.zanoccio.axirassa.webapp.components;
 
 import org.apache.tapestry5.annotations.Environmental;
 import org.apache.tapestry5.annotations.Import;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.services.javascript.JavaScriptSupport;
 
import com.zanoccio.axirassa.util.RandomStringGenerator;

 @Import(library = {
         "context:js/flotr.debug-0.2.0-alpha.js", "context:js/sentinel.js", "context:js/lib/canvas2image.js",
         "context:js/lib/canvastext.js" })
 public class AxPlot {
 
 	@Environmental
 	private JavaScriptSupport javascriptsupport;
 
 
 	void setupRender() {
 		// javascriptsupport.addInitializerCall("plotchart", getID());
 
 		javascriptsupport.addScript("sentinel.plotchart('%s', '%s', '%s', '%s')", getID(), getSource(), getType(),
 		                            getColor());
 	}
 
 
 	// ID
 	public static String generateID() {
		return "plot_" + RandomStringGenerator.getInstance().randomString(5);
 	}
 
 
 	@Parameter(defaultPrefix = "literal")
 	private String id;
 
 
 	public String getID() {
 		if (id == null)
 			id = generateID();
 
 		return id;
 	}
 
 
 	// SOURCE
 	@Parameter(required = true, defaultPrefix = "literal")
 	private String source;
 
 
 	public String getSource() {
 		return source;
 	}
 
 
 	// TYPE
 	@Parameter(required = false, defaultPrefix = "literal")
 	private String type;
 
 
 	public String getType() {
 		if (type == null)
 			type = "percent";
 
 		return type;
 	}
 
 
 	// COLOR
 	@Parameter(required = false, defaultPrefix = "literal")
 	private String color;
 
 
 	public String getColor() {
 		if (color == null)
 			color = "#ff9900";
 
 		return color;
 	}
 
 }
