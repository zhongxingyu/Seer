 /**
  * Copyright (c) 2009-2010 Zauber S.A. <http://www.zaubersoftware.com/>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ar.com.zauber.leviathan.impl.httpclient.charset;
 
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
 
 import org.apache.commons.lang.Validate;
 
 import ar.com.zauber.leviathan.common.CharsetStrategy;
 import ar.com.zauber.leviathan.common.ResponseMetadata;
 
 /**
  * Estrategia estandar para tratar de obtener la codificacion de un http response.
  *
  * @author Mariano Semelman
  * @since Dec 15, 2009
  */
 public class DefaultHttpCharsetStrategy implements CharsetStrategy {
 
     /** @see CharsetStrategy#getCharset(ResponseMetadata, byte[]) */
     public final Charset getCharset(final ResponseMetadata meta,
             final InputStream documento) {
         Validate.notNull(meta);
         Charset res = null;
         if(meta.getEncoding() != null) {
             try {
                 res = Charset.forName(meta.getEncoding());
             } catch (IllegalCharsetNameException e) {
                 // return null;
            } catch (UnsupportedCharsetException e) {
                // return null;
             }
         }
         return res;
     }
 
 }
