 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.jmecolladaloader.client;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingSphere;
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import org.jdesktop.wonderland.client.jme.artimport.*;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
 import org.jdesktop.wonderland.client.cell.utils.CellUtils;
 import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.common.FileUtils;
 import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.common.cell.state.ModelCellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.ModelCellServerState;
 import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Bounds;
 import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
 import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
 import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellComponentServerState;
 
 /**
  * A content importer handler for ModelLoaders
  *
  * @author paulby
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class ModelDndContentImporter implements ContentImporterSPI {
 
     private static Logger logger = Logger.getLogger(ModelDndContentImporter.class.getName());
     private ServerSessionManager loginInfo = null;
     private String[] extensions;
 
     /** Constructor, takes the login information */
     public ModelDndContentImporter(ServerSessionManager loginInfo, String[] extensions) {
         this.loginInfo = loginInfo;
         this.extensions = extensions;
     }
 
     @Override
     public String importFile(File file, String extension) {
 
         final JFrame frame = JmeClientMain.getFrame().getFrame();
 
         // Next check whether the content already exists and ask the user if
         // the upload should still proceed. By default (result == 0), the
         // system will upload (and overwrite) and files.
         int result = JOptionPane.YES_OPTION;
         ContentResource resource = isContentExists(file);
         if (resource != null) {
             Object[] options = {"Replace", "Use Existing", "Cancel" };
             String msg = "The file " + file.getName() + " already exists in" +
                     "the content repository. Do you wish to replace it and " +
                     "continue?";
             String title = "Replace Content?";
             result = JOptionPane.showOptionDialog(frame, msg, title,
                     JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
             // If the user hits Cancel or a "closed" action (e.g. Escape key)
             // then just return
             if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                 return null;
             }
         }
 
         // If the content exists and we do not want to upload a new version,
         // then simply create it and return.
         if (result == JOptionPane.NO_OPTION) {
             URL url = null;
             try {
                 url = resource.getURL();
                 LoaderManager manager = LoaderManager.getLoaderManager();
                 DeployedModel dm = manager.getLoaderFromDeployment(url);
                 createCell(dm);
                 return dm.getModelURL();
             } catch (java.lang.Exception excp) {
                 logger.log(Level.WARNING, "Unable to load existing model, url=" +
                         url, excp);
                 JOptionPane.showMessageDialog(frame,
                         "Failed to display existing model " + file.getAbsolutePath(),
                         "Display Failed",
                         JOptionPane.ERROR_MESSAGE);
                 return null;
             }
         }
 
         // Display a dialog showing a wait message while we import. We need
         // to do this in the SwingWorker thread so it gets drawn
         JOptionPane waitMsg = new JOptionPane("Please wait while " +
                 file.getName() + " is being uploaded");
         final JDialog dialog = waitMsg.createDialog(frame, "Uploading Content");
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 dialog.setVisible(true);
             }
         });
 
         // Next, do the actual upload of the file. This should display a
         // progress dialog if the upload is going to take a long time.
         DeployedModel deployedModel;
         try {
             deployedModel = modelUploadContent(file);
         } catch (java.io.IOException excp) {
             logger.log(Level.WARNING, "Failed to upload content file " +
                     file.getAbsolutePath(), excp);
 
             final String fileName = file.getName();
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     dialog.setVisible(false);
                     JOptionPane.showMessageDialog(frame,
                             "Failed to upload content file " + fileName,
                             "Upload Failed", JOptionPane.ERROR_MESSAGE);
                 }
             });
             return null;
         } finally {
             // Close down the dialog indicating success
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     dialog.setVisible(false);
                 }
             });
         }
 
         // Finally, go ahead and create the cell.
         createCell(deployedModel);
         return deployedModel.getModelURL();
     }
 
     /**
      * Check to see if the model already exists on the server. If so, return
      * the ContentResource of the model's .dep file, or null otherwise.
      */
     private ContentResource isContentExists(File file) {
         // Check to see if the model already exists on the server. It should
         // be under a file named: <filename>/<filename>.dep. If so, then return
         // the ContentResource that points to the file, otherwise, return null.
         String fileName = "art/" + file.getName() + "/" + file.getName() + ".dep";
         ContentCollection userRoot = getUserRoot();
         try {
             ContentNode node = userRoot.getChild(fileName);
             if (node != null && node instanceof ContentResource) {
                 return (ContentResource)node;
             }
             return null;
         } catch (ContentRepositoryException excp) {
             logger.log(Level.WARNING, "Error while try to find " + fileName +
                     " in content repository", excp);
             return null;
         }
     }
 
     /**
      * @inheritDoc()
      */
     public DeployedModel modelUploadContent(File file) throws IOException {
         URL url = file.toURI().toURL();
         ModelLoader loader = LoaderManager.getLoaderManager().getLoader(url);
         ImportSettings importSettings = new ImportSettings(url);
         ImportedModel importedModel = loader.importModel(importSettings);
 
         File tmpDir = File.createTempFile("dndart", null);
         if (tmpDir.isDirectory()) {
             FileUtils.deleteDirContents(tmpDir);
         } else {
             tmpDir.delete();
         }
         tmpDir = new File(tmpDir, file.getName());
         tmpDir.mkdirs();
         System.err.println("DEPLOYING TO "+tmpDir);
 
         // Create a fake entity, which will be used to calculate the model offset
         // from the cell
         Node cellRoot = new Node();
         cellRoot.attachChild(importedModel.getModelBG());
         cellRoot.updateWorldData(0f);
         cellRoot.updateWorldBound();
         Entity entity = new Entity("Fake");
         RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(cellRoot);
         entity.addComponent(RenderComponent.class, rc);
         importedModel.setEntity(entity);
         importedModel.setDeploymentBaseURL("wlcontent://users/" + loginInfo.getUsername() + "/art/");
        String filename = file.getAbsolutePath();
        filename = filename.substring(
                filename.lastIndexOf(File.separatorChar) + 1);
        filename = filename.substring(0, filename.lastIndexOf('.'));
        importedModel.setWonderlandName(filename);
 
         DeployedModel deployedModel = loader.deployToModule(tmpDir, importedModel);
 
         // Now copy the temporary files into webdav
 
         // Create the directory to hold the contents of the model. We place it
         // in a directory named after the kmz file. If the directory already
         // exists, then just use it.
         ContentCollection modelRoot = getUserRoot();
 
         try {
             // Copy from the art directory
             File artDir = FileUtils.findDir(tmpDir, "art");
             copyFiles(artDir, modelRoot);
         } catch (ContentRepositoryException ex) {
             Logger.getLogger(ModelDndContentImporter.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         URL tmpUrl = new URL(deployedModel.getModelURL());
         String modelFile = "art" + tmpUrl.getPath();
 
         ModelCellServerState cellState = (ModelCellServerState) deployedModel.getCellServerState();
         ModelCellComponentServerState compState = (ModelCellComponentServerState) cellState.getComponentServerState(ModelCellComponentServerState.class);
 //        compState.setDeployedModelURL("wlcontent://users/" + loginInfo.getUsername() + "/" + modelFile);
        cellState.setName(importedModel.getWonderlandName());
 
         System.err.println("DEPLOYED DEP FILE "+compState.getDeployedModelURL());
 
 //        deployedModel.setModelURL(compState.getDeployedModelURL()); // Make everything consistent with the state
 
         return deployedModel;
     }
 
     /**
      * Copies all files recursively from a local File to a remote content
      * collection, creating all of the necessary files and directories.
      */
     private void copyFiles(File f, ContentCollection n)
             throws ContentRepositoryException, IOException {
 
         // If the given File is a directory, then attempt to create it if it
         // does not exist and the recursively copy all of its contents
         String fName = f.getName();
         if (f.isDirectory() == true) {
             // We need to create the child directory if it does not yet exist.
             // If it does exist, but is not a collection, then we need to delete
             // the existing resource and create the new collection
             ContentNode node = n.getChild(fName);
             if (node == null) {
                 node = n.createChild(fName, Type.COLLECTION);
             }
             else if (!(node instanceof ContentCollection)) {
                 node.getParent().removeChild(node.getName());
                 node = n.createChild(fName, Type.COLLECTION);
             }
             ContentCollection dir = (ContentCollection)node;
 
             // Recursively descend the children and copy them over too.
             File[] subdirs = f.listFiles();
             if (subdirs != null) {
                 for (File child : subdirs) {
                     copyFiles(child, dir);
                 }
             }
         } else {
             // For a file, create the file if it does not yet exist. If it does
             // exist, but is not a resource, then delete the existing node and
             // create a new resource
             ContentNode node = n.getChild(fName);
             if (node == null) {
                 node = n.createChild(fName, Type.RESOURCE);
             }
             else if (!(node instanceof ContentResource)) {
                 node.getParent().removeChild(node.getName());
                 node = n.createChild(fName, Type.RESOURCE);
             }
             ContentResource resource = (ContentResource)node;
             resource.put(f);
         }
     }
 
     /**
      * @inheritDoc()
      */
     public String[] getExtensions() {
         return extensions;
     }
 
     /**
      * Returns the content repository root for the current user, or null upon
      * error.
      */
     private ContentCollection getUserRoot() {
         ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
         ContentRepository repo = registry.getRepository(loginInfo);
         try {
             return repo.getUserRoot();
         } catch (ContentRepositoryException excp) {
             logger.log(Level.WARNING, "Unable to find repository root", excp);
             return null;
         }
     }
 
     public void createCell(DeployedModel deployedModel) {
         // Fetch the bounds of the Cell and use it as the bounds hint.
         CellServerState state = deployedModel.getCellServerState();
         PositionComponentServerState pcss =
                 (PositionComponentServerState) state.getComponentServerState(PositionComponentServerState.class);
         BoundingVolume boundsHint = null;
         if (pcss != null) {
             Bounds bounds = pcss.getBounds();
             if (bounds.type == Bounds.BoundsType.BOX) {
                 boundsHint = new BoundingBox(Vector3f.ZERO, (float)bounds.x,
                         (float)bounds.y, (float)bounds.z);
             }
             else {
                 boundsHint = new BoundingSphere((float)bounds.x, Vector3f.ZERO);
             }
         }
         BoundingVolumeHint hint = new BoundingVolumeHint(true, boundsHint);
         state.setBoundingVolumeHint(hint);
 
         try {
             CellUtils.createCell(state);
         } catch (CellCreationException excp) {
             logger.log(Level.WARNING, "Unable to create cell for uri " + deployedModel.getModelURL(), excp);
         }
     }
 }
