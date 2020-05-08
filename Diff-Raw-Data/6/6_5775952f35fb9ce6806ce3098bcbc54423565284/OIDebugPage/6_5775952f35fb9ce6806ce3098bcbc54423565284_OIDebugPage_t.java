 package cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.oi.debug;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.log4j.Logger;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.util.ListModel;
 
 import cz.cuni.mff.odcleanstore.configuration.BackendConfig;
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.linker.Linker;
 import cz.cuni.mff.odcleanstore.linker.impl.DebugResult;
 import cz.cuni.mff.odcleanstore.linker.impl.LinkerImpl;
 import cz.cuni.mff.odcleanstore.transformer.EnumTransformationType;
 import cz.cuni.mff.odcleanstore.transformer.TransformationContext;
 import cz.cuni.mff.odcleanstore.transformer.TransformerException;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.Role;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.UploadButton;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.FrontendPage;
 
 @AuthorizeInstantiation({ Role.PIC })
 public class OIDebugPage extends FrontendPage 
 {	
 	private static final long serialVersionUID = 1L;
	protected static Logger logger = Logger.getLogger(OIDebugPage.class);
 	
 	private String rdfInput;
 	
 	public OIDebugPage(Integer groupId)
 	{
 		super(
 			"Home > Backend > OI > Groups > Debug", 
 			"Debug OI rule group"
 		);
 			
 		// register page components
 		//
 		addInputForm(groupId);
 	}
 	
 	private void addInputForm(final Integer groupId)
 	{
 		Form<OIDebugPage> form = new Form<OIDebugPage>(
 				"inputForm", new CompoundPropertyModel<OIDebugPage>(this))
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit()
 			{
 				Linker linker = new LinkerImpl(groupId);
 				try 
 				{
 					List<DebugResult> results = linker.debugRules(rdfInput, createContext());
 					setResponsePage(new OIDebugResultPage(results, groupId));
 				} 
 				catch (TransformerException e)
 				{
					logger.error(e.getMessage(), e);
 					
 					getSession().error("Rule debugging failed due to an unexpected error.");
 				}		
 			}
 		};
 		
 		TextArea<String> rdfInput = createTextarea("rdfInput");
 		form.add(rdfInput);
 		
 		form.setMultiPart(true);
 		FileUploadField fileUpload = new FileUploadField("fileUpload", new ListModel<FileUpload>(new ArrayList<FileUpload>()));
 		form.add(fileUpload);
 		form.add(new UploadButton(fileUpload, rdfInput, "upload"));
 		
 		add(form);
 	}
 	
 	private TransformationContext createContext()
 	{
 		final BackendConfig config = ConfigLoader.getConfig().getBackendGroup();
 		return new TransformationContext()
 		{
 
 			public JDBCConnectionCredentials getDirtyDatabaseCredentials() {
 				return config.getDirtyDBJDBCConnectionCredentials();
 			}
 
 			public JDBCConnectionCredentials getCleanDatabaseCredentials() {
 				return config.getCleanDBJDBCConnectionCredentials();
 			}
 
 			public String getTransformerConfiguration() {
 				return "";
 			}
 
 			public File getTransformerDirectory() {
 				String path = ConfigLoader.getConfig().getWebFrontendGroup().getDebugDirectoryPath();
 				File dir = new File(path);
 				dir.mkdir();
 				return dir;
 			}
 
 			public EnumTransformationType getTransformationType() {
 				return EnumTransformationType.NEW;
 			}			
 		};
 	}
 }
