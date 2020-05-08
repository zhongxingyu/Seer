 package gov.usgs.cida.gdp.wps.parser;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import javax.xml.XMLConstants;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.n52.wps.io.IStreamableParser;
 import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
 import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
 
 public class GMLStreamingParser extends AbstractXMLParser implements IStreamableParser {
 
 	private static Logger LOGGER = Logger.getLogger(GMLStreamingParser.class);
 
 	public GMLStreamingParser() {
 		super();
 	}
 
 	public GMLStreamingParser(boolean readWPSConfig) {
 		super(readWPSConfig);
 	}
 
 	private GTVectorDataBinding parseXML(File file) {
 
 		GMLStreamingFeatureCollection fc = new GMLStreamingFeatureCollection(file);
 
 		GTVectorDataBinding data = new GTVectorDataBinding(fc);
 
 		return data;
 	}
 
 	@Override
 	public GTVectorDataBinding parseXML(String gml) {
 		if (gml.startsWith("<xml-fragment")) {
 			gml = gml.replaceFirst("<xml-fragment .*?>", "");
 			gml = gml.replaceFirst("</xml-fragment>", "");
 		}
 		// TODO find a better solution. XML-beans hands in inappropriate XML, so the namespaces have to be set manually.
 		if (gml.indexOf("xmlns:xsi=") < 0) {
 			gml = gml.replaceFirst("<wfs:FeatureCollection", "<wfs:FeatureCollection xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"");
 		}
 		return parseXML(new ByteArrayInputStream(gml.getBytes()));
 	}
 
 	@Override
 	public GTVectorDataBinding parseXML(InputStream inputStream) {
 		try {
             File tempFile = File.createTempFile(getClass().getSimpleName(), ".xml");
             FileUtils.copyInputStreamToFile(inputStream, tempFile);
 			return parseXML(tempFile);
 		} catch (IOException e) {
 			throw new RuntimeException("Error creating temporary file", e);
 		}
 	}
 
 	@Override
 	public GTVectorDataBinding parseXML(URI uri) {
 		InputStream inputStream = null;
 		try {
 			URL url = uri.toURL();
 			URLConnection connection = url.openConnection();
 			connection.setDoInput(true);
 			connection.setDoOutput(false);
 			inputStream = connection.getInputStream();
 			return parseXML(inputStream);
 		} catch (MalformedURLException e) {
 			throw new RuntimeException("Error creating temporary file", e);
 		} catch (IOException e) {
 			throw new RuntimeException("Error creating temporary file", e);
 		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
 		}
 	}
 
 	@Override
 	public GTVectorDataBinding parse(InputStream input, String mimeType) {
 		return parseXML(input);
 	}
 
 	@Override
 	public Class[] getSupportedInternalOutputDataType() {
 		return new Class[] {GTVectorDataBinding.class} ;
 	}
 
 }
