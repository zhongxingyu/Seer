 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.objectrenderer.utils.alkis;
 
 import de.aedsicad.aaaweb.service.util.Address;
 import de.aedsicad.aaaweb.service.util.Buchungsblatt;
 import de.aedsicad.aaaweb.service.util.Buchungsstelle;
 import de.aedsicad.aaaweb.service.util.Owner;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.objectrenderer.utils.PropertyReader;
 import de.cismet.cids.dynamics.CidsBean;
 import java.awt.Point;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 
 /**
  * @author srichter
  */
 public final class AlkisCommons {
 
     static {
         try {
             PropertyReader serviceProperties = new PropertyReader("/de/cismet/cids/custom/wunda_blau/res/alkis/alkis_conf.properties");
             final String server = serviceProperties.getProperty("SERVER");
             SERVER = server;
             SERVICE = serviceProperties.getProperty("SERVICE");
             USER = serviceProperties.getProperty("USER");
             PASSWORD = serviceProperties.getProperty("PASSWORD");
             //
             CATALOG_SERVICE = serviceProperties.getProperty("CATALOG_SERVICE");
             INFO_SERVICE = serviceProperties.getProperty("INFO_SERVICE");
             SEARCH_SERVICE = serviceProperties.getProperty("SEARCH_SERVICE");
             //
             BUCH_NACHWEIS_SERVICE = server + serviceProperties.getProperty("BUCH_NACHWEIS_SERVICE");
             LISTEN_NACHWEIS_SERVICE = server + serviceProperties.getProperty("LISTEN_NACHWEIS_SERVICE");
             LIEGENSCHAFTSKARTE_SERVICE = server + serviceProperties.getProperty("LIEGENSCHAFTSKARTE_SERVICE");
             //
            String srs = serviceProperties.getProperty("SRS");
            SRS = srs;
            MAP_CALL_STRING = serviceProperties.getProperty("MAP_CALL_STRING") + srs;
             GEO_BUFFER = Double.parseDouble(serviceProperties.getProperty("GEO_BUFFER"));
             //
             Map<String, List<AlkisProduct>> productsMap = new HashMap<String, List<AlkisProduct>>();
             Map<String, Point> formatMap = new HashMap<String, Point>();
             PropertyReader formats = new PropertyReader("/de/cismet/cids/custom/wunda_blau/res/alkis/formats.properties");
             InputStream is = AlkisCommons.class.getClassLoader().getResourceAsStream("de/cismet/cids/custom/wunda_blau/res/alkis/Produktbeschreibung_ALKIS.xml");
             Document document = new SAXBuilder().build(is);
             //---------Kartenprodukte----------
             for (Object o0 : document.getRootElement().getChildren()) {
                 Element category = (Element) o0;
                 String catName = category.getName();
                 if (!"Layernamen".equals(category.getName())) {
                     List<AlkisProduct> productList = new ArrayList<AlkisProduct>();
                     for (Object o1 : category.getChildren()) {
                         Element productClass = (Element) o1;
                         String clazz = productClass.getAttribute("Name").getValue();
                         for (Object o2 : productClass.getChildren()) {
                             Element guiProduct = (Element) o2;
                             String type = guiProduct.getAttribute("Name").getValue();
                             for (Object o3 : guiProduct.getChildren()) {
                                 Element singleProduct = (Element) o3;
                                 String code = singleProduct.getAttribute("ID").getValue();
                                 String dinFormatCode = singleProduct.getAttribute("Layout").getValue();
                                 String layoutDim = formats.getProperty(dinFormatCode);
                                 int width = -1;
                                 int height = -1;
                                 if (layoutDim == null) {
                                     org.apache.log4j.Logger.getLogger(AlkisCommons.class).info("Can not find format dimensions for: " + dinFormatCode);
                                 } else {
                                     String[] dims = layoutDim.split("(x|X)");
                                     width = Integer.parseInt(dims[0]);
                                     height = Integer.parseInt(dims[1]);
                                     formatMap.put(dinFormatCode, new Point(width, height));
                                 }
                                 Element preisFaktoren = (Element) singleProduct.getChildren().get(0);
                                 String dinFormat = preisFaktoren.getAttribute("DINFormat").getValue();
                                 String fileFormat = preisFaktoren.getAttribute("Dateiformat").getValue();
                                 Attribute massstabAttr = preisFaktoren.getAttribute("Massstab");
                                 String massstab;
                                 if (massstabAttr != null) {
                                     massstab = preisFaktoren.getAttribute("Massstab").getValue();
                                 } else {
                                     massstab = "-";
                                 }
                                 productList.add(new AlkisProduct(clazz, type, code, dinFormat, massstab, fileFormat, width, height));
                             }
                         }
                     }
                     productsMap.put(catName, productList);
                 }
             }
             ALKIS_FORMATS = Collections.unmodifiableMap(formatMap);
             ALKIS_PRODUCTS = Collections.unmodifiableMap(productsMap);
         } catch (Exception ex) {
             org.apache.log4j.Logger.getLogger(AlkisCommons.class).error(ex, ex);
             throw new RuntimeException(ex);
         }
     }
 
     public static enum ProductFormat {
 
         PDF("PDF"), HTML("HTML"), TEXT("TXT");
 
         private ProductFormat(String string) {
             this.formatString = string;
         }
         private final String formatString;
 
         @Override
         public String toString() {
             return formatString;
         }
 
         ;
     }
     public static final Map<String, List<AlkisProduct>> ALKIS_PRODUCTS;
     public static final String USER;
     public static final String PASSWORD;
     public static final String SERVICE;
     public static final String SERVER;
     public static final String CATALOG_SERVICE;
     public static final String INFO_SERVICE;
     public static final String SEARCH_SERVICE;
     public static final Map<String, Point> ALKIS_FORMATS;
     public static final String SRS;// = "EPSG:31466";
     public static final String MAP_CALL_STRING;// = "http://s102x082.wuppertal-intra.de:8080/wmsconnector/com.esri.wms.Esrimap/web_navigation_lf?&VERSION=1.1.1&REQUEST=GetMap&SRS=" + SRS + "&FORMAT=image/png&TRANSPARENT=TRUE&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0&STYLES="
 //                + "&BBOX=<cismap:boundingBox>"
 //                + "&WIDTH=<cismap:width>"
 //                + "&HEIGHT=<cismap:height>";
     public static final double GEO_BUFFER;// = 5.0;
     public static final String BUCH_NACHWEIS_SERVICE;
     public static final String LISTEN_NACHWEIS_SERVICE;
     public static final String LIEGENSCHAFTSKARTE_SERVICE;
 
     public static final class Products {
 
         public static final String PRODUCT_GROUP_EINZELNACHWEIS = "Einzelnachweis";
         public static final String PRODUCT_GROUP_KARTE = "Karte";
         public static final String PRODUCT_GROUP_LISTENNACHWEIS = "Listennachweis";
         //
         public static final String PRODUCT_BESTANDSNACHWEIS = "Bestandsnachweis";
         public static final String PRODUCT_FLURSTUECKSNACHWEIS = "Flurstücksnachweis";
         public static final String PRODUCT_EIGENTUMSNACHWEIS = "Flurstücks- und Eigentumsnachweis";
         public static final String PRODUCT_PUNKTLISTE = "Punktliste";
         //
         private static final String IDENTIFICATION = "user=" + USER + "&password=" + PASSWORD + "&service=" + SERVICE;
         //
 
         private Products() {
             throw new AssertionError();
         }
 
         public static void productBestandsnachweisProduct(String buchungsblattCode, ProductFormat format) {
             List<AlkisProduct> einzelnachweise = ALKIS_PRODUCTS.get(PRODUCT_GROUP_EINZELNACHWEIS);
             for (AlkisProduct product : einzelnachweise) {
                 if (PRODUCT_BESTANDSNACHWEIS.equals(product.getType()) && format.formatString.equals(product.getFileFormat()) && product.getCode().contains("GDBNRW.A")) {
                     String url = BUCH_NACHWEIS_SERVICE + "?" + IDENTIFICATION + "&product=" + product.getCode() + "&id=" + buchungsblattCode;
                     ObjectRendererUtils.openURL(url);
                 }
             }
         }
 
         public static void productFlurstuecksnachweis(String parcelCode, ProductFormat format) {
             List<AlkisProduct> einzelnachweise = ALKIS_PRODUCTS.get(PRODUCT_GROUP_EINZELNACHWEIS);
             for (AlkisProduct product : einzelnachweise) {
                 if (PRODUCT_FLURSTUECKSNACHWEIS.equals(product.getType()) && format.formatString.equals(product.getFileFormat()) && product.getCode().contains("GDBNRW.A")) {
                     String url = BUCH_NACHWEIS_SERVICE + "?" + IDENTIFICATION + "&product=" + product.getCode() + "&id=" + parcelCode;
                     ObjectRendererUtils.openURL(url);
                 }
             }
         }
 
         public static void productFlurstuecksEigentumsnachweis(String parcelCode, ProductFormat format) {
             List<AlkisProduct> einzelnachweise = ALKIS_PRODUCTS.get(PRODUCT_GROUP_EINZELNACHWEIS);
             for (AlkisProduct product : einzelnachweise) {
                 if (PRODUCT_EIGENTUMSNACHWEIS.equals(product.getType()) && format.formatString.equals(product.getFileFormat()) && product.getCode().contains("GDBNRW.A")) {
                     String url = BUCH_NACHWEIS_SERVICE + "?" + IDENTIFICATION + "&product=" + product.getCode() + "&id=" + parcelCode;
                     ObjectRendererUtils.openURL(url);
                 }
             }
         }
 
         public static void productPunktliste(String pointID, String pointArt, ProductFormat format) {
             productPunktliste(pointArt + ":" + pointID, format);
         }
 
         public static void productPunktliste(String punktliste, ProductFormat format) {
             List<AlkisProduct> listennachweise = ALKIS_PRODUCTS.get(PRODUCT_GROUP_LISTENNACHWEIS);
             for (AlkisProduct product : listennachweise) {
                 if (PRODUCT_PUNKTLISTE.equals(product.getType()) && format.formatString.equals(product.getFileFormat()) && product.getCode().contains("GDBNRW.A")) {
                     final String url = LISTEN_NACHWEIS_SERVICE + "?" + IDENTIFICATION + "&product=" + product.getCode() + "&ids=" + punktliste;
                     ObjectRendererUtils.openURL(url);
                 }
             }
         }
 
         public static void productPunktliste(String[] pointIDs, String[] pointArts, ProductFormat format) {
             StringBuilder punktListe = new StringBuilder();
             for (int i = 0; i < pointIDs.length; ++i) {
                 if (punktListe.length() > 0) {
                     punktListe.append(",");
                 }
                 punktListe.append(pointArts[i]).append(":").append(pointIDs[i]);
             }
             productPunktliste(punktListe.toString(), format);
         }
 
         public static void productKarte(String parcelCode) {
             String url = LIEGENSCHAFTSKARTE_SERVICE + "?" + IDENTIFICATION + "&landparcel=" + parcelCode;
             ObjectRendererUtils.openURL(url);
         }
 
         public static void productKarte(String parcelCode, AlkisProduct produkt, int winkel, int centerX, int centerY, String zusText, boolean moreThanOneParcel) {
             String url = LIEGENSCHAFTSKARTE_SERVICE + "?" + IDENTIFICATION + "&landparcel=" + parcelCode
                     + "&angle=" + winkel
                     + "&product=" + produkt.getCode()
                     + "&centerx=" + centerX + "&centery=" + centerY;
             if (zusText != null && zusText.length() > 0) {
                 url += "&text=" + zusText;
             }
             if (moreThanOneParcel) {
                 url += "&additionalLandparcel=true";
             }
             url += "&";
             ObjectRendererUtils.openURL(url);
         }
     }
 
     public static String generateLinkFromCidsBean(CidsBean bean, String description) {
         if (bean != null) {
             final int objectID = bean.getMetaObject().getId();
             final StringBuilder result = new StringBuilder("<a href=\"");
 //            result.append(bean.getMetaObject().getMetaClass().getID()).append(LINK_SEPARATOR_TOKEN).append(objectID);
             result.append(bean.getMetaObject().getMetaClass().getID()).append(LINK_SEPARATOR_TOKEN).append(objectID);
             result.append("\">");
             result.append(description);
             result.append("</a>");
             return result.toString();
         }
         return "";
     }
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlkisCommons.class);
     private static final String NEWLINE = "<br>";
     public static final String LINK_SEPARATOR_TOKEN = "::";
 
     public static String buchungsblattToString(Buchungsblatt buchungsblatt, CidsBean buchungsblattBean) {
         final List<Owner> owners = Arrays.asList(buchungsblatt.getOwners());
         if (owners != null && owners.size() > 0) {
             final StringBuilder infoBuilder = new StringBuilder();
             infoBuilder.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"left\" valign=\"top\">");
 //            infoBuilder.append("<tr><td width=\"200\"><b><a href=\"").append(generateBuchungsblattLinkInfo(buchungsblatt)).append("\">").append(buchungsblatt.getBuchungsblattCode()).append("</a></b></td><td>");
             infoBuilder.append("<tr><td width=\"200\"><b>").append(generateLinkFromCidsBean(buchungsblattBean, buchungsblatt.getBuchungsblattCode())).append("</b></td><td>");
             final Iterator<Owner> ownerIterator = owners.iterator();
 //            if (ownerIterator.hasNext()) {
 //                infoBuilder.append(ownerToString(ownerIterator.next(), ""));
 //            }
             infoBuilder.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"left\" valign=\"top\">");
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
 
     public static String addressToString(Address address) {
         if (address != null) {
             final StringBuilder addressStringBuilder = new StringBuilder();
             if (address.getStreet() != null) {
                 addressStringBuilder.append(address.getStreet()).append(" ");
             }
             if (address.getHouseNumber() != null) {
                 addressStringBuilder.append(address.getHouseNumber());
             }
             if (addressStringBuilder.length() > 0) {
                 addressStringBuilder.append(NEWLINE);
             }
             if (address.getPostalCode() != null) {
                 addressStringBuilder.append(address.getPostalCode()).append(" ");
             }
             if (address.getCity() != null) {
                 addressStringBuilder.append(address.getCity());
             }
             if (addressStringBuilder.length() > 0) {
                 addressStringBuilder.append(NEWLINE);
             }
             return addressStringBuilder.toString();
         } else {
             return "";
         }
     }
 
     public static String getLandparcelCodeFromParcelBeanObject(Object landParcel) {
         if (landParcel instanceof CidsBean) {
             CidsBean cidsBean = (CidsBean) landParcel;
             final Object parcelCodeObj = cidsBean.getProperty("alkis_id");
             if (parcelCodeObj != null) {
                 return parcelCodeObj.toString();
             }
         }
         return "";
     }
 
     public static String prettyPrintLandparcelCode(String fullLandparcelCode) {
         final String[] tiles = fullLandparcelCode.split("-");
         if (tiles.length == 1) {
             String flurstueck = tiles[0];
             return _prettyPrintLandparcelCode(flurstueck);
         } else if (tiles.length == 2) {
             String flurstueck = tiles[1];
             String flur = tiles[0];
             String result = _prettyPrintLandparcelCode(flurstueck, flur);
             return result;
         } else if (tiles.length == 3) {
             String flurstueck = tiles[2];
             String flur = tiles[1];
             String gemarkung = tiles[0];
             return _prettyPrintLandparcelCode(flurstueck, flur, gemarkung);
         } else {
             return fullLandparcelCode;
         }
     }
 
     public static String arrayToSeparatedString(String[] strings, String separator) {
         if (strings != null) {
             final StringBuilder result = new StringBuilder();
             for (int i = 0; i < strings.length;/**incremented in loop**/
                     ) {
                 result.append(strings[i]);
                 if (++i < strings.length) {
                     result.append(separator);
                 }
             }
             return result.toString();
         }
         return "";
     }
 
     public static String escapeHtmlSpaces(String toEscape) {
         if (toEscape != null) {
             toEscape = toEscape.replace(" ", "%20");
         }
         return toEscape;
     }
 
     /**
      *
      * @param owner
      * @param spacing Einrückung
      * @return
      */
     public static String ownerToString(Owner owner, String spacing) {
         if (owner != null) {
             final StringBuilder ownerStringBuilder = new StringBuilder();
             ownerStringBuilder.append("<tr><td width=\"75\">").append(spacing);
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
             if (owner.getNameOfBirth() != null) {
                 ownerStringBuilder.append(NEWLINE + "geb. ").append(owner.getNameOfBirth());
             }
             ownerStringBuilder.append(NEWLINE).append("</td></tr>");
             final Address[] addresses = owner.getAddresses();
             if (addresses != null) {
                 for (final Address address : addresses) {
                     if (address != null) {
                         ownerStringBuilder.append("<tr><td></td>").append(spacing).append("<td>");
                         ownerStringBuilder.append(addressToString(address)).append(NEWLINE);
                         ownerStringBuilder.append("</td></tr>");
                     }
                 }
             }
             return ownerStringBuilder.toString();
         }
         return "";
     }
 
     //----------------private
     private static String removeLeadingZeros(String in) {
         return in.replaceAll("^0*", "");
     }
 
     private static String normalizeNameNumber(String nameNumber) {
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
 
     private static String generateBuchungsblattLinkInfo(Buchungsblatt buchungsblatt) {
         //TODO: Should return metaclassID::objectID instead of metaclassID::BuchungsblattCode...
         return new StringBuilder("ALKIS_BUCHUNGSBLATT").append(LINK_SEPARATOR_TOKEN).append(buchungsblatt.getBuchungsblattCode()).toString();
     }
 
     private static String _prettyPrintLandparcelCode(String flurstueck, String flur, String gemarkung) {
         return _prettyPrintLandparcelCode(flurstueck, flur) + " - Gemarkung " + gemarkung;
     }
 
     private static String _prettyPrintLandparcelCode(String flurstueck, String flur) {
         return _prettyPrintLandparcelCode(flurstueck) + " - Flur " + removeLeadingZeros(flur);
     }
 
     private static String _prettyPrintLandparcelCode(String flurstueck) {
         return "Flurstück " + prettyPrintFlurstueck(flurstueck);
     }
 
     private static String prettyPrintFlurstueck(String fsZahlerNenner) {
         String[] tiles = fsZahlerNenner.split("/");
         if (tiles.length == 2) {
             return removeLeadingZeros(tiles[0]) + "/" + removeLeadingZeros(tiles[1]);
         } else if (tiles.length == 1) {
             return removeLeadingZeros(tiles[0]);
         }
         return fsZahlerNenner;
     }
 
     public static String getBuchungsartFromBuchungsblatt(Buchungsblatt blatt) {
         final Buchungsstelle[] buchungsstellen = blatt.getBuchungsstellen();
         if (buchungsstellen != null && buchungsstellen.length > 0) {
             final Buchungsstelle letzteBuchungsstelle = buchungsstellen[buchungsstellen.length - 1];
             if (letzteBuchungsstelle != null) {
                 final StringBuilder result = new StringBuilder();
                 final String prettyFration = prettyPrintFration(letzteBuchungsstelle.getFraction());
                 result.append(prettyFration);
                 if (prettyFration.length() > 0) {
                     result.append(" ");
                 }
                 result.append(letzteBuchungsstelle.getBuchungsart());
                 return result.toString();
             }
         }
         return "";
     }
 
     public static String prettyPrintFration(String fraction) {
         if (fraction != null) {
             final String[] elements = fraction.split("/");
             if (elements != null && elements.length == 2) {
                 String zaehler = elements[0];
                 String nenner = elements[1];
                 zaehler = zaehler.substring(0, zaehler.lastIndexOf("."));
                 nenner = nenner.substring(0, nenner.lastIndexOf("."));
                 return zaehler + "/" + nenner;
             }
         }
         return "";
     }
 }
