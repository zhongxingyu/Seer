 /*
  * Copyright 2000-2005 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.parser;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.*;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.event.NewConceptEvent;
 
 
 import org.jdom.*;
 import org.jdom.input.SAXBuilder;
 import org.jaxen.JaxenException;
 import org.jaxen.jdom.JDOMXPath;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * A writer for XMI files 
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class XMIWriter implements ElementWriter {
 
   private String output = null;
 
   private UserSelections userSelections = UserSelections.getInstance();
   private String input = (String)userSelections.getProperty("FILENAME");
 
   private Element modelElement;
 
   private HashMap<String, Element> elements = new HashMap();
 
   private ElementsLists cadsrObjects = null;
 
   private ReviewTracker reviewTracker = ReviewTracker.getInstance();
   private ChangeTracker changeTracker = ChangeTracker.getInstance();
 
   
   public XMIWriter() {
     try {
       SAXBuilder builder = new SAXBuilder();
       Document doc = builder.build(input);
       modelElement = doc.getRootElement();
     } catch (Exception ex) {
       throw new RuntimeException("Error initializing model", ex);
     }
   }
 
   public void write(ElementsLists elements) throws ParserException {
 
     this.cadsrObjects = elements;
     
     readModel();
     markHumanReviewed();
     updateChangedElements();
     writeModel();
 
   }
 
   public void setOutput(String url) {
     this.output = url;
   }
 
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private void addTaggedValue
     (String tagName,
      String value,
      String newId,
      String xmiid,
      Namespace namespace) throws JaxenException {
 
       Element taggedValue = new Element("TaggedValue", namespace);
       taggedValue.setNamespace(Namespace.getNamespace("UML", "href://org.omg/UML"));
       taggedValue.setAttribute(new Attribute("xmi.id", newId));
       taggedValue.setAttribute(new Attribute("tag", tagName));
       taggedValue.setAttribute(new Attribute("modelElement", xmiid));
       taggedValue.setAttribute(new Attribute("value", value));
       
       Element elem1 = getElement(modelElement, "//*[local-name()='Model'");
       Element parentElement = elem1.getParentElement();
       parentElement.addContent(taggedValue);
 
   }
 
   /**
    *
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private Element getElement
     (Element classElement, String exp)
     throws JaxenException {
 
     Element element = null;
     try {
       element = (Element) (new JDOMXPath(exp)).selectSingleNode(classElement);
       
     } catch (Exception ex) {
       throw new RuntimeException("Error searching for expression " + exp
                                  + " in class " + classElement.getAttributeValue("name"), ex);
     }
     return element;
   }
 
 
   private List<Element> getElements
     (Element classElement, String elementName)
     throws JaxenException {
 
     List<Element> elementList = null;
     try {
       String exp = ".//*[local-name()='"+elementName+"']";
       elementList = (List<Element>) (new JDOMXPath(exp)).selectNodes(classElement);
       
     } catch (Exception ex) {
       throw new RuntimeException("Error searching for elements " + elementName
                                  + " for class " + classElement.getAttributeValue("name"), ex);
     }
     return elementList;
   }
 
 
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private void writeModel() {
     try {
       File f = new File(output);
       
       Writer writer = new OutputStreamWriter
         (new FileOutputStream(f), "UTF-8");
       XMLOutputter xmlout = new XMLOutputter();
       xmlout.setFormat(Format.getPrettyFormat());
       writer.write(xmlout.outputString(modelElement));
       writer.flush();
       writer.close();
     } catch (Exception ex) {
       throw new RuntimeException("Error writing to " + output, ex);
     }
   }
   
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private String getNewId(String xmiid) throws JaxenException
   {
     String id = null;
     try
       {
         String exp = "//*[local-name()='TaggedValue' and @modelElement='"+ xmiid +"']";
         List tvs = (List)(new JDOMXPath(exp)).selectNodes(modelElement);
 
         if(tvs.size() > 0) {
           Element tv = (Element)tvs.get(tvs.size()-1);
           
           if(tv != null)
             id = (String)tv.getAttributeValue("xmi.id")+"_tag";
           else
             id = xmiid+"_tag";
         } else
           id = xmiid+"_tag";
       }
     catch(Exception e)
       {
         e.printStackTrace();
         throw new RuntimeException("Exception while creating getNewId"+e.getMessage());
       }
     
     return id;
   }
   
  
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private void readModel() throws ParserException {
     try {
       String xpath = "//*[local-name()='Model']/*[local-name()='Namespace.ownedElement']/*[local-name()='Package']";
 
       doPackage(xpath, "");
     } catch (JaxenException e){
       throw new ParserException(e);
     } // end of try-catch
   }
 
   private void updateChangedElements() throws ParserException {
     try {
       List<ObjectClass> ocs = cadsrObjects.getElements(DomainObjectFactory.newObjectClass());
       List<DataElement> des = cadsrObjects.getElements(DomainObjectFactory.newDataElement());
       
       for(ObjectClass oc : ocs) {
        String fullClassName = null;
        for(AlternateName an : oc.getAlternateNames()) {
          if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME))
            fullClassName = an.getName();
        }

        Element classElement = elements.get(fullClassName);
        boolean changed = changeTracker.get(fullClassName);

         if(changed) {
           String xpath = "//*[local-name()='TaggedValue' and (starts-with(@tag,'ObjectClass') or starts-with(@tag,'ObjectQualifier') )and @modelElement='"
             + classElement.getAttributeValue("xmi.id")
             + "']";
           
           JDOMXPath path = new JDOMXPath(xpath);
           List<Element> conceptTvs = path.selectNodes(modelElement);
           // drop all current concept tagged values
           for(Element tvElt : conceptTvs) {
             tvElt.getParentElement().removeContent(tvElt);
           }
           
           
           String [] conceptCodes = oc.getPreferredName().split(":");
           
           addConceptTvs(classElement, conceptCodes, XMIParser.TV_TYPE_CLASS);
         }
         
       }
       
       for(DataElement de : des) {
         DataElementConcept dec = de.getDataElementConcept();
        String fullPropName = null;

        for(AlternateName an : de.getAlternateNames()) {
          if(an.getType().equals(AlternateName.TYPE_FULL_NAME))
            fullPropName = an.getName();
        }

         Element attributeElement = elements.get(fullPropName);
         
         boolean changed = changeTracker.get(fullPropName);
         if(changed) {
           String xpath = "//*[local-name()='TaggedValue' and (starts-with(@tag,'Property') or starts-with(@tag,'PropertyQualifier') or @tag='" + XMIParser.TV_DE_ID +"' or @tag='"+ XMIParser.TV_DE_VERSION +"' )and @modelElement='"
             + attributeElement.getAttributeValue("xmi.id")
             + "']";
           
           JDOMXPath path = new JDOMXPath(xpath);
           List<Element> conceptTvs = path.selectNodes(modelElement);
           // drop all current concept tagged values
           for(Element tvElt : conceptTvs) {
             tvElt.getParentElement().removeContent(tvElt);
           }
           
 
           // Map to Existing DE
           if(!StringUtil.isEmpty(de.getPublicId()) && de.getVersion() != null) {
             addTaggedValue
               (XMIParser.TV_DE_ID,
                de.getPublicId(),
                getNewId(attributeElement.getAttributeValue("xmi.id")),
                attributeElement.getAttributeValue("xmi.id"),
                attributeElement.getNamespace());
 
             addTaggedValue
               (XMIParser.TV_DE_VERSION,
                de.getVersion().toString(),
                getNewId(attributeElement.getAttributeValue("xmi.id")),
                attributeElement.getAttributeValue("xmi.id"),
                attributeElement.getNamespace());
 
           } else {
             String [] conceptCodes = dec.getProperty().getPreferredName().split(":");
             addConceptTvs(attributeElement, conceptCodes, XMIParser.TV_TYPE_PROPERTY);
           }
 
         }
       }
       changeTracker.clear();
     } catch (JaxenException e){
       throw new ParserException(e);
     } 
     
   }
 
   private void addConceptTvs(Element elt, String[] conceptCodes, String type) {
     if(conceptCodes.length == 0)
       return;
 
     addConceptTv(elt, conceptCodes[conceptCodes.length - 1], type, "", 0);
 
     for(int i= 1; i < conceptCodes.length; i++) {
       
       addConceptTv(elt, conceptCodes[conceptCodes.length - i - 1], type, XMIParser.TV_QUALIFIER, i);
 
     }
 
   }
 
   private void addConceptTv(Element elt, String conceptCode, String type, String pre, int n) {
 
     Concept con = LookupUtil.lookupConcept(conceptCode);
     if(con == null)
       return;
 
     String tvName = type + pre + XMIParser.TV_CONCEPT_CODE + ((n>0)?""+n:"");
 
     try {
       addTaggedValue
         (tvName,
          con.getPreferredName(),
          getNewId(elt.getAttributeValue("xmi.id")),
          elt.getAttributeValue("xmi.id"),
          elt.getNamespace());
       
       tvName = type + pre + XMIParser.TV_CONCEPT_DEFINITION + ((n>0)?""+n:"");
       addTaggedValue
         (tvName,
          con.getPreferredDefinition(),
          getNewId(elt.getAttributeValue("xmi.id")),
          elt.getAttributeValue("xmi.id"),
          elt.getNamespace());
 
 
       tvName = type + pre + XMIParser.TV_CONCEPT_DEFINITION_SOURCE + ((n>0)?""+n:"");
       addTaggedValue
         (tvName,
          con.getDefinitionSource(),
          getNewId(elt.getAttributeValue("xmi.id")),
          elt.getAttributeValue("xmi.id"),
          elt.getNamespace());
       
       tvName = type + pre + XMIParser.TV_CONCEPT_PREFERRED_NAME + ((n>0)?""+n:"");
       addTaggedValue
         (tvName,
          con.getLongName(),
          getNewId(elt.getAttributeValue("xmi.id")),
          elt.getAttributeValue("xmi.id"),
          elt.getNamespace());
     } catch (JaxenException e){
     } // end of try-catch
 
   }
 
 
   private void markHumanReviewed() throws ParserException {
     try{ 
       List<ObjectClass> ocs = (List<ObjectClass>)cadsrObjects.getElements(DomainObjectFactory.newObjectClass().getClass());
       List<DataElementConcept> decs = (List<DataElementConcept>) cadsrObjects.getElements(DomainObjectFactory.newDataElementConcept().getClass());
 
       for(ObjectClass oc : ocs) {
        String fullClassName = null;
        for(AlternateName an : oc.getAlternateNames()) {
          if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME))
            fullClassName = an.getName();
        }

        Element classElement = elements.get(fullClassName);
         String xpath = "//*[local-name()='TaggedValue' and @tag='HUMAN_REVIEWED' and @modelElement='"
           + classElement.getAttributeValue("xmi.id")
           + "']";
 
         JDOMXPath path = new JDOMXPath(xpath);
         Element tv = (Element)path.selectSingleNode(modelElement);
        boolean reviewed = reviewTracker.get(fullClassName);
         if(tv == null) {
           addTaggedValue("HUMAN_REVIEWED",
                          reviewed?"1":"0",
                          getNewId(classElement.getAttributeValue("xmi.id")),
                          classElement.getAttributeValue("xmi.id"),
                          classElement.getNamespace());
         } else {
           tv.setAttribute("value", reviewed?"1":"0");
         }
 
     }
     for(DataElementConcept dec : decs) {
       String fullPropName = dec.getObjectClass().getLongName() + "." + dec.getProperty().getLongName();
 
       Boolean reviewed = reviewTracker.get(fullPropName);
       if(reviewed == null) {
         continue;
       }
 
       Element attributeElement = elements.get(fullPropName);
       String xpath = "//*[local-name()='TaggedValue' and @tag='HUMAN_REVIEWED' and @modelElement='"
         + attributeElement.getAttributeValue("xmi.id")
         + "']";
 
       JDOMXPath path = new JDOMXPath(xpath);
       Element tv = (Element)path.selectSingleNode(modelElement);
       if(tv == null) {
         addTaggedValue("HUMAN_REVIEWED",
                        reviewed?"1":"0",
                        getNewId(attributeElement.getAttributeValue("xmi.id")),
                        attributeElement.getAttributeValue("xmi.id"),
                        attributeElement.getNamespace());
       } else {
         tv.setAttribute("value", reviewed?"1":"0");
       }
     }
     } catch (JaxenException e){
       throw new ParserException(e);
     } catch (RuntimeException e) {
       throw new ParserException(e);
     }
   }
   
   
   private void doPackage(String xpath, String packageName) throws JaxenException {
     if(packageName.length() > 0)
       xpath = xpath + "/*[local-name()='Namespace.ownedElement']/*[local-name()='Package']";
     
     JDOMXPath path = new JDOMXPath(xpath);
     Collection<Element> packages = (Collection<Element>)path.selectNodes(modelElement);
     
     if(packages.size() == 0)
       return;
 
     for(Element pkg : packages) {
       doPackage(pkg, packageName);
     }
 
   }
   
 
   private void doPackage(Element pkg, String packageName) throws JaxenException {
  
     String packName = pkg.getAttributeValue("name");
     
     if(packName.indexOf(" ") != -1) {
       Element namespaceElt = pkg.getChild("Namespace.ownedElement", pkg.getNamespace());
       Collection<Element> packages = namespaceElt.getChildren("Package", pkg.getNamespace());
       for(Element subPkg : packages) {
         doPackage(subPkg, packageName);
       } 
       return;
     }
     
     if (packageName.length() == 0) {
       packageName = packName;
     }
     else {
       packageName += ("." + packName);
     }
     
 
     Element namespaceElt = pkg.getChild("Namespace.ownedElement", pkg.getNamespace());
     Collection<Element> classes = namespaceElt.getChildren("Class", pkg.getNamespace());
     
     
     for (Element classElement : classes) {
       String className = packageName + "." + classElement.getAttributeValue("name");
       
       elements.put(className, classElement);
       
       List<Element> attributes = getElements(classElement, "Attribute");
       
       for(Element attributeElt : attributes) {
         String attributeName = 
           className + "." + attributeElt.getAttributeValue("name");
         elements.put(attributeName, attributeElt);
       }
     }
 
     namespaceElt = pkg.getChild("Namespace.ownedElement", pkg.getNamespace());
     Collection<Element> packages = namespaceElt.getChildren("Package", pkg.getNamespace());
     for(Element subPkg : packages) {
       doPackage(subPkg, packageName);
     } 
 
   }
 }
