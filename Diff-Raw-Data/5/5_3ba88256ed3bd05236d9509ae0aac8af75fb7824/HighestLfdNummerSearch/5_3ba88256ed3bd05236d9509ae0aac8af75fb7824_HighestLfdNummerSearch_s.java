 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.belis.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import de.cismet.cids.server.search.AbstractCidsServerSearch;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class HighestLfdNummerSearch extends AbstractCidsServerSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(HighestLfdNummerSearch.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final String strassenschluessel;
     private final Integer kennziffer;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new HighestLfdNummerSearch object.
      *
      * @param  strassenschluessel  DOCUMENT ME!
      * @param  kennziffer          DOCUMENT ME!
      */
     public HighestLfdNummerSearch(final String strassenschluessel, final Integer kennziffer) {
         this.strassenschluessel = strassenschluessel;
         this.kennziffer = kennziffer;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Collection performServerSearch() {
         final List<Integer> numbers = new ArrayList<Integer>();
 
         final String query = "SELECT MAX(tdta_standort_mast.lfd_nummer) "
                     + "FROM tdta_standort_mast "
                    + "LEFT JOIN tkey_strassenschluessel ON tdta_standort_mast.fk_strassenschluessel = tkey_strassenschluesselid "
                     + "LEFT JOIN tkey_kennziffer ON tdta_standort_mast.fk_kennziffer = tkey_kennziffer.id "
                    + "WHERE tkey_strassenschluesselpk like '" + strassenschluessel + "' "
                     + "AND tkey_kennziffer.kennziffer = " + kennziffer + ";";
 
         final MetaService metaService = (MetaService)getActiveLocalServers().get("BELIS");
 
         try {
             for (final ArrayList fields : metaService.performCustomSearch(query)) {
                 numbers.add((Integer)fields.get(0));
             }
         } catch (Exception ex) {
             LOG.error("problem fortfuehrung item search", ex);
         }
 
         return numbers;
     }
 }
