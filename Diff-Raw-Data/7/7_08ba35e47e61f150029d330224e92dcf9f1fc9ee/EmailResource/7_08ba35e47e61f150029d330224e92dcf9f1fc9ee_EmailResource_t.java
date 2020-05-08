 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.email.jersey;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.jgit.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yaml.snakeyaml.Yaml;
 
 import com.meltmedia.cadmium.core.CadmiumApiEndpoint;
 import com.meltmedia.cadmium.core.ContentService;
 import com.meltmedia.cadmium.email.EmailException;
 import com.meltmedia.cadmium.email.VelocityHtmlTextEmail;
 import com.meltmedia.cadmium.email.config.EmailComponentConfiguration;
 import com.meltmedia.cadmium.email.config.EmailComponentConfiguration.Field;
 import com.meltmedia.cadmium.email.internal.EmailServiceImpl;
 
 @CadmiumApiEndpoint
 @Path("/email")
 public class EmailResource {
 	
 	private Logger log = LoggerFactory.getLogger(getClass());
 	
 	@Inject
 	private EmailServiceImpl emailService;
 	
 	@Inject
 	private ContentService contentService;
 	
 	public EmailResource() {
 	  log.debug("Initialized EmailResource...");
 	}
 
  	
 
 	@POST
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   @Produces(MediaType.APPLICATION_JSON)
 	public Response emailThisPage(@Context HttpServletRequest request,
 			                          @FormParam(Constants.DIR) String dir, MultivaluedMap<String, String> formData) {
 
   	log.info("Entering Email This Method");
   	VelocityHtmlTextEmail email = new VelocityHtmlTextEmail();
   	Yaml yamlParser;
   	// Setting up template location/files
   	if (dir != null){
   	  File absoluteTemplateDir = new File(contentService.getContentRoot(),"META-INF");
 	  	absoluteTemplateDir = new File(absoluteTemplateDir,dir);
 		  File textTemplateFile = new File(absoluteTemplateDir,Constants.TEMPLATE_NAME + ".txt");
 		  log.info("textTemplateFile: {}", textTemplateFile.getPath());
 	  	File htmlTemplateFile = new File(absoluteTemplateDir,Constants.TEMPLATE_NAME + ".html");
 	  	log.info("htmlTemplateFile: {}", htmlTemplateFile.getPath());
 	  	File componentConfig = new File(absoluteTemplateDir,Constants.CONFIG_NAME);
   	
   	
 	  	if (textTemplateFile.exists() && htmlTemplateFile.exists()  && componentConfig.exists()) {
 	  		yamlParser = new Yaml();
 	  		
 		  	try { 
 		  		EmailComponentConfiguration config = yamlParser.loadAs(FileUtils.readFileToString(componentConfig), EmailComponentConfiguration.class);
 		  		
 		  		//Check captcha if configured
 		  		if(!emailService.validateCaptcha(request, config)) {
 		  		  throw new ValidationException("Incorrect captcha response");
 		  		}
 		  		
 					EmailFormValidator.validate(formData,config,contentService);
 			  	
 					email.addTo(getFieldValueWithOverride("toAddress", config.getToAddress(), formData));
 			  	email.setFrom(emailService.getFromAddress(config.getFromAddress())); 
 			  	email.setSubject(getFieldValueWithOverride("subject", config.getSubject(), formData));
 					email.setProperty("subject", getFieldValueWithOverride("subject", config.getSubject(), formData));
 			  	// Set HTML Template
 			  	email.setHtml(readFromFile(htmlTemplateFile.getAbsolutePath()));
 			  	
 			  	// Set Text Template
 			  	email.setText(readFromFile(textTemplateFile.getAbsolutePath()));
 			  	
 			  	// Populate template properties
 			  	for(Field field : config.getFields()) {
 			  		if("replyTo".equals(field.name)) {
 			  			String value = field.getValue(request,formData);
 			  			if(!StringUtils.isEmptyOrNull(value)) {
 			  				email.setReplyTo(value);
 			  			}
 			  		} 
 			  		if("cc".equals(field.name)) {
 			  			String value = field.getValue(request,formData);
 			  			if(!StringUtils.isEmptyOrNull(value)) {
 			  				email.addCC(value);
 			  			}
 			  		} 
 			  		if("bcc".equals(field.name)) {
 			  			String value = field.getValue(request,formData);
 			  			if(!StringUtils.isEmptyOrNull(value)) {
 			  				email.addBCC(value);
 			  			}
 			  		} 
 			  		email.setProperty(field.name, field.getValue(request,formData));
 			  	}
 			  	
 			  	// Set Properties
 			  	String toName = getFieldValueWithOverride("toName", config.getToName(), formData);
 			  	String fromName = getFieldValueWithOverride("fromName", config.getFromName(), formData);
 			  	if(toName != null) {
 			  		email.setProperty(Constants.TO_NAME, toName);
 			  	}
 			  	if(fromName != null) {
 			  		email.setProperty(Constants.FROM_NAME, fromName);
 			  	}
 								  	
 			  	// Send Email
 			  	log.debug("Before Sending Email");  		
 			  	emailService.send(email);
 			  	log.debug("After Sending Email");
 				} catch (EmailException e) {
 					log.info("EmailException Caught " + e.getMessage(), e);
 					return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
 				} catch (ValidationException e) {
 					log.info("ValidationException Caught");
					if(e.getErrors()[0] != null) {
						log.info("First Error {}",e.getErrors()[0].getMessage());
					}
					else{
						log.info("No error messages were set for this Validation Exception.");
					}
 					return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build();
 				} catch (IOException e) {
 					return Response.status(Response.Status.BAD_REQUEST).entity("Unable to load Configuration").build();
 				} 	
 				return Response.ok().build();
 	  	} else {
 	  		log.info("Couldn't Find Email Templates");
 	  		return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Template Location").build();		  		
 	  	}
   	} else
   		return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Template Location").build();
 	}
 
 	/*
    * Reads data from a given file
    */
   public String readFromFile(String fileName) {
     String content = null;
     BufferedReader br = null;
     try {
       File inFile = new File(fileName);
       br = new BufferedReader(new InputStreamReader(
           new FileInputStream(inFile)));
       content = "";
       while(br.ready()) {
       	content += br.readLine();
       }
       br.close();
     } catch (FileNotFoundException ex) {
     	log.error("Failed to find file.", ex);
     } catch (IOException ex) {
     	log.error("Failed to read file.", ex);
     } finally {
     	if(br != null) {
     		try {
     			br.close();
     		} catch(Exception e){}
     	}
     }
     return content;
   }
   
   private String getFieldValueWithOverride(String name, String overrideValue, MultivaluedMap<String, String> formData) {
   	return StringUtils.isEmptyOrNull(overrideValue) ? formData.get(name).get(0) : overrideValue;
   }
   
 }
