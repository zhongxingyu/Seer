 /*
  * $Id: LogMessageInfoImpl.java 1247 2008-11-04 20:00:09Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials
  *      provided with the distribution.
  *    * Neither the name of the jCoderZ.org Project nor the names of
  *      its contributors may be used to endorse or promote products
  *      derived from this software without specific prior written
  *      permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.commons;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jcoderz.commons.util.ObjectUtil;
 
 /**
  * This class holds logging context information that is bound to the 
  * current thread.
  * 
  * <p>The values of the map should be strictly Strings. This is to avoid
  * misuse of the map as thread local cache and to avoid storing large 
  * object graphs in the context.</p>
  *  
  * @author Andreas Mandel
  */
 public final class LogThreadContext
 {
    private static final ThreadLocal TREAD_CONTEXT = new ThreadLocal()
     {
        protected Object initialValue ()
         {
            return new HashMap();
         }
     };
     
     private LogThreadContext ()
     {
         // No public instances
     }
     
     /**
      * Add a value identified by the given key to the thread context.
      * <p>The adder of the value is responsible that the value is removed
      * if the context is left. There can only be one value set for a 
      * given key.</p> 
      * @param key the identifier of the value to be set.
      * @param value the value to be stored in the context.
      * @return the String representation of the previous value stored 
      *  for this key, or null if no such value was stored.
      */
     public static String put (String key, String value)
     {
         return ObjectUtil.toString(get().put(key, value));
     }
     
     /**
      * Returns the value of the selected context parameter, or null
      * if the selected parameter is not set.
      * @param key the identifier of the value to be retrieved.
      * @return the value of the selected context parameter, or null
      *  if the selected parameter is not set.
      */
     public static String get (String key)
     {
         return ObjectUtil.toString(get().get(key));
     }
     
     /**
      * Removes the value that is currently stored for the given key.
      * @param key the identifier of the value to be removed.
      * @return the value of the selected context parameter, or null
      *  if the selected parameter is not set.
      */
     public static String remove (String key)
     {
         return ObjectUtil.toString(get().remove(key));
     }
 
     /**
      * Returns the underlying map that is used to store context 
      * parameters of the current thread. Changes on the returned Map
      * will also change the stored logging context. 
      * @return the underlying map that is used to store context 
      * parameters of the current thread.
      */
    public static Map/*<String,String>*/ get ()
     {
        return (Map) TREAD_CONTEXT.get();
     }
 }
