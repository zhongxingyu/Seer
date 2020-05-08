 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.kernel.plugins.deployment.xml;
 
 import javax.xml.namespace.QName;
 
 import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
 import org.jboss.xb.binding.sunday.unmarshalling.WildcardBinding;
 
 /**
  * BeanSchemaBindingHelper.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class BeanSchemaBindingHelper
 {
    public static void initAll(SchemaBinding schemaBinding)
    {
       
    }
    
    /**
     * Initialize the handlers for the deployment type
     * 
     * @param deploymentType the deployment type
     */
    public static void initDeploymentHandlers(TypeBinding deploymentType)
    {
       deploymentType.setHandler(DeploymentHandler.HANDLER);
 
       // deployment has a classloader
       deploymentType.pushInterceptor(BeanSchemaBinding20.classloaderQName, DeploymentClassLoaderInterceptor.INTERCEPTOR);
 
       // deployment has a list beans
       deploymentType.pushInterceptor(BeanSchemaBinding20.beanQName, DeploymentBeanInterceptor.INTERCEPTOR);
 
       // deployment has a list beanfactorys
       deploymentType.pushInterceptor(BeanSchemaBinding20.beanFactoryQName, DeploymentBeanInterceptor.INTERCEPTOR);
 
       // Deployment can take wildcards
       deploymentType.getWildcard().setWildcardHandler(DeploymentWildcardHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the bean type
     * 
     * @param beanType the bean type
     */
    public static void initBeanHandlers(TypeBinding beanType)
    {
       beanType.setHandler(BeanHandler.HANDLER);
 
       // bean has a classloader
       beanType.pushInterceptor(BeanSchemaBinding20.classloaderQName, BeanClassLoaderInterceptor.INTERCEPTOR);
 
       // bean has a constructor
       beanType.pushInterceptor(BeanSchemaBinding20.constructorQName, BeanConstructorInterceptor.INTERCEPTOR);
 
       // bean has properties
       beanType.pushInterceptor(BeanSchemaBinding20.propertyQName, BeanPropertyInterceptor.INTERCEPTOR);
 
       // bean has a create
       beanType.pushInterceptor(BeanSchemaBinding20.createQName, BeanCreateInterceptor.INTERCEPTOR);
 
       // bean has a start
       beanType.pushInterceptor(BeanSchemaBinding20.startQName, BeanStartInterceptor.INTERCEPTOR);
 
       // bean has a stop
       beanType.pushInterceptor(BeanSchemaBinding20.stopQName, BeanStopInterceptor.INTERCEPTOR);
 
       // bean has a destroy
       beanType.pushInterceptor(BeanSchemaBinding20.destroyQName, BeanDestroyInterceptor.INTERCEPTOR);
 
       // bean has annotations
       beanType.pushInterceptor(BeanSchemaBinding20.annotationQName, BeanAnnotationInterceptor.INTERCEPTOR);
 
       // bean has installs
       beanType.pushInterceptor(BeanSchemaBinding20.installQName, BeanInstallInterceptor.INTERCEPTOR);
 
       // bean has uninstalls
       beanType.pushInterceptor(BeanSchemaBinding20.uninstallQName, BeanUninstallInterceptor.INTERCEPTOR);
 
       // bean has depends
       beanType.pushInterceptor(BeanSchemaBinding20.dependsQName, BeanDependsInterceptor.INTERCEPTOR);
 
       // bean has demands
       beanType.pushInterceptor(BeanSchemaBinding20.demandQName, BeanDemandsInterceptor.INTERCEPTOR);
 
       // bean has supplies
       beanType.pushInterceptor(BeanSchemaBinding20.supplyQName, BeanSuppliesInterceptor.INTERCEPTOR);
    }
 
    /**
     * Initialize the handlers for the bean factory type
     * 
     * @param beanFactoryType the bean factory type
     */
    public static void initBeanFactoryHandlers(TypeBinding beanFactoryType)
    {
       beanFactoryType.setHandler(BeanFactoryHandler.HANDLER);
 
       // bean factory has a classloader
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.classloaderQName, BeanClassLoaderInterceptor.INTERCEPTOR);
 
       // bean factory has a constructor
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.constructorQName, BeanFactoryConstructorInterceptor.INTERCEPTOR);
 
       // bean factory has properties
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.propertyQName, BeanFactoryPropertyInterceptor.INTERCEPTOR);
 
       // bean factory has a create
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.createQName, BeanFactoryCreateInterceptor.INTERCEPTOR);
 
       // bean factory has a start
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.startQName, BeanFactoryStartInterceptor.INTERCEPTOR);
 
       // bean factory has installs
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.installQName, BeanInstallInterceptor.INTERCEPTOR);
 
       // bean factory has uninstalls
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.uninstallQName, BeanUninstallInterceptor.INTERCEPTOR);
 
       // bean factory has depends
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.dependsQName, BeanDependsInterceptor.INTERCEPTOR);
 
       // bean factory has demands
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.demandQName, BeanDemandsInterceptor.INTERCEPTOR);
 
       // bean factory has supplies
       beanFactoryType.pushInterceptor(BeanSchemaBinding20.supplyQName, BeanSuppliesInterceptor.INTERCEPTOR);
    }
 
    /**
     * Initialize the handlers for the classloader type
     * 
     * @param classloaderType the classloader type
     */
    public static void initClassLoaderHandlers(TypeBinding classloaderType)
    {
       classloaderType.setHandler(ClassLoaderHandler.HANDLER);
 
       configureValueBindings(classloaderType);
    }
 
    /**
     * Initialize the handlers for the constructor type
     * 
     * @param constructorType the constructor type
     */
    public static void initConstructorHandlers(TypeBinding constructorType)
    {
       constructorType.setHandler(ConstructorHandler.HANDLER);
 
       // constructor has annotations
       constructorType.pushInterceptor(BeanSchemaBinding20.annotationQName, ConstructorAnnotationsInterceptor.INTERCEPTOR);
 
       // constructor has a factory
       constructorType.pushInterceptor(BeanSchemaBinding20.factoryQName, ConstructorFactoryInterceptor.INTERCEPTOR);
 
       // constructor has parameters
       constructorType.pushInterceptor(BeanSchemaBinding20.parameterQName, ConstructorParametersInterceptor.INTERCEPTOR);
       
       configureValueBindings(constructorType);
    }
 
    /**
     * Initialize the handlers for the property type
     * 
     * @param propertyType the property type
     */
    public static void initPropertyHandlers(TypeBinding propertyType)
    {
       propertyType.setHandler(PropertyHandler.HANDLER);
 
       // property has annotations
       propertyType.pushInterceptor(BeanSchemaBinding20.annotationQName, PropertyAnnotationsInterceptor.INTERCEPTOR);
 
       // property can take characters
       propertyType.setSimpleType(PropertyCharactersHandler.HANDLER);
       
       configureValueBindings(propertyType);
    }
 
    /**
     * Initialize the handlers for the parameter type
     * 
     * @param parameterType the parameter type
     */
    public static void initParameterHandlers(TypeBinding parameterType)
    {
       parameterType.setHandler(ParameterHandler.HANDLER);
 
       // parameter has annotations
       parameterType.pushInterceptor(BeanSchemaBinding20.annotationQName, ParameterAnnotationsInterceptor.INTERCEPTOR);
 
       // parameter can take characters
       parameterType.setSimpleType(ParameterCharactersHandler.HANDLER);
       
       configureValueBindings(parameterType);
    }
 
    /**
     * Initialize the handlers for the lifecycle type
     * 
     * @param lifecycleType the lifecycle type
     */
    public static void initLifecycleHandlers(TypeBinding lifecycleType)
    {
       lifecycleType.setHandler(LifecycleHandler.HANDLER);
 
       // lifecycle has annotations
       lifecycleType.pushInterceptor(BeanSchemaBinding20.annotationQName, LifecycleAnnotationsInterceptor.INTERCEPTOR);
 
       // lifecycle has parameters
       lifecycleType.pushInterceptor(BeanSchemaBinding20.parameterQName, LifecycleParametersInterceptor.INTERCEPTOR);
    }
 
    /**
     * Initialize the handlers for the install type
     * 
     * @param installType the install type
     */
    public static void initInstallHandlers(TypeBinding installType)
    {
       installType.setHandler(InstallHandler.HANDLER);
 
       // install has annotations
       installType.pushInterceptor(BeanSchemaBinding20.annotationQName, InstallAnnotationsInterceptor.INTERCEPTOR);
 
       // install has parameters
       installType.pushInterceptor(BeanSchemaBinding20.parameterQName, InstallParametersInterceptor.INTERCEPTOR);
    }
 
    /**
     * Initialize the handlers for the annotation type
     * 
     * @param annotationType the annotation type
     */
    public static void initAnnotationHandlers(TypeBinding annotationType)
    {
       annotationType.setHandler(AnnotationHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the depends type
     * 
     * @param dependsType the depends type
     */
    public static void initDependsHandlers(TypeBinding dependsType)
    {
       dependsType.setHandler(DependsHandler.HANDLER);
 
       // depends can take characters
       dependsType.setSimpleType(DependsCharactersHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the demands type
     * 
     * @param demandType the demand type
     */
    public static void initDemandHandlers(TypeBinding demandType)
    {
       demandType.setHandler(DemandHandler.HANDLER);
 
       // demand can take characters
       demandType.setSimpleType(DemandCharactersHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the supply type
     * 
     * @param supplyType the supply type
     */
    public static void initSupplyHandlers(TypeBinding supplyType)
    {
       supplyType.setHandler(SupplyHandler.HANDLER);
 
       // supply can take characters
       supplyType.setSimpleType(SupplyCharactersHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the dependency type
     * 
     * @param dependencyType the dependency type
     */
    public static void initDependencyHandlers(TypeBinding dependencyType)
    {
       dependencyType.setHandler(DependencyHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the injection type
     *
     * @param dependencyType the dependency type
     */
    public static void initInjectionHandlers(TypeBinding dependencyType)
    {
       dependencyType.setHandler(InjectionHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the factory type
     * 
     * @param factoryType the factory type
     */
    public static void initFactoryHandlers(TypeBinding factoryType)
    {
       factoryType.setHandler(FactoryHandler.HANDLER);
       
       // factory has a wildcard
       factoryType.getWildcard().setWildcardHandler(FactoryWildcardHandler.WILDCARD);
    }
 
    /**
     * Initialize the handlers for the plain value type
     * 
     * @param plainValueType the plain value type
     */
    public static void initPlainValueHandlers(TypeBinding plainValueType)
    {
       plainValueType.setHandler(PlainValueHandler.HANDLER);
 
       // plain value can take characters
       plainValueType.setSimpleType(PlainValueCharactersHandler.HANDLER);
    }
 
    /**
     * Initialize the handlers for the value type
     * 
     * @param valueType the value type
     */
    public static void initValueHandlers(TypeBinding valueType)
    {
       valueType.setHandler(ValueHandler.HANDLER);
 
       // value can take characters
       valueType.setSimpleType(ValueCharactersHandler.HANDLER);
       
       BeanSchemaBindingHelper.configureValueBindings(valueType);
    }
 
    /**
     * Initialize the handlers for the map type
     * 
     * @param mapType the map type
     */
    public static void initMapHandlers(TypeBinding mapType)
    {
       mapType.setHandler(MapHandler.HANDLER);
 
       // entry has an entry
       mapType.pushInterceptor(BeanSchemaBinding20.entryQName, MapEntryInterceptor.INTERCEPTOR);
    }
 
    /**
     * Initialize the handlers for the map entry type
     * 
     * @param entryType the map entry type
     */
    public static void initEntryHandlers(TypeBinding entryType)
    {
       entryType.setHandler(EntryHandler.HANDLER);
 
       // entry has a key
       entryType.pushInterceptor(BeanSchemaBinding20.keyQName, EntryKeyInterceptor.INTERCEPTOR);
 
       // entry has value
       entryType.pushInterceptor(BeanSchemaBinding20.valueQName, EntryValueInterceptor.INTERCEPTOR);
    }
    
    /**
     * Configure a collection.
     * 
     * @param schemaBinding the schemabinding
     * @param qname the name of the type
     */
    public static void configureCollection(SchemaBinding schemaBinding, QName qname)
    {
       TypeBinding collectionType = schemaBinding.getType(qname);
       collectionType.setHandler(CollectionHandler.HANDLER);
       configureValueBindings(collectionType);
    }
    
    /**
     * Configure value bindings
     * 
     * @param typeBinding the type binding
     */
    public static void configureValueBindings(TypeBinding typeBinding)
    {
       // type has values
       typeBinding.pushInterceptor(BeanSchemaBinding20.valueQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type has injections
       typeBinding.pushInterceptor(BeanSchemaBinding20.injectQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take a collection
       typeBinding.pushInterceptor(BeanSchemaBinding20.collectionQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take a list
       typeBinding.pushInterceptor(BeanSchemaBinding20.listQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take a set
       typeBinding.pushInterceptor(BeanSchemaBinding20.setQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take an array
       typeBinding.pushInterceptor(BeanSchemaBinding20.arrayQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take a map
       typeBinding.pushInterceptor(BeanSchemaBinding20.mapQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type has a null
       typeBinding.pushInterceptor(BeanSchemaBinding20.nullQName, NullValueElementInterceptor.NULLVALUES);
 
       // type has a this
       typeBinding.pushInterceptor(BeanSchemaBinding20.thisQName, ThisValueElementInterceptor.THISVALUES);
       
       // type has wildcard
       WildcardBinding wcb = typeBinding.getWildcard();
      if( wcb != null )
         wcb.setWildcardHandler(ValueWildcardHandler.WILDCARD);
    }
 }
