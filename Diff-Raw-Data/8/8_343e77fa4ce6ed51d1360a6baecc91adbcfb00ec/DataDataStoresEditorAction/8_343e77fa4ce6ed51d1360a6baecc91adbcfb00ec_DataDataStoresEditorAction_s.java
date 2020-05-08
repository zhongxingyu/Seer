 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 package org.vfny.geoserver.action.data;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFactorySpi;
 import org.geotools.data.DataStoreFactorySpi.Param;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.form.data.DataDataStoresEditorForm;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.util.DataStoreUtils;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author rgould To change the template for this generated type comment go to
  *         Window>Preferences>Java>Code Generation>Code and Comments
  */
 public class DataDataStoresEditorAction extends ConfigAction {
     public ActionForward execute(ActionMapping mapping, ActionForm form,
         UserContainer user, HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         DataDataStoresEditorForm dataStoresForm = (DataDataStoresEditorForm) form;
 
         String dataStoreID = dataStoresForm.getDataStoreId();
         String namespace = dataStoresForm.getNamespaceId();
         String description = dataStoresForm.getDescription();
 
         DataConfig dataConfig = (DataConfig) getDataConfig();
         DataStoreConfig config = null;
 
         config = (DataStoreConfig) dataConfig.getDataStore(dataStoreID);
 
         if( config == null ) {
         	// we are creating a new one.
             dataConfig.addDataStore(getUserContainer(request).getDataStoreConfig());
             config = (DataStoreConfig) dataConfig.getDataStore(dataStoreID);            
         }
         
         // After extracting params into a map
         Map connectionParams = new HashMap(); // values used for connection
         Map paramTexts = new HashMap(); // values as stored
 
         Map params = dataStoresForm.getParams();
 
         DataStoreFactorySpi factory = config.getFactory();
         Param[] info = factory.getParametersInfo();
 
         // Convert Params into the kind of Map we actually need
         //
         for (Iterator i = params.keySet().iterator(); i.hasNext();) {
             String key = (String) i.next();
 
             Param param = DataStoreUtils.find(info, key);
 
             if (param == null) {
 
                 ActionErrors errors = new ActionErrors();
                 errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("error.cannotProcessConnectionParams"));
                 saveErrors(request, errors);
 
                 return mapping.findForward("config.data.store.editor");
             }
 
             Object value;
 
             try {
                 value = param.lookUp(params);
             } catch (IOException erp) {
 
                 ActionErrors errors = new ActionErrors();
                 errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("error.cannotProcessConnectionParams"));
                 saveErrors(request, errors);
 
                 return mapping.findForward("config.data.store.editor");
             }
 
            if (value != null) {
                 connectionParams.put(key, value);
 
                 String text = param.text(value);
                 paramTexts.put(key, text);
             }
         }
 
         // put magic namespace into the mix
         // not sure if we want to do this, as we want the full namespace, not
 	    //the id.  But getParams in DataStore may override this - ch
         connectionParams.put("namespace", dataStoresForm.getNamespaceId());
         paramTexts.put("namespace", dataStoresForm.getNamespaceId());
         
         //dump("editor", connectionParams );
         //dump("texts ",paramTexts );        
         if (!factory.canProcess(connectionParams)) {
             // We could not use these params!
             //
             ActionErrors errors = new ActionErrors();
             errors.add(ActionErrors.GLOBAL_ERROR,
                 new ActionError("error.cannotProcessConnectionParams"));
             saveErrors(request, errors);
 
             return mapping.findForward("config.data.store.editor");
         }
 
         try {
 	    ServletContext sc = request.getSession().getServletContext();
 	    Map niceParams = DataStoreUtils.getParams(connectionParams, sc);
             DataStore victim = factory.createDataStore(niceParams);
 
             if (victim == null) {
                 // We *really* could not use these params!
                 //
     
                 ActionErrors errors = new ActionErrors();
                 errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("error.invalidConnectionParams"));
                 saveErrors(request, errors);
 
                 return mapping.findForward("config.data.store.editor");
             }
             String typeNames[] = victim.getTypeNames();
             dump( "typeNames", typeNames );
         } catch (Throwable throwable) {
             throwable.printStackTrace();
 
             ActionErrors errors = new ActionErrors();
             errors.add(ActionErrors.GLOBAL_ERROR,
                 new ActionError("error.exception", throwable.getMessage()));
 
             saveErrors(request, errors);
 
             return mapping.findForward("config.data.store.editor");
         }
 
         boolean enabled = dataStoresForm.isEnabled();
 
         if (dataStoresForm.isEnabledChecked() == false) {
             enabled = false;
         }
 
         config.setEnabled(enabled);
         config.setNameSpaceId(namespace);
         config.setAbstract(description);
         config.setConnectionParams(paramTexts);
 
         dataConfig.addDataStore(config);
 
         getUserContainer(request).setDataStoreConfig(null);
         getApplicationState().notifyConfigChanged();
 
         return mapping.findForward("config.data.store");
     }
     /** Used to debug connection parameters */
     public void dump( String msg, Map params ){
     	if( msg != null ){
     		System.out.print( msg + " ");
     	}
     	System.out.print( ": { " );
     	for( Iterator i=params.entrySet().iterator(); i.hasNext();){
     		Map.Entry entry = (Map.Entry) i.next();
     		System.out.print( entry.getKey() );
     		System.out.print("=");
     		dump( entry.getValue() );
     		if( i.hasNext() ){
     			System.out.print( ", " );
     		}	
     	}
     	System.out.println( "}" );
     }
     public void dump( Object obj ){
     	if( obj==null){
 			System.out.print("null");
 		}
 		else if ( obj instanceof String){
 			System.out.print("\"");
 			System.out.print( obj );
 			System.out.print("\"");
 		}
 		else {
 			System.out.print( obj );
 		}
 		
     }
     public void dump( String msg, Object array[] ){
     	if( msg != null ){
     		System.out.print( msg + " ");
     	}
     	System.out.print( ": " );
     	if( array == null){
     		System.out.print( "null" );
     		return;
     	}
     	System.out.print( "(" );
     	for( int i=0; i<array.length; i++){
     		dump( array[i] );    	
     		if( i<array.length-1 ){
     			System.out.print( ", " );
     		}
 		}	
     	System.out.println( ")" );
     }
 }
