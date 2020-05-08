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
 
 
 package com.cedarsoft.rest.sample.jaxb;
 
 import com.cedarsoft.JsonUtils;
 import com.cedarsoft.rest.test.Entry;
 import com.cedarsoft.rest.test.JaxbTestUtils;
 import com.cedarsoft.rest.test.SimpleJaxbTest;
 import org.codehaus.jackson.map.AnnotationIntrospector;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.junit.*;
 import org.junit.experimental.theories.*;
 
 import java.io.StringWriter;
 
 public class CameraJaxbTest
   extends SimpleJaxbTest<Camera.Jaxb, Camera.Stub> {
   public CameraJaxbTest() {
     super( Camera.Jaxb.class, Camera.Stub.class );
   }
 
   @Test
   public void testJson() throws Exception {
     ObjectMapper mapper = new ObjectMapper();
     AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
     mapper.getDeserializationConfig().setAnnotationIntrospector( introspector );
     mapper.getSerializationConfig().setAnnotationIntrospector( introspector );
     mapper.getSerializationConfig().setSerializationInclusion( JsonSerialize.Inclusion.NON_NULL );
 
     StringWriter out = new StringWriter();
     mapper.writeValue( out, defaultEntry().getObject() );
     JsonUtils.assertJsonEquals( getClass().getResource( "CameraJaxbTest.json" ), out.toString() );
 
     Camera.Jaxb deserialized = mapper.readValue( out.toString(), Camera.Jaxb.class );
     verifyDeserialized( deserialized, defaultEntry().getObject() );
   }
 
   @DataPoint
   public static Entry<? extends Camera.Jaxb> defaultEntry() {
     Camera.Jaxb object = new Camera.Jaxb( "id" );
     object.setHref( JaxbTestUtils.createTestUriBuilder().build() );
     CameraInfo.Jaxb info = new CameraInfo.Jaxb();
     object.setCameraInfo( info );
     info.setInternalSerial( "INTERNAL_35138574" );
     info.setSerial( 35138574 );
     info.setMake( "Canon" );
     info.setModel( "EOS 7D" );
     object.setDescription( "description" );
     User.Stub owner = new User.Stub();
     owner.setEmail( "mail@mail.com" );
     owner.setName( "daName" );
     object.setOwner( owner );
 
     return create( object, CameraJaxbTest.class.getResource( "CameraJaxbTest.xml" ) );
   }
 
   @DataPoint
   public static Entry<? extends Camera.Stub> stub() {
     Camera.Stub object = new Camera.Stub( "id" );
     object.setHref( JaxbTestUtils.createTestUriBuilder().build() );
     CameraInfo.Stub cameraInfoJaxb = new CameraInfo.Stub();
 
     cameraInfoJaxb.setMake( "Canon" );
     cameraInfoJaxb.setModel( "EOS 7D" );
 
     object.setCameraInfo( cameraInfoJaxb );
 
     return create( object, CameraJaxbTest.class.getResource( "CameraJaxbTest.stub.xml" ) );
   }
 }
