 /*
  *    Copyright 2010 The Meiyo Team
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package org.nnsoft.commons.meiyo.classpath;
 
 import org.nnsoft.commons.meiyo.classpath.filter.Filter;
 
 /**
  * A classpath handler notifies {@link ClassPathEntry} found to {@link ClassPathEntryHandler}
  * if and only if the {@link ClassPathEntry} satisfies the {@link Filter} requirements.
  */
 final class ClassPathHandler {
 
     /**
      * The {@link Filter} reference.
      */
     private final Filter filter;
 
     /**
      * The handlers list to witch {@link ClassPathEntry} will be notified if
      * this last satisfies the {@link Filter} requirements.
      */
     private final ClassPathEntryHandler[] classHandlers;
 
     /**
      * Creates a new classpath hanlder.
      *
      * @param filter the {@link Filter} reference.
      * @param classHandlers the handlers list to witch {@link ClassPathEntry} will be notified.
      */
     public ClassPathHandler(Filter filter, ClassPathEntryHandler...classHandlers) {
         this.filter = filter;
         this.classHandlers = classHandlers;
     }
 
     /**
     * Notified the {@link ClassPathEntry} if the {@link Filter} reference requirements
      * are satisfied.
      *
     * @param entry the classpath entry found.
      */
     public void doHandle(String path, Class<?> classPathEntry) {
         if (this.filter.matches(classPathEntry)) {
             for (ClassPathEntryHandler classHandler : this.classHandlers) {
                 classHandler.doHandle(path, classPathEntry);
             }
         }
     }
 
 }
