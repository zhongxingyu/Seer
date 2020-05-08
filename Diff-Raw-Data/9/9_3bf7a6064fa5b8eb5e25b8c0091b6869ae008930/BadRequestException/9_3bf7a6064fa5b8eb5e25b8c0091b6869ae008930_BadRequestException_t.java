 /*
  * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.sisu.siesta.common;
 
 /**
 * Thrown when an a client issues a bad request against a resource.
  *
  * @since 1.3.1
  */
 public class BadRequestException
     extends RuntimeException
 {
     public BadRequestException() {
         super();
     }
 
     public BadRequestException( final String message ) {
         super(message);
     }
 
     public BadRequestException( final String message, final Throwable cause ) {
         super(message, cause);
     }
 
     public BadRequestException( final Throwable cause ) {
         super(cause);
     }
 }
