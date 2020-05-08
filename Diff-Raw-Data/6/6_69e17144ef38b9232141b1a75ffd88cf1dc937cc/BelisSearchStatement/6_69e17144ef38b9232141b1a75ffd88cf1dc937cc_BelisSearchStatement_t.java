 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.belis.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 import Sirius.server.middleware.types.MetaObjectNode;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Polygon;
 
 import org.apache.log4j.Logger;
 
 import java.rmi.RemoteException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import de.cismet.cids.server.search.AbstractCidsServerSearch;
 import de.cismet.cids.server.search.MetaObjectNodeServerSearch;
 
 import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mroncoroni
  * @version  $Revision$, $Date$
  */
 public class BelisSearchStatement extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(BelisSearchStatement.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final boolean standort;
     private final boolean schaltstelle;
     private final boolean mauerlasche;
     private final boolean leitung;
     private final boolean abzweigdose;
     private final boolean leuchte;
     private Geometry geometry;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new BelisSearchStatement object.
      */
     public BelisSearchStatement() {
         this(true, false, true, true, true, true);
     }
 
     /**
      * Creates a new BelisSearchStatement object.
      *
      * @param  standort      DOCUMENT ME!
      * @param  leuchte       DOCUMENT ME!
      * @param  schaltstelle  DOCUMENT ME!
      * @param  mauerlasche   DOCUMENT ME!
      * @param  leitung       DOCUMENT ME!
      * @param  abzweigdose   DOCUMENT ME!
      */
     public BelisSearchStatement(
             final boolean standort,
             final boolean leuchte,
             final boolean schaltstelle,
             final boolean mauerlasche,
             final boolean leitung,
             final boolean abzweigdose) {
         this.standort = standort;
         this.leuchte = leuchte;
         this.schaltstelle = schaltstelle;
         this.mauerlasche = mauerlasche;
         this.leitung = leitung;
         this.abzweigdose = abzweigdose;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Geometry getGeometry() {
         return geometry;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  geometry  DOCUMENT ME!
      */
     public void setGeometry(final Geometry geometry) {
         this.geometry = geometry;
     }
 
     @Override
     public Collection<MetaObjectNode> performServerSearch() {
         try {
             if (!standort && !leuchte && !schaltstelle && !mauerlasche && !leitung && !abzweigdose) {
                 return new ArrayList<MetaObjectNode>();
             }
 
             final ArrayList<String> union = new ArrayList<String>();
             final ArrayList<String> join = new ArrayList<String>();
             if (standort) {
                 union.add(
                     "SELECT 29 AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Standort'::text AS searchIntoClass FROM tdta_standort_mast");
                 // join.add("");
             }
             if (leuchte) {
                 union.add(
                     "SELECT 29 AS classid, tdta_standort_mast.id AS objectid, tdta_leuchten.id AS searchIntoId, tdta_standort_mast.fk_geom AS fk_geom, 'Leuchte'::text AS searchIntoClass FROM tdta_leuchten LEFT JOIN tdta_standort_mast ON tdta_leuchten.fk_standort = tdta_standort_mast.id");
                 // join.add("");
             }
             if (schaltstelle) {
                 union.add(
                     "SELECT 15 AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Schaltstelle'::text AS searchIntoClass FROM schaltstelle");
             }
             if (mauerlasche) {
                 union.add(
                     "SELECT 14 AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Mauerlasche'::text AS searchIntoClass FROM mauerlasche");
             }
             if (leitung) {
                 union.add(
                     "SELECT 11 AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Leitung'::text AS searchIntoClass FROM leitung");
             }
             if (abzweigdose) {
                 union.add(
                     "SELECT 5 AS classid, id AS objectid, fk_geom, 'Abzweigdose'::text AS searchIntoClass FROM abzweigdose");
             }
             final String implodedUnion = implodeArray(union.toArray(new String[0]), " UNION ");
             final String implodedJoin = implodeArray(join.toArray(new String[0]), " LEFT JOIN ");
 
             String query = "SELECT DISTINCT classid, objectid"
                         + " FROM (" + implodedUnion + ") AS geom_objects"
                         + " LEFT JOIN tdta_standort_mast ON geom_objects.searchIntoClass = 'Standort' AND tdta_standort_mast.id = geom_objects.searchIntoId"
                         + " LEFT JOIN tdta_leuchten ON geom_objects.searchIntoClass = 'Leuchte' AND tdta_leuchten.id = geom_objects.searchIntoId"
                         + " LEFT JOIN schaltstelle ON geom_objects.searchIntoClass = 'Schaltstelle' AND schaltstelle.id = geom_objects.searchIntoId"
                         + " LEFT JOIN mauerlasche ON geom_objects.searchIntoClass = 'Mauerlasche' AND mauerlasche.id = geom_objects.searchIntoId"
                         + " LEFT JOIN leitung ON geom_objects.searchIntoClass = 'Leitung' AND leitung.id = geom_objects.searchIntoId"
                         + " LEFT JOIN abzweigdose ON geom_objects.searchIntoClass = 'Abzweigdose' AND abzweigdose.id = geom_objects.searchIntoId"
                         + ", geom"
                         + " WHERE geom.id = geom_objects.fk_geom"
                         + " AND"
                         + " (tdta_standort_mast.id IS NOT null"
                         + " OR tdta_leuchten.id IS NOT null"
                         + " OR schaltstelle.id IS NOT null"
                         + " OR mauerlasche.id IS NOT null"
                         + " OR leitung.id IS NOT null"
                         + " OR abzweigdose.id IS NOT null"
                         + " )";
 
             if (geometry != null) {
                 final String geostring = PostGisGeometryFactory.getPostGisCompliantDbString(geometry);
                 if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
                     query += " AND geo_field &&\n"
                                 + "st_buffer(\n"
                                 + "GeometryFromText('" + geostring + "')\n"
                                 + ", 0.000001)\n"
                                 + "and intersects(geo_field,st_buffer(GeometryFromText('" + geostring
                                 + "'), 0.000001))";
                 } else {
                     query += " AND geo_field &&\n"
                                 + "st_buffer(\n"
                                 + "GeometryFromText('" + geostring + "')\n"
                                 + ", 0.000001)\n"
                                 + "and intersects(geo_field, GeometryFromText('" + geostring + "'))";
                 }
             }
 
             final String andQueryPart = getAndQueryPart();
             if ((andQueryPart != null) && !andQueryPart.trim().isEmpty()) {
                 query += " AND " + andQueryPart;
             }
 
             final List<MetaObjectNode> result = new ArrayList<MetaObjectNode>();
             final MetaService ms = (MetaService)getActiveLocalServers().get("BELIS");
            final ArrayList<ArrayList> searchResult = ms.performCustomSearch(query);
            LOG.fatal(query);
             for (final ArrayList al : searchResult) {
                 final int cid = (Integer)al.get(0);
                 final int oid = (Integer)al.get(1);
                final MetaObjectNode mon = new MetaObjectNode("BELIS", oid, cid, null);
                 result.add(mon);
             }
 
             return result;
         } catch (RemoteException ex) {
             LOG.error("Problem", ex);
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected String getAndQueryPart() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   field  DOCUMENT ME!
      * @param   id     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateIdQuery(final String field, final Integer id) {
         final String query;
         if (id != null) {
             query = field + " = " + id + "";
         } else {
             query = "TRUE";
         }
         return query;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   field  DOCUMENT ME!
      * @param   like   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateLikeQuery(final String field, final String like) {
         final String query;
         if (like != null) {
             query = field + " like '%" + like + "%'";
         } else {
             query = "TRUE";
         }
         return query;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   field  DOCUMENT ME!
      * @param   von    DOCUMENT ME!
      * @param   bis    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateVonBisQuery(final String field, final String von, final String bis) {
         final String query;
         if (von != null) {
             if (bis != null) {
                 query = field + " BETWEEN '" + von + "' AND '" + bis + "'";
             } else {
                 query = field + " >= '" + von + "'";
             }
         } else if (bis != null) {
             query = field + " <= '" + bis + "'";
         } else {
             query = "TRUE";
         }
         return query;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   inputArray  DOCUMENT ME!
      * @param   glueString  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String implodeArray(final String[] inputArray, final String glueString) {
         String output = "";
         if (inputArray.length > 0) {
             final StringBuilder sb = new StringBuilder();
             sb.append(inputArray[0]);
             for (int i = 1; i < inputArray.length; i++) {
                 sb.append(glueString);
                 sb.append(inputArray[i]);
             }
             output = sb.toString();
         }
         return output;
     }
 }
