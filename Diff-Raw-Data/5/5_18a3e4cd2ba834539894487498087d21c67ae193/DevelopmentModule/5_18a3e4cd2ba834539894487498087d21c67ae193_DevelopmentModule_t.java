 package ${package}.services;
 
 import org.apache.tapestry5.SymbolConstants;
 import org.apache.tapestry5.ioc.MappedConfiguration;
 
 /**
  * This module is automatically included as part of the Tapestry IoC Registry if <em>tapestry.execution-mode</em>
  * includes <code>development</code>.
  */
 public class DevelopmentModule
 {
 	public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration)
 	{
 		// The factory default is true but during the early stages of an application
 		// overriding to false is a good idea. In addition, this is often overridden
 		// on the command line as -Dtapestry.production-mode=false
 		configuration.add(SymbolConstants.PRODUCTION_MODE, false);
 
		// Controls whether whitespace is compressed by default in templates, or left as is.
		// The factory default is to compress whitespace. This can be overridden
		// using the xml:space attribute inside template elements.
		configuration.add(SymbolConstants.COMPRESS_WHITESPACE, false);

 		// The application version number is incorprated into URLs for some
 		// assets. Web browsers will cache assets because of the far future expires
 		// header. If existing assets are changed, the version number should also
 		// change, to force the browser to download new versions.
 		configuration.add(SymbolConstants.APPLICATION_VERSION, "${version}-DEV");
 	}
 }
