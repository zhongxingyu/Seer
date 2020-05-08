 /*
   Copyright (C) 2012-2013 the original author or authors.
 
   See the LICENSE.txt file distributed with this work for additional
   information regarding copyright ownership.
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 
 package jdeps;
 
 /**
 * A callback for when a vertex has been found in topological order.
  *
 * Allows for parallel vertex processing unless one is found that
 * is waiting for processing of 1 or more transitive vertices. In this
 * case, processing will not proceed until the transitive vertices have
  * completed their work.
  */
 public interface ITopologicalSortCallback<T extends IVertex> {
   /**
    * The callback that will be executed inside a thread other than the one
    * invoking the sort.
    *
    * @param dependency Instance of {@link IVertex} that can now be processed.
    * @param coordinator Instance of {@link ITopologicalSortCoordinator} that allows for communication between
    *                    asynchronous sorting submissions done by the driver (which is typically done by an instance
    *                    of {@link ITopologicalSortStrategy}).
    */
   void handle(T dependency, ITopologicalSortCoordinator coordinator) throws Throwable;
 }
