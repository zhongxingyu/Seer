 package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.io.FileUtils;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 
 import uk.ac.warwick.dcs.boss.frontend.Page;
 import uk.ac.warwick.dcs.boss.frontend.PageContext;
 import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
 import uk.ac.warwick.dcs.boss.frontend.sites.AdminPageFactory;
 import uk.ac.warwick.dcs.boss.model.FactoryException;
 import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
 import uk.ac.warwick.dcs.boss.model.dao.DAOException;
 import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
 import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
 import uk.ac.warwick.dcs.boss.model.dao.IPluginMetadataDAO;
 import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;
 import uk.ac.warwick.dcs.boss.model.testing.impl.TemporaryDirectory;
 import uk.ac.warwick.dcs.boss.plugins.InvalidPluginException;
 import uk.ac.warwick.dcs.boss.plugins.PluginManager;
 
 public class PerformEditPluginPage extends Page {
 
 	public PerformEditPluginPage() throws PageLoadException {
 		super("multi_edited", AccessLevel.ADMIN);
 	}
 
 	@Override
 	protected void handleGet(PageContext pageContext, Template template,
 			VelocityContext templateContext) throws ServletException,
 			IOException {
 		throw new ServletException("Unexpected GET request");
 	}
 
 	@Override
 	protected void handlePost(PageContext pageContext, Template template,
 			VelocityContext templateContext) throws ServletException,
 			IOException {
 		IDAOSession f;
 		try {
 			DAOFactory df = (DAOFactory) FactoryRegistrar
 					.getFactory(DAOFactory.class);
 			f = df.getInstance();
 		} catch (FactoryException e) {
 			throw new ServletException("dao init error", e);
 		}
 
 		if (pageContext.hasUploadedFiles()) {
 			InputStream in = null;
 			String uploadedFilename = null;
 			boolean unexpected = true;
 			try {
 				FileItemIterator fileIterator = pageContext.getUploadedFiles();
 
 				// try to obtain the input stream (from uploaded file)
 				while (fileIterator.hasNext()) {
 					FileItemStream fileItemStream = fileIterator.next();
 					if (fileItemStream.getFieldName().equals("file")) {
 						in = fileItemStream.openStream();
 						uploadedFilename = fileItemStream.getName();
 						unexpected = false;
 						break;
 					}
 				}
 			} catch (FileUploadException e) {
 				throw new ServletException("error while uploading plugin file",
 						e);
 			}
 
 			// no file uploaded in request
 			if (unexpected)
 				throw new ServletException("Unexpected POST request");
 
 			// put the uploaded file into a temporary folder for further
 			// inspection and extraction
 			File tempDir;
 
 			InputStream is = new FileInputStream(new File(
 					pageContext.getConfigurationFilePath()));
 			Properties prop = new Properties();
 			prop.load(is);
 			tempDir = TemporaryDirectory.createTempDir("plugin",
 					new File(prop.getProperty("testing.temp_dir")));
 
 			OutputStream out = null;
 			File pluginFile = new File(tempDir, uploadedFilename);
 			try {
 				out = new BufferedOutputStream(new FileOutputStream(pluginFile));
 				int c;
 				while ((c = in.read()) != -1) {
 					out.write(c);
 				}
 				out.flush();
 			} finally {
 				if (in != null)
 					in.close();
 				if (out != null)
 					out.close();
 			}
 			
 			// we have a pluginFile at this point
 			PluginMetadata pluginMetadata = null;
 			boolean success = true;
 			try {
 				// install the plugin
 				pluginMetadata = PluginManager.installPlugin(pluginFile);
 			} catch (InvalidPluginException e) {
 				success = false;
				templateContext.put("message", e.getMessage());
 			} finally {
 				FileUtils.deleteDirectory(tempDir);
 			}
 			
 			if (success) {
 				try {
 					f.beginTransaction();
 					
 					// persist into database
 					IPluginMetadataDAO pluginMetadataDao = f
 							.getPluginMetadataDAOInstance();
 					pluginMetadata.setId(pluginMetadataDao
 							.createPersistentCopy(pluginMetadata));
 
 					// done
 					f.endTransaction();
 				} catch (DAOException e) {
 					f.abortTransaction();
 					throw new ServletException("dao exception", e);
 				}
 			}
 			
 			
 			templateContext.put("greet", pageContext.getSession()
 					.getPersonBinding().getChosenName());
 			templateContext.put("success", success);
 			if (success)
 				templateContext.put("message", "Please restart BOSS for the change to take effect");
 			else {
 				templateContext.put("nextPage", pageContext.getPageUrl(AdminPageFactory.SITE_NAME, AdminPageFactory.PLUGINS_PAGE));
 				templateContext.put("nextPageParamName", "dummy");
 				templateContext.put("nextPageParamValue", "nothing");
 			}
 			pageContext.renderTemplate(template, templateContext);
 		} else {
 			String doString = pageContext.getParameter("do");
 			if (doString != null) {
 				String pluginString = pageContext.getParameter("plugin");
 				if (pluginString == null || pluginString.isEmpty()) {
 					throw new ServletException("Unexpected POST request");
 				}
 				Long pId = Long.valueOf(pluginString);
 
 				try {
 					f.beginTransaction();
 
 					IPluginMetadataDAO pluginMetadataDao = f
 							.getPluginMetadataDAOInstance();
 					PluginMetadata pluginMetadata = pluginMetadataDao
 							.retrievePersistentEntity(pId);
 					f.endTransaction();
 					if (doString.equals("Delete")) {
 						// uninstall
 						PluginManager.uninstallPlugin(pluginMetadata);
 						
 						f.beginTransaction();
 						pluginMetadataDao = f.getPluginMetadataDAOInstance();
 						// delete metadata in database
 						pluginMetadataDao.deletePersistentEntity(pId);
 						f.endTransaction();
 					}
 					else if (doString.equals("Enable")) {
 						PluginManager.enablePlugin(pluginMetadata);
 						f.beginTransaction();
 						pluginMetadataDao = f.getPluginMetadataDAOInstance();
 						pluginMetadataDao.updatePersistentEntity(pluginMetadata);
 						f.endTransaction();
 					}
 					else if (doString.equals("Disable")) {
 						PluginManager.disablePlugin(pluginMetadata);
 						f.beginTransaction();
 						pluginMetadataDao = f.getPluginMetadataDAOInstance();
 						pluginMetadataDao.updatePersistentEntity(pluginMetadata);
 						f.endTransaction();
 					}
 
 					templateContext.put("greet", pageContext.getSession()
 							.getPersonBinding().getChosenName());
 					templateContext.put("success", true);
 					templateContext.put("message", "Please restart BOSS for the change to take effect");
 					pageContext.renderTemplate(template, templateContext);
 				} catch (DAOException e) {
 					f.abortTransaction();
 					throw new ServletException("dao exception", e);
 				}
 
 			} else {
 				throw new ServletException("Unexpected POST request");
 			}
 		}
 	}
 }
