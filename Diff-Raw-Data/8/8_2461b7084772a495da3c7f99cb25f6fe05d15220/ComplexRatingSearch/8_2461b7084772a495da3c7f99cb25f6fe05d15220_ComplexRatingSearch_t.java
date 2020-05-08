 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.wrrl_db_mv.fgsk.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 
 import org.apache.log4j.Logger;
 
 import java.io.Serializable;
 
 import java.math.BigDecimal;
 
 import java.text.MessageFormat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public abstract class ComplexRatingSearch extends AbstractCalcCacheSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ComplexRatingSearch.class);
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ComplexRatingSearch object.
      *
      * @param  tableName  DOCUMENT ME!
      */
     public ComplexRatingSearch(final String tableName) {
         super(tableName);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected Map internalPerformSearch(final MetaService ms) throws SearchException {
         final Map<Integer, Map<Range, Number>> result = new HashMap<Integer, Map<Range, Number>>();
 
         final Map<String, Range> rangeCache = new HashMap<String, Range>();
 
         String query = null;
         try {
             final String subject = getSubject();
             query = MessageFormat.format(getQuery(), subject, tableName);
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("created query: " + query); // NOI18N
             }
 
             final ArrayList<ArrayList> rows = ms.performCustomSearch(query);
             for (final ArrayList row : rows) {
                 final int key = ((BigDecimal)row.get(0)).intValueExact();
                 final double rFrom = ((BigDecimal)row.get(1)).doubleValue();
                 final double rTo = ((BigDecimal)row.get(2)).doubleValue();
                 final Number rating;
                if (row.get(3) instanceof Integer) {
                     rating = (Integer)row.get(3);
                } else if (row.get(3) instanceof BigDecimal) {
                     rating = ((BigDecimal)row.get(3)).doubleValue();
                 } else {
                     throw new IllegalStateException("unsupported column type: " + row.get(3).getClass());
                 }
 
                 Map<Range, Number> rMap;
                 if (result.containsKey(key)) {
                     rMap = result.get(key);
                 } else {
                     rMap = new HashMap<Range, Number>();
                     result.put(key, rMap);
                 }
 
                 final Range range;
                 final String rangeKey = rFrom + "-" + rTo; // NOI18N
 
                 if (rangeCache.containsKey(rangeKey)) {
                     range = rangeCache.get(rangeKey);
                 } else {
                     range = new Range(rFrom, rTo);
                     rangeCache.put(rangeKey, range);
                 }

                 rMap.put(range, rating);
             }
         } catch (final Exception ex) {
             final String message = "cannot perform complex rating search"; // NOI18N
             LOG.error(message, ex);
             throw new SearchException(message, query, ex);
         }
 
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected abstract String getQuery();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected abstract String getSubject();
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public static final class ProfileDepthBreadthRelationRatingSearch extends ComplexRatingSearch {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final String QUERY = "SELECT id_gewaessertyp, {0}_von, {0}_bis, bewertung FROM {1}"; // NOI18N
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ProfileDepthBreadthRelationRatingSearch object.
          */
         public ProfileDepthBreadthRelationRatingSearch() {
             super("public.fgsk_verhaeltnis_profiltiefe_wspbreite_auswertung"); // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected String getQuery() {
             return QUERY;
         }
 
         @Override
         protected String getSubject() {
             return "verhaeltnis"; // NOI18N
         }
     }
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public static class DefaultComplexRatingSearch extends ComplexRatingSearch {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final String QUERY = "SELECT id_gewaessertyp, anz_{0}_von, anz_{0}_bis, bewertung FROM {1}"; // NOI18N
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DefaultComplexRatingSearch object.
          *
          * @param  tableName  DOCUMENT ME!
          */
         public DefaultComplexRatingSearch(final String tableName) {
             super(tableName);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected String getQuery() {
             return QUERY;
         }
 
         @Override
         protected String getSubject() {
             return stripSubject(tableName);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public static final class Range implements Serializable {
 
         //~ Instance fields ----------------------------------------------------
 
         private final double from;
         private final double to;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new Range object.
          *
          * @param  from  DOCUMENT ME!
          * @param  to    DOCUMENT ME!
          */
         public Range(final double from, final double to) {
             this.from = from;
             this.to = to;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param   d  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public boolean withinRange(final double d) {
             return (d >= from) && (d <= to);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public double getFrom() {
             return from;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public double getTo() {
             return to;
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (this == obj) {
                 return true;
             }
 
             if (obj instanceof Range) {
                 final Range that = (Range)obj;
 
                 return (from == that.from) && (to == that.to);
             }
 
             return false;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = (61 * hash)
                         + (int)(Double.doubleToLongBits(this.from) ^ (Double.doubleToLongBits(this.from) >>> 32));
             hash = (61 * hash) + (int)(Double.doubleToLongBits(this.to) ^ (Double.doubleToLongBits(this.to) >>> 32));
 
             return hash;
         }
     }
 }
