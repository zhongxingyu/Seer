 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
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
  *******************************************************************************/
 package org.richfaces.tests.metamer;
 
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import javax.faces.model.SelectItem;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Representation an attribute of a JSF component.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @XmlRootElement(name = "property", namespace = "http://java.sun.com/xml/ns/javaee")
 public class Attribute implements Serializable {
 
     private static final long serialVersionUID = -6716974687380275186L;
     private Logger logger;
     private String name;
     private Object value;
     private Class<?> type;
     private String help;
     private List<SelectItem> selectOptions;
     private Extensions extensions;
 
     public Attribute() {
         logger = LoggerFactory.getLogger(getClass());
     }
 
     public Attribute(String name) {
         logger = LoggerFactory.getLogger(getClass());
         this.name = name;
     }
 
     public Attribute(Attribute attr) {
         this.name = attr.name;
         this.value = null;
         this.type = attr.type;
         this.help = attr.help;
         this.selectOptions = attr.selectOptions;
         this.extensions = attr.extensions;
     }
 
     public Object getValue() {
         return value;
     }
 
     public void setValue(Object value) {
         if (value instanceof String) {
             if (type == Collection.class) {
                String[] splitted = StringUtils.split((String) value, ",[]");
                value = new ArrayList<String>(Arrays.asList(splitted));
             }
         }
         this.value = value;
     }
 
     @XmlElement(name = "description", namespace = "http://java.sun.com/xml/ns/javaee")
     public String getHelp() {
         return help;
     }
 
     public void setHelp(String help) {
         this.help = help;
     }
 
     @XmlElement(name = "property-name", namespace = "http://java.sun.com/xml/ns/javaee")
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @XmlElement(name = "property-extension", namespace = "http://java.sun.com/xml/ns/javaee")
     public Extensions getExtensions() {
         return extensions;
     }
 
     public void setExtensions(Extensions extensions) {
         this.extensions = extensions;
     }
 
     public List<SelectItem> getSelectOptions() {
         return selectOptions;
     }
 
     public void setSelectOptions(List<SelectItem> selectOptions) {
         this.selectOptions = selectOptions;
     }
 
     @XmlElement(name = "property-class", namespace = "http://java.sun.com/xml/ns/javaee")
     @XmlJavaTypeAdapter(value = JavaTypeAdapter.class)
     public Class<?> getType() {
         return type;
     }
 
     public void setType(Class<?> type) {
         this.type = type;
     }
 
     public boolean isBool() {
         return type == Boolean.class || type == boolean.class;
     }
 
     public boolean isGenerate() {
         return extensions.getGenerate();
     }
 
     public boolean isHidden() {
         if (extensions == null) {
             return false;
         }
         return extensions.getHidden();
     }
 
     public boolean isLiteral() {
         return extensions.getLiteral();
     }
 
     public boolean isPassThrough() {
         return extensions.getPassThrough();
     }
 
     public boolean isReadOnly() {
         return extensions.getReadOnly();
     }
 
     public boolean isRequired() {
         return extensions.getRequired();
     }
 }
