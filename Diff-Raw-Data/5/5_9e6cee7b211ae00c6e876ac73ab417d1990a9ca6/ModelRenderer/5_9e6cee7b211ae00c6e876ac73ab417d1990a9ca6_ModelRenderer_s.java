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
 package org.jdesktop.wonderland.client.jme.cellrenderer;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.Geometry;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import com.jme.scene.state.RenderState;
 import com.jme.scene.state.TextureState;
 import java.net.URL;
 import java.util.Map;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.mtgame.RenderUpdater;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.ModelCellComponent;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
 import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
 import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
 import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
 import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
 
 /**
  *
  * @author paulby
  */
 public class ModelRenderer extends BasicRenderer {
 
     private URL deployedModelURL;
     private Vector3f modelTranslation = null;
     private Quaternion modelRotation = null;
     private Vector3f modelScale = null;
 
     private ModelCellComponent modelComponent = null;
     private DeployedModel deployedModel = null;
 
     public ModelRenderer(Cell cell, URL deployedModelURL) {
         this(cell, deployedModelURL, null, null, null, null);
     }
 
     public ModelRenderer(Cell cell, ModelCellComponent modelComponent) {
         super(cell);
         this.modelComponent = modelComponent;
     }
 
     public ModelRenderer(Cell cell, DeployedModel deployedModel) {
         super(cell);
         this.deployedModel = deployedModel;
     }
 
     public ModelRenderer(Cell cell,
             URL deployedModelURL,
             Vector3f modelTranslation,
             Quaternion modelRotation,
             Vector3f modelScale,
             Map<String, Object> properties) {
         super(cell);
         this.deployedModelURL = deployedModelURL;
         this.modelTranslation = modelTranslation;
         this.modelRotation = modelRotation;
         this.modelScale = modelScale;
     }
 
     @Override
     protected Node createSceneGraph(Entity entity) {
         if (modelComponent!=null) {
             return modelComponent.loadModel(entity);
         }
 
         if (deployedModel!=null) {
             ModelLoader loader = deployedModel.getModelLoader();
             if (loader==null) {
                 logger.warning("No loader for model "+deployedModel.getModelURL());
                 return new Node("No Loader");
             }
             Node ret = loader.loadDeployedModel(deployedModel, entity);
             return ret;
         }
 
         ModelLoader loader = LoaderManager.getLoaderManager().getLoader(deployedModelURL);
         if (loader==null) {
             logger.warning("No loader for model "+deployedModel.getModelURL());
             return new Node("No Loader");
         }
         deployedModel = new DeployedModel(deployedModelURL, loader);
         deployedModel.setModelTranslation(modelTranslation);
         deployedModel.setModelRotation(modelRotation);
         deployedModel.setModelScale(modelScale);
 
         return loader.loadDeployedModel(deployedModel, entity);
     }
 
     @Override
     protected void cleanupSceneGraph(Entity entity) {
         RenderComponent rc = entity.getComponent(RenderComponent.class);
         if (rc!=null) {
             ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
 
                 public void update(Object arg0) {
                     TreeScan.findNode((Spatial) arg0, new ProcessNodeInterface() {
                         public boolean processNode(Spatial node) {
                             if (node instanceof Geometry) {
                                 ((Geometry)node).clearBuffers();
                                 TextureState ts = (TextureState) node.getRenderState(RenderState.RS_TEXTURE);
                                if (ts!=null)
                                    ts.deleteAll(true);
                             }
                             return true;
                         }
                     });
                 }
             }, rc.getSceneRoot());
         }
 
         deployedModel = null;
     }
 
 }
