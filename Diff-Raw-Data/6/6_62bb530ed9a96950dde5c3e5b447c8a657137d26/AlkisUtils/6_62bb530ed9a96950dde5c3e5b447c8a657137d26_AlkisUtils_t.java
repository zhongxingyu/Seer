 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2011 thorsten
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.custom.objectrenderer.utils.alkis;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import de.aedsicad.aaaweb.service.util.Address;
 import de.aedsicad.aaaweb.service.util.Buchungsblatt;
 import de.aedsicad.aaaweb.service.util.Buchungsstelle;
 import de.aedsicad.aaaweb.service.util.LandParcel;
 import de.aedsicad.aaaweb.service.util.Owner;
 import de.aedsicad.aaaweb.service.util.Point;
 
 import javafx.beans.binding.StringBinding;
 
 import org.apache.commons.lang.ArrayUtils;
 
 import org.openide.util.Exceptions;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import java.net.URL;
 
 import java.util.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.utils.alkis.AlkisProducts;
 import de.cismet.cids.custom.wunda_blau.search.actions.ServerAlkisSoapAction;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.server.actions.HttpTunnelAction;
 import de.cismet.cids.server.actions.ServerActionParameter;
 
 import de.cismet.security.AccessHandler;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class AlkisUtils {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final AlkisProducts PRODUCTS = new AlkisProducts(
             AlkisConstants.COMMONS.USER,
             AlkisConstants.COMMONS.PASSWORD,
             AlkisConstants.COMMONS.SERVICE);
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlkisUtils.class);
     public static final String ALKIS_HTML_PRODUCTS_ENABLED = "custom.alkis.products.html.enabled";
     public static final String ALKIS_SOAP_OVER_CSA = "alkisSoapTunnelAction";
     static final ObjectMapper mapper = new ObjectMapper();
     static Buchungsblattbezirke buchungsblattbezirke;
 
     static {
         try {
             buchungsblattbezirke = mapper.readValue(Buchungsblattbezirke.class.getResourceAsStream(
                         "/de/cismet/cids/custom/wunda_blau/res/alkis/buchungsblattbezirke.json"),
                     Buchungsblattbezirke.class);
         } catch (Exception ex) {
             log.error("Problem while reading the Buchungsblattbezirke.", ex);
             buchungsblattbezirke = new Buchungsblattbezirke();
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     // --
     /**
      * DOCUMENT ME!
      *
      * @param   bean         DOCUMENT ME!
      * @param   description  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateLinkFromCidsBean(final CidsBean bean, final String description) {
         if (bean != null) {
             final int objectID = bean.getMetaObject().getId();
             final StringBuilder result = new StringBuilder("<a href=\"");
 //            result.append(bean.getMetaObject().getMetaClass().getID()).append(LINK_SEPARATOR_TOKEN).append(objectID);
             result.append(bean.getMetaObject().getMetaClass().getID())
                     .append(AlkisConstants.LINK_SEPARATOR_TOKEN)
                     .append(objectID);
             result.append("\">");
             result.append(description);
             result.append("</a>");
             return result.toString();
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsblattnummer  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String getBuchungsblattbezirkFromBuchungsblattnummer(final String buchungsblattnummer) {
         try {
             final String bezirksNr = buchungsblattnummer.substring(0, buchungsblattnummer.indexOf("-"));
             final String bezirksname = buchungsblattbezirke.getDistrictNamesMap().get(bezirksNr);
             final StringBuffer b = new StringBuffer(bezirksname).append(" (").append(bezirksNr).append(')');
             return b.toString();
         } catch (Exception e) {
             log.error("Error in getBuchungsblattbezirkFromBuchungsblattnummer(" + buchungsblattnummer + ")", e);
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   originatingFlurstueck  DOCUMENT ME!
      * @param   buchungsblatt          DOCUMENT ME!
      * @param   buchungsblattBean      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String buchungsblattToString(final CidsBean originatingFlurstueck,
             final Buchungsblatt buchungsblatt,
             final CidsBean buchungsblattBean) {
         final String alkisId = (String)originatingFlurstueck.getProperty("alkis_id");
 
         String pos = "";
         final Buchungsstelle[] buchungsstellen = buchungsblatt.getBuchungsstellen();
         for (final Buchungsstelle b : buchungsstellen) {
             for (final LandParcel lp : getLandparcelFromBuchungsstelle(b)) {
                 if (lp.getLandParcelCode().equals(alkisId)) {
                     pos = b.getSequentialNumber();
                 }
             }
         }
 
         final List<Owner> owners = Arrays.asList(buchungsblatt.getOwners());
         if ((owners != null) && (owners.size() > 0)) {
             final StringBuilder infoBuilder = new StringBuilder();
             infoBuilder.append(
                 "<table border=\"1px solid black\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"left\" valign=\"top\">");
 //            infoBuilder.append("<tr><td width=\"200\"><b><a href=\"").append(generateBuchungsblattLinkInfo(buchungsblatt)).append("\">").append(buchungsblatt.getBuchungsblattCode()).append("</a></b></td><td>");
             infoBuilder.append("<tr><td width=\"200\">Nr. " + pos + " auf  <b>")
                     .append(generateLinkFromCidsBean(buchungsblattBean, buchungsblatt.getBuchungsblattCode()))
                     .append("</b></td><td>");
             final Iterator<Owner> ownerIterator = owners.iterator();
 //            if (ownerIterator.hasNext()) {
 //                infoBuilder.append(ownerToString(ownerIterator.next(), ""));
 //            }
             infoBuilder.append(
                 "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"left\" valign=\"top\">");
             while (ownerIterator.hasNext()) {
                 infoBuilder.append(ownerToString(ownerIterator.next(), ""));
 //                infoBuilder.append(ownerToString(ownerIterator.next(), "</td><td>"));
             }
             infoBuilder.append("</table>");
             infoBuilder.append("</td></tr>");
             infoBuilder.append("</table>");
 //            infoBuilder.append("</html>");
             return infoBuilder.toString();
 //            lblBuchungsblattEigentuemer.setText(infoBuilder.toString());
         } else {
             return "";
 //            lblBuchungsblattEigentuemer.setText("-");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   address  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String addressToString(final Address address) {
         if (address != null) {
             final StringBuilder addressStringBuilder = new StringBuilder();
             if (address.getStreet() != null) {
                 addressStringBuilder.append(address.getStreet()).append(" ");
             }
             if (address.getHouseNumber() != null) {
                 addressStringBuilder.append(address.getHouseNumber());
             }
             if (addressStringBuilder.length() > 0) {
                 addressStringBuilder.append(AlkisConstants.NEWLINE);
             }
             if (address.getPostalCode() != null) {
                 addressStringBuilder.append(address.getPostalCode()).append(" ");
             }
             if (address.getCity() != null) {
                 addressStringBuilder.append(address.getCity());
             }
             if (addressStringBuilder.length() > 0) {
                 addressStringBuilder.append(AlkisConstants.NEWLINE);
             }
             return addressStringBuilder.toString();
         } else {
             return "";
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   landParcel  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String getLandparcelCodeFromParcelBeanObject(final Object landParcel) {
         if (landParcel instanceof CidsBean) {
             final CidsBean cidsBean = (CidsBean)landParcel;
             final Object parcelCodeObj = cidsBean.getProperty("alkis_id");
             if (parcelCodeObj != null) {
                 return parcelCodeObj.toString();
             }
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsstelle  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static LandParcel[] getLandparcelFromBuchungsstelle(final Buchungsstelle buchungsstelle) {
        if ((buchungsstelle.getBuchungsstellen() == null) && (buchungsstelle.getLandParcel() == null)) {
            log.warn("getLandparcelFromBuchungsstelle returns null. Problem on landparcel with number:"
                        + buchungsstelle.getSequentialNumber());
            return new LandParcel[0];
        } else if (buchungsstelle.getBuchungsstellen() == null) {
             return buchungsstelle.getLandParcel();
         } else {
             LandParcel[] result = buchungsstelle.getLandParcel();
             for (final Buchungsstelle b : buchungsstelle.getBuchungsstellen()) {
                 result = concatArrays(result, getLandparcelFromBuchungsstelle(b));
             }
             return result;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   a  DOCUMENT ME!
      * @param   b  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static LandParcel[] concatArrays(LandParcel[] a, LandParcel[] b) {
         if (a == null) {
             a = new LandParcel[0];
         }
         if (b == null) {
             b = new LandParcel[0];
         }
         return (LandParcel[])ArrayUtils.addAll(a, b);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   land       DOCUMENT ME!
      * @param   gemarkung  DOCUMENT ME!
      * @param   flur       DOCUMENT ME!
      * @param   zaehler    DOCUMENT ME!
      * @param   nenner     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateLandparcelCode(final int land,
             final int gemarkung,
             final int flur,
             final int zaehler,
             final int nenner) {
         final String withoutNenner = generateLandparcelCode(land, gemarkung, flur, zaehler);
         final StringBuilder sb = new StringBuilder(withoutNenner);
         sb.append(String.format("/%04d", nenner));
         return sb.toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   land       DOCUMENT ME!
      * @param   gemarkung  DOCUMENT ME!
      * @param   flur       DOCUMENT ME!
      * @param   zaehler    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateLandparcelCode(final int land,
             final int gemarkung,
             final int flur,
             final int zaehler) {
         return String.format("%02d%04d-%03d-%05d", land, gemarkung, flur, zaehler);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fullLandparcelCode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String prettyPrintLandparcelCode(final String fullLandparcelCode) {
         final String[] tiles = fullLandparcelCode.split("-");
         if (tiles.length == 1) {
             final String flurstueck = tiles[0];
             return _prettyPrintLandparcelCode(flurstueck);
         } else if (tiles.length == 2) {
             final String flurstueck = tiles[1];
             final String flur = tiles[0];
             final String result = _prettyPrintLandparcelCode(flurstueck, flur);
             return result;
         } else if (tiles.length == 3) {
             final String flurstueck = tiles[2];
             final String flur = tiles[1];
             final String gemarkung = tiles[0];
             return _prettyPrintLandparcelCode(flurstueck, flur, gemarkung);
         } else {
             return fullLandparcelCode;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   strings    DOCUMENT ME!
      * @param   separator  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String arrayToSeparatedString(final String[] strings, final String separator) {
         if (strings != null) {
             final StringBuilder result = new StringBuilder();
             for (int i = 0; i < strings.length; /**
                                                  * incremented in loop*
                                                  */) {
                 result.append(strings[i]);
                 if (++i < strings.length) {
                     result.append(separator);
                 }
             }
             return result.toString();
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toEscape  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String escapeHtmlSpaces(String toEscape) {
         if (toEscape != null) {
             toEscape = toEscape.replace(" ", "%20");
         }
         return toEscape;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   owner    DOCUMENT ME!
      * @param   spacing  Einrückung
      *
      * @return  DOCUMENT ME!
      */
     public static String ownerToString(final Owner owner, final String spacing) {
         if (owner != null) {
             final StringBuilder ownerStringBuilder = new StringBuilder();
             ownerStringBuilder.append("<tr><td width=\"50\">").append(spacing);
             if (owner.getNameNumber() != null) {
                 ownerStringBuilder.append(normalizeNameNumber(owner.getNameNumber()));
             }
             ownerStringBuilder.append("</td><td>");
             if (owner.getForeName() != null) {
                 ownerStringBuilder.append(owner.getForeName()).append(" ");
             }
             if (owner.getSurName() != null) {
                 ownerStringBuilder.append(owner.getSurName());
             }
             if (owner.getSalutation() != null) {
                 ownerStringBuilder.append(", ").append(owner.getSalutation());
             }
             if (owner.getDateOfBirth() != null) {
                 ownerStringBuilder.append(", *").append(owner.getDateOfBirth());
             }
             ownerStringBuilder.append("</td><td width=\"15\"></td><td>");
             if (owner.getPart() != null) {
                 ownerStringBuilder.append("<nobr>").append("zu ").append(owner.getPart()).append("</nobr>");
             }
             ownerStringBuilder.append("</td>");
 
             if (owner.getNameOfBirth() != null) {
                 ownerStringBuilder.append("<tr><td></td><td>")
                         .append("geb. ")
                         .append(owner.getNameOfBirth())
                         .append("</td><td></tr>");
             }
             ownerStringBuilder.append(AlkisConstants.NEWLINE).append("</td></tr>");
             final Address[] addresses = owner.getAddresses();
             if (addresses != null) {
                 for (final Address address : addresses) {
                     if (address != null) {
                         ownerStringBuilder.append("<tr><td></td>").append(spacing).append("<td>");
                         ownerStringBuilder.append(addressToString(address)).append(AlkisConstants.NEWLINE);
                         ownerStringBuilder.append("</td><td></td><td></td></tr>");
                     }
                 }
             }
             return ownerStringBuilder.toString();
         }
         return "";
     }
 
     /**
      * ----------------private.
      *
      * @param   in  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String removeLeadingZeros(final String in) {
         return in.replaceAll("^0*", "");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   nameNumber  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String normalizeNameNumber(final String nameNumber) {
         final String[] tokens = nameNumber.split("\\.");
         final StringBuilder result = new StringBuilder();
         for (String token : tokens) {
             token = removeLeadingZeros(token);
             if (token.length() > 0) {
                 result.append(token).append(".");
             }
         }
         if (result.length() > 0) {
             result.deleteCharAt(result.length() - 1);
             return result.toString();
         } else {
             return "0";
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsblatt  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String generateBuchungsblattLinkInfo(final Buchungsblatt buchungsblatt) {
         // TODO: Should return metaclassID::objectID instead of metaclassID::BuchungsblattCode...
         return new StringBuilder("ALKIS_BUCHUNGSBLATT").append(AlkisConstants.LINK_SEPARATOR_TOKEN)
                     .append(buchungsblatt.getBuchungsblattCode())
                     .toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   flurstueck  DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      * @param   gemarkung   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String _prettyPrintLandparcelCode(final String flurstueck,
             final String flur,
             final String gemarkung) {
         return _prettyPrintLandparcelCode(flurstueck, flur) + " - Gemarkung " + gemarkung;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   flurstueck  DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String _prettyPrintLandparcelCode(final String flurstueck, final String flur) {
         return _prettyPrintLandparcelCode(flurstueck) + " - Flur " + removeLeadingZeros(flur);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   flurstueck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String _prettyPrintLandparcelCode(final String flurstueck) {
         return "Flurstück " + prettyPrintFlurstueck(flurstueck);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fsZahlerNenner  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String prettyPrintFlurstueck(final String fsZahlerNenner) {
         final String[] tiles = fsZahlerNenner.split("/");
         if (tiles.length == 2) {
             return removeLeadingZeros(tiles[0]) + "/" + removeLeadingZeros(tiles[1]);
         } else if (tiles.length == 1) {
             return removeLeadingZeros(tiles[0]);
         }
         return fsZahlerNenner;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   blatt  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String getBuchungsartFromBuchungsblatt(final Buchungsblatt blatt) {
         final Buchungsstelle[] buchungsstellen = blatt.getBuchungsstellen();
         if ((buchungsstellen != null) && (buchungsstellen.length > 0)) {
             final ArrayList<Buchungsstelle> alleStellen = new ArrayList<Buchungsstelle>();
             alleStellen.addAll(Arrays.asList(buchungsstellen));
             if (isListOfSameBuchungsart(alleStellen)) {
                 return alleStellen.get(0).getBuchungsart();
             } else {
                 return "diverse";
             }
         }
         return "";
     }
 
     /**
      * Check if in list of Buchungsstellen, all Buchungsstellen have the same Buchungsart. Return true if all have the
      * same Buchungsart, false otherwise. The check is realized with adding the buchungsart to a set. As soon the set
      * contains a second buchungsart, false can be returned.
      *
      * @param   buchungsstellen  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean isListOfSameBuchungsart(final List<Buchungsstelle> buchungsstellen) {
         final Set<String> set = new HashSet<String>();
         for (final Buchungsstelle o : buchungsstellen) {
             if (set.isEmpty()) {
                 set.add(o.getBuchungsart());
             } else {
                 if (set.add(o.getBuchungsart())) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   aufteilungsnummer  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String prettyPrintAufteilungsnummer(final String aufteilungsnummer) {
         if (aufteilungsnummer != null) {
             return "ATP Nr. " + aufteilungsnummer;
         } else {
             return "";
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fraction  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String prettyPrintFraction(final String fraction) {
         if (fraction != null) {
             return "Anteil " + fraction;
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasAlkisPrintAccess() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), "navigator.alkis.print@WUNDA_BLAU")
                         != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis Print Dialog!", ex);
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasAlkisProductAccess() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), "navigator.alkis.products@WUNDA_BLAU")
                         != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis Products!", ex);
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasAlkisBuchungsblattAccess() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), "custom.alkis.buchungsblatt@WUNDA_BLAU")
                         != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis Buchungsblatt!", ex);
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasAlkisHTMLProductAccess() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(), ALKIS_HTML_PRODUCTS_ENABLED)
                         != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis HTML Products!", ex);
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserShouldUseAlkisSOAPServerActions() {
         try {
             return SessionManager.getConnection()
                         .getConfigAttr(SessionManager.getSession().getUser(),
                                 DomainServerImpl.SERVER_ACTION_PERMISSION_ATTRIBUTE_PREFIX
                                 + ALKIS_SOAP_OVER_CSA)
                         != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis SOAP CSA Calls!", ex);
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   pointCode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static Point getPointFromAlkisSOAPServerAction(final String pointCode) throws Exception {
         final ServerActionParameter pointCodeSAP = new ServerActionParameter<String>(
                 ServerAlkisSoapAction.RETURN_VALUE.POINT.toString(),
                 pointCode);
         final Object body = ServerAlkisSoapAction.RETURN_VALUE.POINT;
 
         final Point result = (Point)SessionManager.getProxy()
                     .executeTask(
                             ALKIS_SOAP_OVER_CSA,
                             "WUNDA_BLAU",
                             body,
                             pointCodeSAP);
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsblattCode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static Buchungsblatt getBuchungsblattFromAlkisSOAPServerAction(final String buchungsblattCode)
             throws Exception {
         final ServerActionParameter buchungsblattCodeSAP = new ServerActionParameter<String>(
                 ServerAlkisSoapAction.RETURN_VALUE.BUCHUNGSBLATT.toString(),
                 buchungsblattCode);
         final Object body = ServerAlkisSoapAction.RETURN_VALUE.BUCHUNGSBLATT;
 
         final Buchungsblatt result = (Buchungsblatt)SessionManager.getProxy()
                     .executeTask(
                             ALKIS_SOAP_OVER_CSA,
                             "WUNDA_BLAU",
                             body,
                             buchungsblattCodeSAP);
         return result;
     }
 }
