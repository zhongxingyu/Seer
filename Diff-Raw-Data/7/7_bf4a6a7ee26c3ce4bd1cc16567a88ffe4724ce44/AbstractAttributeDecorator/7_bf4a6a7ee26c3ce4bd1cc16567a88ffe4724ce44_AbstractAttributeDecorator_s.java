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
  * Filename: AbstractAttributeDecorator.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Decorates 'Attribute' instances to provide required
  *              alternate representations of the information stored therein.
  *
  */
 package org.acmsl.queryj.metadata;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.SingularPluralFormConverter;
 import org.acmsl.queryj.metadata.vo.AbstractAttribute;
 import org.acmsl.queryj.metadata.vo.Attribute;
 
 /*
  * Importing some ACM-SL Commons classes.
  */
 import org.acmsl.commons.utils.EnglishGrammarUtils;
 
 /*
 * Importing some JetBrains annotations.
  */
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /*
  * Importing some JDK classes.
  */
 import java.util.Locale;
 
 /**
  * Decorates <code>Attribute</code> instances to provide required alternate
  * representations of the information stored therein.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public abstract class AbstractAttributeDecorator
     extends  AbstractAttribute
     implements  AttributeDecorator
 {
     /**
      * The decorated attribute.
      */
     private Attribute m__Attribute;
 
     /**
      * The metadata type manager.
      */
     private MetadataManager m__MetadataManager;
 
     /**
      * The metadata type manager.
      */
     private MetadataTypeManager m__MetadataTypeManager;
 
     /**
      * Creates an <code>AttributeDecorator</code> with the
      * <code>Attribute</code> to decorate.
      * @param attribute the attribute.
      * @param metadataManager the metadata manager.
      */
     public AbstractAttributeDecorator(
         @NotNull final Attribute attribute, @NotNull final MetadataManager metadataManager)
     {
         this(
             attribute.getName(),
             attribute.getTypeId(),
             attribute.getType(),
             attribute.getTableName(),
             attribute.getComment(),
             attribute.getOrdinalPosition(),
             attribute.getLength(),
             attribute.getPrecision(),
             attribute.getKeyword(),
             attribute.getRetrievalQuery(),
             attribute.isNullable(),
             attribute.getValue(),
             attribute.isReadOnly(),
             attribute.isBoolean(),
             attribute.getBooleanTrue(),
             attribute.getBooleanFalse(),
             attribute.getBooleanNull(),
             attribute,
             metadataManager,
             metadataManager.getMetadataTypeManager());
     }
 
     /**
      * Creates an <code>AttributeDecorator</code> with the following
      * information.
      * @param name the name.
      * @param typeId the type id.
      * @param type the type.
      * @param tableName the table name.
      * @param comment the attribute comment.
      * @param keyword the keyword used to retrieve the value, if any.
      * @param retrievalQuery the query used to retrieve the value, if any.
      * @param allowsNull whether the attribute allows null values or not.
      * @param value the optional attribute value.
      * @param readOnly whether the attribute is marked as read-only.
      * @param isBool whether the attribute is marked as boolean.
      * @param attribute the attribute.
      * @param booleanTrue the symbol for <code>true</code> values in boolean attributes.
      * @param booleanFalse the symbol for <code>false</code> values in boolean attributes.
      * @param booleanNull the symbol for <code>null</code> values in boolean attributes.
      * @param metadataManager the metadata manager.
      * @param metadataTypeManager the metadata type manager.
      */
     public AbstractAttributeDecorator(
         @NotNull final String name,
         final int typeId,
         @NotNull final String type,
         @NotNull final String tableName,
         @Nullable final String comment,
         final int ordinalPosition,
         final int length,
         final int precision,
         @Nullable final String keyword,
         @Nullable final String retrievalQuery,
         final boolean allowsNull,
         @Nullable final String value,
         final boolean readOnly,
         final boolean isBool,
         @Nullable final String booleanTrue,
         @Nullable final String booleanFalse,
         @Nullable final String booleanNull,
         @NotNull final Attribute attribute,
         @NotNull final MetadataManager metadataManager,
         @NotNull final MetadataTypeManager metadataTypeManager)
     {
         super(
             name,
             typeId,
             type,
             tableName,
             comment,
             ordinalPosition,
             length,
             precision,
             keyword,
             retrievalQuery,
             allowsNull,
             value,
             readOnly,
             isBool,
             booleanTrue,
             booleanFalse,
             booleanNull);
 
         immutableSetAttribute(attribute);
         immutableSetMetadataManager(metadataManager);
         immutableSetMetadataTypeManager(metadataTypeManager);
     }
 
     /**
      * Specifies the attribute to decorate.
      * @param attribute such attribute.
      */
     protected final void immutableSetAttribute(@NotNull final Attribute attribute)
     {
         m__Attribute = attribute;
     }
 
     /**
      * Specifies the attribute to decorate.
      * @param attribute such attribute.
      */
     @SuppressWarnings("unused")
     protected void setAttribute(@NotNull final Attribute attribute)
     {
         immutableSetAttribute(attribute);
     }
 
     /**
      * Retrieves the decorated attribute.
      * @return such attribute.
      */
     @NotNull
     public Attribute getAttribute()
     {
         return m__Attribute;
     }
 
     /**
      * Specifies the metadata manager.
      * @param metadataManager such instance.
      */
     protected final void immutableSetMetadataManager(
         @NotNull final MetadataManager metadataManager)
     {
         m__MetadataManager = metadataManager;
     }
 
     /**
      * Specifies the metadata manager.
      * @param metadataManager such instance.
      */
     @SuppressWarnings("unused")
     protected void setMetadataManager(
         @NotNull final MetadataManager metadataManager)
     {
         immutableSetMetadataManager(metadataManager);
     }
 
     /**
      * Retrieves the metadata manager.
      * @return such instance.
      */
     @NotNull
     protected MetadataManager getMetadataManager()
     {
         return m__MetadataManager;
     }
 
     /**
      * Specifies the metadata type manager.
      * @param metadataTypeManager such instance.
      */
     protected final void immutableSetMetadataTypeManager(
         @NotNull final MetadataTypeManager metadataTypeManager)
     {
         m__MetadataTypeManager = metadataTypeManager;
     }
 
     /**
      * Specifies the metadata type manager.
      * @param metadataTypeManager such instance.
      */
     @SuppressWarnings("unused")
     protected void setMetadataTypeManager(
         @NotNull final MetadataTypeManager metadataTypeManager)
     {
         immutableSetMetadataTypeManager(metadataTypeManager);
     }
 
     /**
      * Retrieves the metadata type manager.
      * @return such instance.
      */
     @NotNull
     protected MetadataTypeManager getMetadataTypeManager()
     {
         return m__MetadataTypeManager;
     }
 
     /**
      * Retrieves the name, in upper case.
      * @return such value.
      */
     @NotNull
     public String getNameUppercased()
     {
         return uppercase(getName(), DecorationUtils.getInstance());
     }
 
     /**
      * Retrieves the capitalized name.
      * @return such name.
      */
     @NotNull
     public String getNameCapitalized()
     {
         return capitalize(getName(), DecorationUtils.getInstance());
     }
 
     /**
      * Capitalizes given value.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the alternate version of the value.
      */
     @NotNull
     protected String capitalize(
         @NotNull final String value, @NotNull final DecorationUtils decorationUtils)
     {
         @NotNull final Locale t_Locale = Locale.US;
 
         return decorationUtils.capitalize(value.toLowerCase(t_Locale));
     }
 
     /**
      * Retrieves the uncapitalized name.
      * @return such name.
      */
     @NotNull
     public String getNameUncapitalized()
     {
         return uncapitalize(getName(), DecorationUtils.getInstance());
     }
 
     /**
      * Uncapitalizes given value.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the alternate version of the value.
      */
     @NotNull
     protected String uncapitalize(
         @NotNull final String value, @NotNull final DecorationUtils decorationUtils)
     {
         @NotNull final Locale t_Locale = Locale.US;
 
         return decorationUtils.uncapitalize(value.toLowerCase(t_Locale));
     }
 
     /**
      * Converts given value to upper-case.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the alternate version of the value.
      */@NotNull
     protected String uppercase(
         @NotNull final String value, @NotNull final DecorationUtils decorationUtils)
     {
         return decorationUtils.upperCase(value);
     }
 
     /**
      * Normalizes given value to lower-case.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the alternate version of the value.
      */
     @NotNull
     protected String normalizeLowercase(
         @NotNull final String value, @NotNull final DecorationUtils decorationUtils)
     {
         return decorationUtils.normalizeLowercase(value);
     }
 
     /**
      * Retrieves the name, in lower case.
      * @return such value.
      */
     @NotNull
     public String getNameLowercased()
     {
         return lowercase(getName(), DecorationUtils.getInstance());
     }
 
     /**
      * Converts given value to lower-case.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the alternate version of the value.
      */
     @NotNull
     protected String lowercase(
         @NotNull final String value, @NotNull final DecorationUtils decorationUtils)
     {
         return decorationUtils.lowerCase(value);
     }
 
     /**
      * Retrieves the table name, uncapitalized.
      * @return such value.
      */
     @NotNull
     public String getUncapitalizedTableName()
     {
         return uncapitalize(getTableName(), DecorationUtils.getInstance());
     }
 
     /**
      * Retrieves the value-object name associated to the table name.
      * @return such name.
      */
     @NotNull
     public String getVoName()
     {
         return capitalize(getSingular(getTableName()), DecorationUtils.getInstance());
     }
 
     /**
      * Retrieves the singular of given word.
      * @param word the word.
      * @return the singular.
      */
     @NotNull
     protected String getSingular(@NotNull final String word)
     {
         return getSingular(word, SingularPluralFormConverter.getInstance());
     }
 
     /**
      * Retrieves the singular of given word.
      * @param word the word.
      * @param singularPluralFormConverter the
      * <code>SingularPluralFormConverter</code> instance.
      * @return the singular.
      */
     @NotNull
     protected String getSingular(
         @NotNull final String word,
         @NotNull final EnglishGrammarUtils singularPluralFormConverter)
     {
         return singularPluralFormConverter.getSingular(word);
     }
 
     /**
      * Retrieves the attribute's Java name.
      * @return such information.
      */
     @NotNull
     public String getJavaName()
     {
         return uppercase(getName(), DecorationUtils.getInstance());
     }
 
     /**
      * Retrieves the attribute's associated getter method.
      * @return such information.
      */
     @NotNull
     public String getGetterMethod()
     {
         return getGetterMethod(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves the attribute's associated getter method.
      * @param type the attribute type.
      * @param metadataTypeManager the metadata type manager.
      * @return such information.
      */
     @NotNull
     protected String getGetterMethod(
         final int type, final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.getGetterMethod(type);
     }
 
     /**
      * Retrieves whether this attribute can be modelled as a primitive or not.
      * @return <code>false</code> if no primitive matches.
      */
     @Nullable
     public Boolean isPrimitive()
     {
         return isPrimitive(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether this attribute can be modelled as a primitive or not.
      * @param type the attribute type.
      * @param metadataTypeManager the metadata type manager.
      * @return <code>false</code> if no primitive matches.
      */
     @Nullable
     protected Boolean isPrimitive(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return
             (metadataTypeManager.isPrimitive(type))
             ?  Boolean.TRUE : Boolean.FALSE;
     }
 
     /**
      * Retrieves the object type.
      * @return such information.
      */
     @NotNull
     public String getObjectType()
     {
         return getObjectType(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves the attribute's object type.
      * @param type the attribute type.
      * @param metadataTypeManager the metadata type manager.
      * @return such type.
      */
     @NotNull
     protected String getObjectType(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.getSmartObjectType(type, isBoolean());
     }
 
     /**
      * Retrieves whether the attribute is a blob or not.
      * return such information.
      */
     @SuppressWarnings("unused")
     public boolean isBlob()
     {
         return isBlob(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether the attribute is a blob or not.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * return such information.
      */
     protected boolean isBlob(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.isBlob(type);
     }
 
     /**
      * Retrieves whether the attribute is a clob or not.
      * return such information.
      */
     public boolean isClob()
     {
         return isClob(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether the attribute is a clob or not.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * return such information.
      */
     protected boolean isClob(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.isClob(type);
     }
 
     /**
      * Retrieves whether the attribute is a string or not.
      * return such information.
      */
     public boolean isString()
     {
         return isString(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether the attribute is a string or not.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * return such information.
      */
     protected boolean isString(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.isString(type);
     }
 
     /**
      * Retrieves whether the attribute is a date or not.
      * return such information.
      */
     public boolean isDate()
     {
         return isDate(getType(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether the attribute is a date or not.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * return such information.
      */
     protected boolean isDate(
         final String type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.isDate(type);
     }
 
     /**
      * Retrieves whether the attribute is a timestamp or not.
      * return such information.
      */
     @SuppressWarnings("unused")
     public boolean isTimestamp()
     {
         return isTimestamp(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether the attribute is a timestamp or not.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * return such information.
      */
     protected boolean isTimestamp(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.isTimestamp(type);
     }
 
     /**
      * Retrieves the query to retrieve the externally-managed value.
      * @return such information.
      */
     @Nullable
     public String getQuery()
     {
         return getQuery(isExternallyManaged());
     }
 
     /**
      * Retrieves the query to retrieve the externally-managed value.
      * @param managedExternally whether the attribute is managed externally.
      * @return such information.
      */
     @Nullable
     protected String getQuery(final boolean managedExternally)
     {
         String result = "";
 
         if  (managedExternally)
         {
             result =
                 getQuery(getName(), getTableName(), getMetadataManager());
         }
 
         return result;
     }
 
     /**
      * Retrieves the query to retrieve the externally-managed value.
      * @param name the field name.
      * @param tableName the table name.
      * @param metadataManager the <code>MetadataManager</code> instance.
      * @return such information.
      */
     @SuppressWarnings("unused")
     @Nullable
     protected String getQuery(
         @NotNull final String name,
         @NotNull final String tableName,
         @NotNull final MetadataManager metadataManager)
     {
         return null;
         // TODO    metadataManager. getExternallyManagedFieldRetrievalQuery(
         //    tableName, name);
 
     }
 
     /**
      * Retrieves the QueryJ type.
      * @return the QueryJ type.
      */
     @NotNull
     public String getQueryJFieldType()
     {
         return getQueryJFieldType(getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves the QueryJ type.
      * @param type the type.
      * @param metadataTypeManager the metadata type manager.
      * @return the QueryJ type.
      */
     @NotNull
     protected String getQueryJFieldType(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.getQueryJFieldType(type, isBoolean());
     }
 
     /**
      * Retrieves the QueryJ type for statement setters.
      * @return the QueryJ type.
      */
     @NotNull
     public String getStatementSetterFieldType()
     {
         return
             getStatementSetterFieldType(
                 getTypeId(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves the QueryJ type for statement setters.
      * @param type the type.
      * @param metadataTypeManager the metadata type manager.
      * @return the QueryJ type.
      */
     @NotNull
     protected String getStatementSetterFieldType(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.getStatementSetterFieldType(type);
     }
 
     /**
      * Retrieves the attribute's table in upper-case.
      * @return such information.
      */
     @NotNull
     public String getTableNameUppercased()
     {
         return uppercase(getTableName(), DecorationUtils.getInstance());
     }
 
     /**
      * Retrieves the attribute's table in upper-case.
      * @return such information.
      */
     @NotNull
     public String getTableNameNormalizedLowercased()
     {
         return
             normalizeLowercase(getTableName(), DecorationUtils.getInstance());
     }
 
     /**
      * Retrieves whether the type means the attribute is a
      * number smaller than an int.
      * @return such condition.
      */
     public boolean isNumberSmallerThanInt()
     {
         return isNumberSmallerThanInt(getTypeId(), getMetadataManager());
     }
 
     /**
      * Retrieves whether the type means the attribute is a
      * number smaller than an int.
      * @param type the type.
      * @param metadataManager the <code>MetadataManager</code> instance.
      * @return such condition.
      */
     protected boolean isNumberSmallerThanInt(
         final int type, @NotNull final MetadataManager metadataManager)
     {
         return
             isNumberSmallerThanInt(
                 type, metadataManager.getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether the type means the attribute is a
      * number smaller than an int.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return such condition.
      */
     protected boolean isNumberSmallerThanInt(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.isNumberSmallerThanInt(type);
     }
 
     /**
      * Retrieves whether the attribute is numeric or not.
      * @return such information.
      */
     @SuppressWarnings("unused")
     public boolean isNumeric()
     {
         return isNumeric(getTypeId(), getMetadataManager());
     }
 
     /**
      * Retrieves whether the attribute is numeric or not.
      * @param type the type.
      * @param metadataManager the <code>MetadataManager</code> instance.
      * @return such information.
      */
     protected boolean isNumeric(final int type, @NotNull final MetadataManager metadataManager)
     {
         return isNumeric(type, metadataManager.getMetadataTypeManager());
     }
 
     /**
      * Retrieves whether the attribute is numeric or not.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code> instance.
      * @return such information.
      */
     protected boolean isNumeric(
         final int type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.isNumeric(type);
     }
 
     /**
      * Retrieves the Java type of the attribute.
      * @return such information.
      */
     @Deprecated
     @SuppressWarnings("unused")
     @NotNull
     public String getJavaType()
     {
         return getType(getTypeId(), getMetadataManager());
     }
 
     /**
      * Retrieves the Java type of the property.
      * @param type the type.
      * @param metadataManager the <code>MetadataManager</code>
      * instance.
      * @return such information.
      */
     @NotNull
     protected String getType(
         final int type, @NotNull final MetadataManager metadataManager)
     {
         return
             retrieveType(type, metadataManager, isNullable(), isBoolean());
     }
 
     /**
      * Retrieves the Java type of the attribute, which would be
      * only a primitive Java type if the attribute type matches,
      * and the column allows nulls.
      * @return such information.
      */
     @NotNull
     protected String retrieveType()
     {
         return
             retrieveType(
                 getTypeId(),
                 getMetadataManager(),
                 isNullable(),
                 isBoolean());
     }
 
     /**
      * Retrieves the Java type of the attribute, which would be
      * only a primitive Java type if the attribute type matches,
      * and the column allows nulls.
      * @param type the type.
      * @param metadataManager the <code>MetadataManager</code> instance.
      * @param allowsNull whether the attribute allows null.
      * @param isBool whether the attribute is declared as boolean.
      * @return such information.
      */
     @NotNull
     protected String retrieveType(
         final int type,
         final MetadataManager metadataManager,
         final boolean allowsNull,
         final boolean isBool)
     {
         return
             retrieveType(
                 type,
                 metadataManager.getMetadataTypeManager(),
                 allowsNull,
                 isBool,
                 MetadataTypeUtils.getInstance());
     }
 
     /**
      * Retrieves the Java type of the attribute, which would be
      * only a primitive Java type if the attribute type matches,
      * and the column allows nulls.
      * @param type the type.
      * @param metadataTypeManager the <code>MetadataTypeManager</code>
      * instance.
      * @param allowsNull whether the attribute allows null.
      * @param isBool whether the attribute is declared as boolean.
      * @param metadataTypeUtils the <code>MetadataTypeUtils</code> instance.
      * @return such information.
      */
     @NotNull
     protected String retrieveType(
         final int type,
         @NotNull final MetadataTypeManager metadataTypeManager,
         final boolean allowsNull,
         final boolean isBool,
         @NotNull final MetadataTypeUtils metadataTypeUtils)
     {
         String result =
             metadataTypeManager.getNativeType(type, allowsNull, isBool);
 
         if  (allowsNull)
         {
             result = metadataTypeUtils.getWrapperClass(result);
         }
 
         return result;
     }
 
     /**
      * Retrieves the attribute type, as a {@link java.sql.Types}'s constant.
      *
      * @return such information.
      */
     @Override
     public int getJavaSqlType()
     {
         return getJavaSqlType(getType(), getMetadataTypeManager());
     }
 
     /**
      * Retrieves the attribute type, as a {@link java.sql.Types}'s constant.
      * @param type the attribute type.
      * @param metadataTypeManager the {@link MetadataTypeManager} instance.
      * @return the {@link java.sql.Types}'s constant value.
      */
     protected int getJavaSqlType(@NotNull final String type, @NotNull final MetadataTypeManager metadataTypeManager)
     {
         return metadataTypeManager.getJavaType(type);
     }
 
     /**
      * Checks whether the value is null or not.
      * @return such check.
      */
     @SuppressWarnings("unused")
     public boolean isValueNull()
     {
         return isNull(getValue());
     }
 
     /**
      * Checks whether the value is null or not.
      * @param value the value.
      * @return such check.
      */
     protected boolean isNull(@Nullable final String value)
     {
         return value == null;
     }
 
     /**
      * Retrieves the attribute name.
      * @return such information.
      */
     @NotNull
     public String toString()
     {
         return toString(getAttribute());
     }
 
     /**
      * Retrieves the attribute name.
      * @param attribute the attribute.
      * @return such information.
      */
     @NotNull
     protected String toString(@NotNull final Attribute attribute)
     {
         return attribute.toString();
     }
 
     @Override
     public int hashCode()
     {
         return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.m__Attribute).toHashCode();
     }
 
     @Override
     public boolean equals(final Object obj)
     {
         if (obj == null)
         {
             return false;
         }
         if (getClass() != obj.getClass())
         {
             return false;
         }
         final AbstractAttributeDecorator other = (AbstractAttributeDecorator) obj;
         return new EqualsBuilder().appendSuper(super.equals(obj)).append(this.m__Attribute, other.m__Attribute)
             .isEquals();
     }
 }
