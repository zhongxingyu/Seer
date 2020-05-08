 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 package org.vfny.geoserver.action.data;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.feature.FeatureType;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.FeatureTypeConfig;
 import org.vfny.geoserver.form.data.AttributeDisplay;
 import org.vfny.geoserver.form.data.AttributeForm;
 import org.vfny.geoserver.form.data.TypesEditorForm;
 import org.vfny.geoserver.global.UserContainer;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 /**
  * These Action handles all the buttons for the FeatureType Editor.
  * <p>
  * This one is more complicated then usual since not all the actions require
  * the form bean to be validated! I am going to have to hack a little bit
  * to make that happen, I may end up making the form bean validation differ
  * depending on the selected action.
  * </p>
  * <p>
  * Buttons that make this action go:
  * <ul>
  * <li>Submit: update the FeatureTypeConfig held by the user, punt it back into
  *     DataConfig and return to the FeatureTypeSelect screen.
  *     </li>
  * <li>Up and Down (for each attribute): not quite sure how to make these work
  *     yet - I hope I dont have to give them different names.
  *     </li>
  * </ul>
  * As usual we will have to uninternationlize the action name provided to us.
  * </p>
  * @author Richard Gould
  * @author Jody Garnett
  */
 public class TypesEditorAction extends ConfigAction {
     public ActionForward execute(ActionMapping mapping, ActionForm form,
         UserContainer user, HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         
        TypesEditorForm typeForm = (TypesEditorForm) form;        
         String action = typeForm.getAction();
         System.out.println("TypesEditorAction is "+action );
         if( action.equals("Submit")){
             return executeSubmit(mapping, typeForm, user, request);
         }
         if( action.startsWith("Up")){
             // TODO: implement Attribute Up
         }
         if( action.startsWith("Down")){
             // TODO: implement Attribute Down        
         }
         if( action.startsWith("remove")){
             // TODO: implement Attribute Remove            
         }
         
         // Update, Up, Down, All need to resync
         sync( typeForm, user.getFeatureTypeConfig() );
         form.reset( mapping, request );
         return mapping.findForward("config.data.type.editor");        
     }    
     /**
      * Sync generated attributes with schemaBase.
      * @param mapping
      * @param form
      * @param user
      * @param request
      * @return
      */
     private void sync(TypesEditorForm form, FeatureTypeConfig config ) {
         
         config.setAbstract(form.getAbstract());
         config.setName(form.getName());
         config.setSRS(Integer.parseInt(form.getSRS()));
         config.setTitle(form.getTitle());
         config.setLatLongBBox(getBoundingBox(form));        
         config.setKeywords(keyWords(form));
         
         String schemaBase = form.getSchemaBase();        
         if( schemaBase == null || schemaBase.equals("") || schemaBase.equals("--")){
             config.setSchemaBase( null );
             config.setSchemaAttributes( null );
         }
         else {
             config.setSchemaBase( schemaBase );
             for( Iterator i=form.getAttributes().iterator(); i.hasNext();){
                 Object obj = i.next();
                 
                 if( obj instanceof AttributeDisplay ){
                     continue; // skip - display only attributes
                 }
                 else if (obj instanceof AttributeForm ){
                     AttributeForm attribute = (AttributeForm) obj;
                 }
             }
         }
         form.getAttributes();
         config.setSchemaAttributes( form.toSchemaAttributes() );                
     }
     /**
      * Execute Submit Action.
      * 
 	 * @param mapping
 	 * @param form
 	 * @param user
 	 * @param request
 	 * @return
 	 */
 	private ActionForward executeSubmit(ActionMapping mapping, TypesEditorForm form, UserContainer user, HttpServletRequest request) {
         FeatureTypeConfig config = user.getFeatureTypeConfig();
         sync( form, config );
 
         DataConfig dataConfig = (DataConfig) getDataConfig();        
         dataConfig.addFeatureType( config.getName(), config );
 
         // Don't think reset is needed (as me have moved on to new page)
         // form.reset(mapping, request);
         
         getApplicationState().notifyConfigChanged();
         // Feature no longer selected
         user.setFeatureTypeConfig( null );        
         return mapping.findForward("config.data.type");
 	}
 	/**
 	 * @param typeForm
 	 * @return Bounding box in lat long
 	 */
 	private Envelope getBoundingBox(TypesEditorForm typeForm) {
 		return new Envelope(Double.parseDouble(typeForm.getMinX()), 
                             Double.parseDouble(typeForm.getMinY()),
                             Double.parseDouble(typeForm.getMaxX()),
                             Double.parseDouble(typeForm.getMaxY()));
 	}
 
 	/**
 	 * @param typeForm
 	 * @return Set of keywords
 	 */
 	private Set keyWords(TypesEditorForm typeForm) {
 		HashSet keywords = new HashSet();
         String[] array = (typeForm.getKeywords() != null)
             ? typeForm.getKeywords().split(System.getProperty("line.separator")) : new String[0];
 
         for (int i = 0; i < array.length; i++) {
             keywords.add(array[i]);
         }
 		return keywords;
 	}
 
 	DataStore aquireDataStore(String dataStoreID) throws IOException {
         DataConfig dataConfig = getDataConfig();
         DataStoreConfig dataStoreConfig = dataConfig.getDataStore(dataStoreID);
 
         Map params = dataStoreConfig.getConnectionParams();
 
         return DataStoreFinder.getDataStore(params);
     }
 
     FeatureType getSchema(String dataStoreID, String typeName)
         throws IOException {
         DataStore dataStore = aquireDataStore(dataStoreID);
         FeatureType type;
 
         return dataStore.getSchema(typeName);
     }
 }
