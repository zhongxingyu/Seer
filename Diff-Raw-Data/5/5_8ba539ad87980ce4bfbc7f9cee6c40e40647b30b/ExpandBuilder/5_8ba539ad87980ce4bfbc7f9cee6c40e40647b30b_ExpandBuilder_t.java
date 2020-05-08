 /*
  * Copyright (c) 2007-2011 Sonatype, Inc. All rights reserved.
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
 
 package org.sonatype.sisu.filetasks.builder;
 
 import org.sonatype.sisu.filetasks.FileTask;
 
 /**
  * {@link org.sonatype.sisu.filetasks.task.ExpandTask} builder.
  *
  * @since 1.0
  */
 public interface ExpandBuilder
     extends FileTask
 {
 
     /**
      * Specifies that newer files present in destination should be overwritten.
      *
      * @return itself, for fluent API usage
      * @since 1.0
      */
     ExpandBuilder overwriteNewer();
 
     /**
      * Specifies that newer files present in destination should not be overwritten.
      *
      * @return itself, for fluent API usage
      * @since 1.0
      */
     ExpandBuilder doNotOverwriteNewer();
 
     /**
      * Number of directories to be cut (form source archive), while expanding.
      *
      * @param directoriesToCut number of directories to be cut (form source archive), while expanding
      * @return itself, for fluent API usage
      * @since 1.0
      */
     ExpandBuilder cutDirectories( int directoriesToCut );
 
     /**
      * Adds an include pattern (ANT style) to filter the files to be expanded.
      *
      * @param pattern ANT style file pattern
      * @return itself, for fluent API usage
     * @since 1.4
      */
     ExpandBuilder include( String pattern );
 
     /**
      * Adds an exclude pattern (ANT style) to filter the files to be expanded.
      *
      * @param pattern ANT style file pattern
      * @return itself, for fluent API usage
     * @since 1.4
      */
     ExpandBuilder exclude( String pattern );
 
     /**
      * Ongoing destination builder.
      *
      * @return ongoing destination builder.
      * @since 1.0
      */
     DestinationBuilder to();
 
     /**
      * Ongoing destination builder.
      *
      * @since 1.0
      */
     interface DestinationBuilder
     {
 
         /**
          * Target directory where archive will be expanded.
          *
          * @param directory where archive will be expanded
          * @return itself, for fluent API usage
          * @since 1.0
          */
         ExpandBuilder directory( FileRef directory );
 
     }
 
 }
