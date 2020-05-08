 package com.deviceteam.kezdet.interfaces;
 
 import java.util.Hashtable;
 
 import android.app.Activity;
 import android.content.Context;
 
 /**
  * 
 * @author petere
  *
  */
 public abstract interface IPlugin
 {
   /**
    * Enumeration which defines the available response types that the plugin supports.
    */
   enum EventDataResponseType
   {
     NONE,
     BINARY,
     JSON
   };
 
   /**
    * Initialises the plugin
    * @param context Android Context object
    * @param activity Android Activity object
    * @param callback Notification callback the plugin will use to provide feedback 
    */
   void initialise(Context context, Activity activity, IPluginCallback callback);
   
   /**
    * Registers all available plugin methods with the host
    * @param methods Hashtable of name to IInvokeMethod references that the plugin uses to register its provided methods against.
    */
   void registerMethods( Hashtable< String, IInvokeMethod > methods );
   
   /**
    * Cleanup method used to dispose any resources acquired during the lifetime of the plugin
    */
   void dispose();
 
   /**
    * Call this method to determine what type of data the plugin will supply
    * @return The plugin response data type
    */
   EventDataResponseType getReponseType();
   
   /**
    * Returns plugin data in a binary format. Will throw UnsupportedOperationException if the plugin does not support this format.
    * @return plugin data in binary format
    * @throws UnsupportedOperationException if the plugin does not support this format
    */
   byte[] getBinaryData() throws UnsupportedOperationException;
   
   /**
    * Returns plugin data in a JSON object format. Will throw UnsupportedOperationException if the plugin does not support this format.
    * @return plugin data in JSON object format
    * @throws UnsupportedOperationException if the plugin does not support this format
    */
   String getJSONData() throws UnsupportedOperationException;
 }
