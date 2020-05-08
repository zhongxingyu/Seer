 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */

 package de.weltraumschaf.juberblog.template;
 
 import com.google.common.collect.Maps;
 import de.weltraumschaf.juberblog.Constants;
 import de.weltraumschaf.juberblog.filter.Filter;
 import de.weltraumschaf.juberblog.filter.FilterChain;
 import freemarker.template.Configuration;
 import freemarker.template.TemplateException;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Map;
 import org.apache.commons.lang3.Validate;
 import org.apache.log4j.Logger;
 
 /**
  * Template class which encapsulates Freemarker.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class Template implements Renderable {
 
     /**
      * Logger facility.
      */
     private static final Logger LOG = Logger.getLogger(Template.class);
     /**
      * Configuration used to get template files.
      */
     private final Configuration templateConfiguration;
     /**
      * Template file name relative to the template directory in {@link #templateConfiguration}.
      */
     private final String templateFile;
     /**
      * Holds the assigned variables.
      */
     private final Map<String, Object> templateVariables = Maps.newHashMap();
     /**
      * Encoding of rendered template string.
      */
     private final String encoding;
     /**
      * Applied on rendered string.
      */
     private final FilterChain postfilters = new FilterChain();
 
     /**
      * Initializes {@link #encoding} with {@link Constants#DEFAULT_ENCODING}.
      *
      *
      * @param templateConfiguration must not be {@code null}
      * @param templateFile must not be {@code null} or empty
      */
     public Template(final Configuration templateConfiguration, final String templateFile) {
         this(templateConfiguration, templateFile, Constants.DEFAULT_ENCODING.toString());
     }
 
     /**
      * Dedicated constructor.
      *
      * @param templateConfiguration must not be {@code null}
      * @param templateFile must not be {@code null} or empty
      * @param encoding must not be {@code null} or empty
      */
     public Template(final Configuration templateConfiguration, final String templateFile, final String encoding) {
         super();
         Validate.notNull(templateConfiguration, "Template configuration must not be null!");
         Validate.notEmpty(templateFile, "Template file must not be null or empty!");
         Validate.notEmpty(encoding, "Encoding must not be null or empty!");
         this.templateConfiguration = templateConfiguration;
         this.templateFile = templateFile;
         this.encoding = encoding;
     }
 
     /**
      * Assign any object as template variable.
      *
      * @param name must not be {@code null}
      * @param value must not be {@code null}
      */
     public void assignVariable(final String name, final Object value) {
         Validate.notEmpty(name, "Name must not be null or empty!");
         Validate.notNull(value, "Value must not be null!");
         templateVariables.put(name, value);
     }
 
     /**
      * Get a variable.
      *
      * @param name must not be {@code null}
      * @return never {@code null}, maybe empty string
      */
     protected Object getVariable(final String name) {
         Validate.notEmpty(name, "Name must not be null or empty!");
 
         if (templateVariables.containsKey(name)) {
             return templateVariables.get(name);
         }
 
         LOG.warn(String.format("Wanted template variable '%s' not present! Returning empty string.", name));
         return "";
     }
 
     @Override
     public String render() throws IOException, TemplateException {
         final freemarker.template.Template tpl = templateConfiguration.getTemplate(templateFile);
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         tpl.process(templateVariables, new OutputStreamWriter(out, encoding));
         return postfilters.apply(out.toString(Constants.DEFAULT_ENCODING.toString()));
     }
 
     /**
      * Add a filter to process template after rendering.
      *
      * Filters are applied in order they were added.
      *
      * @param filter must not be {@code null}
      */
     public void addPostFilter(final Filter filter) {
       postfilters.add(filter);
     }

 }
