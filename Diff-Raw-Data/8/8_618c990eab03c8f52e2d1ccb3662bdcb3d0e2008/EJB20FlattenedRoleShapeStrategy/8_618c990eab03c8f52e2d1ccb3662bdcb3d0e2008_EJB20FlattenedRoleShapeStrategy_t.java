 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.ejb.internal.impl;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jem.java.JavaHelpers;
 import org.eclipse.jst.j2ee.ejb.CMPAttribute;
 import org.eclipse.jst.j2ee.ejb.CommonRelationshipRole;
 import org.eclipse.jst.j2ee.ejb.ContainerManagedEntity;
 import org.eclipse.jst.j2ee.ejb.EjbPackage;
 import org.eclipse.jst.j2ee.ejb.IRoleShapeStrategy;
 
 /**
  * Insert the type's description here.
  * Creation date: (11/6/2000 11:41:33 AM)
  * @author: Administrator
  */
 public class EJB20FlattenedRoleShapeStrategy extends RoleShapeStrategy {
 	/**
 	 * FlattenedRoleShapeStrategy constructor comment.
 	 */
 	public EJB20FlattenedRoleShapeStrategy(CommonRelationshipRole aRole) {
 		super(aRole);
 	}
 	protected boolean canContinue() {
 		return !busy && role.getName() != null && (role.isForward() ||
 			(role.isMany() && (role.getOppositeAsCommonRole() == null ||
 			role.getOppositeAsCommonRole().isMany())));
 	}
 	
 	public boolean usesAttributeNamed(String attributeName) {
 		if (attributeName != null) {
 			int index = attributeName.indexOf(IRoleShapeStrategy.ATTRIBUTE_NAME_JOINER);
 			String name = attributeName.substring(0, index);
 			return getRole().getName().equals(name);
 		}
 		return false;
 	}
 
 	/**
 	 * @see RoleShapeStrategy#reconcileAttributes(CommonRelationshipRole, String, List, List)
 	 */
 	protected void reconcileAttributes(CommonRelationshipRole aRole, String attributeName, List aList, List computedNames) {
 		
 		Resource res = aRole.eResource();
 		boolean dirtyFlag = res != null ? res.isModified() : false;
 		try {		
 			if (aRole.getOppositeAsCommonRole() != null) {
 				ContainerManagedEntity entity = aRole.getOppositeAsCommonRole().getSourceEntity();
 		//	ContainerManagedEntityExtension roleType = (ContainerManagedEntityExtension)getTypeExtension(aRole);
 			if (entity != null)
 				collectAttributes(entity, attributeName, aList, computedNames);
 		}
 		} finally {
 			if (res != null)
 				res.setModified(dirtyFlag);
 		}
 	}
 
 	/**
 	 * @see com.ibm.ejs.models.base.extensions.ejbext.impl.FlattenedRoleShapeStrategy#setDerivedAttributeType(CMPAttribute, JavaHelpers)
 	 */
 	protected void setDerivedAttributeType(CMPAttribute attribute, CMPAttribute targetAttribute, boolean isNew) {
 		if (targetAttribute == null) return;
 		JavaHelpers fieldType = targetAttribute.getType();
 		if (fieldType != null) {
 			int singleRoleNameSize = getRole().getName().length() + targetAttribute.getName().length() + 1; //one for the underscore
 			if (!(attribute.getName().length() > singleRoleNameSize)) //don't set the originating type since we are derived from another derived attribute
 				attribute.setOriginatingType(fieldType);
 			attribute.setEType(fieldType.getWrapper());
 		}
 		if (isNew && !getRole().isKey())
 			attribute.setDerived(true);
 	}
 	/**
 	 * getFields method comment.
 	 */
 	protected void collectAttributes(ContainerManagedEntity type, String attributeName, List aList, List computedNames) {
 		collectKeyModelledAttributes(type, attributeName, aList, computedNames);
 		collectKeyRoleAttributes(type, attributeName, aList, computedNames);
 	}
 	/**
 	 * 
 	 */
	protected void collectAttributes(ContainerManagedEntity entity, CMPAttribute type, String attributeName, List aList, List computedNames) {
 		boolean isNewAttribute = false;
 		attributeName = appendName(attributeName, type.getName());
 		computedNames.add(attributeName);
 		CMPAttribute attribute = getCMPEntity().getPersistentAttribute(attributeName);
 		if (attribute == null) {
 			attribute = createPersistentAttribute(attributeName);
 			isNewAttribute = true;
 		}
 		//This is necessary for code generation
		//ContainerManagedEntity entity = (ContainerManagedEntity) type.eContainer();
 		if (entity != null)
 			setDerivedAttributeType(attribute, type, isNewAttribute);
 		if (isNewAttribute) {
 			if (getRole().isKey())
 				getCMPEntity().getKeyAttributes().add(attribute);
 			aList.add(attribute);
 			getCMPEntity().getPersistentAttributes().add(attribute);
 		} else {
 			if (!aList.contains(attribute))
 				aList.add(attribute);
 		}
 	}
 	protected CMPAttribute createPersistentAttribute(String aName) {
 		CMPAttribute attribute = ((EjbPackage)EPackage.Registry.INSTANCE.getEPackage(EjbPackage.eNS_URI)).getEjbFactory().createCMPAttribute();
 		attribute.setName(aName);
 		attribute.setDescription("Generated to support relationships.  Do NOT delete.");
 		return attribute;
 	}
 	/**
 	 * getFields method comment.
 	 */
 	protected void collectKeyModelledAttributes(ContainerManagedEntity entity, String attributeName, List aList, List computedNames) {
 		java.util.Iterator it = entity.getFilteredFeatures(ModelledKeyAttributeFilter.singleton()).iterator();
 		CMPAttribute attribute;
 		while (it.hasNext()) {
 			attribute = (CMPAttribute) it.next();
			collectAttributes(entity, attribute, attributeName, aList, computedNames);
 		}
 	}
 	/**
 	 * getFields method comment.
 	 */
 	protected void collectKeyRoleAttributes(ContainerManagedEntity entity, String attributeName, List aList, List computedNames) {
 		if(visitedKeyTypes.contains(entity)){
 			throw new RuntimeException("Key role cycle detected");
 		}
 		visitedKeyTypes.add(entity);
 		java.util.Iterator it = entity.getFilteredFeatures(KeyRelationshipRoleFilter.singleton()).iterator();
 		CommonRelationshipRole aRole;
 		while (it.hasNext()) {
 			aRole = (CommonRelationshipRole) it.next();
 			String attName = appendName(attributeName, aRole.getName());
 			reconcileAttributes(aRole, attName, aList, computedNames);
 		}
 	}
 	protected ContainerManagedEntity getCMPEntity() {
 		return getRole().getSourceEntity();
 	}
 
 
 }
