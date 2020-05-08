 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.services.captcha;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
import org.apache.log4j.Logger;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.octo.captcha.service.CaptchaServiceException;
 import com.octo.captcha.service.image.ImageCaptchaService;
 import com.sun.image.codec.jpeg.JPEGCodec;
 import com.sun.image.codec.jpeg.JPEGImageEncoder;
 
 /**
  * Capctha implementation based on jcaptcha.
  *
 * @author Willi Schoenborn
  */
 @Singleton
 final class JCaptcha implements Captcha {
 
    private static final Logger LOG = Logger.getLogger(JCaptcha.class);
 
     private final ImageCaptchaService service;
     
     @Inject
     public JCaptcha(CaptchaService service) {
         this.service = Preconditions.checkNotNull(service, "Service");
     }
     
     @Override
     public byte[] getJpegCapchta(String token) {
         final BufferedImage challenge = service.getImageChallengeForID(token);
         final ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
 
         final JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(jpegOutputStream);
 
         try {
             jpegEncoder.encode(challenge);
             return jpegOutputStream.toByteArray();    
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     @Override
     public boolean validate(String id, String userInput) {
         try {
             return service.validateResponseForID(id, userInput);
         } catch (CaptchaServiceException e) {
             LOG.error("validation falied", e);
             return false;
         }
     }
 
 }
