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
 
 package com.cedarsoft.couchdb.test;
 
 import com.cedarsoft.couchdb.AbstractViewTest;
 import org.junit.*;
 import org.mozilla.javascript.Context;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class EmitTest extends AbstractViewTest {
   @Test
   public void testEmit() throws Exception {
     evaluate( getContent( EMIT_JS ) );
     assertEquals( "1,2", Context.toString( evaluate( "emit(1,2);emitted;" ) ) );
   }
 }
