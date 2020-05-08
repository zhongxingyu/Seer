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
 
 package com.cedarsoft;
 
 import com.cedarsoft.quantity.Length;
 import com.cedarsoft.unit.Symbol;
 import com.cedarsoft.unit.Units;
 import com.cedarsoft.unit.other.deg;
 import com.cedarsoft.unit.other.px;
 import com.cedarsoft.unit.prefix.Prefixed;
 import com.cedarsoft.unit.prefix.centi;
 import com.cedarsoft.unit.prefix.kilo;
 import com.cedarsoft.unit.prefix.milli;
 import com.cedarsoft.unit.si.SIBaseUnit;
 import com.cedarsoft.unit.si.SiDerivedUnit;
 import com.cedarsoft.unit.si.cm;
 import com.cedarsoft.unit.si.kg;
 import com.cedarsoft.unit.si.m;
 import com.cedarsoft.unit.si.m2;
 import com.cedarsoft.unit.si.m_s;
 import com.cedarsoft.unit.si.mm;
 import com.cedarsoft.unit.si.rad;
 import com.cedarsoft.unit.si.s;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class UnitTest {
   @m
   public static double calcMetres( @m double amount ) {
     return amount;
   }
 
   @m2
   public static double calcArea( @m double width, @m double height ) {
     return width * height;
   }
 
   @m_s
   public static double calcSpeed( @m double amount, @s double time ) {
     return amount / time;
   }
 
   @m
   public static double convertToMetres( @Length double amount, @NotNull Class<? extends Annotation> unit ) {
     if ( unit == m.class ) {
       return amount;
     }
 
     if ( Prefixed.isPrefixed( unit ) ) {
       double factor = Prefixed.getFactor( unit );
       return amount * factor;
     }
 
     throw new UnsupportedOperationException( "Unsupported unit: " + unit );
   }
 
   @m
   public static double convertToMetres( @cm double centiMetres ) {
     return centiMetres / 100.0;
   }
 
   @m
   public static double large( @kilo( m.class ) double km ) {
     return km / 1000;
   }
 
   @milli( m.class )
   public static double cm2mm( @centi( m.class ) double cm ) {
     return cm / 10.0;
   }
 
   @cm
   public static double m2cm( @m double cm ) {
     return cm / 10.0;
   }
 
   @rad
   public static double toRadians( @deg double angdeg ) {
     return Math.toRadians( angdeg );
   }
 
   @deg
   public static double toDegrees( @rad double angrad ) {
     return Math.toDegrees( angrad );
   }
 
   ////////////////
   // The above methods are used to show the usage of the annotations
   ////////////////
 
   @Test
   public void testIt() {
     Class<?> metreUnit = m.class;
 
     {
       @m
       double metre = 7;
       @m
       double result = calcMetres( metre );
     }
 
     {
       @m
       double result = convertToMetres( 37, cm.class );
       assertEquals( 0.37, result, 0 );
     }
 
     {
       @m
       double result = convertToMetres( 37, mm.class );
       assertEquals( 0.037, result, 0 );
     }
   }
 
   @Test
   public void testConvert() throws Exception {
     @cm
     int cm = 100;
     @m
     double result = convertToMetres( cm );
     assertEquals( 1, result, 0 );
   }
 
   @Test
   public void testSpeed() throws Exception {
     @m_s
     double speed = calcSpeed( 70, 100 );
     assertEquals( 0.7, speed, 0 );
   }
 
   @Test
   public void testArea() throws Exception {
     @m int width = 7;
     @m int height = 3;
     @m2
     double area = calcArea( width, height );
 
     assertEquals( 21, area, 0 );
   }
 
   @Test
   public void testSymbol() throws Exception {
     assertEquals( "m", Units.getSymbol( m.class ) );
     assertEquals( "cm", Units.getSymbol( cm.class ) );
     assertEquals( "kg", Units.getSymbol( kg.class ) );
     assertEquals( "m²", Units.getSymbol( m2.class ) );
     assertEquals( "m/s", Units.getSymbol( m_s.class ) );
     assertEquals( "s", Units.getSymbol( s.class ) );
 
     assertEquals( "px", Units.getSymbol( px.class ) );
     assertEquals( "°", Units.getSymbol( deg.class ) );
   }
 
   @Test
   public void testNames() throws Exception {
     assertEquals( "metre", Units.getName( m.class ) );
     assertEquals( "centimetre", Units.getName( cm.class ) );
     assertEquals( "kilogram", Units.getName( kg.class ) );
     assertEquals( "square metre", Units.getName( m2.class ) );
    assertEquals( "metre per second", Units.getName( m_s.class ) );
     assertEquals( "second", Units.getName( s.class ) );
     assertEquals( "degree", Units.getName( deg.class ) );
   }
 
   @Test
   public void testReflection() throws Exception {
     Method method = UnitTest.class.getMethod( "calcMetres", Double.TYPE );
     assertNotNull( method );
 
     assertEquals( 1, method.getParameterAnnotations().length );
     assertEquals( 1, method.getParameterAnnotations()[0].length );
     Annotation annotation = method.getParameterAnnotations()[0][0];
     assertEquals( "com.cedarsoft.unit.si.m", annotation.annotationType().getName() );
 
     m metre = ( m ) annotation;
 
     assertTrue( annotation.annotationType().isAnnotationPresent( Length.class ) );
     assertTrue( annotation.annotationType().isAnnotationPresent( SIBaseUnit.class ) );
 
     assertTrue( annotation.annotationType().isAnnotationPresent( SIBaseUnit.class ) );
     assertFalse( annotation.annotationType().isAnnotationPresent( SiDerivedUnit.class ) );
 
     assertEquals( "m", annotation.annotationType().getAnnotation( Symbol.class ).value() );
   }
 
   @Test
   public void testReflCm() throws Exception {
     Method method = UnitTest.class.getMethod( "convertToMetres", Double.TYPE );
     assertNotNull( method );
 
     assertEquals( 1, method.getParameterAnnotations().length );
     assertEquals( 1, method.getParameterAnnotations()[0].length );
 
     Annotation annotation = method.getParameterAnnotations()[0][0];
     assertEquals( "com.cedarsoft.unit.si.cm", annotation.annotationType().getName() );
 
     assertTrue( annotation.annotationType().isAnnotationPresent( Length.class ) );
    assertTrue( annotation.annotationType().isAnnotationPresent( SiDerivedUnit.class ) );
     assertFalse( annotation.annotationType().isAnnotationPresent( SIBaseUnit.class ) );
 
    assertTrue( annotation.annotationType().isAnnotationPresent( SiDerivedUnit.class ) );

     assertEquals( "cm", annotation.annotationType().getAnnotation( Symbol.class ).value() );
   }
 }
 
