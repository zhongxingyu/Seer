 package org.apache.maven.plugin.jxr;
 
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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 
 import org.apache.maven.jxr.java.src.JavaSrc;
 import org.apache.maven.jxr.java.src.JavaSrcOptions;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.StringUtils;
 
 /**
  * Base class which wraps all <code>JavaSrc</code> functionalities.
  *
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  */
 public abstract class AbstractJavasrcMojo
     extends AbstractMojo
 {
     // ----------------------------------------------------------------------
     // Mojo parameters
     // ----------------------------------------------------------------------
 
     /**
      * The Maven Project Object
      *
      * @parameter expression="${project}"
      * @required
      */
     protected MavenProject project;
 
     // ----------------------------------------------------------------------
     // JavaSrc parameters
     // ----------------------------------------------------------------------
 
     /**
      * JavaSrc component.
      *
      * @component
      */
     protected JavaSrc javaSrc;
 
     /**
      * The output directory.
      *
      * @parameter expression="${project.build.directory}/javasrc"
      * @required
      * @readonly
      */
     protected File outputDirectory;
 
     /**
      * Specifies the text to be placed at the bottom of each output file.
      *
      * @parameter expression="${bottom}"
      * default-value="Copyright &#169; {inceptionYear}-{currentYear} {organizationName}. All Rights Reserved."
      */
     private String bottom;
 
     /**
      * Specifies the encoding of the generated HTML files.
      *
      * @parameter expression="${docencoding}" default-value="ISO-8859-1"
      */
     private String docencoding;
 
     /**
      * Specifies the title to be placed near the top of the overview summary file.
      *
      * @parameter expression="${doctitle}" default-value="${project.name} ${project.version} XREF"
      */
     private String doctitle;
 
     /**
      * Specifies the encoding name of the source files.
      *
      * @parameter expression="${encoding}"
      */
     private String encoding;
 
     /**
      * Specifies the footer text to be placed at the bottom of each output file.
      *
      * @parameter expression="${footer}"
      */
     private String footer;
 
     /**
      * Specifies the header text to be placed at the top of each output file.
      *
      * @parameter expression="${header}"
      */
     private String header;
 
     /**
      * Specifies the text for upper left frame.
      *
      * @parameter expression="${packagesheader}"
      */
     private String packagesheader;
 
     /**
      * True to apply a recursive scan.
      *
      * @parameter expression="${recurse}" default-value="true"
      */
     private boolean recurse;
 
     /**
      * Specifies the path of an alternate HTML stylesheet file.
      *
      * @parameter expression="${stylesheetfile}"
      */
     private String stylesheetfile;
 
     /**
      * Specifies the top text to be placed at the top of each output file.
      *
      * @parameter expression="${top}"
      */
     private String top;
 
     /**
      * True to verbose the scan.
      *
      * @parameter expression="${verbose}" default-value="false"
      */
     private boolean verbose;
 
     /**
      * Specifies the title to be placed in the HTML title tag.
      *
      * @parameter expression="${title}" default-value="${project.name} ${project.version} JXR"
      */
     private String windowTitle;
 
     /**
      * Execute the <code>JavaSrc</code>.
      *
      * @throws IOException if any
      * @throws MojoExecutionException if any
      */
     public void executeJavaSrc()
         throws IOException
     {
         JavaSrcOptions options = new JavaSrcOptions();
 
         if ( StringUtils.isNotEmpty( getBottomText() ) )
         {
             options.setBottom( getBottomText() );
         }
         if ( StringUtils.isNotEmpty( this.docencoding ) )
         {
             options.setDocencoding( this.docencoding );
         }
         if ( StringUtils.isNotEmpty( this.doctitle ) )
         {
             options.setDoctitle( this.doctitle );
         }
         if ( StringUtils.isNotEmpty( this.encoding ) )
         {
             options.setEncoding( this.encoding );
         }
         if ( StringUtils.isNotEmpty( this.footer ) )
         {
             options.setFooter( this.footer );
         }
        if ( StringUtils.isNotEmpty( this.header ) )
         {
            options.setHeader( this.header );
         }
         if ( StringUtils.isNotEmpty( this.packagesheader ) )
         {
             options.setPackagesheader( this.packagesheader );
         }
         options.setRecurse( this.recurse );
         if ( StringUtils.isNotEmpty( this.stylesheetfile ) )
         {
             options.setStylesheetfile( this.stylesheetfile );
         }
         if ( StringUtils.isNotEmpty( this.top ) )
         {
             options.setTop( this.top );
         }
         options.setVerbose( this.verbose );
         if ( StringUtils.isNotEmpty( this.windowTitle ) )
         {
             options.setWindowtitle( this.windowTitle );
         }
 
         javaSrc.generate( new File( this.project.getBuild().getSourceDirectory() ), this.outputDirectory, options );
     }
 
     // ----------------------------------------------------------------------
     // private methods
     // ----------------------------------------------------------------------
 
     /**
      * Method that sets the bottom text that will be displayed on the bottom of the generated
      * javasrc files.
      *
      * @return a String that contains the text that will be displayed at the bottom of the javasrc
      */
     private String getBottomText()
     {
         int actualYear = Calendar.getInstance().get( Calendar.YEAR );
         String year = String.valueOf( actualYear );
 
         String inceptionYear = project.getInceptionYear();
 
         String theBottom = StringUtils.replace( this.bottom, "{currentYear}", year );
 
         if ( inceptionYear != null )
         {
             if ( inceptionYear.equals( year ) )
             {
                 theBottom = StringUtils.replace( theBottom, "{inceptionYear}-", "" );
             }
             else
             {
                 theBottom = StringUtils.replace( theBottom, "{inceptionYear}", inceptionYear );
             }
         }
         else
         {
             theBottom = StringUtils.replace( theBottom, "{inceptionYear}-", "" );
         }
 
         if ( project.getOrganization() == null )
         {
             theBottom = StringUtils.replace( theBottom, " {organizationName}", "" );
         }
         else
         {
             if ( StringUtils.isNotEmpty( project.getOrganization().getName() ) )
             {
                 if ( StringUtils.isNotEmpty( project.getOrganization().getUrl() ) )
                 {
                     theBottom = StringUtils.replace( theBottom, "{organizationName}", "<a href=\""
                         + project.getOrganization().getUrl() + "\">" + project.getOrganization().getName() + "</a>" );
                 }
                 else
                 {
                     theBottom = StringUtils.replace( theBottom, "{organizationName}", project.getOrganization()
                         .getName() );
                 }
             }
             else
             {
                 theBottom = StringUtils.replace( theBottom, " {organizationName}", "" );
             }
         }
 
         return theBottom;
     }
 }
