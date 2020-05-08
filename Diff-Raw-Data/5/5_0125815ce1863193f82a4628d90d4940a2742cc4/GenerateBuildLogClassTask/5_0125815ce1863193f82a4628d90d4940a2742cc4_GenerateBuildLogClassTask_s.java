 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: GenerateBuildLogClassTask.java,v 1.1 2003-03-13 18:33:10 shahid.shah Exp $
  */
 
 package com.netspective.commons.ant;
 
 import java.io.File;
 import java.io.Writer;
 import java.net.InetAddress;
 import java.util.Date;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 import com.netspective.commons.lang.ClassPath;
 
 public class GenerateBuildLogClassTask extends Task
 {
     static private final String DEFAULT_CLASS_NAME = "BuildLog";
     static private final String DEFAULT_PROPERTIES_PREFIX = "project.build.log.property.";
 
     private File javaFile;
     private String logPackageName;
     private String logClassName = DEFAULT_CLASS_NAME;
     private String propertiesPrefix = DEFAULT_PROPERTIES_PREFIX;
 
     public GenerateBuildLogClassTask()
     {
     }
 
     public void setFile(File file)
     {
         javaFile = file;
     }
 
     public void setPackage(String pgkName)
     {
         logPackageName = pgkName;
     }
 
     public void setClass(String className)
     {
         logClassName = className;
     }
 
     public void setPropertiesPrefix(String prefix)
     {
         propertiesPrefix = prefix;
     }
 
     public void execute() throws BuildException
     {
         try
         {
             Date buildDate = new Date();
             InetAddress localhost = InetAddress.getLocalHost();
 
             Writer writer = new java.io.FileWriter(javaFile);
             writer.write("package " + logPackageName + ";\n\n");
             writer.write("public class " + logClassName + "\n");
             writer.write("{\n");
             writer.write("  public static final int    BUILD_NUMBER = " + project.getProperty("build.number") + ";\n");
             writer.write("  public static final String BUILD_HOST_NAME = \"" + localhost.getHostName() + "\";\n");
             writer.write("  public static final String BUILD_HOST_IP = \"" + localhost.getHostAddress() + "\";\n");
             writer.write("  public static final String BUILD_DATE = \"" + buildDate.toString() + "\";\n\n");
             writer.write("  public static final String BUILD_OS_NAME = \"" + System.getProperty("os.name") + "\";\n");
             writer.write("  public static final String BUILD_OS_VERSION = \"" + System.getProperty("os.version") + "\";\n\n");
             writer.write("  public static final String BUILD_JAVA_VERSION = \"" + System.getProperty("java.version") + "\";\n");
             writer.write("  public static final String BUILD_JAVA_VENDOR = \"" + System.getProperty("java.vendor") + "\";\n\n");
             writer.write("  public static final String BUILD_VM_NAME = \"" + System.getProperty("java.vm.name") + "\";\n");
             writer.write("  public static final String BUILD_VM_VERSION = \"" + System.getProperty("java.vm.version") + "\";\n");
            writer.write("  public static final String BUILD_VM_VENDOR = \"" + System.getProperty("java.vm.vendor") + "\";\n\n");
 
             writer.write("  public static final String[] BUILD_CLASS_PATH = new String[] {\n");
             ClassPath.ClassPathInfo[] cpi = ClassPath.getClassPaths();
             for(int i = 0; i < cpi.length; i++)
             {
                 ClassPath.ClassPathInfo info = cpi[i];
                 if(i > 0)
                     writer.write(", \n");
                 String path = info.getClassPath().getAbsolutePath();
                 writer.write("      \"" + path.replace('\\', '/') + (info.isValid() ? "" : " (INVALID)") + "\"");
             }
             writer.write("      };\n");
 
             writer.write("}\n");
             writer.close();
 
             project.setProperty(propertiesPrefix + "host.name", localhost.getHostName());
             project.setProperty(propertiesPrefix + "host.ip", localhost.getHostAddress());
             project.setProperty(propertiesPrefix + "date", buildDate.toString());
 
             log("Generated '"+propertiesPrefix+".*' properties for " + javaFile.getAbsolutePath());
         }
         catch(Exception e)
         {
             throw new BuildException(e);
         }
     }
 }
