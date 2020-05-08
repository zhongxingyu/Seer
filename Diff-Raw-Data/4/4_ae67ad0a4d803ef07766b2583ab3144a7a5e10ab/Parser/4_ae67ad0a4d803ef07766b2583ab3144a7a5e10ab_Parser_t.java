 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.serialization.generator.parsing;
 
 import com.sun.mirror.apt.AnnotationProcessor;
 import com.sun.mirror.apt.AnnotationProcessorEnvironment;
 import com.sun.mirror.apt.AnnotationProcessorFactory;
 import com.sun.mirror.declaration.AnnotationTypeDeclaration;
 import com.sun.mirror.declaration.ClassDeclaration;
 import com.sun.mirror.declaration.TypeDeclaration;
 import com.sun.tools.apt.Main;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 
 /**
  *
  */
 public class Parser {
   private Parser() {
   }
 
   @NotNull
   public static Result parse( @NotNull File file ) {
     CollectingFactory factory = new CollectingFactory();
     Main.process( factory, file.getAbsolutePath() );
 
     return factory.getResult();
   }
 
   private static class CollectingFactory implements AnnotationProcessorFactory {
     private Result result;
 
     @Override
     public Collection<String> supportedOptions() {
       return Collections.emptyList();
     }
 
     @Override
     public Collection<String> supportedAnnotationTypes() {
       return Arrays.asList( "*" );
     }
 
     @Override
     public AnnotationProcessor getProcessorFor( @NotNull final Set<AnnotationTypeDeclaration> atds, @NotNull final AnnotationProcessorEnvironment env ) {
       if ( result != null ) {
         throw new IllegalStateException( "Has still been called!" );
       }
       result = new Result( env );
 
       return new AnnotationProcessor() {
         @Override
         public void process() {
           for ( TypeDeclaration typeDeclaration : env.getTypeDeclarations() ) {
             //We are just interested in classes
             if ( typeDeclaration instanceof ClassDeclaration ) {
               result.addClassDeclaration( ( ClassDeclaration ) typeDeclaration );
             }
           }
         }
       };
     }
 
     @NotNull
     public Result getResult() {
      if ( result == null ) {
        throw new IllegalStateException( "No result found!" );
      }
       return result;
     }
   }
 }
 
