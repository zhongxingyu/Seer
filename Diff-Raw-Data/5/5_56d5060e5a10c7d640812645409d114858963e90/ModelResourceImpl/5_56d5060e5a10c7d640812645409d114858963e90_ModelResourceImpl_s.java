 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc.
  *  All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  *
  * @author Bob Brodt
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.CallActivity;
 import org.eclipse.bpmn2.CallableElement;
 import org.eclipse.bpmn2.DataInput;
 import org.eclipse.bpmn2.DataOutput;
 import org.eclipse.bpmn2.Interface;
 import org.eclipse.bpmn2.Message;
 import org.eclipse.bpmn2.Property;
 import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerFactory;
 import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerResourceImpl;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.ModelPackage;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.EAttributeImpl;
 import org.eclipse.emf.ecore.util.BasicFeatureMap;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.XMLLoad;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.XMLSave;
 import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Resource </b> associated with the package.
  * <!-- end-user-doc -->
  * @see org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.util.ModelResourceFactoryImpl
  * @generated NOT
  */
 public class ModelResourceImpl extends Bpmn2ModelerResourceImpl {
 	
 	/**
 	 * Creates an instance of the resource.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param uri the URI of the new resource.
 	 * @generated NOT
 	 */
 	public ModelResourceImpl(URI uri) {
 		super(uri);
 	}
 
 
     /**
      * Override this method to hook in our own XmlHandler
      */
     @Override
     protected XMLLoad createXMLLoad() {
         return new XMLLoadImpl(createXMLHelper()) {
             @Override
             protected DefaultHandler makeDefaultHandler() {
                 return new ModelXmlHandler(resource, helper, options);
             }
         };
     }
 
     @Override
 	protected XMLSave createXMLSave() {
 		return new Bpmn2ModelerXMLSave(createXMLHelper()) {
 			
 			private boolean needTargetNamespace = true;
 			
 			@Override
 			protected boolean shouldSaveFeature(EObject o, EStructuralFeature f) {
 				if (Bpmn2Package.eINSTANCE.getDocumentation_Text().equals(f))
 					return false;
 				// don't save the "name" feature of Property, DataInput or DataOutput objects.
 				// see ModelXmlHandler.processElement() for details...
 				if (o instanceof Property ||
 						o instanceof DataInput ||
 						o instanceof DataOutput ||
 						o instanceof Message) {
 					if (f.getName().equals("name"))
 						return false;
 				}
 				return super.shouldSaveFeature(o, f);
 			}
 
 			@Override
 			protected void init(XMLResource resource, Map<?, ?> options) {
 				super.init(resource, options);
 		        doc = new XMLString(Integer.MAX_VALUE, publicId, systemId, null) {
 		        	@Override
 		        	public void addAttribute(String name, String value) {
 		        		if ("targetNamespace".equals(name))
 		        			needTargetNamespace = false;
 		        		else if (XSI_SCHEMA_LOCATION.equals(name)) {
 		        			value = "http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd";
 		        		}
 		        		super.addAttribute(name, value);
 		        	}
 		        };
 			}
 			  
 			@Override
 			protected void addNamespaceDeclarations() {
 				if (needTargetNamespace)
 					doc.addAttribute("targetNamespace", ModelPackage.eNS_URI);
 				super.addNamespaceDeclarations();
 			}
 		};
 	}
 
 	/**
      * We need extend the standard SAXXMLHandler to hook into the handling of attribute references
      * which may be either simple ID Strings or QNames. We'll search through all of the objects'
      * IDs first to find the one we're looking for. If not, we'll try a QName search.
      */
     protected static class ModelXmlHandler extends Bpmn2ModelerXmlHandler {
 
         public ModelXmlHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
             super(xmiResource, helper, options);
         }
 
         @Override
         protected EStructuralFeature getFeature(EObject object, String prefix, String name, boolean isElement)
         {
         	EStructuralFeature result = null;
 		    if (object!=null && object.eClass().getEPackage() == ModelPackage.eINSTANCE) {
 		    	result = object.eClass().getEStructuralFeature(name);
 		    }
 		    if (result==null)
 		    	return super.getFeature(object, prefix, name, isElement);
 		    return result;
         }
         
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void processElement(String name, String prefix, String localName) {
 		    EObject peekObject = objects.peekEObject();
 		    if (peekObject!=null && peekObject.eClass().getEPackage() == ModelPackage.eINSTANCE) {
 		    	prefix = helper.getPrefix(ModelPackage.eINSTANCE);
 		    }
 			super.processElement(name, prefix, localName);
 			
 			// ugly hack for https://bugs.eclipse.org/bugs/show_bug.cgi?id=355686
 			// Remove the "type" attribute from the feature parentMap if there is one.
 			// The XSI type will have already been used to construct the EObject,
 			// so any "type" in the feature parentMap will be a duplicate which will
 			// cause problems during parsing.
 			// See also getXSIType()
 			EObject childObject = objects.peekEObject();
 			if (childObject!=null) {
 				try {
 					EStructuralFeature anyAttribute = childObject.eClass().getEStructuralFeature(Bpmn2Package.BASE_ELEMENT__ANY_ATTRIBUTE);
 					if (anyAttribute!=null) {
 						List<BasicFeatureMap.Entry> anyMap = (List<BasicFeatureMap.Entry>)childObject.eGet(anyAttribute);
 						if (anyMap!=null) {
 							List<BasicFeatureMap.Entry> removed = new ArrayList<BasicFeatureMap.Entry>();
 							for (BasicFeatureMap.Entry fe : anyMap) {
 								if (fe.getEStructuralFeature() instanceof EAttribute) {
 									EAttributeImpl a = (EAttributeImpl)fe.getEStructuralFeature();
 									String n = a.getName();
 									String ns = a.getExtendedMetaData().getNamespace();
 									if (TYPE.equals(n) && XSI_URI.equals(ns)) {
 										removed.add(fe);
 									}
 								}
 							}
 							if (removed.size()>0)
 								anyMap.removeAll(removed);
 						}
 					}
 					
 					// Some objects, like Property, DataInput and DataOutput use the "id" attribute instead
 					// of "name". We need to copy this "id" to "name" so that the UI can deal with them.
 					// Editorial: I don't agree with the decision to allow users to change model object IDs,
 					// since these are (theoretically) supposed to be unique; but it is what it is...
 					if (childObject instanceof Property ||
 							childObject instanceof DataInput ||
 							childObject instanceof DataOutput ||
 							childObject instanceof Message) {
 						EStructuralFeature nameFeature = childObject.eClass().getEStructuralFeature("name");
 						if (nameFeature!=null) {
 							Object value = childObject.eGet(nameFeature);
 							if (value==null || value.toString().isEmpty()) {
 								EStructuralFeature idFeature = childObject.eClass().getEStructuralFeature("id");
 								if (idFeature!=null) {
 									value = childObject.eGet(idFeature);
 									if (value!=null && !value.toString().isEmpty()) {
 										childObject.eSet(nameFeature, value);
 									}
 								}
 							}
 						}
 					}	
 				}
 				catch(Exception e) {
 				}
 			}
 		}
 
 		@Override
 		protected String getXSIType() {
 			if (isNamespaceAware)
 				return attribs.getValue(ExtendedMetaData.XSI_URI,
 						XMLResource.TYPE);
 
 			// If an parameter specifies multiple xsi:type data types, the last one wins.
 			// NOTE: we must check for "type" in any namespace with the URI
 			// "http://www.w3.org/2001/XMLSchema-instance"
 			String value = null;
 			int length = attribs.getLength();
 			for (int i = 0; i < length; ++i) {
 				attribs.getQName(i);
 				String localpart= attribs.getLocalName(i);
 				String prefix = null;
 				int ci = localpart.lastIndexOf(':');
 				if (ci>0) {
 					prefix = localpart.substring(0, ci); 
 					localpart = localpart.substring(ci+1);
 				}
 				if (TYPE.equals(localpart)) {
 					String uri = helper.getNamespaceURI(prefix);
 					if (XSI_URI.equals(uri)) {
 						value = attribs.getValue(i);
 						if (value!=null && value.startsWith("drools:") && value.contains("_._type"))
 							value = value.replace("_._type", "");
 					}
 				}
 			}
 			return value;
 		}
 		
 		@Override
 		protected void setValueFromId(EObject object, EReference eReference, String ids) {
			if (object instanceof CallActivity) {
 				// the CalledElementRef in CallActivity is just an ID. This means we need
 				// to create a CallableElement which is simply a proxy, not a real object.
 				CallActivity ca = (CallActivity)object;
 				CallableElement ce = Bpmn2ModelerFactory.create(CallableElement.class);
 				((InternalEObject)ce).eSetProxyURI(URI.createURI(ids));
 				ca.setCalledElementRef(ce);
 			}
 			else if (object instanceof Interface && eReference==Bpmn2Package.eINSTANCE.getInterface_ImplementationRef()) {
 				// the Interface.implementationRef is yet again just a string
 				Interface iface = (Interface)object;
 				iface.setImplementationRef( ModelUtil.createStringWrapper(ids) );
 			}
 			else
 				super.setValueFromId(object, eReference, ids);
 		}
     }
 } //ModelResourceImpl
