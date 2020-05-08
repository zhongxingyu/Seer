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
 
 import com.cedarsoft.AssertUtils;
import com.cedarsoft.serialization.generator.MirrorUtils;
 import com.cedarsoft.serialization.generator.decision.DefaultXmlDecisionCallback;
 import com.cedarsoft.serialization.generator.decision.XmlDecisionCallback;
 import com.cedarsoft.serialization.generator.model.DomainObjectDescriptor;
 import com.cedarsoft.serialization.generator.model.DomainObjectDescriptorFactory;
 import com.cedarsoft.serialization.generator.output.CodeGenerator;
 import com.cedarsoft.serialization.generator.output.serializer.I18nAnnotationsDecorator;
 import com.cedarsoft.serialization.generator.output.serializer.NotNullDecorator;
 import com.cedarsoft.serialization.generator.parsing.Parser;
 import com.cedarsoft.serialization.generator.parsing.Result;
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.writer.SingleStreamCodeWriter;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.testng.annotations.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import static org.testng.Assert.*;
 
 /**
  *
  */
 public class AbstractGeneratorTest {
   protected DomainObjectDescriptor domainObjectDescriptor;
   protected CodeGenerator<XmlDecisionCallback> codeGenerator;
 
   protected JCodeModel model;
 
   @BeforeMethod
   protected void setUp() throws Exception {
     URL resource = getClass().getResource( "/com/cedarsoft/serialization/generator/staxmate/test/Foo.java" );
     assertNotNull( resource );
     File javaFile = new File( resource.toURI() );
     assertTrue( javaFile.exists() );
     Result parsed = Parser.parse( javaFile );
     assertNotNull( parsed );
 
    MirrorUtils.setTypes( parsed.getEnvironment().getTypeUtils() );
    DomainObjectDescriptorFactory factory = new DomainObjectDescriptorFactory( parsed.getClassDeclaration( "com.cedarsoft.serialization.generator.staxmate.test.Foo" ) );
     domainObjectDescriptor = factory.create();
     assertNotNull( domainObjectDescriptor );
 
     assertEquals( domainObjectDescriptor.getFieldsToSerialize().size(), 7 );
     final DefaultXmlDecisionCallback decisionCallback = new DefaultXmlDecisionCallback( "width", "height" );
     CodeGenerator<XmlDecisionCallback> codeGenerator = new CodeGenerator<XmlDecisionCallback>( decisionCallback );
     this.codeGenerator = codeGenerator;
     this.codeGenerator.addMethodDecorator( new NotNullDecorator( NotNull.class ) );
     codeGenerator.addMethodDecorator( new I18nAnnotationsDecorator( NonNls.class ) );
     model = codeGenerator.getModel();
   }
 
   protected void assertGeneratedCode( @NotNull @NonNls URL expected ) throws IOException {
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     model.build( new SingleStreamCodeWriter( out ) );
 
     AssertUtils.assertEquals( out.toString().trim(), expected );
   }
 
   protected void assertGeneratedCode( @NotNull @NonNls String expected ) throws IOException {
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     model.build( new SingleStreamCodeWriter( out ) );
 
     assertEquals( out.toString().trim(), expected.trim() );
   }
 }
