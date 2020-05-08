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
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.lang3.Validate;
 import org.apache.log4j.Logger;
 
 /**
  * Template class which encapsulates Freemarker.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class Template implements Renderable, Assignable {
 
     /**
      * Logger facility.
      */
     private static final Logger LOG = Logger.getLogger(Template.class);
     /**
      * Rendered template.
      */
     private final freemarker.template.Template template;
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
     * @param templateConfiguration must not be {@code null}, file name relative to the template directory in
     *                              {@link #templateConfiguration}
      * @param templateFile must not be {@code null} or empty
      * @throws IOException if template can't be opened
      */
     public Template(final Configuration templateConfiguration, final String templateFile) throws IOException {
         this(templateConfiguration, templateFile, Constants.DEFAULT_ENCODING.toString());
     }
 
     /**
      * Creates a template from an template string.
      *
      * @param template must not be {@code null}
      * @param encoding must not be {@code null}
      * @throws IOException if template can't be opened
      */
     public Template(final String template, final String encoding) throws IOException {
         this(Freemarker.createTemplate(template), encoding);
     }
 
     /**
      * Dedicated constructor.
      *
      * @param templateConfiguration must not be {@code null}
     * @param templateFile must not be {@code null}, file name relative to the template directory in
     *                     {@link #templateConfiguration}
      * @param encoding must not be {@code null} or empty
      * @throws IOException if template can't be opened
      */
     public Template(final Configuration templateConfiguration, final String templateFile, final String encoding)
         throws IOException {
         this(templateConfiguration.getTemplate(templateFile), encoding);
     }
 
     /**
      * Dedicated constructor.
      *
      * @param template must not be {@code null}
      * @param encoding must not be {@code null} or empty
      * @throws IOException if template can't be opened
      */
     public Template(final freemarker.template.Template template, final String encoding)
         throws IOException {
         super();
         Validate.notNull(template, "Template file must not be null or empty!");
         Validate.notEmpty(encoding, "Encoding must not be null or empty!");
         this.template = template;
         this.encoding = encoding;
     }
 
     /**
      * Assigns for a list of names an empty string.
      *
      * @param assignable must not be {@code null}
      * @param names must not be {@code null}
      */
     public static void initializeVaribales(final Assignable assignable, final List<String> names) {
         Validate.notNull(assignable, "Assignable must not be null!");
         Validate.notNull(names, "Names must not be null!");
 
         for (final String name : names) {
             assignable.assignVariable(name, "");
         }
     }
 
     /**
      * Assign any object as template variable.
      *
      * @param name must not be {@code null}
      * @param value must not be {@code null}
      */
     @Override
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
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         template.process(templateVariables, new OutputStreamWriter(out, encoding));
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
