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
 
 package org.odk.collect.android.tasks;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
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
 import org.odk.collect.android.R;
 import org.odk.collect.android.application.Collect;
 import org.odk.collect.android.database.FileDbAdapter;
 import org.odk.collect.android.listeners.FormLoaderListener;
 import org.odk.collect.android.utilities.FileUtils;
 
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Background task for loading a form.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  * @author Yaw Anokwa (yanokwa@gmail.com)
  */
 public class FormLoaderTask extends AsyncTask<String, String, FormLoaderTask.FECWrapper> {
     private final static String t = "FormLoaderTask";
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
             "org.javarosa.xpath.expr.XPathStringLiteral", "org.javarosa.xpath.expr.XPathUnionExpr",
             "org.javarosa.xpath.expr.XPathVariableReference"
     };
 
     FormLoaderListener mStateListener;
     String mErrorMsg;
 
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
     protected FECWrapper doInBackground(String... path) {
         FormEntryController fec = null;
         FormDef fd = null;
         FileInputStream fis = null;
 
         String formPath = path[0];
         String instancePath = path[1];
        if ( formPath == null && instancePath != null ) {
        	String instanceName = (new File(instancePath)).getName();
        	this.publishProgress(Collect.getInstance().getString(R.string.load_error_no_form,
            		instanceName ));
    		return null;
        }
 
         File formXml = new File(formPath);
         String formHash = FileUtils.getMd5Hash(formXml);
         File formBin = new File(FileUtils.CACHE_PATH + formHash + ".formdef");
 
         if (formBin.exists()) {
             // if we have binary, deserialize binary
         	try {
         		Log.i(
                 t,
                 "Attempting to load " + formXml.getName() + " from cached file: "
                         + formBin.getAbsolutePath());
         		fd = deserializeFormDef(formBin);
         	} catch ( Exception e ) {
         		// didn't load -- 
         		// the common case here is that the javarosa library that 
         		// serialized the binary is incompatible with the javarosa
         		// library that is now attempting to deserialize it.
         	}
 
         	if (fd == null) {
                 // some error occured with deserialization. Remove the file, and make a new .formdef
                 // from xml
                 Log.w(t,
                     "Deserialization FAILED!  Deleting cache file: " + formBin.getAbsolutePath());
                 formBin.delete();
             }
         }
         if (fd == null) {
             // no binary, read from xml
             try {
                 Log.i(t, "Attempting to load from: " + formXml.getAbsolutePath());
                 fis = new FileInputStream(formXml);
                 fd = XFormUtils.getFormFromInputStream(fis);
                 if (fd == null) {
                     mErrorMsg = "Error reading XForm file";
                 } else {
                     serializeFormDef(fd, formPath);
                 }
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
                 mErrorMsg = e.getMessage();
             } catch (XFormParseException e) {
                 mErrorMsg = e.getMessage();
                 e.printStackTrace();
             } catch (Exception e) {
                 mErrorMsg = e.getMessage();
                 e.printStackTrace();
             } finally {
                 if (fd == null) {
                     // remove cache reference from file db if it exists
                     FileDbAdapter fda = new FileDbAdapter();
                     fda.open();
                     if (fda.deleteFile(null, formHash)) {
                         Log.i(t, "Cached file: " + formBin.getAbsolutePath()
                                 + " removed from database");
                     } else {
                         Log.i(t, "Failed to remove cached file: " + formBin.getAbsolutePath()
                                 + " from database (might not have existed...)");
                     }
                     fda.close();
                     return null;
                 } else {
                     // add to file db if it doesn't already exist.
                     // MainMenu will add files that don't exist, but intents can load
                     // FormEntryActivity directly.
                     FileDbAdapter fda = new FileDbAdapter();
                     fda.open();
                     Cursor c = fda.fetchFilesByPath(null, formHash);
                     if (c.getCount() == 0) {
                         fda.createFile(formXml.getAbsolutePath(), FileDbAdapter.TYPE_FORM,
                             FileDbAdapter.STATUS_AVAILABLE);
                     }
                     if (c != null)
                         c.close();
                     fda.close();
                 }
             }
         }
 
         // new evaluation context for function handlers
         EvaluationContext ec = new EvaluationContext();
         fd.setEvaluationContext(ec);
 
         // create FormEntryController from formdef
         FormEntryModel fem = new FormEntryModel(fd);
         fec = new FormEntryController(fem);
 
         try {
             // import existing data into formdef
             if (instancePath != null) {
                 // This order is important. Import data, then initialize.
                 importData(instancePath, fec);
                 fd.initialize(false);
             } else {
                 fd.initialize(true);
             }
         } catch (Exception e) {
             e.printStackTrace();
         	this.publishProgress(Collect.getInstance().getString(R.string.load_error,
             		formXml.getName()) + " : " + e.getMessage());
             return null;
         }
 
         // set paths to FORMS_PATH + formfilename-media/
         // This is a singleton, how do we ensure that we're not doing this
         // multiple times?
         String mediaPath = FileUtils.getFormMediaPath(formXml.getName());
 
         Collect.getInstance().registerMediaPath(mediaPath);
 
         // clean up vars
         fis = null;
         fd = null;
         formBin = null;
         formXml = null;
         formPath = null;
         instancePath = null;
 
         data = new FECWrapper(fec);
         return data;
 
     }
 
 
     public boolean importData(String filePath, FormEntryController fec) {
         // convert files into a byte array
         byte[] fileBytes = FileUtils.getFileAsBytes(new File(filePath));
 
         // get the root of the saved and template instances
         TreeElement savedRoot = XFormParser.restoreDataModel(fileBytes, null).getRoot();
         TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);
 
         // weak check for matching forms
         if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
             Log.e(t, "Saved form instance does not match template form definition");
             return false;
         } else {
             // populate the data model
             TreeReference tr = TreeReference.rootRef();
             tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);
             templateRoot.populate(savedRoot, fec.getModel().getForm());
 
             // populated model to current form
             fec.getModel().getForm().getInstance().setRoot(templateRoot);
 
             // fix any language issues
             // : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
             if (fec.getModel().getLanguages() != null) {
                 fec.getModel()
                         .getForm()
                         .localeChanged(fec.getModel().getLanguage(),
                             fec.getModel().getForm().getLocalizer());
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
         Log.i(t, "Attempting read of " + formDef.getAbsolutePath());
 
         // need a list of classes that formdef uses
         PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
         FileInputStream fis = null;
         FormDef fd = null;
         DataInputStream dis = null;
         try {
             // create new form def
             fd = new FormDef();
             fis = new FileInputStream(formDef);
             dis = new DataInputStream(fis);
 
             // read serialized formdef into new formdef
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
             if (dis != null) {
                 try {
                     dis.close();
                 } catch (IOException e) {
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
     public void serializeFormDef(FormDef fd, String filepath) {
         // if cache folder is missing, create it.
         if (FileUtils.createFolder(FileUtils.CACHE_PATH)) {
 
             // calculate unique md5 identifier
             String hash = FileUtils.getMd5Hash(new File(filepath));
             File formDef = new File(FileUtils.CACHE_PATH + hash + ".formdef");
 
             // formdef does not exist, create one.
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
     }
 
     @Override
 	protected void onProgressUpdate(String... values) {
 		Toast.makeText(Collect.getInstance().getApplicationContext(), 
             values[0], Toast.LENGTH_LONG).show();
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
 
 }
