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
  * $Id: EClassORMAnnotator.java,v 1.10 2011/09/21 14:03:48 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.orm.annotator;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.texo.generator.Annotator;
 import org.eclipse.emf.texo.generator.GeneratorUtils;
 import org.eclipse.emf.texo.modelgenerator.annotator.GenUtils;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EClassModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.ModelcodegeneratorPackage;
 import org.eclipse.emf.texo.orm.annotations.model.orm.AccessType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Entity;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Inheritance;
 import org.eclipse.emf.texo.orm.annotations.model.orm.InheritanceType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.MappedSuperclass;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrmFactory;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Table;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Transient;
 import org.eclipse.emf.texo.orm.ormannotations.EClassORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.OrmannotationsPackage;
 
 /**
  * Responsible for creating the {@link EClassORMAnnotation}.
  * 
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.10 $
  */
 
 public class EClassORMAnnotator extends ETypeElementORMAnnotator implements Annotator<EClassORMAnnotation> {
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#annotate(org.eclipse.emf.texo.annotations.annotationsmodel
    * .ENamedElementAnnotation)
    */
   public void setAnnotationFeatures(EClassORMAnnotation annotation) {
     final EClass eClass = annotation.getEClass();
 
     if (eClass.isInterface() || GenUtils.isDocumentRoot(eClass)) {
       return;
     }
 
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eClass.getEPackage());
     if (annotation.getTransient() != null) {
       final Transient transnt = annotation.getTransient();
       if (GeneratorUtils.isEmptyOrNull(transnt.getName())) {
         transnt.setName(namingStrategy.getEntityName(eClass));
       }
       return;
     }
 
     final EClassModelGenAnnotation modelGenAnnotation = (EClassModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eClass, ModelcodegeneratorPackage.eINSTANCE.getEClassModelGenAnnotation());
 
     if (annotation.getMappedSuperclass() != null) {
       final MappedSuperclass mappedSuperclass = annotation.getMappedSuperclass();
       if (GeneratorUtils.isEmptyOrNull(mappedSuperclass.getClass_())) {
         mappedSuperclass.setClass(modelGenAnnotation.getQualifiedClassName());
       }
       return;
     }
 
     // only add entity if not embeddable
     if (annotation.getEmbeddable() == null && annotation.getEntity() == null) {
       annotation.setEntity(OrmFactory.eINSTANCE.createEntity());
     }
 
     final Entity entity = annotation.getEntity();
 
     if (entity != null) {
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         if (hasItsOwnTable(annotation)) {
           // set the tablename
           if (entity.getTable() == null) {
             entity.setTable(OrmFactory.eINSTANCE.createTable());
           }
           final Table table = entity.getTable();
           if (table.getName() == null) {
             table.setName(namingStrategy.getTableName(eClass));
           }
          if (!isRoot(annotation) && entity.getPrimaryKeyJoinColumn().isEmpty()) {
            // create a join column to the parent
            entity.getPrimaryKeyJoinColumn().add(OrmFactory.eINSTANCE.createPrimaryKeyJoinColumn());
            entity.getPrimaryKeyJoinColumn().get(0).setName(namingStrategy.getPrimaryKeyJoinColumn(eClass));
          }
         }
       }
 
       // with interfaces always access through the property
       if (eClass.isInterface()) {
         entity.setAccess(AccessType.PROPERTY);
       }
 
       if (GeneratorUtils.isEmptyOrNull(entity.getClass_())) {
         entity.setClass(modelGenAnnotation.getQualifiedClassName());
       }
 
       if (GeneratorUtils.isEmptyOrNull(entity.getName())) {
         entity.setName(namingStrategy.getEntityName(eClass));
       }
     }
   }
 
   private boolean hasItsOwnTable(EClassORMAnnotation annotation) {
     final boolean isRoot = isRoot(annotation);
     final Inheritance inheritance;
     if (annotation.getEntity() != null && annotation.getEntity().getInheritance() != null) {
       inheritance = annotation.getEntity().getInheritance();
     } else {
       inheritance = getInheritance(annotation.getEClass());
     }
     return isRoot || inheritance != null && inheritance.getStrategy() != InheritanceType.SINGLETABLE;
   }
 
   private boolean isRoot(EClassORMAnnotation ormAnnotation) {
     final EClass eClass = ormAnnotation.getEClass();
     if (eClass.getESuperTypes().isEmpty()) {
       return true;
     }
 
     // all super types are transient or interface or mapped super class
     for (EClass eSuperType : eClass.getESuperTypes()) {
       final EClassORMAnnotation superOrmAnnotation = (EClassORMAnnotation) getAnnotationManager().getAnnotation(
           eSuperType, OrmannotationsPackage.eINSTANCE.getEClassORMAnnotation());
       if (!superOrmAnnotation.getEClass().isInterface() && superOrmAnnotation.getMappedSuperclass() == null
           && superOrmAnnotation.getTransient() == null) {
         return false;
       }
     }
     return true;
   }
 
   private Inheritance getInheritance(EClass eClass) {
 
     for (EClass eSuperType : eClass.getESuperTypes()) {
       final EClassORMAnnotation orm = (EClassORMAnnotation) getAnnotationManager().getAnnotation(eSuperType,
           OrmannotationsPackage.eINSTANCE.getEClassORMAnnotation());
       if (orm.getEntity() != null && orm.getEntity().getInheritance() != null) {
         return orm.getEntity().getInheritance();
       }
       final Inheritance inheritance = getInheritance(eSuperType);
       if (inheritance != null) {
         return inheritance;
       }
     }
     return null;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @seeorg.eclipse.emf.texo.generator.Annotator#postAnnotating(org.eclipse.emf.texo.annotations.
    * annotationsmodel.ENamedElementAnnotation)
    */
   public void postAnnotating(EClassORMAnnotation annotation) {
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#getAnnotationEClass()
    */
   public EClass getAnnotationEClass() {
     return OrmannotationsPackage.eINSTANCE.getEClassORMAnnotation();
   }
 }
