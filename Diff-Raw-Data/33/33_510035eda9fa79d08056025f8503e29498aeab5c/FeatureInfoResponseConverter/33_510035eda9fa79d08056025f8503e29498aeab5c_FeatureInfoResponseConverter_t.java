 /*
  * Copyright (c) 2012-2013, Dienst Landelijk Gebied - Ministerie van Economische Zaken
  * 
  * Gepubliceerd onder de BSD 2-clause licentie, 
  * zie https://github.com/MinELenI/CBSviewer/blob/master/LICENSE.md voor de volledige licentie. 
  */
 package nl.mineleni.cbsviewer.servlet.wms;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Iterator;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import nl.mineleni.cbsviewer.util.LabelsBundle;
 import nl.mineleni.cbsviewer.util.xml.LayerDescriptor;
 
 import org.geotools.GML;
 import org.geotools.GML.Version;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.feature.DefaultFeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.geometry.primitive.Point;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 /**
  * Utility klasse FeatureInfoResponseConverter kan gebruikt worden om
  * FeatureInfo responses te parsen en te converteren naar een andere vorm.
  * 
  * @author mprins
  * @since 1.7
  */
 public final class FeatureInfoResponseConverter {
 
 	/**
 	 * ondersteunde typen voor conversie.
 	 */
 	public enum CONVERTER_TYPE {
 		GMLTYPE("application/vnd.ogc.gml"), HTMLTYPE("text/html"),
		/* XMLTYPE("application/vnd.ogc.wms_xml") */;

 		private final String type;
 
 		CONVERTER_TYPE(final String type) {
 			this.type = type;
 		}
 
 		/**
 		 * String value van dit object.
 		 * 
 		 * @return de waarde van dit object als string
 		 * @see java.lang.Enum#toString()
 		 * @see java.lang.Number#toString()
 		 */
 		@Override
 		public String toString() {
 			return this.type;
 		}
 	}
 
 	/** logger. */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(FeatureInfoResponseConverter.class);
 
 	/** attribuut namen filter. */
 	private static final AttributesNamesFilter NAMESFILTER = new AttributesNamesFilter();
 
 	/** resource bundle. */
 	private static final LabelsBundle RESOURCES = new LabelsBundle();
 
 	/** attribuut waarden filter. */
 	private static final AttributeValuesFilter VALUESFILTER = new AttributeValuesFilter();
 
 	/**
 	 * Cleanup html.
 	 * 
 	 * @param htmlStream
 	 *            input HTML stream, bijvoorbeeld uit een GetFeatureInfo
 	 *            request.
 	 * @return the string
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	private static String cleanupHTML(final InputStream htmlStream,
 			final LayerDescriptor layer) throws IOException {
 		final Element table = Jsoup.parse(convertStreamToString(htmlStream))
 				.select("table").first();
 		if (table == null) {
 			LOGGER.debug("Geen attribuut info voor deze locatie/zoomnivo.");
 			return RESOURCES.getString("KEY_INFO_GEEN_FEATURES");
 		}
 
 		final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(
 				"internal");
 		final SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
 
 		// 1e rij zijn headers, andere rijen zijn data, geheekl converteren naar
 		// een SimpleFeatureCollection
 		final Element headers = table.select("tr:first-child").first();
 		final Iterator<Element> THs = headers.select("th").iterator();
 		while (THs.hasNext()) {
 			b.add(THs.next().text(), String.class);
 		}
 		b.setCRS(null);
 		b.add("point", Point.class);
 		b.setName("tablerow");
 		final SimpleFeatureType type = b.buildFeatureType();
 		final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
 		final Iterator<Element> rows = table.select("tr:not(:first-child)")
 				.iterator();
 		while (rows.hasNext()) {
 			final Iterator<Element> TH = headers.select("th").iterator();
 			final Iterator<Element> TDs = rows.next().select("td").iterator();
 			final SimpleFeature f = builder.buildFeature(null);
 			while (TDs.hasNext()) {
 				f.setAttribute(TH.next().text(), TDs.next().text());
 			}
 			featureCollection.add(f);
 		}
 		return featureCollectionConverter(featureCollection.features(), layer);
 	}
 
 	/**
 	 * Converteer gml imputstream naar html tabel.
 	 * 
 	 * @param gmlStream
 	 *            input GML stream, bijvoorbeeld uit een GetFeatureInfo request.
 	 * @param layer
 	 *            the layer
 	 * @return een html tabel
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	private static String convertGML(final InputStream gmlStream,
 			final LayerDescriptor layer) throws IOException {
 
 		try {
 			final GML gml = new GML(Version.WFS1_0);
 			final SimpleFeatureIterator iter = gml
 					.decodeFeatureIterator(gmlStream);
 			return featureCollectionConverter(iter, layer);
 		} catch (ParserConfigurationException | SAXException e) {
 			LOGGER.error("Fout tijdens parsen van GML. ", e);
 			return "";
 		} finally {
 			gmlStream.close();
 		}
 
 	}
 
 	/**
 	 * Converteert een stream naar een string.
 	 * 
 	 * @param is
 	 *            de InputStream met data
 	 * @return de data als string
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	private static String convertStreamToString(final InputStream is)
 			throws IOException {
 		final Writer writer = new StringWriter();
 		if (is != null) {
 
 			final char[] buffer = new char[1024];
 			try {
 				final Reader reader = new BufferedReader(new InputStreamReader(
 						is, "UTF-8"));
 				int n;
 				while ((n = reader.read(buffer)) != -1) {
 					writer.write(buffer, 0, n);
 				}
 			} finally {
 				is.close();
 			}
 		}
 		return writer.toString();
 	}
 
 	/**
 	 * Converteer de input naar een html tabel.
 	 * 
 	 * @param input
 	 *            inputstream met de featureinfo response.
 	 * @param type
 	 *            het type conversie, ondersteund is {@code "GMLTYPE"}
 	 * @param layer
 	 *            de layerdescriptor
 	 * @return een html tabel
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	public static String convertToHTMLTable(final InputStream input,
 			final CONVERTER_TYPE type, final LayerDescriptor layer)
 			throws IOException {
 		switch (type) {
 		case GMLTYPE:
 			return convertGML(input, layer);
 		case HTMLTYPE:
 			return cleanupHTML(input, layer);
 		default:
 			return convertStreamToString(input);
 		}
 	}
 
 	private static String featureCollectionConverter(
 			final FeatureIterator<SimpleFeature> iter,
 			final LayerDescriptor layer) {
 		final StringBuilder sb = new StringBuilder();
 		final String[] fieldNames = layer.getAttributes().split(",\\s*");
 
 		if (iter.hasNext()) {
 			// tabel maken
			sb.append("<table id=\"attribuutTabel\" class=\"attribuutTabel\">");
			sb.append("<caption>");
 			sb.append(RESOURCES.getString("KEY_INFO_TABEL_CAPTION"));
 			sb.append("</caption><thead><tr>");
 			for (final String n : fieldNames) {
				sb.append("<th scope=\"col\">");
				sb.append(NAMESFILTER.filterValue(n, layer.getId()));
				sb.append("</th>");
 			}
 			sb.append("</tr></thead><tbody>");
 			int i = 0;
 			while (iter.hasNext()) {
 				sb.append("<tr class=\"" + (((i++ % 2) == 0) ? "odd" : "even")
 						+ "\">");
 				final SimpleFeature f = iter.next();
 				for (final String n : fieldNames) {
 					if (VALUESFILTER.hasFilters()) {
						sb.append("<td>");
						sb.append(VALUESFILTER.filterValue(f.getAttribute(n)));
						sb.append("</td>");
 					} else {
						sb.append("<td>");
						sb.append(f.getAttribute(n));
						sb.append("</td>");
 					}
 				}
 				sb.append("</tr>");
 			}
 			sb.append("</tbody></table>");
 			iter.close();
 			LOGGER.debug("Gemaakte HTML tabel: " + sb);
 			return sb.toString();
 		} else {
 			LOGGER.debug("Geen attribuut info voor deze locatie/zoomnivo.");
 			return RESOURCES.getString("KEY_INFO_GEEN_FEATURES");
 		}
 	}
 
 	/**
 	 * private constructor.
 	 */
 	private FeatureInfoResponseConverter() {
 		// private constructor voor utility klasse
 	}
 }
