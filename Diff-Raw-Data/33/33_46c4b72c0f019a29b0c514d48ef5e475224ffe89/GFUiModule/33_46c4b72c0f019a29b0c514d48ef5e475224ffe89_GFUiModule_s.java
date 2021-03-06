 /**
  * GF Eclipse Plugin
  * http://www.grammaticalframework.org/eclipse/
  * John J. Camilleri, 2011
  * 
  * The research leading to these results has received funding from the
  * European Union's Seventh Framework Programme (FP7/2007-2013) under
  * grant agreement n° FP7-ICT-247914.
  */
 package org.grammaticalframework.eclipse.ui;
 
 import java.io.IOException;
 
 import org.apache.log4j.Layout;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.RollingFileAppender;
 import org.apache.log4j.WriterAppender;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.ui.console.IOConsoleOutputStream;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.grammaticalframework.eclipse.GFPreferences;
 import org.grammaticalframework.eclipse.ui.perspectives.GFConsole;
 
 /**
  * Use this class to register components to be used within the IDE.
  */
 public class GFUiModule extends org.grammaticalframework.eclipse.ui.AbstractGFUiModule {
 
 	/**
 	 * The Constant LOG_FILE_NAME.
 	 */
 	private static final String LOG_FILE_NAME = "gfep.log";
 	
 	/**
 	 * The Apache log4j logger
 	 */
 	public static final Logger log = Logger.getLogger("org.grammaticalframework.eclipse");
 	
 	private GFConsole consoleManager;
 
 	/**
 	 * Instantiates a new GF UI module.
 	 *
 	 * @param plugin the plugin
 	 */
 	public GFUiModule(AbstractUIPlugin plugin) {
 		super(plugin);
 		
 		// Setup logging and direct to console
 		consoleManager = new GFConsole();
 		IOConsoleOutputStream consoleStream = consoleManager.getLogOutputStream();
 		Layout layout = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss,SSS}] %-5p %m%n");
 		log.addAppender(new WriterAppender(layout, consoleStream));
 		try {
 			String logFileFull = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString() + java.io.File.separator + LOG_FILE_NAME;
 			RollingFileAppender logfile = new RollingFileAppender(layout, logFileFull);
 			log.addAppender(logfile);
 		} catch (IOException e) {
 			log.warn(e.getMessage());
 		}
 		
 		log.setLevel( Level.toLevel( GFPreferences.getLogLevel(), Level.INFO ) );
 	}
 	
 	/**
 	 * Bind language root preference page.
 	 */
 	public Class<? extends org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage> bindLanguageRootPreferencePage() {
 		return org.grammaticalframework.eclipse.ui.editor.preferences.GFLanguageRootPreferencePage.class;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.grammaticalframework.eclipse.ui.AbstractGFUiModule#bindIOutlineTreeProvider()
 	 */
 	@Override
 	public Class<? extends org.eclipse.xtext.ui.editor.outline.IOutlineTreeProvider> bindIOutlineTreeProvider() {
 		return org.grammaticalframework.eclipse.ui.outline.GFOutlineTreeProvider.class;
 	}
 //	@Override
 //	public Class<? extends org.eclipse.xtext.ui.editor.outline.impl.IOutlineTreeStructureProvider> bindIOutlineTreeStructureProvider() {
 //		return org.grammaticalframework.eclipse.ui.outline.GFOutlineTreeProvider.class;
 //	}
 
 	
 	/**
 	 * Bind custom URI opener implementation
 	 */
 	public Class<? extends org.eclipse.xtext.ui.editor.LanguageSpecificURIEditorOpener> bindLanguageSpecificURIEditorOpener() {
 		return org.grammaticalframework.eclipse.ui.editor.GFURIEditorOpener.class;
 	}
 	
 	/**
 	 * Bind highlighting configuration.
 	 */
 	public Class<? extends org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration> bindIHighlightingConfiguration() {
 		return org.grammaticalframework.eclipse.ui.editor.syntaxcoloring.GFHighlightingConfiguration.class;
 	}
 	
 	/**
 	 * Bind abstract antlr token to attribute id mapper.
 	 */
 	public Class<? extends org.eclipse.xtext.ui.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
 		return org.grammaticalframework.eclipse.ui.editor.syntaxcoloring.GFAntlrTokenToAttributeIdMapper.class;
 	}
 	
 	/**
 	 * This has 2 functions:
 	 * 		Remove the prompt for Xtext nature (Refer: http://www.eclipse.org/forums/index.php/mv/msg/173440/552043/#msg_552043)
 	 * 		Disabling validation when openind a linked external resource
 	 * 
 	 * 	BUT It introduces a lot of problems when using external files! Therefore I have resolved to always using the xtext nature 
 	 */
 //	@Override
 //	public Class<? extends org.eclipse.xtext.ui.editor.IXtextEditorCallback> bindIXtextEditorCallback() {
 //		return org.grammaticalframework.eclipse.ui.editor.validation.GFValidatingEditorCallback.class;
 ////		return org.eclipse.xtext.ui.editor.validation.ValidatingEditorCallback.class;
 //	}
 }
