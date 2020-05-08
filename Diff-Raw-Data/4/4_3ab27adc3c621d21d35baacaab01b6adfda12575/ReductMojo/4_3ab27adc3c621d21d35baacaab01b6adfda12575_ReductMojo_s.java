 /**
  * Copyright (c) 2013 Martin M Reed
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hardisonbrewing.clover;
 
 import generated.ClassMetrics;
 import generated.Construct;
 import generated.Coverage;
 import generated.FileMetrics;
 import generated.Line;
 import generated.PackageMetrics;
 import generated.Project;
 import generated.ProjectMetrics;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.util.cli.StreamConsumer;
 import org.hardisonbrewing.jaxb.JAXB;
 
 import com.google.common.collect.SortedArraySet;
 
 /**
  * @goal reduct
  * @phase reduct
  * @requiresProject false
  */
 public final class ReductMojo extends AbstractMojo {
 
     private static final int DEFAULT_THREAD_COUNT = 15;
 
     private static final String CLOVER = "clover";
     private static final String WORKING_COPY = "workingCopy";
     private static final String CUTOFF_DATE = "cutoffDate";
     private static final String THREAD_COUNT = "threads";
     private static final String SVN_USERNAME = "svnUsername";
 
     private File cloverReportFile;
 
     private String svnUsername;
     private String workingCopyPath;
     private String cutoffDate;
     private int threadCount = DEFAULT_THREAD_COUNT;
 
     private File targetDirectory;
 
     private long cutoffRevision;
 
     /**
      * @parameter expression="${project}"
      * @required
      */
     private MavenProject mavenProject;
 
     /**
      * @parameter expression="${session}"
      * @readonly
      * @required
      */
     private MavenSession mavenSession;
 
     @Override
     public final void execute() throws MojoExecutionException, MojoFailureException {
 
         if ( !mavenProject.isExecutionRoot() ) {
             return;
         }
 
         long start = System.currentTimeMillis();
 
         try {
             _execute();
         }
         catch (Exception e) {
             throw new IllegalStateException( e );
         }
         finally {
             long end = System.currentTimeMillis();
             getLog().info( "Executed in " + ( ( end - start ) / 1000.0 ) + "s" );
         }
     }
 
     private void _execute() throws Exception {
 
         svnUsername = getProperty( SVN_USERNAME );
         initCloverFilePath();
         initWorkingCopyPath();
         initCutoffDate();
         initThreadCount();
 
         getLog().info( "Using coverage report from: " + cloverReportFile.getPath() );
 
         targetDirectory = new File( "target", "clover-reductor" );
         targetDirectory.mkdirs();
 
         FileUtils.copyFile( cloverReportFile, new File( targetDirectory, "clover-original.xml" ) );
 
         Coverage coverage = JAXB.unmarshal( cloverReportFile, Coverage.class );
         Project project = coverage.getProject();
         getLog().info( "Running Reductor: " + project.getName() );
 
         List<generated.Package> packages = project.getPackage();
         if ( packages.isEmpty() ) {
             getLog().info( "No packages found." );
             return;
         }
 
         cutoffRevision = findCutoffRevision( workingCopyPath );
         getLog().info( "Cutoff Revision: " + cutoffRevision );
 
         int fileCount = 0;
 
        for (generated.Package _package : packages) {
 
             List<generated.File> files = _package.getFile();
 
             if ( files.isEmpty() ) {
                 packages.remove( _package );
                 continue;
             }
 
             fileCount += files.size();
         }
 
         if ( fileCount == 0 ) {
             getLog().info( "No files found." );
             return;
         }
 
         BlameThread[] threads = new BlameThread[Math.min( fileCount, threadCount )];
         List<generated.Package> packagesReduced = new LinkedList<generated.Package>();
 
         for (int i = 0; i < threads.length; i++) {
             threads[i] = new BlameThread( packages, packagesReduced );
             new Thread( threads[i] ).start();
         }
 
         for (BlameThread thread : threads) {
             thread.waitUntilFinished();
         }
 
         Project projectReduced = new Project();
         projectReduced.setMetrics( new ProjectMetrics() );
         projectReduced.setName( project.getName() );
         projectReduced.setTimestamp( project.getTimestamp() );
 
         for (generated.Package _package : packagesReduced) {
             add( projectReduced, _package );
         }
 
         Coverage coverageReduced = new Coverage();
         coverageReduced.setProject( projectReduced );
         coverageReduced.setClover( coverageReduced.getClover() );
         coverageReduced.setGenerated( coverage.getGenerated() );
 
         File cloverReportReducedFile = reducedFile( cloverReportFile );
         getLog().info( "Saving new coverage report to: " + cloverReportReducedFile.getPath() );
         JAXB.marshal( cloverReportReducedFile, coverageReduced );
     }
 
     private File reducedFile( File file ) {
 
         String name = file.getName();
         String extension = FileUtils.extension( name );
         name = name.substring( 0, name.length() - ( extension.length() + 1 ) );
         name = name + "-reduced." + extension;
         return new File( file.getParent(), name );
     }
 
     private String getProperty( String key ) {
 
         String value = mavenSession.getExecutionProperties().getProperty( key );
         if ( value == null ) {
             value = mavenProject.getProperties().getProperty( key );
         }
         if ( value == null ) {
             return null;
         }
         boolean startsWith = value.startsWith( "\"" );
         boolean endsWith = value.endsWith( "\"" );
         if ( startsWith || endsWith ) {
             int length = value.length();
             int start = startsWith ? 1 : 0;
             int end = endsWith ? length - 1 : length;
             value = value.substring( start, end );
         }
         return value;
     }
 
     private void initCloverFilePath() throws Exception {
 
         String cloverReportPath = getProperty( CLOVER );
         if ( cloverReportPath == null || cloverReportPath.length() == 0 ) {
             getLog().error( "Required property `" + CLOVER + "` missing. Use -D" + CLOVER + "=<path to xml>" );
             throw new IllegalArgumentException();
         }
 
         cloverReportFile = new File( cloverReportPath );
         if ( !cloverReportFile.exists() ) {
             throw new FileNotFoundException( cloverReportFile.getPath() );
         }
     }
 
     private void initWorkingCopyPath() throws Exception {
 
         workingCopyPath = getProperty( WORKING_COPY );
         if ( workingCopyPath == null || workingCopyPath.length() == 0 ) {
             getLog().error( "Required property `" + WORKING_COPY + "` missing. Use -D" + WORKING_COPY + "=<path to working copy>" );
             throw new IllegalArgumentException();
         }
 
         if ( !new File( workingCopyPath ).exists() ) {
             throw new FileNotFoundException( workingCopyPath );
         }
 
         if ( !new File( workingCopyPath, ".svn" ).exists() ) {
             getLog().error( "Directory is not a working copy: " + workingCopyPath );
             throw new IllegalArgumentException();
         }
     }
 
     private void initCutoffDate() throws Exception {
 
         cutoffDate = getProperty( CUTOFF_DATE );
         if ( cutoffDate == null || cutoffDate.length() == 0 ) {
             getLog().error( "Required property `" + CUTOFF_DATE + "` missing. Use -D" + CUTOFF_DATE + "=<timestamp>" );
             throw new IllegalArgumentException();
         }
     }
 
     private void initThreadCount() throws Exception {
 
         String threadCountStr = getProperty( THREAD_COUNT );
         if ( threadCountStr != null && threadCountStr.length() > 0 ) {
             try {
                 threadCount = Integer.parseInt( threadCountStr );
             }
             catch (NumberFormatException e) {
                 getLog().error( "Illegal thread count specified: -D" + THREAD_COUNT + "=" + threadCountStr + ". Must be an integer." );
                 throw new IllegalArgumentException();
             }
         }
     }
 
     private generated.File reduceFile( generated.File file ) throws Exception {
 
         String filePath = file.getPath();
         if ( !new File( filePath ).exists() ) {
             throw new FileNotFoundException( filePath );
         }
 
         Properties properties = info( filePath );
         long revision = Long.parseLong( properties.getProperty( "Revision" ) );
         if ( revision < cutoffRevision ) {
             return null;
         }
 
         generated.File fileReduced = null;
         Set<Line> sortedLines = null;
 
         List<Long> revisions = blame( file.getPath() );
 
         for (Line line : file.getLine()) {
 
             int lineNumber = line.getNum();
             if ( cutoffRevision >= revisions.get( lineNumber - 1 ) ) {
                 continue;
             }
 
             if ( fileReduced == null ) {
                 fileReduced = new generated.File();
                 fileReduced.setMetrics( new FileMetrics() );
                 fileReduced.setName( file.getName() );
                 fileReduced.setPath( file.getPath() );
                 sortedLines = new SortedArraySet<Line>( new LineComparator() );
             }
 
             addMetrics( fileReduced, line );
             sortedLines.add( line );
         }
 
         if ( fileReduced != null ) {
             List<Line> lines = fileReduced.getLine();
             lines.addAll( sortedLines );
         }
 
         return fileReduced;
     }
 
     private void addMetrics( generated.File file, Line line ) {
 
         FileMetrics fileMetrics = file.getMetrics();
 
         int elements = 0;
         int coveredElements = 0;
 
         switch (line.getType()) {
             case STMT: {
                 elements = 1;
                 coveredElements = Math.min( 1, line.getCount() );
                 fileMetrics.setLoc( _int( fileMetrics.getLoc() ) + 1 );
                 fileMetrics.setStatements( fileMetrics.getStatements() + elements );
                 fileMetrics.setCoveredstatements( fileMetrics.getCoveredstatements() + coveredElements );
                 break;
             }
             case COND: {
                 elements = 2;
                 coveredElements = Math.min( 1, line.getTruecount() ) + Math.min( 1, line.getFalsecount() );
                 fileMetrics.setConditionals( fileMetrics.getConditionals() + elements );
                 fileMetrics.setCoveredconditionals( fileMetrics.getCoveredconditionals() + coveredElements );
                 break;
             }
             case METHOD: {
                 elements = 1;
                 coveredElements = Math.min( 1, line.getCount() );
                 fileMetrics.setMethods( fileMetrics.getMethods() + elements );
                 fileMetrics.setCoveredmethods( fileMetrics.getCoveredmethods() + coveredElements );
                 break;
             }
         }
 
         fileMetrics.setElements( fileMetrics.getElements() + elements );
         fileMetrics.setCoveredelements( fileMetrics.getCoveredelements() + coveredElements );
     }
 
     private void add( generated.Package _package, generated.File file ) {
 
         PackageMetrics packageMetrics = _package.getMetrics();
         packageMetrics.setFiles( _int( packageMetrics.getFiles() ) + 1 );
         add( packageMetrics, file.getMetrics() );
 
         List<generated.File> files = _package.getFile();
         files.add( file );
     }
 
     private int _int( Integer integer ) {
 
         return integer == null ? 0 : integer.intValue();
     }
 
     private void add( Project project, generated.Package _package ) {
 
         ProjectMetrics projectMetrics = project.getMetrics();
         projectMetrics.setPackages( _int( projectMetrics.getPackages() ) + 1 );
         add( projectMetrics, _package.getMetrics() );
 
         List<generated.Package> packages = project.getPackage();
         packages.add( _package );
     }
 
     private void add( PackageMetrics parent, PackageMetrics child ) {
 
         parent.setFiles( _int( parent.getFiles() ) + _int( child.getFiles() ) );
         add( (FileMetrics) parent, child );
     }
 
     private void add( FileMetrics parent, FileMetrics child ) {
 
         parent.setClasses( _int( parent.getClasses() ) + _int( child.getClasses() ) );
         parent.setLoc( _int( parent.getLoc() ) + _int( child.getLoc() ) );
         add( (ClassMetrics) parent, child );
     }
 
     private void add( ClassMetrics parent, ClassMetrics child ) {
 
         parent.setStatements( parent.getStatements() + child.getStatements() );
         parent.setConditionals( parent.getConditionals() + child.getConditionals() );
         parent.setMethods( parent.getMethods() + child.getMethods() );
         parent.setElements( parent.getElements() + child.getElements() );
 
         parent.setCoveredstatements( parent.getCoveredstatements() + child.getCoveredstatements() );
         parent.setCoveredconditionals( parent.getCoveredconditionals() + child.getCoveredconditionals() );
         parent.setCoveredmethods( parent.getCoveredmethods() + child.getCoveredmethods() );
         parent.setCoveredelements( parent.getCoveredelements() + child.getCoveredelements() );
     }
 
     private Properties info( String filePath ) throws Exception {
 
         List<String> cmd = new LinkedList<String>();
         cmd.add( "svn" );
         cmd.add( "info" );
         if ( svnUsername != null ) {
             cmd.add( "--username=" + svnUsername );
         }
         cmd.add( filePath );
 
         Properties properties = new Properties();
         StreamConsumer streamConsumer = new InfoStreamConsumer( properties );
         CommandLineUtils.executeCommandLine( build( cmd ), streamConsumer, streamConsumer );
         return properties;
     }
 
     private List<Long> blame( String filePath ) throws Exception {
 
         List<String> cmd = new LinkedList<String>();
         cmd.add( "svn" );
         cmd.add( "blame" );
         if ( svnUsername != null ) {
             cmd.add( "--username=" + svnUsername );
         }
         cmd.add( filePath );
 
         List<Long> revisions = new LinkedList<Long>();
         StreamConsumer streamConsumer = new BlameStreamConsumer( revisions );
         CommandLineUtils.executeCommandLine( build( cmd ), streamConsumer, streamConsumer );
         return revisions;
     }
 
     private long findCutoffRevision( String workingCopy ) throws Exception {
 
         Properties properties = info( workingCopy );
 
         List<String> cmd = new LinkedList<String>();
         cmd.add( "svn" );
         cmd.add( "checkout" );
         if ( svnUsername != null ) {
             cmd.add( "--username=" + svnUsername );
         }
         cmd.add( "-r" );
         cmd.add( "{" + cutoffDate + "}" );
         cmd.add( "--depth" );
         cmd.add( "empty" );
         cmd.add( properties.getProperty( "Repository Root" ) );
         cmd.add( targetDirectory.getPath() );
 
         RevisionStreamConsumer streamConsumer = new RevisionStreamConsumer();
         CommandLineUtils.executeCommandLine( build( cmd ), streamConsumer, streamConsumer );
         return streamConsumer.getRevision();
     }
 
     private Commandline build( List<String> cmd ) throws CommandLineException {
 
         Commandline commandLine = new Commandline();
         commandLine.setExecutable( cmd.get( 0 ) );
 
         for (int i = 1; i < cmd.size(); i++) {
             commandLine.createArg().setValue( cmd.get( i ) );
         }
 
         return commandLine;
     }
 
     private final class BlameThread implements Runnable {
 
         private final Object lock = new Object();
 
         private final List<generated.Package> packages;
         private final List<generated.Package> packagesReduced;
 
         private boolean finished;
 
         public BlameThread(List<generated.Package> packages, List<generated.Package> packagesReduced) {
 
             this.packages = packages;
             this.packagesReduced = packagesReduced;
         }
 
         @Override
         public void run() {
 
             try {
 
                 while (true) {
 
                     generated.Package _package = null;
                     generated.File file = null;
 
                     synchronized (packages) {
 
                         if ( packages.isEmpty() ) {
                             break;
                         }
 
                         _package = packages.get( 0 );
 
                         List<generated.File> files = _package.getFile();
                         file = files.remove( 0 );
 
                         if ( files.isEmpty() ) {
                             packages.remove( 0 );
                         }
                     }
 
                     generated.File fileReduced = null;
 
                     try {
                         fileReduced = reduceFile( file );
                     }
                     catch (Exception e) {
                         getLog().error( "Unable to inspect file: " + file.getName() );
                         e.printStackTrace();
                     }
 
                     if ( fileReduced != null ) {
                         synchronized (packages) {
                             generated.Package packageReduced = getPackage( _package.getName() );
                             add( packageReduced, fileReduced );
                         }
                     }
                 }
             }
             finally {
                 finished = true;
                 synchronized (lock) {
                     lock.notify();
                 }
             }
         }
 
         private generated.Package getPackage( String name ) {
 
             for (generated.Package _package : packagesReduced) {
                 if ( name.equals( _package.getName() ) ) {
                     return _package;
                 }
             }
             generated.Package _package = new generated.Package();
             _package.setMetrics( new PackageMetrics() );
             _package.setName( name );
             packagesReduced.add( _package );
             return _package;
         }
 
         private void waitUntilFinished() {
 
             if ( finished ) {
                 return;
             }
 
             synchronized (lock) {
                 while (!finished) {
                     try {
                         lock.wait();
                     }
                     catch (InterruptedException e) {
                         // do nothing
                     }
                 }
             }
         }
     }
 
     private static class LineComparator implements Comparator<Line> {
 
         @Override
         public int compare( Line line1, Line line2 ) {
 
             int num1 = line1.getNum();
             int num2 = line2.getNum();
             if ( num1 != num2 ) {
                 return num1 - num2;
             }
 
             Construct type1 = line1.getType();
             Construct type2 = line2.getType();
             if ( type1 != type2 ) {
                 return type1.ordinal() - type2.ordinal();
             }
 
             return 1;
         }
     }
 }
