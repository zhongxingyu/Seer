 /**
  * 
  */
 package models;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.tika.Tika;
 import org.apache.tika.exception.TikaException;
 import org.apache.tika.io.IOUtils;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.mime.MediaType;
 import org.apache.tika.parser.ParseContext;
 import org.apache.tika.sax.ToTextContentHandler;
 import org.xml.sax.SAXException;
 
 import play.Logger;
 import uk.bl.wa.nanite.droid.DroidDetector;
 import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
 import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
 import uk.gov.nationalarchives.droid.core.signature.FileFormat;
 import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;
 
 /**
  * @author Andrew Jackson <Andrew.Jackson@bl.uk>
  *
  */
 public class Inspection {
 	
 	private Metadata metadata;
 
 	private String contentType;
 
 	private String serverContentType;
 	
 	private MediaType droidContentType;
 
 	private String text;
 	
 	private long size;
 	
 	private String binary;
 	private String binaryText;
 	
 	private String binaryHead;
 	private String binaryHeadText;
 	
 	private String binaryTail;
 	private String binaryTailText;
 
 	
 
 	/* --- */
 	
 	private static Tika tika = new Tika();
 	
 	private static DroidDetector dd;
 	
 	static {
 		try {
 			dd = new DroidDetector();
 		} catch (CommandExecutionException e) {
 			Logger.error("Could not instanciate the Droid Detector: "+e);
 			e.printStackTrace();
 		}
 	}
 	
 	public static FFSignatureFile getDroidBinarySignatures() {
 		return dd.getBinarySignatures();
 	}
 	
 	public static ContainerSignatureDefinitions getDroidContainerSignatures() {
 		return dd.getContainerSignatures();
 	}
 	
 	public static List<FileFormat> getDroidFormatsForMediaType( MediaType mediaType ) {
 		List<FileFormat> ffs = new ArrayList<FileFormat>();
 		for( FileFormat dff : Inspection.getDroidBinarySignatures().getFileFormatCollection().getFileFormats() ) { 
 			if( mediaType.getBaseType().toString().equalsIgnoreCase(dff.getMimeType()) ) {
 				if( mediaType.getParameters().containsKey("version") ) {
 					MediaType droidType = MediaType.parse(dff.getMimeType()+"; version=\""+dff.getVersion()+"\"");
 					if( mediaType.equals(droidType)) {
 						ffs.add(dff);
 						//Logger.info("Versioned type matching: "+droidType);
 					} else {
 						//Logger.info("Versioned type did not match: "+droidType);
 					}
 				} else {
 					ffs.add(dff);
 					//Logger.info("Type matching: "+dff.getPUID());
 				}
 			}
 		}
 		return ffs;
 	}
 
 	/*
 	 * TODO Move the following two utilities into DroidDetector. 
 	 */
 	
 	protected static MediaType getMimeTypeFromFileFormat( FileFormat dff ) {
 		return getMimeTypeFromResults(dff.getMimeType(), dff.getVersion(), dff.getPUID(), dff.getName(), 1);
 	}
 
 	protected static MediaType getMimeTypeFromResults( String mimeType, String version, String puid, String name, int numMatches ) {
 		if( mimeType != null && ! "".equals(mimeType.trim()) ) {
 			// This sometimes has ", " separated multiple types
 			String[] mimeTypeList = mimeType.split(", ");
 			// Taking first (highest priority) MIME type:
 			mimeType = mimeTypeList[0];
 			// Fix case where no base type is supplied (e.g. "vnd.wordperfect"):
 			if( mimeType.indexOf('/') == -1 ) 
 				mimeType = "application/" + mimeType;
 		}
 		// Build a MediaType
 		MediaType mediaType = MediaType.parse(mimeType);
 		Map<String,String> parameters = null;
 		// Is there a MIME Type?
 		if( mimeType != null && ! "".equals(mimeType) ) {
 			parameters = new HashMap<String,String>(mediaType.getParameters());
 			// Patch on a version parameter if there isn't one there already:
 			if( parameters.get("version") == null && 
 					version != null && (! "".equals(version)) &&
 					// But ONLY if there is ONLY one result.
 					numMatches == 1 ) {
 				parameters.put("version", version);
 			}
 		} else {
 			parameters = new HashMap<String,String>();
 			// If there isn't a MIME type, make one up:
 			String id = "puid-"+puid.replace("/", "-");
 			name = name.replace("\"","'");
 			// Lead with the PUID:
 			mediaType = MediaType.parse("application/x-"+id);
 			parameters.put("name", name);
 			// Add the version, if set:
 			if( version != null && !"".equals(version) && !"null".equals(version) ) {
 				parameters.put("version", version);
 			}
 		}
 		
 		return new MediaType(mediaType,parameters);
 	}
 	
 		/**
 		 * TODO Currently opens the connection twice. In the future, this should probably shift to the same core as the warc-discovery indexer, as that would enhance the metadata and prevent multiple downloads.
 		 * 
 		 * @param url
 		 * @return
 		 * @throws IOException
 		 * @throws SAXException
 		 * @throws TikaException
 		 */
 		public Inspection( String url ) throws IOException, SAXException, TikaException {
 			ParseContext context = new ParseContext();
 		    StringWriter content = new StringWriter();
 			
 			metadata = new Metadata();
 			metadata.add(Metadata.RESOURCE_NAME_KEY, url);
 			URL urlResource = new URL(url);
 			URLConnection urlConnection = urlResource.openConnection();
 			serverContentType = urlConnection.getContentType();
 			
 			// Detect:
 		    byte[] bytes = IOUtils.toByteArray(urlConnection.getInputStream());
 			contentType = tika.detect(bytes, url);
			Logger.info("Tika.detect input type: "+contentType);
			// Allow for case where text/plain overrides more specific type accidentally:
			String extContentType = tika.detect(url);
			Logger.info("TEST: "+extContentType);
			if( contentType.startsWith("text/plain") && ! extContentType.equals("application/octet-stream") )
				contentType = extContentType;
				
		    // Binary display
 			int binaryMax = 256;
 			if ( bytes.length < 2*binaryMax ) {
 				this.binary = Base64.encodeBase64String(bytes);
 				this.binaryText = "Hexadecimal representation of all "+bytes.length+" bytes.";
 			} else {
 				this.binaryHead = Base64.encodeBase64String(Arrays.copyOf(bytes,binaryMax));
 				this.binaryHeadText = "Hexadecimal representation of the first [0.."+binaryMax+"] bytes.";
 				this.binaryTail = Base64.encodeBase64String(Arrays.copyOfRange(bytes,bytes.length-binaryMax, bytes.length));
 				this.binaryTailText = "Hexadecimal representation of the last ["+(bytes.length-binaryMax)+".."+(bytes.length)+"] bytes.";
 			}
 			// Also store the size
 			this.size = bytes.length;
 			
 			// Parse it:
 			tika.getParser().parse( new ByteArrayInputStream(bytes), new ToTextContentHandler(content), metadata, context );
 			
 			// Also DROID it:
 			droidContentType = dd.detect(new ByteArrayInputStream(bytes), metadata);
 			Logger.info("DROID: " + droidContentType);
 
 			// Store the text:
 			this.text = content.toString().trim();
 			
 		}
 
 		/**
 		 * @return the metadata
 		 */
 		public Metadata getMetadata() {
 			return metadata;
 		}
 
 		/**
 		 * @return the contentType
 		 */
 		public String getContentType() {
 			return contentType;
 		}
 
 		/**
 		 * @return the serverContentType
 		 */
 		public String getServerContentType() {
 			return serverContentType;
 		}
 		
 		/**
 		 * @return the droidContentType
 		 */
 		public String getDroidContentType() {
 			return droidContentType.toString();
 		}
 		
 		/**
 		 * 
 		 * @return size
 		 */
 		public long getSize() {
 			return this.size;
 		}
 
 		/**
 		 * @return the text
 		 */
 		public String getText() {
 			return text;
 		}
 
 		/**
 		 * @return the binary
 		 */
 		public String getBinary() {
 			return binary;
 		}
 
 		/**
 		 * @return the binaryText
 		 */
 		public String getBinaryText() {
 			return binaryText;
 		}
 
 		/**
 		 * @return the binaryHead
 		 */
 		public String getBinaryHead() {
 			return binaryHead;
 		}
 
 		/**
 		 * @return the binaryText
 		 */
 		public String getBinaryHeadText() {
 			return binaryHeadText;
 		}
 
 		/**
 		 * @return the binaryTail
 		 */
 		public String getBinaryTail() {
 			return binaryTail;
 		}
 
 		/**
 		 * @return the binaryText
 		 */
 		public String getBinaryTailText() {
 			return binaryTailText;
 		}
 		
 }
