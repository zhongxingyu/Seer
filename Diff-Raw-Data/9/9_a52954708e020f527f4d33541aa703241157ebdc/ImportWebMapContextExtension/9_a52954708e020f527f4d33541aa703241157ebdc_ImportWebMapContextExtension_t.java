 /* gvSIG. Sistema de Informacin Geogrfica de la Generalitat Valenciana
  *
  * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ibez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 
 /* CVS MESSAGES:
 *
 * $Id: ImportWebMapContextExtension.java 18964 2008-02-14 11:50:37Z jdominguez $
 * $Log$
 * Revision 1.11  2007-03-06 17:06:43  caballero
 * Exceptions
 *
 * Revision 1.10  2006/11/28 11:46:59  cesar
 * Add support to persist the document internal layout
 *
 * Revision 1.9  2006/09/27 13:28:51  jaume
 * *** empty log message ***
 *
 * Revision 1.8  2006/09/20 10:32:15  jaume
 * remembers last location
 *
 * Revision 1.7  2006/09/15 10:44:24  caballero
 * extensibilidad de documentos
 *
 * Revision 1.6  2006/09/11 15:57:38  jaume
 * now accepts loading cml files from command line
 *
 * Revision 1.5  2006/08/29 07:56:15  cesar
 * Rename the *View* family of classes to *Window* (ie: SingletonView to SingletonWindow, ViewInfo to WindowInfo, etc)
 *
 * Revision 1.4  2006/08/29 07:13:43  cesar
 * Rename class com.iver.andami.ui.mdiManager.View to com.iver.andami.ui.mdiManager.IWindow
 *
 * Revision 1.3  2006/07/21 10:31:05  jaume
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/03 11:10:54  jaume
 * *** empty log message ***
 *
 * Revision 1.1  2006/05/03 07:51:21  jaume
 * *** empty log message ***
 *
 * Revision 1.5  2006/05/02 16:12:12  jorpiell
 * Se ha cambiado la interfaz Extension por dos clases: una interfaz (IExtension) y una clase abstract(Extension). A partir de ahora todas las extensiones deben heredar de Extension
 *
 * Revision 1.4  2006/05/02 15:57:43  jaume
 * Few better javadoc
 *
 * Revision 1.3  2006/04/20 17:11:54  jaume
 * Attempting to export
 *
 * Revision 1.2  2006/04/19 16:34:29  jaume
 * *** empty log message ***
 *
 * Revision 1.1  2006/04/19 07:57:29  jaume
 * *** empty log message ***
 *
 * Revision 1.1  2006/04/12 17:10:53  jaume
 * *** empty log message ***
 *
 *
 */
 package com.iver.cit.gvsig.wmc;
 
 import java.awt.Component;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Date;
 
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.gvsig.gui.beans.swing.JFileChooser;
 import org.gvsig.remoteClient.wms.ICancellable;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.plugins.Extension;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.cit.gvsig.ProjectExtension;
 import com.iver.cit.gvsig.exceptions.layers.UnsupportedVersionLayerException;
 import com.iver.cit.gvsig.fmap.drivers.wms.FMapWMSDriver;
 import com.iver.cit.gvsig.fmap.exceptions.ImportMapContextException;
 import com.iver.cit.gvsig.fmap.layers.FLyrWMS;
 import com.iver.cit.gvsig.gui.panels.WebMapContextFileChooserAccessory;
 import com.iver.cit.gvsig.project.Project;
 import com.iver.cit.gvsig.project.ProjectFactory;
 import com.iver.cit.gvsig.project.documents.view.ProjectView;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 
 public class ImportWebMapContextExtension extends Extension {
 
 	public static final String WMC_FILECHOOSER_ID = "WMC_FILECHOOSER_ID";
 	private String lastPath = null;
 
 	public void initialize() {
 		String[] arguments = PluginServices.getArguments();
 		if (arguments[arguments.length-1].toLowerCase().endsWith(".cml")) {
 			File wmcFile = new File(arguments[arguments.length-1]);
 			if (!wmcFile.exists()) {
 				NotificationManager.addError(PluginServices.getText(this, "could_not_find_web_map_context_file"), new FileNotFoundException());
 				return;
 			}
 
 			readMapContextFile(wmcFile, null);
 		}
 	}
 
 	public void execute(String actionCommand) {
 		if (actionCommand.equals("IMPORT")) {
 			JFileChooser fc = new JFileChooser(WMC_FILECHOOSER_ID, lastPath);
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.isDirectory() || f.getAbsolutePath().toLowerCase().endsWith(WebMapContext.FILE_EXTENSION);
 				}
 
 				public String getDescription() {
					return PluginServices.getText(this, "ogc_mapcontext_file");
 				}
 			});
 			IWindow v = PluginServices.getMDIManager().getActiveWindow();
 
 			// If the current active view is a gvSIG's view, we'll keep its name to
 			// show it at the JFileChooser's accessory.
 			String currentViewName;
 			if (v instanceof View)
 				currentViewName = ((View) v).getModel().getName();
 			else
 				currentViewName = null;
 
 			// Create an accessory for the Web Map Context's file chooser.
 			WebMapContextFileChooserAccessory acc = new WebMapContextFileChooserAccessory(currentViewName);
 
 			// Add the accessory to the file chooser
 			fc.setAccessory(acc);
 
 			// Nothing else than Web Map Context files allowed
 			fc.setAcceptAllFileFilterUsed(false);
 
 			if (fc.showOpenDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION){
 				File f = fc.getSelectedFile();
 				readMapContextFile(f, acc.getSelectedView());
 				String fileName = f.getAbsolutePath();
 				lastPath  = fileName.substring(0, fileName.lastIndexOf(File.separatorChar));
 
 			}
 			fc = null;
 		}
 	}
 
 	/**
 	 * Reads a WebMapContext (.cml) file and loads its layers into the destination
 	 * view.
 	 * @param wmcFile
 	 * @param dstView
 	 */
 	private void readMapContextFile(File wmcFile, ProjectView dstView) {
 		WebMapContext wmc = new WebMapContext();
 		try {
 			wmc.readFile(wmcFile);
 		} catch (UnsupportedVersionLayerException e) {
 			JOptionPane.showMessageDialog(
 					(Component) PluginServices.getMainFrame(),
 					e.getMessage(),
 					PluginServices.getText(this, "unsupported_version"),
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		} catch (ImportMapContextException e) {
 			e.printStackTrace();
 			if (e.isCritical()) {
 				// import impossible: show message and quit
 				JOptionPane.showMessageDialog(
 						(Component) PluginServices.getMainFrame(),
 						e.getMessage(),
 						PluginServices.getText(this, "problems_encountered_while_importing"),
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			} else {
 				JOptionPane.showMessageDialog(
 						(Component) PluginServices.getMainFrame(),
 						e.getMessage() + "\n\n" +
 						PluginServices.getText(this, "edit_layer_properties_to_fix_them"),
 						PluginServices.getText(this, "problems_encountered_while_importing"),
 						JOptionPane.ERROR_MESSAGE);
 			}
 		}
 
 		if (dstView == null) {
 			// Since the destination view is null, a new one will be added to
 			// the project.
 			dstView = ProjectFactory.createView(null);
 			dstView.setName(wmc.title);
 			dstView.setComment("Created from WebMapContext file: "+wmcFile.getName());
 
 			ProjectExtension pe = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
 			Project theProject = pe.getProject();
 			theProject.addDocument(dstView);
 
 			// show the view in a new window
 			View v = null;
 			v = new View();
 			v.initialize();
 			v.setModel(dstView);
 			PluginServices.getMDIManager().addWindow(v);
 		}
 
 		for (int i = 0; i < wmc.layerList.size(); i++) {
 			// WMS layers
 			if (wmc.layerList.get(i) instanceof FLyrWMS) {
 				FLyrWMS layer = (FLyrWMS) wmc.layerList.get(i);
 				/*
 				 * will connect to get the online resources defined by
 				 * server, because WMC only defines the original URL for
 				 * the server but not for the operations.
 				 */
 				try {
 					FMapWMSDriver dr = layer.getDriver();
 					dr.connect(null);
 					dr.getCapabilities(layer.getHost());
 					layer.setOnlineResources(dr.getOnlineResources());
 				} catch (Exception e) {
 					NotificationManager.addInfo(PluginServices.getText(this, "connect_error")+"\n"+
 							PluginServices.getText(this, "failed_restoring_online_resource_values")+ 
 					" ["+new Date(System.currentTimeMillis()).toString()+"]",
 					e);
 				}
 				dstView.getMapContext().getLayers().addLayer(layer);
 			}
 		}
 	}
 
 	public boolean isEnabled() {
 		return true;
 	}
 
 	public boolean isVisible() {
 		return true;
 	}
 
 }
