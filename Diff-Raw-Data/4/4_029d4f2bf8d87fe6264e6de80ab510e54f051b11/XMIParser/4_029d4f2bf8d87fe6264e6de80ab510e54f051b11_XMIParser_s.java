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
 
 import gov.nih.nci.ncicb.cadsr.loader.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 
 import org.apache.log4j.Logger;
 
 import org.omg.uml.foundation.core.*;
 import org.omg.uml.foundation.datatypes.MultiplicityRange;
 import org.omg.uml.foundation.extensionmechanisms.*;
 import org.omg.uml.modelmanagement.Model;
 import org.omg.uml.modelmanagement.UmlPackage;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
 
 import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 
 import gov.nih.nci.codegen.core.util.UML13Utils;
 import gov.nih.nci.codegen.core.access.UML13ModelAccess;
 import gov.nih.nci.codegen.framework.ModelAccess;
 
 import java.io.*;
 
 import java.util.*;
 
 /**
  * Implemetation of <code>Parser</code> for XMI files. Navigates the XMI document and sends UML Object events.
  *
  * @author <a href="mailto:ludetc@mail.nih.gov">Christophe Ludet</a>
  */
 public class XMIParser implements Parser {
   private static final String EA_CONTAINMENT = "containment";
   private static final String EA_UNSPECIFIED = "Unspecified";
   private UMLHandler listener;
 
   private String packageName = "";
   private String className = "";
   private Logger logger = Logger.getLogger(XMIParser.class.getName());
 //   private List<NewGeneralizationEvent> generalizationEvents = new ArrayList<NewGeneralizationEvent>();
 
 
   private List<NewAssociationEvent> associationEvents = new ArrayList<NewAssociationEvent>();
   
   // Key = child class Name
   Map<String, NewGeneralizationEvent> childGeneralizationMap = 
     new HashMap<String, NewGeneralizationEvent>();
 
 
   private ProgressListener progressListener = null;
 
   private final static String VD_STEREOTYPE = "CADSR Value Domain";
 
   public static final String TV_PROP_ID = "CADSR_PROP_ID";
   public static final String TV_PROP_VERSION = "CADSR_PROP_VERSION";
 
   public static final String TV_DE_ID = "CADSR_DE_ID";
   public static final String TV_DE_VERSION = "CADSR_DE_VERSION";
 
   public static final String TV_VALUE_DOMAIN = "Value Domain";
 
   public static final String TV_VD_ID = "CADSR_VD_ID";
   public static final String TV_VD_VERSION = "CADSR_VD_VERSION";
   public static final String TV_OC_ID = "CADSR_OC_ID";
   public static final String TV_OC_VERSION = "CADSR_OC_VERSION";
 
 
   public static final String TV_VD_DEFINITION = "CADSR_ValueDomainDefinition";
   public static final String TV_VD_DATATYPE = "CADSR_ValueDomainDatatype";
   public static final String TV_VD_TYPE = "CADSR_ValueDomainType";
   public static final String TV_CD_ID = "CADSR_ConceptualDomainPublicID";
   public static final String TV_CD_VERSION = "CADSR_ConceptualDomainVersion";
 
   /**
    * Tagged Value name for Concept Code
    */
   public static final String TV_CONCEPT_CODE = "ConceptCode";
 
   /**
    * Tagged Value name for Concept Preferred Name
    */
   public static final String TV_CONCEPT_PREFERRED_NAME = "ConceptPreferredName";
 
   /**
    * Tagged Value name for Concept Definition
    */
   public static final String TV_CONCEPT_DEFINITION = "ConceptDefinition";
 
   /**
    * Tagged Value name for Concept Definition Source
    */
   public static final String TV_CONCEPT_DEFINITION_SOURCE = "ConceptDefinitionSource";
 
 
   /**
    * Qualifier Tagged Value prepender. 
    */
   public static final String TV_QUALIFIER = "Qualifier";
 
   /**
    * Qualifier Tagged Value prepender. 
    */
   public static final String TV_TYPE_CLASS = "ObjectClass";
 
   /**
    * Qualifier Tagged Value prepender. 
    */
   public static final String TV_TYPE_PROPERTY = "Property";
 
 
 
   /**
    * Tagged Value name for Documentation
    */
   public static final String TV_DOCUMENTATION = "documentation";
   public static final String TV_DESCRIPTION = "description";
   public static final String TV_HUMAN_REVIEWED = "HUMAN_REVIEWED";
 
 
   private String[] bannedClassNames = null;
   {
     bannedClassNames = PropertyAccessor.getProperty("banned.classNames").split(",");
   }
 
   public void setEventHandler(LoaderHandler handler) {
     this.listener = (UMLHandler) handler;
   }
 
   public void parse(String filename) throws ParserException {
     try {
 
       ProgressEvent evt = new ProgressEvent();
       evt.setMessage("Parsing ...");
       fireProgressEvent(evt);
 
       ModelAccess access = new UML13ModelAccess();
 
       String s = filename.replaceAll("\\ ", "%20");
 
       // Some file systems use absolute URIs that do 
       // not start with '/'. 
       if(!s.startsWith("/"))
         s = "/" + s;    
       String uriStr = new java.net.URI("file://" + s).toString();
       access.readModel(uriStr, "EA Model");
 
       uml.UmlPackage umlExtent = (uml.UmlPackage) access.getOutermostExtent();
       
       Model model = UML13Utils.getModel(umlExtent, "EA Model");
 
       Iterator it = model.getOwnedElement().iterator();
 
       while (it.hasNext()) {
         Object o = it.next();
 
         if (o instanceof UmlPackage) {
           doPackage((UmlPackage) o);
         }
         else if (o instanceof DataType) {
           doDataType((DataType) o);
         }
         else if (o instanceof UmlAssociation) {
           doAssociation((UmlAssociation) o);
         }
         else if (o instanceof UmlClass) {
           doClass((UmlClass) o);
         }
         else {
           logger.debug("Root Element: " + o.getClass());
         }
       }
 
       fireLastEvents();
     }
     catch (Exception e) {
 //       logger.fatal("Could not parse: " + filename);
 //       logger.fatal(e, e);
       throw new ParserException(e);
     } // end of try-catch
   }
 
   private void doPackage(UmlPackage pack) {
     UMLDefaults defaults = UMLDefaults.getInstance();
 
     if (packageName.length() == 0) {
       //       if(pack.getName().indexOf(" ") == -1)
       packageName = pack.getName();
     }
     else {
       //       if(pack.getName().indexOf(" ") == -1)
       packageName += ("." + pack.getName());
     }
 
     if(isInPackageFilter(packageName)) {
       listener.newPackage(new NewPackageEvent(packageName));
     } else {
       logger.info(PropertyAccessor.getProperty("skip.package", packageName));
     }
 
     Iterator it = pack.getOwnedElement().iterator();
 
     while (it.hasNext()) {
       Object o = it.next();
 
       if (o instanceof UmlPackage) {
         String oldPackage = packageName;
         doPackage((UmlPackage) o);
         packageName = oldPackage;
       }
       else if (o instanceof UmlClass) {
         doClass((UmlClass) o);
       }
       else if (o instanceof Stereotype) {
         doStereotype((Stereotype) o);
       }
       else if (o instanceof Component) {
         doComponent((Component) o);
       }
       else if (o instanceof UmlAssociation) {
         doAssociation((UmlAssociation) o);
       }
       else if (o instanceof Interface) {
         doInterface((Interface) o);
       }
       else {
         logger.debug("Package Child: " + o.getClass());
       }
     }
 
     packageName = "";
   }
 
   private void doClass(UmlClass clazz) {
     UMLDefaults defaults = UMLDefaults.getInstance();
     String pName = getPackageName(clazz);
 
     className = clazz.getName();
 
     Stereotype st = UML13Utils.getStereotype(clazz);
     if(st != null)
       if(st.getName().equals(VD_STEREOTYPE)) {
         doValueDomain(clazz);
         return;
       }
 
     if (pName != null) {
       className = pName + "." + className;
     }
 
     ProgressEvent evt = new ProgressEvent();
     evt.setMessage("Parsing " + className);
     fireProgressEvent(evt);
 
     NewClassEvent event = new NewClassEvent(className.trim());
     event.setPackageName(pName);
 
     setConceptInfo(clazz, event, TV_TYPE_CLASS);
 
     logger.debug("CLASS: " + className);
     logger.debug("CLASS PACKAGE: " + getPackageName(clazz));
 
     if(isClassBanned(className)) {
       logger.info(PropertyAccessor.getProperty("class.filtered", className));
       return;
     }
     
     if(StringUtil.isEmpty(pName)) 
     {
       logger.info(PropertyAccessor.getProperty("class.no.package", className));
       return;
     }
 
     TaggedValue tv = UML13Utils.getTaggedValue(clazz, TV_DOCUMENTATION);
     if(tv != null) {
       event.setDescription(tv.getValue());
     }
 
     tv = UML13Utils.getTaggedValue(clazz, TV_HUMAN_REVIEWED);
     if(tv != null) {
       event.setReviewed(tv.getValue().equals("1")?true:false);
     }
 
     // Hold on this for now
 
 //     tv = UML13Utils.getTaggedValue(clazz, TV_OC_ID);
 //     if(tv != null) {
 //       event.setPersistenceId(tv.getValue());
 //     }
 
 //     tv = UML13Utils.getTaggedValue(clazz, TV_OC_VERSION);
 //     if(tv != null) {
 //       try {
 //         event.setPersistenceVersion(new Float(tv.getValue()));
 //       } catch (NumberFormatException e){
 //       } // end of try-catch
 //     }
 
 
     if(isInPackageFilter(pName)) {
       listener.newClass(event);
     } else {
       logger.info(PropertyAccessor.getProperty("class.filtered", className));
       return;
     }
 
     for (Iterator it = clazz.getFeature().iterator(); it.hasNext();) {
       Object o = it.next();
 
       if (o instanceof Attribute) {
         doAttribute((Attribute) o);
       }
       else if (o instanceof Operation) {
         doOperation((Operation) o);
       }
       else {
         logger.debug("Class child: " + o.getClass());
       }
     }
 
     className = "";
 
     for (Iterator it = clazz.getGeneralization().iterator(); it.hasNext();) {
       Generalization g = (Generalization) it.next();
 
       if (g.getParent() instanceof UmlClass) {
         UmlClass p = (UmlClass) g.getParent();
         NewGeneralizationEvent gEvent = new NewGeneralizationEvent();
         gEvent.setParentClassName(
           getPackageName(p) + "." + p.getName());
 
         gEvent.setChildClassName(
           getPackageName(clazz) + "." + clazz.getName());
 
 //         generalizationEvents.add(gEvent);
         childGeneralizationMap.put(gEvent.getChildClassName(), gEvent);
 
       }
     }
   }
 
   private void doValueDomain(UmlClass clazz) {
     UMLDefaults defaults = UMLDefaults.getInstance();
 
     className = clazz.getName();
 
     ProgressEvent evt = new ProgressEvent();
     evt.setMessage("Parsing " + className);
     fireProgressEvent(evt);
 
     NewValueDomainEvent event = new NewValueDomainEvent(className.trim());
 //     event.setPackageName("ValueDomains");
 
     setConceptInfo(clazz, event, TV_TYPE_CLASS);
 
     logger.debug("Value Domain: " + className);
 
     TaggedValue tv = UML13Utils.getTaggedValue(clazz, TV_VD_DEFINITION);
     if(tv != null) {
       event.setDescription(tv.getValue());
     }
 
     tv = UML13Utils.getTaggedValue(clazz, TV_VD_DATATYPE);
     if(tv != null) {
       event.setDatatype(tv.getValue());
     }
 
     tv = UML13Utils.getTaggedValue(clazz, TV_VD_TYPE);
     if(tv != null) {
       event.setType(tv.getValue());
     }
 
     tv = UML13Utils.getTaggedValue(clazz, TV_CD_ID);
     if(tv != null) {
       event.setCdId(tv.getValue());
     }
     
     tv = UML13Utils.getTaggedValue(clazz, TV_CD_VERSION);
     if(tv != null) {
       try {
         event.setCdVersion(new Float(tv.getValue()));
       } catch (NumberFormatException e){
         logger.warn(PropertyAccessor.getProperty("version.numberFormatException", tv.getValue()));
       } // end of try-catch
     }
 
 
     tv = UML13Utils.getTaggedValue(clazz, TV_HUMAN_REVIEWED);
     if(tv != null) {
       event.setReviewed(tv.getValue().equals("1")?true:false);
     }
 
 
     for (Object o : clazz.getFeature()) {
       if (o instanceof Attribute) {
         doValueMeaning((Attribute) o);
       }
       else {
         logger.debug("Class child: " + o.getClass());
       }
     }
 
    listener.newValueDomain(event);
     className = "";
 
   }
 
 
 
   private void doInterface(Interface interf) {
     className = packageName + "." + interf.getName();
 
     //     logger.debug("Class: " + className);
     listener.newInterface(new NewInterfaceEvent(className.trim()));
 
     Iterator it = interf.getFeature().iterator();
 
     while (it.hasNext()) {
       Object o = it.next();
       if (o instanceof Attribute) {
         doAttribute((Attribute) o);
       }
       else if (o instanceof Operation) {
         doOperation((Operation) o);
       }
       else {
         logger.debug("Class child: " + o.getClass());
       }
     }
 
     className = "";
   }
 
   private void doAttribute(Attribute att) {
     NewAttributeEvent event = new NewAttributeEvent(att.getName().trim());
     event.setClassName(className);
 
     if(att.getType() == null || att.getType().getName() == null) {
       ValidationItems.getInstance()
         .addItem(new ValidationFatal
                  (PropertyAccessor
                   .getProperty
                   ("validation.type.missing.for"
                    , event.getClassName() + "." + event.getName()),
                   null));
       return;
     }
 
     // See if datatype is a simple datatype or a value domain.
     TaggedValue tv = UML13Utils.getTaggedValue(att, TV_VALUE_DOMAIN);
     if(tv != null) {       // Use Value Domain
       event.setType(tv.getValue());
     } else {               // Use datatype
       event.setType(att.getType().getName());
     }
 
     tv = UML13Utils.getTaggedValue(att, TV_DESCRIPTION);
     if(tv != null) {
       event.setDescription(tv.getValue());
     } else {
       tv = UML13Utils.getTaggedValue(att, TV_DOCUMENTATION);
       if(tv != null) {
         event.setDescription(tv.getValue());
       }
     }
 
     tv = UML13Utils.getTaggedValue(att, TV_HUMAN_REVIEWED);
     if(tv != null) {
       event.setReviewed(tv.getValue().equals("1")?true:false);
     }
 
     // Is this attribute mapped to an existing CDE?
     tv = UML13Utils.getTaggedValue(att, TV_DE_ID);
     if(tv != null) {
       event.setPersistenceId(tv.getValue());
     }
 
     tv = UML13Utils.getTaggedValue(att, TV_DE_VERSION);
     if(tv != null) {
       try {
         event.setPersistenceVersion(new Float(tv.getValue()));
       } catch (NumberFormatException e){
       } // end of try-catch
     }
 
     setConceptInfo(att, event, TV_TYPE_PROPERTY);
 
     listener.newAttribute(event);
   }
 
   private void doValueMeaning(Attribute att) {
     NewValueMeaningEvent event = new NewValueMeaningEvent(att.getName().trim());
     event.setValueDomainName(className);
 
     TaggedValue tv = UML13Utils.getTaggedValue(att, TV_HUMAN_REVIEWED);
     if(tv != null) {
       event.setReviewed(tv.getValue().equals("1")?true:false);
     }
 
     setConceptInfo(att, event, TV_TYPE_PROPERTY);
 
     listener.newValueMeaning(event);
   }
 
 
   private void doDataType(DataType dt) {
     listener.newDataType(new NewDataTypeEvent(dt.getName()));
   }
 
   private void doOperation(Operation op) {
     NewOperationEvent event = new NewOperationEvent(op.getName());
     event.setClassName(className);
     listener.newOperation(event);
   }
 
   private void doStereotype(Stereotype st) {
     logger.debug("--- Stereotype " + st.getName());
   }
 
   private void doAssociation(UmlAssociation assoc) {
     Iterator it = assoc.getConnection().iterator();
     NewAssociationEvent event = new NewAssociationEvent();
     event.setRoleName(assoc.getName());
 
     String navig = "";
 
     if (it.hasNext()) {
       Object o = it.next();
 
       if (o instanceof AssociationEnd) {
         AssociationEnd end = (AssociationEnd) o;
 //         logger.debug("end A is navigable: " + end.isNavigable());
 
         if (end.isNavigable()) {
           navig += 'A';
         }
 
         Classifier classif = end.getType();
         if(!isInPackageFilter(getPackageName(classif))) {
           logger.info(PropertyAccessor.getProperty("skip.association", classif.getNamespace().getName()));
           logger.debug("classif name: " + classif.getName());
           return;
         }
 
 
         Collection range = end.getMultiplicity().getRange();
         for (Iterator it2 = range.iterator(); it2.hasNext();) {
           MultiplicityRange mr = (MultiplicityRange) it2.next();
           int low = mr.getLower();
           int high = mr.getUpper();
           event.setALowCardinality(low);
           event.setAHighCardinality(high);
         }
 
         event.setAClassName(getPackageName(classif) + "." + classif.getName());
         event.setARole(end.getName());
         if(event.getAClassName() == null) {
           logger.debug("AClassName: NULL");
           return;
         } else {
           logger.debug("AClassName: " + event.getAClassName());
         }
       }
     } else {
       logger.debug("Association has one missing END");
       return;
     }
 
     if (it.hasNext()) {
       Object o = it.next();
 
       if (o instanceof AssociationEnd) {
         AssociationEnd end = (AssociationEnd) o;
 //         logger.debug("end B is navigable: " + end.isNavigable());
 
         if (end.isNavigable()) {
           navig += 'B';
         }
 
         Classifier classif = end.getType();
         if(!isInPackageFilter(getPackageName(classif))) {
           logger.info(PropertyAccessor.getProperty("skip.association", classif.getName()));
           logger.debug("classif name: " + classif.getNamespace().getName());
           return;
         }
 
         Collection range = end.getMultiplicity().getRange();
         for (Iterator it2 = range.iterator(); it2.hasNext();) {
           MultiplicityRange mr = (MultiplicityRange) it2.next();
           int low = mr.getLower();
           int high = mr.getUpper();
           event.setBLowCardinality(low);
           event.setBHighCardinality(high);
         }
 
         event.setBClassName(getPackageName(classif) + "." + classif.getName());
         event.setBRole(end.getName());
         if(event.getBClassName() == null)
           return;
       }
     } else {
       logger.debug("Association has one missing END");
       return;
     }
 
 //     logger.debug("A END -- " + event.getAClassName() + " " + event.getALowCardinality());
 //     logger.debug("B END -- " + event.getBClassName() + " " + event.getBLowCardinality());
 
     // netbeans seems to read self pointing associations wrong. Such that an end is navigable but has no target role, even though it does in the model.
     if(event.getAClassName().equals(event.getBClassName())) {
       if(navig.equals("B") && StringUtil.isEmpty(event.getBRole())) {
         event.setBRole(event.getARole());
         event.setBLowCardinality(event.getALowCardinality());
         event.setBHighCardinality(event.getAHighCardinality());
       } else if (navig.equals("A") && StringUtil.isEmpty(event.getARole())) {
         event.setARole(event.getBRole());
         event.setALowCardinality(event.getBLowCardinality());
         event.setAHighCardinality(event.getBHighCardinality());
       }
     }
 
     event.setDirection(navig);
 
     logger.debug("Adding association. AClassName: " + event.getAClassName());
     associationEvents.add(event);
 
   }
 
   private void doComponent(Component comp) {
     logger.debug("--- Component: " + comp.getName());
   }
 
   private String cardinality(AssociationEnd end) {
     Collection range = end.getMultiplicity().getRange();
 
     for (Iterator it = range.iterator(); it.hasNext();) {
       MultiplicityRange mr = (MultiplicityRange) it.next();
       int low = mr.getLower();
       int high = mr.getUpper();
 
       if (low == high) {
         return "" + low;
       }
       else {
         String h = (high >= 0) ? ("" + high) : "*";
 
         return low + ".." + h;
       }
     }
 
     return "";
   }
 
   private void fireLastEvents() {
     for (Iterator<NewAssociationEvent> it = associationEvents.iterator(); it.hasNext();) {
       listener.newAssociation(it.next());
     }
 
 
     for(Iterator<String> it = childGeneralizationMap.keySet().iterator(); it.hasNext(); ) {
       String childClass = it.next();
       recurseInheritance(childClass);
       
 //       listener.newGeneralization(childGeneralizationMap.get(childClass));
       it = childGeneralizationMap.keySet().iterator(); it.hasNext();
 
     }
 
     ProgressEvent evt = new ProgressEvent();
     evt.setGoal(100);
     evt.setStatus(100);
     evt.setMessage("Done");
     fireProgressEvent(evt);
 
   }
 
   private void recurseInheritance(String childClass) {
     NewGeneralizationEvent genz = childGeneralizationMap.get(childClass);
     if(childGeneralizationMap.containsKey(genz.getParentClassName())) {
       recurseInheritance(genz.getParentClassName());
     }
 
     listener.newGeneralization(genz);
     childGeneralizationMap.remove(childClass);
 
   }
 
   private void setConceptInfo(ModelElement elt, NewConceptualEvent event, String type) {
     NewConceptEvent concept = new NewConceptEvent();
     setConceptInfo(elt, concept, type, "", 0);
 
     if(!StringUtil.isEmpty(concept.getConceptCode()))
       event.addConcept(concept);
     
     concept = new NewConceptEvent();
     for(int i=1;setConceptInfo(elt, concept, type, TV_QUALIFIER, i); i++) {
 
       if(!StringUtil.isEmpty(concept.getConceptCode()))
         event.addConcept(concept);
 
       concept = new NewConceptEvent();
     }
 
   }
 
   private boolean setConceptInfo(ModelElement elt, NewConceptEvent event, String type, String pre, int n) {
 
     TaggedValue tv = UML13Utils.getTaggedValue(elt, type + pre + TV_CONCEPT_CODE + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptCode(tv.getValue().trim());
     } else 
       return false;
 
     tv = UML13Utils.getTaggedValue(elt, type + pre + TV_CONCEPT_DEFINITION + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptDefinition(tv.getValue().trim());
     }
 
     tv = UML13Utils.getTaggedValue(elt, type + pre + TV_CONCEPT_DEFINITION_SOURCE + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptDefinitionSource(tv.getValue().trim());
     }
     
     tv = UML13Utils.getTaggedValue(elt, type + pre + TV_CONCEPT_PREFERRED_NAME + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptPreferredName(tv.getValue().trim());
     }
 
     event.setOrder(n);
     return true;
 
   }
 
   private boolean isInPackageFilter(String pName) {
     Map packageFilter = UMLDefaults.getInstance().getPackageFilter();
     return (packageFilter.size() == 0) || (packageFilter.containsKey(pName) || (UMLDefaults.getInstance().getDefaultPackageAlias() != null));
   }
 
   private String getPackageName(ModelElement elt) {
     StringBuffer pack = new StringBuffer();
     String s = null;
     do {
       s = null;
       if (elt.getNamespace() != null) {
         s = elt.getNamespace().getName(); 
         if(s.indexOf(" ") == -1) {
 //         if(!s.startsWith("EA ")) {
           if(pack.length() > 0)
             pack.insert(0, '.');
           pack.insert(0, s);
         }
         elt = elt.getNamespace();
       }
     } while (s != null);
     
     return pack.toString();
   }
 
   private boolean isClassBanned(String className) {
     for(int i=0; i<bannedClassNames.length; i++) {
       if(className.indexOf(bannedClassNames[i]) > -1) return true;
     }
     return false;
   }
 
   private void fireProgressEvent(ProgressEvent evt) {
     if(progressListener != null)
       progressListener.newProgressEvent(evt);
   }
 
   public void addProgressListener(ProgressListener listener) {
     progressListener = listener;
   }
   
 }
