 /*
  * Copyright (C) 2008-2009 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package iudex.core;
 
 import com.gravitext.htmap.UniMap;
 
 /**
 * Interface for filters over Content/Reference instances.
  */
 public interface ContentFilter
 {
     /**
      * Accept, transform, or reject content.
      * @throws FilterException to indicate rejection based on failure, to be
      * logged upstream.
      * @throws RuntimeException for more serious errors which should generally
      * terminate processing.
      * @return true if the Item should be kept, false otherwise.
      */
     public boolean filter( UniMap content ) throws FilterException;
 }
