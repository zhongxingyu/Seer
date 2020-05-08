 /*
  * Copyright (c) 2011 Henrik Lynggaard Hansen 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Henrik Lynggaard Hansen
  */
 package dk.hlyh.hudson.tools.jobcreator.helper;
 
 import dk.hlyh.hudson.tools.jobcreator.ImportException;
 import dk.hlyh.hudson.tools.jobcreator.model.Environment;
 import dk.hlyh.hudson.tools.jobcreator.model.Job;
 import dk.hlyh.hudson.tools.jobcreator.model.Pipeline;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
public final class FreemarkerHelper {
 
   private static Configuration freeMarkerConfiguration;
 
   private FreemarkerHelper() {
     super();
   }
 
   public static void writeJob(File outputDirectory, Job job, Environment environment, TemplateValuesBuilder templateValues, Pipeline pipeline) throws ImportException {
     try {
       // merge with template
       String outputName = environment.getOutputPattern();
       outputName = outputName.replace("${environment}", environment.getName());
       outputName = outputName.replace("${pipeline}", pipeline.getName());
       outputName = outputName.replace("${job}", job.getName());
       File jobDirectory = new File(outputDirectory, outputName);
 
       if (!jobDirectory.exists()) {
         jobDirectory.mkdir();
       }
       File configXml = new File(jobDirectory, "config.xml");
 
       // merge with template
       Template template = freeMarkerConfiguration.getTemplate(job.getTemplate());
       Writer out = new OutputStreamWriter(new FileOutputStream(configXml));
       template.process(templateValues.getValues(), out);
       out.close();
     } catch (TemplateException ex) {
       throw new ImportException("Could not process job " + job.getName(), ex);
     } catch (IOException ex) {
       throw new ImportException("Could not process job " + job.getName(), ex);
     }
   }
 
   public static void setupFreemarker(File templateDirectory) throws ImportException {
     try {
       if (templateDirectory == null || !templateDirectory.exists()) {
         throw new ImportException("Template directory must exist '" + templateDirectory + "'");
       }
 
       freeMarkerConfiguration = new Configuration();
       freeMarkerConfiguration.setDirectoryForTemplateLoading(templateDirectory);
       freeMarkerConfiguration.setObjectWrapper(new DefaultObjectWrapper());
     } catch (IOException e) {
       throw new ImportException("Could not configure freemarker with template directory " + templateDirectory);
     }
   }
 }
