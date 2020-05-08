 package org.xbrlapi.sax.identifiers;
 
 import org.xbrlapi.Fragment;
 import org.xbrlapi.impl.ArcroleTypeImpl;
 import org.xbrlapi.impl.ContextImpl;
 import org.xbrlapi.impl.EntityImpl;
 import org.xbrlapi.impl.FractionItemImpl;
 import org.xbrlapi.impl.InstanceImpl;
 import org.xbrlapi.impl.LinkbaseImpl;
 import org.xbrlapi.impl.NonNumericItemImpl;
 import org.xbrlapi.impl.PeriodImpl;
 import org.xbrlapi.impl.RoleTypeImpl;
 import org.xbrlapi.impl.ScenarioImpl;
 import org.xbrlapi.impl.SegmentImpl;
 import org.xbrlapi.impl.SimpleNumericItemImpl;
 import org.xbrlapi.impl.TupleImpl;
 import org.xbrlapi.impl.UnitImpl;
 import org.xbrlapi.impl.UsedOnImpl;
 import org.xbrlapi.impl.XlinkDocumentationImpl;
 import org.xbrlapi.sax.ContentHandler;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 import org.xml.sax.Attributes;
 
 /**
  * Identifies fragments in the XBRL 2.1 namespace.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class XBRLIdentifier extends BaseIdentifier implements Identifier {
 
     /**
      * @see org.xbrlapi.sax.identifiers.BaseIdentifier#BaseIdentifier(ContentHandler)
      */
     public XBRLIdentifier(ContentHandler contentHandler) throws XBRLException {
         super(contentHandler);
     }
 
     /**
      * Set to true if parsing an XBRL instance.
      */
     private boolean parsingAnXBRLInstance = false;
     
     /**
      * Set to true if the element can be a tuple.
      */
     private boolean canBeATuple = false;
     
     /**
      * Finds fragments in the XBRL 2.1 namespace and keeps
      * track of boolean state variables tracking whether the
      * parser is inside and XBRL instance and whether the current
      * element can be a tuple in an XBRL instance.
      * 
      * @see org.xbrlapi.sax.identifiers.BaseIdentifier#startElement(String,String,String,Attributes)
      */
     public void startElement(
             String namespaceURI, 
             String lName, 
             String qName,
             Attributes attrs) throws XBRLException {      
         
         Fragment xbrlFragment = null;
         if (namespaceURI.equals(Constants.XBRL21Namespace.toString())) {
             if (lName.equals("xbrl")) {
                 xbrlFragment = new InstanceImpl();
                 this.parsingAnXBRLInstance = true;
                 this.canBeATuple = true;
             } else if (lName.equals("period")) {
                 xbrlFragment = new PeriodImpl();
             } else if (lName.equals("entity")) {
                 xbrlFragment = new EntityImpl();
             } else if (lName.equals("segment")) {
                 xbrlFragment = new SegmentImpl();
             } else if (lName.equals("scenario")) {
                 xbrlFragment = new ScenarioImpl();
             } else if (lName.equals("context")) {
                 xbrlFragment = new ContextImpl();
                 this.canBeATuple = false;
             } else if (lName.equals("unit")) {
                 xbrlFragment = new UnitImpl();
                 this.canBeATuple = false;
             }
         }
 
         if (xbrlFragment != null) {
             this.processFragment(xbrlFragment,attrs);
             return;
         }
 
         Fragment xbrlLinkFragment = null;
         if (namespaceURI.equals(Constants.XBRL21LinkNamespace.toString())) {
             if (lName.equals("roleType")) {
                 xbrlLinkFragment = new RoleTypeImpl();
             } else if (lName.equals("arcroleType")) {
                 xbrlLinkFragment = new ArcroleTypeImpl();
             } else if (lName.equals("usedOn")) {
                 xbrlLinkFragment = new UsedOnImpl();
             } else if (lName.equals("linkbase")) {
                 xbrlLinkFragment = new LinkbaseImpl();
             } else if (lName.equals("documentation")) {
                 xbrlLinkFragment = new XlinkDocumentationImpl();
             }
             
             if (xbrlLinkFragment != null) {
                 this.processFragment(xbrlLinkFragment,attrs);
                 return;
             }
         }
         
         if (parsingAnXBRLInstance) {
             
             Fragment factFragment = null;
             
             // First handle items
             String contextRef = attrs.getValue("contextRef");
             if (contextRef != null) {
                 String unitRef = attrs.getValue("unitRef");
                 if (unitRef != null) {
                     factFragment = new SimpleNumericItemImpl();
                 } else {
                     factFragment = new NonNumericItemImpl();
                 }
             }
            
             // then handle tuples and fraction items
             if ((factFragment == null) && this.canBeATuple) {
                 Fragment currentFragment = this.getLoader().getFragment();
                if (currentFragment.hasMetaAttribute("fact")) {
                     Fragment fractionItem = new FractionItemImpl();
                     fractionItem.setBuilder(currentFragment.getBuilder());
                     fractionItem.setMetaAttribute("type","org.xbrlapi.impl.FractionItemImpl");
                     getLoader().replaceCurrentFragment(fractionItem);
                 } else {
                     factFragment = new TupleImpl();
                 }
             }
             
             if (factFragment != null) {
                 processFragment(factFragment,attrs);
                 factFragment.setMetaAttribute("fact","true");
                 return;
             }
             
         }
                 
     }
 
     /**
      * Set the boolean state variables based upon whether the next 
      * element can be a tuple in an XBRL instance and whether the
      * parser is within an XBRL instance and whether the parser is
      * within a reference resource.
      * 
      * @see Identifier#endElement(String, String, String, Attributes)
      */
     public void endElement(
             String namespaceURI, 
             String lName, 
             String qName,
             Attributes attrs) throws XBRLException {
 
         if (namespaceURI.equals(Constants.XBRL21Namespace.toString())) {
             if (lName.equals("xbrl")) {
                 this.parsingAnXBRLInstance = false;
                 this.canBeATuple = false;
             } else if (lName.equals("context")) {
                 this.canBeATuple = true;
             } else if (lName.equals("unit")) {
                 this.canBeATuple = true;
             }
 
         } else if (namespaceURI.equals(Constants.XBRL21LinkNamespace.toString())) {
             if (lName.equals("footnoteLink")) {
                 this.canBeATuple = true;
             } else if (lName.equals("schemaRef")) {
                 this.canBeATuple = true;
             } else if (lName.equals("linkbaseRef")) {
                 this.canBeATuple = true;
             } else if (lName.equals("arcroleRef")) {
                 this.canBeATuple = true;
             } else if (lName.equals("roleRef")) {
                 this.canBeATuple = true;
             }
         }
         
     }    
     
 }
