 /**
  * Copyright (c) 2005, 2010, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Ikayzo and others - initial API and implementation
  */
 package org.eclipse.uomo.units;
 
 import java.io.IOException;
 import java.text.ParsePosition;
 
 import org.eclipse.uomo.units.impl.format.LocalUnitFormatImpl;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitFormat;
 
 import com.ibm.icu.text.UFormat;
 import com.ibm.icu.util.ULocale;
 
 
 /**
  * <p> This class provides the interface for formatting and parsing {@link
  *     AbstractUnit units}.</p>
  *
  * <p> For all metric units, the 20 SI prefixes used to form decimal
  *     multiples and sub-multiples of SI units are recognized.
  *     {@link USCustomary US Customary} units are directly recognized. For example:[code]
  *        Unit.valueOf("m°C").equals(SI.MILLI(SI.CELSIUS))
  *        Unit.valueOf("kW").equals(SI.KILO(SI.WATT))
  *        Unit.valueOf("ft").equals(SI.METRE.multiply(3048).divide(10000))[/code]</p>
  *
  * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author  <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version 1.5.1 ($Revision: 215 $), $Date: 2010-09-19 22:12:08 +0200 (So, 19 Sep 2010) $
  * 
  */
 public abstract class AbstractFormat extends UFormat implements UnitFormat {
 
    /**
      * Returns the {@link SymbolMap} for this unit format.
      *
      * @return the symbol map used by this format.
      */
     protected abstract SymbolMap getSymbolMap();
 
     /**
      * Formats the specified unit.
      *
      * @param unit the unit to format.
      * @param appendable the appendable destination.
      * @return The appendable destination passed in as {@code appendable},
      *         with formatted text appended.
      * @throws IOException if an error occurs.
      */
     public abstract Appendable format(Unit<?> unit, Appendable appendable)
             throws IOException;
 
     /**
      * Parses a portion of the specified <code>CharSequence</code> from the
      * specified position to produce a unit. If there is no unit to parse
      * {@link AbstractUnit#ONE} is returned.
      *
      * @param csq the <code>CharSequence</code> to parse.
      * @param cursor the cursor holding the current parsing index.
      * @return the unit parsed from the specified character sub-sequence.
      * @throws IllegalArgumentException if any problem occurs while parsing the
      *         specified character sequence (e.g. illegal syntax).
      */
     public abstract Unit<?> parse(CharSequence csq, ParsePosition cursor)
             throws IllegalArgumentException;
 
     /**
      * Return a formatter for CurrencyAmount objects in the given
      * locale.
      * @param locale desired locale
      * @return a formatter object
      * @stable ICU 3.0
      */
     public static AbstractFormat getUnitFormat(ULocale locale) {
 //    	setLocale(locale,locale);
         return LocalUnitFormatImpl.getInstance(locale.toLocale());
     }
 
     /**
      * Return a formatter for CurrencyAmount objects in the default
      * locale.
      * @return a formatter object
      * @stable ICU 3.0
      */
     public static AbstractFormat getUnitFormat() {
         return getUnitFormat(ULocale.getDefault());
     }
     
     /**
      * Convenience method equivalent to {@link #format(AbstractUnit, Appendable)}
      * except it does not raise an IOException.
      *
      * @param unit the unit to format.
      * @param dest the appendable destination.
      * @return the specified <code>StringBuilder</code>.
      */
     final StringBuilder format(AbstractUnit<?> unit, StringBuilder dest) {
         try {
             return (StringBuilder) this.format(unit, (Appendable) dest);
         } catch (IOException ex) {
             throw new Error(ex); // Can never happen.
         }
     }
 
     /**
      * serialVersionUID
      */
     private static final long serialVersionUID = -2046025267890654321L;
 }
