 /*
  *  Copyright 2009-2010 Mathieu ANCELIN.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.api;
 
 /**
  * Stage definition for specific stage binding definition.
  *
  * @author Mathieu ANCELIN
  */
 public enum Stage {
     /**
     * Bindings declared for developpement stage (in an IDE)
      * and unit testing environment.
      */
    DEVELOPPEMENT,
     /**
      * Bindings declared for test server environment.
      */
     TEST,
     /**
      * Bindings declared for qualification server environment.
      */
     PREPRODUCTION,
     /**
      * Bindings declared for production server environment.
      */
     PRODUCTION
 }
