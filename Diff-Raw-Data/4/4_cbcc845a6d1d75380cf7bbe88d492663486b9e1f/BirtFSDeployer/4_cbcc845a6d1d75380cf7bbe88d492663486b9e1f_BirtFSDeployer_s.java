 /*
  * (C) Copyright 2006-20011 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  *
  */
 
 package org.nuxeo.ecm.platform.reporting.engine;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.birt.core.framework.IPlatformContext;
 import org.nuxeo.common.Environment;
 import org.nuxeo.common.utils.FileUtils;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.common.utils.ZipUtils;
 import org.nuxeo.ecm.platform.reporting.datasource.SupportedDBHelper;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * Because BIRT relies on Equinox deployment, this class is used to create an
  * OSGi/Equinox sandbox on the filesystem and start Birt deployment
  *
  * @author Tiry (tdelprat@nuxeo.com)
  *
  */
 public class BirtFSDeployer implements IPlatformContext {
 
     protected String platformPath;
 
     public static final String BIRT_ZIP_NAME = "birt-runtime-all-2.6.1.zip";
 
     protected static final String JDBC_JAR_DIR = "/plugins/org.eclipse.birt.report.data.oda.jdbc_2.6.1.v20100909/drivers/";
 
     protected static Log log = LogFactory.getLog(BirtFSDeployer.class);
 
     public BirtFSDeployer() {
     }
 
     @Override
     public String getPlatform() {
         if (platformPath == null) {
             synchronized (this) {
                 if (platformPath == null) {
                     deployBirtPlatform();
                 }
             }
         }
         return platformPath;
     }
 
     /**
      * deploy the platform resources to file based platform.
      *
      */
     protected void deployBirtPlatform() {
         if (platformPath == null) {
 
             File dataDir = Environment.getDefault().getData();
             if (Framework.isTestModeSet()) {
                 // runtime path will be removed too soon.
                String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                         "birt-fs" + System.currentTimeMillis()).toString();
                 dataDir = new File(dirPath);
                 dataDir.mkdir();
             }
             File birtPlatformFolder = new File(dataDir, "birt-platform");
             birtPlatformFolder.mkdir();
             try {
                 copyResources(BIRT_ZIP_NAME, birtPlatformFolder);
                 if (!Framework.isTestModeSet()) {
                     // also need to copy the JDBC drivers
                     List<String> driverJars = SupportedDBHelper.getDriverJars();
                     String targetPath = birtPlatformFolder.getAbsolutePath()
                             + JDBC_JAR_DIR;
                     for (String driverJar : driverJars) {
                         FileUtils.copyFile(new File(driverJar), new File(
                                 targetPath));
                     }
                 }
             } catch (Exception e) {
                 log.error(
                         "Error while copying birt resources into working dir",
                         e);
             }
             platformPath = birtPlatformFolder.getAbsolutePath();
         }
     }
 
     protected void copyResources(String resourcePath, File dir)
             throws Exception {
         URL url = Thread.currentThread().getContextClassLoader().getResource(
                 resourcePath);
         if ("jar".equals(url.getProtocol())) {
             String jarPath = url.getFile().split("!")[0].replace("file:", "");
             InputStream zipStream = ZipUtils.getEntryContentAsStream(new File(
                     jarPath), BIRT_ZIP_NAME);
             ZipUtils.unzip(zipStream, dir);
         } else { // unziped jar (unit tests)
             File source = new File(url.toURI());
             ZipUtils.unzip(source, dir);
         }
     }
 
 }
