 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.javaloader.blog;
 
 import org.apache.myfaces.scripting.loaders.java.ScriptingClass;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Collections;
 
 /**
  * @author werpu2
  * @date: 01.09.2009
  */
 public class BlogService {
 
 
 
     /**
      * note we cannot cast on dynamically referenced
      * and recompiled objects which are shared between beans
      * because due to dynamic recompilation
      * <p/>
      * Object a->references b does not reference b of the same class
      * as object c->references b, we have to use introspection in this case
      * we can use our utils class to make it a tiny bit more comfortable
      * <p/>
      * Statically compiled types always stay the same however
      * the same goes for interfaces which are present as compiled code only
      */
     List<Object> blogEntries = Collections.synchronizedList(new LinkedList<Object>());
 
    public void addEntry(Object entry) {
         if (entry != null) {
             blogEntries.add(entry);
         }
     }
 
 
 
     public List<Object> getBlogEntries() {
         return blogEntries;
     }
 
     public void setBlogEntries(List<Object> blogEntries) {
         this.blogEntries = blogEntries;
     }
 
     public String getTest() {
         return "ddd";
     }
 
 }
