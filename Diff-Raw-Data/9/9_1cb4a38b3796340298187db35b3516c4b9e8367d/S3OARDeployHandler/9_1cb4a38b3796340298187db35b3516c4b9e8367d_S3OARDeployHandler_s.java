 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.platforms.ec2.monitor;
 
 import com.elasticgrid.platforms.ec2.config.EC2Configuration;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.security.AWSCredentials;
 import org.rioproject.core.OperationalString;
 import org.rioproject.monitor.AbstractOARDeployHandler;
 import org.rioproject.monitor.DeployHandler;
 import org.rioproject.opstring.OAR;
 import org.rioproject.opstring.OARUtil;
 import org.rioproject.opstring.OpStringLoader;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 
 /**
  * A {@link DeployHandler} that handles OAR files deployed on <a href="http://aws.amazon.com/s3">Amazon S3</a>.
  * <p/>
  * This <tt>DeployHandler</tt> uses two configuration settings; a <tt>dropBucket</tt>, and an <tt>installDirectory</tt>.
  * <p/>
  * <p>The <tt>dropBucket</tt> is polled for files ending in ".oar". If found,
  * the OAR files are installed to the <tt>installDirectory</tt>, where
  * {@link org.rioproject.core.OperationalString}s are created and returned.
  *
  * @author Jerome Bernard
  */
 public class S3OARDeployHandler extends AbstractOARDeployHandler {
     private String dropBucket;
     private File installDirectory;
     private S3Service s3;
 
     /**
      * Create a S3OARDeployHandler with drop bucket and install directory
      *
      * @param dropBucket       The directory where OAR files will be dropped
      * @param installDirectory The directory to install OARs into
      */
     public S3OARDeployHandler(String dropBucket, File installDirectory) {
         this.dropBucket = dropBucket;
         this.installDirectory = installDirectory;
 
         try {
             // retrive S3 configuration parameters
             Properties awsConfig = AWSUtils.loadEC2Configuration();
             String awsAccessID = awsConfig.getProperty(EC2Configuration.AWS_ACCESS_ID);
             String awsSecretKey = awsConfig.getProperty(EC2Configuration.AWS_SECRET_KEY);
             if (awsAccessID == null) {
                 throw new IllegalArgumentException("Could not find AWS Access ID");
             }
             if (awsSecretKey == null) {
                 throw new IllegalArgumentException("Could not find AWS Secret Key");
             }
             AWSCredentials credentials = new AWSCredentials(awsAccessID, awsSecretKey);
 
             // authenticate to S3
             s3 = new RestS3Service(credentials);
             logger.config("Using S3 drop bucket " + dropBucket);
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Could not retrieve AWS credentials", e);
         } catch (S3ServiceException e) {
             logger.log(Level.SEVERE, "Could not create S3OARDeployHandler, unable to authenticate to S3", e);
         }
 
         // create install directory if needed
         if (!installDirectory.exists()) {
             if (installDirectory.mkdirs())
                 logger.config("Created installDir " + installDirectory.getAbsolutePath());
         }
 
         // create the opstring loader
         try {
             opStringLoader = new OpStringLoader(this.getClass().getClassLoader());
         } catch (Exception e) {
             logger.log(Level.SEVERE,
                     "Could not create OpStringLoader, unable to deploy OperationalString Archive (OAR) files", e);
         }
     }
 
     protected List<OperationalString> look(Date from) {
         List<OperationalString> list = new ArrayList<OperationalString>();
         try {
             // get a handle to the S3 bucket
             S3Bucket bucket = s3.getBucket(dropBucket);
             // retrieve the list of objects in the bucket
             List<S3Object> objects = Arrays.asList(s3.listObjects(bucket));
             // filter the list so that only OARs are kept
             for (Iterator<S3Object> iterator = objects.iterator(); iterator.hasNext();) {
                 S3Object object = iterator.next();
                 if (!object.getKey().endsWith("oar"))
                     iterator.remove();
             }
             // process each OAR found
             for (S3Object object : objects) {
                 try {
                     install(new URL("s3://" + dropBucket + '/' + object.getKey()), installDirectory);
                     s3.deleteObject(bucket, object.getKey());
                     if (logger.isLoggable(Level.FINE))
                         logger.fine("Deleted [" + object.getKey() + "] " +
                                     "after installing it to " +
                                     "[" + installDirectory.getAbsolutePath() + "]");
                 } catch (IOException e) {
                     logger.log(Level.WARNING,
                            "Exracting [" + object.getKey() + "] to [" + installDirectory.getName() + "]", e);
                 }
             }
             // parse the OAR file
             File[] files = installDirectory.listFiles();
             for (File file : files) {
                 if (file.isDirectory()) {
                     try {
                         OAR oar = OARUtil.getOAR(file);
                         if (oar != null && oar.getActivationType().equals(OAR.AUTOMATIC)) {
                             list.addAll(parseOAR(oar, from));
                         }
                     } catch (IOException e) {
                         logger.log(Level.WARNING, "Loading [" + file.getName() + "]", e);
                     }
                 }
             }
         } catch (S3ServiceException e) {
             logger.log(Level.SEVERE, "Unexpected S3 error", e);
         }
         return list;
     }
 
 }
