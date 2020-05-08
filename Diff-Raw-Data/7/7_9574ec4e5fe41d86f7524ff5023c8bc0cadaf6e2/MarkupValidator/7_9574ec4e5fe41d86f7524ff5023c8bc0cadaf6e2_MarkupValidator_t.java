 /*
  * Sonar W3C Markup Validation Plugin
  * Copyright (C) 2010 Matthijs Galesloot
  * dev@sonar.codehaus.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.sonar.plugins.web.markup.validation;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.List;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.utils.SonarException;
 import org.sonar.plugins.web.api.ProjectFileManager;
 import org.sonar.plugins.web.markup.constants.MarkupValidatorConstants;
 
 /**
  * Remote Validator using the W3C Markup Validation Service.
  *
  * @see http://validator.w3.org/docs/api.html
  *
  * @author Matthijs Galesloot
  * @since 1.0
  *
  */
 public final class MarkupValidator extends RemoteValidationService {
 
   private static final long DEFAULT_PAUSE_BETWEEN_VALIDATIONS = 1000L;
 
   private static final Logger LOG = Logger.getLogger(MarkupValidator.class);
 
   private static final String OUTPUT = "output";
 
   // suffix for report with soap response
   private static final String REPORT_SUFFIX = ".smv";
   private static final String SOAP12 = "soap12";
 
   private static final String TEXT_HTML_CONTENT_TYPE = "text/html";
 
   private static final String UPLOADED_FILE = "uploaded_file";
 
   private final File buildDir;
   private final String validationUrl;
 
   public MarkupValidator(Configuration configuration, File buildDir) {
     this.buildDir = buildDir;
 
     this.validationUrl = configuration.getString(MarkupValidatorConstants.VALIDATION_URL, MarkupValidatorConstants.DEFAULT_URL);
     setWaitBetweenRequests(configuration.getLong(MarkupValidatorConstants.PAUSE_BETWEEN_VALIDATIONS, DEFAULT_PAUSE_BETWEEN_VALIDATIONS));
   }
 
   /**
    * Post contents of HTML file to the W3C validation service. In return, receive a Soap response message.
    *
    * @see http://validator.w3.org/docs/api.html
    */
   private void postHtmlContents(InputFile inputfile) {
 
     HttpPost post = new HttpPost(validationUrl);
     HttpResponse response = null;
 
     post.addHeader("User-Agent", "sonar-w3c-markup-validation-plugin/1.0");
 
     try {
 
       LOG.info("W3C Validate: " + inputfile.getRelativePath());
 
       // file upload
       MultipartEntity multiPartRequestEntity = new MultipartEntity();
       String charset = CharsetDetector.detect(inputfile.getFile());
       FileBody fileBody = new FileBody(inputfile.getFile(), TEXT_HTML_CONTENT_TYPE, charset);
       multiPartRequestEntity.addPart(UPLOADED_FILE, fileBody);
 
       // set output format
       multiPartRequestEntity.addPart(OUTPUT, new StringBody(SOAP12));
 
       post.setEntity(multiPartRequestEntity);
       response = executePostMethod(post);
 
       // write response to report file
      if (response != null) {
        writeResponse(response, inputfile);
      }
 
     } catch (UnsupportedEncodingException e) {
       LOG.error(e);
     } finally {
       // release any connection resources used by the method
       if (response != null) {
         try {
           EntityUtils.consume(response.getEntity());
         } catch (IOException ioe) {
           LOG.debug(ioe);
         }
       }
     }
   }
 
   /**
    * Create the path to the report file.
    */
   public File reportFile(InputFile inputfile) {
     return new File(buildDir.getPath() + "/" + ProjectFileManager.getRelativePath(inputfile.getFile(), inputfile.getFileBaseDir()) + REPORT_SUFFIX);
   }
 
   /**
    * Validate a file with the W3C Markup Validation Service.
    */
   public void validateFile(InputFile inputfile) {
     postHtmlContents(inputfile);
   }
 
   /**
    * Validate a list of files with the W3C Markup Validation Service
    */
   public void validateFiles(List<InputFile> inputfiles) {
 
     int n = 0;
     for (InputFile inputfile : inputfiles) {
       // skip analysis if the report already exists
       File reportFile = reportFile(inputfile);
       if ( !reportFile.exists()) {
         if (n++ > 0) {
           waitBetweenValidationRequests();
         }
         validateFile(inputfile);
       }
     }
   }
 
   private void writeResponse(HttpResponse response, InputFile inputfile) {
    if (response.getStatusLine().getStatusCode() == 200) {
       LOG.info("Validated:" + inputfile.getRelativePath());
 
       File reportFile = reportFile(inputfile);
       reportFile.getParentFile().mkdirs();
       Writer writer = null;
       try {
         writer = new FileWriter(reportFile);
         IOUtils.copy(response.getEntity().getContent(), writer);
       } catch (IOException e) {
         throw new SonarException(e);
       } finally {
         IOUtils.closeQuietly(writer);
       }
     } else {
       LOG.error("Response " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase());
       LOG.error("Failed to validate file: " + inputfile.getRelativePath());
     }
   }
 }
