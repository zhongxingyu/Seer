 /*
  * Copyright 2004-2010 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.impl;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.util.List;
 import java.util.Map;
 
 import org.seasar.dao.BeanMetaData;
 import org.seasar.dao.DaoAnnotationReader;
 import org.seasar.dao.DtoMetaData;
 import org.seasar.dao.DtoMetaDataFactory;
 import org.seasar.dao.RelationRowCreator;
 import org.seasar.dao.ResultSetHandlerFactory;
 import org.seasar.dao.RowCreator;
 import org.seasar.dao.impl.BeanMetaDataResultSetHandler.RestrictBeanMetaDataResultSetHandler;
 import org.seasar.dao.impl.DtoMetaDataResultSetHandler.RestrictDtoMetaDataResultSetHandler;
 import org.seasar.dao.impl.MapResultSetHandler.RestrictMapResultSetHandler;
 import org.seasar.dao.impl.ObjectResultSetHandler.RestrictObjectResultSetHandler;
 import org.seasar.dao.util.TypeUtil;
 import org.seasar.extension.jdbc.ResultSetHandler;
 
 /**
  * @author manhole
  * @author jflute
  */
 public class ResultSetHandlerFactoryImpl implements ResultSetHandlerFactory {
 
     public static final String dtoMetaDataFactory_BINDING = "bindingType=must";
 
     protected DtoMetaDataFactory dtoMetaDataFactory;
 
     /**
      * 返り値がBean、DTOやMapのメソッドで、結果が2件以上の時に例外を投げるかを設定します。
      */
     protected boolean restrictNotSingleResult = false;
 
     public ResultSetHandler getResultSetHandler(
             final DaoAnnotationReader daoAnnotationReader,
             final BeanMetaData beanMetaData, final Method method) {
 
         final Class beanClass = daoAnnotationReader.getBeanClass();
         final Class clazz = daoAnnotationReader.getBeanClass(method);
         if ((clazz != null) && !clazz.isAssignableFrom(beanClass)) {
             if (TypeUtil.isSimpleType(clazz)) {
                 if (List.class.isAssignableFrom(method.getReturnType())) {
                     return createObjectListResultSetHandler(clazz);
                 } else if (method.getReturnType().isArray()
                         && !TypeUtil.isSimpleType(method.getReturnType())) {
                     return createObjectArrayResultSetHandler(clazz);
                 } else {
                     return createObjectResultSetHandler(clazz);
                 }
             }
             if (Map.class.isAssignableFrom(clazz)) {
                 if (List.class.isAssignableFrom(method.getReturnType())) {
                     return createMapListResultSetHandler();
                 } else if (method.getReturnType().isArray()) {
                     return createMapArrayResultSetHandler();
                 } else {
                     return createMapResultSetHandler();
                 }
             }
             final DtoMetaData dtoMetaData = dtoMetaDataFactory
                     .getDtoMetaData(clazz);
             if (List.class.isAssignableFrom(method.getReturnType())) {
                 return createDtoListMetaDataResultSetHandler(dtoMetaData);
             } else if (method.getReturnType() == clazz) {
                 return createDtoMetaDataResultSetHandler(dtoMetaData);
             } else if (method.getReturnType().isArray()) {
                 return createDtoArrayMetaDataResultSetHandler(dtoMetaData);
             }
         } else {
             if (List.class.isAssignableFrom(method.getReturnType())) {
                 return createBeanListMetaDataResultSetHandler(beanMetaData);
             } else if (isBeanClassAssignable(beanClass, method.getReturnType())) {
                 return createBeanMetaDataResultSetHandler(beanMetaData);
             } else if (method.getReturnType().isAssignableFrom(
                     Array.newInstance(beanClass, 0).getClass())) {
                 return createBeanArrayMetaDataResultSetHandler(beanMetaData);
             }
         }
         return createObjectResultSetHandler(null);
     }
 
     protected ResultSetHandler createDtoListMetaDataResultSetHandler(
             final DtoMetaData dtoMetaData) {
         return new DtoListMetaDataResultSetHandler(dtoMetaData,
                 createRowCreator());
     }
 
     protected ResultSetHandler createDtoMetaDataResultSetHandler(
             final DtoMetaData dtoMetaData) {
         if (restrictNotSingleResult) {
             return new RestrictDtoMetaDataResultSetHandler(dtoMetaData,
                     createRowCreator());
         }
         return new DtoMetaDataResultSetHandler(dtoMetaData, createRowCreator());
     }
 
     protected ResultSetHandler createDtoArrayMetaDataResultSetHandler(
             final DtoMetaData dtoMetaData) {
         return new DtoArrayMetaDataResultSetHandler(dtoMetaData,
                 createRowCreator());
     }
 
     protected ResultSetHandler createMapListResultSetHandler() {
         return new MapListResultSetHandler();
     }
 
     protected ResultSetHandler createMapResultSetHandler() {
         if (restrictNotSingleResult) {
             return new RestrictMapResultSetHandler();
         }
         return new MapResultSetHandler();
     }
 
     protected ResultSetHandler createMapArrayResultSetHandler() {
         return new MapArrayResultSetHandler();
     }
 
     protected ResultSetHandler createBeanListMetaDataResultSetHandler(
             final BeanMetaData beanMetaData) {
         return new BeanListMetaDataResultSetHandler(beanMetaData,
                 createRowCreator(), createRelationRowCreator());
     }
 
     protected ResultSetHandler createBeanMetaDataResultSetHandler(
             final BeanMetaData beanMetaData) {
         if (restrictNotSingleResult) {
             return new RestrictBeanMetaDataResultSetHandler(beanMetaData,
                     createRowCreator(), createRelationRowCreator());
         }
         return new BeanMetaDataResultSetHandler(beanMetaData,
                 createRowCreator(), createRelationRowCreator());
     }
 
     protected ResultSetHandler createBeanArrayMetaDataResultSetHandler(
             final BeanMetaData beanMetaData) {
         return new BeanArrayMetaDataResultSetHandler(beanMetaData,
                 createRowCreator(), createRelationRowCreator());
     }
 
     protected ResultSetHandler createObjectListResultSetHandler(Class clazz) {
         return new ObjectListResultSetHandler(clazz);
     }
 
     protected ResultSetHandler createObjectResultSetHandler(Class clazz) {
         if (restrictNotSingleResult) {
             return new RestrictObjectResultSetHandler(clazz);
         }
         return new ObjectResultSetHandler(clazz);
     }
 
     protected ResultSetHandler createObjectArrayResultSetHandler(Class clazz) {
         return new ObjectArrayResultSetHandler(clazz);
     }
 
     protected RowCreator createRowCreator() {// [DAO-118] (2007/08/25)
         return new RowCreatorImpl();
     }
 
     protected RelationRowCreator createRelationRowCreator() {
         return new RelationRowCreatorImpl();
     }
 
     protected boolean isBeanClassAssignable(final Class beanClass,
             final Class clazz) {
         return beanClass.isAssignableFrom(clazz)
                 || clazz.isAssignableFrom(beanClass);
     }
 
     public void setDtoMetaDataFactory(
             final DtoMetaDataFactory dtoMetaDataFactory) {
         this.dtoMetaDataFactory = dtoMetaDataFactory;
     }
 
     /**
      * 返り値がBean、DTOやMapのメソッドで、結果が2件以上の時に例外を投げるかを設定します。
      * 
      * @param 例外を投げるときに
      *            <code>true</code>
      */
     public void setRestrictNotSingleResult(boolean restrictNotSingleResult) {
         this.restrictNotSingleResult = restrictNotSingleResult;
     }
 
 }
