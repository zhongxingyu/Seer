 /*
  * Copyright (C) VSPLF Software Foundation (VSF), the origin author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.vsplf.i18n.helpers;
 
 import java.text.FieldPosition;
 import java.text.Format;
 import java.text.ParsePosition;
 
 /**
  * Lightweight porting of ICU MessageFormat.
  *
  * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
  * @since Mar 23, 2012
  */
 public final class MessageFormat extends Format {
 
   @Override
  public final StringBuffer format(final Object obj, final StringBuffer toAppendTo,
                                    final FieldPosition pos) {
     return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
 
   @Override
  public final Object parseObject(final String source, final ParsePosition pos) {
     return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
 }
