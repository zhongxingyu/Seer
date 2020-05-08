 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.value.renderer;
 
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.value.*;
 
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
import org.apache.commons.lang.StringUtils;

 /**
  * Factory for FxValueRenderer. A FxValueRenderer provides a transparent way of formatting
  * any FxValue object in a given language.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FxValueRendererFactory {
 
     private static final ConcurrentMap<FxLanguage, FxValueRendererImpl> renderers = new ConcurrentHashMap<FxLanguage, FxValueRendererImpl>();
     /** Internal fallback default language for locale-agnostic formatters */
     static final FxLanguage DEFAULT = new FxLanguage(-1, FxLanguage.DEFAULT.getIso2digit(),
             new FxString("Default fallback formatter"), true);
 
     /**
      * FxDate formatter.
      */
     private static class FxDateFormatter implements FxValueFormatter<Date, FxDate> {
         public String format(FxDate value, Date translation, FxLanguage outputLanguage) {
             return DateFormat.getDateInstance(DateFormat.MEDIUM, outputLanguage.getLocale()).format(translation);
         }
     }
 
     /**
      * FxDateTime formatter.
      */
     private static class FxDateTimeFormatter implements FxValueFormatter<Date, FxDateTime> {
         public String format(FxDateTime container, Date value, FxLanguage outputLanguage) {
             return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, outputLanguage.getLocale()).format(value);
         }
     }
 
     /**
      * FxDouble formatter.
      */
     private static class FxDoubleFormatter implements FxValueFormatter<Double, FxDouble> {
         public String format(FxDouble value, Double translation, FxLanguage outputLanguage) {
             return NumberFormat.getNumberInstance(outputLanguage.getLocale()).format(translation);
         }
     }
 
     /**
      * FxFloat formatter.
      */
     private static class FxFloatFormatter implements FxValueFormatter<Float, FxFloat> {
         public String format(FxFloat value, Float translation, FxLanguage outputLanguage) {
             return NumberFormat.getNumberInstance(outputLanguage.getLocale()).format(translation);
         }
     }
 
     /**
      * FxSelectOne formatter
      */
     private static class FxSelectOneFormatter implements FxValueFormatter<FxSelectListItem, FxSelectOne> {
         public String format(FxSelectOne value, FxSelectListItem translation, FxLanguage outputLanguage) {
             return translation.getLabel().getBestTranslation(outputLanguage);
         }
     }
 
     /**
      * FxSelectMany formatter
      */
     private static class FxSelectManyFormatter implements FxValueFormatter<SelectMany, FxSelectMany> {
         public String format(FxSelectMany value, SelectMany translation, FxLanguage outputLanguage) {
            final List<String> out = new ArrayList<String>(translation.getSelected().size());
             for (FxSelectListItem item: translation.getSelected()) {
                out.add(item.getLabel().getBestTranslation(outputLanguage));
             }
            Collections.sort(out);  // sort by label
            return StringUtils.join(out.iterator(), ", ");
         }
     }
 
     /**
      * Generic object formatter for all types that are not explicitly covered.
      */
     private static class ObjectFormatter implements FxValueFormatter {
         public String format(FxValue value, Object translation, FxLanguage outputLanguage) {
             return translation != null ? translation.toString() : "null";
         }
     }
 
     static {
         addRenderer(DEFAULT, FxDate.class, new FxDateFormatter());
         addRenderer(DEFAULT, FxDateTime.class, new FxDateTimeFormatter());
         addRenderer(DEFAULT, FxDouble.class, new FxDoubleFormatter());
         addRenderer(DEFAULT, FxFloat.class, new FxFloatFormatter());
         addRenderer(DEFAULT, FxSelectOne.class, new FxSelectOneFormatter());
         addRenderer(DEFAULT, FxSelectMany.class, new FxSelectManyFormatter());
         //noinspection unchecked
         addRenderer(DEFAULT, FxValue.class, new ObjectFormatter());
     }
 
     /**
      * Return a <code>FxValueRenderer</code> instance for the user's current language.
      *
      * @return a <code>FxValueRenderer</code> instance for the user's current language.
      */
     public static FxValueRenderer getInstance() {
         final UserTicket ticket = FxContext.get().getTicket();
         return getInstance(ticket != null ? ticket.getLanguage() : null);
     }
 
     /**
      * Returns a <code>FxValueRenderer</code> instance for the given language.
      * If <code>language</code> is null, the default renderer is returned.
      *
      * @param language  the target language. Both the output formatting and the value to be
      * rendered may depend on the renderer's language.
      * @return  a <code>FxValueRenderer</code> instance for the given language.
      */
     public static FxValueRenderer getInstance(FxLanguage language) {
         if (language == null) {
             // default renderer always exists
             return renderers.get(DEFAULT);
         }
         if (!renderers.containsKey(language)) {
             renderers.putIfAbsent(language, new FxValueRendererImpl(language));
         }
         return renderers.get(language);
     }
 
     /**
      * Return the default FxValue formatter for the given FxValue subclass.
      *
      * @param valueType class of the value to be formatted
      * @return  the default FxValue formatter for the given FxValue subclass.
      */
     public static FxValueFormatter getDefaultFormatter(Class valueType) {
         // this works because the addRenderer methods are bounded
         //noinspection unchecked
         return renderers.get(DEFAULT).get(valueType);
     }
 
     private static <DT, T extends FxValue<DT, T>>
     void addRenderer(FxLanguage language, Class<T> valueType, FxValueFormatter<DT, T> formatter) {
         renderers.putIfAbsent(language, new FxValueRendererImpl(language));
         renderers.get(language).put(valueType, formatter);
     }
 }
