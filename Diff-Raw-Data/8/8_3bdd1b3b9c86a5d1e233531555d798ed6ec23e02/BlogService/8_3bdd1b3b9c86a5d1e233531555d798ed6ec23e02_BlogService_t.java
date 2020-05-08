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
 package blog;
 
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Application scoped bean (defined in the faces-config for testing purposes)
  * which stores the blog entries
  */
 @ManagedBean(name="javaBlogService")
 @ApplicationScoped
 public class BlogService implements BlogServiceInterface {
 
     List<Object> blogEntries = Collections.synchronizedList(new LinkedList<Object>());
 

     /**
      * Add an entry to our blogging list
      * Note: we have a testing annotation,
      * which does nothing, it is there for testing
      * purposes only
      *
      * @param entry the entry to be added
      */
     @DependencyTestAnnotation
     public void addEntry(BlogEntry entry) {
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
 
     
 
 }
