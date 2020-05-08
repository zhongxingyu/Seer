 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.wrrl_db_mv.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 
 import org.apache.log4j.Logger;
 
 import java.rmi.RemoteException;
 
 import java.text.MessageFormat;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Locale;
 
 import de.cismet.cids.server.search.AbstractCidsServerSearch;
 
 /**
  * Searchs for the wk_k the given geometry is contained in. The pgsql function getWk_k(integer, geometry) must exist in
  * the database.
  *
  * @author   therter
  * @version  $Revision$, $Date$
  */
 public class WkSearchByStations extends AbstractCidsServerSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(WkSearchByStations.class);
 
     private static final String QUERY = "SELECT   wk_fg.wk_k   , "
                + "         MIN(von.wert), "
                + "         MAX(bis.wert) "
                 + "FROM     wk_fg        , "
                 + "         wk_fg_teile  , "
                 + "         wk_teil      , "
                 + "         station_linie, "
                 + "         station von  , "
                 + "         station bis  , "
                 + "         route "
                 + "WHERE    wk_fg.teile      =wk_fg_teile.wk_fg_reference "
                 + "AND      wk_fg_teile.teil =wk_teil.id "
                 + "AND      wk_teil.linie    =station_linie.id "
                 + "AND      station_linie.von=von.id "
                 + "AND      station_linie.bis=bis.id "
                 + "AND      von.route        =route.id "
                 + "AND      route.gwk        ={0} "
                + "GROUP BY wk_fg.wk_k "
                + "HAVING   ( "
                 + "                  ( "
                + "                           {1}  >= MIN(von.wert) "
                + "                  AND      {1}  < MAX(bis.wert) "
                 + "                  ) "
                 + "         OR "
                 + "                  ( "
                + "                           {2}  > MIN(von.wert) "
                + "                  AND      {2}  <= MAX(bis.wert) "
                 + "                  ) "
                 + "         ) "
 //                + "HAVING   ( "
 //                + "                  ( "
 //                + "                           {1}  <MAX(bis.wert) "
 //                + "                  AND      {2}  >MAX(bis.wert) "
 //                + "                  ) "
 //                + "         OR "
 //                + "                  ( "
 //                + "                           {1}  <MIN(von.wert) "
 //                + "                  AND      {2}  >MIN(von.wert) "
 //                + "                  ) "
 //                + "         OR "
 //                + "                  ( "
 //                + "                           {1}  <=MIN(von.wert) "
 //                + "                  AND      {2}  >=MAX(bis.wert) "
 //                + "                  ) "
 //                + "         ) "
                + "ORDER BY MIN(von.wert)";
 //    private static final String QUERY = "SELECT   wk_fg.wk_k   , "
 //                + "         MIN(von.wert), "
 //                + "         MAX(bis.wert) "
 //                + "FROM     wk_fg        , "
 //                + "         wk_fg_teile  , "
 //                + "         wk_teil      , "
 //                + "         station_linie, "
 //                + "         station von  , "
 //                + "         station bis  , "
 //                + "         route "
 //                + "WHERE    wk_fg.teile      =wk_fg_teile.wk_fg_reference "
 //                + "AND      wk_fg_teile.teil =wk_teil.id "
 //                + "AND      wk_teil.linie    =station_linie.id "
 //                + "AND      station_linie.von=von.id "
 //                + "AND      station_linie.bis=bis.id "
 //                + "AND      von.route        =route.id "
 //                + "AND      route.gwk        ={0} "
 //                + "GROUP BY wk_fg.wk_k "
 //                + "HAVING   ( "
 //                + "                  ( "
 //                + "                           {1}  >=MIN(von.wert) "
 //                + "                  AND      {2}  >=MAX(bis.wert) "
 //                + "                  ) "
 //                + "         OR "
 //                + "                  ( "
 //                + "                           {1}  <=MIN(von.wert) "
 //                + "                  AND      {2}  >=MAX(bis.wert) "
 //                + "                  ) "
 //                + "         OR "
 //                + "                  ( "
 //                + "                           {1}  <=MIN(von.wert) "
 //                + "                  AND      {2}  >=MIN(von.wert) "
 //                + "                  ) "
 //                + "         ) "
 //                + "ORDER BY MIN(von.wert)";
     private static final String WRRL_DOMAIN = "WRRL_DB_MV"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final double from;
     private final double to;
     private final String route_gwk;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new WkSearchByStations object.
      *
      * @param  from       DOCUMENT ME!
      * @param  to         DOCUMENT ME!
      * @param  route_gwk  DOCUMENT ME!
      */
     public WkSearchByStations(final double from, final double to, final String route_gwk) {
         this.from = from;
         this.to = to;
         this.route_gwk = route_gwk;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Collection performServerSearch() {
         final MetaService ms = (MetaService)getActiveLocalServers().get(WRRL_DOMAIN);
 
         if (ms != null) {
             try {
                 final String query = MessageFormat.format(
                         QUERY,
                         route_gwk,
                         String.format(Locale.US, "%f", from),
                         String.format(Locale.US, "%f", to));
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("query: " + query); // NOI18N
                 }
                 final ArrayList<ArrayList> lists = ms.performCustomSearch(query);
                 return lists;
             } catch (RemoteException ex) {
                 LOG.error(ex.getMessage(), ex);
             }
         } else {
             LOG.error("active local server not found"); // NOI18N
         }
 
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         System.out.println(MessageFormat.format(
                 QUERY,
                "4711",
                String.format(Locale.US, "%f", 0.67),
                String.format(Locale.US, "%f", 859457f)));
     }
 }
