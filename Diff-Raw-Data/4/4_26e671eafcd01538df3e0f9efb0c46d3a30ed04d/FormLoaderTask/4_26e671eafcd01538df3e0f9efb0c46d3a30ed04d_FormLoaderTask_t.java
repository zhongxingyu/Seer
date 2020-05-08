 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.radicaldynamic.groupinform.tasks;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.ektorp.Attachment;
 import org.ektorp.AttachmentInputStream;
 import org.javarosa.core.model.FormDef;
 import org.javarosa.core.model.condition.EvaluationContext;
 import org.javarosa.core.model.instance.TreeElement;
 import org.javarosa.core.model.instance.TreeReference;
 import org.javarosa.core.services.PrototypeManager;
 import org.javarosa.core.util.externalizable.DeserializationException;
 import org.javarosa.core.util.externalizable.ExtUtil;
 import org.javarosa.form.api.FormEntryController;
 import org.javarosa.form.api.FormEntryModel;
 import org.javarosa.xform.parse.XFormParseException;
 import org.javarosa.xform.parse.XFormParser;
 import org.javarosa.xform.util.XFormUtils;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.documents.FormDocument;
 import com.radicaldynamic.groupinform.documents.InstanceDocument;
 import com.radicaldynamic.groupinform.listeners.FormLoaderListener;
 import com.radicaldynamic.groupinform.utilities.FileUtils;
 
 /**
  * Background task for loading a form.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  * @author Yaw Anokwa (yanokwa@gmail.com)
  */
 public class FormLoaderTask extends AsyncTask<String, String, FormLoaderTask.FECWrapper> {
     private static final String t = "FormLoaderTask: ";
     
     /**
      * Classes needed to serialize objects. Need to put anything from JR in here.
      */
     public final static String[] SERIALIABLE_CLASSES = {
         "org.javarosa.core.model.FormDef", "org.javarosa.core.model.GroupDef",
         "org.javarosa.core.model.QuestionDef", "org.javarosa.core.model.data.DateData",
         "org.javarosa.core.model.data.DateTimeData",
         "org.javarosa.core.model.data.DecimalData",
         "org.javarosa.core.model.data.GeoPointData",
         "org.javarosa.core.model.data.helper.BasicDataPointer",
         "org.javarosa.core.model.data.IntegerData",
         "org.javarosa.core.model.data.MultiPointerAnswerData",
         "org.javarosa.core.model.data.PointerAnswerData",
         "org.javarosa.core.model.data.SelectMultiData",
         "org.javarosa.core.model.data.SelectOneData",
         "org.javarosa.core.model.data.StringData", "org.javarosa.core.model.data.TimeData",
         "org.javarosa.core.services.locale.TableLocaleSource",
         "org.javarosa.xpath.expr.XPathArithExpr", "org.javarosa.xpath.expr.XPathBoolExpr",
         "org.javarosa.xpath.expr.XPathCmpExpr", "org.javarosa.xpath.expr.XPathEqExpr",
         "org.javarosa.xpath.expr.XPathFilterExpr", "org.javarosa.xpath.expr.XPathFuncExpr",
         "org.javarosa.xpath.expr.XPathNumericLiteral",
         "org.javarosa.xpath.expr.XPathNumNegExpr", "org.javarosa.xpath.expr.XPathPathExpr",
         "org.javarosa.xpath.expr.XPathStringLiteral",
         "org.javarosa.xpath.expr.XPathUnionExpr",
         "org.javarosa.xpath.expr.XPathVariableReference"
     };
     
     FormLoaderListener mStateListener;
     String mErrorMsg = "unexpected error";
 
     protected class FECWrapper {
         FormEntryController controller;
 
         protected FECWrapper(FormEntryController controller) {
             this.controller = controller;
         }
 
         protected FormEntryController getController() {
             return controller;
         }
 
         protected void free() {
             controller = null;
         }
     }
 
     FECWrapper data;
 
     /**
      * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
      * an instance, it will be used to fill the {@link FormDef}.
      */
     @Override
     protected FECWrapper doInBackground(String... ids) {
         FormEntryController fec = null;
         FormDef fd = null;
         
         String formId = ids[0];
         String instanceId = ids[1];
         
         // TODO: we need to handle what happens when a form document no longer exists
         // or perhaps we don't do that here at all...
         FormDocument form = Collect.getInstance().getDbService().getDb().get(FormDocument.class, formId);                
         File formBin = new File(FileUtils.FORMS_PATH + formId + ".formdef");
         
         Log.i(Collect.LOGTAG, t + formId + ": loading form named " + form.getName());
 
         if (formBin.exists() && formBin.lastModified() < form.getDateUpdatedAsCalendar().getTimeInMillis()) {
             /*
              * The cache is stale with regards to the XML so delete the cache file.
              * This is mainly used for development but could be more important going
              * forward if users are updating or adding IAV features to existing forms.
              */
         	Log.d(Collect.LOGTAG, t + formId + ": removing stale form cache file");
         	formBin.delete();
         }
             
         // If we have binary then attempt to deserialize it
         if (formBin.exists()) {        	
         	try {
         	    Log.d(Collect.LOGTAG, t + formId + ": loading serialized form binary");
         		fd = deserializeFormDef(formBin);
         	} catch (Exception e) {
                 /*
                  * If it did not load delete the file and read the XML directly
                  * 
                  * The common case here is that the JavaRosa library that serialized the binary is 
                  * incompatible with the JavaRosa library that is now attempting to deserialize it.
                  */        	    
         	    Log.w(Collect.LOGTAG, t + formId + ": serialized form binary failed to load (deleting cache file): " + e.toString());
         		formBin.delete();
         	}
         }
         
         // Either a binary wasn't present or didn't load -- read directly from XML
         if (fd == null) {            
             try {
             	Log.d(Collect.LOGTAG, t + formId + ": attempting read of " + form.getName() + " XML attachment");
             	
             	AttachmentInputStream ais = Collect.getInstance().getDbService().getDb().getAttachment(formId, "xml");
             	fd = XFormUtils.getFormFromInputStream(ais);
             	ais.close();            	            	
             	
                 if (fd == null) {
                     Log.e(Collect.LOGTAG, t + formId + ": failed to load form definition from XML");
                     mErrorMsg = "Error reading XForm file";
                     return null;
                 } else {                
                     serializeFormDef(fd, formId);
                 }
             } catch (XFormParseException e) {
                 Log.e(Collect.LOGTAG, t + formId + ": failed to load form definition from XML: " + e.toString());
                 mErrorMsg = e.getMessage();
                 e.printStackTrace();
                 return null;
             } catch (Exception e) {
                 Log.e(Collect.LOGTAG, t + formId + ": failed to load form definition from XML: " + e.toString());
                 mErrorMsg = e.getMessage();
                 e.printStackTrace();
                 return null;
             }
         }
 
         // New evaluation context for function handlers
         EvaluationContext ec = new EvaluationContext();
         fd.setEvaluationContext(ec);
 
         // Create FormEntryController from form definition
         FormEntryModel fem = new FormEntryModel(fd);
         fec = new FormEntryController(fem);
 
         // Import existing data into form definition
         try {            
 	        if (instanceId == null) {
 	            Log.d(Collect.LOGTAG, t + formId + ": new instance");
 	            fd.initialize(true);
 	        } else {
 	            // Import data, then initialise (this order is important)
 	            Log.d(Collect.LOGTAG, t + formId + ": existing instance");
                 importData(formId, instanceId, fec);
                 fd.initialize(false);
 	        }
         } catch (Exception e) {
             Log.e(Collect.LOGTAG, t + formId + ": failed loading data into form definition: " + e.toString());
         	e.printStackTrace();
         	
             this.publishProgress(Collect.getInstance().getString(R.string.load_error, form.getName()) + " : " + e.getMessage());
 
         	return null;
         }
 
         Collect.getInstance().registerMediaPath(FileUtils.CACHE_PATH + formId + ".");
 
         fd = null;
         formBin = null;
         form = null;
         formId = null;
         instanceId = null;
 
         data = new FECWrapper(fec);
         
         return data;
     }
 
     @Override
     protected void onPostExecute(FECWrapper wrapper) {
         synchronized (this) {
             if (mStateListener != null) {
                 if (wrapper == null) {
                     mStateListener.loadingError(mErrorMsg);
                 } else {
                     mStateListener.loadingComplete(wrapper.getController());    
                 }
             }
                 
         }
     }
 
     public boolean importData(String formId, String instanceId, FormEntryController fec) throws IOException {
         Log.d(Collect.LOGTAG, t + formId + ": importing instance " + instanceId);
         
         // Retrieve instance XML attachment from database
         AttachmentInputStream ais = Collect.getInstance().getDbService().getDb().getAttachment(instanceId, "xml");
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         byte[] buffer = new byte[8192];
         int bytesRead;
         
         while ((bytesRead = ais.read(buffer)) != -1) {
             output.write(buffer, 0, bytesRead);
         }
         
         ais.close();        
 
         // Get the root of the saved and template instances
         TreeElement savedRoot = XFormParser.restoreDataModel(output.toByteArray(), null).getRoot();
         TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);
         
         output.close();
 
         // Weak check for matching forms
         if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
             Log.e(Collect.LOGTAG, t + formId + ": saved form instance does not match template form definition");
             return false;
         } else {
             // Populate the data model
             TreeReference tr = TreeReference.rootRef();
             tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);
             templateRoot.populate(savedRoot, fec.getModel().getForm());
 
             // Populated model to current form
             fec.getModel().getForm().getInstance().setRoot(templateRoot);
 
             /*
              * Fix any language issues
              * http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
              */
             if (fec.getModel().getLanguages() != null) {
                 fec.getModel().getForm().localeChanged(fec.getModel().getLanguage(), fec.getModel().getForm().getLocalizer());
             }
             
             // Also download any media attachments
             InstanceDocument instance = Collect.getInstance().getDbService().getDb().get(InstanceDocument.class, instanceId);            
             HashMap<String, Attachment> attachments = (HashMap<String, Attachment>) instance.getAttachments();
             
             for (Entry<String, Attachment> entry : attachments.entrySet()) {
                 String key = entry.getKey();
                 
                 // Do not download XML attachments (these are loaded directly into the form model)
                 if (!key.equals("xml")) {
                     ais = Collect.getInstance().getDbService().getDb().getAttachment(instanceId, key);                  
                     
                     FileOutputStream file = new FileOutputStream(new File(FileUtils.CACHE_PATH + key));
                     buffer = new byte[8192];
                     bytesRead = 0;
                     
                     while ((bytesRead = ais.read(buffer)) != -1) {
                         file.write(buffer, 0, bytesRead);
                     }
                     
                     ais.close();
                     file.close();
                 }
             }
 
             return true;
         }
     }
 
     /**
      * Read serialized {@link FormDef} from file and recreate as object.
      * 
      * @param formDef serialized FormDef file
      * @return {@link FormDef} object
      */
     public FormDef deserializeFormDef(File formDef) {
         // TODO: any way to remove reliance on jrsp?
     	Log.i(Collect.LOGTAG, t + "attempting read of " + formDef.getAbsolutePath());
 
         // Need a list of classes that formDef uses
         PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
         FileInputStream fis = null;
         FormDef fd = null;
         DataInputStream dis = null;
         
         try {
             // Create new form definition
             fd = new FormDef();
             fis = new FileInputStream(formDef);
             dis = new DataInputStream(fis);
 
             // Read serialised form definition into new form definition
             fd.readExternal(dis, ExtUtil.defaultPrototypes());
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             fd = null;
         } catch (IOException e) {
             e.printStackTrace();
             fd = null;
         } catch (DeserializationException e) {
             e.printStackTrace();
             fd = null;
         } finally {
         	if ( dis != null ) {
         		try {
         			dis.close();
         		} catch ( IOException e ) {
         			// ignore...
         		}
         	}
         }        
 
         return fd;
     }
 
     /**
      * Write the FormDef to the file system as a binary blog.
      * 
      * @param filepath path to the form file
      */
     public void serializeFormDef(FormDef fd, String id) {
         Log.i(Collect.LOGTAG, t + id + ": serializing form as binary");
 
         // Calculate unique md5 identifier            
         File formDef = new File(FileUtils.FORMS_PATH + id + ".formdef");
 
         // If formDef does not exist, create one
         if (!formDef.exists()) {
             FileOutputStream fos;
 
             try {
                 fos = new FileOutputStream(formDef);
                 DataOutputStream dos = new DataOutputStream(fos);
                 fd.writeExternal(dos);
                 dos.flush();
                 dos.close();
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public void setFormLoaderListener(FormLoaderListener sl) {
         synchronized (this) {
             mStateListener = sl;
         }
     }
 
     public void destroy() {
         if (data != null) {
             data.free();
             data = null;
         }
     }
 
     @Override
     protected void onProgressUpdate(String... values) 
     {
         Toast.makeText(Collect.getInstance().getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
     }
 }
