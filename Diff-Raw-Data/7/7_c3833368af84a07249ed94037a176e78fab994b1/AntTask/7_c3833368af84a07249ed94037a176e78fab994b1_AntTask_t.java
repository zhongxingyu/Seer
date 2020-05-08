//////////////////////////////////////////////////////////////////////////////
// Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003  Lars Kühne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.ant;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import net.sf.clirr.Checker;
 import net.sf.clirr.event.PlainDiffListener;
 import net.sf.clirr.event.XmlDiffListener;
 import org.apache.bcel.classfile.ClassParser;
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.util.ClassLoaderRepository;
 import org.apache.bcel.util.ClassSet;
 import org.apache.bcel.util.Repository;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.Path;
 
 
 /**
  * Implements the Clirr ant task.
  * @author lkuehne
  */
 public final class AntTask extends Task
 {
     private static final String FORMATTER_TYPE_PLAIN = "plain";
     private static final String FORMATTER_TYPE_XML = "xml";
 
     public static final class Formatter
     {
         private String type = null;
         private String outFile = null;
 
         public String getOutFile()
         {
             return outFile;
         }
 
         public void setOutFile(String outFile)
         {
             this.outFile = outFile;
         }
 
         public String getType()
         {
             return type;
         }
 
         public void setType(String type)
         {
             String lowerCase = type.toLowerCase();
             if (!lowerCase.equals(FORMATTER_TYPE_XML) && !lowerCase.equals(FORMATTER_TYPE_PLAIN))
             {
                 throw new BuildException("Illegal formatter type, only plain and xml are supported");
             }
 
             this.type = type;
         }
     }
 
 
     private FileSet origFiles = null;
     private FileSet newFiles = null;
     private Path newClassPath = null;
     private Path origClassPath = null;
 
     private boolean failOnError = true;
     private boolean failOnWarning = false;
     private List formatters = new LinkedList();
 
 
     public Path createNewClassPath()
     {
         if (newClassPath == null)
         {
             newClassPath = new Path(getProject());
         }
         return newClassPath.createPath();
     }
 
     public void setNewClassPath(Path path)
     {
         if (newClassPath == null)
         {
             newClassPath = path;
         }
         else
         {
             newClassPath.append(path);
         }
     }
 
     public Path createOrigClassPath()
     {
         if (origClassPath == null)
         {
             origClassPath = new Path(getProject());
         }
         return origClassPath.createPath();
     }
 
     public void setOrigClassPath(Path path)
     {
         if (origClassPath == null)
         {
             origClassPath = path;
         }
         else
         {
             origClassPath.append(path);
         }
     }
 
     public void addOrigFiles(FileSet origFiles)
     {
         if (this.origFiles != null)
         {
             throw new BuildException();
         }
         this.origFiles = origFiles;
     }
 
     public void addNewFiles(FileSet newFiles)
     {
         if (this.newFiles != null)
         {
             throw new BuildException();
         }
         this.newFiles = newFiles;
     }
 
     public void setFailOnError(boolean failOnError)
     {
         this.failOnError = failOnError;
     }
 
     public void setFailOnWarning(boolean failOnWarning)
     {
         this.failOnWarning = failOnWarning;
     }
 
     public void addFormatter(Formatter formatter)
     {
         formatters.add(formatter);
     }
 
     public void execute()
     {
         if (origFiles == null || newFiles == null)
         {
             throw new BuildException("Missing nested filesetes origFiles and newFiles.", getLocation());
         }
 
         if (newClassPath == null)
         {
             newClassPath = new Path(getProject());
         }
 
         if (origClassPath == null)
         {
             origClassPath = new Path(getProject());
         }
 
         final File[] origJars = scanFileSet(origFiles);
         final File[] newJars = scanFileSet(newFiles);
 
         final ClassSet origClasses = createClassSet(origJars, origClassPath);
         final ClassSet newClasses = createClassSet(newJars, newClassPath);
 
         final Checker checker = new Checker();
         final ChangeCounter counter = new ChangeCounter();
 
         boolean formattersWriteToStdOut = false;
 
         for (Iterator it = formatters.iterator(); it.hasNext();)
         {
             Formatter formatter = (Formatter) it.next();
             final String type = formatter.getType();
             final String outFile = formatter.getOutFile();
 
             formattersWriteToStdOut = formattersWriteToStdOut || (outFile == null);
 
             try
             {
                 if (FORMATTER_TYPE_PLAIN.equals(type))
                 {
                     checker.addDiffListener(new PlainDiffListener(outFile));
                 }
                 else if (FORMATTER_TYPE_XML.equals(type))
                 {
                     checker.addDiffListener(new XmlDiffListener(outFile));
                 }
             }
             catch (IOException ex)
             {
                 log("unable to initialize formatter: " + ex.getMessage(), Project.MSG_WARN);
             }
         }
 
         if (!formattersWriteToStdOut)
         {
             checker.addDiffListener(new AntLogger(this));
         }
 
         checker.addDiffListener(counter);
         checker.diffs(origClasses, newClasses);
 
         if (counter.getWarnings() > 0 && failOnWarning || counter.getErrors() > 0 && failOnError)
         {
             throw new BuildException("detected incompatible API changes");
         }
     }
 
 
     private ClassSet createClassSet(File[] jarFiles, Path classpath)
     {
         ClassLoader classLoader = createClassLoader(jarFiles, classpath);
 
         Repository repository = new ClassLoaderRepository(classLoader);
 
         ClassSet ret = new ClassSet();
 
         for (int i = 0; i < jarFiles.length; i++)
         {
             File jarFile = jarFiles[i];
             ZipFile zip = null;
             try
             {
                 zip = new ZipFile(jarFile, ZipFile.OPEN_READ);
             }
             catch (IOException ex)
             {
                 throw new BuildException("Cannot open " + jarFile + " for reading", ex);
             }
             Enumeration enum = zip.entries();
             while (enum.hasMoreElements())
             {
                 ZipEntry zipEntry = (ZipEntry) enum.nextElement();
                 if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class"))
                 {
                     JavaClass clazz = extractClass(zipEntry, zip, repository);
                     if (clazz.isPublic() || clazz.isProtected())
                     {
                         ret.add(clazz);
                     }
                 }
             }
         }
 
         return ret;
     }
 
     private ClassLoader createClassLoader(File[] jarFiles, Path classpath)
     {
         final URL[] jarUrls = new URL[jarFiles.length];
         for (int i = 0; i < jarFiles.length; i++)
         {
             File jarFile = jarFiles[i];
             try
             {
                 URL url = jarFile.toURL();
                 jarUrls[i] = url;
             }
             catch (MalformedURLException ex)
             {
                 throw new RuntimeException("Cannot create classloader with jar file " + jarFile);
             }
         }
         final URLClassLoader jarsLoader = new URLClassLoader(jarUrls);
 
         final String[] cpEntries = classpath.list();
         final URL[] cpUrls = new URL[cpEntries.length];
         for (int i = 0; i < cpEntries.length; i++)
         {
             String cpEntry = cpEntries[i];
             File entry = new File(cpEntry);
             try
             {
                 URL url = entry.toURL();
                 cpUrls[i] = url;
             }
             catch (MalformedURLException e)
             {
                 throw new RuntimeException("Cannot create classLoader from classpath entry " + entry);
             }
         }
 
         final URLClassLoader retVal = new URLClassLoader(cpUrls, jarsLoader);
 
         return retVal;
     }
 
     private JavaClass extractClass(ZipEntry zipEntry, ZipFile zip, Repository repository)
     {
         String name = zipEntry.getName();
         InputStream is = null;
         try
         {
             is = zip.getInputStream(zipEntry);
 
             ClassParser parser = new ClassParser(is, name);
             JavaClass clazz = parser.parse();
             clazz.setRepository(repository);
             return clazz;
         }
         catch (IOException ex)
         {
             throw new BuildException("Cannot read " + zipEntry + " from " + zip, ex);
         }
         finally
         {
             if (is != null)
             {
                 try
                 {
                     is.close();
                 }
                 catch (IOException ex)
                 {
                     throw new BuildException(ex);
                 }
             }
         }
     }
 
     private File[] scanFileSet(FileSet fs)
     {
         Project prj = getProject();
         DirectoryScanner scanner = fs.getDirectoryScanner(prj);
         scanner.scan();
         File basedir = scanner.getBasedir();
         String[] fileNames = scanner.getIncludedFiles();
         File[] ret = new File[fileNames.length];
         for (int i = 0; i < fileNames.length; i++)
         {
             String fileName = fileNames[i];
             ret[i] = new File(basedir, fileName);
         }
         return ret;
     }
 }
