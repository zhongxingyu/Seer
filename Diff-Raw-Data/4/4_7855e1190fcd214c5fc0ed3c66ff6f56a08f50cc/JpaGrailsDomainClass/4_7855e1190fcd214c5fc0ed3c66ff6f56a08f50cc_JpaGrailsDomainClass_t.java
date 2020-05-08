 /* Copyright 2004-2005 Graeme Rocher
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.grails.jpa.domain;
 
 import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
 import org.codehaus.groovy.grails.commons.GrailsDomainClass;
 import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
 import org.codehaus.groovy.grails.commons.GrailsClassUtils;
 import org.codehaus.groovy.grails.exceptions.GrailsDomainException;
 import org.codehaus.groovy.grails.validation.metaclass.ConstraintsEvaluatingDynamicProperty;
 import org.springframework.validation.Validator;
 import org.springframework.beans.BeanUtils;
 
 import javax.persistence.*;
 import java.util.*;
 import java.beans.PropertyDescriptor;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.Field;
 import java.lang.reflect.Type;
 import java.lang.reflect.ParameterizedType;
 
 import grails.util.GrailsNameUtils;
 import groovy.lang.Closure;
 
 /**
  * Models a JPA domain class hooking the JPA annotations into the Grails meta model
  *
  * @author Graeme Rocher
  * @since 1.0
  *        <p/>
  *        Created: May 15, 2009
  */
 public class JpaGrailsDomainClass extends AbstractGrailsClass implements GrailsDomainClass {
     private Map<String, GrailsDomainClassProperty> propertyMap = new HashMap<String, GrailsDomainClassProperty>();
     private Map<String, GrailsDomainClassProperty> persistentProperties = new HashMap<String, GrailsDomainClassProperty>();
     private GrailsDomainClassProperty[] propertiesArray;
     private JpaDomainClassProperty identifier;
     private JpaDomainClassProperty version;
     private Validator validator;
     private GrailsDomainClassProperty[] persistentPropertyArray;
     private Map constrainedProperties = Collections.EMPTY_MAP;
 
     public JpaGrailsDomainClass(Class clazz) {
         super(clazz, "");
         Annotation entityAnnotation = clazz.getAnnotation(Entity.class);
         if(entityAnnotation == null) {
             throw new GrailsDomainException("Class ["+clazz.getName()+"] is not annotated with java.persistence.Entity!");
         }
         PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);
         evaluateClassProperties(descriptors);
         evaluateConstraints();
         propertiesArray = propertyMap.values().toArray(new GrailsDomainClassProperty[propertyMap.size()]);
         persistentPropertyArray = persistentProperties.values().toArray(new GrailsDomainClassProperty[persistentProperties.size()]);
     }
 
     private void evaluateConstraints() {
         ConstraintsEvaluatingDynamicProperty constraintsEvaluator = new ConstraintsEvaluatingDynamicProperty(getProperties());
         this.constrainedProperties = (Map) constraintsEvaluator.get(getReference().getWrappedInstance());
     }
 
     private void evaluateClassProperties(PropertyDescriptor[] descriptors) {
         for (PropertyDescriptor descriptor : descriptors) {
 
             final JpaDomainClassProperty property = new JpaDomainClassProperty(this, descriptor);
             
             if(property.isAnnotatedWith(Id.class)) {
                 this.identifier = property;
             }
             else if(property.isAnnotatedWith(Version.class)) {
                 this.version = property;
             }
             else {
                 propertyMap.put(descriptor.getName(), property);
                 if(property.isPersistent()) {
                     persistentProperties.put(descriptor.getName(), property);
                 }
             }
         }
 
         this.constrainedProperties = (Map) new ConstraintsEvaluatingDynamicProperty(getPersistentProperties()).get(getReference().getWrappedInstance());
     }
 
     public boolean isOwningClass(Class domainClass) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public GrailsDomainClassProperty[] getProperties() {
         return propertiesArray;
     }
 
     public GrailsDomainClassProperty[] getPersistantProperties() {
         return getPersistentProperties();
     }
 
     public GrailsDomainClassProperty[] getPersistentProperties() {
         return persistentPropertyArray;
     }
 
     public GrailsDomainClassProperty getIdentifier() {
         return this.identifier;
     }
 
     public GrailsDomainClassProperty getVersion() {
         return this.version;
     }
 
     public Map getAssociationMap() {
         return Collections.EMPTY_MAP;
     }
 
     public GrailsDomainClassProperty getPropertyByName(String name) {
         return propertyMap.get(name);
     }
 
     public String getFieldName(String propertyName) {
         GrailsDomainClassProperty prop = getPropertyByName(propertyName);
         return prop != null ? prop.getFieldName() : null;
     }
 
     public boolean isOneToMany(String propertyName) {
         GrailsDomainClassProperty prop = getPropertyByName(propertyName);
         return prop!=null&&prop.isOneToMany();
     }
 
     public boolean isManyToOne(String propertyName) {
         GrailsDomainClassProperty prop = getPropertyByName(propertyName);
         return prop!=null&&prop.isManyToOne();
     }
 
     public boolean isBidirectional(String propertyName) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public Class getRelatedClassType(String propertyName) {
         GrailsDomainClassProperty prop = getPropertyByName(propertyName);
         return prop != null ? prop.getType() : null;
     }
 
     public Map getConstrainedProperties() {
         return this.constrainedProperties;
     }
 
     public Validator getValidator() {
         return this.validator;
     }
 
     public void setValidator(Validator validator) {
         this.validator=validator;
     }
 
     public String getMappingStrategy() {
         return "JPA";
     }
 
     public boolean isRoot() {
         return true;
     }
 
     public Set<GrailsDomainClass> getSubClasses() {
         return Collections.emptySet();
     }
 
     public void refreshConstraints() {
         // NOOP
     }
 
     public boolean hasSubClasses() {
         return false;
     }
 
     public Map getMappedBy() {
         return Collections.EMPTY_MAP;
     }
 
     public boolean hasPersistentProperty(String propertyName) {
         GrailsDomainClassProperty prop = getPropertyByName(propertyName);
         return prop != null && prop.isPersistent();
     }
 
     public void setMappingStrategy(String strategy) {
         // do nothing
     }
 
     private class JpaDomainClassProperty implements GrailsDomainClassProperty {
         private Class ownerClass;
         private PropertyDescriptor descriptor;
         private Field propertyField;
         private String name;
         private Class type;
         private GrailsDomainClass domainClass;
         private Method getter;
         private Column columnAnnotation;
         private boolean persistent;
         private Field field;
         private boolean version;
 
         public JpaDomainClassProperty(GrailsDomainClass domain, PropertyDescriptor descriptor) {
             this.ownerClass = domain.getClazz();
             this.domainClass = domain;
             this.descriptor = descriptor;
             this.name = descriptor.getName();
             this.type = descriptor.getPropertyType();
             this.getter = descriptor.getReadMethod();
             try {
                 this.field = domain.getClazz().getDeclaredField(descriptor.getName());
             } catch (NoSuchFieldException e) {
                 // ignore
             }
             this.columnAnnotation = getAnnotation(javax.persistence.Column.class);
             this.persistent = !isAnnotatedWith(Transient.class);
         }
 
         public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotation) {
             if(field==null) return null;
             return this.field.getAnnotation(annotation);
         }
 
         public int getFetchMode() {
             return FETCH_LAZY;
         }
 
         public String getName() {
             return this.name;
         }
 
         public Class getType() {
             return this.type;
         }
 
         public Class getReferencedPropertyType() {
             if(Collection.class.isAssignableFrom(getType())) {
                 final Type genericType = field.getGenericType();
                 if(genericType instanceof ParameterizedType) {
                     final Type[] arguments = ((ParameterizedType) genericType).getActualTypeArguments();
                     if(arguments.length>0)
                         return (Class) arguments[0];
                 }                
             }
             return getType();
         }
 
         public GrailsDomainClassProperty getOtherSide() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getTypePropertyName() {
             return GrailsNameUtils.getPropertyName(getType());
         }
 
         public GrailsDomainClass getDomainClass() {
             return domainClass;
         }
 
         public boolean isPersistent() {
             return persistent;
         }
 
         public boolean isOptional() {
             return columnAnnotation != null && columnAnnotation.nullable();
         }
 
         public boolean isIdentity() {
             return isAnnotatedWith(javax.persistence.Id.class);
         }
 
         public boolean isOneToMany() {
             return isAnnotatedWith(OneToMany.class);
         }
 
         public boolean isManyToOne() {
             return isAnnotatedWith(ManyToOne.class);
         }
 
         public boolean isManyToMany() {
             return isAnnotatedWith(ManyToMany.class);
         }
 
         public boolean isBidirectional() {
             return false;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getFieldName() {
             return getName().toUpperCase();
         }
 
         public boolean isOneToOne() {
             return isAnnotatedWith(OneToOne.class);
         }
 
		public boolean isHasOne() {
			return isAnnotatedWith(OneToOne.class);
		}

         public GrailsDomainClass getReferencedDomainClass() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public boolean isAssociation() {
             return isOneToMany()||isOneToOne()||isManyToOne()||isManyToMany();
         }
 
         public boolean isEnum() {
             return getType().isEnum();
         }
 
         public String getNaturalName() {
             return GrailsNameUtils.getNaturalName(getShortName());
         }
 
         public void setReferencedDomainClass(GrailsDomainClass referencedGrailsDomainClass) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void setOtherSide(GrailsDomainClassProperty referencedProperty) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public boolean isInherited() {
             return false;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public boolean isOwningSide() {
             return false;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public boolean isCircular() {
             return getType().equals(ownerClass);
         }
 
         public String getReferencedPropertyName() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public boolean isEmbedded() {
             return isAnnotatedWith(Embedded.class);
         }
 
         public GrailsDomainClass getComponent() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void setOwningSide(boolean b) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public boolean isBasicCollectionType() {
             return false;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public boolean isAnnotatedWith(Class annotation) {
             if(field==null) return false;
             return field.getAnnotation(annotation)!=null;
         }
     }
 }
