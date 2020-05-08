 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.otherobjects.cms.jcr;
 
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.jackrabbit.ocm.exception.IncorrectPersistentClassException;
 import org.apache.jackrabbit.ocm.exception.InitMapperException;
import org.apache.jackrabbit.ocm.manager.atomictypeconverter.impl.Date2LongTypeConverterImpl;
 import org.apache.jackrabbit.ocm.manager.beanconverter.impl.DefaultBeanConverterImpl;
 import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.DefaultCollectionConverterImpl;
 import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.MultiValueCollectionConverterImpl;
 import org.apache.jackrabbit.ocm.mapper.Mapper;
 import org.apache.jackrabbit.ocm.mapper.model.BeanDescriptor;
 import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
 import org.apache.jackrabbit.ocm.mapper.model.CollectionDescriptor;
 import org.apache.jackrabbit.ocm.mapper.model.FieldDescriptor;
 import org.apache.jackrabbit.ocm.mapper.model.MappingDescriptor;
 import org.otherobjects.cms.jcr.dynamic.DynaNode;
 import org.otherobjects.cms.jcr.dynamic.DynaNodeDataMapConverterImpl;
 import org.otherobjects.cms.types.PropertyDef;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.types.TypeServiceImpl;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.core.io.Resource;
 import org.springframework.util.Assert;
 
 /**
  * JCR OCM mapper imelementation backed by a TypeService.
  * 
  * <p>TODO Bind to register/unregister events?
  * 
  * @author rich
  */
 public class TypeServiceMapperImpl implements Mapper, InitializingBean
 {
     private TypeService typeService;
     private MappingDescriptor mappingDescriptor;
     private Mapper staticMapper;
 
     public TypeServiceMapperImpl()
     {
     }
 
     public TypeServiceMapperImpl(Resource resource) throws IOException
     {
         this.staticMapper = new ResourceDigesterMappingImpl(resource);
     }
 
     protected Mapper buildMapper()
     {
         if (typeService != null)
         {
             this.mappingDescriptor = new MappingDescriptor();
 
             for (TypeDef typeDef : typeService.getTypes())
             {
                 ClassDescriptor classDescriptor = createClassDescriptor(typeDef);
                 mappingDescriptor.addClassDescriptor(classDescriptor);
             }
 
             this.mappingDescriptor.setMapper(this);
         }
         else
         {
             throw new InitMapperException("No mappings were provided");
         }
 
         // Add DynaNode mapping
         ClassDescriptor classDescriptor = createDynaNodeClassDescriptor();
         mappingDescriptor.addClassDescriptor(classDescriptor);
         this.mappingDescriptor.setMapper(this);
 
         return this;
     }
 
     /**
      * DynaNode mapping needs to be individually configured.
      * 
      * @return
      */
     private ClassDescriptor createDynaNodeClassDescriptor()
     {
         ClassDescriptor cd = new ClassDescriptor();
         cd.setClassName(DynaNode.class.getName());
         cd.setJcrType("oo:node");
 
         // Map common fields
         addStandardFields(null, cd);
 
         // Map data map
         CollectionDescriptor cld = new CollectionDescriptor();
         cld.setFieldName("data");
         cld.setJcrName("data");
         cld.setCollectionConverter(DynaNodeDataMapConverterImpl.class.getName());
         cd.addCollectionDescriptor(cld);
 
         return cd;
     }
 
     protected ClassDescriptor createClassDescriptor(TypeDef typeDef)
     {
         ClassDescriptor cd = new ClassDescriptor();
         cd.setClassName(typeDef.getClassName());
 
         if (typeDef.isComponent())
             cd.setJcrType("oo:component");
         else
             cd.setJcrType("oo:node");
 
         addStandardFields(typeDef, cd);
 
         // Add custom properties
         for (PropertyDef propDef : typeDef.getProperties())
         {
             String propertyType = propDef.getType();
             String propertyName = propDef.getName();
 
             // FIXME Need better way to exclude already mapped properties
             if (!propertyName.equals("code"))
             {
 
                 if (propertyType.equals(PropertyDef.LIST))
                 {
                     String collectionElementType = propDef.getCollectionElementType();
                     Assert.isTrue(StringUtils.isNotEmpty(collectionElementType), "If this property is a collection the collectionElementType needs to have been set: " + typeDef.getName() + "."
                             + propertyName);
                     CollectionDescriptor cld = new CollectionDescriptor();
                     cld.setFieldName(propDef.getName());
                     cld.setJcrName(propDef.getName());
 
                     if (collectionElementType.equals(PropertyDef.COMPONENT))
                     {
                         Assert.isTrue(StringUtils.isNotEmpty(propDef.getRelatedType()), "If this property is a component the relatedType needs to have been set: " + typeDef.getName() + "."
                                 + propertyName);
                         cld.setElementClassName(propDef.getRelatedTypeDef().getClassName());
                         cld.setCollectionConverter(DefaultCollectionConverterImpl.class.getName());
                     }
                     else if (collectionElementType.equals(PropertyDef.REFERENCE))
                     {
                         Assert.isTrue(StringUtils.isNotEmpty(propDef.getRelatedType()), "If this property is a reference the relatedType needs to have been set: " + typeDef.getName() + "."
                                 + propertyName);
                         cld.setElementClassName(propDef.getRelatedTypeDef().getClassName());
                         cld.setCollectionConverter(BeanReferenceCollectionConverterImpl.class.getName());
                     }
                     else
                     {
                         cld.setElementClassName(((TypeServiceImpl) typeService).getJcrClassMapping(collectionElementType).getName());
                         cld.setCollectionConverter(MultiValueCollectionConverterImpl.class.getName());
                     }
 
                     cd.addCollectionDescriptor(cld);
                 }
                 else if (propertyType.equals(PropertyDef.COMPONENT))
                 {
                     Assert
                             .isTrue(StringUtils.isNotEmpty(propDef.getRelatedType()), "If this property is a component the relatedType needs to have been set: " + typeDef.getName() + "."
                                     + propertyName);
                     BeanDescriptor bd = new BeanDescriptor();
                     bd.setFieldName(propDef.getName());
                     bd.setJcrName(propDef.getName());
                     bd.setDefaultPrimaryType("oo:component");
                     bd.setConverter(DefaultBeanConverterImpl.class.getName());
                     cd.addBeanDescriptor(bd);
                 }
                 else if (propertyType.equals(PropertyDef.REFERENCE))
                 {
                     Assert
                             .isTrue(StringUtils.isNotEmpty(propDef.getRelatedType()), "If this property is a reference the relatedType needs to have been set: " + typeDef.getName() + "."
                                     + propertyName);
                     BeanDescriptor bd = new BeanDescriptor();
                     bd.setFieldName(propDef.getName());
                     bd.setJcrName(propDef.getName());
                     bd.setConverter(ReferenceBeanConverterImpl.class.getName());
                     cd.addBeanDescriptor(bd);
                 }
                 else if (propertyType.equals(PropertyDef.TRANSIENT))
                 {
                     // Transient properties are not mapped
                 }
                 else
                 {
                     FieldDescriptor f = new FieldDescriptor();
                     f.setFieldName(propDef.getName());
                     f.setJcrName(propDef.getName());
                     f.setConverter(((TypeServiceImpl) typeService).getJcrConverter(propertyType).getClass().getName());
                     cd.addFieldDescriptor(f);
                 }
             }
         }
         return cd;
     }
 
     private void addStandardFields(TypeDef typeDef, ClassDescriptor cd)
     {
         if (typeDef != null && StringUtils.isNotBlank(typeDef.getCodeProperty()))
         {
             // FIXME This is used for collections. Is there a better way?
             FieldDescriptor fd5 = new FieldDescriptor();
             fd5.setFieldName(typeDef.getCodeProperty());
             fd5.setJcrName(typeDef.getCodeProperty());
             fd5.setId(true);
             cd.addFieldDescriptor(fd5);
         }
 
         if (!cd.getJcrType().equals("oo:component"))
         {
             FieldDescriptor fd2 = new FieldDescriptor();
             fd2.setFieldName("jcrPath");
             fd2.setJcrName("jcrPath");
             fd2.setPath(true);
             cd.addFieldDescriptor(fd2);
         }
 
         FieldDescriptor fd3 = new FieldDescriptor();
         fd3.setFieldName("ooLabel");
         fd3.setJcrName("ooLabel");
         cd.addFieldDescriptor(fd3);
 
         FieldDescriptor fd4 = new FieldDescriptor();
         fd4.setFieldName("ooType");
         fd4.setJcrName("ooType");
         cd.addFieldDescriptor(fd4);
 
         if (!cd.getJcrType().equals("oo:component"))
         {
             FieldDescriptor fd = new FieldDescriptor();
             fd.setFieldName("id");
             fd.setJcrName("id");
             fd.setUuid(true);
             cd.addFieldDescriptor(fd);
 
             FieldDescriptor fd6 = new FieldDescriptor();
             fd6.setFieldName("published");
             fd6.setJcrName("published");
             cd.addFieldDescriptor(fd6);
 
             // Audit info
             FieldDescriptor fd7 = new FieldDescriptor();
             fd7.setFieldName("modifier");
             fd7.setJcrName("modifier");
             cd.addFieldDescriptor(fd7);
 
             FieldDescriptor fd8 = new FieldDescriptor();
             fd8.setFieldName("creator");
             fd8.setJcrName("creator");
             cd.addFieldDescriptor(fd8);
 
             FieldDescriptor fd9 = new FieldDescriptor();
             fd9.setFieldName("modificationTimestamp");
             fd9.setJcrName("modificationTimestamp");
            fd9.setConverter(Date2LongTypeConverterImpl.class.getName());
             cd.addFieldDescriptor(fd9);
 
             FieldDescriptor fd12 = new FieldDescriptor();
             fd12.setFieldName("creationTimestamp");
             fd12.setJcrName("creationTimestamp");
            fd12.setConverter(Date2LongTypeConverterImpl.class.getName());
             cd.addFieldDescriptor(fd12);
 
             FieldDescriptor fd10 = new FieldDescriptor();
             fd10.setFieldName("editingComment");
             fd10.setJcrName("editingComment");
             cd.addFieldDescriptor(fd10);
 
             FieldDescriptor fd11 = new FieldDescriptor();
             fd11.setFieldName("version");
             fd11.setJcrName("version");
             cd.addFieldDescriptor(fd11);
 
         }
     }
 
     /**
     *
     * @see org.apache.jackrabbit.ocm.mapper.Mapper#getClassDescriptorByClass(java.lang.Class)
     */
     @SuppressWarnings("unchecked")
     public ClassDescriptor getClassDescriptorByClass(Class clazz)
     {
         // Try config file mappings first
         try
         {
             return staticMapper.getClassDescriptorByClass(clazz);
         }
         catch (RuntimeException e)
         {
             // TODO Explain why we ignore exception
         }
 
         // Try already build type mappings
         ClassDescriptor descriptor = mappingDescriptor.getClassDescriptorByName(clazz.getName());
 
         if (descriptor != null)
             return descriptor;
 
         // Try building missing mapping
         TypeDef td = typeService.getTypeByClassName(clazz.getName());
 
         // FIXME This must be sychronised to be tread safe
         if (td != null)
         {
             descriptor = createClassDescriptor(td);
             this.mappingDescriptor.addClassDescriptor(descriptor);
             return descriptor;
         }
 
         // No descriptor so throw error
         throw new IncorrectPersistentClassException("Class of type: " + clazz.getName() + " has no descriptor.");
     }
 
     /**
     * @see org.apache.jackrabbit.ocm.mapper.Mapper#getClassDescriptorByNodeType(String)
     */
     public ClassDescriptor getClassDescriptorByNodeType(String jcrNodeType)
     {
         // try static mappings first
         ClassDescriptor descriptor = staticMapper.getClassDescriptorByNodeType(jcrNodeType);
         if (descriptor != null)
             return descriptor;
 
         // then dynamic mapping
         descriptor = mappingDescriptor.getClassDescriptorByNodeType(jcrNodeType);
         if (descriptor == null)
         {
             throw new IncorrectPersistentClassException("Node type: " + jcrNodeType + " has no descriptor.");
         }
         return descriptor;
     }
 
     public TypeService getTypeService()
     {
         return typeService;
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     public void afterPropertiesSet() throws Exception
     {
         Assert.isInstanceOf(TypeServiceImpl.class, typeService);
         buildMapper();
     }
 }
