 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.serialization.generator.output.serializer;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.VersionRange;
 import com.cedarsoft.codegen.CodeGenerator;
 import com.cedarsoft.codegen.NamingSupport;
 import com.cedarsoft.codegen.model.DomainObjectDescriptor;
 import com.cedarsoft.id.NameSpaceSupport;
 import com.cedarsoft.serialization.generator.decision.XmlDecisionCallback;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JInvocation;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JMod;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 /**
  *
  */
 public abstract class AbstractXmlGenerator extends AbstractGenerator<XmlDecisionCallback> {
   /**
   * The default namespace suffix
   */
  @NonNls
  @NotNull
  public static final String DEFAULT_NAMESPACE_SUFFIX = "1.0.0";
  /**
    * The version the serializer supports
    */
   @NotNull
   public static final Version VERSION = Version.valueOf( 1, 0, 0 );
   @NonNls
   public static final String METHOD_NAME_FROM = "from";
   @NonNls
   public static final String METHOD_NAME_TO = "to";
   @NonNls
   public static final String METHOD_SUPER = "super";
 
   /**
    * Creates a new generator
    *
    * @param codeGenerator the used code generator
    */
   protected AbstractXmlGenerator( @NotNull CodeGenerator<XmlDecisionCallback> codeGenerator ) {
     super( codeGenerator );
   }
 
   @Override
   @NotNull
   protected JMethod createConstructor( @NotNull JDefinedClass serializerClass, @NotNull DomainObjectDescriptor domainObjectDescriptor ) {
     JMethod constructor = serializerClass.constructor( JMod.PUBLIC );
     constructor.body()
       .invoke( METHOD_SUPER ).arg( getDefaultElementName( domainObjectDescriptor ) ).arg( getNamespace( domainObjectDescriptor.getQualifiedName() ) )
       .arg( createDefaultVersionRangeInvocation( AbstractXmlGenerator.VERSION, AbstractXmlGenerator.VERSION ) );
     return constructor;
   }
 
   /**
    * Returns the namespace that is used for the serialized documents
    *
    * @param domainObjectType the domain object type
    * @return the namespace
    */
   @NotNull
   @NonNls
   protected String getNamespace( @NotNull @NonNls final String domainObjectType ) {
    return NameSpaceSupport.createNameSpaceUriBase( domainObjectType ) + "/" + DEFAULT_NAMESPACE_SUFFIX;
   }
 
   /**
    * Returns the default element name
    *
    * @param domainObjectDescriptor the descriptor
    * @return the default element name
    */
   @NotNull
   @NonNls
   protected String getDefaultElementName( @NotNull DomainObjectDescriptor domainObjectDescriptor ) {
     return NamingSupport.createXmlElementName( domainObjectDescriptor.getClassDeclaration().getSimpleName() );
   }
 
   /**
    * Creates the default version range invocation
    *
    * @param from the from version
    * @param to   the to version
    * @return the invocation creating the version range
    */
   @NotNull
   protected JInvocation createDefaultVersionRangeInvocation( @NotNull Version from, @NotNull Version to ) {
     JClass versionRangeType = codeModel.ref( VersionRange.class );
     return versionRangeType.staticInvoke( METHOD_NAME_FROM ).arg( JExpr.lit( from.getMajor() ) ).arg( JExpr.lit( from.getMinor() ) ).arg( JExpr.lit( from.getBuild() ) )
       .invoke( METHOD_NAME_TO ).arg( JExpr.lit( to.getMajor() ) ).arg( JExpr.lit( to.getMinor() ) ).arg( JExpr.lit( to.getBuild() ) );
   }
 }
