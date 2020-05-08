 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod;
 
 import static nl.surfnet.bod.BodProperties.getDefaultProperties;
 import static nl.surfnet.bod.BodProperties.getEnvProperties;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import com.google.common.base.Joiner;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.env.ConfigurableEnvironment;
 import org.springframework.core.env.StandardEnvironment;
 import org.springframework.core.io.Resource;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 
 public class AppConfigWebApplicationContext extends AnnotationConfigWebApplicationContext {
 
   private Logger logger = LoggerFactory.getLogger(AppConfigWebApplicationContext.class);
 
   @Override
   protected ConfigurableEnvironment createEnvironment() {
     StandardEnvironment env = new StandardEnvironment();
     String nbiMode = getNbiMode();
 
     switch (nbiMode) {
     case "opendrac":
       env.setActiveProfiles("opendrac");
     case "opendrac-offline":
       env.setActiveProfiles("opendrac-offline");
       break;
     case "onecontrol":
       env.setActiveProfiles("onecontrol");
       break;
     default:
      throw new AssertionError("Could not set the nbi active profile");
     }
 
     logger.info("Starting with active profiles: {}", Joiner.on(",").join(env.getActiveProfiles()));
 
     return env;
   }
 
   private String getNbiMode() {
     Resource propertiesResource = getEnvOrDefaultProperties();
 
     try (InputStream is = propertiesResource.getInputStream()) {
       Properties props = new Properties();
       props.load(is);
       return props.getProperty("nbi.mode");
     } catch (IOException e) {
       throw new AssertionError("Could not determine the nbi.mode");
     }
   }
 
   private Resource getEnvOrDefaultProperties() {
     Resource propertiesResource = getEnvProperties();
 
     return propertiesResource.exists() ? propertiesResource : getDefaultProperties();
   }
 
 }
