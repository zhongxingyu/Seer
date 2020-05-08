 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.template;
 
 import java.io.IOException;
 import java.text.DateFormat;
 
 import org.melati.Melati;
 import org.melati.poem.Field;
 import org.melati.poem.Persistent;
 import org.melati.util.MelatiLocale;
 import org.melati.util.MelatiStringWriter;
 import org.melati.util.MelatiWriter;
 
 /**
  * MarkupLanguage provides a variety of methods for rendering objects in a
  * template.  
  *
  * Each object to be rendered has 3 methods:
  * 1 - String rendered(Object o) - this will render the object to a String
  * 2 - void render(Object o) - renders the object to melati.getWriter()
  * 3 - void render(Object o, MelatiWriter w) - render the object to w.
  *
  * When this class was written it was thought that for maximum 
  * efficiency one should render the object direct to the output stream using
  * method (2) above.  
  * However now all but (1) is deprecated. 
  */
 
 public abstract class AbstractMarkupLanguage implements MarkupLanguage {
 
   protected TempletLoader templetLoader = null;
   protected Melati melati = null;
   protected MelatiLocale locale = null;
 
 
   private String name;
 
   /**
    * Construct a Markup Language object.
    *
    * @param name - the name associated with this markup language.
    *    This is used to determine where to load
    *    templates from ie 'html' templates are
    *    found in the 'html' directory.
    * @param melati - the melati currently in use
    * @param templetLoader - the template loader in use
    *       (taken from org.melati.MelatiServlet.properties)
    * @param locale - the locale in use
    *    (taken from org.melati.MelatiServlet.properties)
    */
   public AbstractMarkupLanguage(String name,
                         Melati melati,
                         TempletLoader templetLoader,
                         MelatiLocale locale) {
     this.name = name;
     this.melati = melati;
     this.templetLoader = templetLoader;
     this.locale = locale;
   }
 
   /**
    * Construct a new MarkupLanguage given a new name and an
    * existing MarkupLanguage.
    *
    * @param name - the name of the new MarkupLanguage
    * @param other - the Markup Language to base this one upon
    */
   protected AbstractMarkupLanguage(String name, AbstractMarkupLanguage other) {
     this(name, other.melati, other.templetLoader, other.locale);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#getName()
    */
   public String getName() {
     return name;
   }
 
   private MelatiStringWriter getStringWriter() throws IOException {
     return (MelatiStringWriter)melati.getStringWriter();
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#rendered(java.lang.Object)
    */
   public String rendered(Object o)
       throws IOException {
     MelatiStringWriter sw = getStringWriter();
     if (o instanceof String)
       render((String)o, sw);
     else if (o instanceof Field) 
       render((Field)o, sw);
     else if (o instanceof Persistent) 
       render(((Persistent)o).displayString(locale, DateFormat.MEDIUM), sw);
     else
       render(o, sw);
     return sw.toString();
   }
 
   /**
    * Render a String in a MarkupLanguage specific way
    * (to the <code>Melati.getOutput()</code>).
    *
    * @param s - the string to be rendered
    * @throws IOException - if there is a problem during rendering
    */
   protected void render(String s) throws IOException {
     render(s,melati.getWriter());
   }
 
   /**
    * Render a String in a MarkupLanguage specific way
    * to a supplied MelatiWriter.
    *
    * @param s - the string to be rendered
    * @param writer - the MelatiWriter to render this String to
    * @throws IOException - if there is a problem during rendering
    */
   protected abstract void render(String s, MelatiWriter writer) throws IOException;
 
   /**
    * @see org.melati.template.MarkupLanguage#rendered(java.lang.String, int)
    */
   public String rendered(String s, int limit) throws IOException {
     MelatiStringWriter sw = getStringWriter();
     render(s,limit,sw);
     return sw.toString();
   }
 
   /**
    * Render a String in a MarkupLanguage specific way, limiting it's length.
    * Rendering to <code>melati.getWriter()</code>.
    *
    * @param s - the string to be rendered
    * @param limit - the lenght to trim the string to
    * @throws IOException - if there is a problem during rendering
    */
   protected void render(String s, int limit) throws IOException {
     render(s,limit,melati.getWriter());
   }
 
   /**
    * Render a String in a MarkupLanguage specific way, limiting it's length.
    * Render to a supplied MelatiWriter.
    *
    * @param s - the string to be rendered
    * @param writer - the MelatiWriter to render this String to
    * @param limit - the lenght to trim the string to
    * @throws IOException - if there is a problem during rendering
    */
   protected void render(String s, int limit, MelatiWriter writer)
       throws IOException {
     render(s.length() < limit + 3 ? s : s.substring(0, limit) + "...", writer);
   }
 
   /**
    * Render an Object in a MarkupLanguage specific way, rendering to
    * the <code>MelatiWriter</code> supplied by <code>melati.getWriter()</code>.
    *
    * @param o - the Object to be rendered
    * @throws IOException - if there is a problem during rendering
    * @throws TemplateEngineException - if there is a problem with the
    *                                   ServletTemplateEngine
    */
   protected void render(Object o) throws TemplateEngineException, IOException {
     MelatiWriter writer = melati.getWriter();
     if (o instanceof String)
       render((String)o, writer);
     else if (o instanceof Field) 
       render((Field)o, writer);
     else if (o instanceof Persistent) 
       render(((Persistent)o).displayString(locale, DateFormat.MEDIUM), writer);
     else
       render(o, writer);
   }
 
 
   /**
    * @see org.melati.template.MarkupLanguage#rendered(org.melati.poem.Field)
    */
   public String rendered(Field field)
       throws TemplateEngineException, IOException {
     MelatiStringWriter sw = getStringWriter();
     render(field, sw);
     return sw.toString();
   }
   protected void render(Field field) throws TemplateEngineException,
           IOException {
     render(field, melati.getWriter());
   }
 
   protected void render(Field field, MelatiWriter writer) throws IOException {
     renderMedium(field, writer);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#rendered(org.melati.poem.Field, int, int)
    */
   public String rendered(Field field, int style, int limit)
       throws TemplateEngineException, IOException {
     MelatiStringWriter sw = getStringWriter();
     render(field, style, limit, sw);
     return sw.toString();
   }
   /**
    * Render a Field Object in a MarkupLanguage specific way, 
    * rendering to the Melati Writer.
    *
    * @param field - the Field to be rendered
    * @param style - a style to format this Field
    * @see org.melati.poem.DatePoemType#stringOfCooked
    *              (java.lang.Object,org.melati.util.MelatiLocale, int)
    * @param limit - the lenght to trim the rendered string to
    * @throws IOException - if there is a problem during rendering
    * @throws TemplateEngineException - if there is a problem with the
    *                                   ServletTemplateEngine
    */
   protected void render(Field field, int style, int limit)
       throws TemplateEngineException, IOException {
     render(field, style, limit, melati.getWriter());
   }
 
   /**
    * Render a Field Object in a MarkupLanguage specific way, 
    * rendering to supplied MelatiWriter.
    *
    * @param field - the Field to be rendered
    * @param style - a style to format this Field.
    * @see org.melati.poem.DatePoemType#stringOfCooked
    *              (java.lang.Object,org.melati.util.MelatiLocale, int)
    * @param limit - the length to trim the rendered string to
    * @param writer - the MelatiWriter to render this Object to
    * @throws IOException - if there is a problem during rendering
    */
   protected void render(Field field, int style, int limit, MelatiWriter writer)
       throws IOException {
     render(field.getCookedString(locale, style), limit, writer);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#rendered(org.melati.poem.Field, int)
    */
   public String rendered(Field field, int style)
       throws TemplateEngineException, IOException {
     MelatiStringWriter sw = getStringWriter();
     render(field, style, sw);
     return sw.toString();
   }
 
 
   /**
    * Render a Field Object in a MarkupLanguage specific way, 
    * rendering to the current MelatiWriter from the Melati.
    * 
    * @param field - the Field to be rendered
    * @param style - a style to format this Field.
    * @throws TemplateEngineException - in other circumstances
    * @throws IOException - if there is a problem during rendering
    */
   protected void render(Field field, int style)
       throws TemplateEngineException, IOException {
     render(field, style, melati.getWriter());
   }
 
   /**
    * Render a Field Object in a MarkupLanguage specific way, 
    * rendering to the supplied MelatiWriter with a hidden limit 
    * of 10,000,000. 
    * 
    * @param field - the Field to be rendered
    * @param style - a style to format this Field.
    * @throws IOException - if there is a problem during rendering
    * @see org.melati.template.MarkupLanguage#render(org.melati.poem.Field, int, org.melati.util.MelatiWriter)
    */
   protected void render(Field field, int style, MelatiWriter writer)
       throws IOException {
     render(field, style, 10000000, writer);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#renderedShort(org.melati.poem.Field)
    */
   public String renderedShort(Field field)
       throws TemplateEngineException, IOException {
     MelatiStringWriter sw = getStringWriter();
     renderShort(field, sw);
     return sw.toString();
   }
 
   protected void renderShort(Field field)
       throws TemplateEngineException, IOException {
     renderShort(field, melati.getWriter());
   }
 
   protected void renderShort(Field field, MelatiWriter writer)
       throws TemplateEngineException, IOException {
     render(field, DateFormat.SHORT, writer);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#renderedMedium(org.melati.poem.Field)
    */
   public String renderedMedium(Field field)
       throws TemplateEngineException, IOException {
     MelatiStringWriter sw = getStringWriter();
     renderMedium(field, sw);
     return sw.toString();
   }
 
   protected void renderMedium(Field field)
       throws TemplateEngineException, IOException {
     renderMedium(field, melati.getWriter());
   }
 
   protected void renderMedium(Field field, MelatiWriter writer)
       throws IOException {
     render(field, DateFormat.MEDIUM, writer);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#renderedLong(org.melati.poem.Field)
    */
   public String renderedLong(Field field)
       throws IOException {
     MelatiStringWriter sw = getStringWriter();
     renderLong(field, sw);
     return sw.toString();
   }
 
   protected void renderLong(Field field)
       throws IOException {
     renderLong(field, melati.getWriter());
   }
 
   protected void renderLong(Field field, MelatiWriter writer)
       throws IOException {
     render(field, DateFormat.LONG, writer);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#renderedFull(org.melati.poem.Field)
    */
   public String renderedFull(Field field)
       throws TemplateEngineException, IOException {
     MelatiStringWriter sw = getStringWriter();
     renderFull(field, sw);
     return sw.toString();
   }
 
   protected void renderFull(Field field)
       throws TemplateEngineException, IOException {
     renderFull(field, melati.getWriter());
   }
 
   protected void renderFull(Field field, MelatiWriter writer)
       throws TemplateEngineException, IOException {
     render(field, DateFormat.FULL, writer);
   }
 
 
   /**
    * @see org.melati.template.MarkupLanguage#renderedStart(org.melati.poem.Field)
    */
   public String renderedStart(Field field)
       throws TemplateEngineException, IOException {
     MelatiStringWriter sw = getStringWriter();
     renderStart(field, sw);
     return sw.toString();
   }
 
   protected void renderStart(Field field)
       throws TemplateEngineException, IOException {
     renderStart(field, melati.getWriter());
   }
 
   protected void renderStart(Field field, MelatiWriter writer)
       throws TemplateEngineException, IOException {
     render(field, DateFormat.MEDIUM, 50, writer);
   }
 
 /*
   public String rendered(Throwable e) throws IOException {
     MelatiStringWriter sw = getStringWriter();
     render(e, sw);
     return sw.toString();
   }
   protected void render(Throwable e) throws IOException {
     render(e, melati.getWriter());
   }
 */
 
   /**
    * Render an Object in a MarkupLanguage specific way, rendering to
    * a supplied Writer.
    *
    * @param tree - the Object to be rendered
    * @param writer - the MelatiWriter to render this Object to
    * @throws IOException - if there is a problem during rendering
    * @throws TemplateEngineException - if there is a problem with the
    *                                   ServletTemplateEngine
    */
   protected void render(Object o, MelatiWriter writer)
       throws IOException {
     try {
       TemplateContext vars =
         melati.getTemplateEngine().getTemplateContext(melati);
       Template templet =
         templetLoader.templet(melati.getTemplateEngine(), this, o.getClass());
       vars.put("object", o);
       // Not happy but 
       if (o instanceof Field) vars.put("field", o);
       vars.put("melati", melati);
       vars.put("ml", melati.getMarkupLanguage());
       expandTemplet(templet, vars, writer);
     }
     catch (NotFoundException e) {
       // This will happen for Integer in 
       // an Attribute Markup Language for example
       render(o.toString(), writer);
     }
     catch (Exception f) {
      //System.err.println("MarkupLanguage failed to render an object:");
      //f.printStackTrace();
       render(o.toString(),writer);
     }
   }
 
 
   //
   // =========
   //  Widgets
   // =========
   //
   
   /**
    * @see org.melati.template.MarkupLanguage#input(org.melati.poem.Field)
    */
   public String input(Field field)
       throws TemplateEngineException,
              IOException {
     return input(field, null, "", false);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#inputAs(org.melati.poem.Field, java.lang.String)
    */
   public String inputAs(Field field, String templetName)
       throws TemplateEngineException,
              IOException {
     return input(field, templetName, "", false);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#searchInput(org.melati.poem.Field, java.lang.String)
    */
   public String searchInput(Field field, String nullValue)
       throws TemplateEngineException,
              IOException{
     return input(field, null, nullValue, true);
   }
 
   protected String input(Field field,
                          String templetName,
                          String nullValue,
                          boolean overrideNullable)
        throws TemplateEngineException,
               IOException {
 
     Template templet;
     try {
       templet =
         templetName == null ?
           templetLoader.templet(melati.getTemplateEngine(), this, field) :
           templetLoader.templet(melati.getTemplateEngine(), this, templetName);
     }
     catch (NotFoundException e) {
       throw new TemplateEngineException(e);
     }
 
     TemplateContext vars =
         melati.getTemplateEngine().getTemplateContext(melati);
 
     if (overrideNullable) {
       field = field.withNullable(true);
       vars.put("nullValue", nullValue);
     }
 
     vars.put("melati", melati);
     vars.put("ml", melati.getMarkupLanguage());
     vars.put("object", field);
     vars.put("field", field);
     return expandedTemplet(templet, vars);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#templet(java.lang.String)
    */
   public Template templet(String templetName) throws TemplateEngineException {
     return templetLoader.templet(melati.getTemplateEngine(), this,
                                  templetName);
   }
 
   /**
    * @see org.melati.template.MarkupLanguage#templet(java.lang.String, java.lang.Class)
    */
   public Template templet(Class clazz)
       throws TemplateEngineException {
     return templetLoader.templet(melati.getTemplateEngine(), this, 
                                  clazz);
   }
   /**
    * @see org.melati.template.MarkupLanguage#templet(java.lang.String, java.lang.Class)
    */
   public Template templet(String purpose, Class clazz)
       throws TemplateEngineException {
     return templetLoader.templet(melati.getTemplateEngine(), this, 
                                  purpose, clazz);
   }
 
   /**
    * Return the results of having interpolated a templet.
    * 
    * @param templet {@link Template} to interpolate
    * @param tc {@link ServletTemplateContext} against which to instantiate variables
    * @return the expanded template as a String
    * @throws TemplateEngineException if something unexpected happens
    * @throws IOException if templet cannot be found
    */
   protected String expandedTemplet(Template templet, TemplateContext tc)
       throws TemplateEngineException, IOException {
     return melati.getTemplateEngine().expandedTemplate(templet,tc);
   }
   
   /**
    * Interpolate a templet and write it out.
    * 
    * @param templet {@link Template} to interpolate
    * @param tc {@link TemplateContext} against which to instantiate variables
    * @param out {@link MelatiWriter} to write results to 
    * @throws TemplateEngineException if something unexpected happens
    * @throws IOException if templet cannot be found
    */
   protected void expandTemplet(Template templet, TemplateContext tc,
                                MelatiWriter out)
       throws TemplateEngineException, IOException {
     melati.getTemplateEngine().expandTemplate(out, templet, tc);
   }
 }
 
 
