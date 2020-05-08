 package org.wyona.yanel.impl.jelly.tags;
 
 import org.apache.commons.jelly.JellyTagException;
 import org.apache.commons.jelly.MissingAttributeException;
 import org.apache.commons.jelly.XMLOutput;
 import org.wyona.yanel.impl.jelly.InputItemWithManySelectableOptions;
 import org.wyona.yanel.impl.jelly.Option;
 import org.xml.sax.helpers.AttributesImpl;
 
 import org.apache.log4j.Logger;
 
 public class CheckboxTag extends YanelTag {
     private static Logger log = Logger.getLogger(CheckboxTag.class);
     private InputItemWithManySelectableOptions item = null;
     
     public void setItem(InputItemWithManySelectableOptions resourceInputItem) {
         this.item = resourceInputItem;
     }
 
     public void doTag(XMLOutput out) throws MissingAttributeException, JellyTagException {
         try {
             
             //TODO: where to put the id. Is it used? in a <div> which is around the inputs?
             
             Option[] selection = item.getOptions();
             
             for (int i = 0; i < selection.length; i++) {
                    
                 AttributesImpl attributes = new AttributesImpl();
                 
                 attributes.addAttribute(XHTML_NAMESPACE, "", "type", "CDATA", "checkbox");
                 attributes.addAttribute(XHTML_NAMESPACE, "", "name", "CDATA", item.getName());
                 attributes.addAttribute(XHTML_NAMESPACE, "", "value", "CDATA", selection[i].getValue());
 
                if (isSelected(selection[i]) // checked upon creation
                || selection[i].getValue().equals("true") // checked during filling out the form
                 ) {
                     attributes.addAttribute(XHTML_NAMESPACE, "", "checked", "CDATA", "checked");
                 }
 
                 out.startElement("input", attributes);
                 out.writeCDATA(selection[i].getLabel());
                 out.endElement("input");
                 out.startElement("br");
                 out.endElement("br");
             }
             
             // Add a single hidden field to make sure that the field is always
             // submitted, even when no option is checked.
             AttributesImpl hiddenAttributes = new AttributesImpl();
             hiddenAttributes.addAttribute(XHTML_NAMESPACE, "", "type", "CDATA", "hidden");
             hiddenAttributes.addAttribute(XHTML_NAMESPACE, "", "id", "CDATA", getId());
             hiddenAttributes.addAttribute(XHTML_NAMESPACE, "", "name", "CDATA", item.getName());
             hiddenAttributes.addAttribute(XHTML_NAMESPACE, "", "value", "CDATA", "");
             out.startElement("input", hiddenAttributes);
             out.endElement("input");
             
             addValidationMessage(item, out);
             
        } catch (Exception e) {
             log.error("Tag could not be rendered.", e);
         }
     }
     
     private boolean isSelected(Option vt) {
         Option[] selected = item.getValue();
         
         for (int i = 0; i < selected.length; i++) {
             if (selected[i].equals(vt)) return true;
         }
         return false;
     }
 
 }
