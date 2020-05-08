 //;-*- mode: java -*-
 /*
                         QueryJ
 
     Copyright (C) 2002-2007  Jose San Leandro Armendariz
                         chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  *****************************************************************************
  *
  * Filename: AbstractAttribute.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Abstract logicless implementation of Attribute interface.
  *
  */
 package org.acmsl.queryj.tools.metadata.vo;
 
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Arrays;
 
 /**
  * Abstract logic-less implementation of {@link Attribute} interface.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public abstract class AbstractAttribute
     implements Attribute
 {
     /**
      * The name.
      */
     private String m__strName;
 
     /**
      * The type.
      */
     private int m__iTypeId;
 
     /**
      * The type.
      */
     private String m__strType;
 
     /**
      * The table name.
      */
     private String m__strTableName;
 
     /**
      * The comment.
      */
     private String m__strComment;
 
     /**
      * Whether the attribute is managed externally or not.
      */
     private boolean m__bManagedExternally;
 
     /**
      * Whether the attribute allows null values or not.
      */
    private boolean m__bNullable = false;
 
     /**
      * The optional value.
      */
     private String m__strValue;
 
     /**
      * Whether the attribute is marked as read-only.
      */
     private boolean m__bReadOnly = false;
 
     /**
      * Whether the attribute is marked as boolean.
      */
     private boolean m__bBoolean = false;
 
     /**
      * The symbol for <code>true</code> values in boolean attributes.
      */
     private String m__strBooleanTrue;
 
     /**
      * The symbol for <code>false</code> values in boolean attributes.
      */
     private String m__strBooleanFalse;
 
     /**
      * The symbol for <code>null</code> values in boolean attributes.
      */
     private String m__strBooleanNull;
 
     /**
      * The stack trace to know who created me.
      */
     private final StackTraceElement[] m__aStackTrace =
         new RuntimeException("fake").getStackTrace();
 
     /**
      * Creates an <code>AbstractAttribute</code> with minimal information,
      * allowing lazy-loading mechanisms for non-essential information.
      * @param tableName the table name.
      * @param name the name.
      */
     protected AbstractAttribute(
         final String tableName, final String name)
     {
         immutableSetTableName(tableName);
         immutableSetName(name);
     }
 
     /**
      * Creates an <code>AbstractAttribute</code> with the following
      * information.
      * @param name the name.
      * @param typeId the type id.
      * @param type the type.
      * @param tableName the table name.
      * @param comment the comment.
      * @param managedExternally whether the attribute is managed externally.
      * @param allowsNull whether the attribute allows null values or not.
      * @param value the optional value.
      * @param readOnly whether the attribute is marked as read-only.
      * @param isBool whether the attribute is marked as boolean.
      * @param booleanTrue the symbol for <code>true</code> values in boolean attributes.
      * @param booleanFalse the symbol for <code>false</code> values in boolean attributes.
      * @param booleanNull the symbol for <code>null</code> values in boolean attributes.
      */
     protected AbstractAttribute(
         final String name,
         final int typeId,
         final String type,
         final String tableName,
         final String comment,
         final boolean managedExternally,
         final boolean allowsNull,
         final String value,
         final boolean readOnly,
         final boolean isBool,
         final String booleanTrue,
         final String booleanFalse,
         final String booleanNull)
     {
         this(tableName, name);
         immutableSetTypeId(typeId);
         immutableSetType(type);
         immutableSetComment(comment);
         immutableSetManagedExternally(managedExternally);
        immutableIsNullable(allowsNull);
         immutableSetValue(value);
         immutableSetReadOnly(readOnly);
         immutableSetBoolean(isBool);
         immutableSetBooleanTrue(booleanTrue);
         immutableSetBooleanFalse(booleanFalse);
         immutableSetBooleanNull(booleanNull);
     }
 
     /**
      * Specifies the name.
      * @param name such name.
      */
     protected final void immutableSetName(final String name)
     {
         m__strName = name;
     }
 
     /**
      * Specifies the name.
      * @param name such name.
      */
     protected void setName(final String name)
     {
         immutableSetName(name);
     }
 
     /**
      * Retrieves the attribute name.
      * @return such name.
      */
     public String getName()
     {
         return m__strName;
     }
 
     /**
      * Specifies the type.
      * @param type such information.
      */
     protected final void immutableSetTypeId(final int type)
     {
         m__iTypeId = type;
     }
 
     /**
      * Specifies the type.
      * @param type such information.
      */
     @SuppressWarnings("unused")
     protected void setTypeId(final int type)
     {
         immutableSetTypeId(type);
     }
 
     /**
      * Retrieves the attribute type.
      * @return its type.
      */
     public int getTypeId()
     {
         return m__iTypeId;
     }
 
     /**
      * Specifies the type.
      * @param type such information.
      */
     protected final void immutableSetType(final String type)
     {
         m__strType = type;
     }
 
     /**
      * Specifies the native type.
      * @param type such information.
      */
     @SuppressWarnings("unused")
     protected void setType(final String type)
     {
         immutableSetType(type);
     }
 
     /**
      * Retrieves the type.
      * @return such information.
      */
     public String getType()
     {
         return m__strType;
     }
 
     /**
      * Specifies the table name.
      * @param tableName such information.
      */
     protected final void immutableSetTableName(final String tableName)
     {
         m__strTableName = tableName;
     }
 
     /**
      * Specifies the table name.
      * @param tableName such information.
      */
     protected void setTableName(final String tableName)
     {
         immutableSetTableName(tableName);
     }
 
     /**
      * Retrieves the table name.
      * @return such information.
      */
     public String getTableName()
     {
         return m__strTableName;
     }
 
     /**
      * Specifies the comment.
      * @param comment such comment.
      */
     protected final void immutableSetComment(final String comment)
     {
         m__strComment = comment;
     }
 
     /**
      * Specifies the comment.
      * @param comment such comment.
      */
     @SuppressWarnings("unused")
     protected void setComment(final String comment)
     {
         immutableSetComment(comment);
     }
 
     /**
      * Retrieves the attribute comment.
      * @return such comment.
      */
     public String getComment()
     {
         return m__strComment;
     }
 
     /**
      * Specifies whether it's managed externally.
      * @param managedExternally such information.
      */
     protected final void immutableSetManagedExternally(
         final boolean managedExternally)
     {
         m__bManagedExternally = managedExternally;
     }
 
     /**
      * Specifies whether it's managed externally.
      * @param managedExternally such information.
      */
     @SuppressWarnings("unused")
     protected void setManagedExternally(final boolean managedExternally)
     {
         immutableSetManagedExternally(managedExternally);
     }
 
     /**
      * Retrieves whether it's managed externally.
      * @return such information.
      */
     public boolean getManagedExternally()
     {
         return m__bManagedExternally;
     }
 
     /**
      * Specifies whether the attribute allows null values or not.
      * @param allowsNull such information.
      */
    protected final void immutableIsNullable(final boolean allowsNull)
     {
        m__bNullable = allowsNull;
     }
 
     /**
      * Specifies whether the attribute allows null values or not.
      * @param allowsNull such information.
      */
     @SuppressWarnings("unused")
    protected void setIsNullable(final boolean allowsNull)
     {
        immutableIsNullable(allowsNull);
     }
 
     /**
      * Retrieves whether it allows null values or not.
      * @return such information.
      */
    public boolean isNullable()
     {
        return m__bNullable;
     }
 
     /**
      * Specifies the optional attribute's value, meaning
      * it doesn't just describe the metadata, but also
      * contains data.
      * @param value such information.
      */
     protected final void immutableSetValue(final String value)
     {
         m__strValue = value;
     }
 
     /**
      * Specifies the optional attribute's value, meaning
      * it doesn't just describe the metadata, but also
      * contains data.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setValue(final String value)
     {
         immutableSetValue(value);
     }
 
     /**
      * Retrieves the optional attribute's value, meaning
      * it doesn't just describe the metadata, but also
      * contains data.
      * @return such information.
      */
     public String getValue()
     {
         return m__strValue;
     }
 
     /**
      * Specifies whether the attribute is marked as read-only.
      * @param flag such condition.
      */
     protected final void immutableSetReadOnly(final boolean flag)
     {
         m__bReadOnly = flag;
     }
 
     /**
      * Specifies whether the attribute is marked as read-only.
      * @param flag such condition.
      */
     @SuppressWarnings("unused")
     protected void setReadOnly(final boolean flag)
     {
         immutableSetReadOnly(flag);
     }
 
     /**
      * Retrieves whether the attribute is marked as read-only.
      * @return such condition.
      */
     public boolean isReadOnly()
     {
         return m__bReadOnly;
     }
 
     /**
      * Specifies whether the attribute is marked as boolean.
      * @param flag such condition.
      */
     protected final void immutableSetBoolean(final boolean flag)
     {
         m__bBoolean = flag;
     }
 
     /**
      * Specifies whether the attribute is marked as boolean.
      * @param flag such condition.
      */
     @SuppressWarnings("unused")
     protected void setBoolean(final boolean flag)
     {
         immutableSetBoolean(flag);
     }
 
     /**
      * Retrieves whether the attribute is marked as boolean.
      * @return such condition.
      */
     public boolean isBoolean()
     {
         return m__bBoolean;
     }
 
     /**
      * Specifies the symbol for <code>true</code> values.
      * @param value such information.
      */
     protected final void immutableSetBooleanTrue(final String value)
     {
         m__strBooleanTrue = value;
     }
 
     /**
      * Specifies the symbol for <code>true</code> values.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setBooleanTrue(final String value)
     {
         immutableSetBooleanTrue(value);
     }
 
     /**
      * Retrieves the symbol for <code>true</code> values.
      * @return such information.
      */
     public String getBooleanTrue()
     {
         return m__strBooleanTrue;
     }
 
     /**
      * Specifies the symbol for <code>false</code> values.
      * @param value such information.
      */
     protected final void immutableSetBooleanFalse(final String value)
     {
         m__strBooleanFalse = value;
     }
 
     /**
      * Specifies the symbol for <code>false</code> values.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setBooleanFalse(final String value)
     {
         immutableSetBooleanFalse(value);
     }
 
     /**
      * Retrieves the symbol for <code>false</code> values.
      * @return such information.
      */
     public String getBooleanFalse()
     {
         return m__strBooleanFalse;
     }
 
     /**
      * Specifies the symbol for <code>null</code> values.
      * @param value such information.
      */
     protected final void immutableSetBooleanNull(final String value)
     {
         m__strBooleanNull = value;
     }
 
     /**
      * Specifies the symbol for <code>null</code> values.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setBooleanNull(final String value)
     {
         immutableSetBooleanNull(value);
     }
 
     /**
      * Retrieves the symbol for <code>null</code> values.
      * @return such information.
      */
     public String getBooleanNull()
     {
         return m__strBooleanNull;
     }
 
     /**
      * Retrieves the stack trace when the attribute was created.
      * @return such information.
      */
     @SuppressWarnings("unused")
     protected StackTraceElement[] getStackTrace()
     {
         return m__aStackTrace;
     }
 
     /**
      * Retrieves the attribute name.
      * @return such information.
      */
     public String toString()
     {
         return getName();
     }
 
     /**
      * Compares given object with this instance.
      * @param object the object to compare to.
      * @return the result of such comparison.
      * @throws ClassCastException if the type of the specified
      * object prevents it from being compared to this Object.
      */
     public int compareTo(final Object object)
         throws  ClassCastException
     {
         int result = 1;
 
         if (   (this == object)
             || (this.equals(object)))
         {
             result = 0;
         }
         else if  (object instanceof Attribute)
         {
             @NotNull final Attribute t_OtherInstance = (Attribute) object;
 
             result =
                 new org.apache.commons.lang.builder.CompareToBuilder()
                     .append(
                         getName(),
                         t_OtherInstance.getName())
                     .append(
                         getTypeId(),
                         t_OtherInstance.getTypeId())
                     .append(
                         getType(),
                         t_OtherInstance.getType())
                      .append(
                          getTableName(),
                          t_OtherInstance.getTableName())
                      .append(
                          getComment(),
                          t_OtherInstance.getComment())
                      .append(
                          getManagedExternally(),
                          t_OtherInstance.getManagedExternally())
                      .append(
                          getAllowsNull(),
                          t_OtherInstance.getAllowsNull())
                      .append(
                          getValue(),
                          t_OtherInstance.getValue())
                      .append(
                          isReadOnly(),
                          t_OtherInstance.isReadOnly())
                      .append(
                          isBoolean(),
                          t_OtherInstance.isBoolean())
                      .append(
                          getBooleanTrue(),
                          t_OtherInstance.getBooleanTrue())
                      .append(
                          getBooleanFalse(),
                          t_OtherInstance.getBooleanFalse())
                      .append(
                          getBooleanNull(),
                          t_OtherInstance.getBooleanNull())
                     .toComparison();
         }
 
         return result;
     }
 
     @Override
     public boolean equals(final Object o)
     {
         if (this == o)
         {
             return true;
         }
         if (o == null || getClass() != o.getClass())
         {
             return false;
         }
 
         @NotNull final AbstractAttribute that = (AbstractAttribute) o;
 
        if (m__bNullable != that.m__bNullable)
         {
             return false;
         }
         if (m__bBoolean != that.m__bBoolean)
         {
             return false;
         }
         if (m__bManagedExternally != that.m__bManagedExternally)
         {
             return false;
         }
         if (m__bReadOnly != that.m__bReadOnly)
         {
             return false;
         }
         if (m__iTypeId != that.m__iTypeId)
         {
             return false;
         }
         if (!Arrays.equals(m__aStackTrace, that.m__aStackTrace))
         {
             return false;
         }
         if (m__strBooleanFalse != null ? !m__strBooleanFalse.equals(that.m__strBooleanFalse)
                                        : that.m__strBooleanFalse != null)
         {
             return false;
         }
         if (m__strBooleanNull != null ? !m__strBooleanNull.equals(that.m__strBooleanNull)
                                       : that.m__strBooleanNull != null)
         {
             return false;
         }
         if (m__strBooleanTrue != null ? !m__strBooleanTrue.equals(that.m__strBooleanTrue)
                                       : that.m__strBooleanTrue != null)
         {
             return false;
         }
         if (m__strComment != null ? !m__strComment.equals(that.m__strComment) : that.m__strComment != null)
         {
             return false;
         }
         if (m__strType != null ? !m__strType.equals(that.m__strType) : that.m__strType != null)
         {
             return false;
         }
         if (m__strName != null ? !m__strName.equals(that.m__strName) : that.m__strName != null)
         {
             return false;
         }
         if (m__strTableName != null ? !m__strTableName.equals(that.m__strTableName) : that.m__strTableName != null)
         {
             return false;
         }
 
         return !(m__strValue != null ? !m__strValue.equals(that.m__strValue) : that.m__strValue != null);
 
     }
 
     @Override
     public int hashCode()
     {
         int result = m__strName != null ? m__strName.hashCode() : 0;
         result = 31 * result + m__iTypeId;
         result = 31 * result + (m__strType != null ? m__strType.hashCode() : 0);
         result = 31 * result + (m__strTableName != null ? m__strTableName.hashCode() : 0);
         result = 31 * result + (m__strComment != null ? m__strComment.hashCode() : 0);
         result = 31 * result + (m__bManagedExternally ? 1 : 0);
         result = 31 * result + (m__bAllowsNull ? 1 : 0);
         result = 31 * result + (m__strValue != null ? m__strValue.hashCode() : 0);
         result = 31 * result + (m__bReadOnly ? 1 : 0);
         result = 31 * result + (m__bBoolean ? 1 : 0);
         result = 31 * result + (m__strBooleanTrue != null ? m__strBooleanTrue.hashCode() : 0);
         result = 31 * result + (m__strBooleanFalse != null ? m__strBooleanFalse.hashCode() : 0);
         result = 31 * result + (m__strBooleanNull != null ? m__strBooleanNull.hashCode() : 0);
         result = 31 * result + (m__aStackTrace != null ? Arrays.hashCode(m__aStackTrace) : 0);
         return result;
     }
 }
