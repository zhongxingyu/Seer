 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcgen4j.core.velocity;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
 import org.fuin.srcgen4j.commons.GenerateException;
import org.fuin.srcgen4j.commons.GeneratorConfig;
 import org.fuin.srcgen4j.core.base.AbstractGenerator;
 import org.fuin.srcgen4j.core.base.GeneratedFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base generator that uses velocity templates for generation.
  * 
  * @param <MODEL>
  *            Type of the model.
  */
 public abstract class VelocityGenerator<MODEL> extends
         AbstractGenerator<MODEL, VelocityGeneratorConfig> {
 
     /** Key for the location of the template files. */
     public static final String TEMPLATE_DIR_KEY = "templateDir";
 
     private static final Logger LOG = LoggerFactory.getLogger(VelocityGenerator.class);
 
     private VelocityEngine ve;
 
     private File templateDir;
 
     /**
      * Returns an initialized velocity engine.
      * 
      * @return Engine - Never NULL after
     *         {@link #generate(GeneratorConfig, MODEL)} was called.
      */
     protected final VelocityEngine getVE() {
         return ve;
     }
 
     /**
      * Returns the template directory.
      * 
      * @return Source directory.
      */
     public final File getTemplateDir() {
         return templateDir;
     }
 
     private VelocityEngine createVelocityEngine(final File templateDir) {
         final VelocityEngine ve = new VelocityEngine();
         if (templateDir == null) {
             ve.addProperty("resource.loader", "class");
         } else {
             ve.addProperty("resource.loader", "file, class");
             ve.addProperty("file.resource.loader.class", FileResourceLoader.class.getName());
             ve.addProperty("file.resource.loader.path", templateDir.toString());
         }
         ve.addProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
         ve.init();
         return ve;
     }
 
     /**
      * Merges the template and context into a file. If the directory of the file
      * does not exists, the full directory path to it will be created.
      * 
      * @param context
      *            Context to use.
      * @param artifactName
      *            Unique name of the generated artifact.
      * @param templateName
      *            Name of the template to use.
      * @param filename
      *            Filename relative to the target directory.
      * 
      * @throws GenerateException
      *             Error merging the template
      */
     protected final void merge(final VelocityContext context, final String artifactName,
             final String templateName, final String filename) throws GenerateException {
 
         final GeneratedFile genFile = getTargetFile(artifactName, filename, templateName);
         if (genFile.isSkip()) {
             LOG.debug("Omitted already existing file: " + genFile + " [" + templateName + "]");
         } else {
             LOG.debug("Start merging velocity template: " + templateName);
             // Merge content
             try {
                 final Writer writer = new FileWriter(genFile.getFile());
                 try {
                     final Template template = ve.getTemplate(templateName);
                     template.merge(context, writer);
                 } finally {
                     writer.close();
                 }
                 genFile.persist();
 
             } catch (final IOException ex) {
                 throw new GenerateException("Error merging template '" + templateName + "' to '"
                         + filename + "'!", ex);
             }
         }
 
     }
 
     @Override
     public final Class<VelocityGeneratorConfig> getSpecificConfigClass() {
         return VelocityGeneratorConfig.class;
     }
 
     @Override
     public final void generate() throws GenerateException {
         this.templateDir = getSpecificConfig().getTemplateDir();
         this.ve = createVelocityEngine(templateDir);
         generateIntern();
     }
 
     /**
      * Generates the files from velocity templates. The method {@link #getVE()}
      * van be used to get a ready to use velocity engine that points to the
      * template directory. The generation context is also available by calling
      * {@link #getCtx()}.
      * 
      * @throws GenerateException
      *             Error generating the files.
      */
     protected abstract void generateIntern() throws GenerateException;
 
 }
