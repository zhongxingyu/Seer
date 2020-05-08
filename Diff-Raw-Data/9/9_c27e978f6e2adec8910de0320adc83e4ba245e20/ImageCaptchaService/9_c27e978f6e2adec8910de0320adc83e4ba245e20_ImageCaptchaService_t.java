 /* ====================================================================
  * The Apache Software License, Version 1.1
  *
  * Copyright (c) 2000 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/)."
  *    Alternately, this acknowledgment may appear in the software itself,
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Apache" and "Apache Software Foundation" must
  *    not be used to endorse or promote products derived from this
  *    software without prior written permission. For written
  *    permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    nor may "Apache" appear in their name, without prior written
  *    permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  *
  */
 package com.octo.captcha.j2ee;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MBeanServerFactory;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.octo.captcha.image.ImageCaptcha;
 import com.octo.captcha.image.ImageCaptchaEngine;
 import com.octo.captcha.utils.ConstantCapacityHashtable;
 import com.octo.captcha.utils.ConstantCapacityHashtableFullException;
 import com.sun.image.codec.jpeg.JPEGCodec;
 import com.sun.image.codec.jpeg.JPEGImageEncoder;
 
 /**
  * @TODO : DOCUMENT ME !
  * @TODO : UNIT TEST ME !
  * @TODO : IMPLEMENTS A MANAGEMENT INTERFACE
  * @TODO : PROVIDE METHODS TO REGISTER / UNREGISTER FROM AN MBEAN SERVER
  * (name provided by the caller)
  * @version $Id$
  *
  * @author <a href="mailto:sbr@octo.com">Sebastien Brunot</a>
  */
 public class ImageCaptchaService implements ImageCaptchaServiceMBean
 {
     ////////////////////////////////////
     // Constants
     ////////////////////////////////////
 
     /**
      * Default value for the maximum length of a captcha ID
      */
     public static final int DEFAULT_CAPTCHA_ID_MAX_LENGTH = 10;
 
     /**
      * The name of the service initialization file
      */
     private static final String INITIALIZATION_FILE_NAME =
         "imageCaptchaService.properties";
 
     /**
      * The name of the key under which the initial value of the
      * maxNumberOfSimultaneousCaptchas attribute is defined in the
      * service initialization file. This is mandatory parameter in
      * the service initialization file.
      */
     public static final String MAX_NUMBER_OF_SIMULTANEOUS_CAPTCHAS_PROP =
         "com.octo.captcha.j2ee.maxNumberOfSimultaneousCaptchas";
 
     /**
      * The name of the key under which the initial value of the
      * minGuarantedStorageDelayInSeconds is defined in the service
      * intialization file. This is a mandatory parameter in the
      * service initialization file.
      */
     public static final String MIN_GUARANTED_STORAGE_DELAY_IN_SECONDS_PROP =
         "com.octo.captcha.j2ee.minGuarantedStorageDelayInSeconds";
 
     /**
      * The name of the key under which the ImageCaptchaEngine implementation
      * class name initialy used by the service is defined in the service
      * initialization file. This is a mandatory parameter in the service
      * initialization file.
      */
     public static final String ENGINE_CLASS_INIT_PARAMETER_PROP =
         "com.octo.captcha.j2ee.imageCaptchaEngineClass";
 
     ////////////////////////////////////
     // Private attributes
     ////////////////////////////////////
 
     /**
      * Logger (commons-logging)
      */
     private static Log log = LogFactory.getLog(ImageCaptchaService.class);
 
     /**
      * The maximum length of a captcha ID
      */
     private int captchaIDMaxLength = DEFAULT_CAPTCHA_ID_MAX_LENGTH;
 
     /**
      * A ConstantCapacityHashtable used to store and check generated captcha
      */
     private ConstantCapacityHashtable internalStore = null;
 
     /**
      * Engine used by the filter to generate captchas (the concrete
      * implementation class must be specified in the initialization
      * file)
      */
     private ImageCaptchaEngine engine = null;
 
     /**
      * The maximum number of captchas the service can manage
      * simultaneously.
      */
     private int maxNumberOfSimultaneousCaptchas = -1;
 
     /**
      * The minimum delay (in seconds) a client can
      * be assured that a captcha generated by the service
      * can be retrieved and a response to its challenge
      * tested
      */
     private int minGuarantedStorageDelayInSeconds = -1;
 
     /**
      * The number of captcha generated since the service is up
      */
     private long numberOfGeneratedCaptchas = 0;
 
     /**
      * The number of correct responses to captcha challenges since
      * the service is up.
      */
     private long numberOfCorrectResponses = 0;
 
     /**
      * The number of uncorrect responses to captcha challenges since
      * the service is up.
      */
     private long numberOfUncorrectResponses = 0;
 
     /**
      * The name under witch the current instance is registered to an
      * MBean server (null if not registered).
      */
     private String registeredName = null;
 
     ////////////////////////////////////
     // Constructors
     ////////////////////////////////////
 
     /**
      * Default constructor : creates the service and initialize it
      * with values provided in the service intialization file.
      * @TODO : DOCUMENT INTIALIZATION FILE
      */
     public ImageCaptchaService()
     {
         Properties initializationValues = new Properties();
         try
         {
             // load the service initialization file in a Properties object
             InputStream initializationFile =
                 this.getClass().getClassLoader().getResourceAsStream(
                     INITIALIZATION_FILE_NAME);
             initializationValues.load(initializationFile);
         }
         catch (Exception e)
         {
             throw new RuntimeException(
                 "Error loading initialization values from "
                     + INITIALIZATION_FILE_NAME);
         }
 
         initializeService(initializationValues);
     }
 
     /**
      * A constructor to use of the initialization parameters are
      * not defined in an initialization class but are provided, in
      * a Properties object, by the service user.
      * @param theInitializationValues the Properties that
      * contains the initialization parameters values.
      * @TODO DOCUMENT INTIALIZATION PARAMETERS
      */
     public ImageCaptchaService(Properties theInitializationValues)
     {
         // initialize the service with the initialization parameters
         // provided in the ResourceBundle
         initializeService(theInitializationValues);
     }
 
     ////////////////////////////////////
     // Public methods
     ////////////////////////////////////
 
     /** 
      * Generate a new ImageCaptcha and render it as a jpeg image
      * @param theCaptchaID a unique ID to store the generated captcha
      * under.
      * @return a jpeg image as an array of bytes (this is image is
      * the generated captcha challenge)
      * @throws IllegalArgumentException if theCaptchaID is null or
      * too long.
      * @throws ImageCaptchaServiceException in case of error. Possible
      * error details are :
      * <ul>
      *  <li> ImageCaptchaServiceException.TOO_MANY_USERS_ERROR </li>
      * </ul>
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceException#TOO_MANY_USERS_ERROR
      */
     public byte[] generateCaptchaAndRenderChallengeAsJpeg(String theCaptchaID)
         throws ImageCaptchaServiceException
     {
         // throw an exception if theCaptchaID is null or too long
         if ((theCaptchaID == null)
             || (theCaptchaID.length() > this.captchaIDMaxLength))
         {
             throw new IllegalArgumentException(
                 "The generateAndRenderCaptchaAsJpeg parameter (the captcha ID)"
                     + "can't be null and should be less than "
                     + this.captchaIDMaxLength
                     + " characters long.");
         }
 
         // generate a new captcha
         ImageCaptcha captcha =
             this.engine.getImageCaptchaFactory().getImageCaptcha();
 
         // the output stream to render the captcha image as jpeg into
         ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
         // a jpeg encoder
         JPEGImageEncoder jpegEncoder =
             JPEGCodec.createJPEGEncoder(jpegOutputStream);
         try
         {
             // render the captcha image as jpeg
             jpegEncoder.encode(captcha.getImageChallenge());
         }
         catch (IOException e)
         {
             throw new ImageCaptchaServiceException(e);
         }
 
         // destroy the captcha internal image buffer
         captcha.disposeChallenge();
 
         // store the captcha in internal store
         try
         {
             this.internalStore.put(theCaptchaID, captcha);
         }
         catch (ConstantCapacityHashtableFullException e)
         {
             throw new ImageCaptchaServiceException(
                 ImageCaptchaServiceException.TOO_MANY_USERS_ERROR,
                 e);
         }
 
         // update statistics
         this.numberOfGeneratedCaptchas += 1;
 
         // return the jpeg image as an array of bytes
         return jpegOutputStream.toByteArray();
     }
 
     /**
      * Verify client response to the challenge of a captcha previously
      * generated by the generateCaptchaAndRenderChallengeAsJpeg method.
      * This method causes the captcha associated with the ID to be removed
      * from the internal store (so this method can only called one time
      * for a particular captcha).
      * @param theCaptchaID the unique ID previously provided to the
      * generateCaptchaAndRenderChallengeAsJpeg method. This is the unique
      * ID the previously generated captcha is associated with.
      * @param theResponse the client response to the captcha challenge
      * @return true is the response to the challenge is correct, false
      * otherwise.
      * @throws ImageCaptchaServiceException in case of error. Possible
      * error details are :
      * <ul>
      *  <li> ImageCaptchaServiceException.NO_CAPTCHA_WITH_THIS_ID_ERROR </li>
      * </ul>
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceException#NO_CAPTCHA_WITH_THIS_ID_ERROR
      */
     public boolean verifyResponseToACaptchaChallenge(
         String theCaptchaID,
         String theResponse)
         throws ImageCaptchaServiceException
     {
         // retrieve the captcha from the internal store
         ImageCaptcha captcha =
             (ImageCaptcha) this.internalStore.get(theCaptchaID);
         if (captcha == null)
         {
             throw new ImageCaptchaServiceException(
                 ImageCaptchaServiceException.NO_CAPTCHA_WITH_THIS_ID_ERROR);
         }
         // remove the captcha from the internal store
         this.internalStore.remove(theCaptchaID);
 
         // verify the answer
         boolean isResponseCorrect =
             captcha.validateResponse(theResponse).booleanValue();
 
         // update statistics
         if (isResponseCorrect)
         {
             this.numberOfCorrectResponses += 1;
         }
         else
         {
             this.numberOfUncorrectResponses += 1;
         }
 
         return isResponseCorrect;
     }
 
     /**
      * Get the maximum length allowed by the service for a captcha ID
      * @return the maximum length allowed by the service for a captcha
      * ID
      */
     public int getCaptchaIDMaxLength()
     {
         return this.captchaIDMaxLength;
     }
 
     /**
      * Set the maximum length allowed by the service for a captcha ID
      * @param theCaptchaIDMaxLength the maximum length allowed by the
      * service for a captcha ID
      */
     public void setCaptchaIDMaxLength(int theCaptchaIDMaxLength)
     {
         captchaIDMaxLength = theCaptchaIDMaxLength;
     }
 
     /**
      * Register self to the first MBean server available in the JVM, if
      * any.
      * @param theRegisteringName the name the service will be registered
      * to the MBean server.
      * @throws ImageCaptchaServiceException in case of error. Possible
      * error details are :
      * <ul>
      *  <li> ImageCaptchaServiceException.MALFORMED_REGISTERING_NAME</li>
      *  <li> ImageCaptchaServiceException.INSTANCE_ALREADY_REGISTERED</li>
      * </ul>
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceException#MALFORMED_REGISTERING_NAME
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceException#INSTANCE_ALREADY_REGISTERED
      */
     public void registerToMBeanServer(String theRegisteringName)
         throws ImageCaptchaServiceException
     {
         ArrayList mbeanServers = MBeanServerFactory.findMBeanServer(null);
         if (mbeanServers.size() == 0)
         {
             log.warn(
                 "No current MBean Server, skiping the registering process");
         }
         else
         {
             MBeanServer mbeanServer = (MBeanServer) mbeanServers.get(0);
             try
             {
                 ObjectName name = new ObjectName(theRegisteringName);
                 mbeanServer.registerMBean(this, name);
                 this.registeredName = theRegisteringName;
             }
             catch (MalformedObjectNameException e)
             {
                 throw new ImageCaptchaServiceException(
                     ImageCaptchaServiceException.MALFORMED_REGISTERING_NAME,
                     e);
             }
             catch (InstanceAlreadyExistsException e)
             {
                 throw new ImageCaptchaServiceException(
                     ImageCaptchaServiceException.INSTANCE_ALREADY_REGISTERED,
                     e);
             }
             catch (MBeanRegistrationException e)
             {
                 // this exception should never be raised (raised
                 // only by an MBean that implements the MBeanRegistration
                 // interface.
                 log.error(
                     "An unexpected exception has been raised : "
                         + "ImageCaptchaService needs maintenance !",
                     e);
             }
             catch (NotCompliantMBeanException e)
             {
                 // this should never happens
                 log.error(
                     "Exception trying to register the service to"
                         + " the MBean server",
                     e);
             }
         }
     }
 
     /**
      * Unregister self from the first MBean server available in the JVM, if any
      */
     public void unregisterFromMBeanServer()
     {
         if (this.registeredName != null)
         {
             ArrayList mbeanServers = MBeanServerFactory.findMBeanServer(null);
             MBeanServer mbeanServer = (MBeanServer) mbeanServers.get(0);
             try
             {
                 ObjectName name = new ObjectName(this.registeredName);
                 mbeanServer.unregisterMBean(name);
             }
             catch (MalformedObjectNameException e)
             {
                 // this should never happens
                 log.error(
                     "Exception trying to create the object name under witch"
                         + " the service is registered",
                     e);
             }
             catch (InstanceNotFoundException e)
             {
                 // this should never happens
                 log.error(
                     "Exception trying to unregister the ImageCaptchaFilter from"
                         + " the MBean server",
                     e);
             }
             catch (MBeanRegistrationException e)
             {
                 // this remains silent for the client
                 log.error(
                     "Exception trying to unregister the ImageCaptchaFilter from"
                         + "the MBean server",
                     e);
             }
         }
     }
 
     //////////////////////////////////////
     // Management interface implementation
     //////////////////////////////////////
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#garbageCollectCaptchaStore()
      */
     public void garbageCollectCaptchaStore()
     {
         this.internalStore.garbageCollect();
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getCaptchaStoreLoad()
      */
     public double getCaptchaStoreLoad()
     {
         double internalStoreSizeAsDouble = (double) this.internalStore.size();
         double capacityAsDouble = (double) this.maxNumberOfSimultaneousCaptchas;
         return ((internalStoreSizeAsDouble / capacityAsDouble) * 100.0);
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getImageCaptchaEngineClass()
      */
     public String getImageCaptchaEngineClass()
     {
         return this.engine.getClass().getName();
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getMaxNumberOfSimultaneousCaptchas()
      */
     public int getMaxNumberOfSimultaneousCaptchas()
     {
         return this.maxNumberOfSimultaneousCaptchas;
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getMinGuarantedStorageDelayInSeconds()
      */
     public int getMinGuarantedStorageDelayInSeconds()
     {
         return this.minGuarantedStorageDelayInSeconds;
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getNumberOfCorrectResponses()
      */
     public long getNumberOfCorrectResponses()
     {
         return this.numberOfCorrectResponses;
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getNumberOfGarbageCollectableCaptchas()
      */
     public int getNumberOfGarbageCollectableCaptchas()
     {
         return this.internalStore.getNumberOfGarbageCollectableEntries();
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getNumberOfGarbageCollectedCaptcha()
      */
     public long getNumberOfGarbageCollectedCaptcha()
     {
        return this
            .internalStore
            .getNumberOfGarbageCollectedEntriesSinceCreation();
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getNumberOfGeneratedCaptchas()
      */
     public long getNumberOfGeneratedCaptchas()
     {
         return this.numberOfGeneratedCaptchas;
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#getNumberOfUncorrectResponses()
      */
     public long getNumberOfUncorrectResponses()
     {
         return this.numberOfUncorrectResponses;
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#setImageCaptchaEngineClass(java.lang.String)
      */
     public void setImageCaptchaEngineClass(String theClassName)
         throws IllegalArgumentException
     {
         // try to use this concrete ImageCaptchaEngine class as the
         // engine
         try
         {
             this.engine =
                 (ImageCaptchaEngine) Class.forName(theClassName).newInstance();
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException(e.getMessage());
         }
     }
 
     /**
      * @see com.octo.captcha.j2ee.ImageCaptchaServiceMBean#setMinGuarantedStorageDelayInSeconds(int)
      */
     public void setMinGuarantedStorageDelayInSeconds(int theDelayInSeconds)
     {
         this.minGuarantedStorageDelayInSeconds = theDelayInSeconds;
         this.internalStore.setTimeToLive(theDelayInSeconds);
     }
 
     //////////////////////////////////////
     // Private methods
     //////////////////////////////////////
 
     /**
      * Initialize the service from a Properties : create
      * the internal store and instanciate an image captcha
      * engine.
      * @param theInitializationValues the Properties that
      * contains the initialization parameters
      */
     private void initializeService(Properties theInitializationValues)
     {
         // Verfify that all required initialization values are present
         if (theInitializationValues == null)
         {
             throw new RuntimeException(
                 "No initialization values provided"
                     + " (Properties object is null)");
         }
         else
         {
             if (!theInitializationValues
                 .containsKey(MAX_NUMBER_OF_SIMULTANEOUS_CAPTCHAS_PROP))
             {
                 throw new RuntimeException(
                     MAX_NUMBER_OF_SIMULTANEOUS_CAPTCHAS_PROP
                         + " initialization parameter missing");
             }
             if (!theInitializationValues
                 .containsKey(MIN_GUARANTED_STORAGE_DELAY_IN_SECONDS_PROP))
             {
                 throw new RuntimeException(
                     MIN_GUARANTED_STORAGE_DELAY_IN_SECONDS_PROP
                         + " initialization parameter missing");
             }
             if (!theInitializationValues
                 .containsKey(ENGINE_CLASS_INIT_PARAMETER_PROP))
             {
                 throw new RuntimeException(
                     ENGINE_CLASS_INIT_PARAMETER_PROP
                         + " initialization parameter missing");
             }
         }
 
         // get the maximum number of simultaneous captchas from the
         // properties
         String maxNumberOfSimultaneousCaptchasAsString =
             theInitializationValues.getProperty(
                 MAX_NUMBER_OF_SIMULTANEOUS_CAPTCHAS_PROP);
         Integer maxNumberOfSimultaneousCaptchas;
         try
         {
             maxNumberOfSimultaneousCaptchas =
                 new Integer(maxNumberOfSimultaneousCaptchasAsString);
         }
         catch (NumberFormatException e)
         {
             throw new RuntimeException(
                 "Initialization error : initialization parameter "
                     + MAX_NUMBER_OF_SIMULTANEOUS_CAPTCHAS_PROP
                     + " must be an integer !");
         }
         this.maxNumberOfSimultaneousCaptchas =
             maxNumberOfSimultaneousCaptchas.intValue();
 
         // get the maximum waiting delay between guaranted to the user
         // between generation and verification of a captcha from the
         // properties
         String minGuarantedStorageDelayInSecondsAsString =
             theInitializationValues.getProperty(
                 MIN_GUARANTED_STORAGE_DELAY_IN_SECONDS_PROP);
         Integer minGuarantedStorageDelayInSeconds;
         try
         {
             minGuarantedStorageDelayInSeconds =
                 new Integer(minGuarantedStorageDelayInSecondsAsString);
         }
         catch (NumberFormatException e)
         {
             throw new RuntimeException(
                 "Initialization error : initialization parameter "
                     + MIN_GUARANTED_STORAGE_DELAY_IN_SECONDS_PROP
                     + " must be an integer !");
         }
         this.minGuarantedStorageDelayInSeconds =
             minGuarantedStorageDelayInSeconds.intValue();
 
         // create the internal store
         this.internalStore =
             new ConstantCapacityHashtable(
                 this.maxNumberOfSimultaneousCaptchas,
                 (this.minGuarantedStorageDelayInSeconds * 1000));
 
         // get the ImageCaptchaEngine class name from the properties
         String engineClassName =
             theInitializationValues.getProperty(
                 ENGINE_CLASS_INIT_PARAMETER_PROP);
 
         // create an instance of the engine
         try
         {
             this.engine =
                 (ImageCaptchaEngine) Class
                     .forName(engineClassName)
                     .newInstance();
         }
         catch (ClassNotFoundException e)
         {
             throw new RuntimeException(
                "Initialization error : can't find class " + engineClassName);
         }
         catch (InstantiationException e)
         {
             throw new RuntimeException(
                 "Initialization error : can't instanciate class "
                     + engineClassName);
         }
         catch (IllegalAccessException e)
         {
             throw new RuntimeException(
                 "Initialization error : can't instanciate class "
                     + engineClassName);
         }
     }
 
 }
