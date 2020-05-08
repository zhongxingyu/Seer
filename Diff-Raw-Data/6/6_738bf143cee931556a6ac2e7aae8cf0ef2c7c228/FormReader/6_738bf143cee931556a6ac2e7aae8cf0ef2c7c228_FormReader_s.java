 package com.radicaldynamic.groupinform.xform;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.UUID;
 
 import android.util.Log;
 
 import com.mycila.xmltool.CallBack;
 import com.mycila.xmltool.XMLDoc;
 import com.mycila.xmltool.XMLDocumentException;
 import com.mycila.xmltool.XMLTag;
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.utilities.StringUtils;
 
 public class FormReader
 {
     private static final String t = "FormReader: ";   
     
     private XMLTag mForm;                           // The "form" as it was loaded by xmltool
     private String mTitle;                          // The title of the template
     private String mInstanceRoot;                   // The name of the instance root element 
     private String mInstanceRootId;                 // The name of the instance root ID attribute
     private String mDefaultPrefix;                  // The name of the default XForm prefix (needed for navigation)
     
     private ArrayList<String> mFieldList = new ArrayList<String>();
     
     // State of binds, fields, instances and translations    
     private ArrayList<Bind> mBinds = new ArrayList<Bind>();
     private ArrayList<Field> mFields = new ArrayList<Field>();
     private ArrayList<Instance> mInstance = new ArrayList<Instance>();
     private ArrayList<Translation> mTranslations = new ArrayList<Translation>();
 
     // Indexed by XMLTool tag location (e.g., *[2]/*[2]/*[3])
     private HashMap<String, Field> mFlatFieldIndex = new HashMap<String, Field>();
 
     {
         // List of valid fields that we can handle
         Collections.addAll(mFieldList, "group", "input", "item", "repeat", "select", "select1", "trigger", "upload");
     }
 
     /*
      * Used to read in a form definition for manipulation by the Form Builder.
      */
     public FormReader(InputStream is) throws Exception
     {
         mForm = XMLDoc.from(is, false);
         
         /*
          * Ensure that namespaces are as expected by loading a default document with a good configuration,
          * removing the body of the template and pushing the contents of the recently loaded template into it.
          * 
          * This works around cases where people load documents from things like KoBo Form Designer that
          * doesn't include the xmlns attribute as expected.
          */
         if (mForm.getPefix(XForm.Value.XMLNS_XFORMS) == null || mForm.getPefix(XForm.Value.XMLNS_XFORMS).length() == 0) {
             try {        
                 InputStream xis = Collect.getInstance().getResources().openRawResource(R.raw.xform_template);        
                 XMLTag tag = XMLDoc.from(xis, false);
                 xis.close();
 
                 tag.gotoRoot().deleteChilds();
 
                 for (XMLTag child : mForm.gotoRoot().getInnerDocument().getChilds()) {
                     tag.gotoRoot().addTag(child);
                 }
 
                 // Reinitialize our XML object now that the <h:head...> element contains the expected namespaces
                 mForm = XMLDoc.from(tag.toString(), false);
             } catch (IOException e) {
                 e.printStackTrace();
                 throw e;
             }
         }
        
         mDefaultPrefix = mForm.getPefix(XForm.Value.XMLNS_XFORMS);
         
         /*
          * Initalize new forms
          * 
          * This hack is in place in case a new form has been created but fails the first save attempt,
          * thereby creating a form that will not contain an instance root.  Since instance roots are
          * expected, the lack of one will crash this application.
          * 
          * FIXME: eventually remove this hack
          */
         if (mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix).getChildCount() == 0) {
             // This might now be rigorous enough for i18n input
             String formName = Collect.getInstance().getFormBuilderState().getFormDefinition().getName(); 
             String instanceRoot = formName.replaceAll("\\s", "").replaceAll("[^a-zA-Z0-9_]", "");
             String instanceRootId = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
             
             // Just in case the form name did not have anything useful in it with which to generate a sane instance root
             if (instanceRoot.length() == 0) {
                 Log.w(Collect.LOGTAG, t + "unable to construct instance root from form getName() of " + formName);
                 instanceRoot = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
             }
             
             // See "Form ID Guidelines" (id is preferred vs. xmlns) http://code.google.com/p/opendatakit/wiki/XFormDesignGuidelines
             mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix);
             mForm.addTag(XMLDoc.from("<" + instanceRoot + " id=\"" + instanceRootId + "\"></" + instanceRoot + ">", false));
         } else {
             setTitle(mForm.gotoRoot().gotoTag("h:head/h:title").getInnerText());
         }
         
         mInstanceRoot = mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix).gotoChild().getCurrentTagName();
         
         try {
             mInstanceRootId = mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix).gotoChild().getAttribute(XForm.Attribute.ID);
         } catch (XMLDocumentException e) {
             Log.w(Collect.LOGTAG, t + e.toString());            
 
             try {
                 // It's possible that the ID attribute doesn't exist -- if this is the case, try and use the old-style XMLNS attribute
                 mInstanceRootId = mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix).gotoChild().getAttribute(XForm.Attribute.XML_NAMESPACE);
             } catch (XMLDocumentException e1) {
                 Log.e(Collect.LOGTAG, t + e1.toString());
                 throw new Exception("Unable to find id or xmlns attribute for instance.\n\nPlease contact our support team with this message at support@groupcomplete.com");
             }
         }        
         
         Log.v(Collect.LOGTAG, t + "default prefix for form: " + mDefaultPrefix);
         Log.v(Collect.LOGTAG, t + "instance root element name: " + mInstanceRoot);
         Log.v(Collect.LOGTAG, t + "instance root ID: " + mInstanceRootId);
         
         parseForm();
 
         // Free immediately
         mFlatFieldIndex.clear();
     }
     
     public ArrayList<Bind> getBinds()
     {
         return mBinds;
     }
     
     public ArrayList<Field> getFields()
     {
         return mFields;
     }
     
     public ArrayList<Instance> getInstance()
     {
         return mInstance;
     }
     
     public String getInstanceRoot()
     {
         return mInstanceRoot;
     }
     
     public String getInstanceRootId()
     {        
         if (mInstanceRootId == null || mInstanceRootId.length() == 0) {
             Log.w(Collect.LOGTAG, t + "missing instance root ID attribute, generating random string");
             mInstanceRootId = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
         }
         
         return mInstanceRootId;
     }
 
     public ArrayList<Translation> getTranslations()
     {
         return mTranslations;
     }
     
     /*
      * Trigger method for doing all of the actual work
      */
     private void parseForm() throws Exception
     {
         if (mForm.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix).hasTag("%1$s:itext", mDefaultPrefix)) {
             Log.d(Collect.LOGTAG, t + "parsing itext form translations...");
             parseFormTranslations(mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:itext", mDefaultPrefix));
         } else {
             Log.d(Collect.LOGTAG, t + "no form translations to parse");
         }
 
         Log.d(Collect.LOGTAG, t + "parsing form binds...");
         parseFormBinds(mForm.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix));
 
         Log.d(Collect.LOGTAG, t + "parsing form body...");
         parseFormBody(mForm.gotoRoot().gotoTag("h:body"));
         
         Log.d(Collect.LOGTAG, t + "parsing form instance...");
         parseFormInstance(mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix).gotoChild(), "/" + mInstanceRoot);
     }
     
     /*
      * Recursively iterate over the form fields, creating objects to represent these fields
      */
     private void parseFormBody(XMLTag tag) throws Exception
     {       
         String ctl = tag.getCurrentTagLocation();
 
         Log.v(Collect.LOGTAG, t + "visiting <" + tag.getCurrentTagName() + "> at " + ctl);
         
         // Is the tag name a field type that we understand?
         if (mFieldList.contains(tag.getCurrentTagName())) {
             Field f = null;
 
             if (ctl.split("/").length == 2) {
                 // Top level field based on a current tag location of say *[2]/*[1]
                 f = new Field(tag, mBinds, mInstanceRoot, null);
                 mFields.add(f);
             } else {
                 // Should belong to an existing parent field that has already been parsed
 
                 /*
                  * Attempt to look up parent field from index.  E.g., if current field is *[2]/*[1]/*[1] 
                  * then we're looking for a field with the location *[2]/*[1].
                  */
                 String ptl = StringUtils.join(ctl.split("/"), "/", ctl.split("/").length - 1);
                 Field p = mFlatFieldIndex.get(ptl);
 
                 if (p == null) {
                     Log.e(Collect.LOGTAG, t + "could not find parent!");
                     throw new Exception("Could not find parent tag at " + ptl + ".\n\nPlease contact our support team with this message at support@groupcomplete.com");
                 } else {
                     f = new Field(tag, mBinds, mInstanceRoot, p);
                     p.getChildren().add(f);
                 }
             }
 
             mFlatFieldIndex.put(ctl, f);
         } else if (mFields.size() > 0) {
             String ptl = StringUtils.join(ctl.split("/"), "/", ctl.split("/").length - 1);
             Field p = mFlatFieldIndex.get(ptl);
 
             if (p == null) {
                 Log.e(Collect.LOGTAG, t + "could not find parent!");
                 throw new Exception("Could not find parent tag at " + ptl + ".\n\nPlease contact our support team with this message at support@groupcomplete.com");
             }
 
             if (tag.getCurrentTagName().equals("label")) {
                 // Handle translated/untranslated labels
                 if (tag.hasAttribute(XForm.Attribute.REFERENCE))
                     p.setLabel(tag.getAttribute(XForm.Attribute.REFERENCE));
                 else
                     p.setLabel(tag.getInnerText());
             } else if (tag.getCurrentTagName().equals("hint")) {
                 // Handle translated/untranslated hints
                 if (tag.hasAttribute(XForm.Attribute.REFERENCE))
                     p.setHint(tag.getAttribute(XForm.Attribute.REFERENCE));
                 else
                     p.setHint(tag.getInnerText());
             } else if (tag.getCurrentTagName().equals("value")) {
                 // Handle select item values
                 p.setItemValue(tag.getInnerText());
             }
         }

        // If field element has children then list fields recursively from the standpoint of every child
        if (tag.getChildCount() > 0) {           
             tag.forEachChild(new CallBack() {
                 @Override
                 public void execute(XMLTag arg0)
                 {
                     try {
                         parseFormBody(arg0);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             });
         }
     }
 
     /*
      * Recursively parse the form translations, adding objects to mTranslationState to represent them
      */
     private void parseFormTranslations(XMLTag tag)
     {
         if (tag.getCurrentTagName().equals("translation")) {
             Log.v(Collect.LOGTAG, t + "adding translations for " + tag.getAttribute(XForm.Attribute.LANGUAGE));
             Translation t = new Translation(tag.getAttribute(XForm.Attribute.LANGUAGE));
             
             // The first translation to be parsed is considered the default/fallback translation for the form
             if (mTranslations.isEmpty())
                 t.setFallback(true);
                 
             mTranslations.add(t);
         } else if (tag.getCurrentTagName().equals("text")) {
             Log.v(Collect.LOGTAG, t + "adding translation ID " + tag.getAttribute(XForm.Attribute.ID));
             mTranslations.get(mTranslations.size() - 1).getTexts().add(new Translation(tag.getAttribute(XForm.Attribute.ID), null));
         } else if (tag.getCurrentTagName().equals("value")) {
             Log.v(Collect.LOGTAG, t + "adding translation: " + tag.getInnerText());
             mTranslations
                 .get(mTranslations.size() - 1).getTexts()
                 .get(mTranslations.get(mTranslations.size() - 1).getTexts().size() - 1)
                 .setValue(tag.getInnerText());
         }
  
         if (tag.getChildCount() > 0) {           
             tag.forEachChild(new CallBack() {
                 @Override
                 public void execute(XMLTag arg0)
                 {
                     parseFormTranslations(arg0);
                 }
             });
         }
     }
     
     /*
      * Go through the list of binds and build objects to represent them.
      * Binds are associated with field objects when the field is instantiated.
      */
     private void parseFormBinds(XMLTag tag)
     {
         tag.forEachChild(new CallBack() {
             @Override
             public void execute(XMLTag arg0)
             {
                 if (arg0.getCurrentTagName().equals("bind"))
                     mBinds.add(new Bind(arg0, mInstanceRoot));               
             }
         });
     }
     
     /*
      * Recursively parse and use the information supplied in the form instance
      * to supplement the field objects in mFields 
      */
     private void parseFormInstance(XMLTag tag, final String instancePath)
     {
         /*
          * If the instancePath does not currently point to the instance root
          * (this only happens the first time this method runs) 
          */
         if (instancePath.equals("/" + mInstanceRoot) == false) {
             Instance newInstance = new Instance(instancePath, tag.getInnerText(), tag.getCurrentTagLocation(), mBinds);
             
             // Attempt to apply this instance to a pre-existing field -- if this fails then the instance is "hidden"
             newInstance.setHidden(!applyInstanceToField(null, newInstance));
             
             if (tag.getCurrentTagLocation().split("/").length == 5) {
                 // Add a top level instance
                 mInstance.add(newInstance);
             } else {
                 attachChildToParentInstance(newInstance, null);
             }
         }
         
         tag.forEachChild(new CallBack() {
             @Override
             public void execute(XMLTag arg0)
             {
                 parseFormInstance(arg0, instancePath + "/" + arg0.getCurrentTagName());
             }
         });
     }
     
     /*
      * Attempts to apply an instance to an existing field
      * 
      * Returns true to indicate that application was successful and false
      * to indicate that it was not (e.g., the instance is hidden)
      */
     private boolean applyInstanceToField(Field field, final Instance instance)
     {
         Iterator<Field> it;
         
         if (field == null) {
             it = mFields.iterator();
         } else { 
             if (field.hasXPath() && field.getXPath().equals(instance.getXPath())) {
                 Log.v(Collect.LOGTAG, t + "instance matched with field object via " + instance.getXPath());
                 field.setInstance(instance);
                 field.getInstance().setField(field);
                 return true;
             }
             
             it = field.getChildren().iterator();
         }
         
         while (it.hasNext()) {
             Field c = it.next();
             
             if (applyInstanceToField(c, instance)) {
                 return true;
             }
         }
         
         return false;
     }
     
     /*
      * For instances that are nested within other instances
      * (e.g., those that are probably nested in a repeated group somewhere)
      */
     private boolean attachChildToParentInstance(Instance child, Instance incomingParent)
     {
         Iterator<Instance> it = null;
         
         if (incomingParent == null)
             it = mInstance.iterator();
         else
             it = incomingParent.getChildren().iterator();
         
         while (it.hasNext()) {
             Instance parent = it.next();
             
             if (child.getLocation().split("/").length - parent.getLocation().split("/").length == 1 &&
                     parent.getLocation().equals(child.getLocation().substring(0, parent.getLocation().length()))) {
                 child.setParent(parent);
                 parent.getChildren().add(child);
                 return true;
             }
                 
             
             if (!parent.getChildren().isEmpty())
                 attachChildToParentInstance(child, parent);
         }      
         
         return false;
     }
 
     public void setTitle(String mTitle) {
         this.mTitle = mTitle;
     }
 
     public String getTitle() {
         return mTitle;
     }
 }
