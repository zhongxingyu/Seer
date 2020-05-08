 package com.radicaldynamic.groupinform.tasks;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.ektorp.Attachment;
 
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.documents.FormDefinition;
 import com.radicaldynamic.groupinform.listeners.DefinitionImportListener;
 import com.radicaldynamic.groupinform.utilities.Base64Coder;
 import com.radicaldynamic.groupinform.xform.FormReader;
 import com.radicaldynamic.groupinform.xform.FormWriter;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 
 public class DefinitionImportTask extends AsyncTask<String, Void, Void>
 {
     private static final String t = "DefinitionImportTask: ";
     
     private DefinitionImportListener mStateListener;
     
     private FormDefinition mFormDefinition = new FormDefinition();
     
     private String mImportMessage;
     private boolean mImportSuccessful = false;    
     
     @Override
     protected Void doInBackground(String... arg0) 
     {       
         try {
             File templateFile = new File(arg0[0]);
             
             // Read form to ensure that it is an XML file
             FileInputStream fis = new FileInputStream(templateFile);
             FormReader fr = new FormReader(fis);
             fis.close();                
             
             // Get title from form, if possible
             String title = fr.getTitle();
             
             if (title == null || title.trim().length() == 0) {
                 // Otherwise, use name of file
                 title = templateFile.getName().substring(0, templateFile.getName().lastIndexOf('.'));
             }
             
             // Read form to attach to document                
             fis = new FileInputStream(templateFile);
             
             // Set up variables to receive data
             ByteArrayOutputStream data = new ByteArrayOutputStream();
             byte[] inputbuf = new byte[8192];            
             int inputlen;
 
             while ((inputlen = fis.read(inputbuf)) > 0) {
                 data.write(inputbuf, 0, inputlen);
             }
             
             fis.close();
             
             mFormDefinition.setName(title);
             mFormDefinition.addInlineAttachment(new Attachment("xml", new String(Base64Coder.encode(data.toByteArray())).toString(), FormWriter.CONTENT_TYPE));
             
             Collect.getInstance().getDbService().getDb().create(mFormDefinition);               
 
             mImportSuccessful = true;
         } catch (FileNotFoundException e) {
             mImportMessage = "The file " + arg0[0] + " could not be found on the external storage.";
             if (Collect.Log.WARN) Log.w(Collect.LOGTAG, t + e.toString());
             e.printStackTrace();
         } catch (IOException e) {
             mImportMessage = e.toString();
             if (Collect.Log.ERROR) Log.e(Collect.LOGTAG, t + e.toString());
             e.printStackTrace();
         } catch (Exception e) {
             mImportMessage = e.toString();
             if (Collect.Log.ERROR) Log.e(Collect.LOGTAG, t + e.toString());
             e.printStackTrace();
         }
         
         return null;
     }
 
     @Override
     protected void onPostExecute(Void nothing)
     {   
         synchronized (this) {
             if (mStateListener != null) {
                 Bundle b = new Bundle();
                 
                 b.putString(DefinitionImportListener.FILENAME, mFormDefinition.getName());
                 b.putString(DefinitionImportListener.MESSAGE, mImportMessage);
                 
                 if (mImportSuccessful) {
                     b.putBoolean(DefinitionImportListener.SUCCESSFUL, true);
                 } else {
                     b.putBoolean(DefinitionImportListener.SUCCESSFUL, false);                        
                 }
                 
                 mStateListener.importTaskFinished(b);
             }
         }
     }
     
     public void setListener(DefinitionImportListener sl) 
     {
         synchronized (this) {
             mStateListener = sl;
         }
     }
 }
