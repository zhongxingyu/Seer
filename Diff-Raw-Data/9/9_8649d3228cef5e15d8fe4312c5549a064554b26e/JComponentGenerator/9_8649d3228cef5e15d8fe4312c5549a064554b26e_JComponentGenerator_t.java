 package com.github.kanafghan.welipse.joomlagen.generator;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.util.EList;
 
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenModel;
 import com.github.kanafghan.welipse.joomlagen.generator.context.Context;
 import com.github.kanafghan.welipse.webdsl.Image;
 import com.github.kanafghan.welipse.webdsl.Page;
 import com.github.kanafghan.welipse.webdsl.PageElement;
 import com.github.kanafghan.welipse.webdsl.Website;
 
 public class JComponentGenerator {
 
 	public static void generate(JoomlaGenModel joomlaGenModel) {
 		final JoomlaGenModel genModel = joomlaGenModel;
 		final Job job = new Job("Generating codes for Joomla! extension.") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				
 				// Create/get the project
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IProject project = workspace.getRoot().getProject(genModel.getComponentName());
 				
 				try {
 					if (!project.exists()) {
 						project.create(new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
 					}
 					project.open(new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
 					
 					buildComponentFolderStructure(genModel, project, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
 					
 					// Generate extension Front-End (FE)
 					JFEGenerator.generate(new Context(genModel), project);
 					
 					// Generate extension Back-End (BE)
 					JBEGenerator.generate(new Context(genModel), project);
 					
					// Generate install SQL
 					InstallGenerator.generate(new Context(genModel), project);
 					
 					// Generate extension manifest
 					ManifestGenerator.generate(new Context(genModel), project);
 				} catch (CoreException e) {
 					return new Status(Status.ERROR, Activator.PLUGIN_ID, 
 						"An exception occurred during the code generation! Please check the error view. "
 						+ e.getMessage(), e);
 				}
 							
 				monitor.worked(1);
 				return new Status(Status.OK, Activator.PLUGIN_ID, "Code successfully generated!");
 			}
 
 			private void buildComponentFolderStructure(JoomlaGenModel genModel, IProject project, IProgressMonitor monitor) throws CoreException {
 				// Create folder for Front-End (FE)
 				Utils.getFolder(project.getFolder("site"), monitor);
 				
 				// Create folder for FE models
 				Utils.getFolder(project.getFolder("site/models"), monitor);
 				// Create folder for FE views
 				Utils.getFolder(project.getFolder("site/views"), monitor);
 				// Create folder for FE views
 				Utils.getFolder(project.getFolder("site/controllers"), monitor);
 				
 				// Create folders for component Media
 				Utils.getFolder(project.getFolder("media"), monitor);
 				IFolder imagesFolder = Utils.getFolder(project.getFolder("media/images"), monitor);
 				Utils.getFolder(project.getFolder("media/js"), monitor);
 				IFolder cssFolder = Utils.getFolder(project.getFolder("media/css"), monitor);
 				
 				// Get all the Static Images which are not URLs
 				getStaticImages(genModel, imagesFolder, monitor);
 				// Get all CSS files
 				getStyles(genModel, cssFolder, monitor);
 				// Get all the images within the initial data archive
 				getInitialDataImages(genModel, imagesFolder, monitor);
 				
 				// Create folder for Back-End (BE)
 				Utils.getFolder(project.getFolder("admin"), monitor);
 				
 				// Create folder for BE models
 				IFolder modelsFodler = Utils.getFolder(project.getFolder("admin/models"), monitor);
 				// Create folder for BE views
 				Utils.getFolder(project.getFolder("admin/views"), monitor);
 				// Create folder for BE controllers
 				Utils.getFolder(project.getFolder("admin/controllers"), monitor);
 				
 				// Create folder for BE forms
 				Utils.getFolder(modelsFodler.getFolder("forms"), monitor);
 				
 				// Create 'tables' folder
 				Utils.getFolder(project.getFolder("admin/tables"), monitor);
 				
 				// Create 'sql' folder
 				Utils.getFolder(project.getFolder("admin/sql"), monitor);
 				
				// Create 'updates' folder
				Utils.getFolder(project.getFolder("admin/sql/updates"), monitor);
				
				// Create 'mysql' update folder
				Utils.getFolder(project.getFolder("admin/sql/updates/mysql"), monitor);
				
 				// Create 'helpers' folder
 				Utils.getFolder(project.getFolder("admin/helpers"), monitor);
 			}
 						
 			private void getInitialDataImages(JoomlaGenModel genModel, IFolder imagesFolder, IProgressMonitor monitor) throws CoreException {
 				// Do we have any data to process?
 				String data = genModel.getInitialData();
 				if (data == null || data.isEmpty()) {
 					return;
 				}
 				
 				ZipFile zf = null;
 				try {
 					zf = new ZipFile(data);
 					
 					Enumeration<? extends ZipEntry> entry = zf.entries();
 					while (entry.hasMoreElements()) {
 						ZipEntry ze = entry.nextElement();
 						if (!ze.isDirectory()) {					
 							String name = ze.getName();
 							if (name.toLowerCase().matches(".*(.jpg|.jpeg|.png|.gif)+")) {
 								String[] path = name.split("/");
 								copyFile(path[path.length-1], zf.getInputStream(ze), imagesFolder, monitor);
 							}
 						}
 					}
 				} catch (Exception e) {
 					throw new CoreException(new Status(
 							Status.ERROR,
 							Activator.PLUGIN_ID,
 							"Could not copy images within initial data archive to media/images folder: "+ e.getMessage()));
 				} finally {
 					if (zf != null) {
 						try {
 							zf.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}	
 				
 			}
 
 			private void getStaticImages(JoomlaGenModel genModel, IFolder folder, IProgressMonitor monitor) throws CoreException {
 				Website extension = genModel.getWebmodel();
 				if (extension != null) {
 					EList<Page> pages = extension.getPages();
 					for (Page page : pages) {
 						EList<PageElement> elements = page.getElements();
 						for (PageElement element : elements) {
 							if (element instanceof Image) {
 								Image sImg = (Image) element;
 								if (!sImg.isStatic())
 									continue;
 
 								String imgName = Utils.getFileName(sImg.getSource().toString());
 								try {
 									copyFile(imgName, new FileInputStream(sImg.getSource().toString()), folder, monitor);
 								} catch (FileNotFoundException e) {
 									throw new CoreException(new Status(
 											Status.ERROR,
 											Activator.PLUGIN_ID,
 											"Image at location: "
 													+ sImg.getSource()
 													+ " does not exist."));
 								}
 							}
 						}
 					}
 				}
 			}
 			
 			private void getStyles(JoomlaGenModel genModel, IFolder folder, IProgressMonitor monitor) throws CoreException {
 				String css = genModel.getCustomCSSFiles();
 				if (css != null && !css.isEmpty()) {
 					String[] paths = css.split(";");
 					for (String path: paths) {
 						String fileName = Utils.getFileName(path);
 						try {
 							copyFile(fileName, new FileInputStream(path), folder, monitor);
 						} catch (FileNotFoundException e) {
 							throw new CoreException(new Status(
 									Status.ERROR,
 									Activator.PLUGIN_ID,
 									"CSS file at location: "
 											+ path
 											+ " does not exist."));
 						}
 					}
 				}
 			}
 			
 			private void copyFile(String fileName, InputStream source, IFolder destination, IProgressMonitor monitor) throws CoreException {
 				IFile localFile = destination.getFile(fileName);
 				if (!localFile.exists()) {
 					localFile.create(source, true, monitor);
 				}
 			}
 		};
 		
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();
 	}
 }
