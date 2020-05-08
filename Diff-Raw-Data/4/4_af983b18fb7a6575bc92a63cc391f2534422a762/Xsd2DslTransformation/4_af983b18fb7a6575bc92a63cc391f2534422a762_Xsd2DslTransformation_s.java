 /*
  *    Project: The EDNA Kernel
  *             http://www.edna-site.org
  *
  *    File: "$Id:$"
  *
  *    Copyright (C) 2008-2009 European Synchrotron Radiation Facility
  *                            Grenoble, France
  *
  *    Principal authors: Marie-Francoise Incardona (incardon@esrf.fr)
  *                       Olof Svensson (svensson@esrf.fr)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published
  *    by the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    and the GNU Lesser General Public License  along with this program.
  *    If not, see <http://www.gnu.org/licenses/>.
  */
 package org.edna.datamodel.transformations.m2m;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xsd.XSDAnnotation;
 import org.eclipse.xsd.XSDComplexTypeDefinition;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDInclude;
 import org.eclipse.xsd.XSDModelGroup;
 import org.eclipse.xsd.XSDParticle;
 import org.eclipse.xsd.XSDSchema;
 import org.eclipse.xsd.XSDTypeDefinition;
 import org.eclipse.xsd.util.XSDSwitch;
 import org.eclipse.xtext.EcoreUtil2;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.resource.IResourceDescription;
 import org.edna.datamodel.datamodel.ComplexType;
 import org.edna.datamodel.datamodel.DatamodelFactory;
 import org.edna.datamodel.datamodel.DatamodelPackage;
 import org.edna.datamodel.datamodel.ElementDeclaration;
 import org.edna.datamodel.datamodel.Model;
 import org.edna.datamodel.datamodel.PrimitiveType;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * Transformation of XSD EDNA Datamodel to EDNA Datamodel DSL.
  * @author Karsten Thoms (karsten.thoms@itemis.de)
  */
 public class Xsd2DslTransformation extends AbstractDatamodelTransformation<XSDSchema, Model>{
 	@Override
 	public Model transform (XSDSchema sourceModel) {
 		this.sourceModel = sourceModel;
 		this.targetModel = DatamodelFactory.eINSTANCE.createModel();
 
 		targetModel.setTargetNamespace(nsUri);
 
 		loadIncludes ();
 
 		// Process XSDComplexTypeDefinitions and produce DSL ComplexType instances
 		new XSDSwitch<EObject>() {
 			private Stack<org.edna.datamodel.datamodel.Package> packageStack = new Stack<org.edna.datamodel.datamodel.Package>();
 
 			public EObject caseXSDSchema(XSDSchema object) {
 				// Sort all type definitions by name and call transformation
 				List<XSDTypeDefinition> types = Lists.newArrayList(object.getTypeDefinitions());
 				Collections.sort(types, Comparators.nameResolverComparator);
 				for (EObject obj : types) {
 					doSwitch(obj);
 				}
 				return object;
 
 			};
 			@Override
 			public EObject caseXSDComplexTypeDefinition(XSDComplexTypeDefinition object) {
 				if (monitor.isCanceled()) return null;
 				monitor.subTask(object.getName());
 				ComplexType type = createComplexType.apply(object);
 				// the package stack is not used yet, but might be later by evaluating qualified names in XSD annotations
 				// coming from a forward-transformed (UML or DSL) model
 				org.edna.datamodel.datamodel.Package currentPackage = packageStack.isEmpty() ? null : packageStack.peek();
 				if (currentPackage != null) {
 					currentPackage.getTypes().add(type);
 				} else {
 					targetModel.getTypes().add(type);
 				}
 				monitor.worked(1);
 				return object;
 			};
 		}.doSwitch(sourceModel);
 
 		// In a second step create the ElementDeclarations for each previously created complex
 		// type. This is because an element that refers to a XSDComplexType is an XSDSimpleTypeDefinition
 		// in the XSDElementDeclaration and we have to refer to the right XSDComplexTypeDefinition instead.
 		new XSDSwitch<EObject>() {
 			public EObject caseXSDSchema(XSDSchema object) {
 				for (XSDTypeDefinition type : object.getTypeDefinitions()) {
 					doSwitch(type);
 				}
 				return object;
 			};
 			public EObject caseXSDComplexTypeDefinition(XSDComplexTypeDefinition source) {
 				ComplexType target = createComplexType.apply(source);
 				XSDParticle complexTypeContent = (XSDParticle) ((XSDComplexTypeDefinition)source).getContent();
 
 				XSDModelGroup sequenceForAttributes = (XSDModelGroup) complexTypeContent.getContent();
 
 				for (XSDParticle p : sequenceForAttributes.getContents()) {
 					ElementDeclaration element = createElementDeclaration.apply(p);
 					target.getElements().add(element);
 				}
 				return target;
 			}
 
 		}.doSwitch(sourceModel);
 
 		return targetModel;
 	}
 
 	private EmfCreateFunction<XSDTypeDefinition, ComplexType> createComplexType =
 		new EmfCreateFunction<XSDTypeDefinition, ComplexType> (DatamodelPackage.Literals.COMPLEX_TYPE) {
 		private Map<String,ComplexType> types = null;
 		protected ComplexType find(XSDTypeDefinition source) {
 			if (types == null) {
 				types = Maps.newTreeMap(Comparators.stringComparator);
 				for (IResourceDescription desc : index.getAllResourceDescriptions()) {
 					for (IEObjectDescription objDesc : desc.getExportedObjects(DatamodelPackage.eINSTANCE.getComplexType())) {
 						ComplexType t = (ComplexType) objDesc.getEObjectOrProxy();
 						types.put(t.getName(), t);
 					}
 				}
 			}
 			return types.get(source.getName());
 		};
 		public ComplexType configure (XSDTypeDefinition source, ComplexType target) {
 			// The same type can be queried once as XSDSimpleTypeDefinition (from an attribute) and XSDComplexTypeDefinition.
 			// Because of this it is important to store the element manually in the types map.
 			types.put(source.getName(), target);
 			target.setName(source.getName());
 			// test for extension of types
 			if (source.getBaseType()!=null && !"anyType".equals(source.getBaseType().getName())) {
 				ComplexType baseType = (ComplexType) getType(source.getBaseType());
 				if (baseType!=null)
 				target.setBaseType(baseType);
 			}
 
 			XSDElementDeclaration elementDeclaration = sourceModel.resolveElementDeclaration(source.getName());
 			if (elementDeclaration != null) {
 				XSDAnnotation a = elementDeclaration.getAnnotation();
 				if (a != null && a.getUserInformation(null) != null && !a.getUserInformation(null).isEmpty()) {
 					String doc = a.getUserInformation(null).get(0).getTextContent();
 					target.setDoc(doc);
 				}
 			}
 			// we cannot create the attributes now. They must be produced in a second transformation step.
 			// A problem occurs when an ElementDeclaration refers to a Complex Type, since this
 			// reference is returned as XSDSimleTypeDefinition
 			return target;
 		};
 	};
 
 	/**
 	 * Polymorphic dispatch for concrete XSDTypeDefinitions
 	 * @param xsdType
 	 * @return
 	 */
 	private Object getType (XSDTypeDefinition xsdType) {
 		return new XSDSwitch<Object>(){
 			public Object caseXSDComplexTypeDefinition(XSDComplexTypeDefinition object) {
 				return createComplexType.apply(object);
 			};
 			public Object caseXSDSimpleTypeDefinition(org.eclipse.xsd.XSDSimpleTypeDefinition object) {
 				PrimitiveType primitiveType = getPrimitiveType(object.getName());
 				if (primitiveType != null) {
 					return primitiveType;
 				} else {
 					return createComplexType.apply(object);
 				}
 			};
 		}.doSwitch(xsdType);
 	}
 
 	/**
 	 * Mapping of primitive types
 	 * @param name A type name
 	 * @return Literal of {@link org.edna.datamodel.datamodel.PrimitiveType} or <code>null</code>
 	 */
 	private PrimitiveType getPrimitiveType (String name) {
 		if("string".equals(name)) return org.edna.datamodel.datamodel.PrimitiveType.STRING;
 		if("integer".equals(name)) return org.edna.datamodel.datamodel.PrimitiveType.INTEGER;
 		if("float".equals(name)) return org.edna.datamodel.datamodel.PrimitiveType.FLOAT;
 		if("double".equals(name)) return org.edna.datamodel.datamodel.PrimitiveType.DOUBLE;
 		if("boolean".equals(name)) return org.edna.datamodel.datamodel.PrimitiveType.BOOLEAN;
 		return null;
 	}
 
 	/**
 	 * Map XSDParticle to (DSL) ElementDeclaration
 	 */
 	private EmfCreateFunction<XSDParticle, ElementDeclaration> createElementDeclaration = new EmfCreateFunction<XSDParticle, ElementDeclaration>(DatamodelPackage.Literals.ELEMENT_DECLARATION) {
 		public ElementDeclaration configure (XSDParticle source, ElementDeclaration target) {
 			XSDElementDeclaration element = (XSDElementDeclaration) source.getTerm();
 			target.setName(element.getName());
 			target.setMultiple(source.getMaxOccurs() != 1);
 			target.setOptional(source.getMinOccurs() == 0);
 			// set documentation
 			if (element.getAnnotation()!=null) {
 				XSDAnnotation a = element.getAnnotation();
 				if (a.getUserInformation(null)!=null) {
 					String doc = a.getUserInformation(null).get(0).getTextContent();
 					target.setDoc(doc);
 				}
 			}
 			// get the type for the ElementDeclaration of that XSDParticle
 			// it can be either a PrimitiveType or a ComplexType
 			Object type = getType(element.getType());
 			if (type != null) {
 				if (type instanceof PrimitiveType) {
 					target.setType((PrimitiveType) type);
 				} else {
 					target.setRef((ComplexType) type);
 				}
 			}
 			return target;
 		};
 	};
 
 	/**
 	 * For each xs:include try to locate the corresponding file within the includePaths
 	 */
 	private void loadIncludes() {
 		for (XSDInclude include : Iterables.filter(sourceModel.getContents(), XSDInclude.class)) {
 			include.getSchemaLocation();
 			for (String includePath : includePaths) {
 				File f = new File (includePath+"/"+include.getSchemaLocation());
 				if (f.exists()) {
 					URI uri = URI.createFileURI(f.getPath());
 					getResourceSet().getResource(uri, true);
 					// load also referenced .edna_datamodel
 					URI dslUri = URI.createURI(uri.toString().replace(".xsd", ".edna_datamodel"));
 					getResourceSet().getResource(dslUri, true);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected int getAmountOfWork(XSDSchema sourceModel) {
 		int numberOfClasses = EcoreUtil2.eAllOfType(sourceModel, ComplexType.class).size();
 		return numberOfClasses+2;
 	}
 }
