 package com.radicaldynamic.groupinform.xform;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.Iterator;
 
 import android.util.Log;
 
 import com.mycila.xmltool.XMLDoc;
 import com.mycila.xmltool.XMLTag;
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.utilities.TranslationSortByDefault;
 
 public final class FormWriter
 {
     private static final String t = "FormWriter: ";
     
     public static final String CONTENT_TYPE = "text/xml";
     
     private static XMLTag mFormTag;
     private static String mDefaultPrefix;
     private static String mInstanceRoot;
     private static String mInstanceRootId;
     
     @SuppressWarnings("serial")
     public static class FormSanityException extends Exception
     {
         FormSanityException(String s)
         {
             super(s);
         }
     }
     
     @SuppressWarnings("serial")
     public static class GroupHasNoChildrenException extends FormSanityException
     {
         GroupHasNoChildrenException(String s)
         {
             super(s);
         }
     }
     
     public static byte[] writeXml(String headTitle, String instanceRoot, String instanceRootId) throws GroupHasNoChildrenException
     {        
         try {        
             // Retrieve and load a template XForm file (this makes it easier than hardcoding a new one from scratch)
             InputStream xis = Collect.getInstance().getResources().openRawResource(R.raw.xform_template);        
             mFormTag = XMLDoc.from(xis, false);
             xis.close();
         } catch (IOException e) {
             // Ignore xis.close() exceptions
             e.printStackTrace();
         }
         
         mDefaultPrefix = mFormTag.getPefix(XForm.Value.XMLNS_XFORMS);
         
         
         mInstanceRoot = instanceRoot;
         mInstanceRootId = instanceRootId;
         
         // Insert the title of this form into the XML
         mFormTag.gotoRoot().gotoTag("h:head/h:title").setText(FieldText.encodeXMLEntities(headTitle));
         
         // Either write out translations or remove the unused itext tag
         if (Collect.getInstance().getFormBuilderState().getTranslations().size() > 0) {
             if (!mFormTag.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix).hasTag("%1$s:itext", mDefaultPrefix)) {
                 mFormTag.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix).addTag(mDefaultPrefix + ":itext");
             }
             
             writeTranslations(null);
         } else {
             if (mFormTag.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix).hasTag("%1$s:itext", mDefaultPrefix)) {
                 mFormTag.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:itext", mDefaultPrefix).delete();
             }
         }
         
         writeInstance(null);        
         writeBinds();        
         writeControls(null);
         
         // Return XML for consumption
         return mFormTag.toBytes();
     }
 
     private static void writeControls(Field incomingField) throws GroupHasNoChildrenException
     {
         Iterator<Field> it;
         
         if (incomingField == null) {
             it = Collect.getInstance().getFormBuilderState().getFields().iterator();
             mFormTag.gotoRoot().gotoTag("h:body");
         } else {
             it = incomingField.getChildren().iterator();
         }
         
         while (it.hasNext()) {
             Field field = it.next();
             
             mFormTag.addTag(field.getType());
 
             // Support for repeat (nodeset) references as well as regular references
             if (field.hasXPath()) {
                 if (field.getType().equals("repeat")) {
                     mFormTag.getCurrentTag().setAttribute(XForm.Attribute.NODESET, field.getXPath());
                 } else {  
                     mFormTag.getCurrentTag().setAttribute(XForm.Attribute.REFERENCE, field.getXPath());
                 }
             }
             
             // Multiple field types
             if (field.getAttributes().containsKey(XForm.Attribute.APPEARANCE))
                 mFormTag.getCurrentTag().setAttribute(XForm.Attribute.APPEARANCE, field.getAttributes().get(XForm.Attribute.APPEARANCE));
 
             // Upload control fields only
             if (field.getAttributes().containsKey(XForm.Attribute.MEDIA_TYPE))
                 mFormTag.getCurrentTag().setAttribute(XForm.Attribute.MEDIA_TYPE, field.getAttributes().get(XForm.Attribute.MEDIA_TYPE));
             
             // If the label does not reference an itext translation then attempt to output a regular label
             if (field.getLabel().getRef() == null) {
                 // We don't gotoParent() after adding a tag with a text value (see longer comment in writeInstance)
                 if (field.getLabel().toString().length() > 0) {
                     /* 
                      * Special support for labels that take advantage of XForm output from instance fields.  E.g.,
                      * <label>review widget. is your email still <output value="/widgets/regex"/>?</label>
                      */ 
                     mFormTag.addTag(XMLDoc.from("<label>" + FieldText.encodeXMLEntities(field.getLabel().toString().replace("xmlns=\"http://www.w3.org/2002/xforms\" ", "")) + "</label>", false));                                       
                 }
             } else {
                 mFormTag.addTag("label").addAttribute(XForm.Attribute.REFERENCE, "jr:itext('" + field.getLabel().getRef() + "')").gotoParent();
             }
             
             // Do the same for hints
             if (field.getHint().getRef() == null) {
                 if (field.getHint().toString().length() > 0) {
                     mFormTag.addTag(XMLDoc.from("<hint>" + FieldText.encodeXMLEntities(field.getHint().toString().replace("xmlns=\"http://www.w3.org/2002/xforms\" ", "")) + "</hint>", false));
                 }                    
             } else {
                 mFormTag.addTag("hint").addAttribute(XForm.Attribute.REFERENCE, "jr:itext('" + field.getHint().getRef() + "')").gotoParent();
             }
             
             // Special support for item control fields
             if (field.getType().equals("item")) {
                 if (field.getItemValue() == null)
                     mFormTag.addTag("value").setText("");
                 else 
                     mFormTag.addTag("value").setText(field.getItemValue());
             }
             
             // Sanity check to make sure groups have children
             if (field.getType().equals("group")) {
                 if (Field.isRepeatedGroup(field) && field.getRepeat().getChildren().size() == 0) {
                     throw new GroupHasNoChildrenException("The repeated group \"" + field.getLabel() + "\" does not contain any fields.\n\nYou must create a field within this group or remove it before saving the form.");        
                 } else if (field.getChildren().size() == 0) {
                     throw new GroupHasNoChildrenException("The group \"" + field.getLabel() + "\" does not contain any fields.\n\nYou must create a field within this group or remove it before saving the form.");
                 }
             }
                 
             writeControls(field);
             
             mFormTag.gotoParent();
         }
     }
 
     private static void writeBinds()
     {
         Iterator<Bind> it = Collect.getInstance().getFormBuilderState().getBinds().iterator();
         
         mFormTag.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix);
         
         while (it.hasNext()) {
             Bind bind = it.next();
             
             if (bind.hasUnhandledAttribute())
                 Log.w(Collect.LOGTAG, t + "bind " + bind.getXPath() + " has unhandled attributes that will not be written; data will be lost!");
             
             mFormTag.addTag("bind").addAttribute("nodeset", bind.getXPath());
             
             // The following are conditional attributes
             if (bind.getType() != null) mFormTag.getCurrentTag().setAttribute("type", bind.getType());
             if (bind.isReadonly()) mFormTag.getCurrentTag().setAttribute("readonly", bind.getReadonly());
             if (bind.isRequired()) mFormTag.getCurrentTag().setAttribute("required", bind.getRequired());
             
             if (bind.getPreload() != null && bind.getPreloadParams() != null) {
                 mFormTag.getCurrentTag().setAttribute("jr:preload", bind.getPreload());
                 mFormTag.getCurrentTag().setAttribute("jr:preloadParams", bind.getPreloadParams());
             }
             
             if (bind.getConstraint()    != null) mFormTag.getCurrentTag().setAttribute("constraint", bind.getConstraint());            
             if (bind.getConstraintMsg() != null) mFormTag.getCurrentTag().setAttribute("jr:constraintMsg", bind.getConstraintMsg());            
             if (bind.getRelevant()      != null) mFormTag.getCurrentTag().setAttribute("relevant", bind.getRelevant());            
             if (bind.getCalculate()     != null) mFormTag.addAttribute("calculate", bind.getCalculate());
             
             // Make sure the next bind is inserted at the same level as this one
             mFormTag.gotoParent();
         }
     }
 
     private static void writeInstance(Instance incomingInstance)
     {
         Iterator<Instance> it;
         
         if (incomingInstance == null) {
             it = Collect.getInstance().getFormBuilderState().getInstance().iterator();
             
             // Initialize the instance root (only done once)
             mFormTag.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix);
             mFormTag.addTag(mInstanceRoot).addAttribute("id", mInstanceRootId);
         } else {
             it = incomingInstance.getChildren().iterator();
         }
             
         while (it.hasNext()) {
             Instance instance = it.next();
            
            // Don't write out nested XML nodes (see Item129: removal of repeated groups leaves broken XForm) 
            if (instance.getChildren().isEmpty() &&
                    !instance.getField().getType().equals("group") &&
                    !instance.getField().getType().equals("repeat")) {

                 /*
                  * For some reason unknown to me we can only call gotoParent() when adding 
                  * an empty tag.  Calling it after adding an empty tag OR a tag with a text
                  * value causes the nesting to get screwed up.  This doesn't make any sense
                  * to me.  It might be a bug with xmltool.
                  */
                 if (instance.getDefaultValue().length() == 0) { 
                     mFormTag.addTag(instance.getName());
                     mFormTag.gotoParent();
                 } else {
                     mFormTag.addTag(instance.getName()).setText(instance.getDefaultValue());
                 }
             } else {
                 // Likely a repeated data set
                 mFormTag.addTag(instance.getName()).addAttribute(XForm.Attribute.JR_TEMPLATE, "");
                 writeInstance(instance);
                 mFormTag.gotoParent();
             }
         }
     }
 
     private static void writeTranslations(Translation i18n)
     {
         Iterator<Translation> it;
         
         if (i18n == null) {            
             Collections.sort(Collect.getInstance().getFormBuilderState().getTranslations(), new TranslationSortByDefault());
             it = Collect.getInstance().getFormBuilderState().getTranslations().iterator();   
         } else {
             it = i18n.getTexts().iterator();
         }
         
         while (it.hasNext()) {
             Translation t = it.next();
             
             if (t.isGroup()) {
                 // Only write out sets that have translations
                 if (!t.getTexts().isEmpty()) {
                     mFormTag.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:itext", mDefaultPrefix);
                     mFormTag.addTag("translation").addAttribute(XForm.Attribute.LANGUAGE, t.getLang());
                     writeTranslations(t);
                 }
             } else {
                 // Only write out translations that have content
                 if (t.getValue() instanceof String && t.getValue().length() > 0) {
                     mFormTag
                         .addTag("text").addAttribute(XForm.Attribute.ID, t.getId())
                         .addTag("value").setText(t.getValue())
                         .gotoParent();
                 }
             }
         }
     }
 }
