 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.sudplan.linz.converter;
 
 import at.ac.ait.enviro.sudplan.util.PropertyNames;
 import at.ac.ait.enviro.tsapi.timeseries.TimeSeries;
 import at.ac.ait.enviro.tsapi.timeseries.TimeStamp;
 import at.ac.ait.enviro.tsapi.timeseries.impl.TimeSeriesImpl;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.NbBundle;
 import org.openide.util.lookup.ServiceProvider;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.math.RoundingMode;
 
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
import java.util.TimeZone;
 
 import de.cismet.cids.custom.sudplan.Unit;
 import de.cismet.cids.custom.sudplan.Variable;
 import de.cismet.cids.custom.sudplan.converter.ConversionException;
 import de.cismet.cids.custom.sudplan.converter.Converter;
 import de.cismet.cids.custom.sudplan.converter.FormatHint;
 import de.cismet.cids.custom.sudplan.converter.TimeseriesConverter;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 @ServiceProvider(service = Converter.class)
 public final class LinzTimeseriesConverter implements TimeseriesConverter, FormatHint {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(LinzTimeseriesConverter.class);
 
     private static final DateFormat DATEFORMAT;
     private static final NumberFormat NUMBERFORMAT;
 
     static {
         NUMBERFORMAT = NumberFormat.getInstance(Locale.US);
         NUMBERFORMAT.setMaximumFractionDigits(3);
         NUMBERFORMAT.setMinimumFractionDigits(0);
         NUMBERFORMAT.setRoundingMode(RoundingMode.HALF_UP);
 
         DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
         DATEFORMAT.setTimeZone(UTC_TIME_ZONE);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   from    DOCUMENT ME!
      * @param   params  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConversionException  DOCUMENT ME!
      */
     @Override
     public TimeSeries convertForward(final InputStream from, final String... params) throws ConversionException {
         final BufferedReader br;
         long count = 0;
         try {
             br = new BufferedReader(new InputStreamReader(from));
 
             String line = br.readLine();
 
             final TimeSeriesImpl ts = new TimeSeriesImpl();
             ts.setTSProperty(TimeSeries.VALUE_KEYS, new String[] { PropertyNames.VALUE });
             ts.setTSProperty(TimeSeries.VALUE_JAVA_CLASS_NAMES, new String[] { Float.class.getName() });
             ts.setTSProperty(TimeSeries.VALUE_TYPES, new String[] { TimeSeries.VALUE_TYPE_NUMBER });
             // FIXME: hardcoded unit and observed property
             ts.setTSProperty(TimeSeries.VALUE_UNITS, new String[] { Unit.MM.getPropertyKey() });
             ts.setTSProperty(
                 TimeSeries.VALUE_OBSERVED_PROPERTY_URNS,
                 new String[] { Variable.PRECIPITATION.getPropertyKey() });
             // must be present
             ts.setTSProperty(
                 PropertyNames.DESCRIPTION,
                 NbBundle.getMessage(
                     LinzTimeseriesConverter.class,
                     "LinzTimeseriesConverter.description")
                         + "("
                         + (System.currentTimeMillis() / 1000000000)
                         + ")");
 
             while (line != null) {
                final String[] split = line.split("   ");              // NOI18N
                 if (split.length == 1) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("token without value: " + split[0]); // NOI18N
                     }
                 } else {
                     if (split.length > 2) {
                         // usually, there should be only 2 splits, but there  might be more (e.g. for comments)
                         LOG.warn("illegal line format: " + line + " -> only first 2 splits are considered"); // NOI18N
                     }
 
                     final String key = split[0];
                     final String value = split[1];
 
                     DATEFORMAT.setTimeZone(UTC_TIME_ZONE);
                     final Date date = DATEFORMAT.parse(key);
                     final float val = NUMBERFORMAT.parse(value.trim()).floatValue();
                     ts.setValue(new TimeStamp(date), PropertyNames.VALUE, val);
                     count++;
                 }
 
                 if (Thread.currentThread().isInterrupted()) {
                     LOG.warn("execution was interrupted"); // NOI18N
                     return null;
                 }
 
                 line = br.readLine();
             }
 
             LOG.info(count + " measurements successfully imported into timeseries '"
                         + ts.getTSProperty(PropertyNames.DESCRIPTION) + '\'');
             return ts;
         } catch (final Exception ex) {
             final String message = "cannot convert from input stream"; // NOI18N
             LOG.error(message, ex);
             throw new ConversionException(message, ex);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   to      DOCUMENT ME!
      * @param   params  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConversionException  DOCUMENT ME!
      */
     @Override
     public InputStream convertBackward(final TimeSeries to, final String... params) throws ConversionException {
         try {
             final Object valueKeyObject = to.getTSProperty(TimeSeries.VALUE_KEYS);
             final String valueKey;
             long count = 0;
             if (valueKeyObject instanceof String) {
                 valueKey = (String)valueKeyObject;
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("found valuekey: " + valueKey);                   // NOI18N
                 }
             } else if (valueKeyObject instanceof String[]) {
                 final String[] valueKeys = (String[])valueKeyObject;
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("found multiple valuekeys: " + valueKeys.length); // NOI18N
                 }
 
                 if (valueKeys.length == 1) {
                     valueKey = valueKeys[0];
                 } else {
                     throw new IllegalStateException("found too many valuekeys");              // NOI18N
                 }
             } else {
                 throw new IllegalStateException("unknown value key type: " + valueKeyObject); // NOI18N
             }
 
             final StringBuilder sb = new StringBuilder();
             final String lineSep = System.getProperty("line.separator"); // NOI18N
 
             final Iterator<TimeStamp> it = to.getTimeStamps().iterator();
             while (it.hasNext()) {
                 final TimeStamp stamp = it.next();
                 final Float value = (Float)to.getValue(stamp, valueKey);
 
                 DATEFORMAT.setTimeZone(UTC_TIME_ZONE);
                 sb.append(DATEFORMAT.format(stamp.asDate()));
                 sb.append("   "); // NOI18N
                 sb.append(NUMBERFORMAT.format(value));
                 sb.append(lineSep);
                 count++;
             }
 
             LOG.info(count + " measurements successfully exported from timeseries '"
                         + to.getTSProperty(PropertyNames.DESCRIPTION) + '\'');
             return new ByteArrayInputStream(sb.toString().getBytes());
         } catch (final Exception e) {
             final String message = "cannot convert timeseries data"; // NOI18N
             LOG.error(message, e);
             throw new ConversionException(message, e);
         }
     }
 
     @Override
     public String toString() {
         return getFormatDisplayName();
     }
 
     @Override
     public String getFormatName() {
         return "linz-timeseries-converter"; // NOI18N
     }
 
     @Override
     public String getFormatDisplayName() {
         return NbBundle.getMessage(
                 LinzTimeseriesConverter.class,
                 "LinzTimeseriesConverter.this.name"); // NOI18N
     }
 
     @Override
     public String getFormatHtmlName() {
         return null;
     }
 
     @Override
     public String getFormatDescription() {
         return NbBundle.getMessage(
                 LinzTimeseriesConverter.class,
                 "LinzTimeseriesConverter.getFormatDescription().description"); // NOI18N
     }
 
     @Override
     public String getFormatHtmlDescription() {
         return NbBundle.getMessage(
                 LinzTimeseriesConverter.class,
                 "LinzTimeseriesConverter.getFormatHtmlDescription().description"); // NOI18N
     }
 
     @Override
     public Object getFormatExample() {
         return NbBundle.getMessage(
                 LinzTimeseriesConverter.class,
                 "LinzTimeseriesConverter.getFormatExample().description"); // NOI18N
     }
 }
