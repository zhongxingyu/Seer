 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: EAttributeORMAnnotator.java,v 1.19 2011/08/26 05:34:12 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.orm.annotator;
 
 import java.sql.Timestamp;
 import java.util.Date;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
 import org.eclipse.emf.texo.generator.Annotator;
 import org.eclipse.emf.texo.generator.GeneratorUtils;
 import org.eclipse.emf.texo.modelgenerator.annotator.GenUtils;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EAttributeModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.ModelcodegeneratorPackage;
 import org.eclipse.emf.texo.orm.annotations.model.orm.AccessType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Basic;
 import org.eclipse.emf.texo.orm.annotations.model.orm.CollectionTable;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Column;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ElementCollection;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Entity;
 import org.eclipse.emf.texo.orm.annotations.model.orm.EnumType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Id;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Lob;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ManyToOne;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OneToMany;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrderColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrmFactory;
 import org.eclipse.emf.texo.orm.annotations.model.orm.TemporalType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.UniqueConstraint;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Version;
 import org.eclipse.emf.texo.orm.ormannotations.EAttributeORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.EClassORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.EDataTypeORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.EEnumORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.EPackageORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.OrmannotationsPackage;
 
 /**
  * Responsible for creating the {@link EAttributeORMAnnotation}.
  * 
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.19 $
  */
 
 public class EAttributeORMAnnotator extends EStructuralFeatureORMAnnotator implements
     Annotator<EAttributeORMAnnotation> {
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#annotate(org.eclipse.emf.texo.annotations.annotationsmodel
    * .ENamedElementAnnotation)
    */
   public void setAnnotationFeatures(EAttributeORMAnnotation annotation) {
 
     final EAttribute eAttribute = annotation.getEAttribute();
     final EPackage ePackage = eAttribute.getEContainingClass().getEPackage();
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(ePackage);
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
 
     final EClass eClass = eAttribute.getEContainingClass();
     if (GenUtils.isDocumentRoot(eClass)) {
       return;
     }
 
     final EClassORMAnnotation eClassORMAnnotation = (EClassORMAnnotation) getAnnotationManager().getAnnotation(eClass,
         OrmannotationsPackage.eINSTANCE.getEClassORMAnnotation());
 
     if (eAttribute.getEAttributeType() instanceof EEnum) {
       copyAnnotationsFromEEnum(annotation);
     } else {
       copyAnnotationsFromDataType(annotation);
     }
 
     // don't do anything anymore if transient
     if (annotation.getTransient() != null) {
       if (GeneratorUtils.isEmptyOrNull(annotation.getTransient().getName())) {
         annotation.getTransient().setName(getName(eAttribute));
       }
       return;
     }
 
     if (annotation.getId() != null) {
       final Id id = annotation.getId();
 
       if (GeneratorUtils.isEmptyOrNull(id.getName())) {
         id.setName(getName(eAttribute));
       }
 
       if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
         id.setAccess(AccessType.PROPERTY);
       }
 
       return;
     }
 
     if (annotation.getVersion() != null) {
       final Version version = annotation.getVersion();
 
       if (GeneratorUtils.isEmptyOrNull(version.getName())) {
         version.setName(getName(eAttribute));
       }
 
       if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
         version.setAccess(AccessType.PROPERTY);
       }
 
       return;
     }
 
     final EAttributeModelGenAnnotation eAttributeModelGen = (EAttributeModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eAttribute, ModelcodegeneratorPackage.eINSTANCE.getEAttributeModelGenAnnotation());
     // features which are part of a featuremap are never mapped as many features
     final boolean isPartOfFeatureMap = GeneratorUtils.isPartOfGroup(eAttribute);
 
     if (annotation.getTransient() != null) {
       if (GeneratorUtils.isEmptyOrNull(annotation.getTransient().getName())) {
         annotation.getTransient().setName(eAttributeModelGen.getValidJavaMemberName());
       }
       return;
     }
 
     if (FeatureMapUtil.isFeatureMap(eAttribute)) {
 
       if (annotation.getFeatureMapEntity() == null) {
         annotation.setFeatureMapEntity(OrmFactory.eINSTANCE.createEntity());
       }
 
       final Entity featureMapEntity = annotation.getFeatureMapEntity();
 
       if (GeneratorUtils.isEmptyOrNull(featureMapEntity.getClass_())) {
         featureMapEntity.setClass(eAttributeModelGen.getFeatureMapQualifiedClassName());
       }
       if (GeneratorUtils.isEmptyOrNull(featureMapEntity.getName())) {
         featureMapEntity.setName(namingStrategy.getFeatureMapEntityName(eAttribute));
       }
 
       if (isPartOfFeatureMap && FeatureMapUtil.isFeatureMap(eAttribute)) {
         if (annotation.getManyToOne() == null) {
           annotation.setManyToOne(OrmFactory.eINSTANCE.createManyToOne());
         }
         final ManyToOne ManyToOne = annotation.getManyToOne();
         ManyToOne.setTargetEntity(eAttributeModelGen.getFeatureMapQualifiedClassName());
         ManyToOne.setCascade(OrmFactory.eINSTANCE.createCascadeType());
         ManyToOne.getCascade().setCascadeAll(OrmFactory.eINSTANCE.createEmptyType());
         ManyToOne.setName(getName(eAttribute));
       } else {
         if (annotation.getOneToMany() == null) {
           annotation.setOneToMany(OrmFactory.eINSTANCE.createOneToMany());
         }
         final OneToMany OneToMany = annotation.getOneToMany();
         OneToMany.setTargetEntity(eAttributeModelGen.getFeatureMapQualifiedClassName());
         OneToMany.setCascade(OrmFactory.eINSTANCE.createCascadeType());
         OneToMany.setOrphanRemoval(true);
         OneToMany.getCascade().setCascadeAll(OrmFactory.eINSTANCE.createEmptyType());
         OneToMany.setJoinTable(OrmFactory.eINSTANCE.createJoinTable());
         OneToMany.setName(getName(eAttribute));
       }
 
       return;
     }
 
     final EDataType eDataType = eAttribute.getEAttributeType();
     boolean isLob = false;
     boolean isTime = false;
     boolean isDate = false;
     if (eDataType.getInstanceClass() != null) {
       isLob = eDataType.getInstanceClass().isArray() && eDataType.getInstanceClass().getComponentType() == byte.class;
       isTime = Timestamp.class == eDataType.getInstanceClass() || eDataType == XMLTypePackage.eINSTANCE.getDateTime();
       isDate = eDataType == XMLTypePackage.eINSTANCE.getDate() || eDataType.getInstanceClass() != null
           && Date.class.isAssignableFrom(eDataType.getInstanceClass());
     }
     final boolean isEnum = eDataType instanceof EEnum;
 
     if (!isPartOfFeatureMap && eAttribute.isMany()) {
       final ElementCollection elementCollection;
       if (annotation.getElementCollection() == null) {
         elementCollection = OrmFactory.eINSTANCE.createElementCollection();
         annotation.setElementCollection(elementCollection);
       } else {
         elementCollection = annotation.getElementCollection();
       }
 
       if (isLob) {
         final Lob lob = OrmFactory.eINSTANCE.createLob();
         elementCollection.setLob(lob);
       } else if (isTime) {
         elementCollection.setTemporal(TemporalType.TIMESTAMP);
       } else if (isDate) {
         elementCollection.setTemporal(TemporalType.DATE);
       } else if (doAddConverter(eAttributeModelGen)) {
         elementCollection.setConvert(ORMUtils.OBJECT_CONVERTER_NAME);
       }
 
       if (isEnum && elementCollection.getEnumerated() == null) {
         elementCollection.setEnumerated(EnumType.STRING);
       }
 
       if (elementCollection.getCollectionTable() == null) {
         final CollectionTable collectionTable = OrmFactory.eINSTANCE.createCollectionTable();
         elementCollection.setCollectionTable(collectionTable);
       }
       if (GeneratorUtils.isEmptyOrNull(elementCollection.getCollectionTable().getName())) {
         elementCollection.getCollectionTable().setName(namingStrategy.getJoinTableName(eAttribute));
       }
 
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         if (elementCollection.getColumn() == null) {
           elementCollection.setColumn(OrmFactory.eINSTANCE.createColumn());
         }
         if (elementCollection.getColumn().getName() == null) {
           elementCollection.getColumn().setName(namingStrategy.getCollectionElementColumnName(eAttribute));
         }
 
         if (elementCollection.getCollectionTable().getJoinColumn().isEmpty()) {
           elementCollection.getCollectionTable().getJoinColumn().add(OrmFactory.eINSTANCE.createJoinColumn());
           elementCollection.getCollectionTable().getJoinColumn().get(0)
               .setName(namingStrategy.getJoinColumnName(eAttribute));
         }
         if (eAttribute.isUnique()) {
           final UniqueConstraint uniqueConstraint = OrmFactory.eINSTANCE.createUniqueConstraint();
           uniqueConstraint.getColumnName().add(elementCollection.getColumn().getName());
           uniqueConstraint.getColumnName().add(elementCollection.getCollectionTable().getJoinColumn().get(0).getName());
         }
       }
 
       final EAttributeModelGenAnnotation eAttributeModelGenAnnotation = (EAttributeModelGenAnnotation) getAnnotationManager()
           .getAnnotation(eAttribute, ModelcodegeneratorPackage.eINSTANCE.getEAttributeModelGenAnnotation());
       OrderColumn orderColumn = elementCollection.getOrderColumn();
       if (ePackageORMAnnotation.isAddOrderColumnToListMappings() && eAttributeModelGenAnnotation.isUseList()
           && elementCollection.getOrderBy() == null && elementCollection.getOrderColumn() == null) {
         orderColumn = OrmFactory.eINSTANCE.createOrderColumn();
         elementCollection.setOrderColumn(orderColumn);
       }
 
       if (orderColumn != null && GeneratorUtils.isEmptyOrNull(orderColumn.getName())
           && namingStrategy.isGenerateAllDBSchemaNames()) {
         orderColumn.setName(namingStrategy.getIndexColumnName(eAttribute));
       }
 
       if (GeneratorUtils.isEmptyOrNull(elementCollection.getName())) {
         elementCollection.setName(getName(eAttribute));
       }
 
       // make the access field if not changeable, as there won't be a setter
       if (!eAttribute.isChangeable()) {
         elementCollection.setAccess(AccessType.FIELD);
       }
 
       return;
     }
 
     boolean basicSet = true;
     if (annotation.getBasic() == null) {
       basicSet = false;
       annotation.setBasic(OrmFactory.eINSTANCE.createBasic());
     }
     final Basic basic = annotation.getBasic();
 
     // make the access field if not changeable, as there won't be a setter
     if (!eAttribute.isChangeable()) {
       basic.setAccess(AccessType.FIELD);
     } else if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
       basic.setAccess(AccessType.PROPERTY);
     }
 
     if (GeneratorUtils.isEmptyOrNull(basic.getName())) {
       basic.setName(getName(eAttribute));
     }
 
     if (isLob) {
       final Lob lob = OrmFactory.eINSTANCE.createLob();
       basic.setLob(lob);
    } else if (!basicSet && isTime) {
       basic.setTemporal(TemporalType.TIMESTAMP);
    } else if (!basicSet && isDate) {
       basic.setTemporal(TemporalType.DATE);
     } else if (doAddConverter(eAttributeModelGen)) {
       basic.setConvert(ORMUtils.OBJECT_CONVERTER_NAME);
     }
 
     if (!basicSet && !GeneratorUtils.isOptional(eAttribute)) {
       basic.setOptional(false);
     }
 
     if (isEnum && basic.getEnumerated() == null) {
       basic.setEnumerated(EnumType.STRING);
     }
 
     if (namingStrategy.isGenerateAllDBSchemaNames()) {
       if (basic.getColumn() == null) {
         basic.setColumn(OrmFactory.eINSTANCE.createColumn());
       }
       final Column column = basic.getColumn();
       if (column.getName() == null) {
         column.setName(namingStrategy.getColumnName(annotation.getEStructuralFeature()));
       }
 
       // in case of single table mapping and not a root
       // then all columns of sub classes need to be nullable
       if (!hasItsOwnTable(eClassORMAnnotation)) {
         column.setNullable(true);
       }
     }
   }
 
   private void copyAnnotationsFromDataType(EAttributeORMAnnotation annotation) {
     final EDataTypeORMAnnotation eDataTypeORMAnnotation = (EDataTypeORMAnnotation) getAnnotationManager()
         .getAnnotation(annotation.getEAttribute().getEAttributeType(),
             OrmannotationsPackage.eINSTANCE.getEDataTypeORMAnnotation());
     // if the main features are already set return
     if (annotation.getId() != null || annotation.getVersion() != null || annotation.getBasic() != null
         || annotation.getElementCollection() != null) {
       return;
     }
     if (eDataTypeORMAnnotation.getId() != null) {
       annotation.setId(EcoreUtil.copy(eDataTypeORMAnnotation.getId()));
       return;
     }
     if (eDataTypeORMAnnotation.getBasic() != null) {
       annotation.setBasic(EcoreUtil.copy(eDataTypeORMAnnotation.getBasic()));
       return;
     }
     if (eDataTypeORMAnnotation.getTransient() != null) {
       annotation.setTransient(EcoreUtil.copy(eDataTypeORMAnnotation.getTransient()));
       return;
     }
     if (eDataTypeORMAnnotation.getVersion() != null) {
       annotation.setVersion(EcoreUtil.copy(eDataTypeORMAnnotation.getVersion()));
       return;
     }
     if (annotation.getEAttribute().isMany() && annotation.getElementCollection() == null) {
       if (eDataTypeORMAnnotation.getElementCollection() != null) {
         annotation.setElementCollection(EcoreUtil.copy(eDataTypeORMAnnotation.getElementCollection()));
       }
     }
   }
 
   private void copyAnnotationsFromEEnum(EAttributeORMAnnotation annotation) {
     final EEnumORMAnnotation eEnumORMAnnotation = (EEnumORMAnnotation) getAnnotationManager().getAnnotation(
         annotation.getEAttribute().getEAttributeType(), OrmannotationsPackage.eINSTANCE.getEEnumORMAnnotation());
     // if the main features are already set return
     if (annotation.getId() != null || annotation.getVersion() != null || annotation.getBasic() != null
         || annotation.getElementCollection() != null) {
       return;
     }
     if (eEnumORMAnnotation.getBasic() != null) {
       annotation.setBasic(EcoreUtil.copy(eEnumORMAnnotation.getBasic()));
       return;
     }
     if (eEnumORMAnnotation.getTransient() != null) {
       annotation.setTransient(EcoreUtil.copy(eEnumORMAnnotation.getTransient()));
       return;
     }
     if (eEnumORMAnnotation.getVersion() != null) {
       annotation.setVersion(EcoreUtil.copy(eEnumORMAnnotation.getVersion()));
       return;
     }
     if (annotation.getEAttribute().isMany() && annotation.getElementCollection() == null) {
       if (eEnumORMAnnotation.getElementCollection() != null) {
         annotation.setElementCollection(EcoreUtil.copy(eEnumORMAnnotation.getElementCollection()));
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @seeorg.eclipse.emf.texo.generator.Annotator#postAnnotating(org.eclipse.emf.texo.annotations.
    * annotationsmodel.ENamedElementAnnotation)
    */
   public void postAnnotating(EAttributeORMAnnotation annotation) {
   }
 
   private String getName(EAttribute eAttribute) {
     final EAttributeModelGenAnnotation modelGenAnnotation = (EAttributeModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eAttribute, ModelcodegeneratorPackage.eINSTANCE.getEAttributeModelGenAnnotation());
     return modelGenAnnotation.getValidJavaMemberName();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#getAnnotationEClass()
    */
   public EClass getAnnotationEClass() {
     return OrmannotationsPackage.eINSTANCE.getEAttributeORMAnnotation();
   }
 }
