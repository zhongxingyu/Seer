 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.external;
 
 import org.apache.commons.io.input.AutoCloseInputStream;
 import org.apache.jackrabbit.value.BinaryImpl;
 import org.jahia.services.content.nodetypes.ExtendedNodeType;
 import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
 import org.jahia.services.content.nodetypes.Name;
 import javax.jcr.*;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.PropertyDefinition;
 import javax.jcr.version.VersionException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Map;
 
 /**
  * Implementation of the {@link javax.jcr.Property} for the {@link org.jahia.modules.external.ExternalData}.
  * User: toto
  * Date: Apr 23, 2008
  * Time: 11:46:28 AM
  *
  */
 public class ExternalPropertyImpl extends ExternalItemImpl implements Property {
 
     private ExternalNodeImpl node;
     private Name name;
     private Value[] values;
     private Value value;
 
     public ExternalPropertyImpl(Name name, ExternalNodeImpl node, ExternalSessionImpl session, Value value) {
         super(session);
         this.name = name;
         this.node = node;
         this.value = value;
     }
 
     public ExternalPropertyImpl(Name name, ExternalNodeImpl node, ExternalSessionImpl session, Value[] values) {
         super(session);
         this.name = name;
         this.node = node;
         this.values = values;
     }
     public ExternalPropertyImpl(Name name, ExternalNodeImpl node, ExternalSessionImpl session) {
         super(session);
         this.name = name;
         this.node = node;
     }
 
     public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         this.value = value;
     }
 
     public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         this.values = values;
     }
 
     public void setValue(String s) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (s != null) {
             setValue(getSession().getValueFactory().createValue(s));
         } else {
             remove();
         }
     }
 
     public void setValue(String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (strings != null) {
             Value[] v = new Value[strings.length];
             for (int i = 0; i < strings.length; i++) {
                 if (strings[i] != null) {
                     v[i] = getSession().getValueFactory().createValue(strings[i]);
                 } else {
                     v[i] = null;
                 }
             }
             setValue(v);
         } else {
             remove();
         }
     }
 
     public void setValue(Binary[] binaries) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (binaries != null) {
             Value[] v = new Value[binaries.length];
             for (int i = 0; i < binaries.length; i++) {
                 if (binaries[i] != null) {
                     v[i] = getSession().getValueFactory().createValue(binaries[i]);
                 } else {
                     v[i] = null;
                 }
             }
             setValue(v);
         } else {
             remove();
         }
     }
 
     public void setValue(InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (inputStream != null) {
             Binary b = null;
             try {
                 b = new BinaryImpl(inputStream);
                 setValue(getSession().getValueFactory().createValue(b));
             } catch (IOException e) {
                 throw new RepositoryException(e);
             } finally {
                 if (b != null ) {
                     b.dispose();
                 }
             }
         } else {
             remove();
         }
     }
 
     public void setValue(long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         setValue(getSession().getValueFactory().createValue(l));
     }
 
     public void setValue(double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         setValue(getSession().getValueFactory().createValue(v));
     }
 
     public void setValue(Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (calendar != null) {
             setValue(getSession().getValueFactory().createValue(calendar));
         } else {
             remove();
         }
     }
 
     public void setValue(boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         setValue(getSession().getValueFactory().createValue(b));
     }
 
     public void setValue(Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public Value getValue() throws ValueFormatException, RepositoryException {
         if (isMultiple()) {
             throw new ValueFormatException(this + " is a multi-valued property,"
                     + " so it's values can only be retrieved as an array");
         }
         return value;
     }
 
     public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (value != null) {
             setValue(getSession().getValueFactory().createValue(value));
         } else {
             remove();
         }
     }
 
     public Value[] getValues() throws ValueFormatException, RepositoryException {
         if (!isMultiple()) {
             throw new ValueFormatException(this + " is a single-valued property,"
                     + " so it's value can not be retrieved as an array");
         }
         return values;
     }
 
     public String getString() throws ValueFormatException, RepositoryException {
         if (value != null) {
             return value.getString();
         }
         return null;
     }
 
     public InputStream getStream() throws ValueFormatException, RepositoryException {
         if (value != null) {
             final Binary binary = value.getBinary();
             return new AutoCloseInputStream(binary.getStream()) {
                 @Override
                 public void close() throws IOException {
                     super.close();
                     binary.dispose();
                 }
             };
         }
         return null;
     }
 
     public long getLong() throws ValueFormatException, RepositoryException {
         if (value != null) {
             return value.getLong();
         }
         return 0;
     }
 
     public double getDouble() throws ValueFormatException, RepositoryException {
         if (value != null) {
             return value.getDouble();
         }
         return 0;
     }
 
     public Calendar getDate() throws ValueFormatException, RepositoryException {
         if (value != null) {
             return value.getDate();
         }
         return null;
     }
 
     public boolean getBoolean() throws ValueFormatException, RepositoryException {
         if (value != null) {
             return value.getBoolean();
         }
         return false;
     }
 
     public Node getNode() throws ValueFormatException, RepositoryException {
         return null;
     }
 
     public long getLength() throws ValueFormatException, RepositoryException {
         return getLength(getValue());
     }
 
     protected long getLength(Value value) throws ValueFormatException, RepositoryException {
         return PropertyType.BINARY == value.getType() ? value.getBinary().getSize() : value.getString().length();
     }
 
     public long[] getLengths() throws ValueFormatException, RepositoryException {
         long[] lengths = new long[getValues().length];
         for (int i = 0; i < values.length; i++) {
             lengths[i] = getLength(values[i]);
         }
         return lengths;
     }
 
     public PropertyDefinition getDefinition() throws RepositoryException {
         ExtendedNodeType parentNodeType = node.getExtendedPrimaryNodeType();
         ExtendedPropertyDefinition propertyDefinition = parentNodeType.getPropertyDefinitionsAsMap().get(getName());
         if (propertyDefinition != null) {
             return propertyDefinition;
         }
         for (Map.Entry<Integer, ExtendedPropertyDefinition> entry : parentNodeType.getUnstructuredPropertyDefinitions().entrySet()) {
             if (entry.getKey() == getType()) {
                 return entry.getValue();
             }
         }
         return null;
     }
 
     public int getType() throws RepositoryException {
        if (isMultiple()) {
            return getValues()[0].getType();
        }
        return getValue().getType();
     }
 
     public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (value != null) {
             if (getBinary() != null) {
                 getBinary().dispose();
             }
             setValue(getSession().getValueFactory().createValue(value));
         } else {
             remove();
         }
     }
 
     public Binary getBinary() throws ValueFormatException, RepositoryException {
         return value.getBinary();
     }
 
     public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
         return value.getDecimal();
     }
 
     public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
         return this;
     }
 
     public boolean isMultiple() throws RepositoryException {
         if (values != null) {
             return true;
         }
         return false;
     }
 
     public String getPath() throws RepositoryException {
         return getParent().getPath() + "/" + name;
     }
 
     public String getName() throws RepositoryException {
         return name.toString();
     }
 
     public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
         return node;
     }
 
     @Override
     public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         ((ExternalNodeImpl) getParent()).removeProperty(getName());
     }
 }
