 /**
  * Copyright (c) 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 package org.eclipse.rmf.internal.serialization;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingExtendedMetaData;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingExtendedMetaDataImpl;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingResource;
 
 // TODO: add javadoc with images
 // TODO: deactivate or implement dom support 
 // TODO: implement prefix overwrite
 // TODO: overwrite all explicitly not used methods from super-class and use asserts to show that it is not allowed to go there
// TODO: consider using the "kind" mechanism for switching between different savepatterns
 /**
  * @author broerkens
  */
 public class XMLPersistenceMappingSaveImpl extends XMLSaveImpl {
 	XMLPersistenceMappingExtendedMetaData xmlPersistenceMappingExtendedMetaData = null;
 	static final String TRUE = "true"; //$NON-NLS-1$
 	Map<String, String> redefinedNamespaceUriToPrefixMap = null;
 
 	final StringBuffer buffer = new StringBuffer();
 
 	public enum SerializationType {
 		attributesOnly, elementsOnly, attributesAndElements
 	}
 
 	public XMLPersistenceMappingSaveImpl(XMLHelper helper) {
 		super(helper);
 	}
 
 	@Override
 	protected boolean saveFeatures(EObject o, boolean attributesOnly) {
 		if (attributesOnly) {
 			return saveFeatures(o, SerializationType.attributesOnly, false);
 		} else {
 			return saveFeatures(o, SerializationType.attributesAndElements, false);
 		}
 	}
 
 	protected boolean saveFeatures(EObject o, SerializationType serializationType, boolean suppressClosingElement) {
 		EClass eClass = o.eClass();
 		int contentKind = extendedMetaData.getContentKind(eClass);
 		if (!toDOM) {
 			switch (contentKind) {
 			case ExtendedMetaData.MIXED_CONTENT:
 			case ExtendedMetaData.SIMPLE_CONTENT: {
 				doc.setMixed(true);
 				break;
 			}
 			}
 		}
 
 		if (o == root) {
 			writeTopAttributes(root);
 		}
 
 		EStructuralFeature[] features = featureTable.getFeatures(eClass);
 		int[] featureKinds = featureTable.getKinds(eClass, features);
 		int[] elementFeatures = null;
 		int elementCount = 0;
 
 		String content = null;
 
 		// Process XML attributes
 		if (SerializationType.elementsOnly == serializationType) {
 			LOOP: for (int i = 0; i < features.length; i++) {
 				int kind = featureKinds[i];
 				EStructuralFeature f = features[i];
 
 				if (kind != TRANSIENT && shouldSaveFeature(o, f)) {
 					switch (kind) {
 					case DATATYPE_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getDataTypeElementSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_SINGLE: {
 						continue LOOP; // next feature, no element required
 					}
 					case DATATYPE_SINGLE_NILLABLE: {
 						if (!isNil(o, f)) {
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ATTRIBUTE_SINGLE: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_MANY: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_SINGLE: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_MANY: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_HREF_SINGLE_UNSETTABLE: {
 						if (isNil(o, f)) {
 							break;
 						}
 						// it's intentional to keep going
 					}
 					case OBJECT_HREF_SINGLE: {
 						if (useEncodedAttributeStyle) {
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocSingle(o, f)) {
 							case SAME_DOC: {
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_HREF_MANY_UNSETTABLE: {
 						if (isEmpty(o, f) && !isXMLPersistenceMappingEnabled(f)) {
 							continue LOOP; // next feature, no element required
 						}
 						// It's intentional to keep going.
 					}
 					case OBJECT_HREF_MANY: {
 						if (useEncodedAttributeStyle) {
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocMany(o, f)) {
 							case SAME_DOC: {
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_IDREF_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_ATTRIBUTE_MANY: {
 						break;
 					}
 					case OBJECT_CONTAIN_MANY_UNSETTABLE:
 					case DATATYPE_MANY: {
 						if (isEmpty(o, f) && !isXMLPersistenceMappingEnabled(f)) {
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_CONTAIN_SINGLE_UNSETTABLE:
 					case OBJECT_CONTAIN_SINGLE:
 					case OBJECT_CONTAIN_MANY:
 					case ELEMENT_FEATURE_MAP: {
 						break;
 					}
 					case ATTRIBUTE_FEATURE_MAP: {
 						continue LOOP; // next feature, no element required
 					}
 					default: {
 						continue LOOP; // next feature, no element required
 					}
 
 					} // end switch
 
 					// We only get here if we should do this.
 					//
 					if (elementFeatures == null) {
 						elementFeatures = new int[features.length];
 					}
 					elementFeatures[elementCount++] = i;
 				} // end if
 			} // end for
 		} else {
 
 			LOOP: for (int i = 0; i < features.length; i++) {
 				int kind = featureKinds[i];
 				EStructuralFeature f = features[i];
 
 				if (kind != TRANSIENT && shouldSaveFeature(o, f)) {
 
 					switch (kind) {
 					case DATATYPE_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getDataTypeElementSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_SINGLE: {
 						saveDataTypeSingle(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case DATATYPE_SINGLE_NILLABLE: {
 						if (!isNil(o, f)) {
 							saveDataTypeSingle(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ATTRIBUTE_SINGLE: {
 						saveEObjectSingle(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_MANY: {
 						saveEObjectMany(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_SINGLE: {
 						saveIDRefSingle(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_MANY: {
 						saveIDRefMany(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_HREF_SINGLE_UNSETTABLE: {
 						if (isNil(o, f)) {
 							break;
 						}
 						// it's intentional to keep going
 					}
 					case OBJECT_HREF_SINGLE: {
 						if (useEncodedAttributeStyle) {
 							saveEObjectSingle(o, f);
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocSingle(o, f)) {
 							case SAME_DOC: {
 								saveIDRefSingle(o, f);
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_HREF_MANY_UNSETTABLE: {
 						if (isEmpty(o, f) && !isXMLPersistenceMappingEnabled(f)) {
 							saveManyEmpty(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						// It's intentional to keep going.
 					}
 					case OBJECT_HREF_MANY: {
 						if (useEncodedAttributeStyle) {
 							saveEObjectMany(o, f);
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocMany(o, f)) {
 							case SAME_DOC: {
 								saveIDRefMany(o, f);
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_IDREF_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_ATTRIBUTE_MANY: {
 						break;
 					}
 					case OBJECT_CONTAIN_MANY_UNSETTABLE:
 					case DATATYPE_MANY: {
 						if (isEmpty(o, f) && !isXMLPersistenceMappingEnabled(f)) {
 							saveManyEmpty(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_CONTAIN_SINGLE_UNSETTABLE:
 					case OBJECT_CONTAIN_SINGLE:
 					case OBJECT_CONTAIN_MANY:
 					case ELEMENT_FEATURE_MAP: {
 						break;
 					}
 					case ATTRIBUTE_FEATURE_MAP: {
 						saveAttributeFeatureMap(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					default: {
 						continue LOOP; // next feature, no element required
 					}
 					}
 
 					if (SerializationType.attributesOnly == serializationType) {
 						continue LOOP; // next feature, no element required
 					}
 
 					// We only get here if we should do this.
 					//
 					if (elementFeatures == null) {
 						elementFeatures = new int[features.length];
 					}
 					elementFeatures[elementCount++] = i;
 				}
 			}
 			processAttributeExtensions(o);
 
 			if (elementFeatures == null) {
 				if (content == null) {
 					content = getContent(o, features);
 				}
 
 				if (content == null) {
 					if (o == root && writeTopElements(root)) {
 						endSaveFeatures(o, 0, null);
 						return true;
 					} else {
 						endSaveFeatures(o, EMPTY_ELEMENT, null);
 						return false;
 					}
 				} else {
 					endSaveFeatures(o, CONTENT_ELEMENT, content);
 					return true;
 				}
 			}
 		}
 
 		if (o == root) {
 			writeTopElements(root);
 		}
 
 		// Process XML elements
 		for (int i = 0; i < elementCount; i++) {
 			int kind = featureKinds[elementFeatures[i]];
 			EStructuralFeature f = features[elementFeatures[i]];
 			switch (kind) {
 			case DATATYPE_SINGLE_NILLABLE: {
 				saveNil(o, f);
 				break;
 			}
 			case ELEMENT_FEATURE_MAP: {
 				saveElementFeatureMap(o, f);
 				break;
 			}
 			case DATATYPE_MANY: {
 				saveDataTypeMany(o, f);
 				break;
 			}
 			case DATATYPE_ATTRIBUTE_MANY: {
 				saveDataTypeAttributeMany(o, f);
 				break;
 			}
 			case DATATYPE_ELEMENT_SINGLE: {
 				saveDataTypeElementSingle(o, f);
 				break;
 			}
 			case OBJECT_CONTAIN_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_CONTAIN_SINGLE: {
 				saveContainedSingle(o, f);
 				break;
 			}
 			case OBJECT_CONTAIN_MANY_UNSETTABLE:
 			case OBJECT_CONTAIN_MANY: {
 				saveContainedMany(o, f);
 				break;
 			}
 			case OBJECT_HREF_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_HREF_SINGLE: {
 				saveHRefSingle(o, f);
 				break;
 			}
 			case OBJECT_HREF_MANY_UNSETTABLE:
 			case OBJECT_HREF_MANY: {
 				saveHRefMany(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_ELEMENT_SINGLE: {
 				saveElementReferenceSingle(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_MANY: {
 				saveElementReferenceMany(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_IDREF_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_ELEMENT_IDREF_SINGLE: {
 				saveElementIDRefSingle(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_IDREF_MANY: {
 				saveElementIDRefMany(o, f);
 				break;
 			}
 			} // end switch
 		} // end for
 		if (!suppressClosingElement) {
 			endSaveFeatures(o, 0, null);
 		}
 		return true;
 	}
 
 	@Override
 	protected void saveNil(EStructuralFeature f) {
 		if (isXMLPersistenceMappingEnabled(f)) {
 			int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(f);
 			switch (featureSerializationStructure) {
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 				// no means for describing null values
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0001__CLASSIFIER_ELEMENT:
 				doc.saveNilElement(getClassifierQName(f.getEType(), f));
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0010__CLASSIFIER_WRAPPER_ELEMENT:
 				doc.saveNilElement(getClassifierWrapperQName(f.getEType()));
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				doc.startElement(getClassifierWrapperQName(f.getEType()));
 				doc.endEmptyElement();
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 				super.saveNil(f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				doc.startElement(getFeatureQName(f));
 				doc.endEmptyElement();
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1000__FEATURE_WRAPPER_ELEMENT:
 				doc.saveNilElement(getFeatureWrapperQName(f));
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				doc.startElement(getFeatureWrapperQName(f));
 				doc.endEmptyElement();
 				break;
 			default:
 				super.saveNil(f);
 			}
 
 		} else {
 			super.saveNil(f);
 		}
 
 	}
 
 	@Override
 	protected void saveElementReferenceSingle(EObject o, EStructuralFeature f) {
 		assert null != helper.getValue(o, f);
 
 		EObject remote = (EObject) helper.getValue(o, f);
 		if (null != remote) {
 
 			int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(f);
 
 			switch (featureSerializationStructure) {
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 				// not allowed - ignore;
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 				// default EMF mapping
 				saveReferenced0100Single(remote, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 				saveReferenced0101Single(remote, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveReferenced1001Single(remote, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 				// if undefined, use the standard EMF mechanism
 				super.saveElementReferenceSingle(o, f);
 				break;
 			default:
 				saveReferenced1001Single(remote, f);
 				break;
 			}
 		}
 
 	}
 
 	protected void saveReferencedHREF(EStructuralFeature f, EObject remote, String qname, boolean doSaveType) {
 		{
 			String href = helper.getHREF(remote);
 			if (href != null) {
 				href = convertURI(href);
 				EClass eClass = remote.eClass();
 				EClass expectedType = (EClass) f.getEType();
 				boolean shouldSaveType = doSaveType && (saveTypeInfo ? xmlTypeInfo.shouldSaveType(eClass, expectedType, f) : eClass != expectedType);
 				doc.startElement(qname);
 				if (shouldSaveType) {
 					saveTypeAttribute(eClass);
 				}
 				doc.endContentElement(href);
 			}
 		}
 	}
 
 	protected void saveReferenced0100Single(EObject remote, EStructuralFeature f) {
 		String qname = getFeatureQName(f);
 		saveReferencedHREF(f, remote, qname, true);
 	}
 
 	protected void saveReferenced0101Single(EObject remote, EStructuralFeature f) {
 		doc.startElement(getFeatureQName(f));
 		String qname = getClassifierQName(remote.eClass(), f);
 		saveReferencedHREF(f, remote, qname, false);
 		doc.endElement();
 	}
 
 	protected void saveReferenced1001Single(EObject remote, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		String qname = getClassifierQName(remote.eClass(), f);
 		saveReferencedHREF(f, remote, qname, false);
 		doc.endElement();
 	}
 
 	@Override
 	protected void saveElementReferenceMany(EObject o, EStructuralFeature f) {
 		assert f.isMany() : "saveElementReferenceMany is applicable for references with true == isMany()"; //$NON-NLS-1$
 		@SuppressWarnings("unchecked")
 		InternalEList<? extends EObject> values = (InternalEList<? extends EObject>) helper.getValue(o, f);
 		int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(f);
 
 		switch (featureSerializationStructure) {
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 			// not allowed - ignore;
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 			// default EMF mapping
 			saveReferenced0100Many(values, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 			saveReferenced0101Many(values, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			saveReferenced1001Many(values, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 			// if undefined, use the standard EMF mechanism
 			super.saveHRefMany(o, f);
 			break;
 		default:
 			saveReferenced1001Many(values, f);
 			break;
 		}
 
 	}
 
 	protected void saveReferenced0100Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		int size = values.size();
 		String qname = getFeatureQName(f);
 		for (int i = 0; i < size; i++) {
 			saveReferencedHREF(f, values.basicGet(i), qname, true);
 		}
 	}
 
 	protected void saveReferenced0101Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		int size = values.size();
 		String qname;
 		EObject value;
 		for (int i = 0; i < size; i++) {
 			doc.startElement(getFeatureQName(f));
 			value = values.basicGet(i);
 			qname = getClassifierQName(value.eClass(), f);
 			saveReferencedHREF(f, value, qname, false);
 			doc.endElement();
 		}
 
 	}
 
 	protected void saveReferenced1001Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		int size = values.size();
 		String qname;
 		EObject value;
 		doc.startElement(getFeatureWrapperQName(f));
 		for (int i = 0; i < size; i++) {
 			value = values.basicGet(i);
 			qname = getClassifierQName(value.eClass(), f);
 			saveReferencedHREF(f, value, qname, false);
 		}
 		doc.endElement();
 	}
 
 	@Override
 	protected void saveDataTypeElementSingle(EObject o, EStructuralFeature f) {
 		assert !f.isMany() : "saveElementReferenceMany is applicable for references with false == isMany()"; //$NON-NLS-1$
 		String svalue = getDatatypeValue(helper.getValue(o, f), f, false);
 		int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(f);
 
 		switch (featureSerializationStructure) {
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 			// not allowed - ignore;
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0001__CLASSIFIER_ELEMENT:
 			saveAttribute0001Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0010__CLASSIFIER_WRAPPER_ELEMENT:
 			saveAttribute0010Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			saveAttribute0011Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 			saveAttribute0100Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 			saveAttribute0101Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 			saveAttribute0110Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			saveAttribute0111Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1000__FEATURE_WRAPPER_ELEMENT:
 			saveAttribute1000Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			saveAttribute1001Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 			saveAttribute1010Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			saveAttribute1011Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
 			saveAttribute1100Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 			saveAttribute1101Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 			saveAttribute1110Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 			saveAttribute1111Single(svalue, f);
 			break;
 		case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 			// if undefined, use the standard EMF mechanism
 			super.saveDataTypeMany(o, f);
 			break;
 		default:
 			saveAttribute1001Single(svalue, f);
 			break;
 		}
 
 	}
 
 	protected void saveAttribute0001Single(String value, EStructuralFeature f) {
 		String name = getClassifierQName(f.getEType());
 		if (value == null) {
 			doc.startElement(name);
 			doc.addAttribute(XSI_NIL, TRUE);
 			doc.endEmptyElement();
 			declareXSI = true;
 		} else {
 			doc.saveDataValueElement(name, value);
 		}
 
 	}
 
 	protected void saveAttribute0010Single(String value, EStructuralFeature f) {
 		String name = getClassifierWrapperQName(f.getEType());
 		if (value == null) {
 			doc.startElement(name);
 			doc.addAttribute(XSI_NIL, TRUE);
 			doc.endEmptyElement();
 			declareXSI = true;
 		} else {
 			doc.saveDataValueElement(name, value);
 		}
 	}
 
 	protected void saveAttribute0011Single(String value, EStructuralFeature f) {
 		doc.startElement(getClassifierWrapperQName(f.getEType()));
 		saveAttribute0001Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute0100Single(String value, EStructuralFeature f) {
 		// this is the default EMF behaviour
 		String name = getFeatureQName(f);
 		if (value == null) {
 			doc.startElement(name);
 			doc.addAttribute(XSI_NIL, TRUE);
 			doc.endEmptyElement();
 			declareXSI = true;
 		} else {
 			doc.saveDataValueElement(name, value);
 		}
 	}
 
 	protected void saveAttribute0101Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureQName(f));
 		saveAttribute0001Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute0110Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureQName(f));
 		saveAttribute0010Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute0111Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureQName(f));
 		doc.startElement(getClassifierWrapperQName(f.getEType()));
 		saveAttribute0001Single(value, f);
 		doc.endElement();
 		doc.endElement();
 	}
 
 	protected void saveAttribute1000Single(String value, EStructuralFeature f) {
 		String name = getFeatureWrapperQName(f);
 		if (value == null) {
 			doc.startElement(name);
 			doc.addAttribute(XSI_NIL, TRUE);
 			doc.endEmptyElement();
 			declareXSI = true;
 		} else {
 			doc.saveDataValueElement(name, value);
 		}
 	}
 
 	protected void saveAttribute1001Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0001Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1010Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0010Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1011Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		doc.startElement(getClassifierWrapperQName(f.getEType()));
 		saveAttribute0001Single(value, f);
 		doc.endElement();
 		doc.endElement();
 	}
 
 	protected void saveAttribute1100Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0100Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1101Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		doc.startElement(getFeatureQName(f));
 		saveAttribute0001Single(value, f);
 		doc.endElement();
 		doc.endElement();
 	}
 
 	protected void saveAttribute1110Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		doc.startElement(getFeatureQName(f));
 		saveAttribute0010Single(value, f);
 		doc.endElement();
 		doc.endElement();
 	}
 
 	protected void saveAttribute1111Single(String value, EStructuralFeature f) {
 		doc.startElement(getFeatureWrapperQName(f));
 		doc.startElement(getFeatureQName(f));
 		doc.startElement(getClassifierWrapperQName(f.getEType()));
 		saveAttribute0001Single(value, f);
 		doc.endElement();
 		doc.endElement();
 		doc.endElement();
 	}
 
 	@Override
 	protected void saveDataTypeMany(EObject o, EStructuralFeature f) {
 		assert f.isMany() : "saveElementReferenceMany is applicable for references with true == isMany()"; //$NON-NLS-1$
 
 		@SuppressWarnings("unchecked")
 		InternalEList<? extends EObject> values = (InternalEList<? extends EObject>) helper.getValue(o, f);
 		if (null != values && !values.isEmpty()) {
 			int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(f);
 
 			switch (featureSerializationStructure) {
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 				// not allowed - ignore;
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0001__CLASSIFIER_ELEMENT:
 				saveAttribute0001Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0010__CLASSIFIER_WRAPPER_ELEMENT:
 				saveAttribute0010Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveAttribute0011Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 				saveAttribute0100Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 				saveAttribute0101Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 				saveAttribute0110Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveAttribute0111Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1000__FEATURE_WRAPPER_ELEMENT:
 				saveAttribute1000Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveAttribute1001Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 				saveAttribute1010Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveAttribute1011Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
 				saveAttribute1100Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 				saveAttribute1101Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 				saveAttribute1110Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveAttribute1111Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 				// if undefined, use the standard EMF mechanism
 				saveAttribute0100Many(values, f);
 				break;
 			default:
 				saveAttribute1001Many(values, f);
 				break;
 			}
 		}
 	}
 
 	protected String getAttributeString(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		EDataType d = (EDataType) f.getEType();
 		EPackage ePackage = d.getEPackage();
 		EFactory fac = ePackage.getEFactoryInstance();
 		buffer.setLength(0);
 		for (Iterator<? extends EObject> i = values.basicIterator();;) {
 			Object value = i.next();
 			String svalue = helper.convertToString(fac, d, value);
 			if (escape != null) {
 				svalue = escape.convertText(svalue);
 			}
 			buffer.append(svalue);
 			if (i.hasNext()) {
 				buffer.append(' ');
 			} else {
 				break;
 			}
 		}
 		return buffer.toString();
 	}
 
 	protected void saveAttribute0001Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		EDataType d = (EDataType) f.getEType();
 		EPackage ePackage = d.getEPackage();
 		EFactory fac = ePackage.getEFactoryInstance();
 		String name = helper.getQName(d);
 		int size = values.size();
 		for (int i = 0; i < size; ++i) {
 			Object value = values.get(i);
 			if (value == null) {
 				doc.startElement(name);
 				doc.addAttribute(XSI_NIL, TRUE);
 				doc.endEmptyElement();
 				declareXSI = true;
 			} else {
 				String svalue = helper.convertToString(fac, d, value);
 				if (escape != null) {
 					svalue = escape.convertText(svalue);
 				}
 				doc.saveDataValueElement(name, svalue);
 			}
 		}
 	}
 
 	protected void saveAttribute0010Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		String name = getClassifierWrapperQName(f.getEType());
 		doc.saveDataValueElement(name, getAttributeString(values, f));
 	}
 
 	protected void saveAttribute0011Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getClassifierWrapperQName(f.getEType()));
 		saveAttribute0001Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute0100Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		// this is the default EMF behaviour
 		assert null != values;
 
 		EDataType d = (EDataType) f.getEType();
 		EPackage ePackage = d.getEPackage();
 		EFactory fac = ePackage.getEFactoryInstance();
 		String name = helper.getQName(f);
 		int size = values.size();
 		for (int i = 0; i < size; ++i) {
 			Object value = values.get(i);
 			if (value == null) {
 				doc.startElement(name);
 				doc.addAttribute(XSI_NIL, TRUE);
 				doc.endEmptyElement();
 				declareXSI = true;
 			} else {
 				String svalue = helper.convertToString(fac, d, value);
 				if (escape != null) {
 					svalue = escape.convertText(svalue);
 				}
 				doc.saveDataValueElement(name, svalue);
 			}
 		}
 	}
 
 	protected void saveAttribute0101Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		EDataType d = (EDataType) f.getEType();
 		EPackage ePackage = d.getEPackage();
 		EFactory fac = ePackage.getEFactoryInstance();
 		String name = getClassifierQName(d);
 		int size = values.size();
 		for (int i = 0; i < size; ++i) {
 			Object value = values.get(i);
 			doc.startElement(getFeatureQName(f));
 			if (value == null) {
 				doc.startElement(name);
 				doc.addAttribute(XSI_NIL, TRUE);
 				doc.endEmptyElement();
 				declareXSI = true;
 			} else {
 				String svalue = helper.convertToString(fac, d, value);
 				if (escape != null) {
 					svalue = escape.convertText(svalue);
 				}
 				doc.saveDataValueElement(name, svalue);
 			}
 			doc.endElement();
 		}
 	}
 
 	protected void saveAttribute0110Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureQName(f));
 		saveAttribute0010Many(values, f);
 		doc.endElement();
 
 	}
 
 	protected void saveAttribute0111Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureQName(f));
 		saveAttribute0011Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1000Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		String name = getFeatureWrapperQName(f);
 		doc.saveDataValueElement(name, getAttributeString(values, f));
 	}
 
 	protected void saveAttribute1001Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0001Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1010Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0010Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1011Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0011Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1100Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0100Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1101Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0101Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1110Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0110Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveAttribute1111Many(InternalEList<? extends EObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveAttribute0111Many(values, f);
 		doc.endElement();
 	}
 
 	@Override
 	protected void saveContainedMany(EObject o, EStructuralFeature f) {
 		assert f.isMany();
 
 		// XML Mapping serialization enabled
 		@SuppressWarnings("unchecked")
 		List<? extends InternalEObject> values = ((InternalEList<? extends InternalEObject>) helper.getValue(o, f)).basicList();
 		if (null != values) {
 			int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(f);
 
 			switch (featureSerializationStructure) {
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 				saveContained0000Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0001__CLASSIFIER_ELEMENT:
 				saveContained0001Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0010__CLASSIFIER_WRAPPER_ELEMENT:
 				saveContained0010Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveContained0011Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 				saveContained0100Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 				saveContained0101Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 				saveContained0110Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveContained0111Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1000__FEATURE_WRAPPER_ELEMENT:
 				saveContained1000Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveContained1001Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 				saveContained1010Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveContained1011Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
 				saveContained1100Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 				saveContained1101Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 				saveContained1110Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 				saveContained1111Many(values, f);
 				break;
 			case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 				// if undefined, use the standard EMF mechanism
 				saveContained0100Many(values, f);
 				break;
 			default:
 				saveContained1001Many(values, f);
 				break;
 			}
 
 		}
 
 	}
 
 	protected void saveContained0000Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		for (EObject value : values) {
 			saveFeatures(value, SerializationType.elementsOnly, true);
 		}
 	}
 
 	protected void saveContained0001Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		for (EObject value : values) {
 			doc.startElement(getClassifierQName(value.eClass()));
 			saveFeatures(value);
 		}
 	}
 
 	protected void saveContained0010Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					saveFeatures(value, SerializationType.elementsOnly, true);
 				}
 			}
 			doc.endElement();
 		}
 	}
 
 	protected void saveContained0011Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					doc.startElement(getClassifierQName(value.eClass()));
 					saveFeatures(value, SerializationType.attributesAndElements, false);
 				}
 			}
 			doc.endElement();
 		}
 	}
 
 	protected void saveContained0100Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		// this is the default EMF behaviour
 		assert null != values;
 		assert !values.isEmpty();
 
 		for (EObject value : values) {
 			saveElement(value, f);
 		}
 	}
 
 	protected void saveContained0101Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		for (EObject value : values) {
 			doc.startElement(getFeatureQName(f));
 			doc.startElement(getClassifierQName(value.eClass()));
 			saveFeatures(value, SerializationType.attributesAndElements, false);
 			doc.endElement();
 		}
 	}
 
 	protected void saveContained0110Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureQName(f));
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					saveFeatures(value, SerializationType.elementsOnly, true);
 				}
 			}
 			doc.endElement();
 		}
 		doc.endElement();
 
 	}
 
 	protected void saveContained0111Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureQName(f));
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					doc.startElement(getClassifierQName(value.eClass()));
 					saveFeatures(value);
 				}
 			}
 			doc.endElement();
 		}
 		doc.endElement();
 	}
 
 	protected void saveContained1000Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0000Many(values, f);
 		doc.endElement();
 		// last end is written by caller
 	}
 
 	protected void saveContained1001Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0001Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1010Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0010Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1011Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0011Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1100Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0100Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1101Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0101Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1110Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0110Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1111Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0111Many(values, f);
 		doc.endElement();
 	}
 
 	@Override
 	protected void saveContainedSingle(EObject o, EStructuralFeature f) {
 		assert !f.isMany();
 
 		if (isXMLPersistenceMappingEnabled(f)) {
 			// XML Mapping serialization enabled
 			EObject value = (EObject) helper.getValue(o, f);
 			if (null != value) {
 				int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(f);
 
 				switch (featureSerializationStructure) {
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 					saveContained0000Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0001__CLASSIFIER_ELEMENT:
 					saveContained0001Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0010__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContained0010Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContained0011Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 					saveContained0100Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContained0101Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContained0110Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContained0111Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1000__FEATURE_WRAPPER_ELEMENT:
 					saveContained1000Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContained1001Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContained1010Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContained1011Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
 					saveContained1100Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContained1101Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContained1110Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContained1111Single(value, f);
 					break;
 				case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 					// if undefined, use the standard EMF mechanism
 					super.saveContainedSingle(o, f);
 					break;
 				default:
 					saveContained1001Single(value, f);
 					break;
 				}
 
 			}
 
 		} else {
 			super.saveContainedSingle(o, f);
 		}
 	}
 
 	protected void saveContained0000Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		saveFeatures(value, SerializationType.elementsOnly, true);
 	}
 
 	protected void saveContained0001Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getClassifierQName(value.eClass()));
 		saveFeatures(value);
 
 	}
 
 	protected void saveContained0010Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		EClass eClass = value.eClass();
 		doc.startElement(getClassifierWrapperQName(eClass));
 		saveFeatures(value);
 	}
 
 	protected void saveContained0011Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		EClass eClass = value.eClass();
 		doc.startElement(getClassifierWrapperQName(eClass));
 		doc.startElement(getClassifierQName(eClass));
 		saveFeatures(value);
 		doc.endElement();
 
 	}
 
 	protected void saveContained0100Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		saveElement(value, f);
 	}
 
 	protected void saveContained0101Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		EClass eClass = value.eClass();
 		doc.startElement(getFeatureQName(f));
 		doc.startElement(getClassifierQName(eClass));
 		saveFeatures(value);
 		doc.endElement();
 	}
 
 	protected void saveContained0110Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		EClass eClass = value.eClass();
 		doc.startElement(getFeatureQName(f));
 		doc.startElement(getClassifierWrapperQName(eClass));
 		saveFeatures(value);
 		doc.endElement();
 
 	}
 
 	protected void saveContained0111Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		EClass eClass = value.eClass();
 		doc.startElement(getFeatureQName(f));
 		doc.startElement(getClassifierWrapperQName(eClass));
 		doc.startElement(getClassifierQName(eClass));
 		saveFeatures(value);
 		doc.endElement();
 		doc.endElement();
 	}
 
 	protected void saveContained1000Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0000Single(value, f);
 		doc.endElement();
 		// last end is written by caller
 	}
 
 	protected void saveContained1001Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0001Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1010Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0010Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1011Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0011Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1100Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0100Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1101Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0101Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1110Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0110Single(value, f);
 		doc.endElement();
 	}
 
 	protected void saveContained1111Single(EObject value, EStructuralFeature f) {
 		assert null != value;
 		assert !f.isMany();
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContained0111Single(value, f);
 		doc.endElement();
 	}
 
 	protected String getFeatureQName(EStructuralFeature eStructuralFeature) {
 		String featureXMLName = xmlPersistenceMappingExtendedMetaData.getXMLName(eStructuralFeature);
 		EPackage ePackage = eStructuralFeature.getEContainingClass().getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, featureXMLName);
 	}
 
 	protected String getFeatureWrapperQName(EStructuralFeature eStructuralFeature) {
 		String featureWrapperXMLName = xmlPersistenceMappingExtendedMetaData.getXMLWrapperName(eStructuralFeature);
 		EPackage ePackage = eStructuralFeature.getEContainingClass().getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, featureWrapperXMLName);
 	}
 
 	protected String getClassifierQName(EClassifier eClassifier) {
 		String classifierXMLName = xmlPersistenceMappingExtendedMetaData.getXMLName(eClassifier);
 		EPackage ePackage = eClassifier.getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, classifierXMLName);
 	}
 
 	protected String getClassifierQName(EClassifier eClassifier, EStructuralFeature eStructuralFeature) {
 		String classifierXMLName = xmlPersistenceMappingExtendedMetaData.getXMLName(eClassifier, eStructuralFeature);
 		EPackage ePackage = eClassifier.getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, classifierXMLName);
 	}
 
 	protected String getClassifierWrapperQName(EClassifier eClassifier) {
 		String classifierWrapperXMLName = xmlPersistenceMappingExtendedMetaData.getXMLWrapperName(eClassifier);
 		EPackage ePackage = eClassifier.getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, classifierWrapperXMLName);
 	}
 
 	protected String getQName(String prefix, String name) {
 		if (null == prefix || 0 == prefix.length()) {
 			return name;
 		} else {
 			return prefix + ":" + name; //$NON-NLS-1$
 		}
 	}
 
 	@Override
 	protected void init(XMLResource resource, Map<?, ?> options) {
 		super.init(resource, options);
 
 		// set extended meta data
 		Object extendedMetaDataOption = options.get(XMLResource.OPTION_EXTENDED_META_DATA);
 		if (null != extendedMetaDataOption && extendedMetaDataOption instanceof XMLPersistenceMappingExtendedMetaData) {
 			xmlPersistenceMappingExtendedMetaData = (XMLPersistenceMappingExtendedMetaData) options.get(XMLResource.OPTION_EXTENDED_META_DATA);
 			extendedMetaData = xmlPersistenceMappingExtendedMetaData;
 		} else {
 			// default
 			xmlPersistenceMappingExtendedMetaData = xmlResource == null || xmlResource.getResourceSet() == null ? XMLPersistenceMappingExtendedMetaData.INSTANCE
 					: new XMLPersistenceMappingExtendedMetaDataImpl(xmlResource.getResourceSet().getPackageRegistry());
 			extendedMetaData = xmlPersistenceMappingExtendedMetaData;
 		}
 		helper.setExtendedMetaData(xmlPersistenceMappingExtendedMetaData);
 
 		Object namespaceToPrefixMapObject = options.get(XMLPersistenceMappingResource.OPTION_NAMEPSACE_TO_PREFIX_MAP);
 		if (null != namespaceToPrefixMapObject && namespaceToPrefixMapObject instanceof Map<?, ?>) {
 			@SuppressWarnings("unchecked")
 			Map<Object, Object> namespaceToPrefixMap = (Map<Object, Object>) namespaceToPrefixMapObject;
 			EMap<String, String> prefixToNamespaceMap = helper.getPrefixToNamespaceMap();
 			for (Map.Entry<Object, Object> entry : namespaceToPrefixMap.entrySet()) {
 				Object namespace = entry.getKey();
 				Object prefix = entry.getValue();
 				if (namespace instanceof String && prefix instanceof String) {
 					if (prefixToNamespaceMap.contains(prefix)) {
 						// TODO: handle namespace redefinitions
 						if (null == redefinedNamespaceUriToPrefixMap) {
 							redefinedNamespaceUriToPrefixMap = new HashMap<String, String>();
 						}
 						redefinedNamespaceUriToPrefixMap.put((String) namespace, (String) prefix);
 					} else {
 						prefixToNamespaceMap.put((String) prefix, (String) namespace);
 					}
 				}
 			}
 		}
 
 		assert null != xmlPersistenceMappingExtendedMetaData : "xmlPersistenceMappingExtendedMetaData should never be null"; //$NON-NLS-1$
 		assert null != extendedMetaData : "extendedMetaData should never be null"; //$NON-NLS-1$
 	}
 
 	protected Collection<EClass> getOrderedClasses(List<? extends InternalEObject> values) {
 		Set<EClass> classesSet = new LinkedHashSet<EClass>();
 		for (EObject value : values) {
 			classesSet.add(value.eClass());
 		}
 
 		List<EClass> classesList = new ArrayList<EClass>(classesSet);
 		Comparator<EClass> comparator = new Comparator<EClass>() {
 			public int compare(EClass o1, EClass o2) {
 				return o1.getName().compareTo(o2.getName());
 			}
 		};
 
 		Collections.sort(classesList, comparator);
 
 		return classesList;
 	}
 
 	protected boolean isXMLPersistenceMappingEnabled(EStructuralFeature feature) {
 		return xmlPersistenceMappingExtendedMetaData.isXMLPersistenceMappingEnabled(feature);
 	}
 
	@Override
	protected void saveElementID(EObject o) {
		// avoid writing the ID since this is already written as usual attribute
		saveFeatures(o);
	}

 }
