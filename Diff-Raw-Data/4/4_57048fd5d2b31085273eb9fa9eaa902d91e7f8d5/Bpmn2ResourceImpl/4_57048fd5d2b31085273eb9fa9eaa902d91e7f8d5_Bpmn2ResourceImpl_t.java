 /**
  * <copyright>
  * 
  * Copyright (c) 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Reiner Hille-Doering (SAP AG) - initial API and implementation and/or initial documentation
  * 
  * </copyright>
  */
 package org.eclipse.bpmn2.util;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
 import org.eclipse.bpmn2.Import;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.XMLLoad;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.XMLSave;
 import org.eclipse.emf.ecore.xmi.impl.SAXXMLHandler;
 import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Resource </b> associated with the package.
  * <!-- end-user-doc -->
  * @see org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl
  * @generated
  */
 public class Bpmn2ResourceImpl extends XMLResourceImpl {
 
     private QNameURIHandler uriHandler;
     private BpmnXmlHelper xmlHelper;
 
     /**
      * Creates an instance of the resource.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @param uri the URI of the new resource.
      * @generated NOT
      */
     public Bpmn2ResourceImpl(URI uri) {
         super(uri);
         this.xmlHelper = new BpmnXmlHelper(this);
         this.uriHandler = new QNameURIHandler(xmlHelper);
         this.getDefaultLoadOptions().put(XMLResource.OPTION_URI_HANDLER, uriHandler);
         this.getDefaultSaveOptions().put(XMLResource.OPTION_URI_HANDLER, uriHandler);
     }
 
     // This method is called by all save methods - save(Document,...), doSave(Writer/OutputStream, ...) - in superclasses.
     @Override
     protected XMLSave createXMLSave() {
         prepareSave();
         return super.createXMLSave();
     }
 
     /**
      * Prepares this resource for saving.
      * 
      * Sets all ID attributes, that are not set, to a generated UUID.
      */
     protected void prepareSave() {
         EObject cur;
         for (Iterator<EObject> iter = getAllContents(); iter.hasNext();) {
             cur = iter.next();
 
             EStructuralFeature idAttr = cur.eClass().getEIDAttribute();
             if (idAttr != null && !cur.eIsSet(idAttr)) {
                 cur.eSet(idAttr, EcoreUtil.generateUUID());
             }
         }
     }
 
     /**
      * We must override this method for having an own XMLHandler
      */
     @Override
     protected XMLLoad createXMLLoad() {
         return new XMLLoadImpl(createXMLHelper()) {
             @Override
             protected DefaultHandler makeDefaultHandler() {
                 return new BpmnXmlHandler(resource, helper, options);
             }
         };
     }
 
     @Override
     protected XMLHelper createXMLHelper() {
         return this.xmlHelper;
     }
 
     /**
      * We need extend the standard SAXXMLHandler to hook into the handling of attribute references - which are no URIs but QNames.
      * @author Reiner Hille
      *
      */
     protected class BpmnXmlHandler extends SAXXMLHandler {
 
         public BpmnXmlHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
             super(xmiResource, helper, options);
         }
 
         /**
          * Overridden to be able to convert QName references in attributes to URIs during load.
          * @param ids
          *  In our case the parameter will contain exactly one QName that we resolve to URI.
          */
         @Override
         protected void setValueFromId(EObject object, EReference eReference, String ids) {
 
             super.setValueFromId(object, eReference, ((QNameURIHandler) uriHandler)
                     .convertQNameToUri(ids));
         }
 
     }
 
     /**
      * Extend XML Helper to gain access to the different XSD namespace handling features.
      * @author Reiner Hille
      *
      */
     protected class BpmnXmlHelper extends XMLHelperImpl {
 
         public BpmnXmlHelper(Bpmn2ResourceImpl resource) {
             super(resource);
         }
 
         private Definitions getDefinitions() {
             for (EObject eobj : getResource().getContents()) {
                 if (eobj instanceof Definitions) {
                     return (Definitions) eobj;
                } else if (eobj instanceof DocumentRoot) {
                    return ((DocumentRoot) eobj).getDefinitions();
                 }
             }
             return null;
         }
 
         /**
          * Checks if the given prefix is pointing to the current target namespace and thus is optional.
          * The method is called during load.
          * @param prefix
          * @return
          */
         public boolean isTargetNamespace(String prefix) {
             return prefix.equals(getDefinitions().getTargetNamespace());
         }
 
         /**
          * Looks up the given prefix in the list of BPMN import elements and returns - if found - the corresponding file location.
          * The method is called during load.
          * @param prefix
          * @return
          */
         public String getPathForPrefix(String prefix) {
             String ns = this.getNamespaceURI(prefix);
             if (ns != null) {
                 for (Import imp : getDefinitions().getImports()) {
                     if (ns.equals(imp.getNamespace())) {
                         // TODO: Also check that imp.getType() is BPMN
                         return imp.getLocation();
                     }
                 }
             }
             return "";
         }
 
         /**
          * Partly stolen from XmlHelperImpl.setPrefixToNamespaceMap().
          * Ensuring that namespace declaration is saved seems to be really tricky.
          * We will necessarily create a dummy package to ensure that later XmlSaveImpl.addNamespaceDeclarations() writes the ns declaration for us
          * @param namespace
          * @return
          */
         private String getPrefixDuringSave(String namespace) {
             EPackage ePackage = extendedMetaData.getPackage(namespace);
             if (ePackage == null) {
                 ePackage = extendedMetaData.demandPackage(namespace);
                 // This will internally create a nice prefix
             }
             String prefix = ePackage.getNsPrefix();
             // I'm not sure if the following code is needed, but I keep it to avoid inconsistencies
             if (!packages.containsKey(ePackage)) {
                 packages.put(ePackage, prefix);
             }
             prefixesToURIs.put(prefix, namespace);
             return prefix;
         }
 
         /**
          * This is called on save to convert from a file-based URI to a namespace prefix.
          * It might be necessary to add a new namespace declaration to the file, if  the 
          * namespace was not known to far.
          * @param filePath
          * @return
          */
         public String getNsPrefix(String filePath) {
             String ns = null;
             String prefix = "";
             for (Import imp : getDefinitions().getImports()) {
                 if (filePath.equals(imp.getLocation())) {
                     // TODO: Also check that imp.getType() is BPMN
                     ns = imp.getNamespace();
                     break;
                 }
             }
             if (ns != null) {
                 prefix = getPrefixDuringSave(ns);
             }
             return prefix;
         }
     }
 
 } //Bpmn2ResourceImpl
