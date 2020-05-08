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
 
 package org.eclipse.bpmn2.modeler.ui.adapters.properties;
 
 import javax.xml.namespace.QName;
 
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.Interface;
 import org.eclipse.bpmn2.Process;
 import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
 import org.eclipse.bpmn2.modeler.core.adapters.FeatureDescriptor;
 import org.eclipse.bpmn2.modeler.core.adapters.ObjectDescriptor;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.bpmn2.modeler.core.utils.NamespaceUtil;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.wst.wsdl.Fault;
 import org.eclipse.wst.wsdl.Input;
 import org.eclipse.wst.wsdl.Message;
 import org.eclipse.wst.wsdl.Operation;
 import org.eclipse.wst.wsdl.Output;
 import org.eclipse.wst.wsdl.Part;
 import org.eclipse.wst.wsdl.PortType;
 import org.eclipse.xsd.XSDAttributeDeclaration;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDSchema;
 import org.eclipse.xsd.XSDTypeDefinition;
 
 /**
  * @author Bob Brodt
  *
  */
 public class InterfacePropertiesAdapter extends ExtendedPropertiesAdapter<Interface> {
 
 	/**
 	 * @param adapterFactory
 	 * @param object
 	 */
 	public InterfacePropertiesAdapter(AdapterFactory adapterFactory, Interface object) {
 		super(adapterFactory, object);
 		
     	final EStructuralFeature ref = Bpmn2Package.eINSTANCE.getInterface_ImplementationRef();
     	setFeatureDescriptor(ref,
 			new FeatureDescriptor<Interface>(adapterFactory,object,ref) {
 				@Override
 				public String getDisplayName(Object context) {
 					final Interface iface = adopt(context);
 							
 					if (iface.getImplementationRef()!=null) {
 						String text = ModelUtil.getStringWrapperValue( iface.getImplementationRef() ); // + type;
 						if (text==null)
 							return ModelUtil.getDisplayName(iface.getImplementationRef());
 						return text;
 					}
 					return "";
 				}
 				
 	    		@Override
 				public EObject createFeature(Resource resource, Object context, EClass eClass) {
 					final Interface iface = adopt(context);
 
 					EObject impl = ModelUtil.createStringWrapper("");
 					iface.setImplementationRef(impl);
 					return impl;
 	    		}
 
 	    		@Override
 	    		public Object getValue(Object context) {
 					final Interface iface = adopt(context);
 					if (iface.getImplementationRef()!=null)
 						return iface.getImplementationRef();
 					return ModelUtil.createStringWrapper("");
 	    		}
 
 	    		@Override
 	    		public void setValue(Object context, Object value) {
 	    			Interface object = adopt(context);
 	    			Resource resource = ModelUtil.getResource(object);
 	    			
 	    			if (value instanceof PortType) {
 	    				PortType portType = (PortType)value;
 	    				QName qname = portType.getQName();
 	    				String prefix = NamespaceUtil.getPrefixForNamespace(resource, qname.getNamespaceURI());
 	    				if (prefix==null)
 	    					prefix = NamespaceUtil.addNamespace(resource, qname.getNamespaceURI());
	    				String str = "";
 	    				if (prefix!=null)
	    					str = prefix + ":";
	    				str += qname.getLocalPart();
	    				value = str;
 	    			}
 	    			else if (value instanceof Process) {
 	    				Process process = (Process)value;
 	    				if (process.getSupportedInterfaceRefs().size()>0)
 	    					value = process.getSupportedInterfaceRefs().get(0).getImplementationRef();
 	    			}
 
 	    			if (value instanceof String) {
 						value = ModelUtil.createStringWrapper((String)value);
 	    			}
 	    			else if (!ModelUtil.isStringWrapper(value)) {
 	    				return;
 	    			}
 	    			super.setValue(object,value);
 	    		}
     		}
     	);
     	
 		setObjectDescriptor(new ObjectDescriptor<Interface>(adapterFactory, object) {
 			@Override
 			public String getDisplayName(Object context) {
 				return getFeatureDescriptor(ref).getDisplayName(context);
 			}
 
 			@Override
 			public boolean equals(Object obj) {
 				if (obj instanceof Interface) {
 					Interface iface = (Interface)obj;
 					if (ModelUtil.compare(iface.getName(),(object).getName())) {
 						if (ModelUtil.compare(iface.getImplementationRef(),(object).getImplementationRef()))
 							return true;
 					}
 				}
 				return false;
 			}
 		});
 
 	}
 
 }
