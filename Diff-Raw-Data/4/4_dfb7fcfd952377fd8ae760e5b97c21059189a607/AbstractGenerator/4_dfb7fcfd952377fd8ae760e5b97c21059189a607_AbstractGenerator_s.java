 /*
  * Copyright (C) 2009 - 2012 SMVP4G.COM
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *  
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package com.smvp4g.generator.generator;
 
 import com.google.gwt.core.ext.Generator;
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.UnableToCompleteException;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import freemarker.cache.URLTemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Map;
 
 /**
  * The Class AbstractGenerator.
  *
  * @author Nguyen Duc Dung
  * @since 11/24/11, 12:33 PM
  */
 public abstract class AbstractGenerator<M extends AbstractTemplateData> extends Generator {
 
     public static final String DEFAULT_FILE_PREFIX = "Generated";
 
     protected GeneratorContext context;
     protected JClassType classType;
 
     @Override
     public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
         try {
             this.context = context;
             classType = context.getTypeOracle().getType(typeName);
             PrintWriter sourceWriter = context.tryCreate(logger, getPackageName(),getClassName());
             if (sourceWriter != null) {
                 StringWriter templateWriter = new StringWriter();
                 createTemplate().process(scan(), templateWriter);
                 sourceWriter.print(templateWriter.toString());
                 context.commit(logger, sourceWriter);
             }
            return getPackageName() + "." + getClassName();
         } catch (Exception e) {
             logger.log(TreeLogger.Type.ERROR, e.getMessage());
         }
        return null;
     }
 
     protected Template createTemplate() {
         Configuration configuration = new Configuration();
         configuration.setTemplateLoader(new URLTemplateLoader() {
             @Override
             protected URL getURL(String name) {
                 return getResourceClass().getResource(getTemplateFileName());
             }
         });
         try {
             return configuration.getTemplate(getTemplateFileName());
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 
 
     protected String getPackageName() {
         if (classType != null) {
             return classType.getPackage().getName();
         }
         return null;
     }
     
 
     protected String getClassName() {
         if (classType != null) {
             return classType.getSimpleSourceName() + getFilePrefix();
         }
         return null;
     }
     
     protected String getFilePrefix() {
         return DEFAULT_FILE_PREFIX;
     }
     
     protected abstract Map<String, M> scan();
 
     protected abstract String getTemplateFileName();
 
     protected abstract Class<?> getResourceClass();   
 }
