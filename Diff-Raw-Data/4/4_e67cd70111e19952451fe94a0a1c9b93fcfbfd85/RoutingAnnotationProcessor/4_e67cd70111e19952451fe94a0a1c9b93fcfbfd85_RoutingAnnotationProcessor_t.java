 /*******************************************************************************
  * Copyright (C) 2013 John Casey.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.commonjava.vertx.vabr.anno.proc;
 
 import groovy.lang.Writable;
 import groovy.text.GStringTemplateEngine;
 import groovy.text.Template;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.tools.Diagnostic.Kind;
 import javax.tools.FileObject;
 import javax.tools.StandardLocation;
 
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.commonjava.vertx.vabr.RouteCollection;
 import org.commonjava.vertx.vabr.anno.Route;
 import org.commonjava.vertx.vabr.anno.Routes;
 
 /* @formatter:off */
 @SupportedAnnotationTypes( { 
     "javax.ws.rs.CookieParam", 
     "javax.ws.rs.HeaderParam",
     "javax.ws.rs.PathParam", 
     "javax.ws.rs.QueryParam",
     "javax.ws.rs.core.Context",
    "org.commonjava.vertx.vabr.anno.Routes",
    "org.commonjava.vertx.vabr.anno.Route"
 } )
 /* @formatter:on */
 @SupportedSourceVersion( SourceVersion.RELEASE_7 )
 public class RoutingAnnotationProcessor
     extends AbstractProcessor
 {
 
     public static final String TEMPLATE = "groovy/routes.groovy";
 
     @Override
     public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv )
     {
         //        if ( annotations.isEmpty() )
         //        {
         //            return true;
         //        }
 
         System.out.println( "Starting Route Processing..." );
 
         final Set<RoutingTemplateInfo> infos = new HashSet<>();
 
         String pkg = null;
 
         for ( final Element elem : roundEnv.getElementsAnnotatedWith( Routes.class ) )
         {
             System.out.printf( "Processing: %s\n", elem );
 
             pkg = selectShortestPackage( pkg, elem );
 
             final Routes routes = elem.getAnnotation( Routes.class );
             if ( routes != null )
             {
                 for ( final Route route : routes.value() )
                 {
                     infos.add( new RoutingTemplateInfo( elem, route ) );
                 }
             }
         }
 
         for ( final Element elem : roundEnv.getElementsAnnotatedWith( Route.class ) )
         {
             final Route route = elem.getAnnotation( Route.class );
             infos.add( new RoutingTemplateInfo( elem, route ) );
             pkg = selectShortestPackage( pkg, elem );
         }
 
         if ( !infos.isEmpty() )
         {
             System.out.printf( "Using package: %s\n", pkg );
             generateOutput( pkg, infos, roundEnv );
         }
 
         return true;
     }
 
     private String selectShortestPackage( final String pkg, final Element elem )
     {
         Element pe = elem;
         do
         {
             pe = pe.getEnclosingElement();
         }
         while ( pe != null && pe.getKind() != ElementKind.PACKAGE );
 
         final String p = ( (PackageElement) pe ).getQualifiedName()
                                                 .toString();
 
         if ( pkg == null || p.length() < pkg.length() )
         {
             System.out.printf( "Setting package: %s\n", p );
             return p;
         }
 
         return pkg;
     }
 
     private void generateOutput( final String pkg, final Set<RoutingTemplateInfo> infos, final RoundEnvironment roundEnv )
     {
         final GStringTemplateEngine engine = new GStringTemplateEngine();
         final URL resource = Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResource( TEMPLATE );
         if ( resource == null )
         {
             throw new IllegalStateException( "Cannot find route template: " + TEMPLATE );
         }
 
         Template template;
         try
         {
             template = engine.createTemplate( resource );
         }
         catch ( CompilationFailedException | ClassNotFoundException | IOException e )
         {
             throw new IllegalStateException( "Cannot load route template: " + TEMPLATE + ". Reason: " + e.getMessage(), e );
         }
 
         System.out.printf( "Package: %s\n", pkg );
         final Map<String, Object> params = new HashMap<>();
         params.put( "pkg", pkg );
         params.put( "routes", infos );
         final Writable output = template.make( params );
 
         final String clsName = pkg + ".Routes";
         System.out.printf( "Generating routes class: %s\n", clsName );
 
         final Filer filer = processingEnv.getFiler();
         Writer sourceWriter = null;
         try
         {
             final FileObject file = filer.createSourceFile( clsName );
 
             sourceWriter = file.openWriter();
             output.writeTo( sourceWriter );
         }
         catch ( final IOException e )
         {
             processingEnv.getMessager()
                          .printMessage( Kind.ERROR, "While generating sources for routes class: '" + clsName + "', error: " + e.getMessage() );
         }
         finally
         {
             if ( sourceWriter != null )
             {
                 try
                 {
                     sourceWriter.close();
                 }
                 catch ( final IOException e )
                 {
                 }
             }
         }
 
         final String resName = "META-INF/services/" + RouteCollection.class.getName();
 
         Writer svcWriter = null;
         try
         {
             final FileObject file = filer.createResource( StandardLocation.SOURCE_OUTPUT, "", resName, (Element[]) null );
             System.out.printf( "Generating routes class service entry for: %s in: %s\n", clsName, file.toUri() );
             svcWriter = file.openWriter();
             svcWriter.write( clsName );
         }
         catch ( final IOException e )
         {
             processingEnv.getMessager()
                          .printMessage( Kind.ERROR,
                                         "While generating service configuration for routes class: '" + resName + "', error: " + e.getMessage() );
         }
         finally
         {
             if ( svcWriter != null )
             {
                 try
                 {
                     svcWriter.close();
                 }
                 catch ( final IOException e )
                 {
                 }
             }
         }
 
     }
 
 }
