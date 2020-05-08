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
 
 package com.cedarsoft.serialization.generator.output.staxmate.serializer;
 
 import com.cedarsoft.serialization.generator.decision.XmlDecisionCallback;
 import com.cedarsoft.serialization.generator.model.DomainObjectDescriptor;
 import com.cedarsoft.serialization.generator.model.FieldDeclarationInfo;
 import com.cedarsoft.serialization.generator.output.CodeGenerator;
 import com.cedarsoft.serialization.generator.output.serializer.AbstractXmlGenerator;
 import com.cedarsoft.serialization.stax.AbstractStaxMateSerializer;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JVar;
 import org.codehaus.staxmate.out.SMOutputElement;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.util.Map;
 
 /**
  * Generator for stax mate based parsers
  */
 public class StaxMateGenerator extends AbstractXmlGenerator {
   @NonNls
   public static final String METHOD_NAME_CLOSE_TAG = "closeTag";
   @NotNull
   private final SerializingEntryGenerator serializingEntryGenerator;
 
   /**
    * Creates a new generator
    *
    * @param codeGenerator the code generator
    */
 
 
   public StaxMateGenerator( @NotNull CodeGenerator<XmlDecisionCallback> codeGenerator ) {
     super( codeGenerator );
     this.serializingEntryGenerator = new SerializingEntryGenerator( codeGenerator );
   }
 
   @NotNull
   @Override
   protected Map<FieldDeclarationInfo, JVar> fillDeSerializationMethods( @NotNull DomainObjectDescriptor domainObjectDescriptor, @NotNull JDefinedClass serializerClass, @NotNull JMethod serializeMethod, @NotNull JMethod deserializeMethod ) {
     try {
       return super.fillDeSerializationMethods( domainObjectDescriptor, serializerClass, serializeMethod, deserializeMethod );
     } finally {
       //Call closeTag( deserializeFrom ); on deserialize
       JVar deserializeFrom = deserializeMethod.listParams()[0];
       deserializeMethod.body().directStatement( "//Finally closing element" );
       deserializeMethod.body().invoke( StaxMateGenerator.METHOD_NAME_CLOSE_TAG ).arg( deserializeFrom );
     }
   }
 
   @Override
   @NotNull
   protected JVar appendDeserializeStatement( @NotNull JDefinedClass serializerClass, @NotNull JMethod deserializeMethod, @NotNull JVar deserializeFrom, @NotNull JVar formatVersion, @NotNull FieldDeclarationInfo fieldInfo ) {
     return serializingEntryGenerator.appendDeserializing( serializerClass, deserializeMethod, deserializeFrom, formatVersion, fieldInfo );
   }
 
   @Override
   protected void appendSerializeStatement( @NotNull JDefinedClass serializerClass, @NotNull JMethod serializeMethod, @NotNull JVar serializeTo, @NotNull JVar object, @NotNull FieldDeclarationInfo fieldInfo ) {
     serializingEntryGenerator.appendSerializing( serializerClass, serializeMethod, serializeTo, object, fieldInfo );
   }
 
   @NotNull
   @Override
   protected JClass createSerializerExtendsExpression( @NotNull JClass domainType ) {
     return codeModel.ref( AbstractStaxMateSerializer.class ).narrow( domainType );
   }
 
   @Override
   @NotNull
   protected Class<?> getExceptionType() {
     return XMLStreamException.class;
   }
 
   @Override
   @NotNull
   protected Class<?> getSerializeFromType() {
     return XMLStreamReader.class;
   }
 
   @Override
   @NotNull
   protected Class<?> getSerializeToType() {
     return SMOutputElement.class;
   }
 }
