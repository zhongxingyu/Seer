 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.verdis.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import de.cismet.cids.server.search.AbstractCidsServerSearch;
 
 import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
 import de.cismet.verdis.commons.constants.VerdisConstants;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
public class KassenzeichenSearchStatement extends AbstractCidsServerSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(KassenzeichenSearchStatement.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final String searchString;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new KassenzeichenSearchStatement object.
      *
      * @param  searchString  DOCUMENT ME!
      */
     public KassenzeichenSearchStatement(final String searchString) {
         if (searchString == null) {
             this.searchString = "-1"; // NOI18N
         } else {
             this.searchString = searchString;
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
    public Collection performServerSearch() {
         try {
             final String sql;
             if (searchString.length() == 6) {
                 sql = "SELECT id FROM kassenzeichen WHERE " // NOI18N
                             + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                             + "/10 = "                      // NOI18N
                             + searchString;
             } else {
                 sql = "SELECT id FROM kassenzeichen WHERE " // NOI18N
                             + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                             + " = "                         // NOI18N
                             + searchString;
             }
 
             final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
 
             final ArrayList<ArrayList> result = ms.performCustomSearch(sql);
 
             final ArrayList aln = new ArrayList();
             for (final ArrayList al : result) {
                 final int oid = (Integer)al.get(1);
                 aln.add(oid);
             }
 
             return aln;
         } catch (final Exception e) {
             LOG.error("problem during kassenzeichen search", e); // NOI18N
 
             return null;
         }
     }
 }
