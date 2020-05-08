 /*
  * Representation.java
  *
  * Created on June 4, 2007, 3:09 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics;
 
 import com.jme.light.Light;
 import com.jme.light.DirectionalLight;
 import com.jme.math.Vector3f;
 import com.jme.scene.Spatial;
 import edu.gatech.statics.objects.SimulationObject;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Node;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.MaterialState;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Connector;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.Point;
 
 /**
  *
  * @author Calvin Ashmore
  */
 abstract public class Representation<SimType extends SimulationObject> extends Node {
 
     private final SimType target;
 
     public SimType getTarget() {
         return target;
     }
     private RepresentationLayer layer;
 
     public RepresentationLayer getLayer() {
         return layer;
     }
 
     public void setLayer(final RepresentationLayer layer) {
         //assert this.layer == null : "Cannot re-assign representation layer!";
         this.layer = layer;
     }
     private boolean renderUpdated;
 
     public boolean getRenderUpdated() {
         return renderUpdated;
     }
 
     public void setRenderUpdated() {
         renderUpdated = true;
     }
     private MaterialState materialState;
     //public MaterialState getMaterialState() {return materialState;}
     private boolean hover;
     private boolean selected;
     private boolean grayed = false;
     private boolean hidden = false;
     private ColorRGBA ambient;// = new ColorRGBA(.2f, .2f, .2f, 1f);
     private ColorRGBA diffuse;// = new ColorRGBA(.8f, .8f, .8f, 1f);;
     //private ColorRGBA specular;
     //private ColorRGBA emissive = ColorRGBA.black;
     //private ColorRGBA selectEmissive = new ColorRGBA(.40f, .40f, .40f, 1f);
     //private ColorRGBA hoverEmissive = new ColorRGBA(.20f, .20f, .20f, 1f);
     private ColorRGBA selectDiffuse;
     private ColorRGBA hoverDiffuse;
     private ColorRGBA grayColor = new ColorRGBA(.2f, .2f, .2f, 1f);
     private ColorRGBA grayEmissive = new ColorRGBA(.40f, .40f, .40f, 1f);
     private boolean useWorldScale = true;
     private boolean synchronizeTranslation = true;
     private boolean synchronizeRotation = true;
     /**
      * Representation is a subclass of Node, and we want it to automatically update its
      * transformations. However, we may wish to have there be relative transformations
      * underneath the main one, and that is what this is for.
      */
     private Node relativeNode;
 
     public Node getRelativeNode() {
         return relativeNode;
     }
 
     /**
      * Use getRelativeNode().attachChild() instead
      * @param child
      * @return
      * @deprecated
      */
     @Override
     @Deprecated
     public int attachChild(Spatial child) {
         return super.attachChild(child);
     }
 
     public ColorRGBA getAmbient() {
         return ambient;
     }
 
     public ColorRGBA getDiffuse() {
         return diffuse;
     }
 
     /*public ColorRGBA getSpecular() {
     return specular;
     }
     
     public ColorRGBA getEmissive() {
     return emissive;
     }
     
     public ColorRGBA getSelectEmissive() {
     return selectEmissive;
     }
     
     public ColorRGBA getHoverEmissive() {
     return hoverEmissive;
     }
      */
     public ColorRGBA getSelectDiffuse() {
         return selectDiffuse;
     }
 
     public ColorRGBA getHoverDiffuse() {
         return hoverDiffuse;
     }
     /* 
     public ColorRGBA getGrayColor() {
     return grayColor;
     }
     
     public ColorRGBA getGrayEmissive() {
     return grayEmissive;
     }*/
 
     protected boolean useWorldScale() {
         return useWorldScale;
     }
 
     public void setSynchronizeTranslation(final boolean synch) {
         synchronizeTranslation = synch;
     }
 
     public void setSynchronizeRotation(final boolean synch) {
         synchronizeRotation = synch;
     }
 
     public void setUseWorldScale(boolean useScale) {
         useWorldScale = useScale;
     }
 
     /*public void setMaterial(final ColorRGBA ambient, final ColorRGBA diffuse, final ColorRGBA specular) {
     this.ambient = ambient;
     this.diffuse = diffuse;
     this.specular = specular;
     updateMaterial();
     }*/
     public void setAmbient(final ColorRGBA ambient) {
         this.ambient = ambient;
     }
 
     public void setDiffuse(final ColorRGBA diffuse) {
         this.diffuse = diffuse;
         this.selectDiffuse = diffuse;
         this.hoverDiffuse = diffuse;
     }
 
     /*public void setSpecular(final ColorRGBA specular) {
     this.specular = specular;
     }
     
     public void setEmissive(final ColorRGBA emissive) {
     this.emissive = emissive;
     }
     
     public void setSelectEmissive(final ColorRGBA selectEmissive) {
     this.selectEmissive = selectEmissive;
     }
     
     public void setHoverEmissive(final ColorRGBA hoverEmissive) {
     this.hoverEmissive = hoverEmissive;
     }
      */
     public void setSelectDiffuse(final ColorRGBA selectDiffuse) {
         this.selectDiffuse = selectDiffuse;
     }
 
     public void setHoverDiffuse(final ColorRGBA hoverDiffuse) {
         this.hoverDiffuse = hoverDiffuse;
     }
     /*
     public void setGrayColor(final ColorRGBA grayColor) {
     this.grayColor = grayColor;
     }
     
     public void setGrayEmissive(final ColorRGBA grayEmissive) {
     this.grayEmissive = grayEmissive;
     }*/
 
     /** Creates a new instance of Representation */
     public Representation(final SimType target) {
         this.target = target;
         relativeNode = new Node();
         attachChild(relativeNode);
 
         materialState = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
         materialState.setMaterialFace(MaterialState.MF_FRONT_AND_BACK);
         setRenderState(materialState);
         updateRenderState();
 
         ambient = new ColorRGBA(materialState.getAmbient());
         diffuse = new ColorRGBA(materialState.getDiffuse());
         //specular = new ColorRGBA(materialState.getSpecular());
 
         selectDiffuse = new ColorRGBA(materialState.getDiffuse());
         hoverDiffuse = new ColorRGBA(materialState.getDiffuse());
 
         lightState = DisplaySystem.getDisplaySystem().getRenderer().createLightState();
         setRenderState(lightState);
         DirectionalLight dLight = new DirectionalLight();
         dLight.setDirection(Vector3f.UNIT_Z.negate());
         light = dLight;
 
         //light.setAmbient(ColorRGBA.white);
         //light.setDiffuse(ColorRGBA.white);
         lightState.setTwoSidedLighting(true);
         //light.setDiffuse(ColorRGBA.green);
         lightState.attach(light);
         light.setEnabled(false);
 
         updateRenderState();
     }
 
     public void setDisplayGrayed(final boolean grayed) {
         this.grayed = grayed;
         updateMaterial();
     }
 
     public void setHidden(final boolean hidden) {
         this.hidden = hidden;
         updateMaterial();
     }
 
     public void setDisplaySelected(final boolean selected) {
         this.selected = selected;
         updateMaterial();
     }
 
     public void setDisplayHighlight(final boolean hover) {
         this.hover = hover;
         updateMaterial();
     }
 
     public void update() {
 
         if (useWorldScale) {
             if (target instanceof Moment) {
                 setLocalScale(StaticsApplication.getApp().getMomentScale());
             } else if (target instanceof Force) {
                 setLocalScale(StaticsApplication.getApp().getForceScale());
             } else if (target instanceof Point) {
                 setLocalScale(StaticsApplication.getApp().getPointScale());
             } else if (target instanceof Connector) {
                 setLocalScale(StaticsApplication.getApp().getJointScale());
             }
         }
         if (synchronizeTranslation) {
             setLocalTranslation(target.getTranslation());
         }
         if (synchronizeRotation) {
             setLocalRotation(target.getRotation());
         }
 
         updateMaterial();
     }
 
     @Override
     public void setCullMode(int mode) {
         super.setCullMode(mode);
     }
 
     public boolean isHidden() {
         return hidden;
     }
 
     private void updateMaterial() {
 
         if (selected) {
             //materialState.setEmissive(selectEmissive);
             //materialState.setAmbient(ambient);
             materialState.setDiffuse(selectDiffuse);
         //materialState.setSpecular(specular);
         } else if (grayed) {
             if (hover) {
                 //materialState.setEmissive(hoverEmissive);
             } else {
                 materialState.setEmissive(grayEmissive);
             }
 
             materialState.setAmbient(grayColor);
             materialState.setDiffuse(grayColor);
         //materialState.setSpecular(ColorRGBA.black);
 
         } else { // default appearance
             if (hover) {
                 //materialState.setEmissive(hoverEmissive);
                 materialState.setDiffuse(hoverDiffuse);
             } else {
             }
             materialState.setEmissive(ColorRGBA.black);
             materialState.setDiffuse(diffuse);
 
             materialState.setAmbient(ambient);
         //materialState.setSpecular(specular);
         }
 
         updateLights();
     }
     private LightState lightState = null;
     private Light light;
     private ColorRGBA selectLightColor = new ColorRGBA(.5f, .5f, .5f, 1);
     private ColorRGBA hoverLightColor = new ColorRGBA(.3f, .3f, .3f, 1);
 
     private void updateLights() {
 
         if (selected) {
             light.setEnabled(true);
            light.setAmbient(selectLightColor);
             light.setDiffuse(selectLightColor);
 
         } else if (hover) {
             light.setEnabled(true);
            light.setAmbient(hoverLightColor);
             light.setDiffuse(hoverLightColor);
 
         } else {
             light.setEnabled(false);
         }
     }
 }
