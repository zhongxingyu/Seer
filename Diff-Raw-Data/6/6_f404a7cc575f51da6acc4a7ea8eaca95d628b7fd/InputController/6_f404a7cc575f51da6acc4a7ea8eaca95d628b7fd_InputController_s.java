 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.bh.controller;
 
 import org.bh.data.DTOAccessException;
 import org.bh.data.IDTO;
 import org.bh.gui.BHValidityEngine;
 import org.bh.gui.View;
 import org.bh.gui.swing.IBHComponent;
 
 /**
  *
  * @author Marco Hammel
  */
 public abstract class InputController extends Controller implements IInputController{
 
      /**
      * Referenz to the model
      * Can be null
      */
     private IDTO<?> model = null;
 
      /**
      * have to be used in case of a UI based and model driven mvc plugin
      * and register the plugin at the platform
      *
      * @param view a instance of a View subclass
      * @param model a instance of a dto
      */
     public InputController(View view, IDTO<?> model){
         super(view);
         this.model = model;
     }
 
     public InputController(){
     	super();
     }
     public InputController(View view){
         super(view);
     }
     public InputController(IDTO<?> model){
     	this(null, model);
     }
 
      /**
      * writes all data to its dto reference
      * @throws DTOAccessException
      */
     protected void saveAllToModel() throws DTOAccessException{
         log.debug("Plugin save to dto");
         this.model.setSandBoxMode(Boolean.TRUE);
         for(String key : this.bhMappingComponents.keySet()){
             this.model.put(key, this.typeConverter(this.bhMappingComponents.get(key).getValue()));
         }
     }
 
     /**
      * save specific component to model
      * @param comp
      * @throws DTOAccessException
      */
    protected void safeToModel(IBHComponent comp)throws DTOAccessException{
         log.debug("Plugin save to dto");
         this.model.setSandBoxMode(Boolean.TRUE);
         //TODO define typeconverter
         //this.model.put(comp.getKey(), comp.getValue());
     }
 
 
     /**
      * writes all dto values with a mathcing key in a IBHComponent to UI
      * @throws DTOAccessException
      */
    @Override
 	protected void loadAllToView()throws DTOAccessException{
         log.debug("Plugin load from dto in view");
         for(String key : this.bhMappingComponents.keySet()){
             //this.bhMappingComponents.get(key).setValue(this.model.get(key));
         }
     }
     /**
      *
      * @param key
      * @throws DTOAccessException
      * @throws ControllerException
      */
    @Override
 	protected void loadToView(String key) throws DTOAccessException, ControllerException{
         log.debug("Plugin load from dto in view");
         //this.bhMappingComponents.get(key).setValue(this.model.get(key));
     }
 
     public void setModel(IDTO<?> model) {
         this.model = model;
     }
 
     public IDTO<?> getModel() {
         return model;
     }
 
     @Override
 	protected BHValidityEngine getValidator(){
         return this.view.getValidator();
     }
 
 
 }
