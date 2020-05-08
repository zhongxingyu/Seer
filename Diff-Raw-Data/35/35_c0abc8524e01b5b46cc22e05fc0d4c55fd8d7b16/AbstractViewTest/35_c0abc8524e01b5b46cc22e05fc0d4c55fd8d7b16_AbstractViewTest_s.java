 /**
  * Copyright (C) cedarsoft GmbH.
  *
 * Licensed under the GNU General Public License version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
  *
 *         http://www.cedarsoft.org/gpl3
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
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
 
 package com.cedarsoft.couchdb;
 
 import org.apache.commons.io.IOUtils;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 
 import java.io.IOException;
 import java.net.URL;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public abstract class AbstractViewTest {
   @NotNull
   @NonNls
   public static final URL EMIT_JS = AbstractViewTest.class.getResource( "emit.js" );
   @NotNull
   @NonNls
   public static final URL EMPTY_JSON = AbstractViewTest.class.getResource( "empty.json" );
 
   protected Context context;
   protected Scriptable scriptable;
 
   @After
   public void tearDown() throws Exception {
     Context.exit();
   }
 
   @Before
   public void setUpJs() throws Exception {
     assertNotNull( EMIT_JS );
     assertNotNull( EMPTY_JSON );
 
     try {
       Context.exit();
     } catch ( IllegalStateException ignore ) {
     }
     context = Context.enter();
     scriptable = context.initStandardObjects();
   }
 
   protected void assertViewForEmptyDoc( @NotNull @NonNls URL view ) throws Exception {
     assertEquals( "undefined", executeView( view, EMPTY_JSON ) );
   }
 
   @NotNull
   @NonNls
   protected String executeView( @NotNull @NonNls URL view, @NotNull @NonNls URL doc ) throws Exception {
     setUpJs();
 
     //Prepare the emit function
     evaluate( getContent( EMIT_JS ) );
 
     //Evaluate the document
     evaluateJson( "doc", doc );
 
     //Evaluate the map function
     evaluate( "var mapFunction = " + getContent( view ) + ";" );
 
     //Call the map function
     evaluate( "mapFunction(doc);" );
 
     //Check what has been emmited!
     return Context.toString( evaluate( "emitted;" ) );
   }
 
   @NotNull
   protected Object evaluateJson( @NotNull @NonNls String objectName, @NotNull @NonNls URL resource ) throws IOException {
     return evaluate( "var " + objectName + " = " + getContent( resource ) + ";" );
   }
 
   @NotNull
   @NonNls
   protected String getContent( @NotNull URL resource ) throws IOException {
     return IOUtils.toString( resource.openStream() );
   }
 
   /**
    * Evaluates a script
    *
    * @param script the script that is evaluated
    * @return the result
    */
   @NotNull
   protected Object evaluate( @NotNull @NonNls String script ) {
     return context.evaluateString( scriptable, script, "<unknown>", 1, null );
   }
 }
