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
 
 package com.cedarsoft.serialization.generator.common.parsing;
 
 import com.cedarsoft.codegen.parser.Parser;
 import com.cedarsoft.codegen.parser.Result;
 import com.cedarsoft.test.utils.MockitoTemplate;
 import com.google.common.collect.ImmutableList;
 import com.sun.mirror.declaration.ClassDeclaration;
 import com.sun.mirror.declaration.FieldDeclaration;
 import com.sun.mirror.declaration.MethodDeclaration;
 import com.sun.mirror.declaration.ParameterDeclaration;
 import com.sun.mirror.declaration.TypeParameterDeclaration;
 import com.sun.mirror.type.ClassType;
 import com.sun.mirror.type.DeclaredType;
 import com.sun.mirror.type.InterfaceType;
 import com.sun.mirror.type.PrimitiveType;
 import com.sun.mirror.type.ReferenceType;
 import com.sun.mirror.type.TypeMirror;
 import com.sun.mirror.type.WildcardType;
 import com.sun.mirror.util.TypeVisitor;
 import com.sun.tools.apt.mirror.type.WildcardTypeImpl;
import org.fest.assertions.Assertions;
 import org.junit.*;
 import org.mockito.Mock;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 /**
  *
  */
 public class ParserTest {
   private Result parsed;
 
   @Before
   public void setUp() throws Exception {
     URL resource = getClass().getResource( "/com/cedarsoft/serialization/generator/common/parsing/test/JavaClassToParse.java" );
     assertNotNull( resource );
     File javaFile = new File( resource.toURI() );
     assertTrue( javaFile.exists() );
     parsed = Parser.parse( null, javaFile );
     assertNotNull( parsed );
   }
 
   @Test
   public void testCollectionType() {
     ClassDeclaration classDeclaration = parsed.getClassDeclaration( "com.cedarsoft.serialization.generator.common.parsing.test.JavaClassToParse.InnerStaticClass" );
     final ImmutableList<FieldDeclaration> fields = ImmutableList.copyOf( classDeclaration.getFields() );
 
     {
       FieldDeclaration field = fields.get( 0 );
       assertEquals( "stringList", field.getSimpleName() );
 
       TypeMirror type = field.getType();
       assertEquals( "java.util.List<java.lang.String>", type.toString() );
       assertTrue( type instanceof DeclaredType );
 
       DeclaredType declaredType = ( DeclaredType ) type;
       assertEquals( 1, declaredType.getActualTypeArguments().size() );
 
       List<TypeMirror> arguments = ImmutableList.copyOf( declaredType.getActualTypeArguments() );
       assertEquals( 1, arguments.size() );
       assertEquals( "java.lang.String", arguments.get( 0 ).toString() );
       assertTrue( arguments.get( 0 ) instanceof DeclaredType );
     }
 
     {
       FieldDeclaration field = fields.get( 1 );
       assertEquals( "wildStringList", field.getSimpleName() );
 
       TypeMirror type = field.getType();
       assertEquals( "java.util.List<? extends java.lang.String>", type.toString() );
       assertTrue( type instanceof DeclaredType );
 
       DeclaredType declaredType = ( DeclaredType ) type;
       assertEquals( 1, declaredType.getActualTypeArguments().size() );
 
       List<TypeMirror> arguments = ImmutableList.copyOf( declaredType.getActualTypeArguments() );
       assertEquals( 1, arguments.size() );
       assertEquals( "? extends java.lang.String", arguments.get( 0 ).toString() );
       assertTrue( arguments.get( 0 ).getClass().getName(), arguments.get( 0 ) instanceof WildcardTypeImpl );
       assertTrue( arguments.get( 0 ).getClass().getName(), arguments.get( 0 ) instanceof WildcardType );
 
       WildcardType wildcardType = ( WildcardType ) arguments.get( 0 );
       assertEquals( 1, wildcardType.getUpperBounds().size() );
       assertEquals( 0, wildcardType.getLowerBounds().size() );
 
       ReferenceType lowerBoundType = wildcardType.getUpperBounds().iterator().next();
       assertEquals( "java.lang.String", lowerBoundType.toString() );
       assertTrue( lowerBoundType instanceof DeclaredType );
     }
   }
 
   @Test
   public void testVisitor() throws Exception {
     ClassDeclaration classDeclaration = parsed.getClassDeclaration( "com.cedarsoft.serialization.generator.common.parsing.test.JavaClassToParse.InnerStaticClass" );
 
     final ImmutableList<FieldDeclaration> fields = ImmutableList.copyOf( classDeclaration.getFields() );
     assertEquals( "wildStringList", fields.get( 1 ).getSimpleName() );
     assertEquals( "a", fields.get( 2 ).getSimpleName() );
 
     new MockitoTemplate() {
       @Mock
       private TypeVisitor visitor;
 
       @Override
       protected void stub() throws Exception {
       }
 
       @Override
       protected void execute() throws Exception {
         fields.get( 1 ).getType().accept( visitor );
         fields.get( 2 ).getType().accept( visitor );
       }
 
       @Override
       protected void verifyMocks() throws Exception {
         verify( visitor ).visitInterfaceType( any( InterfaceType.class ) );
         verify( visitor ).visitPrimitiveType( any( PrimitiveType.class ) );
         verifyNoMoreInteractions( visitor );
       }
     }.run();
   }
 
   @Test
   public void testWildcards() {
     ClassDeclaration classDeclaration = parsed.getClassDeclaration( "com.cedarsoft.serialization.generator.common.parsing.test.JavaClassToParse.InnerStaticClass" );
 
     ImmutableList<FieldDeclaration> fields = ImmutableList.copyOf( classDeclaration.getFields() );
     FieldDeclaration field = fields.get( 0 );
     assertEquals( "stringList", field.getSimpleName() );
 
     TypeMirror type = field.getType();
     assertEquals( "java.util.List<java.lang.String>", type.toString() );
     assertEquals( "com.sun.tools.apt.mirror.type.InterfaceTypeImpl", type.getClass().getName() );
 
     {
       InterfaceType interfaceType = ( InterfaceType ) type;
       assertEquals( 1, interfaceType.getSuperinterfaces().size() );
       assertEquals( "java.util.Collection<java.lang.String>", interfaceType.getSuperinterfaces().iterator().next().toString() );
 
       //Checking the declaration (the java.util.List class itself!
       {
         assertEquals( "java.util.List<E>", interfaceType.getDeclaration().toString() );
         assertEquals( "java.util.List", interfaceType.getDeclaration().getQualifiedName() );
         assertEquals( "List", interfaceType.getDeclaration().getSimpleName() );
         assertEquals( "java.util", interfaceType.getDeclaration().getPackage().getQualifiedName() );
         assertEquals( 1, interfaceType.getDeclaration().getFormalTypeParameters().size() );
         TypeParameterDeclaration typeParameter = interfaceType.getDeclaration().getFormalTypeParameters().iterator().next();
         assertEquals( "E", typeParameter.getSimpleName() );
         assertEquals( typeParameter.getOwner(), interfaceType.getDeclaration() );
         assertEquals( "java.util.List<E>", typeParameter.getOwner().toString() );
         assertEquals( 1, typeParameter.getBounds().size() );
         assertEquals( "java.lang.Object", typeParameter.getBounds().iterator().next().toString() );
       }
 
       //Check the interface itself
       List<TypeMirror> actualTypeArgs = ImmutableList.copyOf( interfaceType.getActualTypeArguments() );
       assertEquals( 1, actualTypeArgs.size() );
       assertEquals( "java.lang.String", actualTypeArgs.get( 0 ).toString() );
       assertEquals( "com.sun.tools.apt.mirror.type.ClassTypeImpl", actualTypeArgs.get( 0 ).getClass().getName() );
       assertEquals( "String", ( ( ClassType ) actualTypeArgs.get( 0 ) ).getDeclaration().getSimpleName() );
       assertEquals( "java.lang.Object", ( ( ClassType ) actualTypeArgs.get( 0 ) ).getSuperclass().toString() );
      Assertions.assertThat( ( ( DeclaredType ) actualTypeArgs.get( 0 ) ).getDeclaration().getMethods().size() ).isGreaterThan( 68 ).isLessThan( 72 );
     }
   }
 
   @Test
   public void testParsing() throws ClassNotFoundException, NoSuchMethodException {
     assertEquals( 4, parsed.getClassDeclarations().size() );
 
     ClassDeclaration classDeclaration = parsed.getClassDeclaration( "com.cedarsoft.serialization.generator.common.parsing.test.JavaClassToParse.InnerStaticClass" );
     {
       assertEquals( 1, classDeclaration.getConstructors().size() );
       assertEquals( 1, classDeclaration.getConstructors().iterator().next().getParameters().size() );
       ParameterDeclaration parameterDeclaration = classDeclaration.getConstructors().iterator().next().getParameters().iterator().next();
       assertEquals( "int", parameterDeclaration.getType().toString() );
     }
 
     {
       assertEquals( 4, classDeclaration.getMethods().size() );
       Iterator<MethodDeclaration> methodIter = classDeclaration.getMethods().iterator();
       MethodDeclaration method0 = methodIter.next();
       MethodDeclaration method1 = methodIter.next();
       MethodDeclaration method2 = methodIter.next();
       MethodDeclaration method3 = methodIter.next();
 
       assertEquals( "getStringList", method0.getSimpleName() );
       assertEquals( "getWildStringList", method1.getSimpleName() );
       assertEquals( "doIt", method2.getSimpleName() );
       assertEquals( "compareTo", method3.getSimpleName() );
     }
   }
 }
